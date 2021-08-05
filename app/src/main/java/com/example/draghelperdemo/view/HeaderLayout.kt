package com.example.draghelperdemo.view

import android.content.Context
import android.util.AttributeSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.draghelperdemo.secondbehavior.SecondHeaderBehavior
import com.google.android.material.appbar.AppBarLayout

class HeaderLayout: AppBarLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var mBehavior: SecondHeaderBehavior? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mBehavior = (layoutParams as CoordinatorLayout.LayoutParams).behavior as SecondHeaderBehavior
        if (mBehavior == null) {
            throw Exception("Behavior is null !!!!!!!")
        }
    }
}