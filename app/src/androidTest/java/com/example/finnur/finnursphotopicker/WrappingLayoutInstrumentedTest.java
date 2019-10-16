// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.example.finnur.contactspicker;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;  // Android Studio project only.
import android.view.View;
import android.view.View.MeasureSpec;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

//import org.chromium.base.test.params.BaseJUnit4RunnerDelegate;
//import org.chromium.base.test.params.ParameterAnnotations.UseMethodParameter;
//import org.chromium.base.test.params.ParameterAnnotations.UseRunnerDelegate;
//import org.chromium.base.test.params.ParameterProvider;
//import org.chromium.base.test.params.ParameterSet;
//import org.chromium.base.test.params.ParameterizedRunner;

import org.chromium.chrome.browser.ui.widget.WrappingLayout; // Android Studio project only.

//import java.util.Arrays;
//import java.util.List;

/** Tests for the {@link WrappingLayout} class. */
@RunWith(AndroidJUnit4.class)
//@RunWith(ParameterizedRunner.class)
//@UseRunnerDelegate(BaseJUnit4RunnerDelegate.class)
public class WrappingLayoutInstrumentedTest {
    /**
     * A class providing input parameters for the test below.
     */
    /* Android Studio does not use parameterized tests, but this list should match
       testWrappingLayoutMain below.
    public static class WrappingLayoutTestParams implements ParameterProvider {
        @Override
        public List<ParameterSet> getParameters() {
            return Arrays.asList(
                    // Test function expects: measureSpec, padding, margin, spacing.
                    new ParameterSet()
                            .value(MeasureSpec.UNSPECIFIED, 0, 0, 0)
                            .name("UnboundedCompact"),
                    new ParameterSet()
                            .value(MeasureSpec.UNSPECIFIED, 15, 0, 0)
                            .name("UnboundedWithPadding"),
                    new ParameterSet()
                            .value(MeasureSpec.UNSPECIFIED, 0, 10, 0)
                            .name("UnboundedWithMargin"),
                    new ParameterSet()
                            .value(MeasureSpec.UNSPECIFIED, 15, 10, 0)
                            .name("UnboundedWithPaddingAndMargin"),
                    new ParameterSet()
                            .value(MeasureSpec.UNSPECIFIED, 0, 0, 3)
                            .name("UnboundedWithspacing"),
                    new ParameterSet()
                            .value(MeasureSpec.UNSPECIFIED, 0, 10, 3)
                            .name("UnboundedWithMarginAndSpacing"),
                    new ParameterSet()
                            .value(MeasureSpec.UNSPECIFIED, 15, 0, 3)
                            .name("UnboundedWithPaddingAndSpacing"),
                    new ParameterSet()
                            .value(MeasureSpec.UNSPECIFIED, 15, 10, 3)
                            .name("UnboundedWithPaddingMarginAndSpacing"),

                    new ParameterSet().value(MeasureSpec.EXACTLY, 0, 0, 0).name("ExactCompact"),
                    new ParameterSet()
                            .value(MeasureSpec.EXACTLY, 15, 0, 0)
                            .name("ExactWithPadding"),
                    new ParameterSet().value(MeasureSpec.EXACTLY, 0, 10, 0).name("ExactWithMargin"),
                    new ParameterSet()
                            .value(MeasureSpec.EXACTLY, 15, 10, 0)
                            .name("ExactWithPaddingAndMargin"),
                    new ParameterSet().value(MeasureSpec.EXACTLY, 0, 0, 3).name("ExactWithSpacing"),
                    new ParameterSet()
                            .value(MeasureSpec.EXACTLY, 0, 10, 3)
                            .name("ExactWithMarginAndSpacing"),
                    new ParameterSet()
                            .value(MeasureSpec.EXACTLY, 15, 0, 3)
                            .name("ExactWithPaddingAndSpacing"),
                    new ParameterSet()
                            .value(MeasureSpec.EXACTLY, 15, 10, 3)
                            .name("ExactWithPaddingMarginAndSpacing"),

                    new ParameterSet().value(MeasureSpec.AT_MOST, 0, 0, 0).name("BoundedCompact"),
                    new ParameterSet()
                            .value(MeasureSpec.AT_MOST, 15, 0, 0)
                            .name("BoundedWithPadding"),
                    new ParameterSet()
                            .value(MeasureSpec.AT_MOST, 0, 10, 0)
                            .name("BoundedWithMargin"),
                    new ParameterSet()
                            .value(MeasureSpec.AT_MOST, 15, 10, 0)
                            .name("BoundedWithPaddingAndMargin"),
                    new ParameterSet()
                            .value(MeasureSpec.AT_MOST, 0, 0, 3)
                            .name("BoundedWithspacing"),
                    new ParameterSet()
                            .value(MeasureSpec.AT_MOST, 0, 10, 3)
                            .name("BoundedWithMarginAndSpacing"),
                    new ParameterSet()
                            .value(MeasureSpec.AT_MOST, 15, 0, 3)
                            .name("BoundedWithPaddingAndSpacing"),
                    new ParameterSet()
                            .value(MeasureSpec.AT_MOST, 15, 10, 3)
                            .name("BoundedWithPaddingMarginAndSpacing"));
        }
    }
    */

    private class WrappingLayoutSubclass extends WrappingLayout {
        Context mContext;
        int mRequestedWidth;
        int mRequestedHeight;

        WrappingLayoutSubclass(Context context) {
            super(context);
            mContext = context;
        }

        public View addView(String tag, int width, int height, int margin, int visibility) {
            View view = new View(mContext);
            view.setTag(tag);
            if (visibility == View.VISIBLE) {
                MarginLayoutParams params = new MarginLayoutParams(width, height);
                params.setMargins(margin, margin, margin, margin);
                view.setLayoutParams(params);
            } else {
                view.setVisibility(View.GONE);
            }
            super.addView(view);
            return view;
        }

        public void updateView(View view, int width, int height, int margin) {
            MarginLayoutParams params = new MarginLayoutParams(width, height);
            params.setMargins(margin, margin, margin, margin);
            view.setLayoutParams(params);
        }

        public void layoutAtSize(
                int width, int height, int measureSpecWidth, int measureSpecHeight, int padding) {
            mRequestedWidth = width + 2 * padding;
            mRequestedHeight = height + 2 * padding;

            int specWidth = MeasureSpec.makeMeasureSpec(mRequestedWidth, measureSpecWidth);
            int specHeight = MeasureSpec.makeMeasureSpec(mRequestedHeight, measureSpecHeight);
            measure(specWidth, specHeight);
            layout(0, 0, mRequestedWidth, mRequestedHeight);
        }

        /**
         * Validates the calculated width and height for the view is correct.
         * @param expectedWidth The expected width of the view, not accounting for layout padding
         *         and view margins.
         * @param expectedHeight The expected height of the view, not accounting for layout padding
         *         and view margins.
         * @param expectedMaxCols The max number of fully visible (not truncated) columns to expect
         *         the views to line up in, once laid out.
         * @param expectedMaxRows The max number of fully visible (not truncated) rows to expect the
         *         views to line up in, once laid out.
         * @param margin The amount of margin (in pixels) to account for during calculations.
         * @param padding The amount of padding (in pixels) to account for during calculations.
         * @param spacing The amount of spacing (in pixels) to account for (between views) during
         *         calculations.
         * @param measureSpecWidth The measureSpec to use for width (EXACT, AT_MOST, UNSPECIFIED).
         * @param measureSpecHeight The measureSpec to use for height (EXACT, AT_MOST, UNSPECIFIED).
         */
        public void validateCalculatedSize(int expectedWidth, int expectedHeight,
                int expectedMaxRows, int expectedMaxCols, int margin, int padding, int spacing,
                int measureSpecWidth, int measureSpecHeight) {
            int measuredWidth = getMeasuredWidth();
            int measuredHeight = getMeasuredHeight();
            String message = "Layout is incorrectly sized. Note: Requested margin was: " + margin
                    + ", padding: " + padding + ", and spacing in-between: " + spacing
                    + ". Failure checking Layout.";
            if (measureSpecWidth == MeasureSpec.EXACTLY
                    && measureSpecHeight == MeasureSpec.EXACTLY) {
                Assert.assertEquals(message + "width:", mRequestedWidth, measuredWidth);
                Assert.assertEquals(message + "height:", mRequestedHeight, measuredHeight);
            } else {
                int expectedWidthWithBuffers = expectedWidth + 2 * padding
                        + (expectedMaxCols * 2 * margin)
                        + (expectedMaxCols > 0 ? (expectedMaxCols - 1) * spacing : 0);
                int expectedHeightWithBuffers = expectedHeight + 2 * padding
                        + (expectedMaxRows * 2 * margin)
                        + (expectedMaxRows > 0 ? (expectedMaxRows - 1) * spacing : 0);

                Assert.assertEquals(message + "width:", expectedWidthWithBuffers, measuredWidth);
                Assert.assertEquals(message + "height:", expectedHeightWithBuffers, measuredHeight);
            }
        }

        /**
         * Validates that a |view| is correctly positioned.
         * @param view The view to validate.
         * @param column The column we expect the view to be in (zero-based).
         * @param row The row we expect the view to be in (zero-based).
         * @param expectedLeft The expected left position of the view, before accounting for layout
         *         padding and view margins.
         * @param expectedTop The expected left position of the view, before accounting for layout
         *         padding and view margins.
         * @param expectedWidth The expected width of the view, before accounting for layout padding
         *         and view margins.
         * @param expectedHeight The expected height of the view, before accounting for layout
         *         padding and view margins.
         * @param margin The amount of margin (in pixels) to account for during calculations.
         * @param padding The amount of padding (in pixels) to account for during calculations.
         * @param spacing The amount of spacing (in pixels) to account for (between views) during
         *         calculations.
         */
        public void validateView(View view, int column, int row, int expectedLeft, int expectedTop,
                int expectedWidth, int expectedHeight, int margin, int padding, int spacing) {
            int left = view.getLeft();
            int right = view.getRight();
            int top = view.getTop();
            int bottom = view.getBottom();
            int expectedLeftWithBuffers =
                    expectedLeft + padding + ((1 + 2 * column) * margin) + (column * spacing);
            int expectedTopWithBuffers =
                    expectedTop + padding + ((1 + 2 * row) * margin) + (row * spacing);
            String message = "View '" + view.getTag() + "', expected to be in (zero-based) col "
                    + column + ", row " + row
                    + ", is incorrectly positioned. Note: Requested margin was: " + margin
                    + ", padding: " + padding + ", and spacing: " + spacing
                    + ". Failure checking View.";
            Assert.assertEquals(message + "left:", expectedLeftWithBuffers, left);
            Assert.assertEquals(message + "top:", expectedTopWithBuffers, top);
            Assert.assertEquals(message + "width:", expectedWidth, right - left);
            Assert.assertEquals(message + "height:", expectedHeight, bottom - top);
        }
    }

    private Context mContext;

    // Constants that improve readability of the tests.
    private static final int FIRST_COL = 0;
    private static final int SECOND_COL = 1;
    private static final int THIRD_COL = 2;
    private static final int FIRST_ROW = 0;
    private static final int SECOND_ROW = 1;
    private static final int THIRD_ROW = 2;

    private static final int TRUNCATED_COLS = 0;
    private static final int ONE_COL = 1;
    private static final int TWO_COLS = 2;
    private static final int THREE_COLS = 3;
    private static final int ONE_ROW = 1;
    private static final int TWO_ROWS = 2;
    private static final int THREE_ROWS = 3;

    @Before
    public void setUp() throws Exception {
        // Android Studio uses a different way of getting context than Chromium.
        mContext = InstrumentationRegistry.getTargetContext();
    }

    // These annotations are only appropriate for when running in Chromium tests.
    //@Test
    //@SmallTest
    //@UseMethodParameter(WrappingLayoutTestParams.class)
    public void testWrappingLayout(int measureSpec, int padding, int margin, int spacing) {
        WrappingLayoutSubclass layout = new WrappingLayoutSubclass(mContext);
        layout.setPadding(padding, padding, padding, padding);
        if (spacing > 0) layout.setSpacingBetweenViews(spacing, spacing);

        layout.addView("hidden", 200, 100, margin, View.GONE);
        View a = layout.addView("a", 180, 90, margin, View.VISIBLE);
        View b = layout.addView("b", 190, 100, margin, View.VISIBLE);
        layout.addView("hidden", 1, 1, margin, View.GONE);
        View c = layout.addView("c", 200, 110, margin, View.VISIBLE);
        layout.addView("hidden", 1, 1, margin, View.GONE);

        // Unbounded width doesn't make sense for a wrapping layout. Assume exact measurement so
        // test can still test unbounded height.
        int specWidth = measureSpec == MeasureSpec.UNSPECIFIED ? MeasureSpec.AT_MOST : measureSpec;
        int specHeight = measureSpec;

        // This tests the easy case (no wrapping). It creates a a layout of 1000x400 (with room
        // for all views) and verifies all views fit within the first row.
        layout.layoutAtSize(1000, 400, specWidth, specHeight, padding);
        layout.validateCalculatedSize(
                570, 110, ONE_ROW, THREE_COLS, margin, padding, spacing, measureSpec, measureSpec);
        layout.validateView(a, FIRST_COL, FIRST_ROW, 0, 0, 180, 90, margin, padding, spacing);
        layout.validateView(b, SECOND_COL, FIRST_ROW, 180, 0, 190, 100, margin, padding, spacing);
        layout.validateView(c, THIRD_COL, FIRST_ROW, 370, 0, 200, 110, margin, padding, spacing);

        // This is the worst-case scenario: Not enough horizontal room for any view because the
        // layout is only 100 in width, but the views are between 180 and 200 each.
        layout.layoutAtSize(100, 400, specWidth, specHeight, padding);
        layout.validateCalculatedSize(100, 300, THREE_ROWS, TRUNCATED_COLS, margin, padding,
                spacing, measureSpec, measureSpec);
        layout.validateView(a, FIRST_COL, FIRST_ROW, 0, 0, 180, 90, margin, padding, spacing);
        layout.validateView(b, FIRST_COL, SECOND_ROW, 0, 90, 190, 100, margin, padding, spacing);
        layout.validateView(c, FIRST_COL, THIRD_ROW, 0, 190, 200, 110, margin, padding, spacing);

        // This tests that wrapping happens correctly when there is only enough space for one
        // view per line (essentially the same test as above, except no truncation occurs).
        layout.layoutAtSize(200 + 2 * margin, 400, specWidth, specHeight, padding);
        layout.validateCalculatedSize(
                200, 300, THREE_ROWS, ONE_COL, margin, padding, spacing, measureSpec, measureSpec);
        layout.validateView(a, FIRST_COL, FIRST_ROW, 0, 0, 180, 90, margin, padding, spacing);
        layout.validateView(b, FIRST_COL, SECOND_ROW, 0, 90, 190, 100, margin, padding, spacing);
        layout.validateView(c, FIRST_COL, THIRD_ROW, 0, 190, 200, 110, margin, padding, spacing);

        // Test what happens if there is room for two views on the first row, and one on the
        // second.
        layout.layoutAtSize(400 + 2 * TWO_COLS * margin, 400, specWidth, specHeight, padding);
        layout.validateCalculatedSize(
                370, 210, TWO_ROWS, TWO_COLS, margin, padding, spacing, measureSpec, measureSpec);
        layout.validateView(a, FIRST_COL, FIRST_ROW, 0, 0, 180, 90, margin, padding, spacing);
        layout.validateView(b, SECOND_COL, FIRST_ROW, 180, 0, 190, 100, margin, padding, spacing);
        layout.validateView(c, FIRST_COL, SECOND_ROW, 0, 100, 200, 110, margin, padding, spacing);

        // Special-case: width set to 200 pixels exactly, height unbounded.
        if (specHeight == MeasureSpec.UNSPECIFIED) {
            layout.layoutAtSize(200 + 2 * margin, 300, MeasureSpec.EXACTLY, specHeight, padding);
            layout.validateCalculatedSize(200, 300, THREE_ROWS, ONE_COL, margin, padding, spacing,
                    MeasureSpec.EXACTLY, measureSpec);
            layout.validateView(a, FIRST_COL, FIRST_ROW, 0, 0, 180, 90, margin, padding, spacing);
            layout.validateView(
                    b, FIRST_COL, SECOND_ROW, 0, 90, 190, 100, margin, padding, spacing);
            layout.validateView(
                    c, FIRST_COL, THIRD_ROW, 0, 190, 200, 110, margin, padding, spacing);
        }

        // Same test as above (room for two views on the first row, and one on the second), except
        // swap A and C.
        layout.updateView(a, 200, 110, margin);
        layout.updateView(c, 180, 90, margin);
        // Remember: Views are: a, b, c = { 200, 110 }, { 190, 100 }, { 180, 90 }.
        layout.layoutAtSize(400 + 2 * TWO_COLS * margin, 400, specWidth, specHeight, padding);
        layout.validateCalculatedSize(
                390, 200, TWO_ROWS, TWO_COLS, margin, padding, spacing, measureSpec, measureSpec);
        layout.validateView(a, FIRST_COL, FIRST_ROW, 0, 0, 200, 110, margin, padding, spacing);
        layout.validateView(b, SECOND_COL, FIRST_ROW, 200, 0, 190, 100, margin, padding, spacing);
        layout.validateView(c, FIRST_COL, SECOND_ROW, 0, 110, 180, 90, margin, padding, spacing);

        // Test what happens if there is room for one view on the first row, and two on the
        // second.
        layout.layoutAtSize(
                370 + (2 * TWO_COLS * margin) + spacing, 400, specWidth, specHeight, padding);
        // Remember: Views are: a, b, c = { 200, 110 }, { 190, 100 }, { 180, 90 }.
        layout.validateCalculatedSize(
                370, 210, TWO_ROWS, TWO_COLS, margin, padding, spacing, measureSpec, measureSpec);
        layout.validateView(a, FIRST_COL, FIRST_ROW, 0, 0, 200, 110, margin, padding, spacing);
        layout.validateView(b, FIRST_COL, SECOND_ROW, 0, 110, 190, 100, margin, padding, spacing);
        layout.validateView(c, SECOND_COL, SECOND_ROW, 190, 110, 180, 90, margin, padding, spacing);
    }

    // This Android Studio project only function does the bulk of the work of the parameterized test in Chromium.
    // Should be kept in sync with WrappingLayoutTestParams above.
    @Test
    @SmallTest
    public void testWrappingLayoutMain() {
        testWrappingLayout(MeasureSpec.UNSPECIFIED, 0, 0, 0);
        testWrappingLayout(MeasureSpec.UNSPECIFIED, 15, 0, 0);
        testWrappingLayout(MeasureSpec.UNSPECIFIED, 0, 10, 0);
        testWrappingLayout(MeasureSpec.UNSPECIFIED, 15, 10, 0);
        testWrappingLayout(MeasureSpec.UNSPECIFIED, 0, 0, 3);
        testWrappingLayout(MeasureSpec.UNSPECIFIED, 0, 10, 3);
        testWrappingLayout(MeasureSpec.UNSPECIFIED, 15, 0, 3);
        testWrappingLayout(MeasureSpec.UNSPECIFIED, 15, 10, 3);

        testWrappingLayout(MeasureSpec.EXACTLY, 0, 0, 0);
        testWrappingLayout(MeasureSpec.EXACTLY, 15, 0, 0);
        testWrappingLayout(MeasureSpec.EXACTLY, 0, 10, 0);
        testWrappingLayout(MeasureSpec.EXACTLY, 15, 10, 0);
        testWrappingLayout(MeasureSpec.EXACTLY, 0, 0, 3);
        testWrappingLayout(MeasureSpec.EXACTLY, 0, 10, 3);
        testWrappingLayout(MeasureSpec.EXACTLY, 15, 0, 3);
        testWrappingLayout(MeasureSpec.EXACTLY, 15, 10, 3);

        testWrappingLayout(MeasureSpec.AT_MOST, 0, 0, 0);
        testWrappingLayout(MeasureSpec.AT_MOST, 15, 0, 0);
        testWrappingLayout(MeasureSpec.AT_MOST, 0, 10, 0);
        testWrappingLayout(MeasureSpec.AT_MOST, 15, 10, 0);
        testWrappingLayout(MeasureSpec.AT_MOST, 0, 0, 3);
        testWrappingLayout(MeasureSpec.AT_MOST, 0, 10, 3);
        testWrappingLayout(MeasureSpec.AT_MOST, 15, 0, 3);
        testWrappingLayout(MeasureSpec.AT_MOST, 15, 10, 3);
    }
}
