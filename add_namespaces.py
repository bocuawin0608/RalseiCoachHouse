import os
import yaml

overlays = "ralsei-gitops-config/k8s/overlays"
for env in ["dev", "staging", "prod"]:
    ns = {
        "apiVersion": "v1",
        "kind": "Namespace",
        "metadata": {"name": f"ralsei-{env}"}
    }
    with open(os.path.join(overlays, env, "namespace.yaml"), "w") as f:
        yaml.dump(ns, f, default_flow_style=False, sort_keys=False)
    
    # Add namespace.yaml to resources
    kust_file = os.path.join(overlays, env, "kustomization.yaml")
    with open(kust_file, "r") as f:
        data = yaml.safe_load(f)
    data["resources"].append("namespace.yaml")
    with open(kust_file, "w") as f:
        yaml.dump(data, f, default_flow_style=False, sort_keys=False)

