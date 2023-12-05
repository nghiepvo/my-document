```shell
# Create a Secret for basic auth

htpasswd -c registry-auth-file admin

kubectl create secret generic registry-basic-auth --from-file=registry-auth-file

kubectl apply -f m-basic-auth.yaml

kubectl apply -f nginx-deploy-main.yaml

kubectl apply -f nginx-service-main.yaml

kubectl apply -f nginx-deploy-main-traefik-ingress-route.yaml
```