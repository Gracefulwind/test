package com.wind.widget.span;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Gracefulwind Wang on 2018/7/10.
 * Email : Gracefulwindbigwang@gmail.com
 *
 * @author : Gracefulwind
 */
public class SpannableContentView extends android.support.v7.widget.AppCompatTextView implements SpanClickAccess/*,ExpandableAccess*/ {

//    private boolean linkClicked = false;
//    private boolean expandable = true;

    public SpannableContentView(Context context) {
        super(context);
    }

    public SpannableContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpannableContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    @Override
//    public boolean performClick() {
//        //如果触发了span的事件了，则直接消费控件的点击事件
//        if(linkClicked){
//            return true;
//        }
//        return super.performClick();
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        //先触发touch.ACTION_UP，然后判断出是点击事件才会触发performClick
//        if(MotionEvent.ACTION_UP == event.getAction()){
//            linkClicked = false;
//        }
////        linkClicked = false;
//        return super.onTouchEvent(event);
//    }
//
//    @Override
//    public void setSpanClicked(boolean clicked) {
//        linkClicked = clicked;
//    }
//
//    @Override
//    public void setExpandable(boolean canExpand) {
//        expandable = canExpand;
//    }
//
//    @Override
//    public boolean canExpand() {
//        return expandable;
//    }
}
