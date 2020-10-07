package com.mgt.zalo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.RenderMode;
import com.mgt.zalo.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.annotation.Nullable;

public class FaceView extends FrameLayout {
    public static final int POSITION_TOP_START = 0;
    public static final int POSITION_TOP_END = 1;
    public static final int POSITION_BOTTOM_START = 2;
    public static final int POSITION_BOTTOM_END = 3;
    private static final int DEFAULT_CLOSE_BUTTON_SIZE = 80;
    private static final int DEFAULT_CLICK_ANIM_SIZE = 120;
    private static final boolean DEFAULT_IS_CLICK_ANIM_SHOW = false;
    private static final int DEFAULT_CLOSE_BUTTON_POSITION = POSITION_TOP_END;
    private static final float DEFAULT_BORDER_WIDTH = 10;
    private static final int DEFAULT_BORDER_COLOR = Color.WHITE;
    private static final float DEFAULT_BORDER_CORNERS_RADIUS = 30;
    private ImageView closeImgView;
    private BorderView borderView;
    private LottieAnimationView clickAnimView;
    private int closeButtonPosition;
    private int closeButtonSize;
    private Drawable closeButtonDrawable;
    private int borderColor;
    private float borderWidth;
    private float borderCornerRadius;
    private boolean isClickAnimLoaded = false;
    private int clickAnimSize;
    private boolean isClickAnimShown;

    public FaceView(Context context) {
        super(context);
        initDefaultValues(context);
        initFaceView(context);
    }

    public FaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceView(Context context,
                    AttributeSet attrs,
                    int defStyle) {
        super(context, attrs, defStyle);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FaceView);

        borderColor = typedArray.getColor(R.styleable.FaceView_borderColor, DEFAULT_BORDER_COLOR);
        borderWidth = typedArray.getDimension(R.styleable.FaceView_borderWidth, DEFAULT_BORDER_WIDTH);
        borderCornerRadius = typedArray.getDimension(R.styleable.FaceView_cornerRadius, DEFAULT_BORDER_CORNERS_RADIUS);
        closeButtonSize = (int) typedArray.getDimension(R.styleable.FaceView_closeButtonSize, DEFAULT_CLOSE_BUTTON_SIZE);
        closeButtonPosition = typedArray.getInt(R.styleable.FaceView_closePosition, DEFAULT_CLOSE_BUTTON_POSITION);
        closeButtonDrawable = typedArray.getDrawable(R.styleable.FaceView_closeButtonSrc);
        clickAnimSize = (int)typedArray.getDimension(R.styleable.FaceView_clickAnimSize, DEFAULT_CLICK_ANIM_SIZE);
        isClickAnimShown = typedArray.getBoolean(R.styleable.FaceView_showClickAnim, DEFAULT_IS_CLICK_ANIM_SHOW);

        typedArray.recycle();

        initFaceView(context);
    }

    private void initDefaultValues(Context context) {
        closeButtonPosition = DEFAULT_CLOSE_BUTTON_POSITION;
        closeButtonSize = DEFAULT_CLOSE_BUTTON_SIZE;
        closeButtonDrawable = context.getDrawable(android.R.drawable.ic_notification_clear_all);
        borderColor = DEFAULT_BORDER_COLOR;
        borderWidth = DEFAULT_BORDER_WIDTH;
        borderCornerRadius = DEFAULT_BORDER_CORNERS_RADIUS;
        clickAnimSize = DEFAULT_CLICK_ANIM_SIZE;
        isClickAnimShown = DEFAULT_IS_CLICK_ANIM_SHOW;
    }

    private void initFaceView(Context context) {
        closeImgView = new ImageView(context);
        closeImgView.setImageDrawable(closeButtonDrawable);
        LayoutParams closeButtonParams = new LayoutParams(closeButtonSize, closeButtonSize);
        adjustCloseButtonPosition(closeButtonParams);

        borderView = new BorderView(context);
        LayoutParams borderParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        adjustBorderMargin(borderParams);

        ScaleAnimation scaleAnimation = new ScaleAnimation(0.975f, 1.025f, 0.975f, 1.025f, Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f);
        scaleAnimation.setDuration(300);
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        borderView.startAnimation(scaleAnimation);

        clickAnimView = new LottieAnimationView(context);
        clickAnimView.setRenderMode(RenderMode.SOFTWARE);
        clickAnimView.setRepeatCount(LottieDrawable.INFINITE);
        if(isClickAnimShown){
            showClickAnim();
        }else{
            hideClickAnim();
        }
        LayoutParams clickAnimParams = new LayoutParams(clickAnimSize, clickAnimSize);
        clickAnimParams.gravity = Gravity.CENTER;
        adjustClickAnimMargin(clickAnimParams);

        setClipChildren(false);

        addView(borderView, borderParams);
        addView(closeImgView, closeButtonParams);
        addView(clickAnimView, clickAnimParams);
    }

    public @ClosePosition
    int getCloseButtonPosition() {
        return closeButtonPosition;
    }

    public void setCloseButtonPosition(@ClosePosition int closeButtonPosition) {
        this.closeButtonPosition = closeButtonPosition;

        adjustCloseButtonPosition();
        adjustBorderMargin();
        adjustClickAnimMargin();
    }

    public int getCloseButtonSize() {
        return closeButtonSize;
    }

    public void setCloseButtonSize(int closeButtonSize) {
        this.closeButtonSize = closeButtonSize;

        adjustCloseButtonSize();
        adjustBorderMargin();
        adjustClickAnimMargin();
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(@ColorInt int borderColor) {
        this.borderColor = borderColor;
        borderView.invalidate();
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
        borderView.invalidate();
    }

    public float getBorderCornerRadius() {
        return borderCornerRadius;
    }

    public void setBorderCornerRadius(float cornerRadius) {
        this.borderCornerRadius = cornerRadius;
        borderView.invalidate();
    }

    public Drawable getCloseButtonDrawable() {
        return closeButtonDrawable;
    }

    public void setCloseButtonDrawable(Drawable closeButtonDrawable) {
        this.closeButtonDrawable = closeButtonDrawable;
        closeImgView.setImageDrawable(closeButtonDrawable);
    }

    public int getClickAnimSize() {
        return clickAnimSize;
    }

    public void setClickAnimSize(int clickAnimSize) {
        this.clickAnimSize = clickAnimSize;

        LayoutParams clickAnimParams = (LayoutParams) clickAnimView.getLayoutParams();
        clickAnimParams.width = clickAnimSize;
        clickAnimParams.height = clickAnimSize;
        clickAnimView.setLayoutParams(clickAnimParams);
    }

    private void adjustCloseButtonPosition() {
        adjustCloseButtonPosition(null);
    }

    private void adjustCloseButtonPosition(LayoutParams closeButtonParams) {
        boolean isParamsProvided = true;
        if (closeButtonParams == null) {
            closeButtonParams = (LayoutParams) closeImgView.getLayoutParams();
            isParamsProvided = false;
        }

        switch (closeButtonPosition) {
            case POSITION_TOP_START:
                closeButtonParams.gravity = Gravity.START | Gravity.TOP;
                break;
            case POSITION_TOP_END:
                closeButtonParams.gravity = Gravity.END | Gravity.TOP;
                break;
            case POSITION_BOTTOM_START:
                closeButtonParams.gravity = Gravity.START | Gravity.BOTTOM;
                break;
            case POSITION_BOTTOM_END:
                closeButtonParams.gravity = Gravity.END | Gravity.BOTTOM;
        }

        if (!isParamsProvided) {
            closeImgView.setLayoutParams(closeButtonParams);
        }
    }

    private void adjustCloseButtonSize() {
        LayoutParams closeButtonParams = (LayoutParams) closeImgView.getLayoutParams();
        closeButtonParams.width = closeButtonSize;
        closeButtonParams.height = closeButtonSize;
        closeImgView.setLayoutParams(closeButtonParams);
    }

    private void adjustBorderMargin() {
        adjustBorderMargin(null);
    }

    private void adjustBorderMargin(LayoutParams borderParams) {
        boolean isParamsProvided = true;
        if (borderParams == null) {
            borderParams = (LayoutParams) borderView.getLayoutParams();
            isParamsProvided = false;
        }

        int halfCloseButtonSize = closeButtonSize / 2;

        switch (closeButtonPosition) {
            case POSITION_TOP_START:
                borderParams.setMargins(halfCloseButtonSize, halfCloseButtonSize, 0, 0);
                break;
            case POSITION_TOP_END:
                borderParams.setMargins(0, halfCloseButtonSize, halfCloseButtonSize, 0);
                break;
            case POSITION_BOTTOM_START:
                borderParams.setMargins(halfCloseButtonSize, 0, 0, halfCloseButtonSize);
                break;
            case POSITION_BOTTOM_END:
                borderParams.setMargins(0, 0, halfCloseButtonSize, halfCloseButtonSize);
                break;
        }

        if (!isParamsProvided) {
            borderView.setLayoutParams(borderParams);
        }
    }

    private void adjustClickAnimMargin() {
        adjustClickAnimMargin(null);
    }

    private void adjustClickAnimMargin(LayoutParams clickAnimParams) {
        boolean isParamsProvided = true;
        if (clickAnimParams == null) {
            clickAnimParams = (LayoutParams) clickAnimView.getLayoutParams();
            isParamsProvided = false;
        }

        int quarterCloseButtonSize = closeButtonSize / 4;

        switch (closeButtonPosition) {
            case POSITION_TOP_START:
                clickAnimParams.setMargins(quarterCloseButtonSize, quarterCloseButtonSize, 0, 0);
                break;
            case POSITION_TOP_END:
                clickAnimParams.setMargins(0, quarterCloseButtonSize, quarterCloseButtonSize, 0);
                break;
            case POSITION_BOTTOM_START:
                clickAnimParams.setMargins(quarterCloseButtonSize, 0, 0, quarterCloseButtonSize);
                break;
            case POSITION_BOTTOM_END:
                clickAnimParams.setMargins(0, 0, quarterCloseButtonSize, quarterCloseButtonSize);
                break;
        }

        if (!isParamsProvided) {
            clickAnimView.setLayoutParams(clickAnimParams);
        }
    }

    public boolean isClickAnimShown() {
        return isClickAnimShown;
    }

    public void showClickAnim(){
        isClickAnimShown = true;

        if(!isClickAnimLoaded){
            LottieCompositionFactory.fromRawRes(getContext(), R.raw.click).addListener((LottieComposition composition) -> {
                clickAnimView.setComposition(composition);
                clickAnimView.setVisibility(View.VISIBLE);
                clickAnimView.playAnimation();
            });
            isClickAnimLoaded = true;
        }else{
            clickAnimView.setVisibility(View.VISIBLE);
            clickAnimView.playAnimation();
        }
    }

    public void hideClickAnim(){
        isClickAnimShown = false;

        clickAnimView.cancelAnimation();
        clickAnimView.setVisibility(View.GONE);
    }

    public void setOnCloseListener(OnCloseListener closeListener) {
        closeImgView.setOnClickListener(v -> closeListener.onClose());
    }

    public void setOnFaceClickListener(OnFaceClickListener faceClickListener) {
        borderView.setOnClickListener(v -> faceClickListener.onFaceClick());
    }

    public interface OnCloseListener {
        void onClose();
    }

    public interface OnFaceClickListener {
        void onFaceClick();
    }

    @IntDef(value = {POSITION_TOP_START, POSITION_TOP_END, POSITION_BOTTOM_START, POSITION_BOTTOM_END})
    @Retention(RetentionPolicy.SOURCE)
    @interface ClosePosition {
    }

    private class BorderView extends View {
        private Paint paint;

        public BorderView(Context context) {
            super(context);
            initBorderView();
        }

        public BorderView(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public BorderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            initBorderView();
        }

        private void initBorderView() {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            paint.setColor(borderColor);
            paint.setStrokeWidth(borderWidth);

            canvas.drawRoundRect(0, 0, getWidth(), getHeight(), borderCornerRadius, borderCornerRadius, paint);
        }
    }
}