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
    1. 提到了为了防止随机读取的耗费时间，所以采用将Bitmap index取出的索引顺序的记录并排序(这里应该指的是记录的位置)，然后再顺序读取，从而获得结果集

### 将数组变成多条记录输出，而不是一个字段。
    select id, unnest(string_to_array(bind_ids, ',')) as bind_id from account where id=502153;// 将这条account记录的bind_ids，按逗号隔开，然后多条输出

### select '\"drm\":\"2\"' ~ '\"drm\":\"2\"'结果为false
    由于\"在正则表达式中只是作为一个转义，所以在~的字符串变成是'"drm":"2"'的这样匹配。
    而数据库\"drm\":\"2\"是一个单独的字符串

### 更新索引字段，对于索引会有怎么样的优化呢
    1. 通过bt_page_items去查询对应的索引数据得到(16699,12)
    2. 接着使用update xxx 来更新索引信息(值不变)
    3. 根据ctid(16699,12)已经查不到数据了
    4. 根据id来查这条数据的ctid已经发生变更(160,9)
    5. 并根据bt_page_items可以查到这条数据(160,9)，而原先的数据还是保留。
    因此经过update之后，索引是直接重新插入一条，并将原来的数据做出偏移
    问题：如果我现在没有更新索引字段的话，会不会触发呢?
    6. 会的。情况与上述情况一样
### bt_page_items中的data是什么值
    比如select * from bt_page_items('idx_test_plays', 2);得出的记录中，后面都有个data,
    1. 如果data为空，代表当前没有右链，否则这条的ctid代表右链的值
    01 00 00 00 00 00 00 00：为1
    00 01 00 00 00 00 00 00：256就是01*256
    00 10 00 00 00 00 00 00：256*16*1
### 针对上次并发update video set plays=case when plays is null then 0 else plays end + 1 where id=1000的死锁分析
    1. 在postgresql中，没有U锁，也就是更新锁，所以在更新的时候，会先获取share，也就是读记录的锁，接着再去获取排他锁。这就造成死锁的关键原因；
    2. 事务1：获取了share锁，然后准备获取排他锁，需等事务2的share锁释放
    3. 事务2：获取了share锁，然后准备获取排他锁，需等事务1的share锁释放
### 数据库锁的分析
    table test(id bigint pk, plays int);
    idx_test_plays on test(plays)建立索引
    1. select * from test where id =1 for update
    分析：1.1 获取索引pk_test的id=1的AccessShare的锁
         1.2 获取表test的的id=1的RowShare锁
         1.3 获取该条记录tuple的For Update行锁

### 分区表使用总结
    1. 创建函数create or replace function xx() return x(返回值，可以是void) as $name$
       begin end; $name$ LANGUAGE 'plpgsql' VOLATILE;
    2. 函数中循环记录
       for r in (selct * from xxx) loop
    3. 转义单引号，可以使用'''来实现单个单引号的转义
    4. 在函数中可以使用execute来执行sql，这样就能对sql进行拼接了

### zhparser扩展使用
    1. select ts_token_type('xxconfigname(如)'); // 支持多少种分词形式
    2. ALTER TEXT SEARCH CONFIGURATION testzhcfg ADD MAPPING FOR n,v,a,i,e,l WITH simple;//设置搜索配置的分词形式。
    3. SELECT to_tsvector('testzhcfg','阿弟仔'); // 按对应的分词来分割


### 查看表文件和表文件位置
	1. select pg_relation_filenode(''),pg_relation_filepath('')

### 查看表有没有执行auto_analyse
	1. pt_stat_all_tables;
		1.1 n_mod_since_analyse 上一次analyse之后，当前更新或者执行了多少条

### 表的计划执行
	1. 从pg_class中获取总页数和总条数,relpages、reltuples
	2. pg_stats(pg_statistic的视图，更加安全),histogram_bounds(直方图)，num_distinct(如果>0,表示有多少种值、如果为-1，表示值唯一，其它负值表示值的个数/行数 * -1)。most_coomon_vals(MCV最常出现的值，一般与num_distinct来混合使用),most_common_freqs(MCV出现的概率)