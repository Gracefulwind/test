package com.wind.widget.span;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by Gracefulwind Wang on 2018/7/6.
 * Email : Gracefulwindbigwang@gmail.com
 *
 * @author : Gracefulwind
 */
public class SpannableNormalMovementMethod extends LinkMovementMethod {

    private static SpannableNormalMovementMethod mInstance;

    public static SpannableNormalMovementMethod getInstance(){
        synchronized (SpannableNormalMovementMethod.class){
            if(null == mInstance){
                mInstance = new SpannableNormalMovementMethod();
            }
        }
        return mInstance;
    }



    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(widget);
                    Selection.removeSelection(buffer);
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(buffer,
                            buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0]));
                }
                return true;
            } else {
                Selection.removeSelection(buffer);
                //还是给执行，但是不消费
                //这会让此TextView可滑动，这和可伸展Textview的理念相冲突，不推荐
                super.onTouchEvent(widget, buffer, event);
                return false;
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }

}