package com.google.engedu.wordstack;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Stack;

public class StackedLayout extends LinearLayout {

    private Stack<View> tiles = new Stack();

    public StackedLayout(Context context) {
        super(context);
    }

    public void push(View tile) {
        if (!tiles.empty()) {
            this.removeView(tiles.peek());
        }
        tiles.push(tile);
        this.addView(tiles.peek());
    }

    public View pop() {
        View popped = tiles.pop();
        this.removeView(popped);
        if (!tiles.empty()) {
            this.addView(tiles.peek());
        }
        return popped;
    }

    public View peek() {
        return tiles.peek();
    }

    public boolean empty() {
        return tiles.empty();
    }

    public void clear() {
        this.removeAllViews();
        tiles.clear();
    }
}
