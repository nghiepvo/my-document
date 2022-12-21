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
