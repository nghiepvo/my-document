# Tips

Use **Terminator** application on Linux.  

**mysql client:** use ```Ctrl + L``` or ```\! cls``` for clear screen LOL.  

## vim

### Copy and parse

**Shift-v** starts visual mode and selects current line  
**j** or arrow next. Visual mode movement 3 lines below current line  
**y** Yanks(copies) the selected stuff in the register  
**p** Paste the contents after current line.  

**/ or ?** enter the search pattern and hit Enter.  

## Fix perl: warning

```conf
vi /etc/default/locale

...
LANGUAGE=en_US.UTF-8
LANG=en_US.UTF-8
LC_ALL=en_US.UTF-8
```

```shell
locale-gen en_US.UTF-8
dpkg-reconfigure locales
apt update
```

## View stored postgresql DB

```shell
psql
SELECT oid from pg_database where datname = 'dbName';

ls -lrha /var/lib/postgresql/15/main/base/
```

## find file

```shell
find / -name filename
```

## change PASS

```shell
passwd username
```

## setup sudo and allow user run sudo without password

```shell
apt install sudo -y

chmod 0777 /etc/sudoers
```

```conf
vi /etc/sudoers
# add end of line
username     ALL=(ALL) NOPASSWD:ALL
```

```shell

chmod 0755 /etc/sudoers
```
## set variable environment all user

```conf
vi /etc/profile
...
export PGDATA="/var/lib/postgresql/15/data"
export ETCDCTL_API="3"
export PATRONI_ETCD_URL="http://127.0.0.1:2379"
export PATRONI_SCOPE="pg_cluster"
patroni-1=192.168.1.181
patroni-2=192.168.1.182
patroni-3=192.168.1.183
ENDPOINTS=$patroni1:2379,$patroni2:2379,$patroni3:2379
```


