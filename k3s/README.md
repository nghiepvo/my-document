```shell
k3sup install --ip 192.168.1.156 --user root --ssh-key ~/.ssh/id_rsa_k3s
k3sup join --ip 192.168.1.157 --server-ip 192.168.1.156 --user root --ssh-key ~/.ssh/id_rsa_k3s
k3sup join --ip 192.168.1.158 --server-ip 192.168.1.156 --user root --ssh-key ~/.ssh/id_rsa_k3s
```