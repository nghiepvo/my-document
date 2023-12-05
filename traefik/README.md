```shell
helm install traefik traefik/traefik --values values.yaml -n traefik --create-namespace

helm upgrade --install traefik traefik/traefik --values values.yaml -n traefik

# cert manager
helm repo add jetstack https://charts.jetstack.io

helm upgrade --install \
  cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --version v1.12.1 \
  --set installCRDs=true \
  --values=./jetstack/values.yaml \
  --create-namespace

# C2bquqXhmcN5C00mEnYasT6Zpz4c1wj7pbSA2-p2

helm uninstall traefik -n traefik

#
```