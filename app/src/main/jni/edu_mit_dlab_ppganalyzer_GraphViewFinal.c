#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "edu_mit_dlab_ppganalyzer_GraphViewFinal.h"
#include "kiss_fft.h"

#define LOG_TAG "ppganalyzer"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

JNIEXPORT jdoubleArray JNICALL Java_edu_mit_dlab_ppganalyzer_GraphViewFinal_fft
  (JNIEnv* pEnv, jobject pThis, jintArray pInput) {
    const jsize N = (*pEnv)->GetArrayLength(pEnv, pInput);
    if (N <=1) {
        return pInput;
    }

    jint* input = (*pEnv)->GetIntArrayElements(pEnv, pInput, 0);
    kiss_fft_cpx fftInput[N];
    int i;
    for (i = 0; i < N; ++i) {
        kiss_fft_cpx cpx = {.r = input[i], .i=0};
        fftInput[i] = cpx;
    }
    kiss_fft_cpx output[N];
    kiss_fft_cfg mycfg=kiss_fft_alloc(N,0,NULL,NULL);
    kiss_fft(mycfg, fftInput, output);

    jdoubleArray lJavaArray = (*pEnv)->NewDoubleArray(pEnv, N/2);
    double realed[N/2];
    for (i = 0; i < N/2; ++i) {
        double real1 = output[2*i].r * output[2*i].r - output[2*i].i*output[2*i].i;
        double im1 = output[2*i].r * output[2*i].i + output[2*i].i*output[2*i].r;

        double real2 = output[2*i+1].r * output[2*i+1].r - output[2*i+1].i*output[2*i+1].i;
        double im2 = output[2*i+1].r * output[2*i+1].i + output[2*i+1].i*output[2*i+1].r;

        double real = real1+real2;
        double im = im1+im2;
        //TODO: try returning maximum instead
        realed[i] = sqrt(real*real + im*im);
    }
    (*pEnv)->SetDoubleArrayRegion(pEnv, lJavaArray, 0, N/2, realed);
    (*pEnv)->ReleaseIntArrayElements(pEnv, pInput, input, 0);
    return lJavaArray;
}

// TODO: change to double
static const double scaleCoeffs[] = {
    // Medium filter
    0.0020799204253810454, 0.0057935895645104835, 0.01232237299836535, 0.02152348715290718,
    0.03230358548227676, 0.04237507998335552, 0.04851687291196782, 0.047304830951436935,
    0.036210018151405066, 0.014790178491791735, -0.014417241162128157,
    -0.045687044736318516, -0.07083363160140335, -0.08086425336853185,
    -0.06824186636612128, -0.029182041953946716, 0.03467925788555512, 0.11583652389403461,
    0.20195098293998626, 0.27825139431414037, 0.3307479234522428, 0.3494527119090597,
    0.3307479234522428, 0.27825139431414037, 0.20195098293998626, 0.11583652389403461,
    0.03467925788555512, -0.029182041953946716, -0.06824186636612128, -0.08086425336853185,
    -0.07083363160140335, -0.045687044736318516, -0.014417241162128157,
    0.014790178491791735, 0.036210018151405066, 0.047304830951436935, 0.04851687291196782,
    0.04237507998335552, 0.03230358548227676, 0.02152348715290718, 0.01232237299836535,
    0.0057935895645104835, 0.0020799204253810454
};

JNIEXPORT jintArray JNICALL Java_edu_mit_dlab_ppganalyzer_GraphViewFinal_firFilter
  (JNIEnv* pEnv, jobject pThis, jintArray values) {
    const int size = (int)pow(2, (int)floor(log((*pEnv)->GetArrayLength(pEnv, values)) / log(2)));
    int filtered[size];
    // Get a local copy, block other threads from accessing and modifying
    // the array while we're working with it.
    jint* input = (*pEnv)->GetIntArrayElements(pEnv, values, 0);

    double acc = 0, usedCoeffSum = 0;

    // TODO: use previous data to allow scaling of first few values
    int i, j;
    int coeffsLength = sizeof(double)/sizeof(scaleCoeffs[0]);
    for (i = 0; i < size; ++i) {
        acc = 0;
        usedCoeffSum = 0;
        for (j = i - coeffsLength + 1; j <= i; ++j) {
            if (j < 0) {
                // if (j + scaleCoeffs.length < 0) {
                continue;
            }
            acc += input[j] * scaleCoeffs[i - j];
            // acc += input[j + scaleCoeffs.length] * scaleCoeffs[i - j];
            usedCoeffSum += fabs(scaleCoeffs[i - j]);
        }
        // divide to normalize
        // TODO: if using integer coefficients, possibly rework the normalization code
        filtered[i] = (int)(acc / usedCoeffSum);
    }

    jintArray lArray = (*pEnv)->NewIntArray(pEnv, size);
    (*pEnv)->SetIntArrayRegion(pEnv, lArray, 0, size, filtered);
    (*pEnv)->ReleaseIntArrayElements(pEnv, values, input, 0);
    return lArray;
}
