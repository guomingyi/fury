#ifndef __FP_H__
#define __FP_H__

//#include <utils/Log.h>
#include <android/log.h>
#ifdef __cplusplus
extern "C" {
#endif
/***************************************************************************/
#undef LOG_TAG
#define LOG_TAG  "[#fury-native]"

#if 1
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_FATAL  , LOG_TAG,__VA_ARGS__)
#else
#define LOGD(...)
#define LOGE(...)
#endif


/***************************************************************************/
#ifdef __cplusplus
}//extern "C"
#endif

#endif

