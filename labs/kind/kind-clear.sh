#!/bin/sh
reg_name='kind-registry'

docker rm -f $reg_name
kind delete cluster --name labs-nv
docker network rm kind
