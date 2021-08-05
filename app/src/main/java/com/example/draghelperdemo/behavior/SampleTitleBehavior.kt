package com.example.draghelperdemo.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView

class SampleTitleBehavior(context: Context?, attrs: AttributeSet?)
  : CoordinatorLayout.Behavior<View>(context, attrs) {

  private var deltaY: Float = 0F

  override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
    return dependency is RecyclerView
  }

  override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
    if (deltaY == 0F) {
      deltaY = dependency.y - child.height
    }
    var dy = dependency.y - child.height
    dy = if (dy < 0F) 0F else dy
    val y = -(dy / deltaY) * child.height
    child.translationY = y

    child.alpha = 1 - (dy / deltaY)

    return true
  }

  override fun onStartNestedScroll(
    coordinatorLayout: CoordinatorLayout,
    child: View,
    directTargetChild: View,
    target: View,
    axes: Int,
    type: Int
  ): Boolean {
    return super.onStartNestedScroll(
      coordinatorLayout,
      child,
      directTargetChild,
      target,
      axes,
      type
    )
  }

}