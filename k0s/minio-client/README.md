```shell
wget https://dl.min.io/client/mc/release/linux-amd64/mc
chmod +x mc
mc alias set minio-local/ http://192.168.1.111:9000 minio Mypassword

# https://www.suse.com/c/rancher_blog/using-minio-as-backup-target-for-rancher-longhorn-2/

mc ls minio-local

mc mb minio-local/backup/longhorn

mc admin user add minio-local longhorn Mypassword

mc admin policy create minio-local backups-policy ./backups-policy.json

mc admin policy attach minio-local backups-policy --user longhorn

echo -n http://192.168.1.111:9000 | base64

echo -n longhorn | base64

echo -n Mypassword | base64

# update on longhorn setting
# Backup Target
# s3://backup@local-1/longhorn

# Backup Target Credential Secret
# minio-secret


kubectl apply -f pvc-test.yaml
kubectl get pvc
kubectl get pv
kubectl -n longhorn-storage get volumes

kubectl apply -f pod-test.yaml
```