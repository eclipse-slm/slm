export default function getImageUrl (imageData) {
    if (imageData != null && imageData !== 'noImage' && imageData !== '') {
        if (imageData.startsWith('data:')) {
            return imageData
        } else {
            return 'data:image/jpeg;base64,' + imageData
        }
    } else {
        return 'noImage'
    }
}
