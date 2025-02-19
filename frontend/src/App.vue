<template>
  <div class="app-container">
    <SimulationView />
  </div>
</template>

<script setup lang="ts">
import SimulationView from "./views/SimulationView.vue";
</script>

<style scoped>
.app-container {
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
