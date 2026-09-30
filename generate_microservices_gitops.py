import os

repo_dir = "ralsei-gitops-config"

def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        f.write(content.strip() + "\n")

services = {
    "auth-service": 8081,
    "customer-service": 8082,
    "staff-service": 8083,
    "driver-service": 8084,
    "payment-service": 8080,
}

resources_list = []

for svc, port in services.items():
    resources_list.append(f"  - {svc}")
    write_file(f"{repo_dir}/k8s/base/{svc}/kustomization.yaml", f"""
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
  - deployment.yaml
  - service.yaml
""")
    write_file(f"{repo_dir}/k8s/base/{svc}/deployment.yaml", f"""
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {svc}
  labels:
    app.kubernetes.io/name: {svc}
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: {svc}
  template:
    metadata:
      labels:
        app.kubernetes.io/name: {svc}
    spec:
      containers:
        - name: {svc}
          image: ralsei/{svc}:latest
          imagePullPolicy: IfNotPresent
          ports:
            - name: http
              containerPort: {port}
              protocol: TCP
          envFrom:
            - configMapRef:
                name: ralsei-be-config
            - secretRef:
                name: ralsei-be-runtime
          resources:
            requests:
              cpu: 100m
              memory: 256Mi
            limits:
              cpu: 500m
              memory: 512Mi
""")
    write_file(f"{repo_dir}/k8s/base/{svc}/service.yaml", f"""
apiVersion: v1
kind: Service
metadata:
  name: {svc}
  labels:
    app.kubernetes.io/name: {svc}
spec:
  ports:
    - name: http
      port: {port}
      targetPort: http
  selector:
    app.kubernetes.io/name: {svc}
""")

# Update kustomization.yaml
with open(f"{repo_dir}/k8s/base/kustomization.yaml", "w") as f:
    f.write("""
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
  - frontend
  - frontend-staff
  - mssql
  - redis
  - ingress
""" + "\n".join(resources_list) + "\n")

