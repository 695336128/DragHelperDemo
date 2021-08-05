package com.example.draghelperdemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingChildHelper;

/**
 * 作为 {@link NestedScrollingChild2} 提供嵌套滑动能力。借鉴 WPF 「冒泡事件」思想，将滑动事件冒泡传递至上层
 * View。通过 {@link BubbleScrollOrderSupplier} 获取滑动顺序，决定何时将滑动事件分发至上层 View。
 *
 * <pre>
 * NestedCoordinatorLayout layout = ...;
 * layout.setBubbleScrollOrderSupplier((layout, target, dx, dy) -> {
 *   // 根据当前滑动状态决定滑动事件的分发顺序
 * });
 * </pre>
 */
public class NestedCoordinatorLayout
    extends CoordinatorLayout
    implements NestedScrollingChild2 {

  /** 不冒泡分发滑动事件，仅自己消费 */
  public static final int BUBBLE_SCROLL_NONE = 0;
  /** 待上层 View 消费滑动之后自己再消费，自己的滑动后于上层 View */
  public static final int BUBBLE_SCROLL_BEFORE = 1;
  /** 待自己消费滑动之后再分发至上层 View，自己的滑动先于上层 View */
  public static final int BUBBLE_SCROLL_AFTER = 2;

  @IntDef({
      BUBBLE_SCROLL_NONE,
      BUBBLE_SCROLL_BEFORE,
      BUBBLE_SCROLL_AFTER})
  @interface BubbleScrollOrder {}

  public NestedCoordinatorLayout(Context context) {
    this(context, null);
  }

  public NestedCoordinatorLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public NestedCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    childHelper.setNestedScrollingEnabled(true);
  }

  private final NestedScrollingChildHelper childHelper = new NestedScrollingChildHelper(this);
  private BubbleScrollOrderSupplier bubbleScrollOrderSupplier;

  public void setBubbleScrollOrderSupplier(BubbleScrollOrderSupplier supplier) {
    bubbleScrollOrderSupplier = supplier;
  }

  @Override
  public boolean isNestedScrollingEnabled() {
    return childHelper.isNestedScrollingEnabled();
  }

  @Override
  public boolean hasNestedScrollingParent(int type) {
    return childHelper.hasNestedScrollingParent(type);
  }

  // ---- Scrolling parent

  @Override
  public boolean onStartNestedScroll(View child, View target, int axes, int type) {
    final boolean handled = super.onStartNestedScroll(child, target, axes, type);
    return startNestedScroll(axes, type) || handled;
  }

  @Override
  public void onNestedPreScroll(View target, int dx, int dy, int[] consumed, int type) {
    final int order;
    if (bubbleScrollOrderSupplier != null) {
      order = bubbleScrollOrderSupplier.getOrder(this, target, dx, dy);
    } else {
      order = BUBBLE_SCROLL_NONE;
    }
    if (order == BUBBLE_SCROLL_NONE) {
      super.onNestedPreScroll(target, dx, dy, consumed, type);
      return;
    }
    if (order == BUBBLE_SCROLL_BEFORE) {
      dispatchNestedPreScroll(dx, dy, consumed, null, type);
      final int parentConsumedX = consumed[0];
      final int parentConsumedY = consumed[1];
      if (parentConsumedX != dx || parentConsumedY != dy) {
        super.onNestedPreScroll(target, dx - parentConsumedX, dy - parentConsumedY, consumed, type);
        consumed[0] += parentConsumedX;
        consumed[1] += parentConsumedY;
      }
      return;
    }
    if (order == BUBBLE_SCROLL_AFTER) {
      super.onNestedPreScroll(target, dx, dy, consumed, type);
      final int consumedX = consumed[0];
      final int consumedY = consumed[1];
      if (consumedX != dx || consumedY != dy) {
        dispatchNestedPreScroll(dx - consumedX, dy - consumedY, consumed, null, type);
        consumed[0] += consumedX;
        consumed[1] += consumedY;
      }
    }
  }

  @Override
  public void onNestedScroll(
      View target,
      int dxConsumed, int dyConsumed,
      int dxUnconsumed, int dyUnconsumed,
      int type) {
    super.onNestedScroll(
        target,
        dxConsumed, dyConsumed,
        dxUnconsumed, dyUnconsumed,
        type);
    dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null, type);
  }

  @Override
  public void onStopNestedScroll(View target, int type) {
    super.onStopNestedScroll(target, type);
    stopNestedScroll(type);
  }

  @Override
  public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
    final boolean handled = super.onNestedPreFling(target, velocityX, velocityY);
    return dispatchNestedPreFling(velocityX, velocityY) || handled;
  }

  @Override
  public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
    final boolean handled = super.onNestedFling(target, velocityX, velocityY, consumed);
    return dispatchNestedFling(velocityX, velocityY, consumed) || handled;
  }

  // --- Scrolling child

  @Override
  public boolean startNestedScroll(int axes, int type) {
    return childHelper.startNestedScroll(axes, type);
  }

  @Override
  public void stopNestedScroll(int type) {
    childHelper.stopNestedScroll(type);
  }

  @Override
  public boolean dispatchNestedPreScroll(
      int dx, int dy,
      @Nullable int[] consumed, @Nullable int[] offsetInWindow,
      int type) {
    return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
  }

  @Override
  public boolean dispatchNestedScroll(
      int dxConsumed, int dyConsumed,
      int dxUnconsumed, int dyUnconsumed,
      @Nullable int[] offsetInWindow,
      int type) {
    return childHelper.dispatchNestedScroll(
        dxConsumed, dyConsumed,
        dxUnconsumed, dyUnconsumed,
        offsetInWindow,
        type);
  }

  @Override
  public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
    return childHelper.dispatchNestedPreFling(velocityX, velocityY);
  }

  @Override
  public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
    return childHelper.dispatchNestedFling(velocityX, velocityY, consumed);
  }

  /** 获取冒泡事件顺序 */
  public interface BubbleScrollOrderSupplier {

    /** 根据当前滑动状态获取冒泡事件顺序 */
    @BubbleScrollOrder
    int getOrder(NestedCoordinatorLayout layout, View target, int dx, int dy);
  }
}
