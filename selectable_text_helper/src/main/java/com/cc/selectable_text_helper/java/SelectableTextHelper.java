package com.cc.selectable_text_helper.java;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.cc.selectable_text_helper.R;

/**
 * Created by guoshichao on 2021/3/17
 * <p>
 * 此View只能包含一个子View
 * CursorHandle  是两个游标
 * OperateWindow  是弹出的操作框
 * FullScreenWindow  全屏弹窗，点击空白全部弹窗消失
 */
public class SelectableTextHelper {

    private Context mContext;
    private TextView mTextView;

    private Spannable mSpannable;
    private final SelectionInfo mSelectionInfo = new SelectionInfo();
    private final static int DEFAULT_SELECTION_LENGTH = 1;
    private BackgroundColorSpan mSpan;
    private final int mCursorHandleColor = R.color.selectable_cursor;
    private final int mSelectedColor = R.color.selectable_select_text_bg;
    private CursorHandle mStartHandle;
    private CursorHandle mEndHandle;
    private boolean isShow = true;
    private OperateWindow mOperateWindow;
    private FullScreenWindow mFullScreenWindow;

    private SelectableOnShowListener onShowListener;
    private SelectableOnChangeListener onChangeListener;

    public SelectableTextHelper() {

    }

    public void setSelectableOnShowListener(SelectableOnShowListener onShowListener) {
        this.onShowListener = onShowListener;
    }

    public void setSelectableOnChangeListener(SelectableOnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    public void showSelectView(TextView textView, int x, int y) {
        if (textView.getPaddingLeft() > 0 || textView.getPaddingRight() > 0
                || textView.getPaddingTop() > 0 || textView.getPaddingBottom() > 0
                || textView.getPaddingStart() > 0 || textView.getPaddingEnd() > 0) {
            throw new SelectFrameLayoutException("不可给TextView设置padding");
        }

        mContext = textView.getContext();
        mTextView = textView;
        mTextView.setText(mTextView.getText(), TextView.BufferType.SPANNABLE);

        mOperateWindow = new OperateWindow(mContext);
        mFullScreenWindow = new FullScreenWindow(mContext);

        hideSelectView();
        resetSelectionInfo();
        isShow = true;
        if (mStartHandle == null)
            mStartHandle = new CursorHandle(true);
        if (mEndHandle == null)
            mEndHandle = new CursorHandle(false);

        //点哪选哪
//        int startOffset = TextLayoutUtil.getPreciseOffset(this, x, y);
//        int endOffset = startOffset + DEFAULT_SELECTION_LENGTH;
        //全选
        int startOffset = 0;
        int endOffset = mTextView.length();
        if (mTextView.getText() instanceof Spannable) {
            mSpannable = (Spannable) mTextView.getText();
        }
        if (mSpannable == null || startOffset >= mTextView.getText().length()) {
            return;
        }
        selectText(startOffset, endOffset);
        mFullScreenWindow.show();
        showCursorHandle(mStartHandle);
        showCursorHandle(mEndHandle);
        mOperateWindow.showWithTextView();
    }

    public void hideSelectView() {
        isShow = false;

        if (mStartHandle != null) {
            mStartHandle.dismiss();
        }
        if (mEndHandle != null) {
            mEndHandle.dismiss();
        }
        if (mOperateWindow != null) {
            mOperateWindow.dismiss();
        }
        if (mFullScreenWindow != null) {
            mFullScreenWindow.dismiss();
        }
    }

    private void showCursorHandle(CursorHandle cursorHandle) {
        Layout layout = mTextView.getLayout();
        int offset = cursorHandle.isLeft ? mSelectionInfo.getStart(mTextView)
                : mSelectionInfo.getEnd(mTextView);
        cursorHandle.show((int) layout.getPrimaryHorizontal(offset),
                layout.getLineBottom(layout.getLineForOffset(offset)));
    }

    public void resetSelectionInfo() {
        mSelectionInfo.mSelectionContent = null;
        if (mSpannable != null && mSpan != null) {
            mSpannable.removeSpan(mSpan);
            mSpan = null;
        }
    }

    /*
     * startPos:起始索引 endPos：尾部索引
     */
    private void selectText(int startPos, int endPos) {
        if (startPos != -1) {
            mSelectionInfo.setStart(startPos);
        }
        if (endPos != -1) {
            mSelectionInfo.setEnd(endPos);
        }
        if (mSelectionInfo.getStart(mTextView) > mSelectionInfo.getEnd(mTextView)) {
            int temp = mSelectionInfo.getStart(mTextView);
            mSelectionInfo.setStart(mSelectionInfo.getEnd(mTextView));
            mSelectionInfo.setEnd(temp);
        }

        if (mSpannable != null) {
            if (mSpan == null) {
                mSpan = new BackgroundColorSpan(mContext.getResources().getColor(mSelectedColor));
            }

            mSelectionInfo.mSelectionContent = mSpannable.subSequence(
                    mSelectionInfo.getStart(mTextView), mSelectionInfo.getEnd(mTextView)).toString();

            // 调用系统方法设置选中文本的状态
            mSpannable.setSpan(mSpan, mSelectionInfo.getStart(mTextView), mSelectionInfo.getEnd(mTextView), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            if (onChangeListener != null) {
                onChangeListener.onChange(mSelectionInfo.mSelectionContent,
                        startPos == 0 && endPos == mTextView.getText().length());
            }
        }
    }

    public int getTextViewX() {
        int[] location = new int[2];
        mTextView.getLocationOnScreen(location);
        return location[0];
    }

    public int getTextViewY() {
        int[] location = new int[2];
        mTextView.getLocationOnScreen(location);
        return location[1];
    }

    /*
     * 游标类
     */
    class CursorHandle extends View {

        private final int mCursorHandleSize = 48;
        private PopupWindow mPopupWindow;
        private Paint mPaint;

        private int mCircleRadius = mCursorHandleSize / 2;
        private int mWidth = mCircleRadius * 2;
        private int mHeight = mCircleRadius * 2;
        private int mPadding = 25;
        private boolean isLeft;

        public CursorHandle(boolean isLeft) {
            super(mContext);
            this.isLeft = isLeft;
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(mContext.getResources().getColor(mCursorHandleColor));

            mPopupWindow = new PopupWindow(this);
            mPopupWindow.setClippingEnabled(false);
            mPopupWindow.setWidth(mWidth + mPadding * 2);
            mPopupWindow.setHeight(mHeight + mPadding / 2);

            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawCircle(mCircleRadius + mPadding, mCircleRadius, mCircleRadius, mPaint);
            if (isLeft) {
                canvas.drawRect(mCircleRadius + mPadding, 0, mCircleRadius * 2
                        + mPadding, mCircleRadius, mPaint);
            } else {
                canvas.drawRect(mPadding, 0, mCircleRadius + mPadding,
                        mCircleRadius, mPaint);
            }
        }

        private int mAdjustX;
        private int mAdjustY;

        private int mBeforeDragStart;
        private int mBeforeDragEnd;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mBeforeDragStart = mSelectionInfo.getStart(mTextView);
                    mBeforeDragEnd = mSelectionInfo.getEnd(mTextView);
                    mAdjustX = (int) event.getX();
                    mAdjustY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mOperateWindow.showWithTextView();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mOperateWindow.dismiss();
                    int rawX = (int) event.getRawX();
                    int rawY = (int) event.getRawY();
                    update(rawX + mAdjustX - mWidth - getTextViewX(), rawY + mAdjustY - mHeight);
                    break;
            }
            return true;
        }

        private void changeDirection() {
            isLeft = !isLeft;
            invalidate();
        }

        public void dismiss() {
            mPopupWindow.dismiss();
        }

        private int[] mTempCoors = new int[2];

        public void update(int x, int y) {
            mTextView.getLocationInWindow(mTempCoors);
            int oldOffset;
            if (isLeft) {
                oldOffset = mSelectionInfo.getStart(mTextView);
            } else {
                oldOffset = mSelectionInfo.getEnd(mTextView);
            }

            y -= mTempCoors[1];

            int offset = TextLayoutUtils.getHysteresisOffset(mTextView, x,
                    y, oldOffset);

            if (offset != oldOffset) {
                resetSelectionInfo();
                if (isLeft) {
                    if (offset > mBeforeDragEnd) {
                        CursorHandle handle = getCursorHandle(false);
                        changeDirection();
                        handle.changeDirection();
                        mBeforeDragStart = mBeforeDragEnd;
                        selectText(mBeforeDragEnd, offset);
                        handle.updateCursorHandle();
                    } else {
                        selectText(offset, -1);
                    }
                    updateCursorHandle();
                } else {
                    if (offset < mBeforeDragStart) {
                        CursorHandle handle = getCursorHandle(true);
                        handle.changeDirection();
                        changeDirection();
                        mBeforeDragEnd = mBeforeDragStart;
                        selectText(offset, mBeforeDragStart);
                        handle.updateCursorHandle();
                    } else {
                        selectText(mBeforeDragStart, offset);
                    }
                    updateCursorHandle();
                }
            }
        }

        private void updateCursorHandle() {
            mTextView.getLocationInWindow(mTempCoors);
            Layout layout = mTextView.getLayout();
            if (isLeft) {
                mPopupWindow.update(
                        (int) layout
                                .getPrimaryHorizontal(mSelectionInfo.getStart(mTextView))
                                - mWidth + getExtraX(),
                        layout.getLineBottom(layout
                                .getLineForOffset(mSelectionInfo.getStart(mTextView)))
                                + getExtraY(), -1, -1);
            } else {
                mPopupWindow.update(
                        (int) layout.getPrimaryHorizontal(mSelectionInfo.getEnd(mTextView))
                                + getExtraX(),
                        layout.getLineBottom(layout
                                .getLineForOffset(mSelectionInfo.getEnd(mTextView)))
                                + getExtraY(), -1, -1);
            }
        }

        public void show(int x, int y) {
            mTextView.getLocationInWindow(mTempCoors);
            int offset = isLeft ? mWidth : 0;
            mPopupWindow.showAtLocation(mTextView, Gravity.NO_GRAVITY, x
                    - offset + getExtraX(), y + getExtraY());
        }

        public int getExtraX() {
            return mTempCoors[0] - mPadding + mTextView.getPaddingLeft();
        }

        public int getExtraY() {
            return mTempCoors[1] + mTextView.getPaddingTop();
        }

    }

    private CursorHandle getCursorHandle(boolean isLeft) {
        if (mStartHandle.isLeft == isLeft) {
            return mStartHandle;
        } else {
            return mEndHandle;
        }
    }

    /*
     * 操作框
     */
    private class OperateWindow {

        private int screenWidth;
        private int paddingLR;

        private PopupWindow mWindow;

        private View contentView;
        private View ivArrow;

        public OperateWindow(final Context context) {
            screenWidth = TextLayoutUtils.getScreenWidth(mContext);
            paddingLR = TextLayoutUtils.dip2px(mContext, 13);

            contentView = LayoutInflater.from(context).inflate(
                    R.layout.select_text_operate_windows, null);
            contentView.measure(View.MeasureSpec.makeMeasureSpec(0,
                    View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                    .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            mWindow = new PopupWindow(contentView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, false);
            mWindow.setClippingEnabled(false);


            ivArrow = contentView.findViewById(R.id.iv_arrow);
        }

        private int getWindowWidth() {
            return contentView.getMeasuredWidth();
        }

        private int getWindowHeight() {
            return contentView.getMeasuredHeight();
        }

        private int getWindowRemoveRight() {
            int removeX = 0;
            Layout layout = mTextView.getLayout();
            int start = (int) layout.getPrimaryHorizontal(mSelectionInfo.getStart(mTextView));
            int end = (int) layout.getPrimaryHorizontal(mSelectionInfo.getEnd(mTextView));
            boolean isSameLine = layout.getLineTop(layout.getLineForOffset(mSelectionInfo.getStart(mTextView))) == layout.getLineTop(layout.getLineForOffset(mSelectionInfo.getEnd(mTextView)));
            if (end > start && isSameLine) {
                removeX = end - start;
            } else {
                removeX = mTextView.getWidth() - start;
            }
            return removeX / 2;
        }

        public void showWithTextView() {
            Layout layout = mTextView.getLayout();
            int posX = (int) layout.getPrimaryHorizontal(mSelectionInfo.getStart(mTextView))
                    + getTextViewX()
                    - getWindowWidth() / 2
                    + getWindowRemoveRight();
            int posY = layout.getLineTop(layout.getLineForOffset(mSelectionInfo.getStart(mTextView)))
                    + getTextViewY()
                    - getWindowHeight()
                    - paddingLR;
            int removeArrow = 0;
            if (posX < paddingLR) {
                removeArrow = posX - paddingLR;
                posX = paddingLR;
            }
            if (posY < 0) {
                posY = paddingLR;
            }
            if (posX + getWindowWidth() > screenWidth - paddingLR) {
                removeArrow = posX - (screenWidth - getWindowWidth() - paddingLR);
                posX = screenWidth - getWindowWidth() - paddingLR;
            }

            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) ivArrow.getLayoutParams();
            lp.leftMargin = removeArrow;
            ivArrow.setLayoutParams(lp);

            mWindow.showAtLocation(mTextView, Gravity.NO_GRAVITY, posX, posY);
        }

        public void showWithView() {
            int posX = getTextViewX()
                    - getWindowWidth() / 2
                    + mTextView.getMeasuredWidth() / 2;
            int posY = getTextViewY()
                    - getWindowHeight()
                    + mTextView.getPaddingTop()
                    - paddingLR;
            int removeArrow = 0;
            if (posX < paddingLR) {
                removeArrow = posX - paddingLR;
                posX = paddingLR;
            }
            if (posY < paddingLR) {
                posY = paddingLR;
            }
            if (posX + getWindowWidth() > screenWidth - paddingLR) {
                removeArrow = posX - (screenWidth - getWindowWidth() - paddingLR);
                posX = screenWidth - getWindowWidth() - paddingLR;
            }

            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) ivArrow.getLayoutParams();
            lp.leftMargin = removeArrow;
            ivArrow.setLayoutParams(lp);

            mWindow.showAtLocation(mTextView, Gravity.NO_GRAVITY, posX, posY);
        }

        public void dismiss() {
            mWindow.dismiss();
        }

        public boolean isShowing() {
            return mWindow.isShowing();
        }

    }

    /*
     * 全屏Window，用来点击空白使其它弹窗消失
     */
    private class FullScreenWindow {

        private PopupWindow mFullScreenWindow;

        public FullScreenWindow(Context context) {
            View contentView = LayoutInflater.from(context).inflate(
                    R.layout.select_text_full_screen_windows, null);
            mFullScreenWindow = new PopupWindow(contentView,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, false);
            mFullScreenWindow.setClippingEnabled(false);

            mFullScreenWindow.setTouchInterceptor(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (mOperateWindow == null
                            || mOperateWindow.contentView == null) {
                        dismiss();
                    }
                    if (!TextLayoutUtils.isInView(mOperateWindow.contentView, event)) {
                        if (mStartHandle != null && mEndHandle != null) {
                            if (!TextLayoutUtils.isInView(mStartHandle, event)
                                    && !TextLayoutUtils.isInView(mEndHandle, event)) {
                                resetSelectionInfo();
                                hideSelectView();
                            }
                        } else {
                            hideSelectView();
                        }
                    }
                    return true;
                }
            });
        }

        public void show() {
            mFullScreenWindow.showAtLocation(mTextView, Gravity.NO_GRAVITY, 0, 0);
        }

        public void dismiss() {
            mFullScreenWindow.dismiss();
        }
    }


}
