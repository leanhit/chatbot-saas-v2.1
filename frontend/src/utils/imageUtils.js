/**
 * Image utility functions for handling mixed content and URL normalization
 */
/**
 * Converts HTTP URLs to HTTPS to avoid mixed content errors
 * Also handles SSL protocol errors for specific servers
 * @param {string} url The original URL
 * @returns {string|undefined} The URL with appropriate protocol
 */
export function secureImageUrl(url) {
  if (!url) return undefined;
  
  // Handle localhost:9000 - convert to production API endpoint
  if (url.includes('localhost:9000')) {
    // Extract filename from URL
    const urlObj = new URL(url);
    const filename = urlObj.pathname.split('/').pop();
    const apiUrl = process.env.VITE_API_URL || 'http://localhost:8080/api';
    const baseUrl = apiUrl.endsWith('/') ? apiUrl.slice(0, -1) : apiUrl;
    return `${baseUrl}/images/public/filename/${filename}/content`;
  }
  
  // Handle direct chatbot-files URLs - convert to API endpoint
  if (url.includes('chatbot-files/')) {
    // Extract filename from URL
    const filename = url.split('chatbot-files/').pop().split('?')[0];
    const apiUrl = process.env.VITE_API_URL || 'http://localhost:8080/api';
    const baseUrl = apiUrl.endsWith('/') ? apiUrl.slice(0, -1) : apiUrl;
    return `${baseUrl}/images/public/filename/${filename}/content`;
  }
  
  // Handle Botpress server SSL issues - use proxy for port 9000
  if (url.includes('cwsv.truyenthongviet.vn:9000')) {
    try {
      const urlObj = new URL(url);
      // Check if we're in development or production
      const isDevelopment = process.env.NODE_ENV === 'development' || window.location.hostname === 'localhost';
      if (isDevelopment) {
        // Development: use local proxy
        return `http://localhost:3004/files${urlObj.pathname}${urlObj.search}`;
      } else {
        // Production: use production proxy on same domain
        return `/files${urlObj.pathname}${urlObj.search}`;
      }
    } catch (e) {
      return url;
    }
  }
  // For other URLs, if it starts with http://, convert to https://
  if (url.startsWith('http://')) {
    return url.replace('http://', 'https://');
  }
  return url;
}
/**
 * Gets a secure image URL with fallback
 * @param {string} url The original URL
 * @param {string} fallback Fallback URL or text
 * @returns {string} The secure URL or fallback
 */
export function getSecureImageUrl(url, fallback) {
  const secureUrl = secureImageUrl(url);
  return secureUrl || fallback || '';
}
