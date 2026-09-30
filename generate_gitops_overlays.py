import os

repo_dir = "ralsei-gitops-config"

def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        f.write(content.strip() + "\n")

# Ingress
write_file(f"{repo_dir}/k8s/base/ingress/kustomization.yaml", """
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
  - ingress.yaml
""")

write_file(f"{repo_dir}/k8s/base/ingress/ingress.yaml", """
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ralsei-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
    - host: dev.ralsei.local
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: ralsei-fe
                port:
                  number: 80
          - path: /staff
            pathType: Prefix
            backend:
              service:
                name: ralsei-fe-staff
                port:
                  number: 80
          - path: /api
            pathType: Prefix
            backend:
              service:
                name: ralsei-be
                port:
                  number: 8080
""")

write_file(f"{repo_dir}/k8s/base/kustomization.yaml", """
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
  - backend
  - frontend
  - frontend-staff
  - mssql
  - redis
  - ingress
""")

# Dev Overlay
write_file(f"{repo_dir}/k8s/overlays/dev/kustomization.yaml", """
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namespace: ralsei-dev
resources:
  - ../../base
  - namespace.yaml
  - pv.yaml
patches:
  - path: patches/mssql-pv.yaml
  - path: patches/redis-pv.yaml
images:
  - name: ralsei/ralsei-coach-house-be
    newName: ralsei/ralsei-coach-house-be
    newTag: latest
""")

write_file(f"{repo_dir}/k8s/overlays/dev/namespace.yaml", """
apiVersion: v1
kind: Namespace
metadata:
  name: ralsei-dev
""")

write_file(f"{repo_dir}/k8s/overlays/dev/pv.yaml", """
apiVersion: v1
kind: PersistentVolume
metadata:
  name: mssql-pv-dev
spec:
  capacity:
    storage: 10Gi
  accessModes:
    - ReadWriteOnce
  persistentVolumeReclaimPolicy: Retain
  storageClassName: local-storage-dev
  hostPath:
    path: /mnt/data/ralsei-dev-mssql
---
apiVersion: v1
kind: PersistentVolume
metadata:
  name: redis-pv-dev
spec:
  capacity:
    storage: 2Gi
  accessModes:
    - ReadWriteOnce
  persistentVolumeReclaimPolicy: Retain
  storageClassName: local-storage-dev
  hostPath:
    path: /mnt/data/ralsei-dev-redis
""")

write_file(f"{repo_dir}/k8s/overlays/dev/patches/mssql-pv.yaml", """
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mssql-data
spec:
  storageClassName: local-storage-dev
""")

write_file(f"{repo_dir}/k8s/overlays/dev/patches/redis-pv.yaml", """
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: redis-data
spec:
  storageClassName: local-storage-dev
""")

# Staging Overlay
write_file(f"{repo_dir}/k8s/overlays/staging/kustomization.yaml", """
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namespace: ralsei-staging
resources:
  - ../../base
  - namespace.yaml
images:
  - name: ralsei/ralsei-coach-house-be
    newName: ralsei/ralsei-coach-house-be
    newTag: latest
""")
write_file(f"{repo_dir}/k8s/overlays/staging/namespace.yaml", """
apiVersion: v1
kind: Namespace
metadata:
  name: ralsei-staging
""")

# Prod Overlay
write_file(f"{repo_dir}/k8s/overlays/prod/kustomization.yaml", """
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namespace: ralsei-prod
resources:
  - ../../base
  - namespace.yaml
images:
  - name: ralsei/ralsei-coach-house-be
    newName: ralsei/ralsei-coach-house-be
    newTag: latest
patches:
  - path: patches/replicas.yaml
""")
write_file(f"{repo_dir}/k8s/overlays/prod/namespace.yaml", """
apiVersion: v1
kind: Namespace
metadata:
  name: ralsei-prod
""")
write_file(f"{repo_dir}/k8s/overlays/prod/patches/replicas.yaml", """
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ralsei-be
spec:
  replicas: 2
""")

