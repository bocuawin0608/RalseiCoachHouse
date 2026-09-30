import yaml
import os

repo_dir = "ralsei-gitops-config"

services = ["auth-service", "customer-service", "driver-service", "staff-service", "payment-service"]

for env in ["dev", "staging", "prod"]:
    kust_file = f"{repo_dir}/k8s/overlays/{env}/kustomization.yaml"
    with open(kust_file, "r") as f:
        data = yaml.safe_load(f)
    
    if "images" not in data:
        data["images"] = []
    
    # ensure microservices are in images
    for svc in services:
        exists = any(img.get("name") == f"ralsei/{svc}" for img in data["images"])
        if not exists:
            data["images"].append({
                "name": f"ralsei/{svc}",
                "newName": f"ralsei/{svc}",
                "newTag": "latest"
            })
            
    with open(kust_file, "w") as f:
        yaml.dump(data, f, sort_keys=False, default_flow_style=False)

