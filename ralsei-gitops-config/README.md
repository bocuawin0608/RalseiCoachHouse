# GitOps Configuration for Ralsei

## Persistent Storage Architecture
For local development (dev overlay), the database utilizes persistent storage configured via `kind`.
The storage path maps as follows:
`host filesystem` -> `kind extraMount` -> `kind node filesystem` -> `PV` -> `PVC` -> `database`

You must configure your `kind` cluster with an `extraMounts` section to expose the host directory to the kind nodes, and the `pv.yaml` will bind to that node directory.
