# Setup Solr Cloud

After run docker compose up, you must be run

```shell
docker exec -it xxxxx sh

# you will be input user and password at here. 
# ex: username: solr, password: YourPassword
bin/solr auth enable -type basicAuth -prompt true -z zoo1:2181,zoo2:2181,zoo3:2181


# and update user and password at here 
export SOLR_AUTH_TYPE="basic" && export SOLR_AUTHENTICATION_OPTS="-Dbasicauth=solr:YourPassword" && bin/solr create -c solr_cloud -n data_driven_schema_configs -s 1 -rf 3

#important!: solr_cloud will be use to ping in HAProxy

#Generate Password on site https://clemente-biondo.github.io/ and update into ./haproxy/haproxy.cfg file and replace base64 endcode "Basic c29scjpPaG1pZGFzQDEyMw=="
```