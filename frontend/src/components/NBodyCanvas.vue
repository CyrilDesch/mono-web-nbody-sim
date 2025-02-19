<template>
  <canvas ref="canvas"></canvas>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount, provide } from "vue";
import { useWebSocket } from "../composables/useWebSocket";
import * as BABYLON from "babylonjs";
import "babylonjs-loaders"; // Assure le support de certains loaders Babylon

// 1) Get bodies list via WebSocket
const { bodies } = useWebSocket();

// 2) Babylon references
const canvas = ref<HTMLCanvasElement | null>(null);
let engine: BABYLON.Engine;
let scene: BABYLON.Scene;
let camera: BABYLON.ArcRotateCamera;

// Tableau local de Mesh (un Mesh par Body)
let spheres: BABYLON.Mesh[] = [];

// Scale factor (if your coordinates are very large on the backend)
const scaleFactor = 1;

onMounted(() => {
  if (!canvas.value) return;

  // Initialisation Babylon
  engine = new BABYLON.Engine(canvas.value, true);
  scene = new BABYLON.Scene(engine);

  // Fond noir
  scene.clearColor = new BABYLON.Color4(0, 0, 0, 1);

  // Possible scene sharing with other components
  provide("babylonScene", scene);

  // Orbital camera
  camera = new BABYLON.ArcRotateCamera(
    "camera",
    Math.PI / 2,
    Math.PI / 4,
    1000, // distance initiale
    BABYLON.Vector3.Zero(),
    scene
  );
  camera.maxZ = 10000; // autorise un champ de vision plus large
  camera.attachControl(canvas.value, true);

  // Hemispheric light
  const hemisphericLight = new BABYLON.HemisphericLight(
    "hemiLight",
    new BABYLON.Vector3(0, 1, 0),
    scene
  );
  hemisphericLight.intensity = 0.8;
  hemisphericLight.diffuse = new BABYLON.Color3(0.7, 0.7, 1);

  // Point light
  const pointLight = new BABYLON.PointLight(
    "pointLight",
    new BABYLON.Vector3(50, 50, 50),
    scene
  );
  pointLight.intensity = 1.2;
  pointLight.diffuse = new BABYLON.Color3(1, 0.8, 0.6);

  // Effet de glow
  const glowLayer = new BABYLON.GlowLayer("glow", scene);
  glowLayer.intensity = 0.5;

  // Boucle de rendu
  engine.runRenderLoop(() => {
    scene.render();
  });

  // Gestion du redimensionnement
  window.addEventListener("resize", onResize);
  onResize(); // adjust size immediately
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", onResize);
  engine.dispose();
});

function onResize() {
  if (canvas.value) {
    canvas.value.width = window.innerWidth;
    canvas.value.height = window.innerHeight;
  }
  engine.resize();
}

/**
 * Observation de bodies (provenant du WebSocket).
 * À chaque mise à jour :
 *   - on ajuste le nombre de sphères (en supprime ou en ajoute si besoin)
 *   - on met à jour position, taille, couleur...
 */
watch(bodies, (newBodies) => {
  // 1) Si on a plus de Mesh que de bodies, on en supprime
  while (spheres.length > newBodies.length) {
    const sphere = spheres.pop();
    sphere?.dispose();
  }

  // 2) For each body, either update existing sphere or create a new one
  newBodies.forEach((body: any, index: number) => {
    // Diameter = body.radius (e.g.) or default 4.0
    const diameter = body.radius || 4.0;

    if (spheres[index]) {
      // -- Update existing sphere --
      const sphere = spheres[index];

      // Position
      sphere.position.set(
        body.x * scaleFactor,
        body.y * scaleFactor,
        body.z * scaleFactor
      );

      // If you want to update size dynamically:
      // Reset scale then recalculate it
      const currentBoundingSize =
        sphere.getBoundingInfo().boundingBox.maximum.x * 2;
      if (currentBoundingSize !== 0) {
        const scaleFactorSphere = diameter / currentBoundingSize;
        sphere.scaling.set(
          scaleFactorSphere,
          scaleFactorSphere,
          scaleFactorSphere
        );
      }

      // Optional: update material (color, etc.) if needed
      if (body.blackHole) {
        (sphere.material as BABYLON.PBRMetallicRoughnessMaterial).baseColor =
          new BABYLON.Color3(0, 0, 0);
      } else {
        // ...
      }
    } else {
      // -- Create new sphere --
      const sphere = BABYLON.MeshBuilder.CreateSphere(
        "sphere",
        { diameter, segments: 16 },
        scene
      );
      sphere.position.set(
        body.x * scaleFactor,
        body.y * scaleFactor,
        body.z * scaleFactor
      );

      // PBR Material
      const material = new BABYLON.PBRMetallicRoughnessMaterial(
        "material",
        scene
      );
      if (body.blackHole) {
        material.baseColor = new BABYLON.Color3(0, 0, 0); // Trou noir
        material.metallic = 1.0;
        material.roughness = 0.0;
        material.emissiveColor = new BABYLON.Color3(0.1, 0.1, 0.1);
      } else {
        material.baseColor = new BABYLON.Color3(
          Math.random(),
          Math.random(),
          Math.random()
        );
        material.metallic = 1.0;
        material.roughness = 0.3;
        material.emissiveColor = new BABYLON.Color3(0.3, 0.3, 0.8);
      }
      sphere.material = material;

      // Store the new sphere
      spheres.push(sphere);
    }
  });
});
</script>

<style scoped>
canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: block;
}
</style>
