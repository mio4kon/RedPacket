package com.mio4kon.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by mio4kon on 16/8/19.
 */
public class RedPacketView extends ImageView {

    //自动显示延迟时间
    public static final long DELAY_START_TIME = 4000;
    public static final long RELATIVE_ERROR = 1;
    //动画默认执行时间
    public static final long DEFAULT_FADE_DURATION = 500;
    public static final long DEFAULT_SUCK_DURATION = 500;
    public static final long DEFAULT_BUBBLE_DURATION = 1500;
    private boolean animationPlaying = false;
    private long mFadeDuration = DEFAULT_FADE_DURATION;
    private long mSuckDuration = DEFAULT_SUCK_DURATION;
    private long mBubbleDuration = DEFAULT_BUBBLE_DURATION;

    private ViewLocation mOriginLocation;
    private ViewLocation mRedPacketLocation;
    private ViewGroup mAnimationLayer;
    private ViewGroup mRootView;
    private Context mContext;
    private OnClickListener mDelegateOnClickListener;
    private float mLastMotionX;
    //满足滑动的最小位移
    private int mTouchSlop;
    private boolean mIsDragged;
    private AnimatorSet mSuckAnimaSet;
    private AnimatorSet mBubbleAnimaSet;
    private ViewLocation mBubbleLocation;
    private boolean mInflated4Suck;
    private View mOutsideCopyView;

    public RedPacketView(Context context) {
        this(context, null);
    }

    public RedPacketView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RedPacketView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }


    private void init() {
        final ViewConfiguration config = ViewConfiguration.get(getContext());
        mTouchSlop = config.getScaledPagingTouchSlop();
        super.setOnClickListener(new InnerClickLisener());
    }

    public void hideRedPacket() {
        if (animationPlaying || isHided()) {
            return;
        }
        animationPlaying = true;
        float left = getX();
        int width = getMeasuredWidth();
        if (left == 0) {
            return;
        }
        AnimatorSet animaSet = new AnimatorSet();
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, X, left, left + width / 2);
        ValueAnimator fadeAnim = ObjectAnimator.ofFloat(this, ALPHA, 1f, 0.3f);
        animaSet.setDuration(mFadeDuration);
        animaSet.play(animator).with(fadeAnim);
        animaSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animaSet.addListener(new AnimEndLisner());
        animaSet.start();
    }

    public void showRedPacket() {
        if (animationPlaying || isShowed()) {
            return;
        }
        animationPlaying = true;
        float left = getX();
        int width = getMeasuredWidth();
        if (left == 0) {
            return;
        }
        AnimatorSet animaSet = new AnimatorSet();
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, X, left, left - width / 2);
        ValueAnimator fadeAnim = ObjectAnimator.ofFloat(this, ALPHA, 0.4f, 1f);
        animaSet.setDuration(mFadeDuration);
        animaSet.play(animator).with(fadeAnim);
        animaSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animaSet.addListener(new AnimEndLisner());
        animaSet.start();
    }


    public boolean isHided() {
        return Math.abs(getLeft() - getX() + getWidth() / 2) <= RELATIVE_ERROR;
    }

    public boolean isShowed() {
        return Math.abs(getLeft() - getX()) <= RELATIVE_ERROR;
    }


    public void setDuration(long duration) {
        mFadeDuration = duration;
    }

    public void setSuckDuration(long suckDuration) {
        this.mSuckDuration = suckDuration;
    }

    public void setBubleDuration(long bubbleDuration) {
        this.mBubbleDuration = bubbleDuration;
    }

    /**
     * if you want link to recyclerView ,must inject recylerView first
     *
     * @param recyclerView be injected view
     */
    public void setRecyclerView(RecyclerView recyclerView) {
        new RedPacketRecyclerViewDecorator(recyclerView);
    }

    public void setOnClickListener(OnClickListener l) {
        mDelegateOnClickListener = l;
    }


    /**
     * suck up view
     *
     * @param suckedView sucked view
     */
    public AnimatorSet suck(View suckedView) {
        if (!suckedView.isShown() && !mInflated4Suck) {
            return null;
        }
        showRedPacket();
        initAnimationLayer();
        mOriginLocation = ViewLocation.getInScreenLocation(suckedView);
        mRedPacketLocation = ViewLocation.getInScreenLocation(this);
        View animaView = createAnimaViewInlayer(suckedView, mOriginLocation);
        AnimatorSet animaSet = getSuckAnimatorSet(animaView);
        animaSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimationLayer.removeAllViews();
                mAnimationLayer.setVisibility(GONE);
            }
        });
        return animaSet;
    }

    public void setSuckAnimatorSet(AnimatorSet suckAnimaSet) {
        mSuckAnimaSet = suckAnimaSet;
    }

    public void setBubbleAnimatorSet(AnimatorSet bubbleAnimaSet) {
        mBubbleAnimaSet = bubbleAnimaSet;
    }

    /**
     * suck up view
     *
     * @param suckedView              sucked view
     * @param animatorListenerAdapter suck Animator Listener
     */
    public void suck(View suckedView, AnimatorListenerAdapter animatorListenerAdapter) {
        AnimatorSet animaSet = suck(suckedView);
        if (animaSet == null) {
            return;
        }
        animaSet.addListener(animatorListenerAdapter);
    }

    /**
     * bubble view
     *
     * @param bubbleView
     * @return
     */
    public AnimatorSet bubble(View bubbleView) {
        showRedPacket();
        initAnimationLayer();
        bubbleView.setAlpha(0);
        setBublePosition(bubbleView, getBubbleLocation(), mAnimationLayer);
        AnimatorSet animaSet = getBubbleAnimatorSet(bubbleView);
        return animaSet;
    }


    /**
     * bubble view
     *
     * @param bubbleView
     * @param animatorListenerAdapter
     */
    public void bubble(View bubbleView, AnimatorListenerAdapter animatorListenerAdapter) {
        AnimatorSet animaSet = bubble(bubbleView);
        if (animaSet == null) {
            return;
        }
        animaSet.addListener(animatorListenerAdapter);
    }

    public void setBubbleLocation(ViewLocation bubbleLocation) {
        mBubbleLocation = bubbleLocation;
    }

    /**
     * use inflate xml to inflate no parent sucked view for support other view(ImageView..)
     *
     * @param outsideView
     */
    public void supportOtherView4Suck(View outsideView) {
        if (outsideView == null) {
            return;
        }
        mInflated4Suck = true;
        mOutsideCopyView = outsideView;
    }

    private ViewLocation getBubbleLocation() {
        if (mBubbleLocation == null) {
            mRedPacketLocation = ViewLocation.getInScreenLocation(this);
            //mRedPacketLocation.x在hide的时候位置会有问题,这里要用left
            mRedPacketLocation.x = getLeft();
            return mRedPacketLocation;
        }
        return mBubbleLocation;
    }

    private View getOutsideCopyView() {
        return mOutsideCopyView;
    }


    @NonNull
    private AnimatorSet getSuckAnimatorSet(View animaView) {
        if (mSuckAnimaSet != null) {
            return mSuckAnimaSet;
        }
        AnimatorSet animaSet = new AnimatorSet();
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(animaView, Y, mOriginLocation.y, mOriginLocation.y - 30);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(animaView, Y, mOriginLocation.y - 30, mRedPacketLocation.y);
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(animaView, SCALE_X, 1.0f, 0.1f);
        ObjectAnimator animator4 = ObjectAnimator.ofFloat(animaView, SCALE_Y, 1.0f, 0.1f);
        animaSet.play(animator1).before(animator2);
        animaSet.playTogether(animator2, animator3, animator4);
        animaSet.setDuration(mSuckDuration);
        animaSet.start();
        return animaSet;
    }

    @NonNull
    private AnimatorSet getBubbleAnimatorSet(View bubbleView) {
        if (mBubbleAnimaSet != null) {
            return mBubbleAnimaSet;
        }
        AnimatorSet animaSet = new AnimatorSet();
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(bubbleView, Y, mRedPacketLocation.y, mRedPacketLocation.y - 100);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(bubbleView, ALPHA, 0, 1);
        animaSet.playTogether(animator1, animator2);
        animaSet.setDuration(mBubbleDuration);
        animaSet.start();
        animaSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimationLayer.removeAllViews();
                mAnimationLayer.setVisibility(GONE);
            }
        });
        return animaSet;
    }


    private void initAnimationLayer() {
        if (mAnimationLayer == null) {
            mAnimationLayer = createAnimaitionLayer();
        }
        mAnimationLayer.removeAllViews();
        mAnimationLayer.setVisibility(VISIBLE);
    }

    private ViewGroup createAnimaitionLayer() {
        mRootView = (ViewGroup) getActivity(mContext).getWindow().getDecorView();
        LinearLayout animLayer = new LinearLayout(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        animLayer.setLayoutParams(layoutParams);
        animLayer.setBackgroundResource(android.R.color.transparent);
        mRootView.addView(animLayer);
        return animLayer;
    }

    private View createAnimaViewInlayer(final View orgView, final ViewLocation location) {
        orgView.setVisibility(INVISIBLE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(orgView.getMeasuredWidth(), orgView.getMeasuredHeight());
        lp.leftMargin = location.x;
        lp.topMargin = location.y;
        View clonedView = mInflated4Suck ? getOutsideCopyView() : getCloneView(orgView);
        mAnimationLayer.addView(clonedView, lp);
        return clonedView;
    }

    private View getCloneView(View orgView) {
        if (orgView instanceof TextView) {
            View clonedView = cloneTextView((TextView) orgView);
            return clonedView;
        }
        //todo auto expand other clone instance
        return null;
    }

    private View cloneTextView(TextView orgTV) {
        TextView clonedTV = new TextView(mContext);
        clonedTV.setText(orgTV.getText() == null ? "" : orgTV.getText());
        clonedTV.setTextColor(orgTV.getTextColors());
        clonedTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, orgTV.getTextSize());
        clonedTV.setBackgroundDrawable(orgTV.getBackground());
        clonedTV.setPadding(orgTV.getPaddingLeft(), orgTV.getPaddingTop(), orgTV.getPaddingRight(), orgTV.getPaddingBottom());
        return clonedTV;
    }


    private Activity getActivity(Context ctx) {
        if (ctx == null) {
            return null;
        } else if (ctx instanceof Activity) {
            return (Activity) ctx;
        } else if (ctx instanceof ContextWrapper) {
            return getActivity(((ContextWrapper) ctx).getBaseContext());
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        float x;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                x = ev.getX();
                mLastMotionX = x;
                mIsDragged = false;
                break;

            case MotionEvent.ACTION_MOVE:
                x = ev.getX();
                final int xDiff = (int) (x - mLastMotionX);
                if (xDiff > mTouchSlop) {
                    mIsDragged = true;
                    hideRedPacket();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mIsDragged) {
                    return true;
                }
                break;

            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    public void setBublePosition(View v, ViewLocation viewLocation, ViewGroup root) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(width, height);
        lp.leftMargin = viewLocation.x + getMeasuredWidth() / 2 - v.getMeasuredWidth() / 2;
        lp.topMargin = viewLocation.y;
        root.addView(v, lp);
        root.requestLayout();
    }

    static class ViewLocation {
        public int x;
        public int y;

        public static ViewLocation getInScreenLocation(View v) {
            ViewLocation viewLocation = new ViewLocation();
            int[] location = new int[2];
            v.getLocationOnScreen(location);
            viewLocation.x = location[0];
            viewLocation.y = location[1];
            return viewLocation;
        }

    }


    class AnimEndLisner extends AnimatorListenerAdapter {

        @Override
        public void onAnimationEnd(Animator animation) {
            animationPlaying = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            animationPlaying = false;
        }

    }


    class RedPacketRecyclerViewDecorator {

        RecyclerView mRecyerView;

        public RedPacketRecyclerViewDecorator(RecyclerView recyclerView) {
            mRecyerView = recyclerView;
            initScroller();
        }

        private void initScroller() {
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    showRedPacket();
                }
            };
            mRecyerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    switch (newState) {
                        case RecyclerView.SCROLL_STATE_IDLE:
                            handler.postDelayed(runnable, DELAY_START_TIME);
                            break;
                        case RecyclerView.SCROLL_STATE_DRAGGING:
                            handler.removeCallbacks(runnable);
                            hideRedPacket();
                            break;
                        case RecyclerView.SCROLL_STATE_SETTLING:
                            break;
                        default:
                            break;
                    }

                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                }
            });
        }
    }


    class InnerClickLisener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (isHided()) {
                showRedPacket();
                return;
            }
            if (mDelegateOnClickListener != null) {
                mDelegateOnClickListener.onClick(v);
            }

        }
    }

}


