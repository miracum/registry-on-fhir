import fhirclient.models.researchstudy as r
import logging
import os
import pytest
import requests
from fhirclient import client
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

LOG = logging.getLogger(__name__)


@pytest.fixture(scope="session", autouse=True)
def wait_for_server_to_be_up_and_trigger_merge(request):
    s = requests.Session()
    retries = Retry(total=15, backoff_factor=5, status_forcelist=[502, 503, 504])
    s.mount("http://", HTTPAdapter(max_retries=retries))

    LOG.info("FHIR: ", os.environ["FHIR_SERVER_URL"])
    response = s.get(os.environ["FHIR_SERVER_URL"] + "/metadata")

    if response.status_code != 200:
        pytest.fail("Failed to wait for server to be up")

    # trigger merge
    LOG.info("Triggering merge update on: ", os.environ["MERGER_UPDATE_URL"])
    r = s.post(os.environ["MERGER_UPDATE_URL"])

    if r.status_code != 200:
        pytest.fail("Failed to trigger merge update")


@pytest.fixture
def smart():
    settings = {"app_id": "integrationtest", "api_base": os.environ["FHIR_SERVER_URL"]}
    smart = client.FHIRClient(settings=settings)
    return smart


def test_creates_expected_number_of_research_studys(smart):
    search = r.ResearchStudy.where(struct={})
    rs = search.perform_resources(smart.server)
    assert len(rs) == 4
