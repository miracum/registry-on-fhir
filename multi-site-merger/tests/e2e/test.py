import logging
import os

import fhirclient.models.researchstudy as r
import pytest
import requests
from fhirclient import client
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

LOG = logging.getLogger(__name__)

FHIR_SERVER_URL = os.environ.get("FHIR_SERVER_URL", "http://localhost:8082/fhir")
MERGER_UPDATE_URL = os.environ.get(
    "MERGER_UPDATE_URL", "http://localhost:8080/updates/trigger"
)


@pytest.fixture(scope="session", autouse=True)
def wait_for_server_to_be_up_and_trigger_merge(smart):
    s = requests.Session()
    retries = Retry(total=15, backoff_factor=5, status_forcelist=[502, 503, 504])
    s.mount("http://", HTTPAdapter(max_retries=retries))

    LOG.info(
        f"FHIR_SERVER_URL: {FHIR_SERVER_URL}",
    )

    response = s.get(FHIR_SERVER_URL + "/metadata")
    if response.status_code != 200:
        pytest.fail("Failed to wait for server to be up")

    # trigger merge
    LOG.info(f"Triggering merge update on: {MERGER_UPDATE_URL}")
    r = s.post(os.environ["MERGER_UPDATE_URL"])

    if r.status_code != 200:
        pytest.fail("Failed to trigger merge update")


@pytest.fixture
def smart():
    settings = {
        "app_id": "registry-on-fhir-e2e",
        "api_base": FHIR_SERVER_URL,
    }
    smart = client.FHIRClient(settings=settings)
    return smart


def test_creates_expected_number_of_research_studys(smart):
    search = r.ResearchStudy.where(struct={})
    rs = search.perform_resources(smart.server)
    assert len(rs) == 4
