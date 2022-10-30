<template>
  <div>
    <study-card v-for="study in items" :key="study.id" :study="study" class="study-list-item" />
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
    items: {
      default: () => [],
      type: Array
    },
  },
};
</script>

<style scoped>
.study-list-item {
  margin-bottom: 1rem;
}
</style>
