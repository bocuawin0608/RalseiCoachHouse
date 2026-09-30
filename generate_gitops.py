import os
import shutil

repo_dir = "ralsei-gitops-config"

def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        f.write(content.strip() + "\n")

# Base - backend
write_file(f"{repo_dir}/k8s/base/backend/kustomization.yaml", """
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
  - deployment.yaml
  - service.yaml
  - configmap.yaml
  - secret.yaml
""")

write_file(f"{repo_dir}/k8s/base/backend/configmap.yaml", """
apiVersion: v1
kind: ConfigMap
metadata:
  name: ralsei-be-config
data:
  SPRING_PROFILES_ACTIVE: "dev"
  SERVER_PORT: "8080"
  SPRING_DATASOURCE_URL: "jdbc:sqlserver://ralsei-mssql:1433;databaseName=VeXeDB;encrypt=true;trustServerCertificate=true;"
  SPRING_DATASOURCE_USERNAME: "sa"
  SPRING_DATASOURCE_DRIVER_CLASS_NAME: "com.microsoft.sqlserver.jdbc.SQLServerDriver"
  SPRING_DATA_REDIS_HOST: "ralsei-redis"
  SPRING_DATA_REDIS_PORT: "6379"
""")

write_file(f"{repo_dir}/k8s/base/backend/secret.yaml", """
apiVersion: v1
kind: Secret
metadata:
  name: ralsei-be-runtime
type: Opaque
stringData:
  SPRING_DATASOURCE_PASSWORD: "CHANGE_ME"
  JWT_SECRET: "CHANGE_ME"
  MAIL_USERNAME: "CHANGE_ME"
  MAIL_PASSWORD: "CHANGE_ME"
""")

write_file(f"{repo_dir}/k8s/base/backend/deployment.yaml", """
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ralsei-be
  labels:
    app.kubernetes.io/name: ralsei-be
    app.kubernetes.io/component: backend
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: ralsei-be
      app.kubernetes.io/component: backend
  template:
    metadata:
      labels:
        app.kubernetes.io/name: ralsei-be
        app.kubernetes.io/component: backend
    spec:
      initContainers:
        - name: wait-for-mssql
          image: busybox:1.36
          command: ['sh', '-c', 'until nc -z ralsei-mssql 1433; do echo waiting for mssql; sleep 2; done']
        - name: wait-for-redis
          image: busybox:1.36
          command: ['sh', '-c', 'until nc -z ralsei-redis 6379; do echo waiting for redis; sleep 2; done']
      containers:
        - name: ralsei-be
          image: ralsei/ralsei-coach-house-be:latest
          imagePullPolicy: IfNotPresent
          ports:
            - name: http
              containerPort: 8080
              protocol: TCP
          envFrom:
            - configMapRef:
                name: ralsei-be-config
            - secretRef:
                name: ralsei-be-runtime
          resources:
            requests:
              cpu: 250m
              memory: 512Mi
            limits:
              cpu: "1"
              memory: 1Gi
          startupProbe:
            httpGet:
              path: /actuator/health/liveness
              port: http
            failureThreshold: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: http
            initialDelaySeconds: 5
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: http
            initialDelaySeconds: 30
            periodSeconds: 20
""")

write_file(f"{repo_dir}/k8s/base/backend/service.yaml", """
apiVersion: v1
kind: Service
metadata:
  name: ralsei-be
  labels:
    app.kubernetes.io/name: ralsei-be
    app.kubernetes.io/component: backend
spec:
  ports:
    - name: http
      port: 8080
      targetPort: http
  selector:
    app.kubernetes.io/name: ralsei-be
    app.kubernetes.io/component: backend
""")

# Base - frontend (Customer)
write_file(f"{repo_dir}/k8s/base/frontend/kustomization.yaml", """
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
  - deployment.yaml
  - service.yaml
""")

write_file(f"{repo_dir}/k8s/base/frontend/deployment.yaml", """
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ralsei-fe
  labels:
    app.kubernetes.io/name: ralsei-fe
    app.kubernetes.io/component: frontend
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: ralsei-fe
      app.kubernetes.io/component: frontend
  template:
    metadata:
      labels:
        app.kubernetes.io/name: ralsei-fe
        app.kubernetes.io/component: frontend
    spec:
      containers:
        - name: ralsei-fe
          image: ralsei-coachhouse-fe:latest
          imagePullPolicy: IfNotPresent
          ports:
            - name: http
              containerPort: 80
              protocol: TCP
          resources:
            requests:
              cpu: 100m
              memory: 128Mi
            limits:
              cpu: 250m
              memory: 256Mi
""")

write_file(f"{repo_dir}/k8s/base/frontend/service.yaml", """
apiVersion: v1
kind: Service
metadata:
  name: ralsei-fe
  labels:
    app.kubernetes.io/name: ralsei-fe
    app.kubernetes.io/component: frontend
spec:
  ports:
    - name: http
      port: 80
      targetPort: http
  selector:
    app.kubernetes.io/name: ralsei-fe
    app.kubernetes.io/component: frontend
""")

# Base - frontend (Staff)
write_file(f"{repo_dir}/k8s/base/frontend-staff/kustomization.yaml", """
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
  - deployment.yaml
  - service.yaml
""")

write_file(f"{repo_dir}/k8s/base/frontend-staff/deployment.yaml", """
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ralsei-fe-staff
  labels:
    app.kubernetes.io/name: ralsei-fe-staff
    app.kubernetes.io/component: frontend-staff
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: ralsei-fe-staff
      app.kubernetes.io/component: frontend-staff
  template:
    metadata:
      labels:
        app.kubernetes.io/name: ralsei-fe-staff
        app.kubernetes.io/component: frontend-staff
    spec:
      containers:
        - name: ralsei-fe-staff
          image: ralsei-coachhouse-fe-staff:latest
          imagePullPolicy: IfNotPresent
          ports:
            - name: http
              containerPort: 80
              protocol: TCP
          resources:
            requests:
              cpu: 100m
              memory: 128Mi
            limits:
              cpu: 250m
              memory: 256Mi
""")

write_file(f"{repo_dir}/k8s/base/frontend-staff/service.yaml", """
apiVersion: v1
kind: Service
metadata:
  name: ralsei-fe-staff
  labels:
    app.kubernetes.io/name: ralsei-fe-staff
    app.kubernetes.io/component: frontend-staff
spec:
  ports:
    - name: http
      port: 80
      targetPort: http
  selector:
    app.kubernetes.io/name: ralsei-fe-staff
    app.kubernetes.io/component: frontend-staff
""")

# Base - database (mssql)
write_file(f"{repo_dir}/k8s/base/mssql/kustomization.yaml", """
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
  - statefulset.yaml
  - service.yaml
  - pvc.yaml
""")

write_file(f"{repo_dir}/k8s/base/mssql/pvc.yaml", """
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mssql-data
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
""")

write_file(f"{repo_dir}/k8s/base/mssql/statefulset.yaml", """
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: ralsei-mssql
  labels:
    app.kubernetes.io/name: ralsei-mssql
    app.kubernetes.io/component: database
spec:
  serviceName: ralsei-mssql
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: ralsei-mssql
      app.kubernetes.io/component: database
  template:
    metadata:
      labels:
        app.kubernetes.io/name: ralsei-mssql
        app.kubernetes.io/component: database
    spec:
      containers:
        - name: ralsei-mssql
          image: mcr.microsoft.com/mssql/server:2022-latest
          imagePullPolicy: IfNotPresent
          env:
            - name: ACCEPT_EULA
              value: "Y"
            - name: MSSQL_SA_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: ralsei-be-runtime
                  key: SPRING_DATASOURCE_PASSWORD
          ports:
            - name: mssql
              containerPort: 1433
              protocol: TCP
          volumeMounts:
            - name: mssql-data
              mountPath: /var/opt/mssql
          resources:
            requests:
              cpu: 500m
              memory: 1Gi
            limits:
              cpu: "2"
              memory: 2Gi
          readinessProbe:
            exec:
              command:
                - /bin/sh
                - -c
                - /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P "$MSSQL_SA_PASSWORD" -Q "SELECT 1" -b -d master -t 3
            initialDelaySeconds: 15
            periodSeconds: 10
            timeoutSeconds: 5
            failureThreshold: 5
          livenessProbe:
            exec:
              command:
                - /bin/sh
                - -c
                - /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P "$MSSQL_SA_PASSWORD" -Q "SELECT 1" -b -d master -t 3
            initialDelaySeconds: 30
            periodSeconds: 20
            timeoutSeconds: 5
            failureThreshold: 3
      volumes:
        - name: mssql-data
          persistentVolumeClaim:
            claimName: mssql-data
""")

write_file(f"{repo_dir}/k8s/base/mssql/service.yaml", """
apiVersion: v1
kind: Service
metadata:
  name: ralsei-mssql
  labels:
    app.kubernetes.io/name: ralsei-mssql
    app.kubernetes.io/component: database
spec:
  ports:
    - name: mssql
      port: 1433
      targetPort: mssql
  selector:
    app.kubernetes.io/name: ralsei-mssql
    app.kubernetes.io/component: database
""")

# Base - redis
write_file(f"{repo_dir}/k8s/base/redis/kustomization.yaml", """
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
  - statefulset.yaml
  - service.yaml
  - pvc.yaml
""")

write_file(f"{repo_dir}/k8s/base/redis/pvc.yaml", """
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: redis-data
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 2Gi
""")

write_file(f"{repo_dir}/k8s/base/redis/statefulset.yaml", """
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: ralsei-redis
  labels:
    app.kubernetes.io/name: ralsei-redis
    app.kubernetes.io/component: cache
spec:
  serviceName: ralsei-redis
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: ralsei-redis
      app.kubernetes.io/component: cache
  template:
    metadata:
      labels:
        app.kubernetes.io/name: ralsei-redis
        app.kubernetes.io/component: cache
    spec:
      containers:
        - name: ralsei-redis
          image: redis:7
          imagePullPolicy: IfNotPresent
          ports:
            - name: redis
              containerPort: 6379
              protocol: TCP
          volumeMounts:
            - name: redis-data
              mountPath: /data
          resources:
            requests:
              cpu: 100m
              memory: 128Mi
            limits:
              cpu: 500m
              memory: 512Mi
      volumes:
        - name: redis-data
          persistentVolumeClaim:
            claimName: redis-data
""")

write_file(f"{repo_dir}/k8s/base/redis/service.yaml", """
apiVersion: v1
kind: Service
metadata:
  name: ralsei-redis
  labels:
    app.kubernetes.io/name: ralsei-redis
    app.kubernetes.io/component: cache
spec:
  ports:
    - name: redis
      port: 6379
      targetPort: redis
  selector:
    app.kubernetes.io/name: ralsei-redis
    app.kubernetes.io/component: cache
""")

