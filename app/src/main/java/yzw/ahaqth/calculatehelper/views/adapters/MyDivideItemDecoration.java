package yzw.ahaqth.calculatehelper.views.adapters;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyDivideItemDecoration extends RecyclerView.ItemDecoration {
    private final Rect mBounds;
    private Paint mPaint;
    private int dividerSize = 4;

    public MyDivideItemDecoration() {
        mBounds = new Rect();
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#dddddd"));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(dividerSize);
        mPaint.setAntiAlias(true);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) != state.getItemCount() - 1) {
            outRect.bottom = dividerSize;
        } else {
            outRect.bottom = 0;
        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (parent.getLayoutManager() != null) {
            this.drawVertical(c, parent,state);
        }
    }

    private void drawVertical(Canvas canvas, RecyclerView parent,RecyclerView.State state) {
        canvas.save();
        int left;
        int right;
        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            canvas.clipRect(left, parent.getPaddingTop(), right, parent.getHeight() - parent.getPaddingBottom());
        } else {
            left = 0;
            right = parent.getWidth();
        }

        int childCount = parent.getChildCount();

        for(int i = 0; i < childCount; ++i) {
            View child = parent.getChildAt(i);
            if(parent.getChildAdapterPosition(child) != state.getItemCount() - 1) {
                parent.getDecoratedBoundsWithMargins(child, this.mBounds);
                int bottom = this.mBounds.bottom + Math.round(child.getTranslationY());
                int center = bottom - dividerSize / 2;
                canvas.drawLine(left,center,right,center,mPaint);
            }
        }
        canvas.restore();
    }
}
