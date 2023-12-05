```shell
# refer: https://sujitpatel.in/article/backup-and-restore-kubernetes-with-velero/
# check new version on https://github.com/vmware-tanzu/velero/releases
wget https://github.com/vmware-tanzu/velero/releases/download/v1.12.2/velero-v1.12.2-linux-amd64.tar.gz

tar -xvzf velero-v1.12.2-linux-amd64.tar.gz 

chmod +x velero-v1.12.2-linux-amd64/velero

mv velero-v1.12.2-linux-amd64/velero /usr/local/bin/

which velero

velero version

rm -rf velero-v1.12.2-linux-amd64

rm velero-v1.12.2-linux-amd64.tar.gz

velero install \
    --provider aws \
    --plugins velero/velero-plugin-for-aws:v1.8.2 \
    --bucket velero-backup \
    --backup-location-config region=local-velero-backup,s3ForcePathStyle=true,s3Url=http://192.168.1.111:9000 \
    --snapshot-location-config region=local-velero-snapshot,s3ForcePathStyle=true,s3Url=http://192.168.1.111:9000 \
    --secret-file ./credentials

kubectl -n velero logs deployment/velero

velero uninstall
```