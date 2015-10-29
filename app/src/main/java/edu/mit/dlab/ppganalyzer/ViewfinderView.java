/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.mit.dlab.ppganalyzer;

import android.annotation.SuppressLint;
import android.content.Context;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 */
@SuppressLint("DrawAllocation")
public final class ViewfinderView extends View {

    private final Paint paint;
    private final int maskColor;
    private final int frameColor;
    private Rect framingRect;
    private int previewWidth, previewHeight;

    private static final int MIN_FRAME_WIDTH = 20;
    private static final int MIN_FRAME_HEIGHT = 20;
    private static final int MAX_FRAME_WIDTH = 480;
    private static final int MAX_FRAME_HEIGHT = 360;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint();
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        frameColor = resources.getColor(R.color.viewfinder_frame);
        this.previewHeight = this.previewWidth = 0;
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        Rect frame = getFramingRect();
        if (frame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // figure out the right coordinates
        float x1 = frame.right - frame.left, x2 = frame.bottom - frame.top;
        float radius = x1 / 2, cx = frame.left + radius, cy = frame.top + x2 / 2;
        if (x2 < x1) {
            radius = x2 / 2;
        }
        int w1 = width / 2, w2 = width - w1;
        if (width > height) {
            --w2;
        }
        Path center = new Path(), outer = new Path();
        RectF r = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);

        // paint the mask
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(maskColor);

        // first half
        outer.moveTo(0, 0);
        outer.lineTo(0, height);
        outer.lineTo(w1, height);
        outer.lineTo(w1, r.bottom);
        if (width > height) {
            outer.arcTo(r, -270, 180.09f);
        } else {
            outer.arcTo(r, -270, 180f);
        }
        outer.lineTo(w1, 0);
        outer.close();
        canvas.drawPath(outer, paint);

        // second half
        outer.reset();
        outer.moveTo(w2, 0);
        outer.lineTo(w2, r.top);
        if (width > height) {
            outer.arcTo(r, -90, 180.09f);
        } else {
            outer.arcTo(r, -90, 180f);
        }
        outer.lineTo(w2, height);
        outer.lineTo(width, height);
        outer.lineTo(width, 0);
        outer.close();
        canvas.drawPath(outer, paint);

        paint.setColor(frameColor);
        paint.setStrokeWidth(1.0f);
        Paint.Style st = paint.getStyle();
        paint.setStyle(Paint.Style.STROKE);
        center.addCircle(cx, cy, radius, Path.Direction.CW);
        canvas.drawPath(center, paint);
        paint.setStyle(st);
    }

    public Rect getFramingRect() {
        if (previewWidth == 0) {
            return null;
        }
        int width = (int)(previewWidth * ImageHandler.getRadius());
        if (width < MIN_FRAME_WIDTH) {
            width = MIN_FRAME_WIDTH;
        } else if (width > MAX_FRAME_WIDTH) {
            width = MAX_FRAME_WIDTH;
        }
        int height = (int)(previewHeight * ImageHandler.getRadius());
        if (height < MIN_FRAME_HEIGHT) {
            height = MIN_FRAME_HEIGHT;
        } else if (height > MAX_FRAME_HEIGHT) {
            height = MAX_FRAME_HEIGHT;
        }
        int leftOffset = (previewWidth - width) / 2;
        int topOffset = (previewHeight - height) / 2;

        framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);

        return framingRect;
    }

    public void setPreviewSize(int w, int h) {
        this.previewWidth = w;
        this.previewHeight = h;
    }

}