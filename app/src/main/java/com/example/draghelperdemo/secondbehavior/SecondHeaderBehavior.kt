package com.example.draghelperdemo.secondbehavior

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.draghelperdemo.R
import com.example.draghelperdemo.source.ViewOffsetBehavior
import com.example.draghelperdemo.view.NestedLinearLayout
import java.lang.ref.WeakReference

class SecondHeaderBehavior(context: Context?, attrs: AttributeSet?) : ViewOffsetBehavior<View>(
    context,
    attrs
) {

    private val STATE_OPENED = 0
    private val STATE_CLOSED = 1
    private val DEFAULT_ANIMATE_DURATION = 400


    private var mContext: Context? = null

    // 列表上完全出现的Item位置
    private var lastPosition = -1

    // 界面整体向上滑动, 达到列表可滑动的临界点
    private var upReach = false

    // 列表向上滑动后，在向下滑动，达到界面整体可滑动的临界点
    private var downReach = false

    // 实现弹性滑动、惯性滑动的辅助类
    private var mOverScroller: OverScroller

    // CoordinatorLayout
    private var mParent: WeakReference<CoordinatorLayout>? = null

    // CoordinatorLayout的子View，即header
    private var mChild: WeakReference<View>? = null

    // 当前Header的状态
    private var mCurState = STATE_OPENED

    // 惯性滑动的Runnable
    private var mFlingRunnable: FlingRunnable? = null

    // 是否消费掉Fling事件。Header关闭后滑动列表的Fling事件交给列表处理。一旦触发下拉展开时需要Header消费掉Fling事件
    private var mCanConsumeFling = false

    init {
        mContext = context
        mOverScroller = OverScroller(context)
    }

    override fun layoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int) {
        super.layoutChild(parent, child, layoutDirection)
        mParent = WeakReference(parent)
        mChild = WeakReference(child)
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: View,
        ev: MotionEvent
    ): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downReach = false
                upReach = false
            }
        }
        return super.onInterceptTouchEvent(parent, child, ev)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL // 只拦截纵向的事件
    }

    override fun onNestedPreFling(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.d("Zhang", "onNestedPreFling --- " + velocityY)
        return mCanConsumeFling // 拦截掉Fling事件，防止关闭时快速下拉列表导致Header的收起和List的展开Fling同时作用
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        type: Int
    ) {
        if (type == ViewCompat.TYPE_TOUCH) {
            reboundAnim(child)
        }
        Log.d("Zhang", "onStopNestedScroll: $type")

        super.onStopNestedScroll(coordinatorLayout, child, target, type)
    }

    override fun onNestedFling(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        Log.d("Zhang", "onNestedFling: ")
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
    }


    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int,
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        // 制造滑动视差，使header的移动比手指滑动慢, 4 为视差系数
        val scrollY = dy / 4.0F
        if (target is NestedLinearLayout) {
            // 处理Header的滑动
            var finalY = child.translationY - scrollY
            if (finalY < getHeaderOffset()) {
                finalY = getHeaderOffset().toFloat()
            } else if (finalY > 0) {
                finalY = 0F
            }
            child.translationY = finalY
            consumed[1] = dy
        } else if (target is RecyclerView) {
            // 处理列表的滑动
            val pos =
                (target.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
            // Header closed状态下，列表上滑再下滑到第一个Item全部显示，此时不让CoordinatorLayout整体下滑
            // 手指重新抬起再下滑才可以整体滑动
            if (pos == 0 && pos < lastPosition) {
                downReach = true    // 下滑到第一个Item全部显示，则到达列表下滑的临界值
                upReach = false
                Log.d("Zhang", "Reach ---- : downReach = true , upReach = false")
            }
            // 列表首个Item全部可见 && 整体可滑动 -> CoordinatorLayout消费掉事件
            if (pos == 0 && canScroll(child, scrollY)) {
                var finalY = child.translationY - scrollY
                // header 已经 closed -> 到达列表上滑的临界点
                if (finalY < getHeaderOffset()) {
                    finalY = getHeaderOffset().toFloat()
                    upReach = true
                    downReach = false
                    Log.d("Zhang", "Reach ---- : downReach = false , upReach = true")
                } else if (finalY > 0) {
                    // header 已经 opened,整体不能继续下滑
                    finalY = 0F
                }
                child.translationY = finalY
                consumed[1] = dy //CoordinatorLayout消费掉事件，实现整体滑动
                mCanConsumeFling = true
            }
            lastPosition = pos
        }
    }

    /**
     * 是否可以整体滑动
     */
    private fun canScroll(child: View, scrollY: Float): Boolean {
        // 上滑 && Header未到顶点
        if (scrollY > 0 && child.translationY > getHeaderOffset()) {
            return true
        }
        // Header到达顶点 && 列表可向上滑动
        if (child.translationY.toInt() == getHeaderOffset() && upReach) {
            return false
        }

        if (scrollY < 0 && downReach) {
            return true
        }

        // 下滑 && 列表可向下滑动
        if (scrollY < 0 && !downReach) {
            return true
        }
        return false
    }

    /**
     * 回弹打开或关闭
     */
    private fun reboundAnim(child: View) {
        Log.d("Zhang", "handleActionUp: " + isClosed())
        if (mFlingRunnable != null) {
            child.removeCallbacks(mFlingRunnable)
            mFlingRunnable = null
        }
        if (isClosed()) {
            // 当前是关闭状态，以顶部阈值为回弹的界限
            if (child.translationY.toInt() in getHeaderOffset()..(getHeaderOffset() * 2 / 3)) {
                scrollToClose()
            } else {
                scrollToOpen()
            }
        } else {
            // 开启状态，以底部的阈值为界限
            if (child.translationY.toInt() in (getHeaderOffset() * 1 / 3)..0) {
                scrollToOpen()
            } else {
                scrollToClose()
            }
        }
    }

    /**
     * 惯性打开Header
     */
    private fun scrollToOpen() {
        Log.d("Zhang", "scrollToOpen: " + mChild?.get()?.translationY?.toInt())
        val curTranslationY = mChild?.get()?.translationY?.toInt()!!
        mOverScroller.startScroll(0, curTranslationY, 0, -curTranslationY, DEFAULT_ANIMATE_DURATION)
        start()
    }

    /**
     * 惯性收起Header
     */
    private fun scrollToClose() {
        Log.d("Zhang", "scrollToClose: " + mChild?.get()?.translationY?.toInt()!!)
        val curTranslationY = mChild?.get()?.translationY?.toInt()!!
        val dy = getHeaderOffset() - curTranslationY
        mOverScroller.startScroll(0, curTranslationY, 0, dy, DEFAULT_ANIMATE_DURATION)
        start()
    }

    /**
     * 开始执行惯性滑动的动画
     */
    private fun start() {
        if (mOverScroller.computeScrollOffset()) {
            mFlingRunnable =
                FlingRunnable(mChild!!.get()!!)
            ViewCompat.postOnAnimation(mChild!!.get()!!, mFlingRunnable)
        } else {
            onFlingFinished(mChild!!.get()!!)
        }
    }

    private fun onFlingFinished(child: View) {
        if (child.translationY == getHeaderOffset().toFloat()) {  // 处于关闭状态
            changeState(STATE_CLOSED)
            mCanConsumeFling = false
        } else {
            changeState(STATE_OPENED)
        }
    }

    private fun changeState(newState: Int) {
        mCurState = newState
        Log.d("Zhang", "changeState: $mCurState")
    }

    /**
     * Header是否收起
     */
    private fun isClosed(): Boolean {
        return mCurState == STATE_CLOSED
    }

    private fun getHeaderOffset(): Int {
        return mContext?.resources?.getDimensionPixelOffset(R.dimen.header_offset) ?: 0
    }

    /**
     * 自动收起/展开的动画。监听mOverScroller并刷新View
     */
    private inner class FlingRunnable(val mLayout: View) : Runnable {
        override fun run() {
            if (mOverScroller.computeScrollOffset()) {
                mLayout.translationY = mOverScroller.currY.toFloat()
                ViewCompat.postOnAnimation(mLayout, this)
            } else {
                onFlingFinished(mLayout)
            }
        }

    }
}