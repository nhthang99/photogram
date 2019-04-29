package com.nht.instagram.Utils;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class SquareImageView extends AppCompatImageView {
    public SquareImageView(Context context){
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    public SquareImageView(Context context, AttributeSet attributeSet, int defStyleAttr){
        super(context, attributeSet, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
