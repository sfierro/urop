/*
 * Copyright (C) 2011 - 2012 Ashametrics, Inc.
 * @author Olufemi Omojola <femi@ashametrics.com>
 */
package edu.mit.dlab.ppganalyzer.util;

import java.util.HashMap;
import java.util.List;

public interface GetCameraInfoInterface {
    public List<HashMap<String, String>> getCameras();

    public boolean hasCameraInfo();

    public android.hardware.Camera getCamera(int camera);
}
