#!/bin/bash   ##shell脚本开始说明

base_tomcat='/cache2/opt/ott' #变量赋值
if [ ! -n "$2" ]; then  # if判断，前后必须空格
   echo " default tomcat path=$base_tomcat"
else
    base_tomcat=$2
fi

restart(){  #定义函数
     ps aux | grep 'java' | grep $1 | awk '{print $2}' | xargs kill -9 #找到进程中含有java，并传入第一个变量的进程，print $2打出进程号，xargs将之前的输出作为参数输入到kill中
     su - ott -c "$base_tomcat/$1/bin/start.sh" #以ott用户来执行后面的命令
}

status(){
    tomcat_tmp_dir="$1"
    st=`ps aux | grep 'java' | grep $tomcat_tmp_dir | awk '{print $2}'`  #赋值输出使用``来包含
    if [ ! -n "$st" ]; then
        #echo 'not run'
        return 0
    else
        #echo "run"
        return 1 
    fi
}

start(){
   echo "$1"
   `status $1`
   if [ $? == 0 ]; then #$?是status函数的返回值
     echo "$base_tomcat/$1/bin/start.sh"
     #su - ott -c "$base_tomcat/$1/bin/start.sh"
   fi
}

tomcat_dirs=`ls -l $base_tomcat | grep '^d' | awk '/tomcat-ott-/{print $NF}'` 
for tomcat_dir in $tomcat_dirs
do   
   case $1 in 
   start)
       #echo "start$tomcat_dir"
       start "$tomcat_dir"
       ;;
   restart)
       restart "$tomcat_dir"
       ;;
   *) echo "no command"
      ;;
   esac
done
