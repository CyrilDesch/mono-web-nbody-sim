<template>
  <div class="controls" data-v-usewebsocket>
    <div class="fps-control">
      <label for="fps">FPS:</label>
      <input type="range" id="fps" v-model.number="fps" min="1" max="60" @change="updateFps" />
      <span>{{ fps }}</span>
    </div>

    <div class="body-controls">
      <h3>Générateur de corps</h3>
      
      <div class="input-group">
        <div class="slider-container">
          <label for="numBodies">Nombre de corps:</label>
          <input type="range" id="numBodies" v-model.number="numBodies" min="1" max="100" step="1" />
          <span>{{ numBodies }}</span>
        </div>
      </div>

      <div class="input-group">
        <div class="slider-container">
          <label for="range">Plage de positions (±):</label>
          <input type="range" id="range" v-model.number="positionRange" min="10" max="500" step="10" />
          <span>{{ positionRange }}</span>
        </div>
      </div>

      <div class="input-group">
        <div class="checkbox-container">
          <label for="blackHole">Trou noir:</label>
          <input type="checkbox" id="blackHole" v-model="newBody.blackHole" @change="onBlackHoleChange" />
        </div>
      </div>

      <button @click="generateBodies" class="primary">Générer les corps</button>
      <button @click="resetSimulation" class="secondary">Réinitialiser</button>
    </div>

    <div class="body-list" v-if="bodies.length > 0">
      <h3>Corps existants</h3>
      <div v-for="(body, index) in bodies" :key="index" class="body-item">
        <span>Corps {{ index }} ({{ body.blackHole ? 'Trou noir' : 'Normal' }}, masse: {{ body.mass }})</span>
        <button @click="deleteBody(index)">Supprimer</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useWebSocket } from '../composables/useWebSocket';

const { sendMessage, bodies } = useWebSocket();

const fps = ref(60);
const numBodies = ref(10);
const positionRange = ref(100);

const defaultBody = {
  mass: 1,
  blackHole: false
};

const newBody = reactive({ ...defaultBody });

function updateFps() {
  sendMessage({ type: 'fps', fps: Number(fps.value) });
}

function onBlackHoleChange() {
  if (newBody.blackHole) {
    newBody.mass = 5e5; // Masse par défaut pour un trou noir
  } else {
    newBody.mass = 1; // Masse par défaut pour un corps normal
  }
}

function resetForm() {
  Object.assign(newBody, defaultBody);
  numBodies.value = 10;
  positionRange.value = 100;
}

function generateRandomNumber(min: number, max: number): number {
  return Math.random() * (max - min) + min;
}

function generateBodies() {
  const bodyToSend = {
    type: 'createBodies',
    count: numBodies.value,
    range: positionRange.value,
    blackHole: newBody.blackHole
  };
  
  sendMessage(bodyToSend);
}

function resetSimulation() {
  sendMessage({ type: 'reset' });
}

function deleteBody(index: number) {
  sendMessage({
    type: 'deleteBody',
    index: Number(index)
  });
}
</script>

<style scoped>
.controls {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  background: rgba(8, 12, 24, 0.85);
  color: #e0e7ff;
  padding: 15px;
  z-index: 1000;
  backdrop-filter: blur(10px);
  border-right: 1px solid rgba(99, 179, 237, 0.3);
  box-shadow: 5px 0 20px rgba(99, 179, 237, 0.2);
  width: 260px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.controls h3 {
  color: #63b3ed;
  font-size: 1rem;
  margin: 10px 0;
  text-transform: uppercase;
  letter-spacing: 1px;
  text-shadow: 0 0 10px rgba(99, 179, 237, 0.5);
}

.fps-control {
  margin-bottom: 15px;
  padding: 10px;
  background: rgba(26, 32, 44, 0.4);
  border-radius: 8px;
  border: 1px solid rgba(99, 179, 237, 0.2);
}

.fps-control label {
  display: block;
  margin-bottom: 5px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin-bottom: 10px;
  padding: 10px;
  background: rgba(26, 32, 44, 0.4);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.slider-container {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.slider-container span {
  font-size: 0.9em;
  opacity: 0.9;
}

input[type="range"] {
  -webkit-appearance: none;
  width: 100%;
  height: 4px;
  background: rgba(99, 179, 237, 0.2);
  border-radius: 2px;
  outline: none;
}

input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 14px;
  height: 14px;
  background: #63b3ed;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 0 10px rgba(99, 179, 237, 0.8);
  transition: all 0.2s ease;
}

button {
  background: linear-gradient(45deg, #4299e1, #63b3ed);
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 600;
  font-size: 0.8em;
  margin-bottom: 8px;
  width: 100%;
  box-shadow: 0 0 15px rgba(99, 179, 237, 0.3);
}

button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 0 20px rgba(99, 179, 237, 0.5);
}

button:active:not(:disabled) {
  transform: translateY(1px);
}

button:disabled {
  background: #2d3748;
  cursor: not-allowed;
  opacity: 0.6;
}

button.secondary {
  background: linear-gradient(45deg, #2d3748, #4a5568);
}

button.secondary:hover:not(:disabled) {
  background: linear-gradient(45deg, #3d4756, #5a6576);
}

.body-controls {
  margin-bottom: 15px;
}

.body-list {
  flex: 1;
  overflow-y: auto;
  margin-top: 10px;
  padding-right: 5px;
}

.body-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px;
  background: rgba(26, 32, 44, 0.4);
  border-radius: 6px;
  margin-bottom: 8px;
  border: 1px solid rgba(99, 179, 237, 0.1);
  font-size: 0.9em;
}

.body-item span {
  word-break: break-word;
}

.body-item button {
  background: linear-gradient(45deg, #e53e3e, #fc8181);
  margin: 0;
  padding: 6px 10px;
}

.checkbox-container {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  background: rgba(26, 32, 44, 0.4);
  border-radius: 6px;
}

.checkbox-container input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  appearance: none;
  -webkit-appearance: none;
  background: rgba(26, 32, 44, 0.8);
  border: 2px solid #63b3ed;
  border-radius: 4px;
  position: relative;
  transition: all 0.3s ease;
}

.checkbox-container input[type="checkbox"]:checked {
  background: #63b3ed;
  box-shadow: 0 0 15px rgba(99, 179, 237, 0.5);
}

.checkbox-container input[type="checkbox"]:checked::after {
  content: '✓';
  position: absolute;
  color: white;
  font-size: 14px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

::-webkit-scrollbar {
  width: 4px;
}

::-webkit-scrollbar-track {
  background: rgba(26, 32, 44, 0.4);
}

::-webkit-scrollbar-thumb {
  background: #63b3ed;
  border-radius: 2px;
}
</style>
