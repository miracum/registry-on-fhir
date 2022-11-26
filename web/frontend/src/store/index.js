import Vue from "vue";
import FHIR from "fhirclient";
import fhirpath from "fhirpath";
import Fuse from "fuse.js";
import Constants from "@/const";

export const studies = Vue.observable({
  all: [],
  index: {},
});

export const sites = Vue.observable({
  all: [],
  lookup: {},
});

export const filters = Vue.observable({
  showOnlyRecruiting: true,
  categories: [],
  sites: [],
  query: "",
});

function getResearchStudyAcronym(researchStudy) {
  // this improves sort performance but assumes there's at most one extension
  // in the ResearchStudy
  return researchStudy.extension
    ? researchStudy.extension[0].valueString
    : researchStudy.title;
}

function createFhirClient() {
  let fhirUrl = process.env.VUE_APP_FHIR_URL;
  if (!fhirUrl) {
    // this is an awkward workaround for FHIR.client not accepting relative paths as valid URLs
    fhirUrl = `${window.location.protocol}//${window.location.host}/fhir`;
  }

  return FHIR.client({ serverUrl: fhirUrl });
}

const masterTag = `${Constants.SYSTEM_REGISTRY_STUDY_ROLE}%7Cmaster`;

export const actions = {
  async fetchStudies() {
    const client = createFhirClient();

    const bundlePromise = await client.request(
      `ResearchStudy/?_include=ResearchStudy:site&_tag=${masterTag}` +
        "&_count=250&_pretty=false&status=active",
      {
        pageLimit: 0,
      }
    );
    const bundle = await bundlePromise;

    performance.mark("process-start");
    sites.all = fhirpath.evaluate(
      bundle,
      "Bundle.entry.resource.where(resourceType='Location').distinct()"
    );

    sites.all.forEach((site) => {
      sites.lookup[`Location/${site.id}`] = site;
    });

    let studiesRaw = fhirpath.evaluate(
      bundle,
      "Bundle.entry.resource.where(resourceType='ResearchStudy').distinct()"
    );

    studiesRaw = studiesRaw.sort((a, b) =>
      getResearchStudyAcronym(a).localeCompare(getResearchStudyAcronym(b))
    );

    studiesRaw = Object.freeze(studiesRaw);

    const fuseOptions = {
      includeScore: true,
      shouldSort: true,
      keys: [
        "title",
        "_title.extension.extension.valueString",
        "description",
        "_description.extension.extension.valueString",
        "identifier.value",
        "condition.text",
        "keyword.text",
        "extension.valueString",
      ],
    };

    const index = Fuse.createIndex(fuseOptions.keys, studiesRaw);

    studies.index = new Fuse(studiesRaw, fuseOptions, index);
    studies.all = studiesRaw;
  },
  resetFilters() {
    filters.showOnlyRecruiting = true;
  },
  async getStudyCount() {
    const client = createFhirClient();

    const request =
      `ResearchStudy/?_summary=count&_tag=${masterTag}` +
      "&status=active&_pretty=false&status=active";

    const bundlePromise = await client.request(request);
    const response = await bundlePromise;
    return response.total;
  },
};
