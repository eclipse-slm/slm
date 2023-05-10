DATABASES = {
    'default': {
        'ATOMIC_REQUESTS': True,
        'ENGINE': 'django.db.backends.postgresql',
        'NAME': "awx",
        'USER': "awx",
        'PASSWORD': "awxpass",
        'HOST': "awx-postgres",
        'PORT': "5432",
    }
}

BROADCAST_WEBSOCKET_SECRET = "TWpYNE9lUWRvcHJwQzFJZU44LWVWdmpvVkNzdDEwa0hxY1VINENVYkMxTGNGNkg5OlpnUWg5OGZwSi1rMlZyZk80LnJCTE8wd2tBVm5pNV9fc2Fkdmp3T0xTcjJiY3ZhUzdzeEJyWDhYd2wyb1RaRjkyNFptXzVqOnN1LTFTaUg="