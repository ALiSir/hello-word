package com.softwinner.TvdFileManager;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.utils.CornerUtils;
import com.flyco.dialog.widget.base.BaseDialog;

/**
 * Created by eafoon on 16-1-8.
 */
public abstract class CustomLayoutAlertDialog extends BaseDialog<CustomLayoutAlertDialog>
	implements OnFocusChangeListener {

    /** container */
    protected LinearLayout mLlContainer;
    //title
    /** title */
    protected TextView mTvTitle;
    /** title content(标题) */
    protected String mTitle;
    /** title textcolor(标题颜色) */
    protected int mTitleTextColor;
    /** title textsize(标题字体大小,单位sp) */
    protected float mTitleTextSize;
    /** enable title show(是否显示标题) */
    protected boolean mIsTitleShow = true;

    //content
    /** content */
    protected TextView mTvContent;
    /** content text */
    protected String mContent;
    /** show gravity of content(正文内容显示位置) */
    protected int mContentGravity = Gravity.CENTER_VERTICAL;
    /** content textcolor(正文字体颜色) */
    protected int mContentTextColor;
    /** content textsize(正文字体大小) */
    protected float mContentTextSize;

    //btns
    /** num of btns, [1,3] */
    protected int mBtnNum = 2;
    /** btn container */
    protected LinearLayout mLlBtns;
    /** btns */
    protected TextView mTvBtnLeft;
    protected TextView mTvBtnRight;
    protected TextView mTvBtnMiddle;
    /** btn text(按钮内容) */
    protected String mBtnLeftText = "确定";
    protected String mBtnRightText = "取消";
    protected String mBtnMiddleText = "继续";
    /** btn textcolor(按钮字体颜色) */
    protected int mLeftBtnTextColor;
    protected int mRightBtnTextColor;
    protected int mMiddleBtnTextColor;
    /** btn textsize(按钮字体大小) */
    protected float mLeftBtnTextSize = 15f;
    protected float mRightBtnTextSize = 15f;
    protected float mMiddleBtnTextSize = 15f;
    /** btn press color(按钮点击颜色) */
    protected int mBtnPressColor = Color.parseColor("#E3E3E3");// #85D3EF,#ffcccccc,#E3E3E3
    /** left btn click listener(左按钮接口) */
    protected OnBtnClickL mOnBtnLeftClickL;
    /** right btn click listener(右按钮接口) */
    protected OnBtnClickL mOnBtnRightClickL;
    /** middle btn click listener(右按钮接口) */
    protected OnBtnClickL mOnBtnMiddleClickL;

    /** corner radius,dp(圆角程度,单位dp) */
    protected float mCornerRadius = 3;
    /** background color(背景颜色) */
    protected int mBgColor = Color.parseColor("#ffffff");

    /** title underline */
    private View mVLineTitle;
    /** vertical line between btns */
    private View mVLineVertical;
    /** vertical line between btns */
    private View mVLineVertical2;
    /** horizontal line above btns */
    private View mVLineHorizontal;
    /** title underline color(标题下划线颜色) */
    private int mTitleLineColor = Color.parseColor("#61AEDC");
    /** title underline height(标题下划线高度) */
    private float mTitleLineHeight = 1f;
    /** btn divider line color(对话框之间的分割线颜色(水平+垂直)) */
    private int mDividerColor = Color.parseColor("#DCDCDC");

    public static final int STYLE_ONE = 0;
    public static final int STYLE_TWO = 1;
    private int mStyle = STYLE_ONE;

    public CustomLayoutAlertDialog(Context context) {
        super(context, true);
        //widthScale(0.5f);

        mLlContainer = new LinearLayout(context);
        mLlContainer.setOrientation(LinearLayout.VERTICAL);

        /** title */
        mTvTitle = new TextView(context);

        /** content */
        mTvContent = new TextView(context);

        /**btns*/
        mLlBtns = new LinearLayout(context);
        mLlBtns.setOrientation(LinearLayout.HORIZONTAL);

        mTvBtnLeft = new TextView(context);
        mTvBtnLeft.setGravity(Gravity.CENTER);

        mTvBtnMiddle = new TextView(context);
        mTvBtnMiddle.setGravity(Gravity.CENTER);

        mTvBtnRight = new TextView(context);
        mTvBtnRight.setGravity(Gravity.CENTER);

        /** default value*/
        mTitleTextColor = Color.parseColor("#61AEDC");
        mTitleTextSize = 22f;
        mContentTextColor = Color.parseColor("#383838");
        mContentTextSize = 17f;
        mLeftBtnTextColor = Color.parseColor("#8a000000");
        mRightBtnTextColor = Color.parseColor("#8a000000");
        mMiddleBtnTextColor = Color.parseColor("#8a000000");
        /** default value*/
    }

    public abstract View onCreateContentView();

    @Override
    public View onCreateView() {
        /** title */
        mTvTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mLlContainer.addView(mTvTitle);

        /** title underline */
        mVLineTitle = new View(mContext);
        mLlContainer.addView(mVLineTitle);

        /** content */
        //mTvContent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
        //        LinearLayout.LayoutParams.WRAP_CONTENT));
        View contentView = onCreateContentView();
        contentView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                  LinearLayout.LayoutParams.WRAP_CONTENT));
        mLlContainer.addView(contentView);

        mVLineHorizontal = new View(mContext);
        mVLineHorizontal.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        mLlContainer.addView(mVLineHorizontal);

        /** btns */
        mTvBtnLeft.setLayoutParams(new LinearLayout.LayoutParams(0, dp2px(45), 1));
        mTvBtnLeft.setFocusable(true);
        mTvBtnLeft.setTag("Left");
        mTvBtnLeft.setOnFocusChangeListener(this);
        mLlBtns.addView(mTvBtnLeft);

        mVLineVertical = new View(mContext);
        mVLineVertical.setLayoutParams(new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT));
        mLlBtns.addView(mVLineVertical);

        mTvBtnMiddle.setLayoutParams(new LinearLayout.LayoutParams(0, dp2px(45), 1));
        mTvBtnMiddle.setFocusable(true);
        mTvBtnMiddle.setTag("Middle");
        mTvBtnMiddle.setOnFocusChangeListener(this);
        mLlBtns.addView(mTvBtnMiddle);

        mVLineVertical2 = new View(mContext);
        mVLineVertical2.setLayoutParams(new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT));
        mLlBtns.addView(mVLineVertical2);

        mTvBtnRight.setLayoutParams(new LinearLayout.LayoutParams(0, dp2px(45), 1));
        mTvBtnRight.setFocusable(true);
        mTvBtnRight.setTag("Right");
        mTvBtnRight.setOnFocusChangeListener(this);
        mLlBtns.addView(mTvBtnRight);

        mLlContainer.addView(mLlBtns);

        return mLlContainer;
    }

    @Override
    public void setUiBeforShow() {
        /** title */
        mTvTitle.setVisibility(mIsTitleShow ? View.VISIBLE : View.GONE);

        mTvTitle.setText(TextUtils.isEmpty(mTitle) ? "温馨提示" : mTitle);
        mTvTitle.setTextColor(mTitleTextColor);
        mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTitleTextSize);

        /** content */
        mTvContent.setGravity(mContentGravity);
        mTvContent.setText(mContent);
        mTvContent.setTextColor(mContentTextColor);
        mTvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, mContentTextSize);
        mTvContent.setLineSpacing(0, 1.3f);

        /**btns*/
        mTvBtnLeft.setText(mBtnLeftText);
        mTvBtnRight.setText(mBtnRightText);
        mTvBtnMiddle.setText(mBtnMiddleText);

        mTvBtnLeft.setTextColor(mLeftBtnTextColor);
        mTvBtnRight.setTextColor(mRightBtnTextColor);
        mTvBtnMiddle.setTextColor(mMiddleBtnTextColor);

        mTvBtnLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, mLeftBtnTextSize);
        mTvBtnRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, mRightBtnTextSize);
        mTvBtnMiddle.setTextSize(TypedValue.COMPLEX_UNIT_SP, mMiddleBtnTextSize);

        if (mBtnNum == 1) {
            mTvBtnLeft.setVisibility(View.GONE);
            mTvBtnRight.setVisibility(View.GONE);
        } else if (mBtnNum == 2) {
            mTvBtnMiddle.setVisibility(View.GONE);
        }

        mTvBtnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnBtnLeftClickL != null) {
                    mOnBtnLeftClickL.onBtnClick();
                } else {
                    dismiss();
                }
            }
        });

        mTvBtnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnBtnRightClickL != null) {
                    mOnBtnRightClickL.onBtnClick();
                } else {
                    dismiss();
                }
            }
        });

        mTvBtnMiddle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnBtnMiddleClickL != null) {
                    mOnBtnMiddleClickL.onBtnClick();
                } else {
                    dismiss();
                }
            }
        });

        /** title */
        if (mStyle == STYLE_ONE) {
            mTvTitle.setMinHeight(dp2px(48));
            mTvTitle.setGravity(Gravity.CENTER_VERTICAL);
            mTvTitle.setPadding(dp2px(15), dp2px(5), dp2px(0), dp2px(5));
            mTvTitle.setVisibility(mIsTitleShow ? View.VISIBLE : View.GONE);
        } else if (mStyle == STYLE_TWO) {
            mTvTitle.setGravity(Gravity.CENTER);
            mTvTitle.setPadding(dp2px(0), dp2px(15), dp2px(0), dp2px(0));
        }

        /** title underline */
        mVLineTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                dp2px(mTitleLineHeight)));
        mVLineTitle.setBackgroundColor(mTitleLineColor);
        mVLineTitle.setVisibility(mIsTitleShow && mStyle == STYLE_ONE ? View.VISIBLE : View.GONE);

        /** content */
        if (mStyle == STYLE_ONE) {
            mTvContent.setPadding(dp2px(15), dp2px(10), dp2px(15), dp2px(10));
            mTvContent.setMinHeight(dp2px(68));
            mTvContent.setGravity(mContentGravity);
        } else if (mStyle == STYLE_TWO) {
            mTvContent.setPadding(dp2px(15), dp2px(7), dp2px(15), dp2px(20));
            mTvContent.setMinHeight(dp2px(56));
            mTvContent.setGravity(Gravity.CENTER);
        }

        /** btns */
        mVLineHorizontal.setBackgroundColor(mDividerColor);
        mVLineVertical.setBackgroundColor(mDividerColor);
        mVLineVertical2.setBackgroundColor(mDividerColor);

        if (mBtnNum == 1) {
            mTvBtnLeft.setVisibility(View.GONE);
            mTvBtnRight.setVisibility(View.GONE);
            mVLineVertical.setVisibility(View.GONE);
            mVLineVertical2.setVisibility(View.GONE);
        } else if (mBtnNum == 2) {
            mTvBtnMiddle.setVisibility(View.GONE);
            mVLineVertical.setVisibility(View.GONE);
        }

        /**set background color and corner radius */
        float radius = dp2px(mCornerRadius);
        mLlContainer.setBackgroundDrawable(CornerUtils.cornerDrawable(mBgColor, radius));
        mTvBtnLeft.setBackgroundDrawable(CornerUtils.btnSelector(radius, mBgColor, mBtnPressColor, 0));
        mTvBtnRight.setBackgroundDrawable(CornerUtils.btnSelector(radius, mBgColor, mBtnPressColor, 1));
        mTvBtnMiddle.setBackgroundDrawable(CornerUtils.btnSelector(mBtnNum == 1 ? radius : 0, mBgColor, mBtnPressColor, -1));
    }
    
    @Override
    public void onFocusChange(View view, boolean hasFocus) {
    	if(hasFocus) {
    		if(view.getTag().equals("Left")) {
    			mTvBtnLeft.setBackgroundColor(Color.parseColor("#61AEDC"));
    		} else if(view.getTag().equals("Middle")) {
    			mTvBtnMiddle.setBackgroundColor(Color.parseColor("#61AEDC"));
    		} else if(view.getTag().equals("Right")) {
    			mTvBtnRight.setBackgroundColor(Color.parseColor("#61AEDC"));
    		}
    	} else {
    		if(view.getTag().equals("Left")) {
    			mTvBtnLeft.setBackgroundColor(Color.parseColor("#FFFFFF"));
    		} else if(view.getTag().equals("Middle")) {
    			mTvBtnMiddle.setBackgroundColor(Color.parseColor("#FFFFFF"));
    		} else if(view.getTag().equals("Right")) {
    			mTvBtnRight.setBackgroundColor(Color.parseColor("#FFFFFF"));
    		}
    	}
    }

    // --->属性设置

    /** set style(设置style) */
    public CustomLayoutAlertDialog style(int style) {
        this.mStyle = style;
        return this;
    }

    /** set title underline color(设置标题下划线颜色) */
    public CustomLayoutAlertDialog titleLineColor(int titleLineColor) {
        this.mTitleLineColor = titleLineColor;
        return this;
    }

    /** set title underline height(设置标题下划线高度) */
    public CustomLayoutAlertDialog titleLineHeight(float titleLineHeight_DP) {
        this.mTitleLineHeight = titleLineHeight_DP;
        return this;
    }

    /** set divider color between btns(设置btn分割线的颜色) */
    public CustomLayoutAlertDialog dividerColor(int dividerColor) {
        this.mDividerColor = dividerColor;
        return this;
    }

    /** set title text(设置标题内容) @return MaterialDialog */
    public CustomLayoutAlertDialog title(String title) {
        mTitle = title;
        return this;
    }

    /** set title textcolor(设置标题字体颜色) */
    public CustomLayoutAlertDialog titleTextColor(int titleTextColor) {
        mTitleTextColor = titleTextColor;
        return this;
    }

    /** set title textsize(设置标题字体大小) */
    public CustomLayoutAlertDialog titleTextSize(float titleTextSize_SP) {
        mTitleTextSize = titleTextSize_SP;
        return this;
    }

    /** enable title show(设置标题是否显示) */
    public CustomLayoutAlertDialog isTitleShow(boolean isTitleShow) {
        mIsTitleShow = isTitleShow;
        return this;
    }

    /** set content text(设置正文内容) */
    public CustomLayoutAlertDialog content(String content) {
        mContent = content;
        return this;
    }

    /** set content gravity(设置正文内容,显示位置) */
    public CustomLayoutAlertDialog contentGravity(int contentGravity) {
        mContentGravity = contentGravity;
        return this;
    }

    /** set content textcolor(设置正文字体颜色) */
    public CustomLayoutAlertDialog contentTextColor(int contentTextColor) {
        mContentTextColor = contentTextColor;
        return this;
    }

    /** set content textsize(设置正文字体大小,单位sp) */
    public CustomLayoutAlertDialog contentTextSize(float contentTextSize_SP) {
        mContentTextSize = contentTextSize_SP;
        return this;
    }

    /**
     * set btn text(设置按钮文字内容)
     * btnTexts size 1, middle
     * btnTexts size 2, left right
     * btnTexts size 3, left right middle
     */
    public CustomLayoutAlertDialog btnNum(int btnNum) {
        if (btnNum < 1 || btnNum > 3) {
            throw new IllegalStateException("btnNum is [1,3]!");
        }
        mBtnNum = btnNum;

        return this;
    }

    /**
     * set btn text(设置按钮文字内容)
     * btnTexts size 1, middle
     * btnTexts size 2, left right
     * btnTexts size 3, left right middle
     */
    public CustomLayoutAlertDialog btnText(String... btnTexts) {
        if (btnTexts.length < 1 || btnTexts.length > 3) {
            throw new IllegalStateException(" range of param btnTexts length is [1,3]!");
        }

        if (btnTexts.length == 1) {
            mBtnMiddleText = btnTexts[0];
        } else if (btnTexts.length == 2) {
            mBtnLeftText = btnTexts[0];
            mBtnRightText = btnTexts[1];
        } else if (btnTexts.length == 3) {
            mBtnLeftText = btnTexts[0];
            mBtnRightText = btnTexts[1];
            mBtnMiddleText = btnTexts[2];
        }

        return this;
    }

    /**
     * set btn textcolor(设置按钮字体颜色)
     * btnTextColors size 1, middle
     * btnTextColors size 2, left right
     * btnTextColors size 3, left right middle
     */
    public CustomLayoutAlertDialog btnTextColor(int... btnTextColors) {
        if (btnTextColors.length < 1 || btnTextColors.length > 3) {
            throw new IllegalStateException(" range of param textColors length is [1,3]!");
        }

        if (btnTextColors.length == 1) {
            mMiddleBtnTextColor = btnTextColors[0];
        } else if (btnTextColors.length == 2) {
            mLeftBtnTextColor = btnTextColors[0];
            mRightBtnTextColor = btnTextColors[1];
        } else if (btnTextColors.length == 3) {
            mLeftBtnTextColor = btnTextColors[0];
            mRightBtnTextColor = btnTextColors[1];
            mMiddleBtnTextColor = btnTextColors[2];
        }

        return this;
    }

    /**
     * set btn textsize(设置字体大小,单位sp)
     * btnTextSizes size 1, middle
     * btnTextSizes size 2, left right
     * btnTextSizes size 3, left right middle
     */
    public CustomLayoutAlertDialog btnTextSize(float... btnTextSizes) {
        if (btnTextSizes.length < 1 || btnTextSizes.length > 3) {
            throw new IllegalStateException(" range of param btnTextSizes length is [1,3]!");
        }

        if (btnTextSizes.length == 1) {
            mMiddleBtnTextSize = btnTextSizes[0];
        } else if (btnTextSizes.length == 2) {
            mLeftBtnTextSize = btnTextSizes[0];
            mRightBtnTextSize = btnTextSizes[1];
        } else if (btnTextSizes.length == 3) {
            mLeftBtnTextSize = btnTextSizes[0];
            mRightBtnTextSize = btnTextSizes[1];
            mMiddleBtnTextSize = btnTextSizes[2];
        }

        return this;
    }

    /** set btn press color(设置按钮点击颜色) */
    public CustomLayoutAlertDialog btnPressColor(int btnPressColor) {
        mBtnPressColor = btnPressColor;
        return this;
    }

    /** set corner radius (设置圆角程度) */
    public CustomLayoutAlertDialog cornerRadius(float cornerRadius_DP) {
        mCornerRadius = cornerRadius_DP;
        return this;
    }

    /** set backgroud color(设置背景色) */
    public CustomLayoutAlertDialog bgColor(int bgColor) {
        mBgColor = bgColor;
        return this;
    }

    /**
     * set btn click listener(设置按钮监听事件)
     * onBtnClickLs size 1, middle
     * onBtnClickLs size 2, left right
     * onBtnClickLs size 3, left right middle
     */
    public void setOnBtnClickL(OnBtnClickL... onBtnClickLs) {
        if (onBtnClickLs.length < 1 || onBtnClickLs.length > 3) {
            throw new IllegalStateException(" range of param onBtnClickLs length is [1,3]!");
        }

        if (onBtnClickLs.length == 1) {
            mOnBtnMiddleClickL = onBtnClickLs[0];
        } else if (onBtnClickLs.length == 2) {
            mOnBtnLeftClickL = onBtnClickLs[0];
            mOnBtnRightClickL = onBtnClickLs[1];
        } else if (onBtnClickLs.length == 3) {
            mOnBtnLeftClickL = onBtnClickLs[0];
            mOnBtnRightClickL = onBtnClickLs[1];
            mOnBtnMiddleClickL = onBtnClickLs[2];
        }
    }
}
