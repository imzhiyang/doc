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
	   解决方法：可通过spring boot的--spring.config.location=xxx来使用公共属性文件。
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

### jdk1.8 tomcat的x-forward-protol失败
	1.问题原因应该是没有配置internalProxies代理信任，所以无法设置schema，添加了internalProxies之后，就能够根据ip地址来设置了

### 数据库多源的配置框架改造
	1. 背景：如果数据库中只配置了源，但是响应一直无法提升。瓶颈在数据库层面，所以考虑是否引入读写分离，从而思考能不能使得这个数据源能够更加人性化
		1.1 本来还想引入使用情况的检测，比如当前服务器最多16CPU，目前已经开启了16个连接，这时候可以采用其它数据源，就是使用尽量空闲的资源。目前只是使用随机分配
		1.2 既然引入了多数据源，那么原先在后台页面这些操作，必然应该使用主库，只有在接口层使用的时候才需要采用备的数据库点进行查询，于是采用aop，检测所有service的方法，是否有transaction的注解，然后进行源的选择，有DataSourceSelect注解则是只读，否则就是注
		1.3 上一步中涉及到一个问题，如果重复进入，如何计算最后是退出的状态，不需要重复选择源的问题，所以就有一个层次计数，进入一次加1，退出则剑1，直到为0，则表示退出，要把源关闭
	2. 多数据源的配置，前面是统一在cms.xml中配置好所有datasource，然后注解给MultiDataResourcePool，但是这样就显得文件很臃肿，扩展性又不强
		2.1 针对2中的问题，考虑如何能够动态扩展，于是将datasource的添加，到java文件去new对象，但是这里碰到一个问题，druid的后台监控，没有办法找到这个new出来的资源。
		2.2 针对上面的问题，采用的解决方案，以为将new出来的对象，直接register到BeanFactory，但是怎么获取BeanFactory有出现问题了
		2.3 针对2.2的情况，采用了bean配置中的scope=prototype，多实例，这个默认就是在beanFactory加入定义，但是不会马上新建实例，只有等到需要用的时候才会去建实例。延伸看到了parent，parent就是定义出一些公共属性，后面bean的创建直接继承这些属性即可。

### 单例模式
	1. 饿汉(一开始就实例化了属性)
	2. 懒汉(属性不初始化，等到要用才初始化)
	3. synchronized(等到要用的时候，还判断一下线程安全)
	4. 枚举类

### Transaction事务传递
	1. NOT SUPPORT，不支持事务，一般用于查询，并要设置readOnly=true，否则在事务结束之后，会自动进行commit
	2. REQUIRED	事务传递
	3. REQUIRED_NEW 新事务
	4. MANDATORY   在事务中，但是不能主动创建
	5. NEVET  不能有事务
	6. NEST   嵌套事务，如果此类发生异常，则不进行回退

	7 如果先是NOT SUPPORT接着再是REQUIRE的话，会是怎么一个流程？
		7.1 两者之间互调，会将事务进行supend(挂起)，区别是NOT SUPPORT的事务被挂起，会导致连接直接释放，而REQUIRED的则不是释放连接
		7.2 NOT SUPPORT不会开启事务，那么表示也就不会直接开启dbConnection，其开启hibernate的session是在SpringSessionContext的currentSession()方法中进行开启，同时向TransactionSynchronizationManager注册一个SpringSessionSynchronization对象。这样就在suspend事务中，会对connection进行回收关闭
### 字符集编码
	1. ascii，一个字节，只能表示127字符
	2. gbk，两个字节，可以用于表示大部分的中文
	3. unicode，国际统一字符，默认两个字节，在java中用"中".getBytes("unicode")的内容为0xFEFF 4e2d，0xFEFF是其的编码前缀，\u4e2d就能表示这个中字
	4. utf-8，可变字节来存储(前n位多少个1表示多少字节，n+1为0，然后每8位开头都是10)
		4.1 0xxxxxxx-0111111用于表示一个字节的内容
		4.2 110xxxxx 10xxxxxx 用于表示两个字节
		4.3 1110xxxx 10xxxxxx 10xxxxxx 用于表示3个字节的内容 

### 线程
	1. 守护线程，当java程序中没有用户线程，都是守护线程，则程序就会停止退出