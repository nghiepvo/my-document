```shell
## install on worker node
systemctl status iscsid

apt install -y nfs-common

## install with helm

helm show values longhorn/longhorn > values.yaml

# update default defaultClassReplicaCount

helm install longhorn longhorn/longhorn --values values.yaml -n longhorn-storage --create-namespace --version 1.5.3

helm upgrade --install longhorn longhorn/longhorn --values values.yaml -n longhorn-storage
```