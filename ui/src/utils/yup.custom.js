function yupValidateIPv4(message = 'Invalid IP address') {
    return this.matches(/(^(\d{1,3}\.){3}(\d{1,3})$)/, {
        message,
        excludeEmptyString: true
    }).test('ip', message, value => {
        return value === undefined || value.trim() === '' ? true
            : value.split('.').find(i => parseInt(i, 10) > 255) === undefined;
    });
}

export default yupValidateIPv4;