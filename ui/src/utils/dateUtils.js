export default function formatDate (dateAsIsoString) {
    if (!dateAsIsoString) return '';
    const date = new Date(dateAsIsoString);
    return new Intl.DateTimeFormat('en-UK', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
    }).format(date);
}

export function formatDateTime(dateAsIsoString) {
    if (!dateAsIsoString) return '';
    const date = new Date(dateAsIsoString);
    return new Intl.DateTimeFormat('en-UK', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
    }).format(date);
}