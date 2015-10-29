LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE	:=	pixelProcessor
LOCAL_SRC_FILES :=	edu_mit_dlab_ppganalyzer_ImageHandler.c edu_mit_dlab_ppganalyzer_GraphViewFinal.c kiss_fft.c
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)