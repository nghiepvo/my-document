```shell
## install on worker node
systemctl status iscsid

apt install -y nfs-common

## install with helm

helm show values longhorn/longhorn > values.yaml

# update default defaultClassReplicaCount

helm install longhorn longhorn/longhorn --values values.yaml -n longhorn-storage --create-namespace --version 1.5.3

helm upgrade --install longhorn longhorn/longhorn --values values.yaml -n longhorn-storage


# Create a Secret for basic auth

htpasswd -c registry-auth-file admin

kubectl -n longhorn-storage create secret generic registry-basic-auth --from-file=registry-auth-file

kubectl -n longhorn-storage apply -f m-basic-auth.yaml

echo -n http://192.168.1.111:9000 | base64

echo -n longhorn | base64

echo -n Mypassword | base64

# update on longhorn setting
# Backup Target
# s3://backup@local-1/longhorn

# Backup Target Credential Secret
# minio-secret

kubectl -n longhorn-storage apply -f minio-secret.yaml

# update on longhorn setting
# Backup Target
# s3://backup@local-1/longhorn

# Backup Target Credential Secret
# minio-secret

kubectl apply -f deploy-test.yaml
kubectl get pvc
kubectl get pv
kubectl -n longhorn-storage get volumes

k exec -it pod/mypod -- bash

kubectl delete -f deploy-test.yaml
```