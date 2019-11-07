#! /bin/bash

# 该脚本用来替换系统内的libc_malloc_debug.so文件

function info {
	echo "该脚本用来替换系统内的libc_malloc_debug.so文件"
	echo "[-r]: 如果手机是root，而不是userdebug系统，请加入该参数"
}

# if [[ $# = 0 ]]; then
# 	info
# 	exit 1
# fi

# 当前目录
cur_dir=`dirname $0`

# 是否是root手机
is_root=false

# 获取输入参数
while getopts "n:c:r" opt; do
  case $opt in
	r)
	  is_root=true
	  ;;
    ?)
      echo "error: 无效参数"
      exit 1
  esac
done

if [[ ${is_root} == true ]]; then
	adb push ./libc_malloc_debug.so /data/local/tmp/
	adb shell su -c 'mount -o remount,rw /system'
	adb shell su -c 'cp /data/local/tmp/libc_malloc_debug.so /system/lib/'
else
	adb root
	result=`adb disable-verity`
	if [[ ${result} =~ "reboot your device" ]]
	then
	    adb reboot
	    echo "正在重启手机，重启后再次执行脚本"
	    exit
	fi
	adb remount
	adb push ./libc_malloc_debug.so /system/lib/
fi

