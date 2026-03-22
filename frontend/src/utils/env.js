// Debug environment variables
console.log('=== Environment Variables Debug ===');
console.log('process.env.VITE_API_URL:', process.env.VITE_API_URL);
console.log('process.env:', process.env);
console.log('import.meta.env:', import.meta.env);

// Export for debugging
export const ENV_DEBUG = {
  VITE_API_URL: process.env.VITE_API_URL,
  NODE_ENV: process.env.NODE_ENV,
  HAS_PROCESS_ENV: !!process.env,
  HAS_IMPORT_META_ENV: !!import.meta.env
}
