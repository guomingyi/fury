#  native.

LOCAL_PATH := $(call my-dir)
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
include $(CLEAR_VARS)

LOCAL_SRC_FILES += \
  fury_native.cpp

LOCAL_MODULE_TAGS := optional
LOCAL_SHARED_LIBRARIES += libcutils libutils 
LOCAL_PRELINK_MODULE := false
LOCAL_C_INCLUDES += $(JNI_H_INCLUDE) $(LOCAL_PATH)
LOCAL_MODULE := libfury_jni
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog

include $(BUILD_SHARED_LIBRARY)


