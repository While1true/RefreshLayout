package coms.kxjsj.refreshlayout_master;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
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

    private View mHeader, mFooter, mScroll;

    /**
     * 滑动方向
     */
    private Orentation orentation;

    /**
     * 滑动值
     */
    private int scrolls = 0;

    /**
     * 头部最大滑动距离
     */
    private int mMaxHeaderScroll = -1;

    /**
     * 尾部最大刷新距离
     */
    private int mMaxFooterScroll = -1;

    /**
     * 头部刷新停留的位置
     */
    private int mHeaderRefreshPosition = -1;

    /**
     * 尾部刷新停留的位置
     */
    private int mFooterRefreshPosition = -1;

    /**
     * 快速滑动Overscroll的距离
     */
    private int mFlingmax;

    /**
     * 刷新完成停留的位置
     */
    private int mHeaderRefreshCompletePosition = -1;
    private int mFooterLoadingCompletePosition = -1;

    /**
     * 刷新状态
     */
    State state = State.IDEL;

    private ValueAnimator valueAnimator;

    /**
     * 属性解析 保存类
     */
    private AttrsUtils attrsUtils;
    private int delayMillis = -1;

    /**
     * 方向
     */
    public enum Orentation {
        HORIZONTAL, VERTICAL
    }

    /**
     * 刷新状态
     * 正在刷新
     * 正在加载
     * 拉头部
     * 拉尾部
     * 闲置
     * 刷新完成位置
     * 加载完成位置
     */
    public enum State {
        REFRESHING, LOADING, PULL_HEADER, PULL_FOOTER, IDEL, REFRESHCOMPLETE, LOADINGCOMPLETE
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
        if (orentation == null) {
            orentation = attrsUtils.orentation;
        }
        if (mHeaderRefreshCompletePosition == -1) {
            mHeaderRefreshCompletePosition = attrsUtils.mHeaderRefreshCompletePosition;
        }
        if (mFooterLoadingCompletePosition == -1) {
            mFooterLoadingCompletePosition = attrsUtils.mFooterLoadingCompletePosition;
        }
        if (delayMillis == -1) {
            delayMillis = attrsUtils.delayCompleteTime;
        }
        try {
            if (baseRefreshWrap == null) {
                baseRefreshWrap = (BaseRefreshWrap) AttrsUtils.builder.defaultRefreshWrap.newInstance();
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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
        Log.i(TAG, "onLayout: " + changed + "hash" + hashCode() + "Size:" + left + "-" + top + "-" + right + "-" + bottom);
        FrameLayout.LayoutParams layoutParams = (LayoutParams) mScroll.getLayoutParams();
        right = right - left;
        bottom = bottom - top;
        top = 0;
        left = 0;
        mScroll.layout(left + layoutParams.leftMargin, top + layoutParams.topMargin, right - layoutParams.rightMargin, bottom - layoutParams.bottomMargin);
        if (orentation == Orentation.VERTICAL) {
            if (attrsUtils.isOVERSCROLL()) {
                if (mMaxFooterScroll == -1) {
                    mMaxHeaderScroll = getMeasuredHeight() / 2;
                }
                if (mMaxFooterScroll == -1) {
                    mMaxFooterScroll = mMaxHeaderScroll;
                }
            } else {
                if (mHeader != null) {
                    FrameLayout.LayoutParams headerParams = (LayoutParams) mScroll.getLayoutParams();

                    if (attrsUtils.mFlingmax == -1) {
                        mFlingmax = mHeader.getMeasuredHeight() / 6;
                    }
                    if (attrsUtils.mMaxHeadertScroll == -1) {
                        mMaxHeaderScroll = 3 * mHeader.getMeasuredHeight();
                    }
                    if (attrsUtils.mHeaderRefreshPosition == -1) {
                        mHeaderRefreshPosition = mHeader.getMeasuredHeight() + headerParams.bottomMargin;
                    }

                    mHeader.layout(left + headerParams.leftMargin, top - mHeader.getMeasuredHeight() - headerParams.bottomMargin, right - headerParams.rightMargin, top - headerParams.bottomMargin);
                }
                if (mFooter != null) {
                    FrameLayout.LayoutParams footerParams = (LayoutParams) mScroll.getLayoutParams();

                    if (attrsUtils.mMaxFooterScroll == -1) {
                        mMaxFooterScroll = (int) (mFooter.getMeasuredHeight() * 1.5f);
                    }
                    if (attrsUtils.mFooterRefreshPosition == -1) {
                        mFooterRefreshPosition = mFooter.getMeasuredHeight() + footerParams.bottomMargin;
                    }
                    mFooter.layout(left + footerParams.leftMargin, bottom + footerParams.topMargin, right - footerParams.rightMargin, bottom + mFooter.getMeasuredHeight() + footerParams.topMargin);
                }
            }

        } else {
            if (attrsUtils.isOVERSCROLL()) {
                if (mMaxFooterScroll == -1) {
                    mMaxHeaderScroll = getMeasuredWidth() / 2;
                }
                if (mMaxFooterScroll == -1) {
                    mMaxFooterScroll = mMaxHeaderScroll;
                }
            } else {
                if (mHeader != null) {
                    FrameLayout.LayoutParams headerParams = (LayoutParams) mScroll.getLayoutParams();

                    if (attrsUtils.mFlingmax == -1) {
                        mFlingmax = mHeader.getMeasuredWidth() / 6;
                    }
                    if (attrsUtils.mMaxHeadertScroll == -1) {
                        mMaxHeaderScroll = 3 * mHeader.getMeasuredWidth();
                    }
                    if (attrsUtils.mHeaderRefreshPosition == -1) {
                        mHeaderRefreshPosition = mHeader.getMeasuredWidth() + headerParams.leftMargin;
                    }
                    mHeader.layout(left - mHeader.getMeasuredWidth() - headerParams.rightMargin, top + headerParams.topMargin, left - layoutParams.leftMargin - headerParams.rightMargin, bottom - headerParams.bottomMargin);
                }
                if (mFooter != null) {
                    FrameLayout.LayoutParams footerParams = (LayoutParams) mScroll.getLayoutParams();

                    if (attrsUtils.mMaxFooterScroll == -1) {
                        mMaxFooterScroll = (int) (mFooter.getMeasuredWidth() * 1.5f);
                    }
                    if (attrsUtils.mFooterRefreshPosition == -1) {
                        mFooterRefreshPosition = mFooter.getMeasuredWidth() + footerParams.leftMargin;
                    }
                    mFooter.layout(right + footerParams.leftMargin, top + footerParams.topMargin, right + mFooter.getMeasuredWidth() + footerParams.leftMargin, bottom - footerParams.bottomMargin);
                }
            }

        }
        baseRefreshWrap.initView(this);
    }

    private void aninatorTo(int from, int to) {
        if (from == to) {
            return;
        }
        valueAnimator.setIntValues(from, to);
        valueAnimator.setDuration(250 + 150 * Math.abs(from - to) / mMaxHeaderScroll);
        valueAnimator.start();
    }

    public void NotifyCompleteRefresh0() {
        state=scrolls<=0?State.REFRESHCOMPLETE:State.LOADINGCOMPLETE;
        callbackState(state);
        mHeader.postDelayed(new Runnable() {
            @Override
            public void run() {
                state = scrolls <= 0 ? State.PULL_HEADER : State.PULL_FOOTER;
                aninatorTo(scrolls, 0);
            }
        }, delayMillis);

    }

    /**
     * 刷新停留到配置位置，再归位
     */
    public void NotifyCompleteRefresh1(Object obj) {
        baseRefreshWrap.setData(obj);
        if (state == State.REFRESHING || state == State.LOADING) {
            state = state == State.REFRESHING ? State.REFRESHCOMPLETE : State.LOADINGCOMPLETE;
            int position = state == State.REFRESHING ? -mHeaderRefreshCompletePosition : mFooterRefreshPosition;
            if (position == 0) {
                callbackState(state);
            }
            aninatorTo(scrolls, position);
        } else {

            NotifyCompleteRefresh0();
        }
    }

    /**
     * 刷新停留到某个位置，再归位
     *
     * @param obj
     * @param position
     */
    public void NotifyCompleteRefresh1(int position, Object obj) {
        baseRefreshWrap.setData(obj);
        if (state == State.REFRESHING || state == State.LOADING) {
            state = state == State.REFRESHING ? State.REFRESHCOMPLETE : State.LOADINGCOMPLETE;
            if (position == 0) {
                callbackState(state);
            }
            aninatorTo(scrolls, state == State.REFRESHCOMPLETE ? -position : position);
        } else {
            NotifyCompleteRefresh0();
        }

    }

    /**
     * 设置动画到正在刷新
     */
    public void setRefreshing() {
        if (state != State.REFRESHING || state != State.LOADING) {
            state = State.REFRESHING;
            aninatorTo(scrolls, -mHeaderRefreshPosition);
        }

    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float animatedFraction = animation.getAnimatedFraction();
        int animatedValue = (int) animation.getAnimatedValue();
        scrolls = animatedValue;
        doScroll(orentation == Orentation.VERTICAL);
        /**
         * 刷新完成时
         * 状态已变成 State.REFRESHCOMPLETE State.LOADINGCOMPLETE
         */
        boolean isComplete = (state == State.REFRESHCOMPLETE) || (state == State.LOADINGCOMPLETE);
        if (isComplete) {
            callbackScroll(state == State.REFRESHCOMPLETE ? State.PULL_HEADER : State.PULL_FOOTER, animatedValue);
        } else {
            callbackScroll(state, animatedValue);
        }
        if (animatedFraction == 1) {
            if (animatedValue != 0) {
                if (!isComplete) {
                    if (animatedValue > 0) {
                        state = State.LOADING;
                    } else {
                        state = State.REFRESHING;
                    }
                } else {
                    NotifyCompleteRefresh0();
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
        if (state == State.PULL_HEADER) {
            baseRefreshWrap.onPullHeader(mHeader, -value);
        } else {
            baseRefreshWrap.onPullFooter(mFooter, value);
        }
    }

    private void callbackState(State state) {
        if (callback != null) {
            callback.call(state);
        }
        baseRefreshWrap.OnStateChange(state);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target) {
        helper.onStopNestedScroll(target);

        if (scrolls != 0 && (state != State.REFRESHING && state != State.LOADING)) {
            changeState(scrolls);
            int mRefreshPosition = scrolls > 0 ? mFooterRefreshPosition : mHeaderRefreshPosition;
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

    private void checkBounds(int scrolltemp) {
        int maxheader = 0;
        int maxfooter = 0;
        if (attrsUtils.isOVERSCROLL()) {
            maxheader = scrolltemp <= 0 ? mMaxHeaderScroll : 0;
            maxfooter = scrolltemp >= 0 ? mMaxHeaderScroll : 0;
        } else {
            maxheader = attrsUtils.isCANHEADER() && scrolltemp <= 0 ? mMaxHeaderScroll : 0;
            maxfooter = attrsUtils.isCANFOOTR() && scrolltemp >= 0 ? mMaxFooterScroll : 0;
        }

        if (scrolls < -maxheader) {
            scrolls = -maxheader;
        }
        if (scrolls > maxfooter) {
            scrolls = maxfooter;
        }
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
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
//            System.out.println("onNestedScroll" + scrolls);
            checkBounds(tempscrolls);
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
//            System.out.println("onNestedPreScroll" + scrolls);
            int scrolltemp = scrolls;
            scrolls += dscroll;
            checkBounds(scrolltemp);
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
        if (attrsUtils.isOVERSCROLL() && attrsUtils.EVALATEABLE) {
            return;
        }
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
        int ore = orentation == Orentation.VERTICAL ? ViewCompat.SCROLL_AXIS_VERTICAL : ViewCompat.SCROLL_AXIS_HORIZONTAL;
        return (axes & ore) != 0;
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
     *
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
        private boolean CANHEADER = false, CANFOOTR = false, OVERSCROLL = false, EVALATEABLE = false;

        private Orentation orentation = Orentation.VERTICAL;
        private int mMaxHeadertScroll = -1, mMaxFooterScroll = -1, mHeaderRefreshPosition = -1, mFooterRefreshPosition = -1, mFlingmax = -1, mHeaderRefreshCompletePosition, mFooterLoadingCompletePosition;
        private static DefaultBuilder builder = new DefaultBuilder();

        private int delayCompleteTime = 1000;

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

            EVALATEABLE = typedArray.getBoolean(R.styleable.RefreshLayout_evaluateable, builder.EVALATEABLE);

            OVERSCROLL = typedArray.getBoolean(R.styleable.RefreshLayout_overscroll, builder.OVERSCROLL_DEFAULT);

            int orentation = typedArray.getInt(R.styleable.RefreshLayout_orentation, 1);
            if (orentation == 0) {
                this.orentation = Orentation.HORIZONTAL;
            }
            mMaxHeadertScroll = (int) typedArray.getDimension(R.styleable.RefreshLayout_mMaxHeadertScroll, mMaxHeadertScroll);
            mMaxFooterScroll = (int) typedArray.getDimension(R.styleable.RefreshLayout_mMaxFooterScroll, mMaxFooterScroll);
            mHeaderRefreshPosition = (int) typedArray.getDimension(R.styleable.RefreshLayout_mHeaderRefreshPosition, mHeaderRefreshPosition);
            mFooterRefreshPosition = (int) typedArray.getDimension(R.styleable.RefreshLayout_mFooterRefreshPosition, mFooterRefreshPosition);
            mHeaderRefreshCompletePosition = (int) typedArray.getDimension(R.styleable.RefreshLayout_mHeaderRefreshCompletePosition, 0);
            mFooterLoadingCompletePosition = (int) typedArray.getDimension(R.styleable.RefreshLayout_mFooterLoadingCompletePosition, 0);
            mFlingmax = (int) typedArray.getDimension(R.styleable.RefreshLayout_mFlingmax, mFlingmax);
            delayCompleteTime = typedArray.getInt(R.styleable.RefreshLayout_delayCompleteTime, delayCompleteTime);


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

        public int getmMaxHeadertScroll() {
            return mMaxHeadertScroll;
        }

        public int getmMaxFooterScroll() {
            return mMaxFooterScroll;
        }

        public int getmHeaderRefreshPosition() {
            return mHeaderRefreshPosition;
        }

        public int getmFooterRefreshPosition() {
            return mFooterRefreshPosition;
        }

        public int getmFlingmax() {
            return mFlingmax;
        }

        public int getmHeaderRefreshCompletePosition() {
            return mHeaderRefreshCompletePosition;
        }

        public int getmFooterLoadingCompletePosition() {
            return mFooterLoadingCompletePosition;
        }
    }

    /**
     * 保存全局默认配置
     */
    public static class DefaultBuilder {
        private int HEADER_LAYOUTID_DEFAULT, SCROLL_LAYOUT_ID_DEFAULT, FOOTER_LAYOUTID_DEFAULT;
        private boolean CANHEADER_DEFAULT = true, CANFOOTR_DEFAULT = true, OVERSCROLL_DEFAULT = false, EVALATEABLE = false;
        private Class defaultRefreshWrap = BaseRefreshWrap.class;

        public DefaultBuilder setBaseRefreshWrap(Class defaultRefreshWrap) {
            this.defaultRefreshWrap = defaultRefreshWrap;
            return this;
        }

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


    private BaseRefreshWrap baseRefreshWrap;

    public void detRefreshWrap(BaseRefreshWrap baseRefreshWrap) {
        this.baseRefreshWrap = baseRefreshWrap;
    }

    public static abstract class BaseRefreshWrap<T> {
        protected T data;

        public abstract void onPullHeader(View view, int scrolls);

        public abstract void onPullFooter(View view, int scrolls);

        public abstract void OnStateChange(State state);

        protected void initView(RefreshLayout layout) {

        }

        protected void setData(Object data) {
            this.data = (T) data;
        }
    }

    public void setOrentation(Orentation orentation) {
        this.orentation = orentation;
    }

    public Orentation getOrentation() {
        return orentation;
    }

    public int getmMaxHeadertScroll() {
        return mMaxHeaderScroll;
    }

    public void setmMaxHeadertScroll(int mMaxHeadertScroll) {
        this.mMaxHeaderScroll = mMaxHeadertScroll;
    }

    public int getmMaxFooterScroll() {
        return mMaxFooterScroll;
    }

    public void setmMaxFooterScroll(int mMaxFooterScroll) {
        this.mMaxFooterScroll = mMaxFooterScroll;
    }

    public AttrsUtils getAttrsUtils() {
        return attrsUtils;
    }

    public void setAttrsUtils(AttrsUtils attrsUtils) {
        this.attrsUtils = attrsUtils;
    }

    public int getmHeaderRefreshPosition() {
        return mHeaderRefreshPosition;
    }

    public void setmHeaderRefreshPosition(int mHeaderRefreshPosition) {
        this.mHeaderRefreshPosition = mHeaderRefreshPosition;
    }

    public int getmFooterRefreshPosition() {
        return mFooterRefreshPosition;
    }

    public void setmFooterRefreshPosition(int mFooterRefreshPosition) {
        this.mFooterRefreshPosition = mFooterRefreshPosition;
    }

    public void setCanFooter(boolean canFooter) {
        attrsUtils.CANFOOTR = canFooter;
    }
    public void setCanHeader(boolean canHeader){
        attrsUtils.CANHEADER=canHeader;
    }

    public int getmFlingmax() {
        return mFlingmax;
    }

    public void setmFlingmax(int mFlingmax) {
        this.mFlingmax = mFlingmax;
    }

    public int getDelayMillis() {
        return delayMillis;
    }

    public void setDelayMillis(int delayMillis) {
        this.delayMillis = delayMillis;
    }

    public <T extends View> T getmHeader() {
        return (T) mHeader;
    }

    public <T extends View> T getmFooter() {
        return (T) mFooter;
    }

    public <T extends View> T getmScroll() {
        return (T) mScroll;
    }

    public <T extends View> T findInHeaderView(int id) {

        return mHeader.findViewById(id);
    }

    public <T extends View> T findInScrollView(int id) {

        return mScroll.findViewById(id);
    }

    public <T extends View> T findInFooterView(int id) {

        return mScroll.findViewById(id);
    }

    public <T extends BaseRefreshWrap> T getBaseRefreshWrap() {
        return (T) baseRefreshWrap;
    }
}
