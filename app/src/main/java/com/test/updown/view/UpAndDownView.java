package com.test.updown.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import com.test.updown.R;
import com.test.updown.callback.UpAndDownListener;

/**
 * Created by dingyasong on 2017/8/31.
 */

public class UpAndDownView extends CustomUpAndDownLayout {
    private Context mContext;

    public UpAndDownView(Context context) {
        super(context);
        initView(context);
    }

    public UpAndDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void initView(final Context mContext) {
        this.mContext = mContext;
        View view = LayoutInflater.from(mContext).inflate(R.layout.up_down_layout, null, false);
        addView(view);
        InnerScrollView innerScrollView = (InnerScrollView) view.findViewById(R.id.scrollView);
        if (innerScrollView != null) {
            innerScrollView.parentView = UpAndDownView.this;
        }
        view.findViewById(R.id.textview).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Click the TextView", 0).show();
            }
        });
    }

    public void setUpDownListener(UpAndDownListener listener) {
        setListener(listener);
    }

    public void setIsSlide(boolean isSlide) {
        isSlide(isSlide);
    }

    public boolean isSlide() {
        return isCanSlide();
    }

}
