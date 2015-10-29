/*
 * edu_mit_dlab_ppganalyzer_ImageHandler.c
 *
 *  Created on: Oct 25, 2013
 *      Author: Kweku
 */

#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include "edu_mit_dlab_ppganalyzer_ImageHandler.h"
#include "edu_mit_dlab_ppganalyzer_ImageHandlerLookupTable.h"
#include <android/bitmap.h>
#include <math.h>

#ifndef NULL
#define NULL   ((void *) 0)
#endif

#define LOG_TAG "ppganalyzer"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

/**
 * What percentage of the width/height (whichever is smaller) should be used to calculate the radius.
 */
static float RADIUS_SCALE = .75;
// Not used at the moment but was used in the old methof of RGB conversion.
static float RADIUS_SCALE_2 = .75 * .75;
/**
 * The maximum value you want when scaling RGB values up. Maximum dynamic range.
 */
static const float UP_SCALE = 1 << 16;
/**
 * The maximum raw RGB value + 1 = 256;
 */
static const float RGB_MAX = 1 << 8;

JNIEXPORT jintArray JNICALL Java_edu_mit_dlab_ppganalyzer_ImageHandler_process
(JNIEnv* pEnv, jobject pThis, jbyteArray pSource, jint height, jint width) {
  int frameSize = height * width;
  int centerY = (height >> 1);
  int centerX = (width >> 1);

  int radius, radius2;
  if (height > width) {
    radius = (int)(width * RADIUS_SCALE) >> 1;
    radius2 = (int)(width * width * RADIUS_SCALE_2) >> 2;
  } else {
    radius = (int)(height * RADIUS_SCALE) >> 1;
    radius2 = (int)(height * height * RADIUS_SCALE_2) >> 2;
  }

  int smallApothem = (int)(radius / sqrt(2));
  int smallMinX = centerX - smallApothem;
  int smallMaxX = centerX + smallApothem;
  int smallMinY = centerY - smallApothem;
  int smallMaxY = centerY + smallApothem;

  int minX = centerX - radius;
  int maxX = centerX + radius;
  int minY = centerY - radius;
  int maxY = centerY + radius;

  // Get a local copy, block other threads from accessing and modifying
  // the array while we're working with it.
  jbyte* lSource = (*pEnv)->GetPrimitiveArrayCritical(pEnv, pSource, 0);
  if (!lSource) {
    return 0;
  }

  int Y =0, Cr=0, Cb=0, totalPixels=0, Cr0, Cb0;
  int R = 0, G = 0, B = 0;
  int RTotal = 0, GTotal = 0, BTotal = 0;

  // processes all pixels within a square contained within vision circle
  // TODO: convert to RGB in this loop
  int xPos = 0, yPos = 0;
  int xDiff = 0, yDiff = 0;
  int pixelPtr = 0, yDiv2 = 0;
  int cOff=0;
  int radiusSquared = radius * radius;
  float f;
  for (yPos = minY; yPos <= maxY; ++yPos) {
    yDiff = yPos - centerY;
    pixelPtr = width * yPos;
    yDiv2 = yPos >> 1;
    for (xPos = minX; xPos <= maxX; ++xPos) {
      xDiff = xPos - centerX;

      // Only sample all pixels that are within a radius of center.
      if (xDiff*xDiff + yDiff*yDiff > radiusSquared) {
	continue;
      }

      Y = lSource[pixelPtr + xPos] & 0xFF;
      if(Y < 0) {
	Y += 255;
      }
      if((xPos & 0x1) != 1) {
	cOff = frameSize + yDiv2 * width + xPos - (xPos % 2);//+ xPos & 0x1; //(xPos >> 1) * 2;
	Cr0 = lSource[cOff] & 0xFF;
	//                if(Cr < 0) {
	//                    Cr += 127;
	//                } else {
	Cr = Cr0 - 128;
	//                }
	Cb0 = lSource[cOff + 1] & 0xFF;
	//                if(Cb < 0) {
	//                    Cb += 127;
	//                } else {
	Cb = Cb0 - 128;
	//                }
      }

      // Trying integer arithmetic instead
      //            int y1192 = 1192 * Y;
      //            int r = y1192 + 1634 * Cr;
      //            int g = y1192 - 833 * Cr - 400 * Cb;
      //            int b = y1192  + 2066 * Cb;
      //
      //            if(b < 0) {
      //                b = 0;
      //            } else if(b > 262143) {
      //                b = 262143;
      //            }
      //            if(g < 0) {
      //                g = 0;
      //            } else if(g > 262143) {
      //                g = 262143;
      //            }
      //            if(b < 0) {
      //                b = 0;
      //            } else if(b > 262143) {
      //                b = 262143;
      //            }
      //
      //            R = r >> 10;
      //            G = g >> 10;
      //            B = b >> 10;
      
      // R = Y + 0.0000*(Cb-128) + 1.403*(Cr-128)
      // G = Y - 0.34414*(Cb-128) - 0.71414*(Cr-128)
      // B = Y + 1.772*(Cb-128) + 0.0000*(Cr-128)
      //R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);//1.406*~1.403
      R = Y + R_Cr[Cr0];
      //R = (int)(Y + 1.403f*Cr);
      if(R < 0) {
	R = 0;
      } else if(R > 255) {
	R = 255;
      }

      //G = Y - (Cb >> 2) - (Cb >> 4) - (Cb >> 5) - (Cr >> 1) - (Cr >> 3) - (Cr >> 4) - (Cr >> 5);//-.34414Cb - .71414Cr
      G = Y + G_Cb[Cb]+G_Cr[Cr];
      //G = Y - G_Cb[Cb0] - G_Cr[Cr0];
      //G = (int)(Y - 0.34414f*Cb - 0.71414f*Cr);
      if(G < 0) {
	G = 0;
      } else if(G > 255) {
	G = 255;
      }

      //B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);//1.765~1.770
      B = Y + B_Cb[Cb0];
      //B = (int)(Y + 1.772f*Cb);
      if(B < 0) {
	B = 0;
      } else if(B > 255) {
	B = 255;
      }

      RTotal += R;
      GTotal += G;
      BTotal += B;
      ++totalPixels;
    }
  }

  // TODO: try dividing by a fraction of totalPixels instead to scale, maybe up to 2^32.
  // maybe something like RTotal = (UP_SCALE*rTotal)/(totalPixels*RGB_MAX)
  int scale_factor=1024; //use this to increase pixels used
  RTotal /= (totalPixels/scale_factor);
  GTotal /= (totalPixels/scale_factor);
  BTotal /= (totalPixels/scale_factor);
  R = RTotal;
  G = GTotal;
  B = BTotal;

  // Old code that would add up Y, Cr, and Cb values before converting to RGB. Kept here for reference.
  //    for (yPos = smallMinY; yPos <= smallMaxY; ++yPos) {
  //        for (xPos = smallMinX; xPos <= smallMaxX; ++xPos) {
  //            int index = yPos * width + xPos;
  //            Y += lSource[index] & 0xFF;
  //            int crcbp = frameSize + (yPos >> 1) * width + xPos - (xPos % 2);
  //            Cr += lSource[crcbp] & 0xFF;
  //            Cb += lSource[crcbp + 1] & 0xFF;
  //            ++totalPixels;
  //
  //            ////////////////////////////
  //            int x2 = (xPos - centerX) * (xPos - centerX);
  //            int yPos2 = 0;
  //            for (yPos2 = smallMinY - 1; yPos2 >= minY; --yPos2) {
  //                int y2 = (yPos2 - centerY) * (yPos2 - centerY);
  //                if (x2 + y2 > radius2)
  //                    break;
  //                int index2 = yPos2 * width + xPos;
  //                Y += lSource[index] & 0xFF;
  //                int crcbp = frameSize + (yPos2 >> 1) * width + xPos - (xPos % 2);
  //                Cr += lSource[crcbp] & 0xFF;
  //                Cb += lSource[crcbp + 1] & 0xFF;
  //                ++totalPixels;
  //            }
  //
  //            for (yPos2 = smallMaxY + 1; yPos2 <= maxY; ++yPos2) {
  //                int y2 = (yPos2 - centerY) * (yPos2 - centerY);
  //                if (x2 + y2 > radius2)
  //                    break;
  //                int index = yPos2 * width + xPos;
  //                Y += lSource[index] & 0xFF;
  //                int crcbp = frameSize + (yPos2 >> 1) * width + xPos - (xPos % 2);
  //                Cr += lSource[crcbp] & 0xFF;
  //                Cb += lSource[crcbp + 1] & 0xFF;
  //                ++totalPixels;
  //            }
  //            ////////////////////////////////////
  //        }
  //
  //        int y2 = (yPos - centerY) * (yPos - centerY);
  //        for (xPos = smallMinX - 1; xPos >= minX; --xPos) {
  //            int x2 = (xPos - centerX) * (xPos - centerX);
  //            if (x2 + y2 > radius2)
  //                break;
  //            int index = yPos * width + xPos;
  //            Y += lSource[index] & 0xFF;
  //            int crcbp = frameSize + (yPos >> 1) * width + xPos - (xPos % 2);
  //            Cr += lSource[crcbp] & 0xFF;
  //            Cb += lSource[crcbp + 1] & 0xFF;
  //            ++totalPixels;
  //        }
  //
  //        for (xPos = smallMaxX + 1; xPos <= maxX; ++xPos) {
  //            int x2 = (xPos - centerX) * (xPos - centerX);
  //            if (x2 + y2 > radius2)
  //                break;
  //            int index = yPos * width + xPos;
  //            Y += lSource[index] & 0xFF;
  //            int crcbp = frameSize + (yPos >> 1) * width + xPos - (xPos % 2);
  //            Cr += lSource[crcbp] & 0xFF;
  //            Cb += lSource[crcbp + 1] & 0xFF;
  //            ++totalPixels;
  //        }
  //    }
  //
  //
  //    //    Y /= totalPixels;
  //    //    Cr /= totalPixels;
  //    //    Cb /= totalPixels;
  //
  //	// TODO: figure out proper normalization factors
  //	// TODO: change to integer arithmetic
  //	R = (int)((1.0*(UP_SCALE/RGB_MAX) *(Y + 1.402 * (Cr - 128*totalPixels))) /(2.402*totalPixels));
  //	G = (int)((1.0*(UP_SCALE/RGB_MAX) *(Y - .34414 * (Cb - 128*totalPixels) - .71414 * (Cr - 128*totalPixels))) /(2.05828*totalPixels));
  //	B = (int)((1.0*(UP_SCALE/RGB_MAX) *(Y + 1.772 * (Cb - 128*totalPixels))) /(2.772*totalPixels));

  // Dynamically change vision circle radius so that we don't process too many pixels in subsequent iterations.
  // 200,000 and 800,000 were picked arbitrarily based on the processing of a Motorola Droid Bionic.
  if (totalPixels > 800000) {
    Java_edu_mit_dlab_ppganalyzer_ImageHandler_setRadius(pEnv, pThis, .6*RADIUS_SCALE);
  } else if (totalPixels < 200000) {
    Java_edu_mit_dlab_ppganalyzer_ImageHandler_setRadius(pEnv, pThis, 2*RADIUS_SCALE);
  }

  (*pEnv)->ReleasePrimitiveArrayCritical(pEnv, pSource, lSource, 0);

  jintArray lRGBArray = (*pEnv)->NewIntArray(pEnv, 3);
  if (!lRGBArray) {
    return 0;
  }
  jint* rgb = (*pEnv)->GetIntArrayElements(pEnv, lRGBArray, 0);
  rgb[0] = R;
  rgb[1] = G;
  rgb[2] = B;
  (*pEnv)->ReleaseIntArrayElements(pEnv, lRGBArray, rgb, 0);

  return lRGBArray;
}

JNIEXPORT void JNICALL Java_edu_mit_dlab_ppganalyzer_ImageHandler_setRadius
(JNIEnv* pEnv, jobject pThis, jdouble pScale) {
  RADIUS_SCALE = pScale;
  RADIUS_SCALE_2 = pScale * pScale;
}

JNIEXPORT jdouble JNICALL Java_edu_mit_dlab_ppganalyzer_ImageHandler_getRadius
(JNIEnv* pEnv, jclass pClass) {
  return RADIUS_SCALE;
}
