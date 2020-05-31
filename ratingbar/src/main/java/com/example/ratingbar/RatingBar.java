package com.example.ratingbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class RatingBar extends LinearLayout implements View.OnTouchListener {

    private OnRatingListener onRatingListener;
    private Object bindObject;
    private float starImageSize;
    private Drawable starEmptyDrawable;
    private Drawable starFillDrawable;
    private int mStarCount;
    private Drawable starHalfDrawable;
    private float mPaddingVertical;
    private int mDefaultColor;
    private boolean mEnable = true;
    private boolean mHasColorFilter;

    private int currentRating = 0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                currentRating = getRatingNum(Math.min(Math.max(event.getX(), 0), getWidth()),
                        event.getY());
                updateStars();
                break;
            case MotionEvent.ACTION_UP:
                onRatingListener.onRating(this, toFloat());
                break;
        }
        return true;
    }

    private float toFloat() {
        float extra = currentRating % 2 == 0 ? 0f : 0.5f;
        return ((int) (currentRating / 2)) + extra;
    }

    private int getRatingNum(float x, float y) {
        float width = starImageSize + mPaddingVertical * 2;
//        final float parentWidth = getWidth();
        final int baseCount = ((int) (x / width));
        final float left = (x - (baseCount * width));
        final float starHalf = (width / 2);
        final int half = left > starHalf ? 2 : (left > (starHalf / 2)) ? 1 : 0;
        return half + baseCount * 2;
    }

    public interface OnRatingListener {
        void onRating(RatingBar v, float RatingScore);
    }

    public RatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.HORIZONTAL);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RatingBar);
        starImageSize = ta.getDimension(R.styleable.RatingBar_starImageSize, 42);
        mPaddingVertical = ta.getDimension(R.styleable.RatingBar_paddingVertical, 0);
        mDefaultColor = ta.getColor(R.styleable.RatingBar_starColor, Color.BLACK);
        mHasColorFilter = ta.hasValue(R.styleable.RatingBar_starColor);
        starEmptyDrawable = ta.getDrawable(R.styleable.RatingBar_starEmpty);
        if (starEmptyDrawable == null) {
            starEmptyDrawable = context.getResources().getDrawable(R.drawable.ic_star_border_black_24dp);
        }
        starHalfDrawable = ta.getDrawable(R.styleable.RatingBar_starHalf);
        if (starHalfDrawable == null) {
            starHalfDrawable = context.getResources().getDrawable(R.drawable.ic_star_half_black_24dp);
        }
        starFillDrawable = ta.getDrawable(R.styleable.RatingBar_starFill);
        if (starFillDrawable == null) {
            starFillDrawable = context.getResources().getDrawable(R.drawable.ic_star_black_24dp);
        }
        ta.recycle();

        for (int i = 0; i < 5; ++i) {
            ImageView imageView = getStarImageView(context, attrs);
            if (mHasColorFilter)
                imageView.setColorFilter(mDefaultColor);
            addView(imageView);
        }
        setOnTouchListener(this);
    }

    /**
     * 是否可以设置分数
     *
     * @param enable
     */
    public void setEnable(boolean enable) {
        mEnable = enable;
        setOnTouchListener(mEnable ? this : null);
    }

    private ImageView getStarImageView(Context context, AttributeSet attrs) {
        ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams para = new LinearLayout.LayoutParams(Math.round(starImageSize), Math.round(starImageSize));
        para.leftMargin = (int) mPaddingVertical;
        para.rightMargin = (int) mPaddingVertical;
        imageView.setLayoutParams(para);
        imageView.setImageDrawable(starEmptyDrawable);
        return imageView;
    }

    public void updateStars() {
        final int fulls = ((int) (currentRating / 2));
        final boolean hasHalf = !(currentRating % 2 == 0);
        for (int i = 0; i < 5; i++) {
            ImageView view = (ImageView) getChildAt(i);
            if (i < fulls) {
                view.setImageDrawable(starFillDrawable);
            } else if (i == fulls && hasHalf) {
                view.setImageDrawable(starHalfDrawable);
            } else {
                view.setImageDrawable(starEmptyDrawable);
            }
        }
    }

    public Drawable getStarHalfDrawable() {
        return starHalfDrawable;
    }

    public void setStarHalfDrawable(Drawable starHalfDrawable) {
        this.starHalfDrawable = starHalfDrawable;
    }

    public void setStarFillDrawable(Drawable starFillDrawable) {
        this.starFillDrawable = starFillDrawable;
    }

    public void setStarEmptyDrawable(Drawable starEmptyDrawable) {
        this.starEmptyDrawable = starEmptyDrawable;
    }

    /**
     * @param star 0.0 ~ 5.0
     */
    public void setStar(float star) {
        if (star < 5f && star > 0f) {
            float d = star * 2f;
            int tmpStar = ((int) d) + ((d % 1) > 0.5f ? 1 : 0);
            currentRating = Math.max(0, Math.min(tmpStar, 10));
            updateStars();
        }
    }

    /**
     * 这个回调，可以获取到用户评价给出的星星等级
     */
    public void setOnRatingListener(OnRatingListener onRatingListener) {
        this.onRatingListener = onRatingListener;
    }
}