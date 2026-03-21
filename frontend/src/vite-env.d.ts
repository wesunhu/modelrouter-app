/**
 * Vite TypeScript environment declarations.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
