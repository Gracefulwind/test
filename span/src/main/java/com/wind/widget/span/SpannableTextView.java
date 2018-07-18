package com.wind.widget.span;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by Gracefulwind Wang on 2018/7/6.
 * Email : Gracefulwindbigwang@gmail.com
 *
 * @author : Gracefulwind
 *
 *  之前在工程中，TextView在限定了最大行数与超链接后，非展开情况下点击超链接视图会出现显示bug(出现文字上移半行现象，第一行只能看到半行，最底下多出半行)，
 * 经研究发现是点击时触发了文字的高亮状态，重绘引起显示错误。而系统的LinkMoveMethod是可滑动的，所以虽然显示状态异常，但是可以通过滑动让文本完整显示出来。
 * 这个和Spannable的目的是相冲突的，所以这里关闭了MoveMethod的高亮以及滑动功能。如果对于非可展的TV，可以用包内的SpannableNormalMovementMethod来实现高亮功能(
 * 这里最好不要让此tv可scroll)
 *
 *
 */
public class SpannableTextView extends FrameLayout{


    private static final int[] STATE_EXPAND = new int[]{R.attr.expand};

    private Context mContext;

    private int expandLines;
    private static final int EXPAND_LINES = Integer.MAX_VALUE;

    //attrs
    private boolean expand = false;
    private SpannableContentView tvContent;
    private ImageView ivExpandIcon;
    private int textColor;
    private String textText;
    private float textSize;
    private int iconSrc;
    private float dimenCommon;
    private int linkColor;

    //默认可伸缩
    private boolean canExpand = true;
    private OnClickListener contentListener;

    private LinkClickListener linkClickListener;
    private int xmlHeight;
    private int xmlWidth;
    private int xmlMarginLeft;
    private int xmlMarginTop;
    private int xmlMarginRight;

    public SpannableTextView(Context context) {
        this(context, null);
    }

    public SpannableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public SpannableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        mContext = context;
        View inflate = LayoutInflater.from(context).inflate(layout(), this, true);
        tvContent = findViewById(R.id.stv_tv_content);
        ivExpandIcon = findViewById(R.id.stv_iv_expand_icon);

        if(null == attrs){
            return;
        }
        dimenCommon = context.getResources().getDimension(R.dimen.dimen_12sp);
        //==开始读取属性======
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SpannableTextView);
        //差一行，和MAX_LINE产生差异，让伸缩图标能正常变化
        expandLines = typedArray.getInt(R.styleable.SpannableTextView_expandLines, Integer.MAX_VALUE - 1);
        textText = typedArray.getString(R.styleable.SpannableTextView_text);
        textColor = typedArray.getColor(R.styleable.SpannableTextView_textColor, Color.BLACK);
        textSize = typedArray.getDimension(R.styleable.SpannableTextView_textSize, dimenCommon);

        linkColor = typedArray.getColor(R.styleable.SpannableTextView_textColor, getResources().getColor(R.color.color_default_link_text));
        //icon
        context.obtainStyledAttributes(attrs, R.styleable.SpannableTextView);
        expand = typedArray.getBoolean(R.styleable.SpannableTextView_expand, false);
        canExpand = typedArray.getBoolean(R.styleable.SpannableTextView_canExpand, true);
        iconSrc = typedArray.getResourceId(R.styleable.SpannableTextView_iconSrc, R.drawable.selector_icon);
        xmlHeight = typedArray.getLayoutDimension(R.styleable.SpannableTextView_IconHeight, -5);
        xmlWidth = typedArray.getLayoutDimension(R.styleable.SpannableTextView_IconWidth, -5);
        xmlMarginLeft = typedArray.getLayoutDimension(R.styleable.SpannableTextView_IconMarginLeft, -5);
        xmlMarginTop = typedArray.getLayoutDimension(R.styleable.SpannableTextView_IconMarginTop, -5);
        xmlMarginRight = typedArray.getLayoutDimension(R.styleable.SpannableTextView_IconMarginRight, -5);
        typedArray.recycle();
        //=====
        //==设置右上图标====
        setIcon(iconSrc);
        //==设置宽高==
        setIconLayout();
        //设置可点击文本
        setContextText();
        initListener();
    }

    private void initListener() {
        tvContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != contentListener){
                    contentListener.onClick(v);
                }
                if(canExpand){
                    changeExpandStatus();
                }
            }
        });
    }

    /**
     *
     * 就算在link高亮后调用这个方法，也只能控制控件高度，无法控制控件文本的显示情况。即使invalidate()也不行
     * */
    public void setExpandLines(int expandLines){
        this.expandLines = expandLines;
        int tempLines = tvContent.getMaxLines();
        //展开模式下不改变当前显示状态
        if(EXPAND_LINES == tempLines){
            return;
        }
        tvContent.setMaxLines(expandLines);
//        invalidate();
    }

    private void changeExpandStatus() {
        int maxLines = tvContent.getMaxLines();
        if(maxLines == expandLines){
            expand = true;
            tvContent.setMaxLines(EXPAND_LINES);
        }else {
            expand = false;
            tvContent.setMaxLines(expandLines);
        }
        refreshDrawableState();
    }

    private void setContextText() {
        CharSequence clickableHtml = getClickableHtml(textText);
        tvContent.setText(clickableHtml);
        tvContent.setMovementMethod(SpannableLinkMovementMethod.getInstance());
        tvContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        tvContent.setTextColor(textColor);
        if(expand){
            tvContent.setMaxLines(Integer.MAX_VALUE);
        }else {
            tvContent.setMaxLines(expandLines);
        }
    }

    public void setLinkColor(int color){
        linkColor = color;
        tvContent.invalidate();
    }

    /**
     *
     * setTextSize的默认单位是SP，但是我们代码中获取到的宽高等值都是(float)px
     *
     * */
    public void setTextSize(float textSize){
        this.textSize = textSize;
        tvContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        setIconLayout();
    }

//    ClickableSpan clickableSpan = new ClickableSpan() {
//        @Override
//        public void updateDrawState(TextPaint ds) {
//            // super.updateDrawState(ds);
//            ds.setUnderlineText(false); // 去除下划线
//            ds.setColor(linkColor);
//        }
//
//        @Override
//        public void onClick(View view) {
//            if(null != linkClickListener){
//                boolean expended = linkClickListener.onLinkClicked(SpannableTextView.this, urlSpan);
//                if(expended){
//                    return;
//                }
//            }
//            //默认操作，打开浏览器访问url
//            try{
//                String url = urlSpan.getURL();
//                Uri uri = Uri.parse(url);
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                getContext().startActivity(intent);
//            }catch (Exception excetion){
//                excetion.printStackTrace();
//                Toast.makeText(getContext(), "url异常，无法打开浏览器", Toast.LENGTH_SHORT).show();
//            }
//
//        }
//    };
    /**
     *
     * 设置自定义伸缩图标的大小、位置。
     * 如果不设置，则为默认值，且会随着字体的大小变化而自适应。
     * 如果设置了，则使用用户自定义的大小，不再自适应。
     * MATCH_PARENT和WRAP_CONTENT强烈不推荐。。。
     *
     * */
    private void setIconLayout() {
        //图标倍率，让图标随着文字大小适应
//        LinearLayout.LayoutParams.MATCH_PARENT = -1
//        LinearLayout.LayoutParams.WRAP_CONTENT = -2
        float rate = textSize / dimenCommon;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) ivExpandIcon.getLayoutParams();
        if(xmlHeight < 0){
            if(LayoutParams.MATCH_PARENT == xmlHeight){
                layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
            }else if(LayoutParams.WRAP_CONTENT == xmlHeight) {
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            }else {
                float defaultHeight = mContext.getResources().getDimension(R.dimen.dimen_6dp);
                layoutParams.height = (int) (rate * defaultHeight);
            }
        }else {
            layoutParams.height = xmlHeight;
        }
        if(xmlWidth < 0){
            if(LayoutParams.MATCH_PARENT == xmlWidth){
                layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
            }else if(LayoutParams.WRAP_CONTENT == xmlWidth) {
                layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            }else {
                float defaultWidth = mContext.getResources().getDimension(R.dimen.dimen_12dp);
                layoutParams.width = (int) (rate * defaultWidth);
            }
        }else {
            layoutParams.height =  xmlWidth;
        }
        if(xmlMarginLeft < 0){
            float defaultMarginLeft = mContext.getResources().getDimension(R.dimen.dimen_0dp);
            layoutParams.leftMargin = (int) (rate * defaultMarginLeft);
        }else {
            layoutParams.leftMargin = xmlMarginLeft;
        }
        if(xmlMarginTop < 0){
            float defaultMarginTop = mContext.getResources().getDimension(R.dimen.dimen_4dp);
            layoutParams.topMargin = (int) (rate * defaultMarginTop);
        }else {
            layoutParams.topMargin = xmlMarginTop;
        }
        if(xmlMarginRight < 0){
            float defaultMarginRight = mContext.getResources().getDimension(R.dimen.dimen_3dp);
            layoutParams.rightMargin = (int) (rate * defaultMarginRight);
        }else {
            layoutParams.rightMargin = xmlMarginRight;
        }
        ivExpandIcon.setLayoutParams(layoutParams);
    }


//    private void setIcon(int iconSrc) {
//        this.iconSrc = iconSrc;
//        refreshDrawableState();
//    }

    private void setIcon(int iconSrc) {
        ivExpandIcon.setVisibility(canExpand ? VISIBLE : GONE);
        ivExpandIcon.setImageResource(iconSrc);
    }


    public boolean getExpand(){
        return expand;
    }

    public void setCanExpand(boolean canExpand) {
        this.canExpand = canExpand;
        setIcon(iconSrc);
    }

    public boolean isCanExpand() {
        return canExpand;
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        if(expand){
            final int[] states = super.onCreateDrawableState(extraSpace + 1);
            mergeDrawableStates(states, STATE_EXPAND);
            return states;
        }else {
            return super.onCreateDrawableState(extraSpace);
        }
    }

    public void setExpand(boolean expand){
        System.out.println("pre expand=" + expand);
        if(this.expand != expand){
            changeExpandStatus();
        }
    }

    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
    }

    protected @LayoutRes int layout() {
        return R.layout.spannable_text_view;
    }
    //======

//    @Override
//    public void setSpanClicked(boolean clicked) {
//        linkClicked = true;
//    }
//
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


    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
        this.contentListener = l;
    }

    public void setLinkClickListener(LinkClickListener linkClickListener){
        this.linkClickListener = linkClickListener;
    }

    /**
     *
     * 设置url同时设置url点击事件，
     * 如果不需要修改点击事件，请看:
     * @see #setText(CharSequence)
     *
     * */
    public void setText(CharSequence charSeq, LinkClickListener linkClickListener){
        this.linkClickListener = linkClickListener;
        setText(charSeq);
    }

    public void setText(CharSequence charSeq){
        tvContent.setText(getClickableHtml(charSeq));
//        SpannableString text = (SpannableString) tvContent.getText();
//        tvContent.setText(text);
//        System.out.println("======");
    }

    /**
     * 格式化超链接文本内容并设置点击处理
     */
    public CharSequence getClickableHtml(CharSequence html) {
        if(null == html){
            html = "";
        }
        Spanned spannedHtml = Html.fromHtml(html.toString());
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(spannedHtml);
        URLSpan[] urls = spannableStringBuilder.getSpans(0, spannedHtml.length(), URLSpan.class);
        for (final URLSpan span : urls) {
            setLinkClickable(spannableStringBuilder, span);
        }
        return spannableStringBuilder;
    }

    /**
     * 设置点击超链接对应的处理内容
     */
    public void setLinkClickable(final SpannableStringBuilder clickableHtmlBuilder,  final URLSpan urlSpan) {
        int start = clickableHtmlBuilder.getSpanStart(urlSpan);
        int end = clickableHtmlBuilder.getSpanEnd(urlSpan);
        int flags = clickableHtmlBuilder.getSpanFlags(urlSpan);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                // super.updateDrawState(ds);
                ds.setUnderlineText(false); // 去除下划线
                ds.setColor(linkColor);
            }

            @Override
            public void onClick(View view) {
                if(null != linkClickListener){
                    boolean expended = linkClickListener.onLinkClicked(SpannableTextView.this, urlSpan);
                    if(expended){
                        return;
                    }
                }
                //默认操作，打开浏览器访问url
                try{
                    String url = urlSpan.getURL();
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                }catch (Exception excetion){
                    excetion.printStackTrace();
                    Toast.makeText(getContext(), "url异常，无法打开浏览器", Toast.LENGTH_SHORT).show();
                }

            }
        };
        //将可点span替换掉原内容
        clickableHtmlBuilder.setSpan(clickableSpan, start, end, flags);
        clickableHtmlBuilder.removeSpan(urlSpan);
    }

    /**
     *
     * URL点击事件接口
     *
     * */
    public interface LinkClickListener {
        boolean onLinkClicked(SpannableTextView view, URLSpan urlSpan);
    }

}
