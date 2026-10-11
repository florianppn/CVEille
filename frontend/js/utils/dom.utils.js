/**
 * CVEille • DOM & Security Utilities
 * Zero dependencies, reusable across components
 */

/**
 * Escapes characters with HTML entities to prevent XSS vulnerabilities.
 * @param {*} str - Input string or value
 * @returns {string} Sanitized string
 */
export function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
