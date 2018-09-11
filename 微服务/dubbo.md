## Dubbo学习指标

### dubbo是什么？
   一套阿里巴巴的微服务框架，里面有Provider、consumer、registry、monitor等四大组件，并可扩展其它组件，比如进行网关控制之类
### 测试和生产公用一套zookeeper，怎么保证消费不冲突
用group或者version来区分
### zookeeper是如何保证事务的顺序一致性的
   #zxid

### zookeeper的leader选举
    类似于分布式锁的实现
    1. 节点1往zookeeper注册一个序号节点(node_000001)，节点2注册一个序号节点(node_00002)
    2. 检测序号值最小的作为leader，另外一个作为备用节点
    3. 如果leader丢失，则备用节点升为leader
    4. 节点在回复，则重新注册一个node_00003,作为备用leader 
### leader选举过程 

### 客户端对serverList的轮询机制
    1. startTime与warmup时间，
    2. 根据weight
    3. 根据本地的sequece

### ZK为什么不提供一个永久性的Watcher注册机制 
    1. zookeeper最主要是提供最新的数据
    2. 提供永久的监听器，可能导致没有必要的数据通知和释放

### 创建的临时节点什么时候会被删除，是连接一断就删除吗？延时是多少？
    立即删除的。 当客户端close之后，给zookeeper发完包，zookeeper马上response xid=-1表示事件通知
### 是否可以拒绝单个IP对ZK的访问,操作 

### ZooKeeper集群中服务器之间是怎样通信的？ 

### 出现调用超时com.alibaba.dubbo.remoting.TimeoutException异常怎么办？ 

### 出现java.util.concurrent.RejectedExecutionException或者Thread pool exhausted怎么办？  

### ZooKeeper
   1. 有序(根据客户端发送的顺序执行)
   2. 原子性(要吗成功，要吗失败),通过Leader来

### Zookeeper锁
   1. persist_node(持久化)，客户端关闭不会删除。需要手动删除
   2. persist_sequence(持久化顺序)
   3. ephemeral(临时)，关闭客户端自动删除，也可手动删除。不能创建子节点
   4. ephemeral_sequence(临时顺序)
### myid找不到问题处理
   在data目录新建myid文件并写入内容(server.x中的x值)
### stat not in white-lists
   在zoo.cfg中新增4lw.commands.whitelist

### monitor
   1. 在consumer.xml和provider.xml中添加<dubbo:monitor protocol="registry"/>。这是通过注册中心去同步monitor
   2. consumer在调用provider中的服务时，通过invoker中添加了MonitorFilter（由url中是否带有参数monitor）
   3. monitor在收集数据，一段周期后，通过consumer与provider的调用形式，上报给monitor