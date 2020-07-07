package vng.zalo.tdtai.zalo.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.util.List;

import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.util.Utils;

public class Temp {
    private ImageView imageView;
    private ConstraintLayout rootLayout;
    private Context context;
    private Utils utils;

    private void attachFaces(Bitmap bitmap, List<FirebaseVisionFace> faces) {
        imageView.post(() -> {
            int viewWidth = imageView.getWidth();
            int viewHeight = imageView.getHeight();
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();

            float scaleX = viewWidth / (float) bitmapWidth;
            float scaleY = viewHeight / (float) bitmapHeight;
            float scale = Math.min(scaleX, scaleY);

            int bitmapWidthScaled = (int) (bitmapWidth * scale);
            int bitmapHeightScaled = (int) (bitmapHeight * scale);

            int cropX = (viewWidth - bitmapWidthScaled) / 2;
            int cropY = (viewHeight - bitmapHeightScaled) / 2;

            //for bouncing animation visible outside view bounds
            rootLayout.setClipChildren(false);

            ConstraintSet set = new ConstraintSet();
            set.clone(rootLayout);

            FaceView biggestFaceView = null;
            Float biggestFaceViewArea = null;

            for (FirebaseVisionFace face : faces) {
                FaceView faceView = getFaceView();
                faceView.setId(View.generateViewId());
                rootLayout.addView(faceView);

                Rect rect = face.getBoundingBox();

                int halfCloseButtonSize = faceView.getCloseButtonSize() / 2;

                int marginStart = (int) (rect.left * scale + cropX);
                int marginEnd = (int) (bitmapWidthScaled - rect.right * scale + cropX) - halfCloseButtonSize;
                int marginTop = (int) (rect.top * scale + cropY) - halfCloseButtonSize;
                int marginBottom = (int) (bitmapHeightScaled - rect.bottom * scale + cropY);

                set.connect(faceView.getId(), ConstraintSet.TOP, rootLayout.getId(), ConstraintSet.TOP, marginTop);
                set.connect(faceView.getId(), ConstraintSet.START, rootLayout.getId(), ConstraintSet.START, marginStart);
                set.connect(faceView.getId(), ConstraintSet.BOTTOM, rootLayout.getId(), ConstraintSet.BOTTOM, marginBottom);
                set.connect(faceView.getId(), ConstraintSet.END, rootLayout.getId(), ConstraintSet.END, marginEnd);

                faceView.setOnCloseListener(() -> rootLayout.removeView(faceView));
                faceView.setOnFaceClickListener(() -> Toast.makeText(context, "face clicked", Toast.LENGTH_SHORT).show());

                Float curArea = (rect.right - rect.left) * (rect.bottom - rect.top) * scale * scale;

                if (biggestFaceViewArea == null || biggestFaceViewArea < curArea) {
                    biggestFaceView = faceView;
                    biggestFaceViewArea = curArea;
                }
            }

            if (biggestFaceView != null) {
                biggestFaceView.showClickAnim();
            }

            set.applyTo(rootLayout);
        });
    }

    private FaceView getFaceView() {
        FaceView faceView = new FaceView(context);
        faceView.setCloseButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ic_close_small_round));
        faceView.setCloseButtonSize(utils.dpToPx(35));
        faceView.setBorderWidth(utils.dpToPx(3));
        faceView.setBorderCornerRadius(utils.dpToPx(16));
        faceView.setBorderColor(Color.WHITE);
        faceView.setClickAnimSize(utils.dpToPx(60));
        return faceView;
    }

    private float dpToPx(Number valueInDp) {
        return (valueInDp.floatValue() * (context.getResources().getDisplayMetrics().densityDpi / (float) DisplayMetrics.DENSITY_DEFAULT));
    }

    private float pxToDp(Number valueInPx) {
        return (valueInPx.floatValue() / (context.getResources().getDisplayMetrics().densityDpi / (float) DisplayMetrics.DENSITY_DEFAULT));
    }
}
