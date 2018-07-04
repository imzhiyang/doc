## Hadoop
### 安装过程中碰到的一些问题
    在windows下使用，由于缺少winutils.exe和hadoop.dll，导致运行时，出现unlink...的异常
### 3.0.0源码编译
    1. 安装protobuffer，主要是下载protoc-2.5.0-win32，并将其解压，添加到环境变量Path中。下载地址：https://github.com/google/protobuf/releases?after=v2.6.1
    2. 安装cmake,下载地址：https://cmake.org/download/，装的是cmake-3.10.1-win64-x64.msi
    3. maven，http://maven.apache.org/download.cgi，下载apache-maven-3.5.2-bin.zip，解压，然后把apache-maven-3.5.2\bin添加到path中
    4. jdk，安装1.8版本
    5. 项目需要使用bash命令，所以可下载git（不需要下载cygwin），然后将gin\bin添加到path中即可
    6. zlib安装，下载地址：http://gnuwin32.sourceforge.net/packages/zlib.htm，然后找到Complete package, except sources，对应的下载按钮，点击下载。安装完成之后，新建目录(zlib或者其它都行)，在GnuWin32的安装目录下，
       6.1 bin下面的zlib1.dll，拷贝到zlib
       6.2 include下面的两个文件也拷贝到zlib下
       6.3 修改zonf.h，我是版本zlib-1.2.3。所以是287行，或者找到(unistd.h这一行的位置)往上有#if 1这样的代码，把1改成HAVE_UNISTD_H，只改这一个。
       6.4 添加环境变量ZLIB_HOME,值为zlib的路径
    7. 安装Visual Studio 2010 Professional,之前我使用(Windows SDK 7.1会出错)，下载地址thunder://QUFodHRwOi8vYmlnMi5jcjE3My5jb20vL1ZTMjAxMFByb1RyaWFsQ0hTLnJhclpa。然后默认安装，或者自定义，都行
    8. 这时候找到Visual Studio 命令提示(2010)(或者进入普通的cmd，然后执行"C:\Program Files (x86)\Microsoft Visual Studio 10.0\VC\vcvarsall.bat"，这个就是visual的安装目录)，这个会帮你设置一些编译环境变量之类（其实就是设置一些环境变量，比如直接进入visual studio的cmd也是可以，从开始菜单->Microsoft vi..->命令提示符，同样对sdk 7.0也是一样的，Windows SDK 7.1 Command Prompt）。
    9. 执行set Platform=x64 设置操作系统的位数；如果是32位的话，set Platform=Win32 
    10. cd进入源码目录，然后根据执行mvn package -Pdist,native-win -DskipTests -Dtar。等待编译完成，hadoop-dist\target\hadoop-3.0.0就有对应的hadoop可用目录
