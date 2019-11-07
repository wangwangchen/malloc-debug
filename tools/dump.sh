#! /bin/bash

function info {
	echo "执行该脚本来打开应用的native内存泄露检测"
	echo "执行该脚本将导致应用程序重新启动"
	echo "[-n]: 应用程序包名，必填参数"
  echo "[-s]: 带符号表so所在目录"
}

if [[ $# = 0 ]]; then
  info
  exit 1
fi

# 当前目录
cur_dir=`dirname $0`

# 应用程序包名
package_name=""

# 带符号表so所在目录
symbols_dir=""

# 获取输入参数
while getopts "n:s:" opt; do
  case $opt in
    n)
      package_name=${OPTARG}
      ;;
	  s)
	    symbols_dir=${OPTARG}
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

# 确保NDK环境变量已经设置
OLD_IFS="$IFS" 
IFS=":" 
arr=(${PATH}) 
IFS="$OLD_IFS"
exit_obj=false
exit_addr2line=false 
for dir in ${arr[@]} 
do 
    if [[ -f "${dir}/toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64/bin/arm-linux-androideabi-objdump" ]]; then
    	# 设置别名，因为一会解析heap文件的python脚本需要用到
    	objdump="${dir}/toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64/bin/arm-linux-androideabi-objdump"
    	exit_obj=true
    fi
    if [[ -f "${dir}/toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64/bin/arm-linux-androideabi-addr2line" ]]; then
    	addr2line="${dir}/toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64/bin/arm-linux-androideabi-addr2line"
    	exit_addr2line=true
    fi
done

if [[ ${exit_obj} = false || ${exit_addr2line} = false ]]; then
	echo "请配置NDK环境变量"
	exit 1
fi


# 导出native内存信息到手机上的/data/local/tmp/heap.txt
adb shell am dumpheap -n ${package_name} /data/local/tmp/${package_name}.txt
# 因为上面这个命令是异步执行的，所以需要加休眠
sleep 5

package_name_full=${package_name}
OLD_IFS="$IFS" 
IFS=":" 
arr=(${package_name}) 
IFS="$OLD_IFS" 
if [[ ${#arr[@] > 0} ]]; then
  package_name=${arr[0]}
fi

# 把heap文件从手机上导出来
if [[ ! -d "${cur_dir}/heaps/" ]]; then
	mkdir "${cur_dir}/heaps/"
fi
adb pull /data/local/tmp/${package_name_full}.txt ${cur_dir}/heaps/

# 转换文件
if [[ ! -d "${cur_dir}/heaps_format/" ]]; then
	mkdir "${cur_dir}/heaps_format/"
fi

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
# 设置符号表so所在目录
if [[ -z ${symbols_dir} ]]; then
  symbols_dir="${cur_dir}/symbols/${package_path}/lib/arm"
fi
python ${cur_dir}/native_heapdump_viewer.py --html --reverse --package ${package_name} --objdump ${objdump} --addr2line ${addr2line} --symbols ${symbols_dir} ${cur_dir}/heaps/${package_name_full}.txt > ${cur_dir}/heaps_format/${package_name_full}.html
# python ./native_heapdump_viewer.py --html --reverse --symbols ${cur_dir}/symbols/ ./heaps/com.example.hellojni.txt > ./heaps_format/com.example.hellojni.html


# 打开文件
open ${cur_dir}/heaps_format/${package_name_full}.html




