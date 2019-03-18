##Spring Boot

###jar如何替换内容继续运行
	首先spring boot目前都是以jar打包进行部署，但是这种部署有个缺点，所有的文件都在jar里面，
	    如果想要替换某个class，目前的方式都是重新打包，然后重新上传包，这样每次部署，上传包都带来了很大的时间消耗，于是就考虑是否只更新jar里面的内容？
	    1. 之前修改jar里面的Properties文件信息，可以通过vim的方式来进行修改，然后保存
	    2. 在windows下面，还可以通过以winrar的方式来拖拉修改文件，但是不能通过先解压出来，后面再压缩，改后缀名的方式。但是这种还有个极限，就是要将包在整个上传到服务器，又回到问题的初衷了。
	    2. 后面研究了jar的一些指令，来修改文件
	    3. jar -xvf xx.jar 会将jar解压出来
	    4. jar -uxf xxx.jar xxx 会将xxx更新到xxx.jar的里面。但是这里有个问题，这样会把xxx的内容进行压缩(DEFLATED)，但是Spring boot启动的需要没有压缩的情况才行
	    5. jar -uxf0（这个是阿拉伯数字0） xxx.jar xxx 这样就是没有压缩状态下，这样重新打包后的jar才能运行
	
	利用jar进行打包
		1. jar -cvf0 test.jar ./ 把当前目录的所有文件都打入jar，但是运行的时候，却报“中没有主清单属性”。说明的运行的主文件没有被识别。查看打包过程中，出现了“正在忽略条目META-INF/”。
		2. jar -cvfm0 test.jar xxx.mf文件 ./  这样即把main的执行文件清单打入，才行
	    
### idea调试spring boot jar
	1. idea新增jar application
     
### spring bean
	1. @Bean 属于factory的来生成bean
	2. @Service、@Component这种注解都属于普通的来生成bean，
	3. @Bean的优先级会高于@Service等，所以会出现@Bean覆盖@Service的BeanDefinition
		3.1 ClassPathBeanDefinitionScanner doScan 会根据目录的排序，扫描注册所有的类
		3.2 ConfigurationClassBeanDefinitionReader loadBeanDefinitionsForConfigurationClass会加载@Bean、@ImportResource、@Import等类定义
		3.3 所以才有了@Bean会把Service的类定义给覆盖
	4. 待研究，先解析全局对bean的定义Definition，然后需要注解用到实例的时候，才进行bean的create。
		4.1 如果scopeName是singleton，spring会在加载bean之后，直接先初始化实例到池中
		4.2 如果scopeName是prototype，就不会预先定义(需要用到才new，所以不必预先生成对象池)

