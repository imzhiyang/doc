##Netty

### 为什么要引入workgroup和bossgroup呢？
     因为这里有使用到nio中的多路复用原则(reactor),这个可以后续看一下。主要是采用了selector.selectNow()关键