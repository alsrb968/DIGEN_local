#
# Copyright (C) 2012 Telechips, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# GTVS package
ifeq ($(GTVS_BUILD),true)
$(call inherit-product, vendor/google/products/gms.mk)
PRODUCT_PROPERTY_OVERRIDES += ro.com.google.clientidbase=android-telechips-tv
endif

ifeq ($(TARGET_PREBUILT_KERNEL),)
LOCAL_KERNEL := kernel/arch/arm/boot/zImage
else
LOCAL_KERNEL := $(TARGET_PREBUILT_KERNEL)
endif

PRODUCT_SHIPPING_API_LEVEL := 28

TARGET_BOARD_SOC := tcc899x
KERNEL_VERSION := v4_14
ARM_VERSION := arm

# vpu 4k-d2 mode : reproduces different media performance
# : make in sync. with cq depth in kernel/include/video/tcc/TCC_VPU_4K_D2.h
# : WAVE4_COMMAND_QUEUE_DEPTH - 1 (cq1), 2 (cq2)
KERNEL_VPU_4KD2_MODE := cq1
#KERNEL_VPU_4KD2_MODE := cq2

# DolbyVision Support
#BOARD_HDMI_DOLBYVISION := true
#DOLBYVISION_VER := v2_3_GA
DOLBYVISION_VER := v2_4_2_GA
#BOARD_HDMI_DOLBYVISION_DEV := true
#BOARD_HDMI_DOLBYVISION_DEV_SPLITER := true

ifeq ($(DOLBYVISION_VER),v2_3_GA)
PRODUCT_PROPERTY_OVERRIDES += \
	persist.vendor.tcc.dolbyvision_preferred_std = 1
endif

USB_DEFAULT_HOST := false
USE_MASS_STORAGE := false

# Wi-Fi defines
DEVICE_USES_REALTEK_WIFI := false
DEVICE_USES_BROADCOM_WIFI := true
DEVICE_USES_MARVELL_WIFI := false

USE_FB_4K := false
ifeq ($(USE_MASS_STORAGE),true)
DEVICE_PACKAGE_OVERLAYS := device/telechips/tcc899x/ums/overlay
endif

ifeq ($(TV_DEVICE_BUILD),true)
ifeq ($(CTS_BUILD),true)
DEVICE_PACKAGE_OVERLAYS += device/telechips/tcc899x/atv/cts_overlay
else
DEVICE_PACKAGE_OVERLAYS += device/telechips/tcc899x/atv/overlay
endif
else
DEVICE_PACKAGE_OVERLAYS += device/telechips/tcc899x/overlay
endif

ifeq ($(TV_DEVICE_BUILD),true)
PRODUCT_AAPT_CONFIG := normal large xlarge xhdpi
else
PRODUCT_AAPT_CONFIG := normal large xlarge mdpi
endif

# Define for Output Mode
PRODUCT_PROPERTY_OVERRIDES += \
        persist.vendor.tcc.output_mode = 1

# Define for HDMI
# Setting Menu enable
PRODUCT_PROPERTY_OVERRIDES += \
	ro.vendor.system.hdmi_active = true

# For Extenddisplay
PRODUCT_PROPERTY_OVERRIDES += \
	persist.vendor.tcc.hdmi_resize_up = 0 \
	persist.vendor.tcc.hdmi_resize_dn = 0 \
	persist.vendor.tcc.hdmi_resize_lt = 0 \
	persist.vendor.tcc.hdmi_resize_rt = 0 \
	vendor.tcc.hdmi.uisize = 1920x1080 \
	persist.vendor.tcc.hdmi_resolution = 125 \
	persist.vendor.tcc.hdmi_mode = 0 \
	persist.vendor.tcc.hdmi_aspect_ratio = 0 \
	persist.vendor.tcc.hdmi_color_space = 125 \
	persist.vendor.tcc.hdmi_color_depth = 0

# HDMI extra mode (1-bit:HDR10, 2-bit:HLG
PRODUCT_PROPERTY_OVERRIDES += \
	persist.vendor.tcc.hdmi_extra_mode = 3

# MADI(De-Interlacer)
PRODUCT_PROPERTY_OVERRIDES += vendor.tcc.video.deinterlace.madi = 0

# GC420 Compression mode
#PRODUCT_PROPERTY_OVERRIDES += vendor.hwc.hwc.2d.compression = 1

# Define for CEC
# Setting Menu enable
PRODUCT_PROPERTY_OVERRIDES += \
	ro.vendor.system.cec_active = true

# HDMI HAL
ifeq ($(TV_DEVICE_BUILD),true)
PRODUCT_PROPERTY_OVERRIDES += \
	ro.hdmi.device_type = 4
	ro.hdmi.set_menu_language = true
endif

# Define for Composite
# Setting Menu enable
PRODUCT_PROPERTY_OVERRIDES += \
	ro.vendor.system.composite_active = true \
	persist.vendor.tcc.composite_resize_up = 3 \
	persist.vendor.tcc.composite_resize_dn = 3 \
	persist.vendor.tcc.composite_resize_lt = 3 \
	persist.vendor.tcc.composite_resize_rt = 3 \
	persist.vendor.tcc.composite_mode = 0

# Define for Component
# Setting Menu enable
PRODUCT_PROPERTY_OVERRIDES += \
	ro.vendor.system.component_active = true \
	persist.vendor.tcc.component_mode = 0

# Component Settings
PRODUCT_PROPERTY_OVERRIDES += \
	persist.vendor.tcc.component_resize_up = 3 \
	persist.vendor.tcc.component_resize_dn = 3 \
	persist.vendor.tcc.component_resize_lt = 3 \
	persist.vendor.tcc.component_resize_rt = 3

# Component Settings for ADV7343 Component Chip
PRODUCT_PROPERTY_OVERRIDES += \
	persist.vendor.tcc.component_720_up = 3 \
	persist.vendor.tcc.component_720_dn = 7 \
	persist.vendor.tcc.component_720_lt = 0 \
	persist.vendor.tcc.component_720_rt = 10 \
	persist.vendor.tcc.component_1080_up = 9 \
	persist.vendor.tcc.component_1080_dn = 4 \
	persist.vendor.tcc.component_1080_lt = 0 \
	persist.vendor.tcc.component_1080_rt = 10

# Support Virtual Display option
# (CVBS)      tcc.hwc.use.cvbs.presentation = 1 & set ro.vendor.system.composite_active = false
# (Component) tcc.hwc.use.com.presentation = 1  & set ro.vendor.system.component_active = false
PRODUCT_PROPERTY_OVERRIDES += \
	vendor.tcc.hwc.use.cvbs.presentation = 0 \
	vendor.tcc.hwc.use.com.presentation = 0

# Define for Setting Screen app
PRODUCT_PROPERTY_OVERRIDES += \
	persist.vendor.tcc.auto_resolution = 1

PRODUCT_PROPERTY_OVERRIDES += \
	persist.vendor.tcc.renderer_onthefly = true

PRODUCT_PROPERTY_OVERRIDES += \
	ro.vendor.system.audio_active = true \
	persist.vendor.tcc.spdif_setting = 0

# Define TCC set-top/media box solution
PRODUCT_PROPERTY_OVERRIDES += \
	vendor.tcc.solution.mbox = 1 \
	vendor.tcc.solution.video = 1 \
	vendor.tcc.solution.preview = 0 \
	vendor.tcc.solution.mbox.sleep = 1 \

# Define TCC internal subtitle
PRODUCT_PROPERTY_OVERRIDES += \
	tcc.internal.subtitle = 1

# Define skip meta of video & image
#
PRODUCT_PROPERTY_OVERRIDES += \
        vendor.tcc.solution.skipVideoMeta = 1 \
        vendor.tcc.solution.skipImageMeta = 1

#FEATURE_OPENGLES_EXTENSION_PACK support string config file
PRODUCT_COPY_FILES += \
	frameworks/native/data/etc/android.hardware.opengles.aep.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.opengles.aep.xml

PRODUCT_COPY_FILES += \
	$(LOCAL_KERNEL):kernel \
	device/telechips/$(TARGET_BOARD_SOC)/init.recovery.$(TARGET_BOARD_SOC).rc:recovery/root/init.recovery.$(TARGET_BOARD_SOC).rc \
	device/telechips/$(TARGET_BOARD_SOC)/ueventd.$(TARGET_BOARD_SOC).rc:$(TARGET_COPY_OUT_VENDOR)/ueventd.rc \
	device/telechips/$(TARGET_BOARD_SOC)/init.$(TARGET_BOARD_SOC).usb.rc:$(TARGET_COPY_OUT_VENDOR)/etc/init/init.$(TARGET_BOARD_SOC).usb.rc \
	device/telechips/$(TARGET_BOARD_SOC)/init.$(TARGET_BOARD_SOC).wifi.realtek.rc:$(TARGET_COPY_OUT_VENDOR)/etc/init/init.$(TARGET_BOARD_SOC).wifi.realtek.rc \
	device/telechips/$(TARGET_BOARD_SOC)/init.$(TARGET_BOARD_SOC).wifi.broadcom.rc:$(TARGET_COPY_OUT_VENDOR)/etc/init/init.$(TARGET_BOARD_SOC).wifi.broadcom.rc \
	device/telechips/$(TARGET_BOARD_SOC)/init.$(TARGET_BOARD_SOC).wifi.marvell.rc:$(TARGET_COPY_OUT_VENDOR)/etc/init/init.$(TARGET_BOARD_SOC).wifi.marvell.rc

# copy device tree(.dtb) files
PRODUCT_COPY_FILES += \
	kernel/arch/$(ARM_VERSION)/boot/dts/tcc/tcc8990-android-lpd4322-v0.1.dtb:tcc8990-android-lpd4322-v0.1.dtb

PRODUCT_COPY_FILES += \
        device/telechips/$(TARGET_BOARD_SOC)/init.tcc899x.emmc.rc:$(TARGET_COPY_OUT_VENDOR)/etc/init/init.tcc899x.emmc.rc \
        device/telechips/$(TARGET_BOARD_SOC)/init.tcc899x.emmc.rc:root/init.recovery.tcc899x.emmc.rc \
        device/telechips/$(TARGET_BOARD_SOC)/fstab.tcc8990.stb:$(TARGET_COPY_OUT_VENDOR)/etc/fstab.tcc899x \
        device/telechips/$(TARGET_BOARD_SOC)/recovery.fstab.tcc899x:recovery/root/etc/factory.fstab

ifeq ($(USE_MASS_STORAGE),true)
PRODUCT_COPY_FILES += \
	device/telechips/$(TARGET_BOARD_SOC)/ums/init.tcc899x.rc:$(TARGET_COPY_OUT_VENDOR)/etc/init/init.tcc899x.rc
else
PRODUCT_COPY_FILES += \
	device/telechips/$(TARGET_BOARD_SOC)/init.tcc899x.rc:$(TARGET_COPY_OUT_VENDOR)/etc/init/init.tcc899x.rc
endif

#for bluetooth
ifeq ($(DEVICE_USES_MARVELL_WIFI),true)
PRODUCT_PACKAGES += \
    libbt-vendor \
    android.hardware.bluetooth@1.0-impl-mrvl \
    android.hardware.bluetooth@1.0-service \
    fmapp
else
PRODUCT_PACKAGES += \
    libbt-vendor \
    android.hardware.bluetooth@1.0-impl \
    android.hardware.bluetooth@1.0-service

PRODUCT_PACKAGES += \
	avinfo \
	hciconfig \
	hcitool \
	l2ping \
	bccmd \
	bcmtool \
	rfcomm \
	bt_vendor.conf
endif

PRODUCT_PACKAGES += \
	audio.a2dp.default \
	audio.r_submix.default \
	audio.usb.default

# Bluetooth config file
#PRODUCT_COPY_FILES += \
    system/bluetooth/data/main.nonsmartphone.conf:system/etc/bluetooth/main.conf \

# audio mixer paths
#PRODUCT_COPY_FILES += \
#    device/asus/grouper/mixer_paths.xml:system/etc/mixer_paths.xml

# audio policy configuration
PRODUCT_COPY_FILES += \
    device/telechips/$(TARGET_BOARD_SOC)/audio_policy.conf:$(TARGET_COPY_OUT_VENDOR)/etc/audio_policy.conf

# Prebuilted NTFS driver modules
#
# Removed due to compile error
PRODUCT_COPY_FILES += \
	device/telechips/$(TARGET_BOARD_SOC)/ufsd_$(TARGET_BOARD_SOC)_$(KERNEL_VERSION).ko:$(TARGET_COPY_OUT_VENDOR)/lib/ntfs/ufsd.ko \
	device/telechips/$(TARGET_BOARD_SOC)/jnl_$(TARGET_BOARD_SOC)_$(KERNEL_VERSION).ko:$(TARGET_COPY_OUT_VENDOR)/lib/ntfs/jnl.ko

# Prebuilt NTFS tools
#
PRODUCT_COPY_FILES += \
	device/telechips/$(TARGET_BOARD_SOC)/mkntfs:$(TARGET_COPY_OUT_VENDOR)/bin/mkntfs \
	device/telechips/$(TARGET_BOARD_SOC)/chkntfs:$(TARGET_COPY_OUT_VENDOR)/bin/chkntfs\
	device/telechips/$(TARGET_BOARD_SOC)/chkntfs:$(TARGET_COPY_OUT_VENDOR)/bin/chkufsd

PRODUCT_COPY_FILES += \
    frameworks/av/media/libstagefright/data/media_codecs_google_audio.xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs_google_audio.xml \
    frameworks/av/media/libstagefright/data/media_codecs_google_telephony.xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs_google_telephony.xml \
    frameworks/av/media/libstagefright/data/media_codecs_google_video.xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs_google_video.xml

ifeq ($(CTS_BUILD),true)
PRODUCT_COPY_FILES += \
    device/telechips/$(TARGET_BOARD_SOC)/media_codecs_$(TARGET_BOARD_SOC)_$(KERNEL_VPU_4KD2_MODE)_cts.xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs.xml
else
PRODUCT_COPY_FILES += \
    device/telechips/$(TARGET_BOARD_SOC)/media_codecs_$(TARGET_BOARD_SOC).xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs.xml
endif

# avoid CTS fail Oero-m1
# android.permission2.cts.PrivappPermissionsTest#testPrivappPermissionsEnforcement
#

ifeq ($(TV_DEVICE_BUILD),true)
ifeq ($(CTS_BUILD),true)
PRODUCT_COPY_FILES += \
	device/telechips/$(TARGET_BOARD_SOC)/privapp-permissions-android.ext.services.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/privapp-permissions-android.ext.services.xml \
	device/telechips/$(TARGET_BOARD_SOC)/privapp-permissions-com.android.systemui.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/privapp-permissions-com.android.systemui.xml \
	device/telechips/$(TARGET_BOARD_SOC)/privapp-permissions-com.android.tv.settings.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/privapp-permissions-com.android.tv.settings.xml
endif
endif

ifeq ($(CTS_BUILD),true)
PRODUCT_COPY_FILES += device/telechips/$(TARGET_BOARD_SOC)/media_codecs_performance_$(TARGET_BOARD_SOC)_$(KERNEL_VPU_4KD2_MODE)_cts.xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs_performance.xml
else
PRODUCT_COPY_FILES += device/telechips/$(TARGET_BOARD_SOC)/media_codecs_performance_$(TARGET_BOARD_SOC).xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs_performance.xml
endif

#vendor seccomp
PRODUCT_COPY_FILES += \
    device/telechips/common/seccomp_policy/mediaextractor.policy:$(TARGET_COPY_OUT_VENDOR)/etc/seccomp_policy/mediaextractor.policy \
    device/telechips/common/seccomp_policy/mediacodec.policy:$(TARGET_COPY_OUT_VENDOR)/etc/seccomp_policy/mediacodec.policy

#for coldboot
PRODUCT_COPY_FILES += \
	kernel/drivers/usb/host/ehci-hcd.ko:$(TARGET_COPY_OUT_VENDOR)/lib/usb/host/ehci-hcd.ko \
	kernel/drivers/usb/host/ehci-platform.ko:$(TARGET_COPY_OUT_VENDOR)/lib/usb/host/ehci-platform.ko \
	kernel/drivers/usb/host/ehci-tcc.ko:$(TARGET_COPY_OUT_VENDOR)/lib/usb/host/ehci-tcc.ko \
	kernel/drivers/usb/host/ohci-hcd.ko:$(TARGET_COPY_OUT_VENDOR)/lib/usb/host/ohci-hcd.ko \
	kernel/drivers/usb/host/ohci-platform.ko:$(TARGET_COPY_OUT_VENDOR)/lib/usb/host/ohci-platform.ko \
	kernel/drivers/usb/host/ohci-tcc.ko:$(TARGET_COPY_OUT_VENDOR)/lib/usb/host/ohci-tcc.ko \
	kernel/drivers/usb/host/xhci-hcd.ko:$(TARGET_COPY_OUT_VENDOR)/lib/usb/host/xhci-hcd.ko \
	kernel/drivers/usb/host/xhci-plat-hcd.ko:$(TARGET_COPY_OUT_VENDOR)/lib/usb/host/xhci-plat-hcd.ko \
	kernel/drivers/usb/storage/usb-storage.ko:$(TARGET_COPY_OUT_VENDOR)/lib/usb/storage/usb-storage.ko \
	kernel/drivers/usb/dwc_otg/v3.20a/tcc_dwc_otg.ko:$(TARGET_COPY_OUT_VENDOR)/lib/usb/dwc_otg/tcc_dwc_otg.ko \
	kernel/drivers/usb/dwc3/dwc3.ko:$(TARGET_COPY_OUT_VENDOR)/lib/usb/dwc3/dwc3.ko \
	kernel/drivers/usb/dwc3/dwc3-tcc.ko:$(TARGET_COPY_OUT_VENDOR)/lib/usb/dwc3/dwc3-tcc.ko \

# VPU(Video Manager/Encoder/Decoder)
PRODUCT_COPY_FILES += \
	device/telechips/common/vpu_d6_lib_$(ARM_VERSION)_$(KERNEL_VERSION).ko:$(TARGET_COPY_OUT_VENDOR)/lib/vpu/vpu_lib.ko \
	kernel/drivers/char/vpu/vpu.ko:$(TARGET_COPY_OUT_VENDOR)/lib/vpu/vpu.ko \
	device/telechips/common/jpu_c6_lib_$(ARM_VERSION)_$(KERNEL_VERSION).ko:$(TARGET_COPY_OUT_VENDOR)/lib/vpu/jpu_lib.ko \
	kernel/drivers/char/vpu/jpu_dev.ko:$(TARGET_COPY_OUT_VENDOR)/lib/vpu/jpu_dev.ko \
	device/telechips/common/vpu_4k_d2_lib_$(ARM_VERSION)_$(KERNEL_VERSION).ko:$(TARGET_COPY_OUT_VENDOR)/lib/vpu/vpu_4k_d2_lib.ko \
	kernel/drivers/char/vpu/vpu_4k_d2_dev.ko:$(TARGET_COPY_OUT_VENDOR)/lib/vpu/vpu_4k_d2_dev.ko

# Audio Driver (T-sound or not)
PRODUCT_COPY_FILES += \
	kernel/sound/soc/tcc/tcc_adma_pcm.ko:$(TARGET_COPY_OUT_VENDOR)/lib/sound/tcc_adma_pcm.ko \
	kernel/sound/soc/tcc/tcc_i2s.ko:$(TARGET_COPY_OUT_VENDOR)/lib/sound/tcc_i2s.ko \
	kernel/sound/soc/tcc/tcc_spdif.ko:$(TARGET_COPY_OUT_VENDOR)/lib/sound/tcc_spdif.ko \
	kernel/sound/soc/tcc/tcc_cdif.ko:$(TARGET_COPY_OUT_VENDOR)/lib/sound/tcc_cdif.ko \
	kernel/sound/soc/tcc/tcc_audio_chmux.ko:$(TARGET_COPY_OUT_VENDOR)/lib/sound/tcc_audio_chmux.ko \
	kernel/sound/soc/tcc/tcc-snd-card.ko:$(TARGET_COPY_OUT_VENDOR)/lib/sound/tcc-snd-card.ko

PRODUCT_COPY_FILES += \
	kernel/sound/soc/codecs/snd-soc-wm8524.ko:$(TARGET_COPY_OUT_VENDOR)/lib/sound/snd-soc-wm8524.ko

# Media Profiles
PRODUCT_COPY_FILES += \
	device/telechips/$(TARGET_BOARD_SOC)/media_profiles_V1_0.xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_profiles_V1_0.xml

#tinyalsa utilities
PRODUCT_PACKAGES += \
	tinyplay \
	tinycap \
	tinymix

# Audio Mixer Path config
PRODUCT_COPY_FILES += \
 hardware/telechips/common/audio/mixer_paths_wm8731.xml:$(TARGET_COPY_OUT_VENDOR)/etc/mixer_paths_wm8731.xml \
 hardware/telechips/common/audio/mixer_paths_es8388.xml:$(TARGET_COPY_OUT_VENDOR)/etc/mixer_paths_es8388.xml \
 hardware/telechips/common/audio/mixer_paths_wm8988.xml:$(TARGET_COPY_OUT_VENDOR)/etc/mixer_paths_wm8988.xml \
 hardware/telechips/common/audio/mixer_paths_wm8524.xml:$(TARGET_COPY_OUT_VENDOR)/etc/mixer_paths_wm8524.xml \
 hardware/telechips/common/audio/mixer_paths_rt5633.xml:$(TARGET_COPY_OUT_VENDOR)/etc/mixer_paths_rt5633.xml \
 hardware/telechips/common/audio/mixer_paths_rt5631.xml:$(TARGET_COPY_OUT_VENDOR)/etc/mixer_paths_rt5631.xml \
 hardware/telechips/common/audio/mixer_paths_rt5625.xml:$(TARGET_COPY_OUT_VENDOR)/etc/mixer_paths_rt5625.xml

# ION Memory
PRODUCT_PACKAGES += libion

# Define TCC video vsync mode
PRODUCT_PROPERTY_OVERRIDES += \
	vendor.tcc.video.vsync.support = 1 \
	vendor.tcc.video.vsync.threshold = 0 \
	vendor.tcc.video.surface.support = 1 \
	vendor.tcc.video.mvc.support = 0 \
	vendor.tcc.video.mvc.enable = 0

# Define aspect ratio setting for video screen
# vendor.tcc.video.arc.ratio (0: 4:3 ratio, 1: 16:9 ratio)
# vendor.tcc.video.arc.screen (0: full screen, 1: original ratio)
PRODUCT_PROPERTY_OVERRIDES += \
	vendor.tcc.video.arc.enable = 0 \
	vendor.tcc.video.arc.ratio = 0 \
	vendor.tcc.video.arc.screen = 0

# Define TCC video deinteralce mode, this is a sub item of vsync mode, so you have to enable vsync mode first to use deinterlace mode.
PRODUCT_PROPERTY_OVERRIDES += \
	vendor.tcc.video.deinterlace.support = 0 \
	vendor.tcc.video.m2m_60hz.viqe_support = 1

# Gallery is used only image
PRODUCT_PROPERTY_OVERRIDES += \
	vendor.tcc.solution.onlyimage = 1

# Define EXCLUSIVE UI
PRODUCT_PROPERTY_OVERRIDES += \
	vendor.tcc.exclusive.ui.enable = 0

# Define 3D UI mode
PRODUCT_PROPERTY_OVERRIDES += \
	vendor.tcc.3d.ui.enable = 0

# Define display mode for external output (0:normal, 1:auto-detection(HDMI<->CVBS), 2:HDMI/CVBS<->CVBS, 3:HDMI/CVBS<->CVBS/Component)
PRODUCT_PROPERTY_OVERRIDES += \
	persist.vendor.tcc.display.mode = 0

# Define display path for mouse in composite output (0:framework, 1:kernel)
PRODUCT_PROPERTY_OVERRIDES += \
	vendor.tcc.composite.mouse.path = 1

# Define properties releated STB for using files which will be release library
PRODUCT_PROPERTY_OVERRIDES += \
	vendor.tcc.display.output.stb = 1 \
	persist.vendor.tcc.component.chip = adv7343

ifeq ($(DEVICE_USES_MARVELL_WIFI),true)
PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0 \
	wifi.supplicant_scan_interval=15
else
PRODUCT_PROPERTY_OVERRIDES += \
	wifi.interface=wlan0 \
	wifi.supplicant_scan_interval=15
endif

# Set default USB interface
ifeq ($(USB_DEFAULT_HOST),true)
PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
    persist.vendor.usb.config.to.system=host
else ifeq ($(USB_DEFAULT_HOST),false)
ifeq ($(USE_MASS_STORAGE),true)
PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
    persist.vendor.usb.config.to.system=mass_storage
else ifeq ($(USE_MASS_STORAGE),false)
PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
    persist.vendor.usb.config.to.system=mtp,adb
endif
endif

# Product Package
ifeq ($(TV_DEVICE_BUILD),true)
#PRODUCT_PACKAGES += \
    TelechipsSystemUpdater \
    TelechipsLauncher \
    TelechipsApkInstaller \
    TelechipsAppsManager \
    LatinIME \
    VideoPlayer

ifeq ($(GTVS_BUILD),true)
else
PRODUCT_PACKAGES += TvSampleLeanbackLauncher
endif

PRODUCT_PACKAGES += \
    TelechipsSystemUpdater \
    LatinIME \
    VideoPlayer
else
PRODUCT_PACKAGES += \
    LiveWallpapers \
    LiveWallpapersPicker \
    VisualizationWallpapers \
    PhaseBeam \
    librs_jni \
    Camera  \
    TelechipsSystemUpdater \
    Gallery \
    Launcher3 \
    TelechipsApkInstaller \
    Music
endif

# Telechips Bluetooth Auto Pairing
PRODUCT_PACKAGES += TCCAutoBT
PRODUCT_COPY_FILES += device/telechips/$(TARGET_BOARD_SOC)/gpio_keys_polled.kl:system/usr/keylayout/gpio_keys_polled.kl

#CTS test pass for CDD 3.4.2 Browser Compatibility
ifeq ($(TV_DEVICE_CTS_CDD342_PASS_BUILD),true)
PRODUCT_PACKAGES += \
    Music \
    Gallery \
    Browser2 \
    Calendar \
    CalendarProvider \
    DeskClock  \
    Contacts \
    ContactsProvider \
    DocumentsUI \
    DownloadProviderUi \
    QuickSearchBox \
    Settings
endif



# Product Package for Wifi
ifeq ($(DEVICE_USES_MARVELL_WIFI),true)
PRODUCT_PACKAGES += \
    android.hardware.wifi@1.0-service.mrvl \
    libwpa_client \
    hostapd \
    wificond \
    wifilogd \
    mlanutl \
    wpa_supplicant \
    fs_config_files

PRODUCT_COPY_FILES += \
    device/telechips/$(TARGET_BOARD_SOC)/wifi/marvell/wpa_supplicant.conf:system/etc/wifi/wpa_supplicant.conf \
    device/telechips/$(TARGET_BOARD_SOC)/wifi/marvell/wpa_supplicant.conf:$(TARGET_COPY_OUT_VENDOR)/etc/wifi/wpa_supplicant.conf
else
PRODUCT_PACKAGES += \
    android.hardware.wifi@1.0-service \
    libwpa_client \
    lib_driver_cmd_bcmdhd \
    hostapd \
    wificond \
    wifilogd \
    wpa_supplicant \
    wpa_supplicant.conf \
    libwpa_client
endif

USE_CUSTOM_AUDIO_POLICY := 0
USE_XML_AUDIO_POLICY_CONF := 0

# specific management of audio_policy.conf
PRODUCT_COPY_FILES += \
#    device/asus/fugu/audio_policy_configuration.xml:$(TARGET_COPY_OUT_VENDOR)/etc/audio_policy_configuration.xml \
 #   frameworks/av/services/audiopolicy/config/a2dp_audio_policy_configuration.xml:$(TARGET_COPY_OUT_VENDOR)/etc/a2dp_audio_policy_configuration.xml \
  #  frameworks/av/services/audiopolicy/config/r_submix_audio_policy_configuration.xml:$(TARGET_COPY_OUT_VENDOR)/etc/r_submix_audio_policy_configuration.xml \
   # frameworks/av/services/audiopolicy/config/usb_audio_policy_configuration.xml:$(TARGET_COPY_OUT_VENDOR)/etc/usb_audio_policy_configuration.xml \
    #frameworks/av/services/audiopolicy/config/default_volume_tables.xml:$(TARGET_COPY_OUT_VENDOR)/etc/default_volume_tables.xml \
   # frameworks/av/services/audiopolicy/config/audio_policy_volumes.xml:$(TARGET_COPY_OUT_VENDOR)/etc/audio_policy_volumes.xml

# Product Package for Audio VTS Test
#LIB_XML_2
LIB_XML2:=libxml2
PRODUCT_PACKAGES+=$(LIB_XML2)

PRODUCT_COPY_FILES += \
    device/telechips/common/audiopolicyconfig/audio_policy_configuration.xml:$(TARGET_COPY_OUT_VENDOR)/etc/audio_policy_configuration.xml \
    device/telechips/common/audiopolicyconfig/a2dp_audio_policy_configuration.xml:$(TARGET_COPY_OUT_VENDOR)/etc/a2dp_audio_policy_configuration.xml \
    device/telechips/common/audiopolicyconfig/r_submix_audio_policy_configuration.xml:$(TARGET_COPY_OUT_VENDOR)/etc/r_submix_audio_policy_configuration.xml \
    device/telechips/common/audiopolicyconfig/usb_audio_policy_configuration.xml:$(TARGET_COPY_OUT_VENDOR)/etc/usb_audio_policy_configuration.xml \
    device/telechips/common/audiopolicyconfig/default_volume_tables.xml:$(TARGET_COPY_OUT_VENDOR)/etc/default_volume_tables.xml \
    device/telechips/common/audiopolicyconfig/audio_policy_volumes.xml:$(TARGET_COPY_OUT_VENDOR)/etc/audio_policy_volumes.xml \
    device/telechips/common/audiopolicyconfig/audio_effects.xml:$(TARGET_COPY_OUT_VENDOR)/etc/audio_effects.xml

# SoundTrigger HAL
PRODUCT_PACKAGES += \
		audio.r_submix.default\
		sound_trigger.primary.default

# Keymaster HAL
PRODUCT_PACKAGES += \
		android.hardware.keymaster@3.0-service\
		android.hardware.keymaster@3.0-impl

PRODUCT_PACKAGES += \
    netutils-wrapper-1.0

PRODUCT_PACKAGES += \
    android.hardware.audio@2.0-service \
    android.hardware.audio@4.0-impl \
    android.hardware.audio.effect@4.0-impl \
    android.hardware.broadcastradio@1.0-impl \
    android.hardware.soundtrigger@2.1-impl

PRODUCT_PACKAGES += \
    android.hardware.graphics.allocator@2.0-impl \
    android.hardware.graphics.allocator@2.0-service \
    android.hardware.graphics.mapper@2.0-impl

# HW Composer
PRODUCT_PACKAGES += \
    android.hardware.graphics.composer@2.1-impl \
    android.hardware.graphics.composer@2.1-service

# RenderScript HAL
PRODUCT_PACKAGES += \
    android.hardware.renderscript@1.0-impl

# Dumpstate HAL
PRODUCT_PACKAGES += \
	android.hardware.dumpstate@1.0-service.tcc

# Power HAL
PRODUCT_PACKAGES += \
	android.hardware.power@1.0-service

PRODUCT_FULL_TREBLE_OVERRIDE := true
#ifeq ($(CTS_BUILD),true)
PRODUCT_COMPATIBLE_PROPERTY_OVERRIDE := true
#endif

# Input device calibration files
PRODUCT_COPY_FILES += \
	device/telechips/$(TARGET_BOARD_SOC)/tcc-ts.idc:$(TARGET_COPY_OUT_VENDOR)/usr/idc/tcc-ts.idc

PRODUCT_COPY_FILES += \
	device/telechips/$(TARGET_BOARD_SOC)/tcc-ts-goodix-cap.idc:$(TARGET_COPY_OUT_VENDOR)/usr/idc/tcc-ts-goodix-cap.idc

# These are the hardware-specific features
# android.software.app_widgets.xml add for testing on the GSI image
# need to avoid Launcher3 exception
ifeq ($(TV_DEVICE_BUILD),true)
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.wifi.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.wifi.xml \
    frameworks/native/data/etc/android.hardware.wifi.direct.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.wifi.direct.xml \
    frameworks/native/data/etc/android.hardware.bluetooth_le.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.bluetooth_le.xml \
    frameworks/native/data/etc/android.hardware.bluetooth.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.bluetooth.xml \
    frameworks/native/data/etc/android.hardware.usb.accessory.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.usb.accessory.xml \
    frameworks/native/data/etc/android.hardware.usb.host.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.usb.host.xml \
    frameworks/native/data/etc/android.hardware.hdmi.cec.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.hdmi.cec.xml \
    frameworks/native/data/etc/android.software.midi.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.software.midi.xml \
    frameworks/native/data/etc/android.software.app_widgets.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.software.app_widgets.xml \
    frameworks/native/data/etc/android.software.verified_boot.xml:system/etc/permissions/android.software.verified_boot.xml
else
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/tablet_core_hardware.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/tablet_core_hardware.xml \
    frameworks/native/data/etc/android.hardware.location.gps.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.location.gps.xml \
    frameworks/native/data/etc/android.hardware.wifi.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.wifi.xml \
    frameworks/native/data/etc/android.software.sip.voip.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.software.sip.voip.xml \
    frameworks/native/data/etc/android.hardware.usb.host.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.usb.host.xml \
    frameworks/native/data/etc/android.hardware.usb.accessory.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.usb.accessory.xml \
    frameworks/native/data/etc/android.hardware.wifi.direct.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.wifi.direct.xml \
    frameworks/native/data/etc/android.hardware.bluetooth_le.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.bluetooth_le.xml
endif

#PRODUCT_COPY_FILES += \
   kernel/drivers/usb/dwc3/dwc3.ko:$(TARGET_COPY_OUT_VENDOR)/lib/modules/dwc3.ko
#PRODUCT_COPY_FILES += \
   kernel/drivers/usb/dwc3/dwc3-tcc.ko:$(TARGET_COPY_OUT_VENDOR)/lib/modules/dwc3-tcc.ko
#PRODUCT_COPY_FILES += \
   kernel/drivers/usb/gadget/g_android.ko:$(TARGET_COPY_OUT_VENDOR)/lib/modules/g_android.ko
#PRODUCT_COPY_FILES += \
   kernel/drivers/usb/gadget/udc-core.ko:$(TARGET_COPY_OUT_VENDOR)/lib/modules/udc-core.ko

#ifeq ($(TARGET_HAVE_TSLIB),true)
#PRODUCT_COPY_FILES += \
         $(LOCAL_PATH)/../../../external/tslib/ts.conf:$(TARGET_COPY_OUT_VENDOR)/ts.conf
#endif

# Vendor Interface Manifest
PRODUCT_COPY_FILES += \
    device/telechips/$(TARGET_BOARD_SOC)/manifest.xml:$(TARGET_COPY_OUT_VENDOR)/manifest.xml

PRODUCT_PROPERTY_OVERRIDES += \
	ro.opengles.version=196610 #196610 is decimal for 0x30002 to report version 3.2

ifeq ($(USE_FB_4K), true)
PRODUCT_PROPERTY_OVERRIDES += \
	ro.sf.lcd_density=480
else ifeq ($(TV_DEVICE_BUILD),true)
PRODUCT_PROPERTY_OVERRIDES += \
	ro.sf.lcd_density=320
else
PRODUCT_PROPERTY_OVERRIDES += \
	ro.sf.lcd_density=240
endif

PRODUCT_TAGS += dalvik.gc.type-precise

# Initial SettingScreen
#PRODUCT_PACKAGES += \
        SettingScreen

# Initial SettingDispaly apk
#PRODUCT_PACKAGES += \
	SettingDisplay

PRODUCT_PACKAGES += \
	librs_jni \
	com.android.future.usb.accessory

#ifeq ($(TARGET_HAVE_TSLIB),true)
PRODUCT_PACKAGES += \
        TSCalibration \
        libtslib \
        inputraw \
        pthres \
        dejitter \
        linear
#endif

ifeq ($(USE_FB_4K), true)
PRODUCT_COPY_FILES += \
	device/telechips/common/initlogo3840x2160.rle:$(TARGET_COPY_OUT_VENDOR)/initlogo.rle
else
PRODUCT_COPY_FILES += \
	device/telechips/common/initlogo1920x1080.rle:$(TARGET_COPY_OUT_VENDOR)/initlogo.rle
endif

# Memtrack HAL
PRODUCT_PACKAGES += \
    android.hardware.memtrack@1.0-service \
    android.hardware.memtrack@1.0-impl

# Power HAL
PRODUCT_PACKAGES += \
    power.tcc \
    android.hardware.power@1.0-impl \

# HDMI HAL
ifeq ($(TV_DEVICE_BUILD),true)
PRODUCT_PACKAGES += \
	hdmi_cec.$(TARGET_BOARD_SOC) \
	android.hardware.tv.cec@1.0-impl \
	android.hardware.tv.cec@1.0-service
endif



# Camera
PRODUCT_PACKAGES += \
	android.hardware.camera.provider@2.4-impl \
	android.hardware.camera.provider@2.4-service
USBCAM_UNPLUGGED_MODE := false

ifeq ($(CTS_BUILD),true)
PRODUCT_PACKAGES += \
    vr_hwc

USE_VR_FLINGER := 1
endif

#File system management packages
PRODUCT_PACKAGES += \
		    make_ext4fs\
		    e2fsck\
		    mkmtdimg

#Making Splash Image packages
PRODUCT_PACKAGES += \
				mksplashimg \
				mksplash

#PRODUCT_PACKAGES += TelechipsWFDSink

# Drm HAL
PRODUCT_PACKAGES += \
	android.hardware.drm@1.0-service \
	android.hardware.drm@1.0-impl

# health HAL
PRODUCT_PACKAGES += \
	android.hardware.health@2.0-service.tcc \
	android.hardware.health@2.0-impl
#Telechips Security Solution
$(call inherit-product, device/telechips/tcc899x/tcsec.tcc899x.mk)

ifeq ($(TV_DEVICE_BUILD),true)
PRODUCT_PROPERTY_OVERRIDES += \
    dalvik.vm.heapgrowthlimit=96m \

$(call inherit-product, frameworks/native/build/tablet-7in-hdpi-1024-dalvik-heap.mk)
else
$(call inherit-product, frameworks/native/build/tablet-dalvik-heap.mk)
endif

# Realtek Wi-Fi module
#$(call inherit-product, device/telechips/tcc899x/wifi/realtek.mk)

# Broadcom Wi-Fi module
#$(call inherit-product, device/telechips/tcc899x/wifi/tcm3800.mk)
#$(call inherit-product, device/telechips/tcc899x/wifi/tcm3830.mk)
$(call inherit-product, device/telechips/tcc899x/wifi/tcm3840.mk)
#$(call inherit-product, device/telechips/tcc899x/wifi/bcm4354.mk)
#$(call inherit-product, device/telechips/tcc899x/wifi/bcm43241.mk)

# Marvell Wi-Fi module
#$(call inherit-product, device/telechips/tcc899x/wifi/88w8997.mk)

# Bluetooth module
ifeq ($(DEVICE_USES_MARVELL_WIFI),true)
$(call inherit-product, device/telechips/tcc899x/bluetooth/88w8997.mk)
else
$(call inherit-product, device/telechips/tcc899x/bluetooth/tcm3840.mk)
endif

# stuff common to all Telechips tcc899x devices
$(call inherit-product, hardware/telechips/common/tcc899x.mk)
$(call inherit-product-if-exists, vendor/telechips/proprietary/tcc-vendor.mk)
$(call inherit-product, device/telechips/common/common.mk)

# Telechips remote(IR)
$(call inherit-product, device/telechips/common/telechips-remote.mk)

# Google TV remote
#$(call inherit-product, device/telechips/common/googletv-remote.mk)

#PRODUCT_PROPERTY_OVERRIDES += \
    ro.vendor.vndk.version=26.1.0 \

PRODUCT_PACKAGES += \
    vndk-sp

#PRODUCT_PACKAGES += \
    android.hardware.renderscript@1.0.vndk-sp\
    android.hardware.graphics.allocator@2.0.vndk-sp\
    android.hardware.graphics.mapper@2.0.vndk-sp\
    android.hardware.graphics.common@1.0.vndk-sp\
    libhwbinder.vndk-sp\
    libbase.vndk-sp\
    libcutils.vndk-sp\
    libhardware.vndk-sp\
    libhidlbase.vndk-sp\
    libhidltransport.vndk-sp\
    libutils.vndk-sp\
    libc++.vndk-sp\
    libRS_internal.vndk-sp\
    libRSDriver.vndk-sp\
    libRSCpuRef.vndk-sp\
    libbcinfo.vndk-sp\
    libblas.vndk-sp\
    libft2.vndk-sp\
    libpng.vndk-sp\
    libcompiler_rt.vndk-sp\
    libbacktrace.vndk-sp\
    libunwind.vndk-sp\
    liblzma.vndk-sp\
    libion.vndk-sp\

#Litbig_Apps
PRODUCT_PACKAGES += \
    Litbig_Launcher \
    Litbig_Movie \
    Litbig_Music \
    Litbig_Photo \
    Litbig_Setting \
    Litbig_WifiSetting \
    Litbig_Keyboard \