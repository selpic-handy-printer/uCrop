package com.yalantis.ucrop.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.yalantis.ucrop.R;
import com.yalantis.ucrop.callback.CropBoundsChangeListener;
import com.yalantis.ucrop.callback.OverlayViewChangeListener;

public class UCropView extends FrameLayout {

    protected CropImageView mCropImageView;
    protected OverlayView mViewOverlay;

    public UCropView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UCropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        prepareInnerView(context);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ucrop_UCropView);
        mViewOverlay.processStyledAttributes(a);
        mCropImageView.processStyledAttributes(a);
        a.recycle();

        setListenersToViews();
    }

    /**
     * To create {@link #mCropImageView} and {@link #mViewOverlay}, then add they to UCropView.
     */
    protected void prepareInnerView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.ucrop_view, this, true);
        mCropImageView = findViewById(R.id.image_view_crop);
        mViewOverlay = findViewById(R.id.view_overlay);
    }

    private void setListenersToViews() {
        mCropImageView.setCropBoundsChangeListener(new CropBoundsChangeListener() {
            @Override
            public void onCropAspectRatioChanged(float cropRatio) {
                mViewOverlay.setTargetAspectRatio(cropRatio);
            }
        });
        mViewOverlay.setOverlayViewChangeListener(new OverlayViewChangeListener() {
            @Override
            public void onCropRectUpdated(RectF cropRect) {
                mCropImageView.setCropRect(cropRect);
            }
        });
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @NonNull
    public CropImageView getCropImageView() {
        return mCropImageView;
    }

    @NonNull
    public OverlayView getOverlayView() {
        return mViewOverlay;
    }

    /**
     * Method for reset state for UCropImageView such as rotation, scale, translation.
     * Be careful: this method recreate UCropImageView instance and reattach it to layout.
     */
    public void resetCropImageView() {
        removeView(mCropImageView);
        mCropImageView = new GestureCropImageView(getContext());
        setListenersToViews();
        mCropImageView.setCropRect(getOverlayView().getCropViewRect());
        addView(mCropImageView, 0);
    }

    public void setScaleEnabled(boolean scaleEnabled) {
        if (mCropImageView instanceof GestureCropImageView) {
            ((GestureCropImageView) mCropImageView).setScaleEnabled(scaleEnabled);
        }
    }

    public void setRotateEnabled(boolean rotateEnabled) {
        if (mCropImageView instanceof GestureCropImageView) {
            ((GestureCropImageView) mCropImageView).setRotateEnabled(rotateEnabled);
        }
    }

    public void rotate90(boolean forceFillView) {
        RectF oldCropRect = new RectF(mViewOverlay.getCropViewRect());
        mViewOverlay.rotate90(forceFillView);
        RectF cropRect = mViewOverlay.getCropViewRect();
        float scale = cropRect.height() / oldCropRect.width();
        float deltaX = cropRect.centerX() - oldCropRect.centerX();
        float deltaY = cropRect.centerY() - oldCropRect.centerY();
        // calculate params
        float deltaScale = mCropImageView.getCurrentScale() * scale - mCropImageView.getCurrentScale();
        float finalCenterX = mCropImageView.getCropRect().centerX();
        float finalCenterY = mCropImageView.getCropRect().centerY();
        mCropImageView.cancelAllAnimations(); // cancel prev animation
        mCropImageView.setImageToPosition(deltaX, deltaY, deltaScale, 90, finalCenterX - deltaX, finalCenterY - deltaY, true);
    }
}