// Debug environment variables





// Export for debugging
export const ENV_DEBUG = {
  VITE_API_URL: process.env.VITE_API_URL,
  NODE_ENV: process.env.NODE_ENV,
  HAS_PROCESS_ENV: !!process.env,
  HAS_IMPORT_META_ENV: !!import.meta.env
}
