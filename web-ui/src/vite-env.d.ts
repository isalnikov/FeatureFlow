/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL: string;
  readonly VITE_PLANNING_ENGINE_URL: string;
  readonly VITE_INTEGRATION_HUB_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
