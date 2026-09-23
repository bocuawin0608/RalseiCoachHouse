# Ralsei backend Kubernetes deployment

The Jenkins pipeline applies this directory to the `default` namespace, then
sets `deployment/ralsei-be` container `ralsei-be` to the exact image tag from
the build. The image in `deployment.yaml` is only a bootstrap value; do not
deploy it manually before replacing the image.

Create the runtime Secret before the first deployment. Do not commit it, and
use an external secret manager in production. For a local Kind cluster, copy
the tracked template, replace all placeholders, and create the Secret:

```sh
cp backend-springboot/k8s/runtime-secret.example.env \
  backend-springboot/k8s/runtime-secret.env
# Edit backend-springboot/k8s/runtime-secret.env with real values first.
kubectl -n default create secret generic ralsei-be-runtime \
  --from-env-file=backend-springboot/k8s/runtime-secret.env \
  --dry-run=client -o yaml | kubectl apply -f -
```

If Jenkins reports `Kubernetes API is unavailable or the active kubeconfig
context is invalid`, this is not a Secret problem. Restore the Kind cluster
and its kubeconfig before creating or applying any manifests. For example,
on the Jenkins agent that owns the cluster:

```sh
kind export kubeconfig --name local
kubectl cluster-info
```

The service is internal (`ClusterIP`) on port 8000 and forwards to the
container's HTTP port 8080. For a local Kind smoke test:

```sh
kubectl -n default port-forward service/ralsei-be 8000:8000
```

The probes intentionally use TCP. Spring Security does not currently permit
an unauthenticated HTTP actuator health endpoint, so an HTTP probe would
return an authorization error instead of describing process health.
