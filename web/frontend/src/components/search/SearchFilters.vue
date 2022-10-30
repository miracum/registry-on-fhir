<template>
  <div class="search-filters has-text-white">
    <SearchBar v-model="filters.query" />

    <div v-if="sitesChunked.length" class="filter-tabs">
      <h2 class="title is-6 has-text-white">Nach Standort filtern:</h2>
      <div class="columns">
        <div v-for="(sitesChunk, chunkIndex) in sitesChunked" :key="'chunk-' + chunkIndex" class="column is-half">
          <div v-for="(site, siteIndex) in sitesChunk" :key="siteIndex" class="field">
            <b-checkbox v-model="filters.sites" :native-value="`Location/${site.id}`" type="is-success">{{ site.name }}
            </b-checkbox>
          </div>
        </div>
      </div>

      <div class="level">
        <div class="level-left">
          <div class="level-item">
            <p id="result-count">
              {{ filteredStudies.length }}
              <span v-if="filteredStudies.length == 1">Ergebnis</span>
              <span v-else>Ergebnisse</span>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import fhirpath from "fhirpath";
import { studies, filters, sites } from "@/store";
import SearchBar from "@/components/search/SearchBar.vue";

export default {
  name: "SearchFilters",
  components: {
    SearchBar,
  },
  props: {
    filteredStudies: {
      default: () => [],
      type: Array,
    },
  },
  data() {
    return {
      errorMessage: "",
      activeTab: 0,
      filteredTags: [],
      selectedCategories: [],
    };
  },
  computed: {
    filters: () => filters,
    studies: () => studies.all,
    categories() {
      const categoriesList = fhirpath.evaluate(
        this.studies,
        "keyword.distinct().select(coding.code | text)"
      );

      // categoriesList is an array of the form
      // [categoryACode, categoryAText, categoryBCode, categoryBText, ...]
      // this transforms the list into an array of the form
      // [{code: categoryACode, text: categoryAText}, {code: categoryBCode, text: categoryBText}]
      // which is better suited for displaying
      const categories = [];
      for (let i = 0; i < categoriesList.length / 2; i += 2) {
        categories.push({
          code: categoriesList[i],
          text: categoriesList[i + 1],
        });
      }
      return categories;
    },
    sitesChunked() {
      const chunks = [];
      const chunkSize = 5;
      for (let i = 0, j = sites.all.length; i < j; i += chunkSize) {
        chunks.push(sites.all.slice(i, i + chunkSize));
      }
      return chunks;
    },
  },
  methods: {
    getFilteredTags(text) {
      this.filteredTags = this.categories.filter((option) => option.text.toLowerCase().indexOf(text.toLowerCase()) >= 0);
    },
    onTagChanged() {
      filters.categories = this.selectedCategories.map((t) => t.code);
    },
  },
};
</script>

<style scoped>
.filter-tabs {
  margin-top: 1rem;
}
</style>
