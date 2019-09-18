LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
                   src/com/litbig/app/music/aidl/IMusicService.aidl \
                   src/com/litbig/app/music/aidl/IMusicServiceCallback.aidl

LOCAL_RESOURCE_DIR += \
	$(LOCAL_PATH)/res \
	prebuilts/sdk/current/support/v7/appcompat/res \
	prebuilts/sdk/current/support/v7/gridlayout/res

LOCAL_PACKAGE_NAME := Litbig_Launcher

LOCAL_PRIVATE_PLATFORM_APIS := true

LOCAL_CERTIFICATE := platform
	
LOCAL_STATIC_JAVA_LIBRARIES += \
	android-support-v4 \
	android-support-v7-appcompat \
	android-support-v7-gridlayout \
	android-support-v13

LOCAL_PRIVILEGED_MODULE := true

LOCAL_AAPT_FLAGS += \
	--auto-add-overlay \
	--extra-packages android.support.v7.appcompat:android.support.v7.gridlayout

#LOCAL_PROGUARD_FLAGS := -include $(LOCAL_PATH)/proguard.flags

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

include $(BUILD_MULTI_PREBUILT)

#Use the following include to make instrumentation test cases
include $(call all-makefiles-under,$(LOCAL_PATH))