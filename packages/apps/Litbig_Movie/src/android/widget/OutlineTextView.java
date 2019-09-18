package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

public class OutlineTextView extends TextView {
    private int mOutlineColor = 0xFF000000;

    public OutlineTextView(Context context) {
        super(context);
    }

    public OutlineTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public OutlineTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        ColorStateList states = getTextColors();
        getPaint().setStyle(Style.STROKE);
        getPaint().setStrokeWidth(0);
        setTextColor(mOutlineColor);
        super.onDraw(canvas);
        getPaint().setStyle(Style.FILL);
        setTextColor(states);
        super.onDraw(canvas);
    }

    public void setOutlineColor(int color) {
        mOutlineColor = color;
    }
}
