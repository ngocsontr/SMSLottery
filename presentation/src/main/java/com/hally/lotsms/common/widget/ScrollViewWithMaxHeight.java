package com.hally.lotsms.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class ScrollViewWithMaxHeight extends ScrollView {

    public ScrollViewWithMaxHeight(Context context) {
        super(context);
    }

    public ScrollViewWithMaxHeight(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollViewWithMaxHeight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}