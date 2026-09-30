import os
import yaml

base_dir = "ralsei-gitops-config"
k8s_dir = os.path.join(base_dir, "k8s")
base_k8s = os.path.join(k8s_dir, "base")
overlays = os.path.join(k8s_dir, "overlays")

def ensure_dir(path):
    os.makedirs(path, exist_ok=True)

ensure_dir(base_k8s)
ensure_dir(overlays)

# Services definition
services = {
    "auth-service": {"port": 8081, "image": "ralsei/auth-service"},
    "customer-service": {"port": 8082, "image": "ralsei/customer-service"},
    "staff-service": {"port": 8083, "image": "ralsei/staff-service"},
    "driver-service": {"port": 8084, "image": "ralsei/driver-service"},
    "payment-service": {"port": 8080, "image": "ralsei/payment-service"},
    "backend": {"port": 8080, "image": "ralsei/ralsei-coach-house-be"},
    "frontend": {"port": 80, "image": "ralsei-coachhouse-fe"},
    "frontend-staff": {"port": 80, "image": "nhaxetuanmv-fe-staff"}
}

def write_yaml(path, data):
    with open(path, 'w') as f:
        yaml.dump(data, f, default_flow_style=False, sort_keys=False)

def create_service_manifests(name, conf):
    svc_dir = os.path.join(base_k8s, name)
    ensure_dir(svc_dir)
    
    # Deployment
    deployment = {
        "apiVersion": "apps/v1",
        "kind": "Deployment",
        "metadata": {"name": name},
        "spec": {
            "replicas": 1,
            "selector": {"matchLabels": {"app": name}},
            "template": {
                "metadata": {"labels": {"app": name}},
                "spec": {
                    "containers": [{
                        "name": name,
                        "image": f"{conf['image']}:latest",
                        "ports": [{"containerPort": conf['port']}],
                        "envFrom": [{"configMapRef": {"name": f"{name}-config"}}]
                    }]
                }
            }
        }
    }
    
    # Service
    service = {
        "apiVersion": "v1",
        "kind": "Service",
        "metadata": {"name": name},
        "spec": {
            "selector": {"app": name},
            "ports": [{"port": conf['port'], "targetPort": conf['port']}]
        }
    }
    
    # ConfigMap placeholder
    configmap = {
        "apiVersion": "v1",
        "kind": "ConfigMap",
        "metadata": {"name": f"{name}-config"},
        "data": {
            "SPRING_PROFILES_ACTIVE": "dev"
        }
    }
    
    # Add environment specifics
    if "service" in name or name == "backend":
        configmap["data"]["SPRING_DATASOURCE_URL"] = "jdbc:sqlserver://database:1433;databaseName=ralsei_db;encrypt=true;trustServerCertificate=true;"
        configmap["data"]["SPRING_DATASOURCE_USERNAME"] = "sa"
        configmap["data"]["SPRING_DATASOURCE_PASSWORD"] = "01102006Duc."
        configmap["data"]["SPRING_DATA_REDIS_HOST"] = "redis"
        configmap["data"]["SPRING_DATA_REDIS_PORT"] = "6379"
        
    if name == "auth-service":
        configmap["data"]["SPRING_DATASOURCE_URL"] = "jdbc:sqlserver://database:1433;databaseName=auth_db;encrypt=true;trustServerCertificate=true;"
    if name == "customer-service":
        configmap["data"]["SPRING_DATASOURCE_URL"] = "jdbc:sqlserver://database:1433;databaseName=customer_db;encrypt=true;trustServerCertificate=true;"
    if name == "driver-service":
        configmap["data"]["SPRING_DATASOURCE_URL"] = "jdbc:sqlserver://database:1433;databaseName=driver_db;encrypt=true;trustServerCertificate=true;"
    if name == "staff-service":
        configmap["data"]["SPRING_DATASOURCE_URL"] = "jdbc:sqlserver://database:1433;databaseName=staff_db;encrypt=true;trustServerCertificate=true;"

    write_yaml(os.path.join(svc_dir, "deployment.yaml"), deployment)
    write_yaml(os.path.join(svc_dir, "service.yaml"), service)
    write_yaml(os.path.join(svc_dir, "configmap.yaml"), configmap)
    
    kustomization = {
        "apiVersion": "kustomize.config.k8s.io/v1beta1",
        "kind": "Kustomization",
        "resources": ["deployment.yaml", "service.yaml", "configmap.yaml"]
    }
    write_yaml(os.path.join(svc_dir, "kustomization.yaml"), kustomization)

for name, conf in services.items():
    create_service_manifests(name, conf)

# Database StatefulSet
db_dir = os.path.join(base_k8s, "database")
ensure_dir(db_dir)

db_sts = {
    "apiVersion": "apps/v1",
    "kind": "StatefulSet",
    "metadata": {"name": "database"},
    "spec": {
        "serviceName": "database",
        "replicas": 1,
        "selector": {"matchLabels": {"app": "database"}},
        "template": {
            "metadata": {"labels": {"app": "database"}},
            "spec": {
                "containers": [{
                    "name": "mssql",
                    "image": "mcr.microsoft.com/mssql/server:2022-latest",
                    "ports": [{"containerPort": 1433}],
                    "env": [
                        {"name": "ACCEPT_EULA", "value": "Y"},
                        {"name": "MSSQL_SA_PASSWORD", "value": "01102006Duc."}
                    ],
                    "volumeMounts": [{"name": "mssql-data", "mountPath": "/var/opt/mssql"}]
                }]
            }
        },
        "volumeClaimTemplates": [{
            "metadata": {"name": "mssql-data"},
            "spec": {
                "accessModes": ["ReadWriteOnce"],
                "resources": {"requests": {"storage": "5Gi"}}
            }
        }]
    }
}
db_svc = {
    "apiVersion": "v1",
    "kind": "Service",
    "metadata": {"name": "database"},
    "spec": {
        "selector": {"app": "database"},
        "ports": [{"port": 1433, "targetPort": 1433}]
    }
}
write_yaml(os.path.join(db_dir, "statefulset.yaml"), db_sts)
write_yaml(os.path.join(db_dir, "service.yaml"), db_svc)
write_yaml(os.path.join(db_dir, "kustomization.yaml"), {
    "apiVersion": "kustomize.config.k8s.io/v1beta1",
    "kind": "Kustomization",
    "resources": ["statefulset.yaml", "service.yaml"]
})

# Redis StatefulSet
redis_dir = os.path.join(base_k8s, "redis")
ensure_dir(redis_dir)
redis_sts = {
    "apiVersion": "apps/v1",
    "kind": "StatefulSet",
    "metadata": {"name": "redis"},
    "spec": {
        "serviceName": "redis",
        "replicas": 1,
        "selector": {"matchLabels": {"app": "redis"}},
        "template": {
            "metadata": {"labels": {"app": "redis"}},
            "spec": {
                "containers": [{
                    "name": "redis",
                    "image": "redis:alpine",
                    "ports": [{"containerPort": 6379}]
                }]
            }
        }
    }
}
redis_svc = {
    "apiVersion": "v1",
    "kind": "Service",
    "metadata": {"name": "redis"},
    "spec": {
        "selector": {"app": "redis"},
        "ports": [{"port": 6379, "targetPort": 6379}]
    }
}
write_yaml(os.path.join(redis_dir, "statefulset.yaml"), redis_sts)
write_yaml(os.path.join(redis_dir, "service.yaml"), redis_svc)
write_yaml(os.path.join(redis_dir, "kustomization.yaml"), {
    "apiVersion": "kustomize.config.k8s.io/v1beta1",
    "kind": "Kustomization",
    "resources": ["statefulset.yaml", "service.yaml"]
})

# Base kustomization
write_yaml(os.path.join(base_k8s, "kustomization.yaml"), {
    "apiVersion": "kustomize.config.k8s.io/v1beta1",
    "kind": "Kustomization",
    "resources": list(services.keys()) + ["database", "redis"]
})

# Overlays
for env in ["dev", "staging", "prod"]:
    env_dir = os.path.join(overlays, env)
    ensure_dir(env_dir)
    write_yaml(os.path.join(env_dir, "kustomization.yaml"), {
        "apiVersion": "kustomize.config.k8s.io/v1beta1",
        "kind": "Kustomization",
        "namespace": f"ralsei-{env}",
        "resources": ["../../base"],
        "images": [{"name": conf["image"], "newTag": "latest"} for conf in services.values()]
    })

# Readme
with open(os.path.join(base_dir, "README.md"), "w") as f:
    f.write("# GitOps Configuration for Ralsei\n")

