/*
 * Copyright (C) 2008 The Android Open Source Project
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
 */

#include <stdio.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include "jni.h"
#include <pthread.h>
#include "fury_native.h"

/*****************************************************************************************/
#define TAG LOG_TAG

/*****************************************************************************************/

static pthread_t _thd;
/*****************************************************************************************/

static int native_init(JNIEnv *env, jobject object, jobject obj)
{
	int fd = -1;
	LOGD("native_init...\n");

	fd = open("/dev/input/event2",O_RDONLY);
	if(fd < 0) {
		LOGD("cannot open: /dev/input/event2\n");
		return -1;
	}

	return fd;
}
static int native_monitor(JNIEnv *env, jobject object, jobject obj)
{
	int ret = 200;

	usleep(1000);
	return ret;
}

//java class name
static const char *classPathName = "com/fury/jni/Native";

static JNINativeMethod methods[] = {   
        { "native_init", "()I", (void *)native_init },
		{ "native_monitor", "()I", (void *)native_monitor },

};

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods) {
	jclass clazz;
	
	clazz = env->FindClass(className);
	if (clazz == NULL) {
		return JNI_FALSE;
	}
	if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
		return JNI_FALSE;
	}

	return JNI_TRUE;
}

/*
 * Register native methods for all classes we know about.
 *
 * returns JNI_TRUE on success.
 */
static int registerNatives(JNIEnv* env) {
	if (!registerNativeMethods(env, classPathName,
	             methods, sizeof(methods) / sizeof(methods[0]))) {
		return JNI_FALSE;
	}

	return JNI_TRUE;
}

/*
 * This is called by the VM when the shared library is first loaded.
 */
 
typedef union {
    JNIEnv* env;
    void* venv;
} UnionJNIEnvToVoid;

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	UnionJNIEnvToVoid uenv;
	uenv.venv = NULL;
	jint result = -1;
	JNIEnv* env = NULL;

	if (vm->GetEnv(&uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
		goto bail;
	}

	env = uenv.env;
	if (registerNatives(env) != JNI_TRUE) {
		goto bail;
	}

	result = JNI_VERSION_1_4;

	bail:
	return result;
}


