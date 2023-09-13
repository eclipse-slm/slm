const URL = 'http://10.3.7.173:7080/auth/realms/master/protocol/openid-connect/token';

test = async () => {
    const response = await fetch(URL, {
        method: 'POST',
        body: JSON.stringify({
            clientId: 'admin-cli',
            grant_type: 'password',
            username: 'admin',
            password: 'password'
        }),
        headers: {
            'Content-type': 'application/x-www-form-urlencoded'
        }
    });
    const json = await response.json();
    console.log(json);
};

test();
