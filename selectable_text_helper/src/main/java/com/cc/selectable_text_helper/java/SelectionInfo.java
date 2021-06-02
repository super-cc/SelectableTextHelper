package com.cc.selectable_text_helper.java;

import android.text.Spannable;
import android.widget.TextView;

public class SelectionInfo {
    private int mStart;
    private int mEnd;
    public String mSelectionContent;

    public int getStart(TextView textView) {
        if (textView == null) {
            return 0;
        }
        if (mStart > textView.length()) {
            return textView.length();
        }
        if (mStart < 0) {
            return 0;
        }
        return mStart;
    }

    public int getStart(CharSequence charSequence) {
        if (charSequence == null) {
            return 0;
        }
        if (mStart > charSequence.length()) {
            return charSequence.length();
        }
        if (mStart < 0) {
            return 0;
        }
        return mStart;
    }

    public void setStart(int start) {
        this.mStart = start;
    }

    public int getEnd(TextView textView) {
        if (textView == null) {
            return 0;
        }
        if (mEnd > textView.length()) {
            return textView.length();
        }
        if (mEnd < 0) {
            return 0;
        }
        return mEnd;
    }

    public int getEnd(CharSequence charSequence) {
        if (charSequence == null) {
            return 0;
        }
        if (mEnd > charSequence.length()) {
            return charSequence.length();
        }
        if (mEnd < 0) {
            return 0;
        }
        return mEnd;
    }

    public void setEnd(int end) {
        this.mEnd = end;
    }
}
