# Ralsei backend Kubernetes deployment

The Jenkins pipeline applies this directory to the `default` namespace, then
sets `deployment/ralsei-be` container `ralsei-be` to the exact image tag from
the build. The image in `deployment.yaml` is only a bootstrap value; do not
deploy it manually before replacing the image.

Create the runtime Secret before the first deployment. Do not commit it, and
use an external secret manager in production.

```sh
kubectl -n default create secret generic ralsei-be-runtime \
  --from-literal=SPRING_DATASOURCE_URL='jdbc:sqlserver://<host>:1433;databaseName=VeXeDB;encrypt=true;trustServerCertificate=false;' \
  --from-literal=SPRING_DATASOURCE_USERNAME='<database-user>' \
  --from-literal=SPRING_DATASOURCE_PASSWORD='<database-password>' \
  --from-literal=SPRING_DATA_REDIS_HOST='<redis-host>' \
  --from-literal=JWT_SECRET='<jwt-secret>' \
  --from-literal=SEPAY_API_TOKEN='<sepay-token>' \
  --from-literal=GOONG_API_KEY='<goong-api-key>' \
  --from-literal=MAIL_USERNAME='<smtp-user>' \
  --from-literal=MAIL_PASSWORD='<smtp-password>' \
  --from-literal=MAIL_FROM='<from-address>'
```

The service is internal (`ClusterIP`) on port 8000 and forwards to the
container's HTTP port 8080. For a local Kind smoke test:

```sh
kubectl -n default port-forward service/ralsei-be 8000:8000
```

The probes intentionally use TCP. Spring Security does not currently permit
an unauthenticated HTTP actuator health endpoint, so an HTTP probe would
return an authorization error instead of describing process health.
