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
apt install lsb-release gnupg2 wget vim -y
apt-cache search postgresql | grep postgresql
sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
wget -qO- https://www.postgresql.org/media/keys/ACCC4CF8.asc | tee /etc/apt/trusted.gpg.d/pgdg.asc &>/dev/null
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
SHOW data_directory;
SHOW config_file;
SHOW hba_file;
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

```shell

apt list pgpool2 libpgpool2 postgresql-15-pgpool2 -a
apt -y install pgpool2 libpgpool2 postgresql-15-pgpool2
apt list --installed | grep pgpool2

```

```conf
vi /etc/pgpool2/pgpool.conf

...
listen_addresses = '*'
...
port = 5432
...
socket_dir = '/var/run/postgresql'
...
backend_hostname0 = 'pg-1'
backend_port0 = 5432
backend_weight0 = 0
backend_data_directory0 = '/data/pg-1/'

...
backend_hostname1 = 'pg-2'
backend_port1 = 5432
backend_weight1 = 1
backend_data_directory1 = '/data/pg-2'
...
log_statement = on
log_per_node_statement = on
...
pid_file_name = '/var/run/postgresql/pgpool.pid'
...
sr_check_period = 10
sr_check_user = 'repl_user'
sr_check_password = '123456'
...
health_check_period = 10
health_check_user = 'repl_user'
health_check_password = '123456'
```
