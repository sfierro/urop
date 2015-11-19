package edu.mit.dlab.ppganalyzer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO
 */
public class GraphView2 extends View {

    private Canvas canvas;
    private Paint paint;
    private String[] verlabels;

    public List<Integer> redValues = new ArrayList<Integer>();

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
    private Boolean autoscale = true;

    private double incrementX, slowFactor;

    public void setRedValues(List<Integer> reds) {
        this.redValues = reds;
    }

//
//    public GraphView2(Context context) {
//        super(context);
//        init();
//    }

    public GraphView2(Context context, AttributeSet attributes) {
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
        this.incrementX = 1.0 * width / bufferSize;
        this.bufferSize = (int)Math.pow(2, 10);
        this.slowFactor = 1;
        this.redValues = MainActivity.getRedValues();
//        this.verlabels = new String[] {
//                maximumPlottableValue + "", maximumPlottableValue / 2 + "", "0"
//        };
        this.verlabels = GraphViewFinal.verlabels;
        paint = new Paint();
        paint.setStrokeWidth(2);
        border = 20;
    }

    /*
    * This method is called every time the view is invalidated. We can then
    * redraw the graph. (non-Javadoc)
    * @see android.view.View#onDraw(android.graphics.Canvas)
    */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;

        int i;
        float horstart = border * 2;
        graphheight = getHeight() - 2 * border;

//        if (autoscale) {
//            scale();
//        }

        incrementX = 1.0 * getWidth() / bufferSize;
//        if (redPlotOn) {
//            synchronized (redValues) {
                plot(MainActivity.getRedValues(), Color.RED);
//            }
//        }

        lastX = lastX + (int)width;

        paint.setTextAlign(Paint.Align.LEFT);
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
        paint.setTextAlign(Paint.Align.CENTER);
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
     * Resizes the axes.
     */
    private void scale() {
        minY = Integer.MAX_VALUE;
        maxY = 0;

//        // Only red is synchronized here because I stopped working with the other colors
//        synchronized (redValues) {
//            if (redPlotOn && redValues.size() > 0) {
                minY = Math.min(minY, Collections.min(MainActivity.getRedValues()));
                maxY = Math.max(maxY, Collections.max(MainActivity.getRedValues()));
//            }
//        }

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


}
