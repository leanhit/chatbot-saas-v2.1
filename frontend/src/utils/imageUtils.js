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
  
  // Handle localhost:9000 - convert to backend proxy
  if (url.includes('localhost:9000')) {
    try {
      const urlObj = new URL(url);
      // Extract filename from path (remove /chatbot-files/ prefix)
      const filename = urlObj.pathname.replace('/chatbot-files/', '');
      // Convert to backend proxy URL
      return `https://chat.truyenthongviet.vn/api/images/public/filename/${filename}/content`;
    } catch (e) {
      return url;
    }
  }
  
  // Handle direct chatbot-files URLs - convert to backend proxy
  if (url.includes('chatbot-files/')) {
    try {
      const urlObj = new URL(url);
      // Extract filename from path (remove /chatbot-files/ prefix)
      const filename = urlObj.pathname.replace('/chatbot-files/', '');
      // Convert to backend proxy URL
      return `https://chat.truyenthongviet.vn/api/images/public/filename/${filename}/content`;
    } catch (e) {
      return url;
    }
  }
  
  // Handle Botpress server SSL issues - use proxy for port 9000
  if (url.includes('cwsv.truyenthongviet.vn:9000')) {
    try {
      const urlObj = new URL(url);
      // Check if we're in development or production
      const isDevelopment = process.env.NODE_ENV === 'development' || window.location.hostname === 'localhost';
      if (isDevelopment) {
        // Development: use local proxy with HTTPS
        return `https://localhost:3004/files${urlObj.pathname}${urlObj.search}`;
      } else {
        // Production: use production proxy on same domain with HTTPS
        return `https://cwsv.truyenthongviet.vn/files${urlObj.pathname}${urlObj.search}`;
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
