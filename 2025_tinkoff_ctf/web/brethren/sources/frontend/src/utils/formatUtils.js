/**
 * Formats a ticket title by replacing underscores with spaces and properly capitalizing words
 * @param {string} title - The ticket title to format
 * @returns {string} The formatted title
 */
export const formatTicketTitle = (title) => {
  if (!title) return '';
  return title
    .split('_')
    .join(' ')
    .replace(/\b\w/g, char => char.toUpperCase());
}; 