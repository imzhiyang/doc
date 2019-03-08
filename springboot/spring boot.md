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
	    
### idea调试spring boot jar
	1. idea新增jar application
     