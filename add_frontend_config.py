import os
import yaml

base_k8s = "ralsei-gitops-config/k8s/base"
for fe in ["frontend", "frontend-staff"]:
    cm = {
        "apiVersion": "v1",
        "kind": "ConfigMap",
        "metadata": {"name": f"{fe}-config"},
        "data": {
            "VITE_FIREBASE_API_KEY": "AIzaSyC6L7I7N6Wdvzkx_wMFScubaiNDs2hePw4",
            "VITE_FIREBASE_AUTH_DOMAIN": "swp-firebase-dbdba.firebaseapp.com",
            "VITE_FIREBASE_PROJECT_ID": "swp-firebase-dbdba",
            "VITE_FIREBASE_STORAGE_BUCKET": "swp-firebase-dbdba.firebasestorage.app",
            "VITE_FIREBASE_MESSAGING_SENDER_ID": "522469509916",
            "VITE_FIREBASE_APP_ID": "1:522469509916:web:9285df72bec0f97fe7c8d9",
            "VITE_FIREBASE_MEASUREMENT_ID": "G-NC5379FXKE",
            "VITE_USE_FIREBASE_EMULATOR": "false"
        }
    }
    with open(os.path.join(base_k8s, fe, "configmap.yaml"), "w") as f:
        yaml.dump(cm, f, default_flow_style=False, sort_keys=False)
    
    # Update kustomization.yaml
    kust_file = os.path.join(base_k8s, fe, "kustomization.yaml")
    with open(kust_file, "r") as f:
        data = yaml.safe_load(f)
    if "configmap.yaml" not in data.get("resources", []):
        data.setdefault("resources", []).append("configmap.yaml")
    with open(kust_file, "w") as f:
        yaml.dump(data, f, default_flow_style=False, sort_keys=False)
        
    # Update deployment to use configmap
    dep_file = os.path.join(base_k8s, fe, "deployment.yaml")
    with open(dep_file, "r") as f:
        dep = yaml.safe_load(f)
    dep["spec"]["template"]["spec"]["containers"][0]["envFrom"] = [{"configMapRef": {"name": f"{fe}-config"}}]
    with open(dep_file, "w") as f:
        yaml.dump(dep, f, default_flow_style=False, sort_keys=False)

