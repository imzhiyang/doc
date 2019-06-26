## NIO

	NIO编程主要就是No Buffer IO，非阻塞的IO。其实就是利用一个channel，然后通过所有IO都往channel里面塞，然后循环获取channel的selectKeys，通过selectKey来处理数据

### acceptor、selector(管道)、selectKey分别处理
	1. 新建acceptor线程，通过accept来接收客户端的连接(configureBlock true),是阻塞获取的。通知poller
	2. 通过acceptor.accept之后，poller线程接到通知，来获取对应的selectKeys，将对应socketChannel注册到对应的selector里面，这样selector里面就可以通过selectKeys获取通道中(sc.register(selector, SelectionKey.OP_READ, "first");)
	3. 循环selectKeys，通过selectKey.interestOps(xx & (~OP_READ))，取消read的监听，这样我的selectKey就能进入线程中处理，不会再被外面的selector.selectKeys获取到
		3.1 这里有个问题，目前在sc.read返回=0时候，不知道是客户端还未输入完毕还是在等待服务端的输出，所以目前在read=0的注册了OP_READ和OP_WRITE的监听
		3.2 在OP_WRITE之后，又重新注册OP_READ的监听，直到后面read=-1，才将所有的流和管道关闭。
		3.3 tomcat下面是怎么判断client的socket流结束或者在等待输入。