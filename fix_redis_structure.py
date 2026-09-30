import os
import yaml

redis_dir = "ralsei-gitops-config/k8s/base/redis"

# PVC
pvc = {
    "apiVersion": "v1",
    "kind": "PersistentVolumeClaim",
    "metadata": {"name": "redis-pvc"},
    "spec": {
        "accessModes": ["ReadWriteOnce"],
        "resources": {"requests": {"storage": "1Gi"}}
    }
}

# PV
pv = {
    "apiVersion": "v1",
    "kind": "PersistentVolume",
    "metadata": {"name": "redis-pv"},
    "spec": {
        "capacity": {"storage": "1Gi"},
        "accessModes": ["ReadWriteOnce"],
        "hostPath": {"path": "/var/lib/k8s/redis"}
    }
}

# StatefulSet
sts_file = os.path.join(redis_dir, "statefulset.yaml")
with open(sts_file, "r") as f:
    sts = yaml.safe_load(f)

sts["spec"]["template"]["spec"]["volumes"] = [{"name": "redis-data", "persistentVolumeClaim": {"claimName": "redis-pvc"}}]
sts["spec"]["template"]["spec"]["containers"][0]["volumeMounts"] = [{"name": "redis-data", "mountPath": "/data"}]

with open(os.path.join(redis_dir, "pvc.yaml"), "w") as f:
    yaml.dump(pvc, f, default_flow_style=False, sort_keys=False)
with open(os.path.join(redis_dir, "pv.yaml"), "w") as f:
    yaml.dump(pv, f, default_flow_style=False, sort_keys=False)
with open(sts_file, "w") as f:
    yaml.dump(sts, f, default_flow_style=False, sort_keys=False)

kust_file = os.path.join(redis_dir, "kustomization.yaml")
with open(kust_file, "r") as f:
    kust = yaml.safe_load(f)
kust["resources"] = ["statefulset.yaml", "service.yaml", "pvc.yaml", "pv.yaml"]
with open(kust_file, "w") as f:
    yaml.dump(kust, f, default_flow_style=False, sort_keys=False)

