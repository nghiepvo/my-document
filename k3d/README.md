```shell
k3d cluster create -a 2

docker run -d --name=webtop -e PUID=1000 -e PGID=1000 -e TZ=Etc/UTC -p 3000:3000 -v /Users/nv/repos/webtop:/config \
  -v /var/run/docker.sock:/var/run/docker.sock `#optional` --shm-size="2gb" --restart unless-stopped \
  --network k3d-k3s-default lscr.io/linuxserver/webtop:debian-xfce-version-f39da3d2
```