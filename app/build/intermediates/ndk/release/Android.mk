LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := pixelProcessor
LOCAL_SRC_FILES := \
	/Users/samfierro/Documents/PPGAnalyzer/app/src/main/jni/Android.mk \
	/Users/samfierro/Documents/PPGAnalyzer/app/src/main/jni/edu_mit_dlab_ppganalyzer_GraphViewFinal.c \
	/Users/samfierro/Documents/PPGAnalyzer/app/src/main/jni/edu_mit_dlab_ppganalyzer_ImageHandler.c \
	/Users/samfierro/Documents/PPGAnalyzer/app/src/main/jni/kiss_fft.c \
	/Users/samfierro/Documents/PPGAnalyzer/app/src/main/jni/kissfft.hh \

LOCAL_C_INCLUDES += /Users/samfierro/Documents/PPGAnalyzer/app/src/main/jni
LOCAL_C_INCLUDES += /Users/samfierro/Documents/PPGAnalyzer/app/src/release/jni

include $(BUILD_SHARED_LIBRARY)
