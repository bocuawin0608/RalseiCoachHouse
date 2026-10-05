#!/bin/bash
file="ralsei-gitops-config/k8s/base/kong/ingress/backend-ingress.yaml"
# Create a temporary file
awk '
BEGIN { in_rules = 0 }
/^spec:/ {
    print $0
    print "  rules:"
    print "  - host: nhaxetuanmv.local"
    print "    http:"
    print "      paths:"
    in_rules = 1
    next
}
/^  rules:/ { next }
/^  - http:/ { next }
/^      paths:/ { next }
{
    if (in_rules == 1 && /^      - path:/) {
        # Print the path for the first host
        print $0
    } else if (in_rules == 1) {
        print $0
    } else {
        print $0
    }
}
' $file > temp.yaml
cp temp.yaml $file
