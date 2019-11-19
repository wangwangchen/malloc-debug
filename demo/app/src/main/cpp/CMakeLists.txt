cmake_minimum_required(VERSION 3.6.0)
set(ANDROID_NDK_REVISION 15)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 ")

add_library(hello-jni SHARED
            hello-jni.cpp)

# Include libraries needed for hello-jni lib
target_link_libraries(hello-jni
                      android
                      log)
