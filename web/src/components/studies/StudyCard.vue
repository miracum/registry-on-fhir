<template>
  <div class="card study-card">
    <div class="card-content">
      <div class="columns study-card-header">
        <div class="column">
          <p class="title is-size-4 is-size-5-mobile">
            {{ studyAcronym || studyTitle }}
          </p>
        </div>
        <div
          class="column is-2 has-text-right has-text-left-mobile has-text-weight-semibold"
        >
          <b-tag
            class="recruitment-status"
            :type="getTagTypeFromRecruitmentStatus(study.status)"
            >{{ statusText }}</b-tag
          >
        </div>
      </div>
      <p class="study-title">{{ studyTitle }}</p>
    </div>
    <b-collapse :open="false" :aria-id="study.id" class="has-text-centered">
      <b-button
        rounded
        outlined
        class="button is-primary more-info-button"
        slot="trigger"
        :aria-controls="study.id"
        @click.prevent="isExpanded = !isExpanded"
      >
        <template v-if="isExpanded">weitere Informationen ausblenden</template>
        <template v-else>weitere Informationen einblenden</template>
      </b-button>
      <div class="notification has-text-left">
        <div class="content">
          <template v-if="studyDescription">
            <h3 class="is-size-5">Beschreibung</h3>
            <p>{{ studyDescription }}</p>
          </template>

          <template v-if="study.condition">
            <h3 class="is-size-5">Studieninhalt</h3>
            <ul>
              <li v-for="(condition, index) in study.condition" :key="index">
                {{
                  condition.text ||
                  condition.coding[0].display ||
                  condition.coding[0].code
                }}
              </li>
            </ul>
          </template>

          <template v-if="study.keyword">
            <h3 class="is-size-5">Kategorien</h3>
            <ul>
              <li v-for="(keyword, index) in keywords" :key="index">
                {{ keyword }}
              </li>
            </ul>
          </template>

          <template v-if="study.site">
            <h3 class="is-size-5">Kontakt</h3>

            <div
              v-for="(contactChunk, index) in siteContacts"
              :key="'chunk-' + index"
              class="columns is-desktop"
            >
              <div
                v-for="(site, index) in contactChunk"
                :key="index"
                class="column"
              >
                <strong>{{ site.name }}</strong>
                <br />
                <b-tag
                  class="has-text-weight-semibold"
                  :type="getTagTypeFromRecruitmentStatus(site.status)"
                  >{{ site.statusText }}</b-tag
                >
                <br />
                <template v-if="site.contact">
                  {{ site.contact.name }}
                  <ul class="list-unstyled">
                    <li
                      v-for="(telecom, comIndex) in site.contact.telecom"
                      :key="comIndex"
                    >
                      <a
                        v-if="telecom.system == 'email'"
                        :href="`mailto:${telecom.value}?subject=Anfrage%20bez??glich%20${studyAcronym}%20Studie`"
                      >
                        {{ telecom.value }}
                      </a>
                      <a
                        v-else-if="telecom.system == 'phone'"
                        :href="`tel:${telecom.value}`"
                      >
                        {{ telecom.value }}
                      </a>
                    </li>
                  </ul>
                </template>
              </div>
            </div>
          </template>

          <template v-if="study.relatedArtifact">
            <h3 class="is-size-5">Weiterf??hrende Links</h3>
            <ul class="list-unstyled">
              <li
                v-for="(artifact, index) in study.relatedArtifact"
                :key="index"
              >
                <a v-if="artifact.url" :href="artifact.url">{{
                  artifact.display || artifact.label || artifact.url
                }}</a>
                <p v-else>{{ artifact.display || artifact.label }}</p>
              </li>
            </ul>
          </template>
        </div>
      </div>
    </b-collapse>
  </div>
</template>

<script>
import fhirpath from "fhirpath";
import Constants from "@/const";
import { sites } from "@/store";

const statusLookup = {
  active: "Rekrutierung l??uft",
  "administratively-completed": "Studie beendet",
  approved: "Studie ist genehmigt",
  "closed-to-accrual": "Rekrutierung beendet",
  "closed-to-accrual-and-intervention": "Rekrutierung beendet",
  completed: "Studie abgeschlossen",
  disapproved: "Studie wurde abgelehnt",
  "in-review": "Studie wird momentan bewertet",
  "temporarily-closed-to-accrual": "Rekrutierung tempor??r ausgesetzt",
  "temporarily-closed-to-accrual-and-intervention":
    "Rekrutierung tempor??r ausgesetzt",
  withdrawn: "Studie wurde zur??ckgezogen",
  default: "unbekannter Rekrutierungsstatus",
};

export default {
  name: "StudyCard",
  components: {},
  data() {
    return {
      isExpanded: false,
    };
  },
  props: {
    study: {},
  },
  methods: {
    getTagTypeFromRecruitmentStatus(status) {
      if (!status) {
        return "is-warning";
      }
      return status === "active" ? "is-success" : "is-dark";
    },
    getStudyTitle() {
      if (this.study.title) {
        return this.study.title;
      }
      // eslint-disable-next-line no-underscore-dangle
      const titleExtra = this.study._title;

      if (titleExtra) {
        const translation = fhirpath.evaluate(
          titleExtra,
          "extension.where(url=%acronymUrl).extension.where(url='content').valueString",
          {
            acronymUrl: Constants.SYSTEM_TRANSLATION,
          }
        )[0];
        return translation;
      }
      return null;
    },
    getStudyDescription() {
      if (this.study.description) {
        return this.study.description;
      }
      // eslint-disable-next-line no-underscore-dangle
      const descriptionElement = this.study._description;

      if (descriptionElement) {
        const translation = fhirpath.evaluate(
          descriptionElement,
          "extension.where(url=%acronymUrl).extension.where(url='content').valueString",
          {
            acronymUrl: Constants.SYSTEM_TRANSLATION,
          }
        )[0];
        return translation;
      }
      return null;
    },
  },
  computed: {
    siteContacts() {
      const siteVM = this.study.site.map((site) => {
        const contact = fhirpath.evaluate(
          site,
          "extension.where(url=%contactExtUrl).valueContactDetail.first()",
          {
            contactExtUrl: Constants.SYSTEM_SITE_CONTACT_EXTENSION,
          }
        )[0];

        const status = fhirpath.evaluate(
          site,
          "extension.where(url=%statusExtUrl).valueCodeableConcept.coding.code.first()",
          {
            statusExtUrl: Constants.SYSTEM_SITE_STATUS_EXTENSION,
          }
        )[0];

        return {
          name: sites.lookup[site.reference].name,
          status,
          statusText: statusLookup[status] || statusLookup.default,
          contact,
        };
      });
      const chunks = [];
      const chunkSize = 4;
      for (let i = 0, j = siteVM.length; i < j; i += chunkSize) {
        chunks.push(siteVM.slice(i, i + chunkSize));
      }
      return chunks;
    },
    eligibilityCharacteristic() {
      const characteristic = fhirpath.evaluate(
        this.study,
        "contained.first().characteristic"
      );
      return characteristic;
    },
    keywords() {
      if (!this.study.keyword) {
        return [];
      }
      return this.study.keyword
        .filter((keyword) => keyword.text)
        .map((keyword) => keyword.text);
    },
    studyAcronym() {
      const acronym = fhirpath.evaluate(
        this.study,
        "extension.where(url=%acronymUrl).valueString",
        {
          acronymUrl: Constants.SYSTEM_STUDY_ACRONYM,
        }
      )[0];

      return acronym;
    },
    studyTitle() {
      return this.getStudyTitle();
    },
    studyDescription() {
      return this.getStudyDescription();
    },
    statusText() {
      if (!this.study.status) {
        return statusLookup.default;
      }
      return statusLookup[this.study.status] || statusLookup.default;
    },
  },
};
</script>

<style scoped>
ul.list-unstyled {
  list-style: none;
  margin: 0;
}

.study-card-header {
  margin-bottom: 0rem;
}

.more-info-button {
  margin-bottom: 0.5rem;
}

.category-tags {
  margin-top: 1rem;
}

.study-card {
  border-radius: 4px;
}
</style>
