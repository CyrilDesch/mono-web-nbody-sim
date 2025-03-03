<template>
  <canvas ref="canvas" data-v-usewebsocket></canvas>
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

  // Créer un shader personnalisé pour l'effet de distorsion du trou noir
  BABYLON.Effect.ShadersStore["blackHoleVertexShader"] = `
    precision highp float;
    attribute vec3 position;
    attribute vec2 uv;
    uniform mat4 world;
    uniform mat4 viewProjection;
    varying vec2 vUV;
    void main(void) {
      gl_Position = viewProjection * world * vec4(position, 1.0);
      vUV = uv;
    }
  `;

  BABYLON.Effect.ShadersStore["blackHoleFragmentShader"] = `
    precision highp float;
    varying vec2 vUV;
    uniform float time;
    uniform vec3 accretionColor;
    
    void main(void) {
      vec2 center = vec2(0.5, 0.5);
      float dist = distance(vUV, center);
      
      // Effet de distorsion de la lumière
      float blackHoleRadius = 0.2;
      float accretionDiskRadius = 0.45;
      float outerGlowRadius = 0.7;
      
      if (dist < blackHoleRadius) {
        // Centre noir du trou noir avec un contour plus lumineux
        float edgeGlow = smoothstep(blackHoleRadius - 0.1, blackHoleRadius, dist);
        vec3 edgeColor = vec3(0.4, 0.2, 0.6);  // Violet plus lumineux
        gl_FragColor = vec4(edgeColor * edgeGlow * 2.0, 1.0);
      } else if (dist < accretionDiskRadius) {
        // Disque d'accrétion avec effet de rotation
        float angle = atan(vUV.y - 0.5, vUV.x - 0.5);
        float spiral = sin(angle * 6.0 + time * 2.0);
        float brightness = smoothstep(blackHoleRadius, accretionDiskRadius, dist);
        brightness *= (1.0 + spiral * 0.7);
        
        // Couleurs plus intenses pour le disque d'accrétion
        vec3 innerColor = vec3(1.0, 0.7, 0.2);  // Orange doré
        vec3 outerColor = vec3(1.0, 0.3, 1.0);  // Rose vif
        vec3 diskColor = mix(innerColor, outerColor, brightness);
        
        gl_FragColor = vec4(diskColor * brightness * 2.0, 1.0);
      } else if (dist < outerGlowRadius) {
        // Effet de distorsion lumineux autour du trou noir
        float glow = smoothstep(outerGlowRadius, accretionDiskRadius, dist);
        vec3 glowColor = vec3(0.7, 0.3, 0.7) * glow;
        gl_FragColor = vec4(glowColor, glow);
      } else {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
      }
    }
  `;

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
    if (sphere) {
      sphere.material?.dispose();
      sphere.dispose();
    }
  }

  // 2) For each body, either update existing sphere or create a new one
  newBodies.forEach((body: any, index: number) => {
    const diameter = body.blackHole ? 20.0 : 4.0;

    if (index < spheres.length) {
      // Update existing sphere
      const sphere = spheres[index];
      sphere.position.set(
        body.x * scaleFactor,
        body.y * scaleFactor,
        body.z * scaleFactor
      );

      const currentBoundingSize = sphere.getBoundingInfo().boundingBox.maximum.x * 2;
      if (currentBoundingSize !== 0) {
        const scaleFactorSphere = diameter / currentBoundingSize;
        sphere.scaling.set(scaleFactorSphere, scaleFactorSphere, scaleFactorSphere);
      }

      if (body.blackHole) {
        // Mise à jour du matériau du trou noir si ce n'est pas déjà un shader material
        if (!(sphere.material instanceof BABYLON.ShaderMaterial)) {
          sphere.material?.dispose();
          const shaderMaterial = new BABYLON.ShaderMaterial(
            "blackHole",
            scene,
            {
              vertex: "blackHole",
              fragment: "blackHole",
            },
            {
              attributes: ["position", "uv"],
              uniforms: ["world", "viewProjection", "time", "accretionColor"],
              needAlphaBlending: true
            }
          );
          shaderMaterial.setVector3("accretionColor", new BABYLON.Vector3(1.0, 0.5, 0.0));
          shaderMaterial.backFaceCulling = false;
          sphere.material = shaderMaterial;
        }
        // Mise à jour du temps pour l'animation
        (sphere.material as BABYLON.ShaderMaterial).setFloat("time", performance.now() / 1000.0);
      } else if (sphere.material instanceof BABYLON.ShaderMaterial) {
        // Si c'était un trou noir et ne l'est plus, changer le matériau
        sphere.material.dispose();
        const material = new BABYLON.PBRMetallicRoughnessMaterial("material", scene);
        material.baseColor = new BABYLON.Color3(
          Math.random(),
          Math.random(),
          Math.random()
        );
        material.metallic = 0.8;
        material.roughness = 0.3;
        material.emissiveColor = new BABYLON.Color3(0.1, 0.1, 0.3);
        material.emissiveIntensity = 0.2;
        sphere.material = material;
      }
    } else {
      // Create new sphere
      const sphere = BABYLON.MeshBuilder.CreateSphere(
        "sphere",
        { diameter, segments: 32 },
        scene
      );
      sphere.position.set(
        body.x * scaleFactor,
        body.y * scaleFactor,
        body.z * scaleFactor
      );

      if (body.blackHole) {
        const shaderMaterial = new BABYLON.ShaderMaterial(
          "blackHole",
          scene,
          {
            vertex: "blackHole",
            fragment: "blackHole",
          },
          {
            attributes: ["position", "uv"],
            uniforms: ["world", "viewProjection", "time", "accretionColor"],
            needAlphaBlending: true
          }
        );
        shaderMaterial.setFloat("time", performance.now() / 1000.0);
        shaderMaterial.setVector3("accretionColor", new BABYLON.Vector3(1.0, 0.5, 0.0));
        shaderMaterial.backFaceCulling = false;
        sphere.material = shaderMaterial;
      } else {
        const material = new BABYLON.PBRMetallicRoughnessMaterial("material", scene);
        material.baseColor = new BABYLON.Color3(
          Math.random(),
          Math.random(),
          Math.random()
        );
        material.metallic = 0.8;
        material.roughness = 0.3;
        material.emissiveColor = new BABYLON.Color3(0.1, 0.1, 0.3);
        material.emissiveIntensity = 0.2;
        sphere.material = material;
      }

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
