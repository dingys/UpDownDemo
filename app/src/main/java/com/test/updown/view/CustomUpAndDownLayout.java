package com.test.updown.view;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;
import com.test.updown.callback.UpAndDownListener;
import com.test.updown.utils.Utils;

/**
 * 可上下滑动的view
 *
 * @author dingys
 * @description:
 */
public class CustomUpAndDownLayout extends RelativeLayout implements
        OnTouchListener {
    private Context mContext;
    private int screenHeigh;
    private VelocityTracker mVelocityTracker;
    private int slideState;
    private boolean isSliding;
    private int moveMinValue = 20;// 单位dp,在做事件拦截时使用
    private int defaultShowHeight = 200;// 单位dp
    // 是否已经完全显示了
    private boolean isFullVisible = false;
    /*
     * 滚动显示和隐藏上侧布局时，手指滑动需要达到的速度。
     */
    public static final int SNAP_VELOCITY = 200;

    /**
     * 滑动状态 表示未进行任何滑动。
     */
    public static final int DO_NOTHING = 0;
    /**
     * 滑动状态，表示正在往下滑动
     */
    public static final int HIDING = 1;

    /**
     * 表示View 正在显示
     */
    public static final int SHOWING = 3;
    /**
     * 滑动状态的一种，表示已经到达了底部时，继续向下滑动。
     */
    public static final int HIDEING_2 = 4;
    private int touchSlop;//在被判定为滚动之前用户手指可以移动的最大值。
    private float xDown;
    private float yDown;
    private float xMove;
    private float yMove;
    private float yUp;
    private LayoutParams contentLayoutParams;
    private boolean once = false;
    private int showMaxHeight;// 像素
    private UpAndDownListener upAndDownListener;
    private boolean isSlide = true;

    public CustomUpAndDownLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public CustomUpAndDownLayout(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public void setListener(UpAndDownListener upAndDownListener) {
        this.upAndDownListener = upAndDownListener;
    }

    public void setDefaultShowHeight(int showHeight) {
        this.defaultShowHeight = showHeight;
    }

    public void isOnce(boolean once, boolean isFull) {
        this.once = once;
        this.isFullVisible = isFull;
    }

    // 当底部bar显示或者隐藏时，重新设置this显示的最大高度
    public void setMaxHeight(int h) {
        showMaxHeight = h;
        // 当是全屏时重新设置它的高度(情况有：底部状态栏显示隐藏，和横竖屏切换)
        if (isFullVisible) {
            if (contentLayoutParams != null) {
                contentLayoutParams.height = h;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CustomUpAndDownLayout.this
                                .setLayoutParams(contentLayoutParams);
                    }
                }, 10);
            }
        }

    }

    public int getShowMaxH() {
        return showMaxHeight;
    }


    int n = 0;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!once) {
            // 横竖屏切换时重新设置isFullVisible=false
            if (contentLayoutParams != null) {// 重新设置高度
                contentLayoutParams.height = Utils.dp2Px(mContext,
                        defaultShowHeight);
                this.setLayoutParams(contentLayoutParams);
            }
            // 重新设置up and down icon
            if (upAndDownListener != null) {
                if (isFullVisible) {
                    upAndDownListener.onOpen();
                } else {
                    upAndDownListener.onClose();
                }
            }
            once = true;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int arg1, int arg2, int arg3,
                            int arg4) {
        contentLayoutParams = (LayoutParams) this.getLayoutParams();
        super.onLayout(changed, arg1, arg2, arg3, arg4);
    }

    private void init() {
        screenHeigh = Utils.getScreenHeight(mContext);
        showMaxHeight = screenHeigh;
        setOnTouchListener(this);
        touchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();

    }


    /**
     * 设置是否允许滑动
     *
     * @param isSlide
     */
    public void isSlide(boolean isSlide) {
        this.isSlide = isSlide;
    }

    public boolean isCanSlide() {
        return isSlide;
    }

    private boolean isScrolling = false;
    public float downY;// 抬起时判断滑动距离时使用

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (!isSlide) {
            return false;
        }
        createVelocityTracker(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下时，记录按下时的坐标
                xDown = event.getRawX();
                yDown = event.getRawY();
                downY = event.getRawY();
                // 将滑动状态初始化为DO_NOTHING
                slideState = DO_NOTHING;

                // currTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                if (xDown == 0 && yDown == 0) {
                    xDown = event.getRawX();
                    yDown = event.getRawY();
                    downY = event.getRawY();
                } else {
                    xMove = event.getRawX();
                    yMove = event.getRawY();
                    int moveDistanceX = (int) (xMove - xDown);
                    int moveDistanceY = (int) (yMove - yDown);
                    // 当默认是要显示全屏时，这个地方获取到的高度是-1（原因不清楚）
                    if (contentLayoutParams.height == -1) {
                        contentLayoutParams.height = showMaxHeight;
                    }
                    int tempheight = contentLayoutParams.height;// 得当前Layout距离顶部的距离
                    // 当布局已经到达底部或者已经到达顶部时，移动无效
                    if ((tempheight >= showMaxHeight && moveDistanceY < 0)
                            || (tempheight <= Utils.dp2Px(mContext,
                            defaultShowHeight) && moveDistanceY > 0)) {
                        break;
                    }
                    // 检查当前的滑动状态
                    checkSlideState(moveDistanceX, moveDistanceY);
                    switch (slideState) {
                        case HIDING:
                            contentLayoutParams.height = showMaxHeight - moveDistanceY;
                            setLayoutParams(contentLayoutParams);
                            break;
                        case HIDEING_2:
                        case SHOWING:
                            int height = contentLayoutParams.height;
                            contentLayoutParams.height = height - moveDistanceY;
                            setLayoutParams(contentLayoutParams);
                            yDown = event.getRawY();
                            break;
                        default:
                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                yUp = event.getRawY();
                if (isSliding) {
                    // 手指抬起时，进行判断当前手势的意图，这里可根据当前的位置判断显示还是隐藏
                    switch (slideState) {
                        case HIDING:
                        case HIDEING_2:
                            if (shouldScrollToUpMenu()) {
                                scrollToUpMenu();
                            } else {
                                scrollToContentFromUpMenu();
                            }
                            break;
                        case SHOWING:
                            if (shouldScrollToContentFromUpMenu()) {
                                scrollToContentFromUpMenu();
                            } else {
                                scrollToUpMenu();
                            }
                            break;
                        default:
                            break;
                    }
                }
                recycleVelocityTracker();
                break;
        }
        return true;
    }

    /*
     * 将界面从底部界面滚动到内容界面，滚动速度设定为30.
     */
    public void scrollToContentFromUpMenu() {
        new UpMenuScrollTask().execute(300);
    }

    /**
     * 判断是否应该滚动将底部界面展示出来。如果手指移动距离大于屏幕宽度的1/4，或者手指移动速度大于SNAP_VELOCITY，
     * 就认为应该滚动将底部界面展示出来。
     *
     * @return 如果应该将底部界面展示出来返回true，否则返回false。
     */
    private boolean shouldScrollToUpMenu() {
        return yUp - downY > 8;
    }

    /**
     * 判断是否应该从底部界面滚动到内容布局，如果手指移动距离大于屏幕宽度的1/4，或者手指移动速度大于SNAP_VELOCITY，且必须是向上滚动的
     * 就认为应该从底部界面滚动到内容布局。
     *
     * @return 如果应该从底部界面滚动到内容布局返回true，否则返回false。
     */
    private boolean shouldScrollToContentFromUpMenu() {
        float moveY = yUp - downY;
        return (downY - yUp > screenHeigh / 4 || getScrollVelocity() > SNAP_VELOCITY)
                && moveY < 0;
    }

    /**
     * 获取手指在绑定布局上的滑动速度。
     *
     * @return 滑动速度，以每秒钟移动了多少像素值为单位。
     */
    private int getScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) mVelocityTracker.getXVelocity();
        return Math.abs(velocity);
    }

    /**
     * 将界面滚动到底部界面界面，滚动速度设定为-30.
     */
    public void scrollToUpMenu() {
        new UpMenuScrollTask().execute(-300);
    }


    /**
     * 检查滑动状态
     *
     * @param moveDistanceX
     * @param moveDistanceY
     */
    private void checkSlideState(int moveDistanceX, int moveDistanceY) {
        if (!isFullVisible) {
            // Y小于0 向上滑   Y 大于0 向下滑
            if (!isSliding && Math.abs(moveDistanceY) >= touchSlop
                    && moveDistanceY < 0) {
                // 当在Y方向滑动的距离 大于手指滑动的最小距离 且向上滑动的时候
                isSliding = true;
                slideState = SHOWING;
            } else if (!isSliding && Math.abs(moveDistanceY) >= touchSlop
                    && moveDistanceY > 0) {
                isSliding = true;
                slideState = HIDEING_2;
            }
        } else {
            // true是其全屏显示的时候，此时向上滑动不起作用，因此只在向下滑动做处理，此处少考虑了一种情况，当向下滑动之后，再
            if (!isSliding && Math.abs(moveDistanceY) >= touchSlop
                    && moveDistanceY > 0 && Math.abs(moveDistanceX) < touchSlop) {
                isSliding = true;
                slideState = HIDING;
                contentLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                contentLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                setLayoutParams(contentLayoutParams);
            }
        }
    }

    private void createVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    public boolean isShow() {
        return isFullVisible;
    }

    /**
     * 显示
     */
    public void showView() {
        new UpMenuScrollTask().execute(50);
    }

    /**
     * 隐藏
     */
    public void hideView() {
        new UpMenuScrollTask().execute(-50);
    }

    /**
     * 切换
     */
    public void toggle() {
        if (isFullVisible) {
            hideView();
        } else {
            showView();
        }
    }


    class UpMenuScrollTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected Integer doInBackground(Integer... speed) {
            contentLayoutParams = (LayoutParams) getLayoutParams();
            int height = contentLayoutParams.height;
            // 根据传入的速度来滚动界面，当滚动到达边界值时，跳出循环。
            while (true) {
                height = height + speed[0];
                if (height >= showMaxHeight) {
                    height = showMaxHeight;
                    break;
                }
                if (height <= Utils.dp2Px(mContext, defaultShowHeight)) {
                    height = Utils.dp2Px(mContext, defaultShowHeight);
                    break;
                }
                isScrolling = true;
                publishProgress(height);
                // 为了要有滚动效果产生，每次循环使线程睡眠一段时间，这样肉眼才能够看到滚动动画。
                sleep(10);
            }
            if (speed[0] > 0) {
                isFullVisible = true;
                isScrolling = false;

            } else {
                isFullVisible = false;

            }
            slideState = DO_NOTHING;
            isSliding = false;
            return height;
        }

        @Override
        protected void onProgressUpdate(Integer... height) {
            contentLayoutParams.height = height[0];
            setLayoutParams(contentLayoutParams);
            unFocusBindView();
        }

        @Override
        protected void onPostExecute(Integer height) {
            contentLayoutParams.height = height;
            setLayoutParams(contentLayoutParams);
            if (upAndDownListener != null) {
                if (isFullVisible) {
                    upAndDownListener.onOpen();
                } else {
                    upAndDownListener.onClose();
                }
            }
        }
    }

    private void unFocusBindView() {
        setPressed(false);
        setFocusable(false);
        setFocusableInTouchMode(false);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 回收VelocityTracker对象。
     */
    private void recycleVelocityTracker() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

    float xDown1 = 0;
    float yDown1 = 0;
    boolean bScrollUpDown = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isScrolling = false;
                xDown = 0;
                yDown = 0;
                xDown1 = ev.getX();
                yDown1 = ev.getY();
                bScrollUpDown = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = ev.getX();
                float moveY = ev.getY();
                float disX = moveX - xDown1;
                float disY = moveY - yDown1;
                if (Math.abs((int) disY) > Utils.dp2Px(mContext, moveMinValue)) {
                    bScrollUpDown = Math.abs(disX * 1000) < Math.abs(disY * 1000);
                }
                return bScrollUpDown;
            case MotionEvent.ACTION_UP:
                break;
        }
        boolean bRet = super.onInterceptTouchEvent(ev);
        return bRet;
    }
}