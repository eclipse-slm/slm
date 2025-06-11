export default function formatDate (dateAsIsoString) {
    if (!dateAsIsoString) return '';
    const date = new Date(dateAsIsoString);
    return new Intl.DateTimeFormat('en-UK', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
    }).format(date);
}