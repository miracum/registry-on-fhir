<template>
  <div class="search-box">
    <div class="search-icon">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 6.805 6.805" height="25.719" width="25.719">
        <path
          d="M2.646 0a2.65 2.65 0 012.646 2.646c0 .633-.223 1.214-.596 1.67l2.109 2.108-.38.38-2.109-2.108a2.625 2.625 0 01-1.67.596A2.65 2.65 0 010 2.646 2.65 2.65 0 012.646 0zm0 .53A2.113 2.113 0 00.529 2.645c0 1.172.945 2.116 2.117 2.116a2.113 2.113 0 002.117-2.116A2.113 2.113 0 002.646.529z"
          fill="#314659" />
      </svg>
    </div>
    <input id="search" v-model="query" name="search" autocomplete="off" type="text" placeholder="Studien durchsuchen..."
      aria-label="Suche" @input="onInput" />
  </div>
</template>

<script>
import debounce from "lodash.debounce";

export default {
  name: "SearchBar",
  props: {
    value: {
      default: () => "",
      type: String,
    },
  },
  data() {
    return {
      query: "",
    };
  },
  watch: {
    value() {
      this.query = this.value;
    },
  },
  methods: {
    onInput: debounce(function onDebounce(e) {
      this.$emit("input", e.target.value);
    }, 350),
  },
};
</script>

<style scoped>
.search-box {
  position: relative;
  width: 100%;
  max-width: 560px;
  height: 60px;
  border-radius: 120px;
  margin: 0 auto;
  background-color: #ffffff;
}

.search-icon {
  position: absolute;
  top: 0;
  height: 60px;
  width: 86px;
  line-height: 70px;
  text-align: center;
  left: 0;
  pointer-events: none;
  font-size: 1.22em;
}

#search {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 120px;
  border: none;
  background: rgba(255, 255, 255, 0);
  padding: 0 68px 0 68px;
  font-size: 1.32em;
  font-weight: 400;
  letter-spacing: -0.015em;
  outline: none;
}
</style>
