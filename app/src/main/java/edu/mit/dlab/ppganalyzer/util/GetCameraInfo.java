/*
 * Copyright (C) 2011 - 2013 Ashametrics, Inc.
 * @author Olufemi Omojola <femi@ashametrics.com>
 */
package edu.mit.dlab.ppganalyzer.util;

import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class GetCameraInfo implements GetCameraInfoInterface {
    @Override
    public abstract List<HashMap<String, String>> getCameras();

    @Override
    public abstract boolean hasCameraInfo();

    @Override
    public abstract android.hardware.Camera getCamera(int camera);

    private static class GetCameraInfo8 extends GetCameraInfo {
        @Override
        public List<HashMap<String, String>> getCameras() {
            HashMap<String, String> hm = new HashMap<String, String>(5);
            hm.put("cameraId", "0");
            hm.put("label", "1");
            hm.put("description", "Only camera");
            ArrayList<HashMap<String, String>> al = new ArrayList<HashMap<String, String>>(1);
            al.add(hm);
            return al;
        }

        @Override
        public boolean hasCameraInfo() {
            return false;
        }

        @Override
        public android.hardware.Camera getCamera(int camera) {
            return android.hardware.Camera.open();
        }
    }

    private static class GetCameraInfo9 extends GetCameraInfo {
        @Override
        public List<HashMap<String, String>> getCameras() {
            int i, n = android.hardware.Camera.getNumberOfCameras();
            ArrayList<HashMap<String, String>> al = new ArrayList<HashMap<String, String>>(n);
            android.hardware.Camera.CameraInfo ci = new android.hardware.Camera.CameraInfo();
            HashMap<String, String> hm;
            String desc;
            for (i = 0; i < n; i++) {
                android.hardware.Camera.getCameraInfo(i, ci);
                hm = new HashMap<String, String>(5);
                hm.put("cameraId", String.valueOf(i));
                hm.put("label", String.valueOf(i + 1));
                if (ci.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    desc = "faces front; ";
                } else {
                    desc = "faces back; ";
                }
                desc += " oriented " + ci.orientation;
                hm.put("description", desc);
                al.add(hm);
            }
            return al;
        }

        @Override
        public boolean hasCameraInfo() {
            return true;
        }

        @Override
        public android.hardware.Camera getCamera(int camera) {
            return android.hardware.Camera.open(camera);
        }
    }

    public static GetCameraInfo newInstance() {
        final int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= 9)
            return new GetCameraInfo9();
        return new GetCameraInfo8();
    }
}
