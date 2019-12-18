## 问题
	1.Redis和memcheched 什么区别？？为什么单线程的redis比多线程的memched效率要高？？
		1.1 redis是单线程的，memcheched是多线程。redis支持持久化，memcache不支持持久化
		1.2 单线程的操作，可以防止数据并发而对数据进行加锁，也减少了进程之间切换，导致资源
	2.Redis有什么数据类型？？都在哪些场景下使用？？
		2.1 String
		2.2 Hash
		2.3 list
		2.4 set
		2.5 zset(有序组合)
	3.redis主从复制是怎么实现的？？Redis的集群模式是如何实现的？？redis的key是如何寻址的？？
		3.1 先全量全部，这里可以是快照同步或者无盘同步(repl-diskless-sync)，快照会进行磁盘写入，保存到rdb文件，然后进行传输。无盘同步则不用，直接拷贝一份同样大小的内存，然后进行同步
		3.2 根据命令进行同步，这个命令会在一个本地的内存buffer,从节点会上报对应的偏移量
		3.3 从节点可以通过slaveof x.x.x.x x
		3.4 主节点可以通过info replication来查看从节点信息
		3.5 repl-backlog-size：增量的环形Buffer，当slave重启之后，会上报offset，判断offset是否在buffer中，如果不在，则执行全量同步
		3.6 client-output-buffer-limit:配置内容具体没有明白？？？
	4.使用redis如何设计分布式锁， 使用zk可以么？？如何实现？？这两种那个效率更高？？
		4.1 watch，setexists
		4.2 zk可以通过创建series_temp节点，每次监控节点是否为最小的，来达到分布式锁
		4.3 
	5.知道redis的持久化么？？ 都有什么优缺点？？具体底层实现呢？？
		5.1 快照存储，rdb，全量存储
			优点：1.适合大规模数据的恢复
			缺点：1. 数据一致性和完整性较差，因为没有保存而丢失，或者在保存的时候，刚好在对数据进行更改
				 2. 保存的时候会造成内存的成倍增长，
		5.2 aof，增量存储
			优点：数据一致性和完整性较好，能够做到每秒保存，或者每次保存都写入
			缺点：1.如果选择每次保存都写入，性能差
				 2.文件大小会越来越大，如果没有进行重写的话，可能一个key值会有多条指令，导致空间浪费
			
	6.Redis淘汰策略都有哪些？？LRU??写一下java版本的代码？？
		6.1 valatile-lru 过期键值，最久未使用
		6.2 allkeys-lru  所有键值，最久未使用，可能造成自己的key也被删除
		6.3 volatile-random 过期随机
		6.4 allkeys-random 所有键值唯一
		6.5 volatile-ttl   键值即将过期(距离过期时间最短)
		6.6 noeviction     不存入
		如果volatile已经删除干净了，这时候就会采取noeviction,拒绝存入
		这次cms中考虑并不只是listByIds的数据存入缓存，考虑也将detail、topic、search出来按id列表查找的考虑存入。于是打算listByids的存的是不过期的，而detail、topic、search等存入过期的
	7.sentinel模式
		7.1 master 6379,slave1 6380,slave2 6381,sentinel1 26379,sentinel2 26380,sentinel3 26381，当6379停了之后，6380会主动切为master，但是6381是怎么将master切换过去的呢？
	8. 过期键处理
		8.1 惰性：等到获取的时候，过期了再删除
		8.2 定时，每10s随机获取25key，如果key过期了，则删除；如果这次的策略有1/4的key过期了，则再重新随机获取，但是会把时间控制在25ms
		8.3 定期，指定时间去删除
		8.3 从节点不会有expired的概念，主节点在expired时进行delete的话，从节点同步了这个命令
## Redis数据类型
	1. String
		1.1 扩容的时候，有什么策略？(通过append的时候，比如s1的值为redis，那么sds存储则是，free:0,length:5,value:redis，那么这时候append s1 cluster，那么会计算出长度为5+6=11,则redis会优先分配free:11,length:11,双倍分配，)
			小于1M，默认按双倍大小进行扩容，如果大于1M，最多就多加1M
		1.2 bitfield test get i8 40，从test的40位开始获取8位二进制数(i8)，组成一个有符号整数
		1.3 当小于44字节的字符串用embstr，当超过了用raw.因为jemmalloc最大能够分配64字节的数据-20key字节(redisobject16+sds3+null1)
	2. list(使用的linkedlist双向列表)
		2.1 lrange，必须由左边开始获取，比如a、b、c、d的列表，0-3可以，-3-0就会为空(-3是b，0是a，不能逆向获取) 
		2.2 ltrim 是清除start和end之外的元素，如果start和end获取出来的数据为空，则会清空整个list
	3. zset
		3.1 有序不重复，可以用于栈顶数据，比如这次cms，保留前面n个视频缓存
		3.2 zrevrange z1 0 1 可以逆序获得倒数第0个，倒数第1个；等同于zrange z1 -1 -1
	4. hyperLogLog
		4.1 可用于uv的统计，初步估算，并不能精确地计算(如果数值比较大)
		4.2 就是将一个字符串，转换为64位的hash值，然后取前14位的二进制做为桶的下标(2^14=16384)，再根据剩下的50位，看出现1的位置(从右往左)，然后将其设置到桶的二进制里面(6位=2^6=64),所以总共的空间=2^14 * 6 / 2的10次方 / 8 = 12Kb
	5.	布隆过滤
		5.1 非精确地统计出是否出现过某元素
	6. hash
		6.1 hash-max-ziplist-entries 512  当超过512元素的时候，使用hashtable
		6.2 hash-max-ziplist-value 64 当value超过64个字符的时候，采用hashtable
		6.3 非6.1和6.2则使用ziplist