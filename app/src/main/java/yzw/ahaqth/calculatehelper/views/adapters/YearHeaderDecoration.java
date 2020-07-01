package yzw.ahaqth.calculatehelper.views.adapters;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class YearHeaderDecoration extends RecyclerView.ItemDecoration {
    private List<LocalDate> mList;
    private int headerHeight = 120;
    private TextPaint textPaint;
    private int textSize = 60;
    private Paint bgPaint;
    private int bgColor = Color.parseColor("#55000000");
    private int textColor = Color.WHITE;

    private void drawHeader(Canvas c, RecyclerView parent, int i) {
        c.save();
        View child = parent.getChildAt(i);
        int index = parent.getChildAdapterPosition(child);
        int left = 0;
        int right = parent.getWidth();
        int bottom = (int) (child.getTop() - 40 + child.getTranslationY());
        int top = bottom - headerHeight + 40;
        int centerY = top + 40;
        int centerX = (right - left) / 2;
        Rect bg = new Rect(left, top, right, bottom);
        c.drawRect(bg, bgPaint);
        int baseLine = (int) (centerY + (textPaint.descent() - textPaint.ascent()) / 2 - textPaint.descent());
        String text = mList.get(index).format(DateTimeFormatter.ofPattern("yyyy年", Locale.CHINA));
        c.drawText(text, centerX, baseLine, textPaint);
        c.restore();
    }

    public YearHeaderDecoration(List<LocalDate> list) {
        mList = list;
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStrokeWidth(12f);

        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(bgColor);
        bgPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            int index = parent.getChildAdapterPosition(parent.getChildAt(i));
            int month = mList.get(index).getMonth().getValue();
            if (month == 12) {
                drawHeader(c, parent, i);
            }
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int index = parent.getChildAdapterPosition(view);
        int month = mList.get(index).getMonth().getValue();
        if (month == 12 || month == 11 || month == 10 || month == 9) {
            outRect.top = headerHeight;
        } else {
            outRect.top = 0;
        }
    }
}
