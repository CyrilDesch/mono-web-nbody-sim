<template>
  <div class="controls">
    <label>FPS:</label>
    <input type="number" v-model="fps" min="1" max="60" @change="changeFPS" />
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useWebSocket } from "../composables/useWebSocket";

// Valeur initiale du FPS
const fps = ref(60);

// Get the unique instance of the WebSocket connection
const { updateFPS } = useWebSocket(fps.value);

const changeFPS = () => {
  updateFPS(fps.value);
};
</script>

<style scoped>
.controls {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 10px;
  border-radius: 5px;
}
</style>
