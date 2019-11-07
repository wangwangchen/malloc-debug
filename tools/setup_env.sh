#! /bin/bash

function info {
	echo "执行该脚本来打开应用的native内存泄露检测"
	echo "执行该脚本将导致应用程序重新启动"
	echo "[-n]: 应用程序包名，必填参数"
	echo "[-d]: 记录的内存申请调用栈深度，默认是8"
	echo "[-r]: 如果手机是root，而不是userdebug系统，请加入该参数"
}

if [[ $# = 0 ]]; then
	info
	exit 1
fi

# 当前目录
cur_dir=`dirname $0`

# 应用程序包名
package_name=""
# malloc debug配置参数
malloc_deep=8
# 是否是root手机
is_root=false

# 获取输入参数
while getopts "n:d:r" opt; do
  case $opt in
    n)
      package_name=${OPTARG}
      ;;
    d)
	  malloc_deep=${OPTARG}
	  ;;
	r)
	  is_root=true
	  ;;
    ?)
      echo "error: 无效参数"
      exit 1
  esac
done

# 必须输入包名，-z 检测字符串长度是否为0， 为0返回true
if [[ -z ${package_name} ]]; then
	echo "error: 必须输入包名"
	exit 1
fi


####################### 开始启动malloc debug #######################

# 手机进入root，因为是耗时方法，所以需要用``括起来
adb root
sleep 1- 

# 设置wrap环境
if [[ ${malloc_deep} -le 0 ]]; then
	echo "config malloc debug with default config"
	if [[ ${is_root} == true ]]; then
		adb shell su -c "setprop wrap.${package_name} '\"LIBC_DEBUG_MALLOC_OPTIONS=backtrace=8\"'"
	else
		adb shell setprop wrap.${package_name} '"LIBC_DEBUG_MALLOC_OPTIONS=backtrace=8"'	
	fi
else
	echo "config malloc debug with diy config"
	if [[ ${is_root} == true ]]; then
		adb shell su -c "setprop wrap.${package_name} '\"LIBC_DEBUG_MALLOC_OPTIONS=backtrace=${malloc_deep}\"'"
	else
		adb shell setprop wrap.${package_name} "LIBC_DEBUG_MALLOC_OPTIONS=backtrace=${malloc_deep}"
	fi
fi

OLD_IFS="$IFS" 
IFS=":" 
arr=(${package_name}) 
IFS="$OLD_IFS" 
if [[ ${#arr[@] > 0} ]]; then
  package_name=${arr[0]}
fi
# 关闭进程
adb shell am force-stop ${package_name}

# 休息1S
# sleep 1

# 启动应用，这里采用monkey程序来启动，只需要包名就能启动
adb shell monkey -p ${package_name} 1

# 创建用来存储带符号表so的目录
# 获取手机APP安装路径
package_path=`adb shell pm path ${package_name}`
# 分割路径
OLD_IFS="$IFS" 
IFS="\ " 
arr=(${package_path}) 
IFS="$OLD_IFS"
for dir in ${arr[@]} 
do
	package_path=${dir}
	break
done
# 再次分割
package_path=${package_path:8}
OLD_IFS="$IFS" 
IFS="\/" 
arr=(${package_path}) 
IFS="$OLD_IFS"
exit_app_fold=false
# 拼凑本地路径
package_path=""
for dir in ${arr[@]} 
do
	package_path+="${dir}/"
	if [[ ${exit_app_fold} == true ]]; then
		break;
	fi
	if [[ ${dir} == "app" ]]; then
		exit_app_fold=true
	fi
done

function tipSymbols {
	show_confirm=false
	if [[ $1 == true ]]; then
		show_confirm=true
	fi
	echo ""
	echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
	echo "> 请把最新的带符号表的so放在目录(dump信息的时候需要用到)："
	echo "${cur_dir}/symbols/${package_path}lib/arm"
	if [[ ${show_confirm} == true ]]; then
		echo "如果已经放入了，请忽略此信息"	
	fi
	echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
}

# 创建路径
if [[ ! -d "${cur_dir}/symbols/${package_path}lib/arm" ]]; then
	mkdir -p "${cur_dir}/symbols/${package_path}lib/arm"
fi

# if [ "`ls -A ${cur_dir}/symbols/${package_path}lib/arm`" = "" ]; then
#   	tipSymbols false
# else
#   	tipSymbols true
# fi

