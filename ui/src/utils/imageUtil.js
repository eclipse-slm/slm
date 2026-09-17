function detectMimeTypeFromBase64 (base64Data) {
    if (base64Data.startsWith('iVBORw0KGgo')) {
        return 'image/png'
    }

    if (base64Data.startsWith('/9j/')) {
        return 'image/jpeg'
    }

    if (base64Data.startsWith('R0lGOD')) {
        return 'image/gif'
    }

    if (base64Data.startsWith('UklGR')) {
        return 'image/webp'
    }

    if (base64Data.startsWith('PHN2Zy') || base64Data.startsWith('PD94bWwg')) {
        return 'image/svg+xml'
    }

    return 'image/jpeg'
}

export default function getImageUrl (imageData) {
    if (imageData != null && imageData !== 'noImage' && imageData !== '') {
        if (imageData.startsWith('data:')) {
            return imageData
        }

        const normalizedImageData = imageData.replace(/\s/g, '')
        const mimeType = detectMimeTypeFromBase64(normalizedImageData)
        return `data:${mimeType};base64,${normalizedImageData}`
    }

    return 'noImage'
}
