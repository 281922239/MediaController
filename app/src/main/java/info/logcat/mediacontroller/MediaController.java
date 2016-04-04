package info.logcat.mediacontroller;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by 0xloggg on 1/4/2016.
 */
public class MediaController extends RelativeLayout {

    private final int BREATH_DURATION = 1500;
    private int leftEdge, rightEdge;

    private boolean isPlaying;

    private ViewDragHelper viewDragHelper;
    private MediaControlListener listener;
    private ImageView centerControl, leftBreath, rightBreath;
    private Animation breathAnim;


    public MediaController(Context context) {
        super(context);
        init(context);
    }

    public MediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MediaController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        viewDragHelper = ViewDragHelper.create(this, 1f, new DragCallback());

        centerControl = new ImageView(context);
        leftBreath = new ImageView(context);
        rightBreath = new ImageView(context);
        LayoutParams centerControlLp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        LayoutParams lBreathLp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        LayoutParams rBreathLp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        centerControlLp.addRule(CENTER_IN_PARENT);
        lBreathLp.addRule(ALIGN_PARENT_LEFT);
        lBreathLp.addRule(CENTER_VERTICAL);
        rBreathLp.addRule(ALIGN_PARENT_RIGHT);
        rBreathLp.addRule(CENTER_VERTICAL);
        centerControl.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
        leftBreath.setImageResource(R.drawable.ic_keyboard_arrow_left_black_36dp);
        rightBreath.setImageResource(R.drawable.ic_keyboard_arrow_right_black_36dp);
        initBreathLight(leftBreath);
        initBreathLight(rightBreath);

        leftBreath.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                viewDragHelper.smoothSlideViewTo(centerControl, leftEdge, getPaddingTop());
                if (listener != null) {
                    listener.onPrevious();
                }
                centerControl.setImageResource(R.drawable.ic_skip_previous_black_48dp);
                isPlaying = true;
            }
        });
        rightBreath.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                viewDragHelper.smoothSlideViewTo(centerControl, rightEdge - centerControl.getWidth(), getPaddingTop());
                if (listener != null) {
                    listener.onNext();
                }
                centerControl.setImageResource(R.drawable.ic_skip_next_black_48dp);
                isPlaying = true;
            }
        });

        addView(leftBreath, lBreathLp);
        addView(rightBreath, rBreathLp);
        addView(centerControl, centerControlLp);
    }

    private void initBreathLight(ImageView view) {
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        view.setFocusable(false);
        breathAnim = new AlphaAnimation(1, 0);
        breathAnim.setDuration(BREATH_DURATION);
        breathAnim.setInterpolator(new LinearInterpolator());
        breathAnim.setRepeatCount(Animation.INFINITE);
        breathAnim.setRepeatMode(Animation.REVERSE);
        view.setAnimation(breathAnim);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        leftEdge = getPaddingLeft();
        rightEdge = MeasureSpec.getSize(widthMeasureSpec) - getPaddingRight();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private class DragCallback extends ViewDragHelper.Callback {

        float downX;

        @Override
        public boolean tryCaptureView(View view, int i) {
            if (view == centerControl) {
                downX = view.getX();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return getPaddingTop();
        }

        @Override
        public int clampViewPositionHorizontal(View view, int left, int dx) {
            hideBreathLight();
            if (left <= leftEdge) {  // 捕获滑动到最左
                centerControl.setImageResource(R.drawable.ic_skip_previous_black_48dp);
                return leftEdge;
            } else if (left >= (rightEdge - view.getWidth())) {  // 捕获滑动到最右
                centerControl.setImageResource(R.drawable.ic_skip_next_black_48dp);
                return rightEdge - view.getWidth();
            } else {
                changeControlIcon();
            }
            return left;
        }

        @Override
        public void onViewReleased(View view, float xvel, float yvel) {
            if (view.getX() == downX) {
                onControlBtnClick();
            } else {
                if (view.getLeft() == leftEdge) {  // 捕获上一曲操作
                    isPlaying = true;   // 切曲操作自动播放
                    if (listener != null) {
                        listener.onPrevious();
                    }
                } else if (view.getLeft() == rightEdge-view.getWidth()) {  // 捕获下一曲操作
                    isPlaying = true;   // 切曲操作自动播放
                    if (listener != null) {
                        listener.onNext();
                    }
                }
            }
            viewDragHelper.smoothSlideViewTo(view, (getWidth() - view.getWidth()) / 2, getPaddingTop());
            showBreathLigth();
            changeControlIcon();
        }
    }

    private void onControlBtnClick() {
        isPlaying = !isPlaying;
        if (listener != null) {
            if (isPlaying) {
                listener.onPlay();
            } else {
                listener.onPause();
            }
        }
        changeControlIcon();
    }

    private void changeControlIcon() {
        centerControl.setImageResource(isPlaying ?
                R.drawable.ic_pause_circle_outline_black_48dp : R.drawable.ic_play_circle_outline_black_48dp);
    }

    private void hideBreathLight() {
        leftBreath.clearAnimation();
        rightBreath.clearAnimation();
        leftBreath.setVisibility(INVISIBLE);
        rightBreath.setVisibility(INVISIBLE);
    }

    private void showBreathLigth() {
        leftBreath.startAnimation(breathAnim);
        rightBreath.startAnimation(breathAnim);
        leftBreath.setVisibility(VISIBLE);
        rightBreath.setVisibility(VISIBLE);
    }

    public void setMediaControlListener(MediaControlListener listener) {
        this.listener = listener;
    }

    @Override
    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        } else if (viewDragHelper.getViewDragState() != ViewDragHelper.STATE_DRAGGING) {
            changeControlIcon();
            viewDragHelper.smoothSlideViewTo(centerControl, (getWidth() - centerControl.getWidth()) / 2, getPaddingTop());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;
    }


    public interface MediaControlListener {
        void onPlay();

        void onPause();

        void onPrevious();

        void onNext();
    }
}
