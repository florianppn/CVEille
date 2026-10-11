/**
 * CVEille • Date Formatting & Math Utilities
 */

/**
 * Formats an ISO publish date into a compact date + time string: "YYYY-MM-DD (HH:MM)".
 * @param {string} isoDate - e.g. "2026-10-10T20:16:32.947"
 * @returns {string} e.g. "2026-10-10 (20:16)"
 */
export function formatPublishDate(isoDate) {
  if (!isoDate || typeof isoDate !== 'string') {
    return '—';
  }
  const datePart = isoDate.substring(0, 10);
  const timePart = isoDate.length >= 16 ? isoDate.substring(11, 16) : '';
  return timePart ? `${datePart} (${timePart})` : datePart;
}

/**
 * Formats a synchronization ISO timestamp in French format with UTC marker.
 * @param {string} isoDate - e.g. "2026-10-11T16:13:40.061Z"
 * @returns {string} e.g. "11/10/2026 16:13 UTC"
 */
export function formatSyncDate(isoDate) {
  if (!isoDate) {
    return 'Sync inconnue';
  }
  const d = new Date(isoDate);
  if (isNaN(d.getTime())) {
    return 'Date invalide';
  }
  const formatted = d.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
  return `${formatted} UTC`;
}

/**
 * Checks if a given ISO date timestamp is within the last N hours.
 * @param {string} isoDate
 * @param {number} hours - default 48
 * @returns {boolean}
 */
export function isWithinPastHours(isoDate, hours = 48) {
  if (!isoDate) return false;
  const time = new Date(isoDate).getTime();
  if (isNaN(time)) return false;
  const threshold = Date.now() - (hours * 60 * 60 * 1000);
  return time >= threshold;
}
