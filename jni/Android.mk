LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := bugu_lib
LOCAL_SRC_FILES := bugu_lib.c

include $(BUILD_SHARED_LIBRARY)