// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.chromium.base.VisibleForTesting;

import java.util.ArrayList;

import com.example.finnur.contactspicker.R;  // Android-studio project only.

/**
 * A horizontal layout that can wrap to the next line, if there's not enough space to fit all views.
 */
public class WrappingLayout extends ViewGroup {
    // The amount of horizontal space to apply to each child view (in pixels), in addition to any
    // margins set by the child.
    private int mHorizontalSpacing;

    // The amount of vertical space to apply to each child view (in pixels), in addition to any
    // margins set by the child.
    private int mVerticalSpacing;

    // The indices of visible child views of this layout. Allocated as a member class to avoid
    // allocations while drawing.
    private ArrayList<Integer> mVisibleChildren = new ArrayList<Integer>();

    public WrappingLayout(Context context) {
        this(context, null);
    }

    public WrappingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WrappingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = getContext().obtainStyledAttributes(
                attrs, R.styleable.WrappingLayout, defStyleAttr, 0);
        mHorizontalSpacing =
                array.getDimensionPixelSize(R.styleable.WrappingLayout_horizontalSpacing, 0);
        mVerticalSpacing =
                array.getDimensionPixelSize(R.styleable.WrappingLayout_verticalSpacing, 0);
    }

    /**
     * Sets the amount of spacing between views.
     * @param horizontal The amount of horizontal spacing to add (in pixels).
     * @param vertical The amount of vertical spacing to add (in pixels).
     */
    @VisibleForTesting
    // TODO(finnur): Figure out why this needs to be public for Android Studio tests.
    public void setSpacingBetweenViews(int horizontal, int vertical) {
        mHorizontalSpacing = horizontal;
        mVerticalSpacing = vertical;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams params) {
        return params instanceof MarginLayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams params) {
        return generateDefaultLayoutParams();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specModeForWidth = MeasureSpec.getMode(widthMeasureSpec);
        int specModeForHeight = MeasureSpec.getMode(heightMeasureSpec);

        // A wrapping layout must have bounded width to be able to figure out it's actual size. Do
        // not call setMeasuredDimension in order to trigger assert.
        if (specModeForWidth == MeasureSpec.UNSPECIFIED) return;

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        if (specModeForWidth == MeasureSpec.EXACTLY && specModeForHeight == MeasureSpec.EXACTLY) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            setMeasuredDimension(width, height);
            return;
        }

        // Don't account for padding yet, it will be added at the end.
        int width = MeasureSpec.getSize(widthMeasureSpec) - (getPaddingLeft() + getPaddingRight());

        int measuredWidth = 0;
        int measuredHeight = 0;

        int x = 0;
        // Height of the tallest child in the row, including top and bottom margins.
        int tallestChildInRow = 0;

        for (int i = 0; i < getChildCount(); ++i) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            mVisibleChildren.add(i);
        }

        int count = mVisibleChildren.size();
        for (int i = 0; i < count; ++i) {
            View child = getChildAt(mVisibleChildren.get(i));
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            int childWidth = lp.getMarginStart() + child.getMeasuredWidth() + lp.getMarginEnd();
            int childHeight = lp.topMargin + child.getMeasuredHeight() + lp.bottomMargin;

            if (x + childWidth <= width) {
                // This item fits in the current row.
                if (x != 0) x += mHorizontalSpacing;
                x += childWidth;

                tallestChildInRow = Math.max(tallestChildInRow, childHeight);
            } else {
                // This item is too large for the remaining space.
                if (tallestChildInRow != 0) measuredHeight += tallestChildInRow + mVerticalSpacing;

                x = childWidth;
                tallestChildInRow = childHeight;
            }

            measuredWidth = Math.max(measuredWidth, x);

            if (i + 1 == count) measuredHeight += tallestChildInRow;
        }

        // Account for padding again.
        measuredWidth += getPaddingLeft() + getPaddingRight();
        measuredHeight += getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(
                resolveSizeAndState(measureDimension(measuredWidth, getSuggestedMinimumWidth(),
                                            widthMeasureSpec),
                        widthMeasureSpec, 0),
                resolveSizeAndState(measureDimension(measuredHeight, getSuggestedMinimumHeight(),
                                            heightMeasureSpec),
                        heightMeasureSpec, 0));

        mVisibleChildren.clear();
    }

    /**
     * Resolves the actual size, after taking the measure spec into account.
     * @param desiredSize The desired size of the view (in pixels).
     * @param minSize The suggested minimum size (in pixels).
     * @param measureSpec The measure spec to use to determine the actual size.
     * @return The actual size of the view, in pixels.
     */
    private int measureDimension(int desiredSize, int minSize, int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        int result = 0;
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(desiredSize, size);
            } else {
                result = desiredSize;
            }

            result = Math.max(result, minSize);
        }

        return result;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int count = getChildCount();

        int x = getPaddingLeft();
        int y = getPaddingTop();

        // Height of the tallest child in the row, including top and bottom margins and
        // mVerticalSpacing (if applicable).
        int tallestChildInRow = 0;

        boolean isRtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;

            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // childLeft and childTop should point to the x,y coordinates of where the view will be
            // drawn.
            int childLeft = x + lp.getMarginStart();
            int childTop = y + lp.topMargin;

            boolean firstViewInRow = x == getPaddingLeft();

            int widthWithMargins = lp.getMarginStart() + childWidth + lp.getMarginEnd();
            int heightWithMargins = lp.topMargin + childHeight + lp.bottomMargin;

            if (!firstViewInRow && x + widthWithMargins > getMeasuredWidth()) {
                // We've found a view that should wrap to the next line. Note that the first view in
                // a row can never wrap, otherwise it would leave an empty slot behind it.

                // Reset view coordinates to the start of a new line.
                childLeft = getPaddingLeft() + lp.getMarginStart();
                childTop += tallestChildInRow + mVerticalSpacing;
                tallestChildInRow = heightWithMargins;

                y = childTop - lp.topMargin;
            } else {
                // We've found a view that fits on the current line (or the allocated space is so
                // small that it won't fit anywhere and it should be drawn truncated).
                tallestChildInRow = Math.max(tallestChildInRow, heightWithMargins);
            }

            int childRight = childLeft + childWidth;
            if (isRtl) {
                int tmp = childWidth - childRight;
                childRight = childWidth - childLeft;
                childLeft = tmp;
            }

            child.layout(childLeft, childTop, childRight, childTop + childHeight);

            x = childLeft + childWidth + lp.getMarginEnd() + mHorizontalSpacing;
        }
    }
}
