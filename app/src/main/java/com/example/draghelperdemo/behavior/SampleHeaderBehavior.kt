package com.example.draghelperdemo.behavior

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SampleHeaderBehavior(context: Context?, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<View>(context, attrs) {

    // 界面整体向上滑动, 达到列表可滑动的临界点
    private var upReach = false

    // 列表向上滑动后，在向下滑动，达到界面整体可滑动的临界点
    private var downReach = false

    // 列表上一个全部可见的item位置
    private var lastPosition = -1

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout, child: View, ev: MotionEvent
    ): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downReach = false
                upReach = false
            }
            MotionEvent.ACTION_UP -> {
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
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        if (target is RecyclerView) {
            val pos = (target.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
            if (pos == 0 && pos < lastPosition) {
                downReach = true
            }
            // 整体可以滑动，否则RecyclerView消费滑动事件
            if (canScroll(child, dy) && pos == 0) {
                var finalY = child.translationY - dy
                if (finalY < -child.height) {
                    finalY = (-child.height).toFloat()
                    upReach = true
                } else if (finalY > 0) {
                    finalY = 0F
                }
                child.translationY = finalY
                consumed[1] = dy
            }
            lastPosition = pos
        }
    }

    private fun canScroll(child: View, scrollY: Int): Boolean {
        if (scrollY > 0 && child.translationY.toInt() == -child.height && !upReach) {
            return false
        }
        if (downReach) {
            return true
        }
        return true
    }
}