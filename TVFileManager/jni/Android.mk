LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS := -llog

LOCAL_MODULE    := bdpush_V2_4
LOCAL_SRC_FILES := prebuilt/$(TARGET_ARCH_ABI)/libbdpush_V2_4.so

include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_LDLIBS := -llog -L$(LOCAL_PATH)/lib/x86 -lcutils
LOCAL_C_INCLUDES += $(LOCAL_PATH)

LOCAL_MODULE    := sambasetting_tvfm
LOCAL_SRC_FILES := sambasetting.cpp

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_LDLIBS := -llog -L$(LOCAL_PATH)/lib/x86 -lcutils
LOCAL_C_INCLUDES += $(LOCAL_PATH)

LOCAL_MODULE    := mountfolder
LOCAL_SRC_FILES := mountfolder.cpp

include $(BUILD_SHARED_LIBRARY)

#LOCAL_CERTIFICATE := platform
