FROM fabos4ai/awx:15.0.1

USER root

COPY credentials.py /etc/tower/conf.d/credentials.py
COPY environment.sh /etc/tower/conf.d/environment.sh
COPY SECRET_KEY /etc/tower/SECRET_KEY

RUN chmod 777 /etc/tower/SECRET_KEY /etc/tower/conf.d/credentials.py /etc/tower/conf.d/environment.sh /etc/tower/settings.py
