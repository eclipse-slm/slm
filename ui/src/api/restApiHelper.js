export default function logRequestError (error) {
    console.error(error);
    if (error.response) {
        console.error(`Request failed \n${error.response.config.method.toString().toUpperCase()} ${error.response.data.path} \nStatus-Code: ${error.response.status} ${error.response.statusText.toString().toUpperCase()} \nMessage: ${error.response.data.message}`);
        console.error(error);
    } else if (error.request) {
        console.error(error.request);
    } else {
        console.error('Error', error.message);
    }
}
