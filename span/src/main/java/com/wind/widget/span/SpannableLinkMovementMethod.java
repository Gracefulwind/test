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
public class SpannableLinkMovementMethod extends LinkMovementMethod {

    private static SpannableLinkMovementMethod mInstance;

    public static SpannableLinkMovementMethod getInstance(){
        synchronized (SpannableLinkMovementMethod.class){
            if(null == mInstance){
                mInstance = new SpannableLinkMovementMethod();
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
                    if(!(widget instanceof SpanClickAccess)){
                        Selection.removeSelection(buffer);
                    }
                    Selection.removeSelection(buffer);
                } else if (action == MotionEvent.ACTION_DOWN) {
                    //正是这个选择区域，会导致视图伸缩的显示bug
                    //当存在伸缩接口时不执行亮选操作
//                    Selection.setSelection(buffer,
//                            buffer.getSpanStart(link[0]),
//                            buffer.getSpanEnd(link[0]));
                    if(!(widget instanceof SpanClickAccess)){
                        Selection.setSelection(buffer,
                                buffer.getSpanStart(link[0]),
                                buffer.getSpanEnd(link[0]));
                    }
                }
                return true;
            } else {
                if(!(widget instanceof SpanClickAccess)){
                    Selection.removeSelection(buffer);
                }
                //不给执行，LinkMoveMethod的MoveEvent会让此TextView可滑动
                //用BaseMoveMethod后直接返回false了
//                super.onTouchEvent(widget, buffer, event);
                return false;
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }

}