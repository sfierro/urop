package edu.mit.dlab.ppganalyzer;

import edu.mit.dlab.ppganalyzer.MainActivity.snapshot;


import java.util.concurrent.LinkedBlockingQueue;

/**
 * Creates a handler to update graph
 */
public class ImageHandler extends Thread {

    static {
        System.loadLibrary("pixelProcessor");
    }

    static final int CHANNEL_RED = 0;
//  static final int CHANNEL_GREEN = 1;
//  static final int CHANNEL_BLUE = 2;

    public native int[] process(byte[] data, int height, int width);
    public static native double getRadius();

    private boolean _stopped;
    private LinkedBlockingQueue<snapshot> queue;
    private GraphViewFinal graphviewfinal;

    //in milliseconds. Leads to about 50 data points/sec.
    public static long TIME_STEP = 20; // desired time interval

    //In Hz
    public static double SAMPLING_FREQUENCY = 1000.0 / TIME_STEP;

    public ImageHandler() {
        queue = new LinkedBlockingQueue<snapshot>();
        this._stopped = false;
    }

    public void done() {
        this._stopped = true;
    }

    @Override
    public void start() {
        this._stopped = false;
        super.start();
    }

    public void setGraphViewFinal(GraphViewFinal gvf) {
        graphviewfinal = gvf;
    }

    @Override
    public void run() {
        snapshot s;
        long lastTimeInterval = System.currentTimeMillis();
        long lastTime = System.currentTimeMillis();
        int lastR = -1;
//        int lastG = -1;
//        int lastB = -1;

        while (!_stopped) {
            try {
                if ((s = queue.take()) != null) {
                    long time = s.time;

                    // Used to time how long rgb conversion took
                    // long now = System.currentTimeMillis();
                    int[] RGB = process(s.data, s.height, s.width);
                    int R = RGB[0];
//                  int G = RGB[1];
//                  int B = RGB[2];

                    if (lastTime != -1) {
                        long dx = time - lastTime;
                        if (dx == 0) {
                            graphviewfinal.updateGraph(R);
//                          graphviewfinal.updateGraph(R, G, B);
                        } else {
                            float slopeR = ((float) R - lastR) / dx;
//                          float slopeG = ((float) G - lastG) / dx;
//                          float slopeB = ((float) B - lastB) / dx;

                            // Interpolate data
                            while (lastTimeInterval <= time) {
                                long x = lastTimeInterval - lastTime;

                                int red = ((int) (slopeR * x + lastR));
//                              int green = ((int) (slopeG * x + lastG));
//                              int blue = ((int) (slopeB * x + lastB));

                                graphviewfinal.updateGraph(red);
//                              graphviewfinal.updateGraph(red, green, blue);
                                lastTimeInterval += TIME_STEP;
                                Recorder.recordString(Integer.toString(red));
                            }
                        }
                    } else {
                        lastTimeInterval = time;
                        graphviewfinal.updateGraph(R);
//                      graphviewfinal.updateGraph(R, G, B);
                        Recorder.recordString(Integer.toString(R));
//                      Recorder.recordString(Integer.toString(R)+","+Integer.toString(G)+","+Integer.toString(B));
                    }

                    lastTime = time;
                    lastR = R;
//                  lastG = G;
//                  lastB = B;
                    graphviewfinal.updateGraph();
                }
            } catch (Exception e) {

            }
        }
    }

    public void queue(snapshot s) {
        if (!_stopped) {
            queue.add(s);
        }
    }

}