package cat.lafosca.facecropper.sample;

/**
 * Created by david on 6/10/14.
 */

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FaceCropImageView extends ImageView {
    public static enum PictureOrientation { LANDSCAPE, PORTRAIT }

    private PointF faceCenter;
    private PictureOrientation pictureOrientation;

    public FaceCropImageView(Context context) {
        super(context);
        setup();
    }

    public FaceCropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public FaceCropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    private void setup() {
        setScaleType(ScaleType.MATRIX);
        setId(R.id.imageViewCropped);
        setSaveEnabled(true);
    }

    public void setFaceCenter(PointF faceCenter) {
        this.faceCenter = faceCenter;
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {

        if (getDrawable() == null || faceCenter == null) {
            return super.setFrame(l, t, r, b);
        }

        final Matrix matrix = getImageMatrix();

        float scale;

        final int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        final int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        final int drawableWidth = getDrawable().getIntrinsicWidth();
        final int drawableHeight = getDrawable().getIntrinsicHeight();

        pictureOrientation = drawableWidth > drawableHeight ? PictureOrientation.LANDSCAPE : PictureOrientation.PORTRAIT;

        if (drawableWidth * viewHeight > drawableHeight * viewWidth) {
            scale = (float) viewHeight / (float) drawableHeight;
        } else {
            scale = (float) viewWidth / (float) drawableWidth;
        }

        RectF drawableRect;

        if(pictureOrientation == PictureOrientation.PORTRAIT) {
            float viewScaleCorrection =  viewHeight / ( 2 * scale);

            float upperLimit = faceCenter.y - viewScaleCorrection;
            float lowerLimit = faceCenter.y + viewScaleCorrection;

            float upperCorrection = 0;
            float lowerCorrection = 0;

            if(upperLimit < 0) {
                upperLimit = 0;
                upperCorrection = viewScaleCorrection - faceCenter.y;
            }

            if(lowerLimit > drawableHeight) {
                lowerCorrection = lowerLimit - drawableHeight;
                lowerLimit = drawableHeight;
            }

            drawableRect = new RectF(0, upperLimit - lowerCorrection, drawableWidth, lowerLimit + upperCorrection);
        }
        else {
            float landscapeScale = viewWidth / viewHeight;

            float leftLimit;
            float rightLimit;

            float leftCorrection;
            float rightCorrection;

            if(landscapeScale * drawableHeight < drawableWidth) {
                float viewScaleCorrection =  viewWidth / ( 2 * scale);

                PointF drawableMiddlePoint = new PointF(drawableWidth / 2, drawableHeight / 2);

                leftLimit = drawableMiddlePoint.x - viewScaleCorrection;
                rightLimit = drawableMiddlePoint.x + viewScaleCorrection;

                leftCorrection = 0;
                rightCorrection = 0;

                if(leftLimit < 0) {
                    leftLimit = 0;
                    leftCorrection = viewScaleCorrection - drawableMiddlePoint.x;
                }

                if(rightLimit > drawableWidth) {
                    rightCorrection = rightLimit - drawableWidth;
                    rightLimit = drawableWidth;
                }

                setScaleType(ScaleType.CENTER_CROP);
            }
            else {
                leftLimit = 0;
                rightLimit = drawableWidth;

                leftCorrection = 0;
                rightCorrection = 0;

                setScaleType(ScaleType.CENTER_INSIDE);
            }


            drawableRect = new RectF(leftLimit - rightCorrection, 0, rightLimit + leftCorrection, drawableHeight);
        }

        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.FILL);

        setImageMatrix(matrix);

        return super.setFrame(l, t, r, b);
    }
}
