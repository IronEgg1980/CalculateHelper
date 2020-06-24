package yzw.ahaqth.calculatehelper.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import yzw.ahaqth.calculatehelper.tools.BigDecimalHelper;
import yzw.ahaqth.calculatehelper.tools.Tools;

public class BrokenLineGraph extends View {
    public interface BrokenLineGraphEntity {
        String getLabel();

        double getValue();
    }

    private class FlingRunnable implements Runnable {
        int mInitX, mVelocityX, mMinX, mMaxX;

        void start(int initX,
                   int velocityX,
                   int minX,
                   int maxX) {
            this.mInitX = initX;
            this.mVelocityX = velocityX;
            this.mMinX = minX;
            this.mMaxX = maxX;

            // 先停止上一次的滚动
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }

            // 开始 fling
            mScroller.fling(initX, 0, velocityX,
                    0, 0, maxX, 0, 0);
            post(this);
        }

        void stop() {
            if (!mScroller.isFinished())
                mScroller.abortAnimation();
        }

        @Override
        public void run() {
            // 如果已经结束，就不再进行
            if (!mScroller.computeScrollOffset()) {
                return;
            }

            // 计算偏移量
            int currX = mScroller.getCurrX();
            int diffX = mInitX - currX;

            // 用于记录是否超出边界，如果已经超出边界，则不再进行回调，即使滚动还没有完成
            boolean isEnd = false;

            if (diffX != 0) {
                // 超出右边界，进行修正
                if (getScrollX() >= totalWidth - screenWidth) {
                    diffX = totalWidth - screenWidth - getScrollX();
                    isEnd = true;
                }

                // 超出左边界，进行修正
                if (getScrollX() <= 0) {
                    diffX = -getScrollX();
                    isEnd = true;
                }

                if (!mScroller.isFinished()) {
                    scrollBy(diffX, 0);
                }
                mInitX = currX;
            }

            if (!isEnd) {
                post(this);
            }

        }
    }

    private final String TAG = "殷宗旺";
    private BrokenLineGraphEntity[] dataList;
    private double maxValue = 0, perValue;
    private int itemWidth, itemHeight;
    private int screenWidth, totalWidth, totalHeight, padding;
    private float ellipsWidth = 0;
    private int xlineCount;
//    private int maxRight;

    private List<Point> points;
    private List<RectF> valuesBgs;
    private List<Integer> valuesBaseLines;
    private Path linePath;
    private List<String> labels;
    private Paint pointPaint, linePaint, textPaint;
    private float textBaseLine, textHeight;
    private boolean unCaculated = true;
    private Scroller mScroller;
    private FlingRunnable flingRunnable;
    private VelocityTracker velocityTracker;
    private int mMaximumVelocity, mMinimumVelocity;

    public void setData(@NonNull BrokenLineGraphEntity[] list) {
        this.unCaculated = true;
        this.dataList = list;
        for (BrokenLineGraphEntity entity : list) {
            maxValue = Math.max(entity.getValue(), maxValue);
        }
        generateLabel();
        requestLayout();
    }

    private void generatePoint() {
        linePath = new Path();
        if (valuesBgs == null)
            valuesBgs = new ArrayList<>();
        if (valuesBaseLines == null)
            valuesBaseLines = new ArrayList<>();
        valuesBgs.clear();
        valuesBaseLines.clear();
        if (points == null)
            points = new ArrayList<>();
        points.clear();

        int valueBgPadding = 20;

        for (int i = 0; i < dataList.length; i++) {
            BrokenLineGraphEntity entity = dataList[i];
            double ratio = BigDecimalHelper.divide(maxValue - entity.getValue(), maxValue, 2);
            Point point = new Point();
            point.x = padding + itemWidth * (i + 1);
            point.y = (int) (itemHeight * ratio) + padding;
            points.add(point);

            if (i == 0)
                linePath.moveTo(point.x, point.y);
            else
                linePath.lineTo(point.x, point.y);

            Rect rect = new Rect();
            String text = String.valueOf(dataList[i].getValue());
            textPaint.getTextBounds(text, 0, text.length(), rect);

            float bottom = point.y - 25;
            float left = point.x - rect.width() / 2 - valueBgPadding;
            float top = bottom - valueBgPadding * 2 - rect.height();
            float right = point.x + rect.width() / 2 + valueBgPadding;

            RectF rectF = new RectF(left, top, right, bottom);
            valuesBgs.add(rectF);

            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            int baseLine = (int) ((fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent + (bottom - rect.height() / 2 - valueBgPadding));
            valuesBaseLines.add(baseLine);
        }
    }

    private void generateLabel() {
        if (labels == null)
            labels = new ArrayList<>();
        labels.clear();

        StringBuilder stringBuilder = new StringBuilder();
        for (BrokenLineGraphEntity entity : dataList) {
            stringBuilder.delete(0, stringBuilder.length());
            String text = entity.getLabel();

            Rect rect = new Rect();
            textPaint.getTextBounds(text, 0, text.length(), rect);
            textHeight = Math.max(textHeight, rect.height());
            float[] widths = new float[text.length()];
            textPaint.getTextWidths(text, widths);
            int totalTextWidth = 0;
            for (int i = 0; i < widths.length; i++) {
                totalTextWidth = (int) (totalTextWidth + widths[i]);
                if (totalTextWidth + ellipsWidth >= itemWidth) {
                    stringBuilder.append("...");
                    break;
                } else {
                    stringBuilder.append(text.charAt(i));
                }
            }
            labels.add(stringBuilder.toString());
        }
    }

    private void setTextBaseLine() {
        if (dataList == null || dataList.length == 0)
            return;
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        textBaseLine = totalHeight - textHeight / 2 - padding + 50 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
    }

    private void initial(Context context) {
        this.screenWidth = MainActivity.SCREEN_WIDTH;

        this.padding = Tools.dip2px(context, 48);
        this.itemWidth = Tools.dip2px(context, 64);
        this.itemHeight = Tools.dip2px(context, 120);
        this.totalHeight = itemHeight + padding * 2;
        this.totalWidth = dataList == null || dataList.length == 0 ? itemWidth : itemWidth * dataList.length;
        this.totalWidth = this.totalWidth + padding * 2;

        this.textPaint = new Paint();
        this.textPaint.setTextAlign(Paint.Align.CENTER);
        this.textPaint.setColor(Color.WHITE);
        this.textPaint.setAntiAlias(true);
        this.textPaint.setTextSize(Tools.dip2px(context, 12));
        this.textPaint.setTextScaleX(1f);
        this.textPaint.setStrokeWidth(2f);

        this.ellipsWidth = this.textPaint.measureText("...");

        this.pointPaint = new Paint();
        this.pointPaint.setStrokeWidth(20);
        this.pointPaint.setAntiAlias(true);
        this.pointPaint.setColor(Color.GREEN);

        this.linePaint = new Paint();
        this.linePaint.setAntiAlias(true);
        this.linePaint.setStrokeWidth(2);
        this.linePaint.setColor(Color.GREEN);
        this.linePaint.setStyle(Paint.Style.STROKE);

        this.mScroller = new Scroller(context);
        this.flingRunnable = new FlingRunnable();
        this.mMaximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        this.mMinimumVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
    }

    public BrokenLineGraph(Context context) {
        super(context);
        initial(context);
    }

    public BrokenLineGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initial(context);
    }

    public BrokenLineGraph(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initial(context);
    }

    public void calculateValues() {
        String perValueString = String.valueOf(BigDecimalHelper.divide(maxValue, xlineCount));
        int index = perValueString.contains(".") ? perValueString.indexOf(".") : perValueString.length();
        int firstInt = Integer.parseInt(perValueString.substring(0, 1));
        perValue = (firstInt + 1) * Math.pow(10, (index - 1));
        maxValue = BigDecimalHelper.multiply(perValue, xlineCount);
        unCaculated = false;
    }

    private void drawXY(Canvas canvas) {
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(0.75f);
        int perHeight = itemHeight / xlineCount;
        for (int i = 0; i <= xlineCount; i++) {
            int y = padding + perHeight * i;
            int right = padding + itemWidth * dataList.length;
            canvas.drawLine(padding, y, right, y, linePaint);
        }
        for (int i = 0; i <= dataList.length; i++) {
            int x = itemWidth * i + padding;
            canvas.drawLine(x, padding, x, itemHeight + padding, linePaint);
        }
    }

    private void drawSideBar(Canvas canvas) {
        int perHeight = itemHeight / xlineCount;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(getScrollX(), 0, getScrollX() + padding, getBottom(), paint);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        for (int i = 0; i <= xlineCount; i++) {
            int y = padding + perHeight * i;
            int value = (int) (maxValue - i * perValue);
            int baseLine = (int) (y + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent);
            canvas.drawText(String.valueOf(value), getScrollX() + padding - 10, baseLine, textPaint);
        }
    }

    private void drawValue(Canvas canvas) {
        textPaint.setTextAlign(Paint.Align.CENTER);
        for (int index = 0; index < dataList.length; index++) {
            Point point = points.get(index);
            pointPaint.setColor(Color.GREEN);
            canvas.drawCircle(point.x, point.y, 10, pointPaint);
            pointPaint.setColor(Color.parseColor("#008577"));
            canvas.drawRoundRect(valuesBgs.get(index), 20, 20, pointPaint);
            canvas.drawText(String.valueOf(dataList[index].getValue()), point.x, valuesBaseLines.get(index), textPaint);
            canvas.drawText(labels.get(index), point.x, textBaseLine, textPaint);//底部label
        }
    }

    private void drawBg(Canvas canvas) {
        RectF rectF = new RectF(0, 0, getRight(), getBottom());
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawRect(rectF, paint);
    }

    private void constrainScroll(int dx) {
        Rect rect = new Rect();
        getLocalVisibleRect(rect);
        if (dx > 0) {
            dx = Math.min(dx, totalWidth - rect.right);
        } else if (dx < 0) {
            dx = Math.max(dx, 0 - rect.left);
        }
        scrollBy(dx, 0);
    }

//    @Override
//    public void computeScroll() {
//        if (mScroller.computeScrollOffset()) {
//            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
//            // 通过重绘让系统调用onDraw，onDraw中又会调用computeScroll，如此不断循环，直到Scroller执行完毕
//            postInvalidate();
//        }
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        totalWidth = dataList == null || dataList.length == 0 ? itemWidth : itemWidth * dataList.length;
        totalWidth = Math.max(width, totalWidth + padding * 2);
        totalHeight = Math.max(height, itemHeight + padding * 2);
        itemHeight = (int) (totalHeight - padding * 2 - textHeight);
        xlineCount = itemHeight / itemWidth;
        setMeasuredDimension(totalWidth, totalHeight);
        if (unCaculated) {
            setTextBaseLine();
            calculateValues();
            generatePoint();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBg(canvas);
        drawXY(canvas);

        linePaint.setColor(Color.YELLOW);
        linePaint.setStrokeWidth(2f);
        canvas.drawPath(linePath, linePaint);
        drawValue(canvas);

        drawSideBar(canvas);
    }

    private int lastX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                flingRunnable.stop();
                lastX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                int offset = lastX - x;
                constrainScroll(offset);
                lastX = x;
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                // 获取横向速度
                int velocityX = (int) velocityTracker.getXVelocity();
                // 速度要大于最小的速度值，才开始滑动
                if (Math.abs(velocityX) > mMinimumVelocity) {
                    int initX = getScrollX();
                    int maxX = totalWidth - screenWidth;
                    if (maxX > 0) {
                        flingRunnable.start(initX, velocityX, 0, maxX);
                    }
                }

                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }

                break;
        }
        return true;
    }
}
