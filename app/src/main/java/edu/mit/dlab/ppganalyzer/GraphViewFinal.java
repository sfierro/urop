package edu.mit.dlab.ppganalyzer;

import android.annotation.SuppressLint;
import android.content.Context;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GraphView creates a plot of raw values or FFT output at the bottom of the screen
 */
public class GraphViewFinal extends View {

    static {
        System.loadLibrary("pixelProcessor");
    }

    public native double[] fft(int[] values);
    public native int[] firFilter(int[] values);

    private Canvas canvas;
    private Paint paint;
    public static String[] verlabels;

    public List<Integer> redValues = new ArrayList<Integer>();
    private List<Integer> greenValues = new ArrayList<Integer>();
    private List<Integer> blueValues = new ArrayList<Integer>();

    private float width = 10.0f;
    private float graphheight;
    private float border;
    int bufferSize;
    float minY = 0;
    float maxY, startX;
    private int channel, lastX;

    public static final int maximumPlottableValue = (int)Math.pow(2, 16);
    float range = maximumPlottableValue;

    private boolean redPlotOn = true;
    private boolean greenPlotOn = false;
    private boolean bluePlotOn = false;
    private Boolean autoscale = true;

    private double incrementX, slowFactor;
    private FivePointDerivative deriv=new FivePointDerivative(20);

    public GraphViewFinal(Context context) {
        super(context);
        init();
    }

    public GraphViewFinal(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    @Override
    public void onFinishInflate() {
        init();
    }

    public void init()
    {
        this.channel = ImageHandler.CHANNEL_RED;
        this.lastX = 0;
        this.startX = 45;
        this.incrementX = 10;
        this.bufferSize = -1;
        this.slowFactor = 1;

        this.verlabels = new String[] {
                maximumPlottableValue + "", maximumPlottableValue / 2 + "", "0"
        };
        paint = new Paint();
        paint.setStrokeWidth(2);
        border = 20;
    }

    /**
     * Plot the values onto the current canvas using the desired color. This
     * method will be called from the onDraw method.
     * @param values to plot
     * @param color to use
     */
    private void plot(List<Integer> values, int color) {
        paint.setColor(color);
        float y, prevY = 0;
        int size = values.size();
        float k = 0;
        float j = startX;

        for (int i = 0; i < size; i++, j += k * incrementX) {
            float tempY = (values.get(i) - minY) * graphheight / range;
            y = graphheight - tempY + 10;

            this.canvas.drawPoint(j, y, paint);
            if (i != 0) {
                this.canvas.drawLine((float)(j - Math.floor(k) * incrementX), prevY, j, y, paint);
            }

            prevY = y;
            if (k >= 1) {
                k = 0;
            }
            k += 1.0 / slowFactor;
        }
    }

    /**
     * Adds value to list and makes sure that list is not longer than
     * bufferSize.
     */
    private void addPoint(int value, List<Integer> list) {
        while (list.size() >= bufferSize) {
            list.remove(0);
        }

        list.add(value);
    }

    /**
     * Resizes the axes.
     */
    private void scale() {
        minY = Integer.MAX_VALUE;
        maxY = 0;

        // Only red is synchronized here because I stopped working with the other colors
        synchronized (redValues) {
            if (redPlotOn && redValues.size() > 0) {
                minY = Math.min(minY, Collections.min(redValues));
                maxY = Math.max(maxY, Collections.max(redValues));
            }
        }

        synchronized (greenValues) {
          if (greenPlotOn && greenValues.size() > 0) {
              minY = Math.min(minY, Collections.min(greenValues));
              maxY = Math.max(maxY, Collections.max(greenValues));
          }
        }
        synchronized (blueValues) {
          if (bluePlotOn && blueValues.size() > 0) {
              minY = Math.min(minY, Collections.min(blueValues));
              maxY = Math.max(maxY, Collections.max(blueValues));
          }
        }

        if (maxY - minY < 2) {
            maxY++;
            minY--;
        }
        range = maxY - minY;
        verlabels = new String[] {
                Integer.toString(Math.round(maxY)),
                Integer.toString(Math.round((maxY + minY) / 2)), Integer.toString(Math.round(minY))
        };
    }

    // Used to see if the screen has been rotated
    private int previousOrientation = -1;

    /*
     * This method is called every time the view is invalidated. We can then
     * redraw the graph. (non-Javadoc)
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;

        // Buffersize = -1 only on first run; if current orientation is not
        // equal to
        // previousOrientation if the screen has rotated
        // call setGraphWidth to recalculate proportions
        if (bufferSize == -1
                || getResources().getConfiguration().orientation != previousOrientation) {
            previousOrientation = getResources().getConfiguration().orientation;
            calculatePlottingVariables(getWidth());
        }

        int i;
        float horstart = border * 2;
        graphheight = getHeight() - 2 * border;

        if (autoscale) {
            scale();
        }

        incrementX = 1.0 * getWidth() / bufferSize;
        if (redPlotOn) {
            synchronized (redValues) {
                plot(redValues, Color.RED);
            }
        }

        lastX = lastX + (int)width;

        paint.setTextAlign(Align.LEFT);
        int vers = verlabels.length - 1;

        // Draws the grid lines (axes) of the graph.
        for (i = 0; i < verlabels.length; i++) {
            paint.setColor(Color.DKGRAY);
            paint.setAlpha(155);
            float y = graphheight / vers * i + 10 + 2 * (i - 1);// + border;
            this.canvas.drawLine(horstart, y, 1000 * width, y, paint);
            if (i < vers) {
                this.canvas.drawLine(horstart, y, horstart, graphheight / vers * (i + 1) + 10,
                        paint);
            }
            paint.setColor(Color.WHITE);
            this.canvas.drawText(verlabels[i], 0, y, paint);
        }

        paint.setColor(Color.WHITE);
        paint.setTextAlign(Align.CENTER);
    }

    public void setAutoscale(boolean b) {
        synchronized (autoscale) {
            autoscale = b;
            if (!autoscale) {
                minY = 0;
                maxY = maximumPlottableValue;
            }
        }
    }

    /**
     * Calculates the required variables for plotting based on the given width.
     */
    private void calculatePlottingVariables(int width) {

//        bufferSize = (int)Math.pow(2, 10);
        bufferSize = (int)Math.pow(2, 8.5);
        incrementX = 1.0 * width / bufferSize;

        synchronized (redValues) {
            while (redValues.size() > bufferSize) {
                // Item 0 is the oldest element in the arraylist
                redValues.remove(0);
            }
        }

        if (greenPlotOn) {
          synchronized (greenValues) {
              plot(greenValues, Color.GREEN);
          }
        }

        if (bluePlotOn) {
          synchronized (blueValues) {
              plot(blueValues, Color.BLUE);
          }
        }

    }

    /*
     * The GraphViewFinal lives in the UI thread while ImageHandler lives in a
     * background thread. "invalidate()" must be called from the UI thread so
     * the Handler is necessary to communicate from the background thread to the
     * UI thread.
     */
    @SuppressLint("HandlerLeak")
    private Handler h = new Handler() {
        @Override
        public void handleMessage(Message m) {
            if (m.arg1 == -1) {
                invalidate();
                return;
            }
            Integer[] points = (Integer[])m.obj;
            if (redPlotOn || channel == ImageHandler.CHANNEL_RED) {
                synchronized (redValues) {
                    addPoint(points[0], redValues);
                }
            }
        }
    };

    void updateGraph(int red) {
        Message m = Message.obtain();
        m.obj = new Integer[] {
                red
        };
        m.arg1 = 0;
        // Send the RGB values to the Handler to process.
        h.sendMessageDelayed(m, 1);
    }

    /**
     * Call this to notify the view to invalidate and redraw.
     */
    void updateGraph() {
        Message m = Message.obtain();
        m.arg1 = -1;
        h.sendMessage(m);
    }

    private boolean continuousHRCalculation = false;
    

    public void onPause() {
        continuousHRCalculation = false;
    }
    public void onDestroy() {
        continuousHRCalculation = false;
    }

    double getHeartRate(boolean plotIt) {
        int[] rawValues = new int[0];
        switch(channel){
        case ImageHandler.CHANNEL_RED:
            synchronized (redValues) {
                // Provide the fft algorithm with input that has a size that is a power of 2.
                int size = (int)Math.pow(2, (int)Math.floor(Math.log(redValues.size()) / Math.log(2)));
                rawValues = new int[size];
                for (int i = 0; i < size; i++) {
                    rawValues[i] = redValues.get(i);
                }
            }
            break;

            case ImageHandler.CHANNEL_GREEN:
          synchronized (greenValues) {
              // Provide the fft algorithm with input that has a size that is a power of 2.
              int size = (int)Math.pow(2, (int)Math.floor(Math.log(greenValues.size()) / Math.log(2)));
              rawValues = new int[size];
              for (int i = 0; i < size; i++) {
                  rawValues[i] = greenValues.get(i);
              }
          }
          break;
        case ImageHandler.CHANNEL_BLUE:
          synchronized (blueValues) {
              // Provide the fft algorithm with input that has a size that is a power of 2.
              int size = (int)Math.pow(2, (int)Math.floor(Math.log(blueValues.size()) / Math.log(2)));
              rawValues = new int[size];
              for (int i = 0; i < size; i++) {
                  rawValues[i] = blueValues.get(i);
              }
          }
          break;

        }
        // Filter the raw data
        int[] filteredInput = firFilter(rawValues);
        for (int i=0; i< filteredInput.length; i++){
            filteredInput[i]=deriv.d(filteredInput[i]);
        }

        if (plotIt) {MainActivity.plotRaw = true;}

        double[] fftOutput = fft(filteredInput);

        // maximum heart rate supported is 240. HR =
        // index*sampling_freq/output.length*60
        // Therefore, max index to look at is 4*output.length/sampling_freq
        // minimum supported HR = 30 -> min = 1/2*output.length/sampling_freq
        int minHrIndex = (int)(fftOutput.length / ImageHandler.SAMPLING_FREQUENCY / 2);
        int maxHrIndex = (int)(4 * fftOutput.length / ImageHandler.SAMPLING_FREQUENCY);

        double max = Integer.MIN_VALUE;
        int index = -1;

        for (int i = minHrIndex; i <= maxHrIndex && i < fftOutput.length; ++i) {
            if (fftOutput[i] > max) {
                max = fftOutput[i];
                index = i;
            }
        }

        double heartRate = index * ImageHandler.SAMPLING_FREQUENCY / fftOutput.length * 60;
        return heartRate;
    }
    public void setChannel(int channel)
    {
        this.channel = channel;
    }
}