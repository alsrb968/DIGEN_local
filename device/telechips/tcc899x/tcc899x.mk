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

# Inherit from those products. Most specific first.
$(call inherit-product, device/telechips/tcc899x/device.mk)
# This is where we'd set a backup provider if we had one
#$(call inherit-product, device/sample/products/backup_overlay.mk)

# Build option for TV device
TV_DEVICE_BUILD := false
CTS_BUILD := false

ifeq ($(TV_DEVICE_BUILD),true)
$(call inherit-product, device/google/atv/products/atv_base.mk)
else
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base.mk)
endif

ifeq ($(TV_DEVICE_BUILD),true)
TV_DEVICE_CTS_CDD342_PASS_BUILD := false
endif

# Build option to plug in/out GTVS package on TV device
ifeq ($(TV_DEVICE_BUILD),true)
GTVS_BUILD := false
endif

# How this product is called in the build system
PRODUCT_NAME := tcc899x
PRODUCT_DEVICE := tcc899x
PRODUCT_BRAND := Lion
PRODUCT_MANUFACTURER := Telechips

# Define the name of target board
TARGET_BOARD_8990_STB := true

ifeq ($(TV_DEVICE_BUILD),true)
PRODUCT_CHARACTERISTICS := tv,sdcard
else
PRODUCT_CHARACTERISTICS := tablet,sdcard
endif

# The user-visible product name
PRODUCT_MODEL := LION_STB

