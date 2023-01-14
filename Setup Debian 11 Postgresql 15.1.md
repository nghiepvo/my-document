# Setup Debian 11 Postgresql 15.1

## Information PC and setup version

- Debian 11  
- Postgresql 15.1  
- PgPool-II  

| IP            | Node name  |
|---------------|------------|
| 192.168.1.160 | pgpool-0   |
| 192.168.1.161 | pg-1       |
| 192.168.1.162 | pg-2       |
| 192.168.1.163 | pg-3       |

## Apply all pg nodes

### Setup on hosts file

```shell
apt update && apt dist-upgrade -y
apt install lsb-release gnupg2 wget vim curl -y
sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add -

 curl https://dl.enterprisedb.com/default/release/get/deb | bash
apt update

```

```conf
vi /etc/hosts

....
192.168.1.160 pgpool-0
192.168.1.161 pg-1
192.168.1.162 pg-2
192.168.1.163 pg-3
```

```shell
apt install postgresql postgresql-client -y
/lib/systemd/systemd-sysv-install enable postgresql
systemctl status postgresql
su - postgres
psql
ALTER USER postgres PASSWORD '123456';
SHOW data_directory; # /var/lib/postgresql/15/main
SHOW config_file; # /etc/postgresql/15/main/postgresql.conf
SHOW hba_file; #/etc/postgresql/15/main/pg_hba.conf
SHOW log_directory;
SHOW log_filename;
exit
# show main path of postgresql
#ps aux | grep postgres | grep -- -D
```

```conf
vi /etc/postgresql/15/main/postgresql.conf


...
listen_addresses = '*'
...

log_statement = 'all'
log_directory = 'pg_log'
#tail -f /var/log/postgresql/postgresql-15-main.log
#ls -lrtr /var/lib/postgresql/15/main/pg_log
#tail -f  /var/lib/postgresql/15/main/pg_log/postgresql-xxxx-xx-xx.log
log_filename = 'postgresql-%Y-%m-%d.log'
logging_collector = on
log_min_error_statement = warning
```

```conf
vi /etc/postgresql/15/main/pg_hba.conf

...
# Database administrative login by Unix domain socket
local  all             postgres                                scram-sha-256

# TYPE  DATABASE        USER            ADDRESS                 METHOD

# "local" is for Unix domain socket connections only
local   all             all                                     peer
# IPv4 local connections:
host    all             all             127.0.0.1/32            scram-sha-256
host    all             all             0.0.0.0/0               scram-sha-256
# IPv6 local connections:
host    all             all             ::1/128                 scram-sha-256
# Allow replication connections from localhost, by a user with the
# replication privilege.
local   replication     all                                     peer
host    replication     all             127.0.0.1/32            scram-sha-256
host    replication     all             ::1/128                 scram-sha-256
```

Save password on your PC. [Environment Variables](https://www.postgresql.org/docs/current/libpq-envars.html)  

```conf
vi .bashrc

...
export PGUSER="postgres"
export PGPASSWORD="123456"
```

```shell
systemctl restart postgresql
# logout and login again.
# should login with root account without password
psql
```

## Setup logical Replication

```conf
vi /etc/postgresql/15/main/postgresql.conf

...

wal_level = logical
```

```shell
systemctl restart postgresql
```

**Master node.**

```shell
psql
create database app_db;
\c app_db
create table table_1(id int primary key, name varchar);
insert into table_1 values (generate_series(1,10), 'data-'||generate_series(1,10));
select * from table_1;
\q
# trace log
pg_dump -t table_1 -s app_db

pg_dump -t table_1 -s app_db | psql -h pg-2 app_db
```

**Slave node.**

```shell
create database app_db;
```

**Master node.**

```shell
# copy schema to other db
pg_dump -t table_1 -s app_db | psql -h pg-2 app_db
```

**Slave node.**

```shell
\c app_db
\dt
```

**Master node.**

```shell
\c app_db
create publication app_pub for table table_1;
```

**Slave node.**

```shell
\c app_db
create subcription app_sub connection 'dbname=app_db host=pg-1 user=postgres password=123456' publication app_pub;
```

**Testing for logical replication.**

## Setup stream replication

**Note:** Of course, we should clear all the pc

**Master node.**

```shell
psql
#CREATE ROLE repl_user WITH REPLICATION PASSWORD '123456' LOGIN;
create user repl_user replication;
ALTER USER repl_user PASSWORD '123456' LOGIN;

create database app_db;
\c app_db
create table table_1(id int primary key, name varchar);
insert into table_1 values (generate_series(1,10), 'data-'||generate_series(1,10));
select * from table_1;
```

**Slave node.**

```shell
systemctl stop postgresql
su - postgres
rm -rf /var/lib/postgresql/15/main/*
pg_basebackup -R -h pg-1 -U repl_user -D /var/lib/postgresql/15/main
chmod 750 -R /var/lib/postgresql/15/main
exit
systemctl restart postgresql
```

**Testing for stream replication.**

## Setup PGPool-II
 curl https://dl.enterprisedb.com/default/release/get/deb | sudo bash
```shell

apt list pgpool2 libpgpool2 postgresql-15-pgpool2 -a
apt -y install pgpool2 libpgpool2 postgresql-15-pgpool2
apt list --installed | grep pgpool2

chown -R postgres:postgres /usr/lib/postgresql/15/bin/
ln -s /usr/lib/postgresql/15/bin/* /usr/sbin/
```

```conf
vi /etc/profile

...
export PGDATA="/var/lib/postgresql/15/main/"
export PGARCHIVE="/var/lib/postgresql/15/archivedir"
export PGTOOL="/usr/lib/postgresql/15/bin/"
```

```shell
# exit root and login again
exist

su - postgres

mkdir $PGARCHIVE
systemctl stop postgresql
systemctl disable postgresql

```

```shell
# pg-1
rm -rf $PGDATA
initdb -d $PGDATA
#'cp "%p" "/var/lib/postgresql/15/archivedir/%f"'
chown postgres:postgres /etc/pgpool2/{failover.sh,follow_primary.sh}
```

```conf
pg-1:5432:replication:repl:123456
pg-2:5432:replication:repl:123456
pg-3:5432:replication:repl:123456

pg-1:5432:postgres:postgres:123456
pg-2:5432:postgres:postgres:123456
pg-3:5432:postgres:postgres:123456

#failover_command ='/etc/pgpool2/failover.sh %d %h %p %D %m %H %M %P %r %R %N %S'
#follow_primary_command = '/etc/pgpool2/follow_primary.sh %d %h %p %D %m %H %M %P %r %R'

#/usr/lib/postgresql/15
#echo 'pgpool:'`pg_md5 PCP password` >> /etc/pgpool2/pcp.conf

#ip link set ens19 up
```

## repmgr

```conf

# repmgr

apt install postgresql-15 -y
systemctl stop postgresql
/lib/systemd/systemd-sysv-install disable postgresql


# Import PATH 
# vi /etc/profile
if ...
    PATH=....
else
    PATH = /usr/local/sbin:/usr/sbin:.... 

# install sudoes, and set password for postgres
apt install sudo -y
chmod 0755 /etc/sudoers

passwd postgres

#vi /etc/sudoers
username     ALL=(ALL:ALL) ALL

chown -R postgres:postgres /usr/lib/postgresql/15/bin/

su - postgres
# import postgresql tool

sudo ln -s /usr/lib/postgresql/15/bin/* /usr/sbin/

systemctl stop postgresql
sudo rm -rf /var/lib/postgresql/15/main/*

initdb /var/lib/postgresql/15/main

# su - postgres -c "/usr/lib/postgresql/15/bin/initdb /var/lib/postgresql/15/main"
pg_ctl -D /var/lib/postgresql/15/main status

# make some run file
#vi /etc/postgresql/15/main/start.sh
su - postgres -c "/usr/lib/postgresql/15/bin/pg_ctl -D /var/lib/postgresql/15/main start"
# chmod +x /etc/postgresql/15/main/start.sh

#vi /run/postgresql/stop.sh
su - postgres -c "/usr/lib/postgresql/15/bin/pg_ctl -D /var/lib/postgresql/15/main stop"
# chmod +x /etc/postgresql/15/main/stop.sh

#vi /etc/postgresql/15/main/reload.sh
su - postgres -c "/usr/lib/postgresql/15/bin/pg_ctl -D /var/lib/postgresql/15/main reload"
# chmod +x /etc/postgresql/15/main/reload.sh


# create a service systemctl
sudo vi /lib/systemd/system/pg_cluster.service

[Unit]
Description=PG Cluster 15 (pg_ctl -D /var/lib/postgresql/15/main status)

[Service]
Type=simple
ExecStart=/bin/bash /etc/postgresql/15/main/start.sh

[Install]
WantedBy=multi-user.target

# -------
sudo systemctl daemon-reload
/lib/systemd/systemd-sysv-install enable postgresql
sudo systemctl start pg_cluster
sudo systemctl status pg_cluster

# mkdir -p /etc/repmgr/15
# vi /etc/repmgr/15/repmgr.conf


node_id=1
node_name='pg-1'
conninfo='host=192.168.1.161 user=repmgr dbname=repmgr connect_timeout=2'
data_directory='/var/lib/postgresql/15/main'

node_id=2
node_name='pg-2'
conninfo='host=192.168.1.162 user=repmgr dbname=repmgr connect_timeout=2'
data_directory='/var/lib/postgresql/15/main'

node_id=3
node_name='pg-3'
conninfo='host=192.168.1.163 user=repmgr dbname=repmgr connect_timeout=2'
data_directory='/var/lib/postgresql/15/main'

# Primary and Witness

pg_ctl /var/lib/postgresql/15/main
# vi /var/lib/postgresql/15/main/postgresql.conf
listen_addresses = '*' 
max_wal_senders = 10
max_replication_slots = 10
wal_level = 'replica'
hot_standby = on
archive_mode = on
archive_command = '/bin/true'
wal_log_hints = on

# vi /var/lib/postgresql/15/main/pg_hba.conf

local   replication     repmgr                                  trust
host    replication     repmgr          127.0.0.1/32            trust
host    replication     repmgr          192.168.1.1/24          trust

local   repmgr          repmgr                                  trust
host    repmgr          repmgr          127.0.0.1/32            trust
host    repmgr          repmgr          192.168.1.1/24          trust

# test from standby node
psql 'host=192.168.1.161 user=repmgr dbname=repmgr connect_timeout=2'
psql 'host=192.168.1.161 user=repmgr dbname=repmgr connect_timeout=2'
psql 'host=192.168.1.161 user=repmgr dbname=repmgr connect_timeout=2'
# alias
# vi /etc/profile
# primary 
alias rep='repmgr -f /etc/repmgr/15/repmgr.conf'

# standby config
# Note: these are command of repmgr run on postgres account other will be run root account
systemctl stop postgresql
rm -rf /var/lib/postgresql/15/main/*


# try test clone 
repmgr -h 192.168.1.161 -U repmgr -d repmgr -f /etc/repmgr/15/repmgr.conf standby clone --dry-run

# clone
repmgr -h 192.168.1.161 -U repmgr -d repmgr -f /etc/repmgr/15/repmgr.conf standby clone

# start 
systemctl start postgresql

# register standby
repmgr -f /etc/repmgr/15/repmgr.conf standby register
```
