# Setup Debian 11 MariaDB 10.5.15 Galera-4 26.4.11.
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

vim: copy and parse

**Shift-v** starts visual mode and selects current line  
**j** or arrow next. Visual mode movement 3 lines below current line  
**y** Yanks(copies) the selected stuff in the register  
**p** Paste the contents after current line.  

## Command galera-1, galera-2, galera-3 nodes

**Check.**  
> apt list mariadb-server mariadb-backup -a  

**Install.**
> apt install mariadb-server mariadb-backup -y  

**Verify.**
> apt list --installed | grep mariadb  
> apt list --installed | grep galera  

**Backup configuration file.**
> cp /etc/mysql/mariadb.conf.d/50-server.cnf /etc/mysql/mariadb.conf.d/50-server.cnf.bak  

## Verify install successful

> mysql -u root  
> show global variables like 'datadir';  
> select user();  
> show create user root@localhost;  

**Create a local access user.**
> create user user1@localhost identified via unix_socket;  
> show create user user1@localhost;  

**Quit mariadb cli and create account.**  
> groupadd user1  
> useradd -g user1 user1  
> su - user1  
> mariadb  

## Innit a secure

> mariadb-secure-installation  

**Note**: All Y and setup password for root accout

**Verify:**
> mariadb  
> show create user root@localhost;  
> exit  

**Login with password:**
> mariadb -u root -p  

## Setup replication

> vi /etc/mysql/mariadb.conf.d/50-server.cnf  

*Comment:*

```text
#bind_address = 127.0.0.1
```

*Append under **[mariadb]**.*

```text
log_error
log_bin=mariadb-bin
log_slave_updates=1
server_id=171
```

**Save and restart mariadb.**
> systemctl restart mariadb  

## Master Node

**Make sure mariadb-bin.\* existed (2 file).**
> ls -lrt /var/lib/mysql  

**Create a replication user.**
> mariadb  
> create user repl_user@'192.168.1.%' identified by 'password';  
> grant replication slave on *.* to repl_user@'192.168.1.%';  
> show grants for repl_user@'192.168.1.%';  
> show binary logs;  

**Go back terminal for check.**  
*mariadb-bin.000001 in list file of mysql folder.*  
**Important:** *look on GTID and make sure it existed.*  
> ls -lrt /var/lib/mysql  
> mysqlbinlog -v mariadb-bin.000001 | less  

*Make sure you will see position that shall the same in mariadb-bin.000001 file.*  
*GTID is [gtid]-[serverid]-[position of transaction].*  
> mariadb  
> show master status;  
>select binlog_gtid_pos('mariadb-bin.000001', 673);  

## Slave Node

> mariadb  
> show master status;  
> select binlog_gtid_pos('mariadb-bin.000001', 673);  

*It should empty.*  

set global gtid_slave_pos='';  
> change master to master_host='192.168.1.171', > master_port=3306, master_user='repl_user', master_password='password', master_use_gtid=current_pos;  
> start slave;  
> show slave status\G;  

*Make sure dont have any error property.*  

**Verify.**
> show create user repl_user@'192.168.1.%';  
> exit  
> ls -lrt /var/lib/mysql  
> mysqlbinlog -v mariadb-bin.000001 | less  

*Make sure it shall be the same with master node.*  


## Maxscale Node

> wget https://dlm.mariadb.com/2700606/MaxScale/22.08.3/packages/debian/bullseye/x86_64/maxscale-22.08.3-1.debian.bullseye.x86_64.deb  
> apt install libcurl4 -y  
> dpkg -i maxscale-22.08.3-1.debian.bullseye.x86_64.deb  
> vi /etc/maxscale.cnf  