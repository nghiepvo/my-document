
```shell
kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.13.12/config/manifests/metallb-native.yaml
kubectl apply -f metallb-config.yaml
# Test
kubectl apply -f https://kind.sigs.k8s.io/examples/loadbalancer/usage.yaml


kubectl delete -f https://kind.sigs.k8s.io/examples/loadbalancer/usage.yaml
```