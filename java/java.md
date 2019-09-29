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

### jvm垃圾回收
	1. 验证parallel Scanvenge回收器中的GCTimeRatio的含义
	2. 垃圾回收打印的一些关键字(Allocation Failure 没有空间可分配了 Ergonomics开启了UseAdaptiveSizePolicy而触发的Full GC)

### JVM 内存分析
	1. 程序计数器，每个线程独立
		记录当前解释执行的程序指令(读取方法区中的那些字节码指令，比如第n条是aload2)
	2. 栈，线程独立 -Xss来设置
		存储每个执行的变量栈
	3. 堆(共用) -Xms -Xmx来指定最大和最小(一般两个值设置一样，防止在垃圾回收的时候，引起堆内存的重新分配)
		动态对象的分配内存,经常说的垃圾回收，一般就是指这个区(Eden、From、To，-Xmn来指定年轻代大小，由于采用复制拷贝，所以from和to只有一个会拿来使用,-XXNewRatio,默认为2，表示新生代占1/(1+2)),
	4. 方法区
		存储常量(final)、静态(static)、类信息描述等一些常量信息
		4.1 运行时方法区(字面量和符号引号，字面量就是一些值(1、2、"123"等)，符号引用就是在生成字节码的时候，类还没有分配内存，无法找到对应的类，而将一些类名统一用一些符号来表示)

### java类加载器
	同一个类名通过不同的类加载器加载，那么出来的Class的值是不一样的
	1. 加载，将字节码加载
	2. 连接
		2.1 验证 验证字节码是否有效
		2.2 准备，为final、static等变量分配空间，为方法区的分配空间
		2.3 解析，替换符号引用为对应的类指针(准备已经分配了指针)
	3. 初始化（init、cinit静态块）
	4. 使用
	5. 卸载

### 工作内存与主内存
	1. 工作内存就是在线程中，普通变量就是通过主内存读取(read)，然后加载到工作内存(load)中，或者通过assign（进行初始值），之后才能进行(use、store)
	
### synchronized与Reentrantlock
	1. 同样可以对内存进行同步加锁，但是lock需要手动release
	2. synchoronized是非公平，lock可以使用公平(默认不公平)
	3. wait、notify问题(synchronized需要多个对象，lock只需要newConditio后得到的condition，condition.wait和notify)
	4. synchoronized是可重入锁，ReentrantLock可重入锁
	5. 自选锁，是循环等待锁的获取，

### jvm的内存分配
	1. 内存回收
		1.1 GCRoot是本地变量表，静态变量表，常量变量表，native变量表
		1.2 当需要一次GC的时候，每一段代码都有自己的oopmap，这样就方便逆向去查找对应的GCRoots，不需要查找整个GCRoots
		1.3 GCRoot完，eden会进入到old
	2. String
		2.1 String str = "123"; String str1= new String("12") + new String("3");
			str1.intern(); 那么str1==str1.inern()吗？ 答案是false，因为123已经在常量池，str1.intern()，只会返回已有的常量池"123"，所以不相等
		2.2 String str = new String("12") + new String("3"); 那么str==str.intern()，答案是对的，因为intern是将串拷贝到常量池，也只是将引用拷贝过去，所以两者是相等

