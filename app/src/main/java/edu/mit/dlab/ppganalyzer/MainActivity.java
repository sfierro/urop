package edu.mit.dlab.ppganalyzer;

import android.annotation.SuppressLint;

import android.app.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import edu.mit.dlab.ppganalyzer.util.GetCameraInfo;


/**
* Creates activity to measure heart rate
*/
public class MainActivity extends Activity {

    private Preview mPreview;
    private Camera.Size previewSize;

    private ViewfinderView viewfinder;
    private GraphViewFinal graphviewfinal;
    private ImageHandler processor;
    private static final int SETTINGS = 1;
    private SharedPreferences prefs;
    private int channel;
    private View myView;

    private ProgressDialog _progressDialog;
    private int _progress = 0;
    private Handler _progressHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        previewSize = null;
        viewfinder = null;

        setContentView(R.layout.activity_main);

        channel = ImageHandler.CHANNEL_RED;

        prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        processor = null;
        viewfinder = (ViewfinderView) findViewById(R.id.viewfinder_view);
        graphviewfinal = (GraphViewFinal) findViewById(R.id.graphviewfinal2);
        graphviewfinal.setAutoscale(true);
        graphviewfinal.setChannel(channel);
        mPreview = new Preview(this, (SurfaceView) findViewById(R.id.preview));
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

        myView = (View) findViewById(R.id.view);
        myView.bringToFront();
        graphviewfinal.bringToFront();
        Button startButton = (Button) findViewById(R.id.start_btn);
        startButton.bringToFront();
        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(1);
                _progress = 0;
                _progressDialog.setProgress(0);
                _progressHandler.sendEmptyMessage(0);
            }
        });

        /**
         * start progress thread
         */
        _progressHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(_progress>=26) {
                    Intent in = new Intent(MainActivity.this,AnalyzeActivity.class);
                    in.putExtra("hr",graphviewfinal.getHeartRate(false));
                    startActivity(in);
//                    Toast.makeText(
//                            MainActivity.this,
//                            ""
//                                    + graphviewfinal.getHeartRate(false),
//                            Toast.LENGTH_SHORT).show();


                } else {

                    if (graphviewfinal.getHeartRate(false) > 40) {
                        _progress++;
                        _progressDialog.incrementProgressBy(4);
                    }
                    _progressHandler.sendEmptyMessageDelayed(0, 1000);
                }
            }
        };

        Button heartRateButton = (Button) findViewById(R.id.heart_rate_button);
        final MainActivity parent = this;
        heartRateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(
                        parent,
                        "calculated heartRate: "
                                + graphviewfinal.getHeartRate(false),
                        Toast.LENGTH_SHORT).show();
            }
        });

        final ToggleButton recorderButton = (ToggleButton) findViewById(R.id.recorder_button);
        recorderButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recorderButton.isChecked()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            parent);
                    builder.setTitle("Set File Name for Recording");
                    // Set up the input
                    final EditText input = new EditText(parent);
                    input.setText(Recorder.baseFileName());
                    // Specify the type of input expected; this, for example,
                    // sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);
                    // Set up the buttons
                    builder.setPositiveButton("Record",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    Recorder.fileName = input.getText()
                                            .toString();
                                    Recorder.startRecording();
                                }
                            });
                    builder.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.cancel();
                                }
                            });

                    builder.show();
                }
                else {
                    Recorder.stopRecording();
                }
            }
        });

    }

    /**
     * create dialog for taking heart rate
     */
    @Deprecated
    protected Dialog onCreateDialog(int i) {

        _progressDialog = new ProgressDialog(this);

        _progressDialog.setIcon(R.drawable.heartsmall);

        _progressDialog.setTitle("Taking measurements, please wait");
        _progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        _progressDialog.setProgressNumberFormat(null);
        _progressDialog.setProgressPercentFormat(null);
        _progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                _progressHandler.removeMessages(0);
                _progressDialog.dismiss();
                Toast.makeText(
                        MainActivity.this,
                        ""
                                + graphviewfinal.getHeartRate(false),
                        Toast.LENGTH_SHORT).show();
            }
        });
        return _progressDialog;
    }

    @Override
    protected void onResume() {
        final ToggleButton recorderButton = (ToggleButton) findViewById(R.id.recorder_button);
        recorderButton.setChecked(false);
        super.onResume();
        if (mPreview != null && mPreview.getCamera() != null) {
            Parameters p = mPreview.getCamera().getParameters();
            p.setFlashMode(Parameters.FLASH_MODE_TORCH);
            mPreview.getCamera().setParameters(p);
        }
    }

    @Override
    protected void onPause() {
        Recorder.stopRecording();
        if (mPreview != null && mPreview.getCamera() != null) {
            try {
                Parameters p = mPreview.getCamera().getParameters();
                p.setFlashMode(Parameters.FLASH_MODE_OFF);
                mPreview.getCamera().setParameters(p);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        if (processor != null) {
            processor.done();
        }
        graphviewfinal.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        Recorder.stopRecording();
        if (processor != null) {
            processor.done();
            processor = null;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Recorder.stopRecording();
        if (processor != null) {
            processor.done();
            processor = null;
        }
        graphviewfinal.onDestroy();
        mPreview.kill();
        super.onDestroy();
    }

//  void showPreferences() {
////        Intent it = new Intent(this, Preferences.class);
//
//      // load it up
//      Camera cam = mPreview.getCamera();
//
//      if (cam == null) {
//          Toast.makeText(this, getString(R.string.message_camera_not_found),
//                  Toast.LENGTH_LONG).show();
//      }
//      if (cam != null) {
//          List<String> opts;
//          String[] output = new String[1];
//
//          // white balance
//          it.putExtra("current_" + getString(R.string.pref_white_balance),
//                  cam.getParameters().getWhiteBalance());
//          opts = cam.getParameters().getSupportedWhiteBalance();
//          if (opts != null && opts.size() > 1) {
//              it.putExtra(getString(R.string.pref_white_balance),
//                      opts.toArray(output));
//          }
//
//          // color effects
//          it.putExtra("current_" + getString(R.string.pref_color_effects),
//                  cam.getParameters().getColorEffect());
//          opts = cam.getParameters().getSupportedColorEffects();
//          if (opts != null && opts.size() > 1) {
//              it.putExtra(getString(R.string.pref_color_effects),
//                      opts.toArray(output));
//          }
//
//          // flash modes
//          it.putExtra("current_" + getString(R.string.pref_flash_modes), cam
//                  .getParameters().getFlashMode());
//          opts = cam.getParameters().getSupportedFlashModes();
//          if (opts != null && opts.size() > 1) {
//              it.putExtra(getString(R.string.pref_flash_modes),
//                      opts.toArray(output));
//          }
//
//          // focus modes
//          it.putExtra("current_" + getString(R.string.pref_focus_modes), cam
//                  .getParameters().getFocusMode());
//          opts = cam.getParameters().getSupportedFocusModes();
//          if (opts != null && opts.size() > 1) {
//              it.putExtra(getString(R.string.pref_focus_modes),
//                      opts.toArray(output));
//          }
//
//          // scene modes
//          it.putExtra("current_" + getString(R.string.pref_scene_modes), cam
//                  .getParameters().getSceneMode());
//          opts = cam.getParameters().getSupportedSceneModes();
//          if (opts != null && opts.size() > 1) {
//              it.putExtra(getString(R.string.pref_scene_modes),
//                      opts.toArray(output));
//          }
//
//          // anti banding
//          it.putExtra("current_" + getString(R.string.pref_anti_banding), cam
//                  .getParameters().getAntibanding());
//          opts = cam.getParameters().getSupportedAntibanding();
//          if (opts != null && opts.size() > 1) {
//              it.putExtra(getString(R.string.pref_anti_banding),
//                      opts.toArray(output));
//          }
//      }
//      // run it
//      startActivityForResult(it, SETTINGS);
//  }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case SETTINGS: {
                Message msg = Message.obtain();
                msg.what = SETTINGS;
                msg.obj = data;
                msgHandler.sendMessageDelayed(msg, 1500);
            }
                break;
            }
        }
    }

//  @Override
//  public boolean onCreateOptionsMenu(Menu menu) {
//      super.onCreateOptionsMenu(menu);
//      this.menu = menu;
//      MenuInflater inflater = getMenuInflater();
//      inflater.inflate(R.menu.mainoptions, menu);
//      return true;
//  }

    public static boolean plotRaw = true;
//  public static boolean plotFilteredSignal = false;
//  public boolean continousHRMode = false;
//  public boolean rawMode = true;

//  public void updateMenu() {
//      menu.findItem(R.id.menu_plot_toggle_continuous_heartrate).setChecked(
//              continousHRMode);
//      menu.findItem(R.id.menu_plot_toggle_raw).setChecked(rawMode);
//  }

//  public void setBackground(int color){
//      RelativeLayout background= (RelativeLayout) findViewById(R.id.enclosing_frame);
//      if (color!=0){
//          background.setBackgroundColor(color);
//          this.mPreview.surface.setAlpha(0);
//          this.viewfinder.setAlpha(0);
//      }else{
//          this.mPreview.surface.setAlpha(1);
//          this.viewfinder.setAlpha(1);
//      }
//  }
    
//  @Override
//  public boolean onOptionsItemSelected(MenuItem item) {
//      switch (item.getItemId()) {
//      case R.id.menu_toggle_color_green:
//          setBackground(Color.GREEN);
//          break;
//      case R.id.menu_toggle_color_red:
//          setBackground(Color.RED);
//          break;
//      case R.id.menu_toggle_color_camera:
//          setBackground(0);
//          break;
//      case R.id.menu_viewfinder:
//          break;
//      case R.id.menu_camera:
////            showPreferences();
//          break;
//      case R.id.menu_plot:
//          break;
//      case R.id.menu_info:
//          break;
//      case R.id.menu_viewfinder_75:
//          viewfinder.setScale(.75);
//          processor.setRadiusScale(.75);
//          return true;
//      case R.id.menu_viewfinder_50:
//          viewfinder.setScale(.50);
//          processor.setRadiusScale(.50);
//          return true;
//      case R.id.menu_viewfinder_25:
//          viewfinder.setScale(.25);
//          processor.setRadiusScale(.25);
//          return true;
//      case R.id.menu_plot_toggle_red:
//          item.setChecked(graphviewfinal.toggleRed());
//          return true;
//      case R.id.menu_plot_toggle_blue:
//          item.setChecked(graphviewfinal.toggleBlue());
//          return true;
//      case R.id.menu_plot_toggle_green:
//          item.setChecked(graphviewfinal.toggleGreen());
//          return true;
//      case R.id.menu_calculate_heartrate:
//          Toast.makeText(
//                  this,
//                  "calculated heartRate: "
//                          + graphviewfinal.getHeartRate(false),
//                  Toast.LENGTH_SHORT).show();
//          return true;
//
//      case R.id.menu_plot_toggle_continuous_heartrate:
//          graphviewfinal
//                  .toggleContinuousHeartRateCalculation((TextView) findViewById(R.id.txt_heart_rate));
//          continousHRMode = !continousHRMode;
//          if (continousHRMode) {
//              rawMode = false;
//          } else {
//              rawMode = !rawMode;
//          }
//          updateMenu();
//          return true;
//
//      case R.id.menu_plot_toggle_raw:
//          plotRaw = !plotRaw;
//          if (!continousHRMode) {
//              rawMode = !rawMode;
//          }
//          updateMenu();
//          return true;
//
//      case R.id.menu_plot_toggle_filtered_signal:
//          plotFilteredSignal = !plotFilteredSignal;
//          item.setChecked(plotFilteredSignal);
//          return true;
//      case R.id.menu_analyzer:
//          selectChannel();
//          return true;
//      case R.id.menu_plot_toggle_derivative:
////                graphviewfinal.useDerivative();
////                item.setChecked(true);
//              item.setChecked(false);
//          return true;
//      }
//
//      return super.onOptionsItemSelected(item);
//  }

    class Preview implements SurfaceHolder.Callback {
        SurfaceHolder mHolder;
        Camera mCamera;
        private Activity activity;
//        private SurfaceView surface;
        private int cameraId;

        Preview(Activity activity, SurfaceView surface) {
            this.activity = activity;
//            this.surface = surface;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = surface.getHolder();
            mHolder.addCallback(this);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            cameraId = prefs.getInt(getString(R.string.pref_camera), 0);
            GetCameraInfo gci = GetCameraInfo.newInstance();
            mCamera = gci.getCamera(cameraId);
            if (mCamera == null) {
                Toast.makeText(getApplicationContext(),
                        "Device needs a camera.", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Surface will be destroyed when we return, so stop the preview.
            // Because the CameraDevice object is not a shared resource, it's
            // very important to release it when the activity is paused.
            try {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        public void kill() {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }

        private Camera.Size getOptimalPreviewSize(Camera camera,
                Camera.Parameters parameters, int w, int h) {
            Camera.Size optimalSize = null;
            String[] sizes = parameters.get("preview-size-values").split(",");
            String[] p;
            int pw, ph;
            for (int i = 0; i < sizes.length; i++) {
                p = sizes[i].split("x");
                if (p.length > 1) {
                    pw = Integer.parseInt(p[0]);
                    ph = Integer.parseInt(p[1]);
                    if (pw <= w && ph <= h) {
                        optimalSize = camera.new Size(pw, ph);

                        return optimalSize;
                    }
                }
            }
            return optimalSize;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int w,
                int h) {
            // Now that the size is known, set up the camera parameters and
            // begin the preview.
            // The Surface has been created, acquire the camera and tell it
            // where to draw.
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                int[] bestFpsRange = new int[] { -1, -1 };
                for (int[] ranges : parameters.getSupportedPreviewFpsRange()) {
                    if (ranges[0] >= bestFpsRange[1]) {
                        bestFpsRange = ranges;
                    } else if (ranges[0] >= bestFpsRange[0]
                            && ranges[1] >= bestFpsRange[1]) {
                        bestFpsRange = ranges;
                    }
                }
                previewSize = getOptimalPreviewSize(mCamera, parameters, w, h);
                if (viewfinder != null) {
                    viewfinder.setPreviewSize(w, h);
                }
                if (previewSize != null) {
                    parameters.setPreviewSize(previewSize.width,
                            previewSize.height);
                }
                parameters.setPreviewFormat(ImageFormat.NV21);

                mCamera.setPreviewDisplay(holder);
                mCamera.setParameters(parameters);
                mCamera.setPreviewCallbackWithBuffer(callback);
                int yStride = (int) Math.ceil(previewSize.width / 16.0) * 16;
                int uvStride = (int) Math.ceil(yStride / 2 / 16.0) * 16;
                int ySize = yStride * previewSize.height;
                int uvSize = uvStride * previewSize.height / 2;
                int size = ySize + uvSize * 2;
                for (int i = 0; i < 5; ++i) {
                    // Just add 5 for leg room.
                    mCamera.addCallbackBuffer(new byte[size]);
                }
                mCamera.startPreview();
                Parameters p = mPreview.getCamera().getParameters();
                p.setFlashMode(Parameters.FLASH_MODE_TORCH);
                mPreview.getCamera().setParameters(p);
                
                
                setCameraDisplayOrientation(activity, cameraId, mCamera);
            } catch (Exception exception) {
                System.err.println(exception.getMessage());
            }
        }
        Camera getCamera() {
            return mCamera;
        }
    }

    public static void setCameraDisplayOrientation(Activity activity,
            int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
        case Surface.ROTATION_0:
            degrees = 0;
            break;
        case Surface.ROTATION_90:
            degrees = 90;
            break;
        case Surface.ROTATION_180:
            degrees = 180;
            break;
        case Surface.ROTATION_270:
            degrees = 270;
            break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @SuppressLint("HandlerLeak")
    private Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            }
        }
    };

    class snapshot {
        byte[] data;
        int height;
        int width;
        long time;
        int channel;
    }

    private Camera.PreviewCallback callback = new Camera.PreviewCallback() {

        private double lastRadius = ImageHandler.getRadius();

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (processor == null) {
                startProcessor();
            }
            // feed the timestamp and the data to a separate queue
            snapshot s = new snapshot();
            s.width = previewSize.width;
            s.height = previewSize.height;
            s.data = data;
            s.channel = channel;
            s.time = System.currentTimeMillis();
            processor.queue(s);
            camera.addCallbackBuffer(data);

            if (viewfinder != null && lastRadius != ImageHandler.getRadius()) {
                lastRadius = ImageHandler.getRadius();
                viewfinder.invalidate();
            }
        }
    };

    void startProcessor() {
        Recorder.setContext(getApplicationContext());
        if (processor == null) {
            processor = new ImageHandler();
            processor.setGraphViewFinal(graphviewfinal);
            processor.start();
        }
    }

//  void setParameters(Camera.Parameters params) {
//      SharedPreferences prefs = PreferenceManager
//              .getDefaultSharedPreferences(getBaseContext());
//
//      if (prefs.contains(getString(R.string.pref_scene_modes))) {
//          // set the scene mode
//          params.setSceneMode(prefs.getString(
//                  getString(R.string.pref_scene_modes), ""));
//      }
//
//      if (prefs.contains(getString(R.string.pref_flash_modes))) {
//          // set the scene mode
//          params.setFlashMode(prefs.getString(
//                  getString(R.string.pref_flash_modes), ""));
//      }
//
//      if (prefs.contains(getString(R.string.pref_focus_modes))) {
//          // set the scene mode
//          params.setFocusMode(prefs.getString(
//                  getString(R.string.pref_focus_modes), ""));
//      }
//
//      if (prefs.contains(getString(R.string.pref_white_balance))) {
//          // set the scene mode
//          params.setWhiteBalance(prefs.getString(
//                  getString(R.string.pref_white_balance), ""));
//      }
//
//      if (prefs.contains(getString(R.string.pref_anti_banding))) {
//          // set the scene mode
//          params.setAntibanding(prefs.getString(
//                  getString(R.string.pref_anti_banding), ""));
//      }
//
//      if (prefs.contains(getString(R.string.pref_color_effects))) {
//          // set the scene mode
//          params.setColorEffect(prefs.getString(
//                  getString(R.string.pref_color_effects), ""));
//      }
//  }

//    private static final int DIALOG_SELECT_CHANNEL = 1;
//
//    protected Dialog onCreateDialog(int id) {
//        switch (id) {
//        case DIALOG_SELECT_CHANNEL: {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            return builder
//                    .setTitle(getString(R.string.title_select_channel))
//                    .setCancelable(true)
//                    .setSingleChoiceItems(
//                            getResources().getStringArray(
//                                    R.array.channel_options), channel,
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog,
//                                        int item) {
//                                    dialog.dismiss();
//                                    updateChannel(item);
//                                }
//                            }).create();
//        }
//        }
//        return null;
//    }

    void updateChannel(int item) {
        if (channel == item)
            return;
        channel = item;
        graphviewfinal.setChannel(channel);
    }
}