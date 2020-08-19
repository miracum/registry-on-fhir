<template>
  <div>
    <study-card class="study-list-item" v-for="study in items" :study="study" :key="study.id" />
  </div>
</template>

<script>
import StudyCard from "@/components/studies/StudyCard.vue";

export default {
  name: "StudyList",
  components: {
    StudyCard,
  },
  props: {
    items: {},
  },
};
</script>

<style scoped>
.study-list-item {
  margin-bottom: 1rem;
}
</style>
