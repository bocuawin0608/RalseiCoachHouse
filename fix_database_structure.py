import os
import yaml

db_dir = "ralsei-gitops-config/k8s/base/database"

# Secret
secret = {
    "apiVersion": "v1",
    "kind": "Secret",
    "metadata": {"name": "database-secret"},
    "type": "Opaque",
    "data": {
        "MSSQL_SA_PASSWORD": "MDExMDIwMDZEdWMu" # base64 of 01102006Duc.
    }
}

# PVC
pvc = {
    "apiVersion": "v1",
    "kind": "PersistentVolumeClaim",
    "metadata": {"name": "database-pvc"},
    "spec": {
        "accessModes": ["ReadWriteOnce"],
        "resources": {"requests": {"storage": "5Gi"}}
    }
}

# PV
pv = {
    "apiVersion": "v1",
    "kind": "PersistentVolume",
    "metadata": {"name": "database-pv"},
    "spec": {
        "capacity": {"storage": "5Gi"},
        "accessModes": ["ReadWriteOnce"],
        "hostPath": {"path": "/var/lib/k8s/mssql"}
    }
}

# StatefulSet
sts_file = os.path.join(db_dir, "statefulset.yaml")
with open(sts_file, "r") as f:
    sts = yaml.safe_load(f)

# Modify STS to use PVC and Secret
if "volumeClaimTemplates" in sts["spec"]:
    del sts["spec"]["volumeClaimTemplates"]

sts["spec"]["template"]["spec"]["volumes"] = [{"name": "mssql-data", "persistentVolumeClaim": {"claimName": "database-pvc"}}]
sts["spec"]["template"]["spec"]["containers"][0]["env"] = [
    {"name": "ACCEPT_EULA", "value": "Y"},
    {"name": "MSSQL_SA_PASSWORD", "valueFrom": {"secretKeyRef": {"name": "database-secret", "key": "MSSQL_SA_PASSWORD"}}}
]

with open(os.path.join(db_dir, "secret.yaml"), "w") as f:
    yaml.dump(secret, f, default_flow_style=False, sort_keys=False)
with open(os.path.join(db_dir, "pvc.yaml"), "w") as f:
    yaml.dump(pvc, f, default_flow_style=False, sort_keys=False)
with open(os.path.join(db_dir, "pv.yaml"), "w") as f:
    yaml.dump(pv, f, default_flow_style=False, sort_keys=False)
with open(sts_file, "w") as f:
    yaml.dump(sts, f, default_flow_style=False, sort_keys=False)

kust_file = os.path.join(db_dir, "kustomization.yaml")
with open(kust_file, "r") as f:
    kust = yaml.safe_load(f)
kust["resources"] = ["statefulset.yaml", "service.yaml", "pvc.yaml", "pv.yaml", "secret.yaml"]
with open(kust_file, "w") as f:
    yaml.dump(kust, f, default_flow_style=False, sort_keys=False)

