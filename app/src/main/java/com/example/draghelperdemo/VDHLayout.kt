package com.example.draghelperdemo

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.customview.widget.ViewDragHelper
import java.sql.DriverManager.println

class VDHLayout(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var mDragView: View? = null
    private var mAutoBackView: View? = null
    private var mEdgeTrackerView: View? = null
    private val mAutoBackOriginPos = Point()
    private lateinit var mDragger: ViewDragHelper

    init {
        mDragger = ViewDragHelper.create(this, 1f, object : ViewDragHelper
        .Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                return child == mDragView || child == mAutoBackView
            }

            override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
                return left
            }

            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
                return top
            }

            // 手指释放时回调
            override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
                // mAutoBackView手指释放时可以自动回去
                if (releasedChild == mAutoBackView) {
                    mDragger.settleCapturedViewAt(mAutoBackOriginPos.x, mAutoBackOriginPos.y)
                    invalidate()
                }
            }


            // 在边界拖动时回调
            override fun onEdgeDragStarted(edgeFlags: Int, pointerId: Int) {
                println("VDHLayout.onEdgeDragStarted -- $pointerId")
                mDragger.captureChildView(mEdgeTrackerView!!, pointerId)
            }

        })

        mDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_ALL)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        mAutoBackOriginPos.x = mAutoBackView?.left!!
        mAutoBackOriginPos.y = mAutoBackView?.right!!
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mDragView = getChildAt(0)
        mAutoBackView = getChildAt(1)
        mEdgeTrackerView = getChildAt(2)
    }

    override fun computeScroll() {
        if (mDragger.continueSettling(true)) {
            invalidate()
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return mDragger.shouldInterceptTouchEvent(ev!!)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mDragger.processTouchEvent(event!!)
        return true
    }
}