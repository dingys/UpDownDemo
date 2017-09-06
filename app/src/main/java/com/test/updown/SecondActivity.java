package com.test.updown;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import com.test.updown.callback.UpAndDownListener;
import com.test.updown.utils.Utils;
import com.test.updown.view.UpAndDownView;

public class SecondActivity extends AppCompatActivity {
    private UpAndDownView upAndDownView;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_second);
        upAndDownView = (UpAndDownView) findViewById(R.id.up_down_view);
        findViewById(R.id.show_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpDownView();
            }
        });
        findViewById(R.id.hide_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideUpDownView();
            }
        });
        findViewById(R.id.scroll_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upAndDownView.showView();
            }
        });
        findViewById(R.id.forbid_scroll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (upAndDownView.isSlide()){
                    upAndDownView.setIsSlide(false);
                }else {
                    upAndDownView.setIsSlide(true);
                }
            }
        });
        upAndDownView.setUpDownListener(new UpAndDownListener() {
            @Override
            public void onClose() {
                Toast.makeText(mContext,"Close",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onOpen() {
                Toast.makeText(mContext,"Open",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAssignHeight(int height) {

            }
        });
    }

    public void showUpDownView() {
        android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) upAndDownView
                .getLayoutParams();
        upAndDownView.setDefaultShowHeight(Constant.POI_DEFAULT_SHOW_HEIGHT_BOTTOM);
        params.height = Utils.dp2Px(SecondActivity.this, Constant.POI_DEFAULT_SHOW_HEIGHT_BOTTOM);
        upAndDownView.setLayoutParams(params);
        upAndDownView.setVisibility(View.VISIBLE);
    }

    public void hideUpDownView() {
        upAndDownView.setVisibility(View.GONE);
    }
}
