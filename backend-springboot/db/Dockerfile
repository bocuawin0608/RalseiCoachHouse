FROM mcr.microsoft.com/mssql/server:2022-latest

USER root

WORKDIR /usr/src/app

COPY ddl.sql Procedure.sql fakedata1.sql entrypoint.sh /usr/src/app/

RUN chmod +x /usr/src/app/entrypoint.sh && chown -R mssql /usr/src/app

USER mssql

ENTRYPOINT ["/usr/src/app/entrypoint.sh"]
