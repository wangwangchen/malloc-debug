/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <jni.h>
#include <malloc.h>
#include <stdlib.h>
#include <android/log.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_example_hellojni_HelloJni_testNativeLeak(JNIEnv *env, jobject instance) {
    char *p;

    p = (char *)malloc(1024 * 1024);

    // memset(NULL, 97, 1024 * 1024);
    memset(p, 97, 1024 * 1024);

    // 不断的申请内存，但是不释放。
    // free(p);
}

extern "C"
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    char *pathvar;
    pathvar = getenv("LIBC_DEBUG_MALLOC_OPTIONS");
    __android_log_print(ANDROID_LOG_ERROR, "cww", "LIBC_DEBUG_MALLOC_OPTIONS: %s", pathvar);

    return JNI_VERSION_1_6;
}