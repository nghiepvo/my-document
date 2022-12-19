# Setup Debian 11 MariaDB 10.5.15 Galera-4 26.4.11

## Information PC and setup version

- Debian 11  
- MariaDB 10.5.15
- Galera-4 26.4.11  

| IP            | Node name  |
|---------------|------------|
| 192.168.1.170 | maxscale-0 |
| 192.168.1.171 | galera-1   |
| 192.168.1.172 | galera-2   |
| 192.168.1.173 | galera-3   |

## Tips

Use **Terminator** application on Linux.  

**mysql client:** use ```Ctrl + L``` or ```\! cls``` for clear screen LOL.  

## Command galera-1, galera-2, galera-3 nodes

**Check.**  

```shell
apt list mariadb-server mariadb-backup -a  
```

**Install.**

```shell
apt install mariadb-server mariadb-backup -y  
```

**Verify.**

```shell
apt list --installed | grep mariadb  
apt list --installed | grep galera  
```

**Backup configuration file.**

```shell
cp /etc/mysql/mariadb.conf.d/50-server.cnf /etc/mysql/mariadb.conf.d/50-server.cnf.bak  
```

## Verify install successful

```shell
mysql -u root  
show global variables like 'datadir';  
select user();  
show create user root@localhost;  
```

**Create a local access user.**

```shell
create user user1@localhost identified via unix_socket;  
show create user user1@localhost;  
```

**Quit mariadb cli and create account.**  

```shell
groupadd user1  
useradd -g user1 user1  
su - user1  
mariadb  
```

## Innit a secure on galera-1, galera-2, galera-3 nodes

mariadb-secure-installation  

**Note**: All Y and setup password for root accout

**Verify:**

```shell
mariadb  
show create user root@localhost;  
exit  
```

**Login with password:**

```shell
mariadb -u root -p  
```

## Setup replication

```shell
vi /etc/mysql/mariadb.conf.d/50-server.cnf  
```

*Comment:*

```text
#bind_address = 127.0.0.1
```

*Append under **[mysqld]**.*

```text
skip-grant-tables
```

*Append under **[mariadb]**.*

```text
log_error
log_bin=mariadb-bin
log_slave_updates=1
server_id=171
```

```conf
#
# These groups are read by MariaDB server.
# Use it for options that only the server (but not clients) should see

# this is read by the standalone daemon and embedded servers
[server]

# this is only for the mysqld standalone daemon
[mysqld]

#
# * Basic Settings
#

user                    = mysql
pid-file                = /run/mysqld/mysqld.pid
basedir                 = /usr
datadir                 = /var/lib/mysql
tmpdir                  = /tmp
lc-messages-dir         = /usr/share/mysql
lc-messages             = en_US
skip-external-locking
skip-grant-tables
# Broken reverse DNS slows down connections considerably and name resolve is
# safe to skip if there are no "host by domain name" access grants
#skip-name-resolve

# Instead of skip-networking the default is now to listen only on
# localhost which is more compatible and is not less secure.
bind-address            = 127.0.0.1

#
# * Fine Tuning
#

#key_buffer_size        = 128M
#max_allowed_packet     = 1G
#thread_stack           = 192K
#thread_cache_size      = 8
# This replaces the startup script and checks MyISAM tables if needed
# the first time they are touched
#myisam_recover_options = BACKUP
#max_connections        = 100
#table_cache            = 64

#
# * Logging and Replication
#

# Both location gets rotated by the cronjob.
# Be aware that this log type is a performance killer.
# Recommend only changing this at runtime for short testing periods if needed!
#general_log_file       = /var/log/mysql/mysql.log
#general_log            = 1

# When running under systemd, error logging goes via stdout/stderr to journald
# and when running legacy init error logging goes to syslog due to
# /etc/mysql/conf.d/mariadb.conf.d/50-mysqld_safe.cnf
# Enable this if you want to have error logging into a separate file
#log_error = /var/log/mysql/error.log
# Enable the slow query log to see queries with especially long duration
#slow_query_log_file    = /var/log/mysql/mariadb-slow.log
#long_query_time        = 10
#log_slow_verbosity     = query_plan,explain
#log-queries-not-using-indexes
#min_examined_row_limit = 1000

# The following can be used as easy to replay backup logs or for replication.
# note: if you are setting up a replication slave, see README.Debian about
#       other settings you may need to change.
#server-id              = 1
#log_bin                = /var/log/mysql/mysql-bin.log
expire_logs_days        = 10
#max_binlog_size        = 100M

#
# * SSL/TLS
#

# For documentation, please read
# https://mariadb.com/kb/en/securing-connections-for-client-and-server/
#ssl-ca = /etc/mysql/cacert.pem
#ssl-cert = /etc/mysql/server-cert.pem
#ssl-key = /etc/mysql/server-key.pem
#require-secure-transport = on

#
# * Character sets
#

# MySQL/MariaDB default is Latin1, but in Debian we rather default to the full
# utf8 4-byte character set. See also client.cnf
character-set-server  = utf8mb4
collation-server      = utf8mb4_general_ci

#
# * InnoDB
#

# InnoDB is enabled by default with a 10MB datafile in /var/lib/mysql/.
# Read the manual for more InnoDB related options. There are many!
# Most important is to give InnoDB 80 % of the system RAM for buffer use:
# https://mariadb.com/kb/en/innodb-system-variables/#innodb_buffer_pool_size
#innodb_buffer_pool_size = 8G

# this is only for embedded server
[embedded]

# This group is only read by MariaDB servers, not by MySQL.
# If you use the same .cnf file for MySQL and MariaDB,
# you can put MariaDB-only options here
[mariadb]
log_error
log_bin=mariadb-bin
log_slave_updates=1
server_id=172

# This group is only read by MariaDB-10.5 servers.
# If you #use the same .cnf file for MariaDB of different versions,
# use this group for options that older servers don't understand
[mariadb-10.5]
```

**Save and restart mariadb.**

```shell
systemctl restart mariadb  
```

## Master Node

**Make sure mariadb-bin.\* existed (2 file).**

```shell
ls -lrt /var/lib/mysql  
```

**Create a replication user.**

```shell
mariadb  
create user repl_user@'192.168.1.%' identified by 'password';  
grant replication slave on *.* to repl_user@'192.168.1.%';  
show grants for repl_user@'192.168.1.%';  
show binary logs;  
```

**Go back terminal for check.**  

*mariadb-bin.000001 in list file of mysql folder.*  
**Important:** *look on GTID and make sure it existed.*  

```shell
ls -lrt /var/lib/mysql  
mysqlbinlog -v mariadb-bin.000001 | less  
```

*Make sure you will see position that shall the same in mariadb-bin.000001 file.*  

*GTID is [gtid]-[serverid]-[position of transaction].*

```shell
mariadb  
show master status;  
select binlog_gtid_pos('mariadb-bin.000001', 673);  
```

## Slave Node

```shell
mariadb  
show master status;  
select binlog_gtid_pos('mariadb-bin.000001', 673);  
```

*It should empty.*  

```shell
set global gtid_slave_pos='';
change master to master_host='192.168.1.171', master_port=3306, master_user='repl_user', master_password='password', master_use_gtid=current_pos;
start slave;
show slave status\G;
```

*Make sure dont have any error property.*  

**Verify.**

```shell
show create user repl_user@'192.168.1.%';  
exit  
ls -lrt /var/lib/mysql  
mysqlbinlog -v mariadb-bin.000001 | less  
```

*Make sure it shall be the same with master node.*  

## Maxscale Node

```shell
wget <https://dlm.mariadb.com/2700606/MaxScale/22.08.3/packages/debian/bullseye/x86_64/maxscale-22.08.3-1.debian.bullseye.x86_64.deb 
apt install libcurl4 -y  
dpkg -i maxscale-22.08.3-1.debian.bullseye.x86_64.deb  
vi /etc/maxscale.cnf  
```

```conf
# MaxScale documentation:
# https://mariadb.com/kb/en/mariadb-maxscale-6/

# Global parameters
#
# Complete list of configuration options:
# https://mariadb.com/kb/en/mariadb-maxscale-6-mariadb-maxscale-configuration-guide/

[maxscale]
threads=auto

# Server definitions
#
# Set the address of the server to the network
# address of a MariaDB server.
#

[galera-1]
type=server
address=192.168.1.171
port=3306
protocol=MariaDBBackend


[galera-2]
type=server
address=192.168.1.172
port=3306
protocol=MariaDBBackend


# Monitor for the servers
#
# This will keep MaxScale aware of the state of the servers.
# MariaDB Monitor documentation:
# https://mariadb.com/kb/en/maxscale-6-monitors/
#
# Create the monitor user with:
#
#  CREATE USER 'monitor_user'@'%' IDENTIFIED BY 'monitor_pw';
#  GRANT REPLICATION CLIENT, FILE, SUPER, RELOAD, PROCESS, SHOW DATABASES, EVENT ON *.* TO 'monitor_user'@'%';
#

[MariaDB-Monitor]
type=monitor
module=mariadbmon
servers=galera-1, galera-2
user=maxmon_user
password=password
monitor_interval=2s
verify_master_failure=true
enforce_read_only_slaves=true
auto_failover=true
auto_rejoin=true

# Service definitions
#
# Service Definition for a read-only service and
# a read/write splitting service.
#
# Create the service user with:
#
#  CREATE USER 'service_user'@'%' IDENTIFIED BY 'service_pw';
#  GRANT SELECT ON mysql.user TO 'service_user'@'%';
#  GRANT SELECT ON mysql.db TO 'service_user'@'%';
#  GRANT SELECT ON mysql.tables_priv TO 'service_user'@'%';
#  GRANT SELECT ON mysql.columns_priv TO 'service_user'@'%';
#  GRANT SELECT ON mysql.procs_priv TO 'service_user'@'%';
#  GRANT SELECT ON mysql.proxies_priv TO 'service_user'@'%';
#  GRANT SELECT ON mysql.roles_mapping TO 'service_user'@'%';
#  GRANT SHOW DATABASES ON *.* TO 'service_user'@'%';
#

# ReadConnRoute documentation:
# https://mariadb.com/kb/en/mariadb-maxscale-6-readconnroute/

[Read-Only-Service]
type=service
router=readconnroute
servers=galera-2
user=max_user
password=password
router_options=slave

# ReadWriteSplit documentation:
# https://mariadb.com/kb/en/mariadb-maxscale-6-readwritesplit/

[Read-Write-Service]
type=service
router=readwritesplit
servers=galera-1,galera-2
user=max_user
password=password
master_reconnection=true
master_failure_mode=error_on_write
transaction_replay=true
slave_selection_criteria=ADAPTIVE_ROUTING
master_accept_reads=true

# Listener definitions for the services
#
# These listeners represent the ports the
# services will listen on.
#

[Read-Only-Listener]
type=listener
service=Read-Only-Service
protocol=MariaDBClient
port=3307

[Read-Write-Listener]
type=listener
service=Read-Write-Service
protocol=MariaDBClient
port=3306
```

**Go on master node.**

```shell
maria  
create user maxmon_user@'%' identified by 'password';
create user max_user@'%' identified by 'password';
grant all on *.* to maxmon_user@'%';
grant all on *.* to max_user@'%';
```

*it's should testing evironment, LOL.*  

```shell
systemctl restart maxscale  
systemctl enable maxscale  
systemctl status maxscale  
tail -f /var/log/maxscale/maxscale.log  
maxctrl show maxscale  
maxctrl list services  
maxctrl list servers  
maxctrl list monitors  
```

*Make to back master.*

```shell
maxctrl call command mariamon switchover Mariadb-Monitor galera-1 Backup  
```

## End
