import { ref } from "vue";

const WS_URL = "ws://localhost:8080/nbody";

// Variable qui stocke l'instance unique
let instance: ReturnType<typeof createWebSocketInstance> | null = null;

function createWebSocketInstance(initialFPS: number) {
  const bodies = ref([]);
  const timestamp = ref(0);
  let socket: WebSocket | null = null;

  const updateFPS = (fps: number) => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({ fps, type: "fps" }));
    } else {
      console.warn("Impossible d'envoyer le FPS, WebSocket non connectée");
    }
  };

  const connect = () => {
    if (socket) return; // Prevent new connection if already connected
    socket = new WebSocket(WS_URL);

    socket.onopen = () => {
      console.log("WebSocket connectée");
      updateFPS(initialFPS); // Envoi du FPS initial lors de la connexion
    };

    socket.onmessage = (event) => {
      const data = JSON.parse(event.data);
      timestamp.value = data.timestamp;
      bodies.value = data.bodies;
    };

    socket.onclose = () => {
      console.log("WebSocket déconnectée");
      socket = null;
    };

    socket.onerror = (error) => console.error("WebSocket erreur:", error);
  };

  // Establish connection when instance is created
  connect();

  return { bodies, timestamp, updateFPS };
}

export function useWebSocket(initialFPS: number = 60) {
  // Single instance creation, even if multiple components import it
  if (!instance) {
    instance = createWebSocketInstance(initialFPS);
  }
  return instance;
}
