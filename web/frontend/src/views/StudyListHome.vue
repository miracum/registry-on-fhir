<template>
  <div class="study-list-home">
    <section class="hero">
      <div class="hero-body has-text-centered">
        <div class="container">
          <h1 class="title is-size-1 is-size-4-mobile has-text-white">
            Übersicht über die an den MIRACUM-Standorten laufenden klinischen Studien.</h1>
        </div>
      </div>
    </section>
    <section class="search-section">
      <search-filters :filtered-studies="filteredStudies" />
    </section>

    <section class="study-list-section">
      <b-loading :active="isLoading" :is-full-page="false" />
      <template v-if="!isLoading">
        <b-message v-if="failedToLoad" type="is-danger">
          Studien konnten nicht geladen werden:
          <br />
          <pre>{{ errorMessage }}</pre>
        </b-message>
        <b-message v-else-if="noStudies" type="is-warning">Keine Studien vorhanden.</b-message>
        <study-list :items="filteredStudies" />
      </template>
    </section>
  </div>
</template>

<script>
import fhirpath from "fhirpath";
// import ICountUp from "vue-countup-v2";
import StudyList from "@/components/studies/StudyList.vue";
import SearchFilters from "@/components/search/SearchFilters.vue";
import { studies, filters, actions } from "@/store";

export default {
  name: "StudyListHome",
  components: {
    StudyList,
    SearchFilters,
    // ICountUp,
  },
  data() {
    return {
      failedToLoad: false,
      isLoading: true,
      errorMessage: "",
      countUp: {
        delay: 500,
        endVal: 200,
        options: {
          useEasing: true,
          useGrouping: true,
          separator: ",",
          decimal: ".",
          prefix: "",
          suffix: "",
          duration: 10,
        },
      },
    };
  },
  computed: {
    studies: () => studies.all,
    noStudies: () => !studies.all || studies.all.length === 0,
    filteredStudies() {
      let filtered = studies.all;

      const query = filters.query.toLowerCase();
      if (query) {
        const result = studies.index.search(query);
        filtered = result.map((match) => match.item);
      }

      if (filters.showOnlyRecruiting) {
        filtered = filtered.filter((study) => study.status === "active");
      }

      if (filters.sites.length > 0) {
        filtered = filtered.filter((study) => {
          const studySites = fhirpath.evaluate(
            study,
            "ResearchStudy.site.reference"
          );

          return studySites.some((filterSite) =>
            filters.sites.includes(filterSite)
          );
        });
      }

      if (filters.categories.length > 0) {
        filtered = filtered.filter((study) => {
          const studyCategories = fhirpath.evaluate(
            study,
            "ResearchStudy.keyword.coding.code"
          );

          return filters.categories.some((categoryCode) =>
            studyCategories.includes(categoryCode)
          );
        });
      }

      return filtered;
    },
  },
  async created() {
    // used for testing the loading screen if the server is too fast...
    // await new Promise(resolve => setTimeout(resolve, 5000));

    try {
      const total = await actions.getStudyCount();
      if (total) {
        this.countUp.endVal = total;
      }
    } catch (exc) {
      // eslint-disable-next-line no-console
      console.log(exc);
    }

    // await new Promise((resolve) => setTimeout(resolve, 5000));
    try {
      await actions.fetchStudies();
    } catch (exc) {
      this.errorMessage = exc;
      this.failedToLoad = true;
      // eslint-disable-next-line no-console
      console.log(exc);
    } finally {
      this.isLoading = false;
    }
  },
};
</script>

<style scoped>
a.miracum-link {
  color: #ffffff;
  font-weight: 600;
}

a.miracum-link:hover {
  text-decoration: underline;
}

.search-section {
  margin-bottom: 0.75rem;
}

.study-list-section {
  position: relative;
  min-height: 100px;
}

.hero-body {
  padding-top: 1rem;
  padding-bottom: 1.5rem;
}
</style>
