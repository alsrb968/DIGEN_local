LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
                   $(call all-Iaidl-files-under, src)

LOCAL_AIDL_INCLUDES := $(call all-Iaidl-files-under, src)

LOCAL_RESOURCE_DIR += \
	$(LOCAL_PATH)/res \
	prebuilts/sdk/current/support/v7/appcompat/res \
	prebuilts/sdk/current/support/v7/gridlayout/res

LOCAL_PACKAGE_NAME := Litbig_Photo

LOCAL_CERTIFICATE := platform

LOCAL_PRIVATE_PLATFORM_APIS := true

LOCAL_PRIVILEGED_MODULE := true
LOCAL_AAPT_FLAGS += \
	--auto-add-overlay \
	--extra-packages android.support.v7.appcompat:android.support.v7.gridlayout

LOCAL_STATIC_JAVA_LIBRARIES += \
	android-support-v4 \
	android-support-v7-appcompat \
	android-support-v7-gridlayout \
	android-support-v13 \
	libglide

#LOCAL_PROGUARD_FLAGS := -include $(LOCAL_PATH)/proguard.flags

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libandroid-support:libs/android-support-v4.jar libglide:libs/glide-full-4.7.0.jar

include $(BUILD_MULTI_PREBUILT)

#Use the following include to make instrumentation test cases
include $(call all-makefiles-under,$(LOCAL_PATH))