## JAVA

### 线程追踪
	1.线上有个job是利用java thread跑的，但是跑了一半，却停了。想要通过命令看看java线程是否还在运行中。(注意：必须以运行用户才能就stack，比如tomcat)
		jstack -l pid()

### ConcurrentHashMap的分段锁
	1. 会默认创建16的array节点，16个segement

### keytool
	1. 如何导入已有的keystore文件？
	2. 为何用keytool import证书的东西，没有办法在https使用呢?

### ehcache
	1. RMICacheManagerPeerListenerFactory。会通过hostName去Name.bind(//hostname:port/cacheName,..)；// 作为rmi的注册;默认是会注册LocalRegistry.registry(port),如果要开启hostname必须，在cache中写入RMICacheListener
	2. RMICacheManagerPeerProviderFactory中会每次在cache有改变的时候，去listRemoterPeer，uri利用“|”分割，例子：rmiUrls=//ip:port1/cache1|//ip2:port2/cache2，通过获取了RMICachePeer的rmi示例RMICachePeer_Stub，来远程调用

### 源码解读
	1. ArrayList
		1.1 数组大小重新分配策略，扩展容量(minCapacity > data.length)
		1.2 int i = max(10, len+1)
		1.3 capacity = length + length >> 1; 这里是扩展大小为原来大小的一半(length>>1，就是除于2的操作)
		1.4 重新分配数组大小，并拷贝原来的元素Arrays.copy(data,capacity)
	2. HashMap
		2.1 数据结构(array+link)，数组用于存在hash & (array.size-1)，由于array.size默认都是2的幂，所以hash & (array.size-1)等价于hash %(array.size - 1)取模，来取对应的桶位置
		2.2 hash的取值：(k = key.hashCode()) ^ (k >>> 16)。 


### spring mvc中如果不返回接口，或者返回null
	1. 没有@ResponseBody，默认采用了ModelAndViewReturnValueHandler来处理
	2. 会设置默认的view为requestUri
	3. 查找对应的view进行forward

### response.sendRedirect
	1. 会把response.appCommited设为true，这样在最后view.render的时候，会判断response是否已经提交或者关闭，采用了include的方式。
	2. response.close也是同上的操作。
	3. 如果通过response.setStatus，这时候又不去close response，就会导致forward view，然后status变成了404。如果302没有配套的location在responseHeader中，浏览器中不会做处理

### spring boot的属性文件加载
	1. 问题，在ai项目中，有三个mmc、mms、mmr三个module，这三个module有属性公用，比如数据库连接属性，redis属性，ccas、ppas、k8s等属性。但是每一个module都有一份相同属性配置的文件，没有把公共属性统一设置，线上需要对应修改三份属性
	   解决方法：可通过spring boot的--sprint.config.location=xxx来使用公共属性文件。
	2. 原理：spring boot会启动PropertySource(MapPropertySource location.properties)、PropertySource(MapPropertySource application.properties)。这样spring.config.location的内容会比外面来得高