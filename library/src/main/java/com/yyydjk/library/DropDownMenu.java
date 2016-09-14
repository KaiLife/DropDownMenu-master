package com.yyydjk.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;


/**
 * Created by dongjunkun on 2015/6/17.
 */
public class DropDownMenu extends LinearLayout {

    //顶部菜单布局
    private LinearLayout tabMenuView;
    //底部容器，包含popupMenuViews，maskView
    private FrameLayout containerView;
    //弹出菜单父布局
    private FrameLayout popupMenuViews;
    //遮罩半透明View，点击可关闭DropDownMenu
    private View maskView;
    //tabMenuView里面选中的tab位置，-1表示未选中
    private int current_tab_position = -1;

    //分割线颜色
    private int dividerColor = 0xffcccccc;
    //tab选中颜色
    private int textSelectedColor = 0xff890c85;
    //tab未选中颜色
    private int textUnselectedColor = 0xff111111;
    //遮罩颜色
    private int maskColor = 0x88888888;
    //tab字体大小
    private int menuTextSize = 14;

    //tab的高度
    private int menuHeight = 54;

    //tab选中图标
    private int menuSelectedIcon;
    //tab未选中图标
    private int menuUnselectedIcon;

    private int underlineColor;
    private int underlineIndicatorColor;

    public DropDownMenu(Context context) {
        super(context, null);
    }

    public DropDownMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DropDownMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);

        //为DropDownMenu添加自定义属性
        int menuBackgroundColor = 0xffffffff;
        underlineColor = 0xffcccccc;
        underlineIndicatorColor = 0xff30a8ff;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DropDownMenu);
        underlineColor = a.getColor(R.styleable.DropDownMenu_ddunderlineColor, underlineColor);
        underlineIndicatorColor = a.getColor(R.styleable.DropDownMenu_ddunderlineIndicatorColor, underlineIndicatorColor);
        dividerColor = a.getColor(R.styleable.DropDownMenu_dddividerColor, dividerColor);
        textSelectedColor = a.getColor(R.styleable.DropDownMenu_ddtextSelectedColor, textSelectedColor);
        textUnselectedColor = a.getColor(R.styleable.DropDownMenu_ddtextUnselectedColor, textUnselectedColor);
        menuBackgroundColor = a.getColor(R.styleable.DropDownMenu_ddmenuBackgroundColor, menuBackgroundColor);
        maskColor = a.getColor(R.styleable.DropDownMenu_ddmaskColor, maskColor);
        menuTextSize = a.getDimensionPixelSize(R.styleable.DropDownMenu_ddmenuTextSize, menuTextSize);
        menuHeight = a.getDimensionPixelSize(R.styleable.DropDownMenu_ddmenuHeight, menuHeight);
        menuSelectedIcon = a.getResourceId(R.styleable.DropDownMenu_ddmenuSelectedIcon, menuSelectedIcon);
        menuUnselectedIcon = a.getResourceId(R.styleable.DropDownMenu_ddmenuUnselectedIcon, menuUnselectedIcon);
        a.recycle();

        //初始化tabMenuView并添加到tabMenuView
        tabMenuView = new LinearLayout(context);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, menuHeight);
        tabMenuView.setOrientation(HORIZONTAL);
        tabMenuView.setBackgroundColor(menuBackgroundColor);
        tabMenuView.setLayoutParams(params);
        addView(tabMenuView, 0);

        //初始化containerView并将其添加到DropDownMenu
        containerView = new FrameLayout(context);
        containerView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        addView(containerView, 1);
    }

    /**
     * 初始化DropDownMenu
     *
     * @param tabTexts
     * @param popupViews
     * @param contentView
     */
    public void setDropDownMenu(@NonNull List<String> tabTexts, @NonNull List<View> popupViews, @NonNull View contentView, float dividerMargin) {
        if (tabTexts.size() != popupViews.size()) {
            throw new IllegalArgumentException("params not match, tabTexts.size() should be equal popupViews.size()");
        }

        for (int i = 0; i < tabTexts.size(); i++) {
            addTab(tabTexts, i, dividerMargin);
        }
        containerView.addView(contentView, 0);

        maskView = new View(getContext());
        maskView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        maskView.setBackgroundColor(maskColor);
        maskView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
            }
        });
        containerView.addView(maskView, 1);
        maskView.setVisibility(GONE);

        popupMenuViews = new FrameLayout(getContext());
        popupMenuViews.setVisibility(GONE);
        containerView.addView(popupMenuViews, 2);

        for (int i = 0; i < popupViews.size(); i++) {
            popupViews.get(i).setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            popupMenuViews.addView(popupViews.get(i), i);
        }
    }

    private void addTab(@NonNull List<String> tabTexts, int i, float margin) {
        final RelativeLayout tab = new RelativeLayout(getContext());
        tab.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView textView = new TextView(getContext());
        textView.setId(R.id.drop_down_menu_tab_tv_content);
        textView.setSingleLine();
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, menuTextSize);
        RelativeLayout.LayoutParams textViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        textViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        textViewParams.setMargins(0, dpTpPx(12), dpTpPx(4f), dpTpPx(12));
        textView.setLayoutParams(textViewParams);
        textView.setTextColor(textUnselectedColor);
        textView.setText(tabTexts.get(i));
        tab.addView(textView, 0);

        ImageView imageView = new ImageView(getContext());
        RelativeLayout.LayoutParams imageViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        imageViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        imageViewParams.addRule(RelativeLayout.RIGHT_OF, R.id.drop_down_menu_tab_tv_content);
        imageView.setLayoutParams(imageViewParams);
        imageView.setImageResource(menuUnselectedIcon);
        tab.addView(imageView, 1);

        View underLine = new View(getContext());
        RelativeLayout.LayoutParams underLineParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, dpTpPx(1.0f));
        underLineParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        underLine.setLayoutParams(underLineParams);
        underLine.setBackgroundColor(underlineColor);
        tab.addView(underLine, 2);

        tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchMenu(tab);
            }
        });

        tabMenuView.addView(tab);
        //添加分割线
        LinearLayout layout = null;
        View view = null;
        View underView = null;
        LayoutParams params = null;
        if (i < tabTexts.size() - 1) {
            layout = new LinearLayout(getContext());
            layout.setOrientation(VERTICAL);
            layout.setLayoutParams(new LayoutParams(dpTpPx(0.5f), ViewGroup.LayoutParams.MATCH_PARENT));

            view = new View(getContext());
            params = new LayoutParams(dpTpPx(0.5f), 0, 1.0f);
            params.setMargins(0, dpTpPx(margin), 0, dpTpPx(margin - 1));
            view.setLayoutParams(params);
            view.setBackgroundColor(dividerColor);
            layout.addView(view);

            underView = new View(getContext());
            underView.setLayoutParams(new LayoutParams(dpTpPx(0.5f), dpTpPx(1f)));
            underView.setBackgroundColor(underlineColor);
            layout.addView(underView);

            tabMenuView.addView(layout);
        }
    }

    /**
     * 改变tab文字
     *
     * @param text
     */
    public void setTabText(String text) {
        if (current_tab_position != -1) {
            ((TextView) ((RelativeLayout) tabMenuView.getChildAt(current_tab_position)).getChildAt(0)).setText(text);
        }
    }

//    public void setTabClickable(boolean clickable) {
//        for (int i = 0; i < tabMenuView.getChildCount(); i = i + 2) {
//            tabMenuView.getChildAt(i).setClickable(clickable);
//        }
//    }

    /**
     * 关闭菜单
     */
    public void closeMenu() {
        if (current_tab_position != -1) {
            ((TextView) ((RelativeLayout) tabMenuView.getChildAt(current_tab_position)).getChildAt(0)).setTextColor(textUnselectedColor);
            ((ImageView) ((RelativeLayout) tabMenuView.getChildAt(current_tab_position)).getChildAt(1)).setImageResource(menuUnselectedIcon);
            ((RelativeLayout) tabMenuView.getChildAt(current_tab_position)).getChildAt(2).setBackgroundColor(underlineColor);
            popupMenuViews.setVisibility(View.GONE);
            popupMenuViews.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_out));
            maskView.setVisibility(GONE);
            maskView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
            current_tab_position = -1;
        }
    }

    /**
     * DropDownMenu是否处于可见状态
     *
     * @return
     */
    public boolean isShowing() {
        return current_tab_position != -1;
    }

    /**
     * 切换菜单
     *
     * @param target
     */
    private void switchMenu(View target) {
        for (int i = 0; i < tabMenuView.getChildCount(); i = i + 2) {
            if (target == tabMenuView.getChildAt(i)) {
                if (current_tab_position == i) {
                    closeMenu();
                } else {
                    if (current_tab_position == -1) {
                        popupMenuViews.setVisibility(View.VISIBLE);
                        popupMenuViews.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_in));
                        maskView.setVisibility(VISIBLE);
                        maskView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_in));
                        popupMenuViews.getChildAt(i / 2).setVisibility(View.VISIBLE);
                    } else {
                        popupMenuViews.getChildAt(i / 2).setVisibility(View.VISIBLE);
                    }
                    current_tab_position = i;
                    ((TextView) ((RelativeLayout) tabMenuView.getChildAt(i)).getChildAt(0)).setTextColor(textSelectedColor);
                    ((ImageView) ((RelativeLayout) tabMenuView.getChildAt(i)).getChildAt(1)).setImageResource(menuSelectedIcon);
                    ((RelativeLayout) tabMenuView.getChildAt(i)).getChildAt(2).setBackgroundColor(underlineIndicatorColor);
                }
            } else {
                ((TextView) ((RelativeLayout) tabMenuView.getChildAt(i)).getChildAt(0)).setTextColor(textUnselectedColor);
                ((ImageView) ((RelativeLayout) tabMenuView.getChildAt(i)).getChildAt(1)).setImageResource(menuUnselectedIcon);
                ((RelativeLayout) tabMenuView.getChildAt(i)).getChildAt(2).setBackgroundColor(underlineColor);
                popupMenuViews.getChildAt(i / 2).setVisibility(View.GONE);
            }
        }
    }

    public int dpTpPx(float value) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, dm) + 0.5);
    }
}
