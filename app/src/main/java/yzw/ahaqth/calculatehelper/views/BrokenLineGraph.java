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
import android.view.View;

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

    private BrokenLineGraphEntity[] dataList;
    private double maxValue = 0, perValue;
    private int itemWidth, itemHeight;
    private int totalWidth, totalHeight, padding;
    private float ellipsWidth = 0;
    private int xlineCount;

    private List<Point> points;
    private List<Rect> valuesBgs;
    private Path linePath;
    private List<String> labels;
    private Paint pointPaint, linePaint, textPaint;
    private float textBaseLine, textHeight;


    public void setData(@NonNull BrokenLineGraphEntity[] list) {
        this.dataList = list;
        for (BrokenLineGraphEntity entity : list) {
            maxValue = Math.max(entity.getValue(), maxValue);
        }
        generateLabel();
        invalidate();
    }

    private void generatePoint() {
        linePath = new Path();
        if (valuesBgs == null)
            valuesBgs = new ArrayList<>();
        valuesBgs.clear();
        if (points == null)
            points = new ArrayList<>();
        points.clear();

        int valueBgPadding = 20;

        for (int i = 0; i < dataList.length; i++) {
            BrokenLineGraphEntity entity = dataList[i];
            double ratio = BigDecimalHelper.divide(maxValue - entity.getValue(), maxValue, 2);
            Point point = new Point();
            point.x = itemWidth / 2 + padding + itemWidth * i;
            point.y = (int) (itemHeight * ratio) + padding;
            points.add(point);

            if (i == 0)
                linePath.moveTo(point.x, point.y);
            else
                linePath.lineTo(point.x, point.y);

            Rect rect = new Rect();
            String text = String.valueOf(dataList[i].getValue());
            textPaint.getTextBounds(text, 0, text.length(), rect);
            rect.left = point.x - rect.width() / 2 - valueBgPadding;
            rect.right = point.x + rect.width() / 2 + valueBgPadding;
            rect.top = point.y - rect.height() - valueBgPadding * 2 - 50;
            rect.bottom = point.y - 50;
            valuesBgs.add(rect);
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
        this.padding = Tools.dip2px(context, 40);
        this.itemWidth = Tools.dip2px(context, 80);
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
    }

    private void drawXY(Canvas canvas) {
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(0.75f);
        int perHeight = itemHeight / xlineCount;
        for (int i = 0; i <= xlineCount; i++) {
            int y = padding + perHeight * i;
            int value = (int) (maxValue - i * perValue);
            int right = padding + itemWidth * dataList.length;
            canvas.drawLine(padding, y, right, y, linePaint);
            canvas.drawText(String.valueOf(value), padding, y, textPaint);
        }
        for (int i = 0; i <= dataList.length; i++) {
            int x = itemWidth * i + padding;
            canvas.drawLine(x, padding, x, itemHeight + padding, linePaint);
        }
    }

    private void drawValue(Canvas canvas, int index) {
        Point point = points.get(index);
        canvas.drawCircle(point.x, point.y, 10, pointPaint);
        String text = String.valueOf(dataList[index]);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        Rect rect = valuesBgs.get(index);
        int baseLine = (int) (((fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent)+(rect.height() / 2));
        canvas.drawRoundRect(rect.left, rect.top, rect.right, rect.bottom, 20, 20, pointPaint);
        canvas.drawText(text,point.x,baseLine,textPaint);
    }

    private void drawBg(Canvas canvas) {
        RectF rectF = new RectF(0, 0, getRight(), getBottom());
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawRect(rectF, paint);
    }

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
        setTextBaseLine();
        setMeasuredDimension(totalWidth, totalHeight);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        calculateValues();
        generatePoint();

        drawBg(canvas);
        drawXY(canvas);
        linePaint.setColor(Color.YELLOW);
        linePaint.setStrokeWidth(2f);

        canvas.drawPath(linePath, linePaint);

        for (int i = 0; i < dataList.length; i++) {
            drawValue(canvas, i);
//            canvas.drawText(labels.get(i), point.x, textBaseLine, textPaint);
//            if (i > 0) {
//                Point prePoint = points.get(i - 1);
//                canvas.drawLine(prePoint.x, prePoint.y, point.x, point.y, linePaint);
//                canvas.drawCircle(prePoint.x, prePoint.y, 10, pointPaint);
//                canvas.drawText(String.valueOf(dataList[i - 1].getValue()), prePoint.x, prePoint.y - 25, textPaint);
//            }
////            else {
////                canvas.drawCircle(point.x, point.y, 20, pointPaint);
////            }
//            if (i == dataList.length - 1) {
//                canvas.drawCircle(point.x, point.y, 10, pointPaint);
//                canvas.drawText(String.valueOf(dataList[i].getValue()), point.x, point.y - 25, textPaint);
//            }
        }
    }
}
