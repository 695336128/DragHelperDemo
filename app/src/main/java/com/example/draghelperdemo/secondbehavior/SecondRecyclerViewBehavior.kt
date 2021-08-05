package com.example.draghelperdemo.secondbehavior

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.draghelperdemo.R
import com.example.draghelperdemo.source.HeaderScrollingViewBehavior

class SecondRecyclerViewBehavior(context: Context?, attrs: AttributeSet?) :
    HeaderScrollingViewBehavior(context, attrs) {

    private var mContext: Context? = null
    init {
        mContext = context
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        return isDependOn(dependency)
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        // header_transY / header_偏移量 * header_高度
        val commentScrollY = dependency.translationY / getHeaderOffset() * dependency.height
        val y = dependency.height - commentScrollY
        child.y = y
        return true
    }

    override fun getScrollRange(v: View): Int {
        return if (isDependOn(v)) {
            0.coerceAtLeast(v.measuredHeight)
        } else {
            super.getScrollRange(v)
        }
    }

    private fun getHeaderOffset(): Int {
        return  mContext?.resources?.getDimensionPixelOffset(R.dimen.header_offset) ?: 0
    }

    override fun findFirstDependency(views: MutableList<View>?): View? {
        return views?.firstOrNull { isDependOn(it) }
    }

    private fun isDependOn(dependency: View?): Boolean {
        return dependency?.id == R.id.header
    }
}