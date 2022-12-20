# Setup Debian 11 Postgresql 15.1

## Information PC and setup version

- Debian 11  
- Postgresql 15.1  
- PgPool-II  

| IP            | Node name  |
|---------------|------------|
| 192.168.1.160 | pgpool-0 |
| 192.168.1.161 | pg-1   |
| 192.168.1.162 | pg-2   |
| 192.168.1.163 | pg-3   |

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
ALTER USER postgres PASSWORD 'Str0ngP@ssw0rd';
SHOW data_directory;
SHOW config_file;
SHOW hba_file;
SHOW log_directory;
SHOW log_filename;
exit
```

```conf
vi /etc/postgresql/15/main/postgresql.conf


...
listen_addresses = '*' 
...

log_statement = 'all'
log_directory = 'pg_log'
log_filename = 'postgresql-%Y-%m-%d_%H%M%S.log'
logging_collector = on
log_min_error_statement = info
```

```conf
vi /etc/postgresql/15/main/pg_hba.conf

# IPv4 local connections:
host    all             all             127.0.0.1/32            scram-sha-256
# Allow all access
host    all             all             0.0.0.0/0               scram-sha-256
```

Save password on your PC. [Environment Variables](https://www.postgresql.org/docs/current/libpq-envars.html)  

```conf
vi .bashrc

...
export PGPASSWORD="Str0ngP@ssw0rd"
export PGUSER="postgres"
```

```shell
systemctl restart postgresql
```
