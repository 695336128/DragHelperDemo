package com.example.draghelperdemo.source;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public abstract class HeaderBehavior <V extends View> extends ViewOffsetBehavior<V> {

    private static final int INVALID_POINTER = -1;

    private boolean isBeingDragged;
    private int activePointerId = INVALID_POINTER;
    private int lastMotionY;
    private int touchSlop = -1;

    public HeaderBehavior() {}

    public HeaderBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(
            @NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent ev) {
        if (touchSlop < 0) {
            touchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
        }
        Log.d("Zhang", "onInterceptTouchEvent" + ev.getAction());
        // Shortcut since we're being dragged
        if (ev.getActionMasked() == MotionEvent.ACTION_MOVE && isBeingDragged) {
            if (activePointerId == INVALID_POINTER) {
                // If we don't have a valid id, the touch down wasn't on content.
                return false;
            }
            int pointerIndex = ev.findPointerIndex(activePointerId);
            if (pointerIndex == -1) {
                return false;
            }

            int y = (int) ev.getY(pointerIndex);
            int yDiff = Math.abs(y - lastMotionY);
            if (yDiff > touchSlop) {
                lastMotionY = y;
                return true;
            }
        }

        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            activePointerId = INVALID_POINTER;

            int x = (int) ev.getX();
            int y = (int) ev.getY();
            isBeingDragged = canDragView(child) && parent.isPointInChildBounds(child, x, y);
            if (isBeingDragged) {
                lastMotionY = y;
                activePointerId = ev.getPointerId(0);
            }
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(
            @NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent ev) {
        boolean consumeUp = false;
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = ev.findPointerIndex(activePointerId);
                if (activePointerIndex == -1) {
                    return false;
                }

                final int y = (int) ev.getY(activePointerIndex);
                int dy = lastMotionY - y;
                lastMotionY = y;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                int newIndex = ev.getActionIndex() == 0 ? 1 : 0;
                activePointerId = ev.getPointerId(newIndex);
                lastMotionY = (int) (ev.getY(newIndex) + 0.5f);
                Log.d("Zhang", "onTouchEvent: MotionEvent.ACTION_POINTER_UP");
                break;
            case MotionEvent.ACTION_UP:
                Log.d("Zhang", "onTouchEvent: MotionEvent.ACTION_UP");
                // $FALLTHROUGH
            case MotionEvent.ACTION_CANCEL:
                isBeingDragged = false;
                activePointerId = INVALID_POINTER;
                Log.d("Zhang", "onTouchEvent: MotionEvent.ACTION_CANCEL");
                break;
        }

        Log.d("Zhang", "onTouchEvent: " + (isBeingDragged || consumeUp));
        return isBeingDragged || consumeUp;
    }

    /** Return true if the view can be dragged. */
    boolean canDragView(V view) {
        return false;
    }


}
