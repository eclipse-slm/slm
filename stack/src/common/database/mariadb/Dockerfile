FROM mariadb:10.5

ENV MYSQL_DATABASE=default-schema \
    MYSQL_USER=defaultUser

COPY startup.sh /opt/startup.sh

RUN chmod +x /opt/startup.sh

CMD [ "/opt/startup.sh" ]
