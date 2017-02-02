package tmnt.example.slidviewdemo;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * 滑动控件
 * Created by tmnt on 2017/2/2.
 */
public class SlideView extends LinearLayout {

    private static final String TAG = "SlideView";

    private ViewDragHelper mViewDragHelper;

    private ViewGroup mContent, mSlide;

    /**
     * 滑动距离
     */
    private int mViewDragRange;

    /**
     * 左滑最大距离
     */
    private int mBehindLayoutWidth;

    /**
     * 宽度
     */
    private int mContentLayoutWidth;

    private static ViewDragListener mViewDragListener;

    private boolean isOpen;

    public interface ViewDragListener {
        void onOpen();

        void onClose();

        void onDrag(float slide);
    }

    public SlideView(Context context) {
        super(context);
        //Log.i(TAG, "SlideView: start");
        init();
    }

    public SlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //Log.i(TAG, "SlideView: start");
        init();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        //判读是否移动到指定位置 返回true需要重绘
        if (mViewDragHelper.continueSettling(true)) {
            android.support.v4.view.ViewCompat.postInvalidateOnAnimation(this);//this为当前ViewGroup,开启重绘
        }
    }

    public void init() {
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, new DragCallback());
    }


    @Override
    protected void onFinishInflate() {
        mContent = (ViewGroup) getChildAt(0);
        mSlide = (ViewGroup) getChildAt(1);
        mContent.setClickable(true);
        mSlide.setClickable(true);
        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mContentLayoutWidth = mContent.getMeasuredWidth();
        mBehindLayoutWidth = mSlide.getMeasuredWidth();
        //Log.i(TAG, "onMeasure: " + mBehindLayoutWidth);
    }

    public void setViewDragListener(ViewDragListener mViewDragListener) {
        this.mViewDragListener = mViewDragListener;
    }


    public class DragCallback extends ViewDragHelper.Callback {

        /**
         * 指定可拖动的子View  child=指定view
         *
         * @param child     指定view
         * @param pointerId
         * @return
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mContent;
            //return true;
        }

        /**
         * 在水平方向上拖动
         *
         * @param child 指定view
         * @param left  水平方向上移动的距离 范围不能超过layout
         * @param dx    较前一次的增量
         * @return 默认返回0 即不移动
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            // Log.i(TAG, "clampViewPositionHorizontal: "+left);
            if (child == mContent) {
                int newLeft = Math.min(
                        Math.max((-getPaddingLeft() - mBehindLayoutWidth), left), 0);
                //Log.i(TAG, "clampViewPositionHorizontal: "+newLeft);
                return newLeft;
            } else {
                return 0;
            }
        }


        /**
         * 限定动画执行速度
         *
         * @param child
         * @return
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return mContentLayoutWidth;
        }

        /**
         * 拖动后的状态
         *
         * @param releasedChild
         * @param xvel          水平方向的速度  向右为正
         * @param yvel          操作方向的速度  向下为正
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (releasedChild == mContent) {
                //Log.i(TAG, "onViewReleased: " + mViewDragRange);
                if (xvel <= 0) {//向左滑动
                    if (-mViewDragRange >= mBehindLayoutWidth / 2
                            && -mViewDragRange <= mBehindLayoutWidth) {
                        open();  //到达最终状态
                    } else {
                        close();
                    }
                } else {//向右滑动
                    if (-mViewDragRange >= 0
                            && -mViewDragRange <= mBehindLayoutWidth) {
                        close();
                    } else {
                        open();
                    }
                }
            }
        }

        /**
         * 位置改变时回调
         *
         * @param changedView
         * @param left
         * @param top
         * @param dx
         * @param dy
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mViewDragRange = left;
            //Log.i(TAG, "onViewPositionChanged: " + left);
            float percent = Math.abs((float) left / (float) mContentLayoutWidth);
//            if (null != mViewDragListener) {
//                mViewDragListener.onDrag(percent);
//            }
            if (changedView == mContent) {
                mSlide.offsetLeftAndRight(dx);//子view靠此方法进行拖动

            } else {
                mContent.offsetLeftAndRight(dx);
            }
            invalidate();
        }
    }


    /**
     * 如果未到达位置 则进行动画过渡到此位置
     */
    public void open() {

        if (mViewDragHelper.smoothSlideViewTo(
                mContent, -mBehindLayoutWidth, 0)) {
            android.support.v4.view.ViewCompat.postInvalidateOnAnimation(this);
        }
        if (null != mViewDragListener)
            mViewDragListener.onOpen();
        isOpen = true;
    }

    /**
     * 关闭
     */
    public void close() {
        if (mViewDragHelper.smoothSlideViewTo(mContent, 0, 0)) {
            android.support.v4.view.ViewCompat.postInvalidateOnAnimation(this);
        }
        if (null != mViewDragListener)
            mViewDragListener.onClose();
        isOpen = false;
    }

    public boolean isOpen() {
        return isOpen;
    }


}
