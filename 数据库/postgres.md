##系统设置
### set (session) xxx=value
session是代表，只在当前session有效，xxx代表参数名，value代表值
### set_config(param,value,true)
同上，设置某个参数的值，true代表在session有效；false代表只在事务中有效；
### show param
展示某个属性的值
### select current_setting('param')
同show

### join\_collapse_limit
如果=1：按join的顺序来获取<br/>&nbsp;&nbsp;&nbsp;&nbsp;\>1：执行计划优化

有个问题，目前join，执行计划都会变成小表查询大表，虽然小表数据可以加载到内存，减少页的少描，但是同时页存在大表全表扫描的情况；但是如果大表join小表，这时候limit 10条记录，那么根据大表去小表扫描，效率可能会有提升
数据库也会优化自己的查询计划，设置了该项的值，也没有按照自己的join顺序出来

### 排序索引
create index idx_sss on table(column collate "C" desc nulls last);
<br/>collate字母排序规则，默认c语言的排序方式
<br/>nulls last空可以放到最后，不过对应的使用的排序都要使用desc nulls last 或者 asc nulls first才能走索引

### coalesce
函数可以保证null值替换成其它值，coalesce(null, '')这样可以保证null时输出空串

###权限控制
grant select on table in schema xx to user;
<br/>grant usage on schema xx to user;
<br/>revoke grant option for select on table in schema xx from user;

### 表记录的事务id查看
    select ctid,xmin,xmax,cmin,cmax from table
    ctid:物理位置
    xmin:插入事务的id
    xmax:同一条事务的第几条命令
    cmin:删除的事务id
    cmax:删除的事务的第几条指令


###备份与还原
增量备份：

    热备的一种方式。必须先开启wal(预写日志模块)，在postgresql.conf种配置
    wal_level为mininal以上
    archive_mode = on
    arichive_command = 'copy %p dir/%f' #拷贝增量日志到该目录
    每一个增量日志默认配置为16M，archive_time的时候自动备份。当然也可以执行select pg_switch_xlog() 手动备份
    

两种方法：

    1. 利用pg_basebackup新建一个初始备份
    2. 修改postgresql.conf和pg_hba.conf
    3. 新增recover.conf
       resotre_command = 'cp dir/%f %p' #拷贝wal日志
       recovery_target_time 还原到某个时间点
       recovery_target_xid 还原到某个事务id（select xmin from table）
第二种方法：

    1. select pg_start_backup('label');指定开始备份点
    2. tar -cf data --exclude=pg_xlogs 压缩数据目录
    3. select pg_stop_backup();结束备份
    4. 解压目录
    5. 其它同第一种方法

之前的错误尝试，如果一个backup，被启动之后，就会指定了还原点，这时候即使再修改recover.conf，也是无法生效的。必须重新弄一个新的backup，因为还原的时间点是不可逆的

### 流复制
    同备份与还原，
    主服务器：开启wal日志和wal_senders
    备服务器：recover.conf中新增primary_conninfo=..和standby = 'on'然后配置postgresql.conf中的hot_standBy=on

### 对于bitmap index的搜索。
    1. 提到了为了防止随机读取的耗费时间，所以采用将Bitmap index取出的索引顺序的记录并排序(这里应该指的是记录的位置)，然后从开始位置到结束位置，读取对应的记录，从而获得结果集
