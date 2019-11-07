#! /bin/bash

function info {
	echo "如果想要取消应用的内存泄露追踪，执行这个脚本"
	echo "[-n]: 应用程序包名，必填参数"
}

if [[ $# = 0 ]]; then
  info
  exit 1
fi

# 当前目录
cur_dir=`dirname $0`

# 应用程序包名
package_name=""

# 获取输入参数
while getopts "n:c:h" opt; do
  case $opt in
    n)
      package_name=${OPTARG}
      ;;
	h)
	  info
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

# 取消wrap配置
adb shell setprop wrap.${package_name} '""'

OLD_IFS="$IFS" 
IFS=":" 
arr=(${package_name}) 
IFS="$OLD_IFS" 
if [[ ${#arr[@] > 0} ]]; then
  package_name=${arr[0]}
fi
# 关闭进程
adb shell am force-stop ${package_name}