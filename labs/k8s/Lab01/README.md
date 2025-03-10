# Lab tìm hiểu về Kubernetes

*Giả sử đã cài sẵn Docker, Kind, (Local Registry), kubectl*  

## Kịch bản

- Ứng dụng demo: quản lý sản phẩm sử dụng NodeJS và kết nối đến Postgresql

  - Ứng dụng nodejs bao gồm CRUD triển khai replicate 3.

  - Postgresql replicate 3, dùng Patroni, PG-Pool

- Viết giúp tôi một ứng dụng demo: quản lý nhân viên sử dụng python và kết nối đến MySQL

  - Ứng dụng cũng bao gồm CRUD triển khai replicate 3.

  - MySQL replicate 3, dùng ProxySQL, và công cụ nào để quản lý failover

- Viết giúp tôi một ứng dụng demo: quản lý người dùng sử dụng .NET và kết nối đến Redis

  - Ứng dụng cũng bao gồm CRUD triển khai replicate 3.

  - Redis replicate 3

- Dùng treafik để quản lý các proxy đi ra ngoài, Dùng DuckDNS và Let's Encrypt để bên ngoài máy local của tôi người khác có thể thấy, vd:

  - demo-nv.duckdns.org/products cho quản lý sản phẩm

  - demo-nv.duckdns.org/employees cho quản lý nhân viên.

  - demo-nv.duckdns.org/users cho quản lý người dùng.

  - demo-nv.duckdns.org:5432 kết nối vào portgresql

  - demo-nv.duckdns.org:3306 kết nối vào mysql

  - demo-nv.duckdns.org:6379 kết nối vào Redis

## Các Bước Thực Hiện

### Tạo cluster Kind với 3 node

```bash
# xem file kind-with-registry.sh, trong đây chứa script tạo cluster và cấu hình local registry với kind

chmod +x kind-with-registry.sh
./kind-with-registry.sh
EOF

# Kiểm tra
sudo kubectl cluster-info --context kind-demo-cluster
sudo kubectl get nodes
```

### Cài đặt MetalLB làm LoadBalancer

```bash
# xen metallb-namespace.yaml config
sudo kubectl apply -f metallb-namespace.yaml
sudo kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.14.9/config/manifests/metallb-native.yaml
sudo kubectl apply -f metallb-config.yaml

# Kiểm tra
sudo kubectl get pods -n metallb-system
```

### Cài đăt tự đông cập nhật IP lên DuckDNS

- Tạo subdomain demo-nv.
- Cập nhật token.

```bash
sudo docker run -d --name duckdns -e SUBDOMAINS=demo-nv -e TOKEN=your-duckdns-token -e UPDATE_IP=ipv4 lscr.io/linuxserver/duckdns:latest

sudo docker logs duckdns
```--

### Cài đặt Traefik làm Ingress Controller

```bash
sudo helm repo add traefik https://helm.traefik.io/traefik
sudo helm repo update
sudo helm install traefik traefik/traefik -f traefik-values.yaml --namespace traefik --create-namespace

sudo kubectl get pods -n traefik
sudo kubectl get svc -n traefik

#sudo helm uninstall traefik -n traefik
# Kiểm tra
```

### cấu hình Let's Encrypt qua CRD

```bash
sudo helm repo add jetstack https://charts.jetstack.io
sudo helm repo update
sudo helm install cert-manager jetstack/cert-manager --namespace cert-manager --create-namespace --set installCRDs=true

# nếu có public IP
# sudo kubectl apply -f letsencrypt-issuer.yaml
# sudo kubectl apply -f letsencrypt-certificate.yaml

# Nếu không có public IP
sudo kubectl apply -f letsencrypt-dns01-issuer.yaml
sudo kubectl apply -f letsencrypt-dns01-certificate.yaml

sudo kubectl apply -f traefik-dashboard-ingress.yaml

# Kiêm tra
sudo kubectl get clusterissuer letsencrypt -o yaml

sudo kubectl get certificate -n traefik demo-nv-cert -o yaml
sudo kubectl get secret -n traefik demo-nv-tls

sudo kubectl describe certificate -n traefik demo-nv-cert
sudo kubectl get secret -n traefik demo-nv-tls
```

#### Cài đặt Webhook DuckDNS

```bash
git clone https://github.com/ebrianne/cert-manager-webhook-duckdns.git


sudo kubectl apply -f duckdns-secret.yaml

sudo helm install duckdns-webhook ./cert-manager-webhook-duckdns/deploy/cert-manager-webhook-duckdns \
  -n cert-manager \
  --set duckdns.token="9f4caf6e-217f-4f4b-9ee1-79e25007f3e4" \
  --set clusterIssuer.email="nghiep.voke@gmail.com" \
  --set logLevel=2


# Xóa sau khi cài đặt
sudo helm uninstall duckdns-webhook -n cert-manager
rm -r cert-manager-webhook-duckdns

```

### Cài đặt PostgreSQL với Patroni và PgPool-

```bash
# Xem config trong postgres-values.yaml 
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install postgres bitnami/postgresql-ha -f postgres-values.yaml --namespace product-ns --create-namespace
```

### Cài đặt MySQL với ProxySQL

```bash
# Xem config trong mysql-values.yaml 
helm install mysql bitnami/mysql -f mysql-values.yaml --namespace employee-ns --create-namespace
```

### Cài đặt Redis

```bash
# Xem config trong redis-values.yaml 
helm install redis bitnami/redis -f redis-values.yaml --namespace user-ns --create-namespace
```

#### Triển khai các ứng dụng demo

#### Ứng dụng Quản lý sản phẩm (NodeJS + PostgreSQL)
