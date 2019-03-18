## JAVA

### 线程追踪
	1.线上有个job是利用java thread跑的，但是跑了一半，却停了。想要通过命令看看java线程是否还在运行中。(注意：必须以运行用户才能就stack，比如tomcat)
		jstack -l pid()

### ConcurrentHashMap的分段锁
	1. 会默认创建16的array节点，16个segement