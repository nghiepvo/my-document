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
export PGDATA="/var/lib/postgresql/15/main"
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
systemctl disable postgresql
rm -rf /var/lib/postgresql/15/main/*

# Import PATH 
# vi /etc/profile
if ...
    PATH=....
else
    PATH = /usr/local/sbin:/usr/sbin:.... 
...
export PGDATA="/var/lib/postgresql/15/main"
alias pgctl='/usr/lib/postgresql/15/bin/pg_ctl -D /var/lib/postgresql/15/main'
alias rep='repmgr -f /etc/repmgr/15/repmgr.conf'
alias pg='su - postgres'
alias checkprimary='psql "host=192.168.1.161 user=repmgr dbname=repmgr connect_timeout=2"'
alias checkwitness='psql "host=192.168.1.162 user=repmgr dbname=repmgr connect_timeout=2"'
alias checkstandby1='psql "host=192.168.1.163 user=repmgr dbname=repmgr connect_timeout=2"'

# exit and login again for effect alias and export

# install sudoes, and set password for postgres
apt install sudo -y
chmod 0755 /etc/sudoers

passwd postgres

# vi /etc/sudoers
postgres ALL=(ALL:ALL) ALL
postgres ALL = NOPASSWD: /usr/bin/systemctl restart pg_cluster.service, /usr/bin/systemctl start repmgrd.service, /usr/bin/systemctl stop repmgrd.service

chown -R postgres:postgres /usr/lib/postgresql/15/bin/

# import postgresql tool
ln -s /usr/lib/postgresql/15/bin/* /usr/sbin/


# make some run file
# vi /etc/postgresql/15/main/start.sh
su - postgres -c "/usr/lib/postgresql/15/bin/pg_ctl -D /var/lib/postgresql/15/main start"
# chmod +x /etc/postgresql/15/main/start.sh

# vi /etc/postgresql/15/main/stop.sh
su - postgres -c "/usr/lib/postgresql/15/bin/pg_ctl -D /var/lib/postgresql/15/main stop"
# chmod +x /etc/postgresql/15/main/stop.sh


# create a service systemctl
# vi /lib/systemd/system/pg_cluster.service

#!/bin/bash
### BEGIN INIT INFO
# Provides:          pg_cluster
# Required-Start:    $all
# Required-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:
# Short-Description: pg_ctl runner.
### END INIT INFO

[Unit]
Description=PG Cluster 15 (pg_ctl -D /var/lib/postgresql/15/main status)
StartLimitIntervalSec=400
StartLimitBurst=3
After=network.target

[Service]
Type=simple
ExecStart=/bin/bash /etc/postgresql/15/main/start.sh
RemainAfterExit=true
ExecStop=/bin/bash /etc/postgresql/15/main/stop.sh
StandardOutput=journal
PIDFile=/run/postgres/pg_cluster.pid

[Install]
WantedBy=multi-user.target
# -------

systemctl daemon-reload
systemctl enable pg_cluster
# if have any error please run sudo rm -f /etc/init.d/pg_cluster
systemctl start pg_cluster
systemctl status pg_cluster


# Primary and Witness

su - postgres

initdb $PGDATA

# vi /var/lib/postgresql/15/main/postgresql.conf
listen_addresses = '*' 
max_wal_senders = 10
max_replication_slots = 10
wal_level = 'replica'
hot_standby = on
archive_mode = on
archive_command = '/bin/true'
wal_log_hints = on

pgctl reload

# Primary and standby
apt install postgresql-15-repmgr -y

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

node_id=4
node_name='witness'
conninfo='host=192.168.1.164 user=repmgr dbname=repmgr connect_timeout=2'
data_directory='/var/lib/postgresql/15/main'

# Primary Witness
su - postgres

createuser --superuser repmgr
createdb --owner=repmgr repmgr
psql -c "ALTER USER repmgr SET search_path TO repmgr, public;"

# vi /var/lib/postgresql/15/main/postgresql.conf
shared_preload_libraries = 'repmgr'

# vi /var/lib/postgresql/15/main/pg_hba.conf

local   replication     repmgr                                  trust
host    replication     repmgr          127.0.0.1/32            trust
host    replication     repmgr          192.168.1.1/24          trust

local   repmgr          repmgr                                  trust
host    repmgr          repmgr          127.0.0.1/32            trust
host    repmgr          repmgr          192.168.1.1/24          trust


pgctl reload

# Standby 
# test from standby node or run checkprimay if setup alias revious step.
psql 'host=192.168.1.161 user=repmgr dbname=repmgr connect_timeout=2'

# Primary

rep primary register
rep cluster show

# standby config
# Note: these are command of repmgr run on postgres account other will be run root account
systemctl stop pg_cluster
rm -rf /var/lib/postgresql/15/main/*

su - postgres # pg

# try test clone and fix any warning
repmgr -h 192.168.1.161 -U repmgr -d repmgr -f /etc/repmgr/15/repmgr.conf standby clone --dry-run

# clone
repmgr -h 192.168.1.161 -U repmgr -d repmgr -f /etc/repmgr/15/repmgr.conf standby clone

# start  
sudo systemctl start pg_cluster

# register standby
repmgr -f /etc/repmgr/15/repmgr.conf standby register

# verify 
rep cluster show --compact

# Witness

repmgr -f /etc/repmgr/15/repmgr.conf witness register -h 192.168.1.161

# verify
rep cluster show --compact


# sudo vi /etc/repmgr/15/repmgr.conf

... 
failover='automatic'
promote_command='/usr/bin/repmgr standby promote -f /etc/repmgr/15/repmgr.conf --log-to-file'
follow_command='/usr/bin/repmgr standby follow -f /etc/repmgr/15/repmgr.conf --log-to-file --upstream-node-id=%n'
monitor_interval_secs=2
connection_check_type='ping'
reconnect_attempts=4
reconnect_interval=8
primary_visibility_consensus=true
standby_disconnect_on_failover=true
repmgrd_service_start_command='sudo /usr/bin/systemctl start repmgrd.service'
repmgrd_service_stop_command='sudo /usr/bin/systemctl stop repmgrd.service'
service_start_command='/usr/lib/postgresql/15/bin/pg_ctl -D /var/lib/postgresql/15/main start'
service_stop_command='/usr/lib/postgresql/15/bin/pg_ctl -D /var/lib/postgresql/15/main stop'
service_restart_command='sudo /usr/bin/systemctl restart pg_cluster.service'
service_reload_command='/usr/lib/postgresql/15/bin/pg_ctl -D /var/lib/postgresql/15/main reload'
monitoring_history=yes
log_status_interval=60
priority=60 # pg-1: 100, pg-2: 60, pg-3: 40


# vi /etc/default/repmgrd

sudo /usr/bin/systemctl stop repmgrd.service

sudo vi /etc/default/repmgrd

# default settings for repmgrd. This file is source by /bin/sh from
# /etc/init.d/repmgrd

# disable repmgrd by default so it won't get started upon installation
# valid values: yes/no
REPMGRD_ENABLED=yes

# configuration file (required)
REPMGRD_CONF="/etc/repmgr/15/repmgr.conf"

# additional options
#REPMGRD_OPTS="--daemonize=false"

# user to run repmgrd as
REPMGRD_USER=postgres

# repmgrd binary
REPMGRD_BIN=/usr/bin/repmgrd

# pid file
REPMGRD_PIDFILE=/var/run/repmgrd.pid



repmgr -f /etc/repmgr/15/repmgr.conf daemon start --dry-run
repmgr -f /etc/repmgr/15/repmgr.conf daemon start

# verify 

rep cluster event --event=repmgrd_start

# stop primary

pgctl stop

# check node 2
rep cluster show --compact

# rejoin primary
repmgr node rejoin -f /etc/repmgr/15/repmgr.conf -d 'host=192.168.1.162 user=repmgr dbname=repmgr connect_timeout=2' --config-files=postgresql.local.conf,postgresql.conf --verbose --force-rewind --dry-run


pg_rewind -D '/var/lib/postgresql/15/main' --source-server='host=192.168.1.162 user=repmgr dbname=repmgr connect_timeout=2'
```
