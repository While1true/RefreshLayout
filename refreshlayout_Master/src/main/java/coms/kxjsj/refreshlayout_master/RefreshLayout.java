package coms.kxjsj.refreshlayout_master;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import static java.lang.Math.signum;


/**
 * Created by vange on 2017/12/14.
 */

public class RefreshLayout extends FrameLayout implements NestedScrollingParent, ValueAnimator.AnimatorUpdateListener {
    public static final String TAG = "RefreshLayout";
    private NestedScrollingParentHelper helper;

    View mHeader, mFooter, mScroll;

    Orentation orentation = Orentation.HORIZONTAL;

    int scrolls = 0;

    int mMaxScroll = 120, mRefreshPosition = 60, mFlingmax;

    State state = State.IDEL;
    private ValueAnimator valueAnimator;
    private AttrsUtils attrsUtils;

    enum Orentation {
        HORIZONTAL, VERTICAL
    }

    public enum State {
        REFRESHING, LOADING, PULL_HEADER, PULL_FOOTER, IDEL
    }

    public RefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public RefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        attrsUtils = new AttrsUtils();
        attrsUtils.ParseAttrs(context, attrs);

        initView(context);
    }

    private void initView(Context context) {
        helper = new NestedScrollingParentHelper(this);
        ViewCompat.setNestedScrollingEnabled(this, true);
        LayoutInflater inflater = LayoutInflater.from(context);
        mScroll = inflater.inflate(attrsUtils.getScrollLayoutId(), this, false);
        addView(mScroll);
        if (!attrsUtils.isOVERSCROLL()) {
            if (attrsUtils.isCANHEADER()) {
                mHeader = inflater.inflate(attrsUtils.getHeaderLayoutid(), this, false);
                addView(mHeader);
            }
            if (attrsUtils.isCANFOOTR()) {
                mFooter = inflater.inflate(attrsUtils.getFooterLayoutid(), this, false);
                addView(mFooter);
            }
        }

        initAnimator();

    }

    private void initAnimator() {
        valueAnimator = ValueAnimator.ofInt();
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(this);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.i(TAG, "onLayout: " + changed);
        if (changed) {
            return;
        }
        FrameLayout.LayoutParams layoutParams = (LayoutParams) mScroll.getLayoutParams();
        mScroll.layout(left, top, right, bottom);
        if (orentation == Orentation.VERTICAL) {
            if (mHeader != null) {
                mHeader.layout(left, top - mHeader.getMeasuredHeight() - layoutParams.topMargin, right, top - layoutParams.topMargin);
            }
            if (mFooter != null) {
                mFooter.layout(left, bottom + layoutParams.bottomMargin, right, bottom + mFooter.getMeasuredHeight() + layoutParams.bottomMargin);
            }
        } else {
            if (mHeader != null) {
                mHeader.layout(left - mHeader.getMeasuredWidth() - layoutParams.leftMargin, top, left - layoutParams.leftMargin, bottom);
            }
            if (mFooter != null) {
                mFooter.layout(right + layoutParams.rightMargin, top, right + mFooter.getMeasuredWidth() + layoutParams.rightMargin, bottom);
            }
        }


    }

    private void aninatorTo(int from, int to) {
        if (from == to) {
            return;
        }
        valueAnimator.setIntValues(from, to);
        valueAnimator.setDuration(250 + 150 * Math.abs(from - to) / mMaxScroll);
        valueAnimator.start();
    }

    public void NotifyCompleteRefresh() {
        aninatorTo(scrolls, 0);
    }

    public void setRefreshing() {
        if (state != State.REFRESHING || state != State.LOADING) {
            state = State.REFRESHING;
            aninatorTo(scrolls, -mRefreshPosition);
        }

    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float animatedFraction = animation.getAnimatedFraction();
        int animatedValue = (int) animation.getAnimatedValue();
        scrolls = animatedValue;
        doScroll(orentation == Orentation.VERTICAL);

        callbackScroll(state, animatedValue);

        if (animatedFraction == 1) {
            if (animatedValue != 0) {
                if (signum(animatedValue) > 0) {
                    state = State.LOADING;
                } else {
                    state = State.REFRESHING;
                }
            } else {
                state = State.IDEL;
            }
            callbackState(state);
        }
    }

    private void callbackScroll(State state, int value) {
        if (callback != null) {
            callback.call(state, value);
        }
    }

    private void callbackState(State state) {
        if (callback != null) {
            callback.call(state);
        }
    }

    @Override
    public void onStopNestedScroll(@NonNull View target) {
        helper.onStopNestedScroll(target);

        if (scrolls != 0 && (state != State.REFRESHING && state != State.LOADING)) {
            changeState(scrolls);
            if (Math.abs(scrolls) >= mRefreshPosition && !attrsUtils.isOVERSCROLL()) {
                aninatorTo(scrolls, (int) signum(scrolls) * mRefreshPosition);
            } else {
                aninatorTo(scrolls, 0);
            }
        }
    }

    private void changeState(int scrolls) {
        State statex = scrolls > 0 ? State.PULL_FOOTER : State.PULL_HEADER;
        if (statex != state) {
            callbackState(statex);
        }
        this.state = statex;
    }

    private void checkBounds() {
        int maxheader = 0;
        int maxfooter = 0;
        if (attrsUtils.isOVERSCROLL()) {
            maxheader = mMaxScroll;
            maxfooter = mMaxScroll;
        } else {
            maxheader = attrsUtils.isCANHEADER() ? mMaxScroll : 0;
            maxfooter = attrsUtils.isCANFOOTR() ? mMaxScroll : 0;
        }
        if (scrolls < -maxheader) {
            scrolls = -maxheader;
        }
        if (scrolls > maxfooter) {
            scrolls = maxfooter;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        System.out.println(dxConsumed);
        if (state == State.REFRESHING || state == State.LOADING) {
            return;
        } else if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }


        boolean isvertical = orentation == Orentation.VERTICAL;
        int dscroll = isvertical ? dyUnconsumed : dxUnconsumed;

        if ((dscroll < 0 && !canScroll(isvertical, -1)) || (dscroll > 0 && !canScroll(isvertical, 1))) {
            int tempscrolls = scrolls;
            scrolls += dscroll;
            System.out.println("onNestedScroll" + scrolls);
            checkBounds();
            doScroll(isvertical);
            changeState(tempscrolls);
            callbackScroll(state, scrolls);
        }
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @Nullable int[] consumed) {
        if (state == State.REFRESHING || state == State.LOADING) {
            return;
        }
        boolean isvertical = orentation == Orentation.VERTICAL;
        int dscroll = isvertical ? dy - consumed[1] : dx - consumed[0];
        if ((dscroll > 0 && scrolls < 0) || (dscroll < 0 && scrolls > 0)) {
            System.out.println("onNestedPreScroll" + scrolls);
            int scrolltemp = scrolls;
            scrolls += dscroll;
//            checkBounds();
            if (isvertical) {
                consumed[1] = scrolls - scrolltemp;
            } else {
                consumed[0] = scrolls - scrolltemp;
            }
            doScroll(isvertical);
            changeState(scrolltemp);
            callbackScroll(state, scrolls);
        }
    }

    private void doScroll(boolean isvertical) {
        if (isvertical) {
            scrollTo(0, scrolls);
        } else {
            scrollTo(scrolls, 0);
        }
    }

    private boolean canScroll(boolean isvertical, int direction) {
        if (isvertical) {
            return mScroll.canScrollVertically(direction);
        } else {
            return mScroll.canScrollHorizontally(direction);
        }
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes) {
        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        helper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return super.onNestedPreFling(target, velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return super.onNestedFling(target, velocityX, velocityY, consumed);
    }

    @Override
    public int getNestedScrollAxes() {
        return helper.getNestedScrollAxes();
    }


    Callback1<State> callback;

    public void setListener(Callback1<State> callback) {
        this.callback = callback;
    }


    public abstract static class Callback1<T> {
        public void call(T t) {
        }

        public void call(T t, int scroll) {
        }
    }

    /**
     * 初始化全局配置
     * @param defaultBuilder
     */
    public static void init(DefaultBuilder defaultBuilder) {
        AttrsUtils.setBuilder(defaultBuilder);
    }

    /**
     * 解析xml属性
     */
    public static class AttrsUtils {
        private int HEADER_LAYOUTID, SCROLL_LAYOUT_ID, FOOTER_LAYOUTID;
        private boolean CANHEADER = false, CANFOOTR = false, OVERSCROLL = false;
        private static DefaultBuilder builder = new DefaultBuilder();

        private static void setBuilder(DefaultBuilder builderx) {
            builder = builderx;
        }

        public void ParseAttrs(Context context, AttributeSet attr) {
            TypedArray typedArray = context.obtainStyledAttributes(attr, R.styleable.RefreshLayout);
            HEADER_LAYOUTID = typedArray.getResourceId(R.styleable.RefreshLayout_headerID, builder.HEADER_LAYOUTID_DEFAULT);

            FOOTER_LAYOUTID = typedArray.getResourceId(R.styleable.RefreshLayout_footerID, builder.FOOTER_LAYOUTID_DEFAULT);


            SCROLL_LAYOUT_ID = typedArray.getResourceId(R.styleable.RefreshLayout_scrollID, builder.SCROLL_LAYOUT_ID_DEFAULT);

            CANHEADER = typedArray.getBoolean(R.styleable.RefreshLayout_canHeader, builder.CANHEADER_DEFAULT);

            CANFOOTR = typedArray.getBoolean(R.styleable.RefreshLayout_canFooter, builder.CANFOOTR_DEFAULT);

            OVERSCROLL = typedArray.getBoolean(R.styleable.RefreshLayout_overscroll, builder.OVERSCROLL_DEFAULT);

            typedArray.recycle();
        }

        public int getHeaderLayoutid() {
            return HEADER_LAYOUTID;
        }

        public int getScrollLayoutId() {
            return SCROLL_LAYOUT_ID;
        }

        public int getFooterLayoutid() {
            return FOOTER_LAYOUTID;
        }

        public boolean isCANHEADER() {
            return CANHEADER;
        }

        public boolean isCANFOOTR() {
            return CANFOOTR;
        }

        public boolean isOVERSCROLL() {
            return OVERSCROLL;
        }

    }

    /**
     * 保存全局默认配置
     */
    public static class DefaultBuilder {
        private  int HEADER_LAYOUTID_DEFAULT, SCROLL_LAYOUT_ID_DEFAULT, FOOTER_LAYOUTID_DEFAULT;
        private  boolean CANHEADER_DEFAULT = true, CANFOOTR_DEFAULT = false, OVERSCROLL_DEFAULT = false;

        public DefaultBuilder setHeaderLayoutidDefault(int headerLayoutidDefault) {
            HEADER_LAYOUTID_DEFAULT = headerLayoutidDefault;
            return this;
        }

        public DefaultBuilder setScrollLayoutIdDefault(int scrollLayoutIdDefault) {
            SCROLL_LAYOUT_ID_DEFAULT = scrollLayoutIdDefault;
            return this;
        }

        public DefaultBuilder setFooterLayoutidDefault(int footerLayoutidDefault) {
            FOOTER_LAYOUTID_DEFAULT = footerLayoutidDefault;
            return this;
        }

        public DefaultBuilder setCanheaderDefault(boolean canheaderDefault) {
            CANHEADER_DEFAULT = canheaderDefault;
            return this;
        }

        public DefaultBuilder setCanfootrDefault(boolean canfootrDefault) {
            CANFOOTR_DEFAULT = canfootrDefault;
            return this;
        }

        public DefaultBuilder setOverscrollDefault(boolean overscrollDefault) {
            OVERSCROLL_DEFAULT = overscrollDefault;
            return this;
        }
    }


}
