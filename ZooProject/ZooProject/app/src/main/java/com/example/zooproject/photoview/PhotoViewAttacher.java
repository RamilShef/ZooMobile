/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.example.zooproject.photoview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import java.lang.ref.WeakReference;

import com.example.zooproject.photoview.gestures.OnGestureListener;
import com.example.zooproject.photoview.gestures.VersionedGestureDetector;
import com.example.zooproject.photoview.log.LogManager;
import com.example.zooproject.photoview.log.Logger;
import com.example.zooproject.photoview.scrollerproxy.ScrollerProxy;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;

public class PhotoViewAttacher implements IPhotoView, View.OnTouchListener,
        OnGestureListener,
        ViewTreeObserver.OnGlobalLayoutListener {

    private static final String LOG_TAG = "PhotoViewAttacher";

    // let debug flag be dynamic, but still Proguard can be used to remove from
    // release builds
    private static final boolean DEBUG = /*Log.isLoggable(LOG_TAG, Log.DEBUG)*/true;

    static final Interpolator sInterpolator = new AccelerateDecelerateInterpolator();
    int ZOOM_DURATION = DEFAULT_ZOOM_DURATION;

    static final int EDGE_NONE = -1;
    static final int EDGE_LEFT = 0;
    static final int EDGE_RIGHT = 1;
    static final int EDGE_BOTH = 2;

    private float mMinScale = DEFAULT_MIN_SCALE;
    private float mMidScale = DEFAULT_MID_SCALE;
    private float mMaxScale = DEFAULT_MAX_SCALE;

    private boolean mAllowParentInterceptOnEdge = true;

    private static void checkZoomLevels(float minZoom, float midZoom,
                                        float maxZoom) {
        if (minZoom >= midZoom) {
            throw new IllegalArgumentException(
                    "MinZoom has to be less than MidZoom");
        } else if (midZoom >= maxZoom) {
            throw new IllegalArgumentException(
                    "MidZoom has to be less than MaxZoom");
        }
    }

    /**
     * @return true if the ImageView exists, and it's Drawable existss
     */
    private static boolean hasDrawable(ImageView imageView) {
        return null != imageView && null != imageView.getDrawable();
    }

    /**
     * 该方法再该类的{@link #setScaleType(ScaleType)}方法中调用，虽然PhotoView中重写
     * 了setScaleType方法，但在方法的实现中并没有调用super.setScaleType()，而是转而
     * 调用该类中的setScaleType方法。<p>
     * 在setScaleType()中设置的scaleType并没有被设置到ImageView中，而是保存下来，然后
     * 根据该值来改变matrix的值，进而设置到ImageView上。<p>
     * 所以，在这里设置的scaleType的值，不能为ScaleType.Matrix.
     * @return true if the ScaleType is supported.
     */
    private static boolean isSupportedScaleType(final ScaleType scaleType) {
        if (null == scaleType) {
            return false;
        }

        switch (scaleType) {
            case MATRIX:
                throw new IllegalArgumentException(scaleType.name()
                        + " is not supported in PhotoView");

            default:
                return true;
        }
    }

    /**
     * Set's the ImageView's ScaleType to Matrix.
     * 
     * 将当前的ImageView的ScaleType设置为Matrix.
     */
    private static void setImageViewScaleTypeMatrix(ImageView imageView) {
        /**
         * PhotoView sets it's own ScaleType to Matrix, then diverts all calls
         * setScaleType to this.setScaleType automatically.
         */
    	// 这里用imageView instanceOf PhotoView应该更好，因为在PhotoView中
    	// 已经设置了ImageView的ScaleType为Matrix.
        if (null != imageView && !(imageView instanceof IPhotoView)) {
            if (!ScaleType.MATRIX.equals(imageView.getScaleType())) {
                imageView.setScaleType(ScaleType.MATRIX);
            }
        }
    }

    private WeakReference<ImageView> mImageView;

    // Gesture Detectors
    private GestureDetector mGestureDetector;
    private com.example.zooproject.photoview.gestures.GestureDetector mScaleDragDetector;

    // These are set so we don't keep allocating them on the heap
    private final Matrix mBaseMatrix = new Matrix();
    private final Matrix mDrawMatrix = new Matrix();
    private final Matrix mSuppMatrix = new Matrix();
    private final RectF mDisplayRect = new RectF();
    private final float[] mMatrixValues = new float[9];

    // Listeners
    private OnMatrixChangedListener mMatrixChangeListener;
    private OnPhotoTapListener mPhotoTapListener;
    private OnViewTapListener mViewTapListener;
    private OnLongClickListener mLongClickListener;

    private int mIvTop, mIvRight, mIvBottom, mIvLeft;
    private FlingRunnable mCurrentFlingRunnable;
    private int mScrollEdge = EDGE_BOTH;

    private boolean mZoomEnabled;
    
    /**
     * 这个值只是在这里保存了下来，并没有被设置到ImageView中去，它的
     * 作用是合理更改Matrix的值，然后将Matrix设置到ImageView中去。
     */
    private ScaleType mScaleType = ScaleType.FIT_CENTER;

    public PhotoViewAttacher(ImageView imageView) {
        mImageView = new WeakReference<ImageView>(imageView);

        imageView.setDrawingCacheEnabled(true);
        imageView.setOnTouchListener(this);

        ViewTreeObserver observer = imageView.getViewTreeObserver();
        if (null != observer)
            observer.addOnGlobalLayoutListener(this);

        // Make sure we using MATRIX Scale Type
        setImageViewScaleTypeMatrix(imageView);

        if (imageView.isInEditMode()) {
            return;
        }
        // Create Gesture Detectors...
        mScaleDragDetector = VersionedGestureDetector.newInstance(
                imageView.getContext(), this);

        mGestureDetector = new GestureDetector(imageView.getContext(),
                new GestureDetector.SimpleOnGestureListener() {

                    // forward long click listener
                    @Override
                    public void onLongPress(MotionEvent e) {
                        if (null != mLongClickListener) {
                            mLongClickListener.onLongClick(getImageView());
                        }
                    }

                });

        mGestureDetector.setOnDoubleTapListener(new DefaultOnDoubleTapListener(this));

        // Finally, update the UI so that we're zoomable
        setZoomable(true);
    }

    @Override
    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener newOnDoubleTapListener) {
        if (newOnDoubleTapListener != null)
            this.mGestureDetector.setOnDoubleTapListener(newOnDoubleTapListener);
        else
            this.mGestureDetector.setOnDoubleTapListener(new DefaultOnDoubleTapListener(this));
    }

    @Override
    public boolean canZoom() {
        return mZoomEnabled;
    }

    /**
     * Clean-up the resources attached to this object. This needs to be called when the ImageView is
     * no longer used. A good example is from {@link View#onDetachedFromWindow()} or
     * from {@link android.app.Activity#onDestroy()}. This is automatically called if you are using
     * {@link com.example.zooproject.photoview.PhotoView}.
     */
    @SuppressWarnings("deprecation")
    public void cleanup() {
        if (null == mImageView) {
            return; // cleanup already done
        }

        final ImageView imageView = mImageView.get();

        if (null != imageView) {
            // Remove this as a global layout listener
            ViewTreeObserver observer = imageView.getViewTreeObserver();
            if (null != observer && observer.isAlive()) {
                observer.removeGlobalOnLayoutListener(this);
            }

            // Remove the ImageView's reference to this
            imageView.setOnTouchListener(null);

            // make sure a pending fling runnable won't be run
            cancelFling();
        }

        if (null != mGestureDetector) {
            mGestureDetector.setOnDoubleTapListener(null);
        }

        // Clear listeners too
        mMatrixChangeListener = null;
        mPhotoTapListener = null;
        mViewTapListener = null;

        // Finally, clear ImageView
        mImageView = null;
    }

    @Override
    public RectF getDisplayRect() {
        checkMatrixBounds();
        return getDisplayRect(getDrawMatrix());
    }

    @Override
    public boolean setDisplayMatrix(Matrix finalMatrix) {
        if (finalMatrix == null)
            throw new IllegalArgumentException("Matrix cannot be null");

        ImageView imageView = getImageView();
        if (null == imageView)
            return false;

        if (null == imageView.getDrawable())
            return false;

        mSuppMatrix.set(finalMatrix);
        setImageViewMatrix(getDrawMatrix());
        checkMatrixBounds();

        return true;
    }

    /**
     * @deprecated use {@link #setRotationTo(float)}
     */
    @Override
    public void setPhotoViewRotation(float degrees) {
        mSuppMatrix.setRotate(degrees % 360);
        checkAndDisplayMatrix();
    }

    @Override
    public void setRotationTo(float degrees) {
        mSuppMatrix.setRotate(degrees % 360);
        checkAndDisplayMatrix();
    }

    @Override
    public void setRotationBy(float degrees) {
        mSuppMatrix.postRotate(degrees % 360);
        checkAndDisplayMatrix();
    }

    public ImageView getImageView() {
        ImageView imageView = null;

        if (null != mImageView) {
            imageView = mImageView.get();
        }

        // If we don't have an ImageView, call cleanup()
        if (null == imageView) {
            cleanup();
            Log.i(LOG_TAG,
                    "ImageView no longer exists. You should not use this PhotoViewAttacher any more.");
        }

        return imageView;
    }

    @Override
    @Deprecated
    public float getMinScale() {
        return getMinimumScale();
    }

    @Override
    public float getMinimumScale() {
        return mMinScale;
    }

    @Override
    @Deprecated
    public float getMidScale() {
        return getMediumScale();
    }

    @Override
    public float getMediumScale() {
        return mMidScale;
    }

    @Override
    @Deprecated
    public float getMaxScale() {
        return getMaximumScale();
    }

    @Override
    public float getMaximumScale() {
        return mMaxScale;
    }

    @Override
    public float getScale() {
        return (float) Math.sqrt((float) Math.pow(getValue(mSuppMatrix, Matrix.MSCALE_X), 2) + (float) Math.pow(getValue(mSuppMatrix, Matrix.MSKEW_Y), 2));
    }

    @Override
    public ScaleType getScaleType() {
        return mScaleType;
    }

    @Override
    public void onDrag(float dx, float dy) {
        if (mScaleDragDetector.isScaling()) {
            return; // Do not drag if we are already scaling
        }

        if (DEBUG) {
            LogManager.getLogger().d(LOG_TAG,
                    String.format("onDrag: dx: %.2f. dy: %.2f", dx, dy));
        }

        ImageView imageView = getImageView();
        mSuppMatrix.postTranslate(dx, dy);
        checkAndDisplayMatrix();

        /**
         * Here we decide whether to let the ImageView's parent to start taking
         * over the touch event.
         *
         * First we check whether this function is enabled. We never want the
         * parent to take over if we're scaling. We then check the edge we're
         * on, and the direction of the scroll (i.e. if we're pulling against
         * the edge, aka 'overscrolling', let the parent take over).
         */
        ViewParent parent = imageView.getParent();
        if (mAllowParentInterceptOnEdge && !mScaleDragDetector.isScaling()) {
            if (mScrollEdge == EDGE_BOTH
                    || (mScrollEdge == EDGE_LEFT && dx >= 1f)
                    || (mScrollEdge == EDGE_RIGHT && dx <= -1f)) {
                if (null != parent)
                    parent.requestDisallowInterceptTouchEvent(false);
            }
        } else {
            if (null != parent) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
    }

    @Override
    public void onFling(float startX, float startY, float velocityX,
                              float velocityY) {
        if (DEBUG) {
            LogManager.getLogger().d(
                    LOG_TAG,
                    "onFling. sX: " + startX + " sY: " + startY + " Vx: "
                            + velocityX + " Vy: " + velocityY);
        }
        ImageView imageView = getImageView();
        mCurrentFlingRunnable = new FlingRunnable(imageView.getContext());
        mCurrentFlingRunnable.fling(getImageViewWidth(imageView),
                getImageViewHeight(imageView), (int) velocityX, (int) velocityY);
        imageView.post(mCurrentFlingRunnable);
    }
    
    @Override
    public void onScale(float scaleFactor, float focusX, float focusY) {
        if (DEBUG) {
            LogManager.getLogger().d(
                    LOG_TAG,
                    String.format("onScale: scale: %.2f. fX: %.2f. fY: %.2f",
                            scaleFactor, focusX, focusY));
        }

        if (getScale() < mMaxScale || scaleFactor < 1f) {
            mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
            checkAndDisplayMatrix();
        }
    }

    @Override
    public void onGlobalLayout() {
        ImageView imageView = getImageView();

        if (null != imageView) {
            if (mZoomEnabled) {
                final int top = imageView.getTop();
                final int right = imageView.getRight();
                final int bottom = imageView.getBottom();
                final int left = imageView.getLeft();

                /**
                 * We need to check whether the ImageView's bounds have changed.
                 * This would be easier if we targeted API 11+ as we could just use
                 * View.OnLayoutChangeListener. Instead we have to replicate the
                 * work, keeping track of the ImageView's bounds and then checking
                 * if the values change.
                 */
                if (top != mIvTop || bottom != mIvBottom || left != mIvLeft
                        || right != mIvRight) {
                    // Update our base matrix, as the bounds have changed
                    updateBaseMatrix(imageView.getDrawable());

                    // Update values as something has changed
                    mIvTop = top;
                    mIvRight = right;
                    mIvBottom = bottom;
                    mIvLeft = left;
                }
            } else {
                updateBaseMatrix(imageView.getDrawable());
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        boolean handled = false;
        
        if (mZoomEnabled && hasDrawable((ImageView) v)) {
            ViewParent parent = v.getParent();
            switch (ev.getAction()) {
                case ACTION_DOWN:
                    // First, disable the Parent from intercepting the touch
                    // event
                    if (null != parent)
                        parent.requestDisallowInterceptTouchEvent(true);
                    else
                        Log.i(LOG_TAG, "onTouch getParent() returned null");

                    // If we're flinging, and the user presses down, cancel
                    // fling
                    cancelFling();
                    break;

                case ACTION_CANCEL:
                case ACTION_UP:
                    // If the user has zoomed less than min scale, zoom back
                    // to min scale
                    if (getScale() < mMinScale) {
                        RectF rect = getDisplayRect();
                        if (null != rect) {
                            v.post(new AnimatedZoomRunnable(getScale(), mMinScale,
                                    rect.centerX(), rect.centerY()));
                            handled = true;
                        }
                    }
                    break;
            }

            // 这里一定会返回true，所以对PhotoView设置OnClickListener和OnLongClickListener都无效
            // Try the Scale/Drag detector
            if (null != mScaleDragDetector
                    && mScaleDragDetector.onTouchEvent(ev)) {
                handled = true;
            }

            // Check to see if the user double tapped
            if (null != mGestureDetector && mGestureDetector.onTouchEvent(ev)) {
                handled = true;
            }
        }

        return handled;
    }

    @Override
    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAllowParentInterceptOnEdge = allow;
    }

    @Override
    @Deprecated
    public void setMinScale(float minScale) {
        setMinimumScale(minScale);
    }

    @Override
    public void setMinimumScale(float minimumScale) {
        checkZoomLevels(minimumScale, mMidScale, mMaxScale);
        mMinScale = minimumScale;
    }

    @Override
    @Deprecated
    public void setMidScale(float midScale) {
        setMediumScale(midScale);
    }

    @Override
    public void setMediumScale(float mediumScale) {
        checkZoomLevels(mMinScale, mediumScale, mMaxScale);
        mMidScale = mediumScale;
    }

    @Override
    @Deprecated
    public void setMaxScale(float maxScale) {
        setMaximumScale(maxScale);
    }

    @Override
    public void setMaximumScale(float maximumScale) {
        checkZoomLevels(mMinScale, mMidScale, maximumScale);
        mMaxScale = maximumScale;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener listener) {
        mLongClickListener = listener;
    }

    @Override
    public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
        mMatrixChangeListener = listener;
    }

    @Override
    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        mPhotoTapListener = listener;
    }

    @Override
    public OnPhotoTapListener getOnPhotoTapListener() {
        return mPhotoTapListener;
    }

    @Override
    public void setOnViewTapListener(OnViewTapListener listener) {
        mViewTapListener = listener;
    }

    @Override
    public OnViewTapListener getOnViewTapListener() {
        return mViewTapListener;
    }

    @Override
    public void setScale(float scale) {
        setScale(scale, false);
    }

    @Override
    public void setScale(float scale, boolean animate) {
        ImageView imageView = getImageView();

        if (null != imageView) {
            setScale(scale,
                    (imageView.getRight()) / 2,
                    (imageView.getBottom()) / 2,
                    animate);
        }
    }

    @Override
    public void setScale(float scale, float focalX, float focalY,
                         boolean animate) {
        ImageView imageView = getImageView();

        if (null != imageView) {
            // Check to see if the scale is within bounds
            if (scale < mMinScale || scale > mMaxScale) {
                LogManager
                        .getLogger()
                        .i(LOG_TAG,
                                "Scale must be within the range of minScale and maxScale");
                return;
            }

            if (animate) {
                imageView.post(new AnimatedZoomRunnable(getScale(), scale,
                        focalX, focalY));
            } else {
                mSuppMatrix.setScale(scale, scale, focalX, focalY);
                checkAndDisplayMatrix();
            }
        }
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (isSupportedScaleType(scaleType) && scaleType != mScaleType) {
            mScaleType = scaleType;

            // Finally update
            update();
        }
    }

    @Override
    public void setZoomable(boolean zoomable) {
        mZoomEnabled = zoomable;
        update();
    }

    /**
     * 3种情况下调用该方法：
     * 1. 改变图片的源
     * 2. 改变图片的ScaleType
     * 3. 改变图片的可缩放性
     */
    public void update() {
        ImageView imageView = getImageView();

        if (null != imageView) {
            if (mZoomEnabled) {
                // Make sure we using MATRIX Scale Type
                setImageViewScaleTypeMatrix(imageView);

                // Update the base matrix using the current drawable
                updateBaseMatrix(imageView.getDrawable());
            } else {
                // Reset the Matrix...
                resetMatrix();
            }
        }
    }

    @Override
    public Matrix getDisplayMatrix() {
        return new Matrix(getDrawMatrix());
    }

    /**
     * @return mSuppMatrix * mBaseMatrix
     */
    public Matrix getDrawMatrix() {
        mDrawMatrix.set(mBaseMatrix);
        mDrawMatrix.postConcat(mSuppMatrix);
        return mDrawMatrix;
    }

    private void cancelFling() {
        if (null != mCurrentFlingRunnable) {
            mCurrentFlingRunnable.cancelFling();
            mCurrentFlingRunnable = null;
        }
    }

    /**
     * Helper method that simply checks the Matrix, and then displays the result
     */
    private void checkAndDisplayMatrix() {
        if (checkMatrixBounds()) {
            setImageViewMatrix(getDrawMatrix());
        }
    }

    private void checkImageViewScaleType() {
        ImageView imageView = getImageView();

        /**
         * PhotoView's getScaleType() will just divert to this.getScaleType() so
         * only call if we're not attached to a PhotoView.
         */
        if (null != imageView && !(imageView instanceof IPhotoView)) {
            if (!ScaleType.MATRIX.equals(imageView.getScaleType())) {
                throw new IllegalStateException(
                        "The ImageView's ScaleType has been changed since attaching a PhotoViewAttacher");
            }
        }
    }

    /**
     * 检查图片的边界，使图片的边界出现在合适的位置，
     * 该方法主要是改变mSuppMatrix的值。<p>
     * 有些方法中调用了该方法后，并没有进行setImageMatrix操作，
     * 比如{@link #setDisplayMatrix(Matrix)}，可能是因为有时候
     * 用户希望图片显示到那个位置。其他的地方一般这两个值都是0.
     * @return
     */
    private boolean checkMatrixBounds() {
        final ImageView imageView = getImageView();
        if (null == imageView) {
            return false;
        }

        final RectF rect = getDisplayRect(getDrawMatrix());
        if (null == rect) {
            return false;
        }

        final float height = rect.height(), width = rect.width(); // 新的图像的宽度和高度
        float deltaX = 0, deltaY = 0; // 为保证图像按要求正常显示，图像需要移动的距离

        // 1. 垂直方向上
        final int viewHeight = getImageViewHeight(imageView);
        if (height <= viewHeight) { // 图像在高度上能完全显示在屏幕上
            switch (mScaleType) {
                case FIT_START:
                    deltaY = -rect.top; // 向上移动到屏幕顶部
                    break;
                case FIT_END:
                    deltaY = viewHeight - height - rect.top; // 向下移动到屏幕底部
                    break;
                default:
                    deltaY = (viewHeight - height) / 2 - rect.top; // 移动到屏幕中间位置
                    break;
            }
        // 图像的高度大于屏幕的高度，即图像不能完全显示在屏幕上
        } else if (rect.top > 0) { // 如果图像的上边界出现在屏幕中
            deltaY = -rect.top;    // 则将图像的上边界移回到屏幕的上边缘处
        } else if (rect.bottom < viewHeight) { // 如果图像的下边界出现在屏幕中
            deltaY = viewHeight - rect.bottom; // 则将图像的下边界移回到屏幕的下边缘处
        }

        // 2. 水平方向上，同垂直方向
        final int viewWidth = getImageViewWidth(imageView);
        if (width <= viewWidth) {
            switch (mScaleType) {
                case FIT_START:
                    deltaX = -rect.left;
                    break;
                case FIT_END:
                    deltaX = viewWidth - width - rect.left;
                    break;
                default:
                    deltaX = (viewWidth - width) / 2 - rect.left;
                    break;
            }
            mScrollEdge = EDGE_BOTH;
        } else if (rect.left > 0) {
            mScrollEdge = EDGE_LEFT;
            deltaX = -rect.left;
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right;
            mScrollEdge = EDGE_RIGHT;
        } else {
            mScrollEdge = EDGE_NONE;
        }

        // Finally actually translate the matrix
        mSuppMatrix.postTranslate(deltaX, deltaY);
        
        return true;
    }

    /**
     * Helper method that maps the supplied Matrix to the current Drawable
     * 得到图像变换后，图片的显示位置
     * @param matrix - Matrix to map Drawable against
     * @return RectF - Displayed Rectangle
     */
    private RectF getDisplayRect(Matrix matrix) {
        ImageView imageView = getImageView();

        if (null != imageView) {
            Drawable d = imageView.getDrawable();
            if (null != d) {
                mDisplayRect.set(0, 0, d.getIntrinsicWidth(),
                        d.getIntrinsicHeight());
                
                // mapRect方法的作用就是将matrix应用于rect上面，
                // 其实可以把这个rect当做一个ImageView，给这个
                // ImageView应用matrix.
                matrix.mapRect(mDisplayRect);
                
                return mDisplayRect;
            }
        }
        return null;
    }

    public Bitmap getVisibleRectangleBitmap() {
        ImageView imageView = getImageView();
        return imageView == null ? null : imageView.getDrawingCache();
    }

    @Override
    public void setZoomTransitionDuration(int milliseconds) {
        if (milliseconds < 0)
            milliseconds = DEFAULT_ZOOM_DURATION;
        this.ZOOM_DURATION = milliseconds;
    }

    @Override
    public IPhotoView getIPhotoViewImplementation() {
        return this;
    }

    /**
     * Helper method that 'unpacks' a Matrix and returns the required value
     *
     * @param matrix     - Matrix to unpack
     * @param whichValue - Which value from Matrix.M* to return
     * @return float - returned value
     */
    private float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    /**
     * Resets the Matrix back to FIT_CENTER, and then displays it.s
     */
    private void resetMatrix() {
        mSuppMatrix.reset();
        setImageViewMatrix(getDrawMatrix());
        checkMatrixBounds();
    }

    private void setImageViewMatrix(Matrix matrix) {
        ImageView imageView = getImageView();
        if (null != imageView) {

            checkImageViewScaleType();
            imageView.setImageMatrix(matrix);

            // Call MatrixChangedListener if needed
            if (null != mMatrixChangeListener) {
                RectF displayRect = getDisplayRect(matrix);
                if (null != displayRect) {
                    mMatrixChangeListener.onMatrixChanged(displayRect);
                }
            }
        }
    }

    /**
     * 该方法实质上只在初始化图片的时候调用，根据ImageView控件的大小、
     * 图片的大小以及我们设置的ScaleType来调整图片的位置，进而获得
     * 此时ImageView的Matrix，这是初始化ImageView的时候就有的Matrix，
     * 所以叫做base matrix.
     * 
     * Calculate Matrix for FIT_CENTER
     *
     * @param d - Drawable being displayed
     */
    private void updateBaseMatrix(Drawable d) {
        ImageView imageView = getImageView();
        if (null == imageView || null == d) {
            return;
        }

        final float viewWidth = getImageViewWidth(imageView);
        final float viewHeight = getImageViewHeight(imageView);
        final int drawableWidth = d.getIntrinsicWidth();
        final int drawableHeight = d.getIntrinsicHeight();

        if (DEBUG) {
        	LogManager.getLogger().d(LOG_TAG, "dw:" + drawableWidth + " , dh:" + drawableHeight);
        }
        
        // 此时图像的左上角位于坐标（0，0）处
        
        mBaseMatrix.reset();

        final float widthScale = viewWidth / drawableWidth;
        final float heightScale = viewHeight / drawableHeight;

        // 不对图像进行缩放，只将图像的中心移动到屏幕的中心
        if (mScaleType == ScaleType.CENTER) {
            mBaseMatrix.postTranslate((viewWidth - drawableWidth) / 2F,
                    (viewHeight - drawableHeight) / 2F);

        } else if (mScaleType == ScaleType.CENTER_CROP) {
        	// 1.保证永远只有一个方向（水平或垂直）能完全显示在屏幕中，除非图像的宽高比等于显示控件的宽高比
        	// 2.如果是宽图，则按高度比例缩放
        	// 3.如果是长图，则按宽度比例缩放
        	// 4.最后将缩放后的图像移动到屏幕中心
            float scale = Math.max(widthScale, heightScale);
            
            // 相对于原点进行缩放，规则参考上面2、3
            // 会根据图片大小缩放，将图片缩放方向上完全显示在控件中
            mBaseMatrix.postScale(scale, scale);
            
            // drawableWidth * scale、drawableHeight * scale为新图片的宽和高
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                    (viewHeight - drawableHeight * scale) / 2F);

        } else if (mScaleType == ScaleType.CENTER_INSIDE) {
        	// 和CENTER_CROP缩放思想一致，但规则相反
        	// 1.保证整个图像完全显示在屏幕中
        	// 2.按缩放比例大的（即看宽和高哪个变化更大）缩放
        	// 3.如果图像本身就能完全显示在屏幕中，则不缩放
        	// 4.最后将缩放后的图像移动到屏幕中心
        	
        	// 这里Math.min(1.0f...是保证图片不会被放大，比如整个图片小于ImageView的时候
            float scale = Math.min(1.0f, Math.min(widthScale, heightScale));
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                    (viewHeight - drawableHeight * scale) / 2F);

        } else {
        	// 参考：http://www.imobilebbs.com/wordpress/archives/1625
            RectF tempSrc = new RectF(0, 0, drawableWidth, drawableHeight);
            RectF tempDst = new RectF(0, 0, viewWidth, viewHeight);

            switch (mScaleType) {
                case FIT_CENTER:
                    mBaseMatrix.setRectToRect(tempSrc, tempDst, ScaleToFit.CENTER);
                    break;

                case FIT_START:
                    mBaseMatrix.setRectToRect(tempSrc, tempDst, ScaleToFit.START);
                    break;

                case FIT_END:
                    mBaseMatrix.setRectToRect(tempSrc, tempDst, ScaleToFit.END);
                    break;

                case FIT_XY:
                    mBaseMatrix.setRectToRect(tempSrc, tempDst, ScaleToFit.FILL);
                    break;

                default:
                    break;
            }
        }

        resetMatrix();
    }

    private int getImageViewWidth(ImageView imageView) {
        if (null == imageView)
            return 0;
        return imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
    }

    private int getImageViewHeight(ImageView imageView) {
        if (null == imageView)
            return 0;
        return imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
    }

    /**
     * Interface definition for a callback to be invoked when the internal Matrix has changed for
     * this View.
     *
     * @author Chris Banes
     */
    public static interface OnMatrixChangedListener {
        /**
         * Callback for when the Matrix displaying the Drawable has changed. This could be because
         * the View's bounds have changed, or the user has zoomed.
         *
         * @param rect - Rectangle displaying the Drawable's new bounds.
         */
        void onMatrixChanged(RectF rect);
    }

    /**
     * Interface definition for a callback to be invoked when the Photo is tapped with a single
     * tap.
     *
     * @author Chris Banes
     */
    public static interface OnPhotoTapListener {

        /**
         * A callback to receive where the user taps on a photo. You will only receive a callback if
         * the user taps on the actual photo, tapping on 'whitespace' will be ignored.
         *
         * @param view - View the user tapped.
         * @param x    - where the user tapped from the of the Drawable, as percentage of the
         *             Drawable width.
         * @param y    - where the user tapped from the top of the Drawable, as percentage of the
         *             Drawable height.
         */
        void onPhotoTap(View view, float x, float y);
    }

    /**
     * Interface definition for a callback to be invoked when the ImageView is tapped with a single
     * tap.
     *
     * @author Chris Banes
     */
    public static interface OnViewTapListener {

        /**
         * A callback to receive where the user taps on a ImageView. You will receive a callback if
         * the user taps anywhere on the view, tapping on 'whitespace' will not be ignored.
         *
         * @param view - View the user tapped.
         * @param x    - where the user tapped from the left of the View.
         * @param y    - where the user tapped from the top of the View.
         */
        void onViewTap(View view, float x, float y);
    }

    private class AnimatedZoomRunnable implements Runnable {

        private final float mFocalX, mFocalY;
        private final long mStartTime;
        private final float mZoomStart, mZoomEnd;

        public AnimatedZoomRunnable(final float currentZoom, final float targetZoom,
                                    final float focalX, final float focalY) {
            mFocalX = focalX;
            mFocalY = focalY;
            mStartTime = System.currentTimeMillis();
            mZoomStart = currentZoom;
            mZoomEnd = targetZoom;
        }

        @Override
        public void run() {
            ImageView imageView = getImageView();
            if (imageView == null) {
                return;
            }

            float t = interpolate();
            float scale = mZoomStart + t * (mZoomEnd - mZoomStart);
            float deltaScale = scale / getScale();

            // 在本次scale的基础上变动deltaScale，就变成新的scale了
            mSuppMatrix.postScale(deltaScale, deltaScale, mFocalX, mFocalY);
            checkAndDisplayMatrix();

            // We haven't hit our target scale yet, so post ourselves again
            if (t < 1f) {
                Compat.postOnAnimation(imageView, this);
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / ZOOM_DURATION;
            t = Math.min(1f, t);
            t = sInterpolator.getInterpolation(t);
            return t;
        }
    }

    private class FlingRunnable implements Runnable {

        private final ScrollerProxy mScroller;
        private int mCurrentX, mCurrentY;

        public FlingRunnable(Context context) {
            mScroller = ScrollerProxy.getScroller(context);
        }

        public void cancelFling() {
            if (DEBUG) {
                LogManager.getLogger().d(LOG_TAG, "Cancel Fling");
            }
            mScroller.forceFinished(true);
        }

        public void fling(int viewWidth, int viewHeight, int velocityX,
                          int velocityY) {
            final RectF rect = getDisplayRect();
            if (null == rect) {
                return;
            }

            final int startX = Math.round(-rect.left);
            final int minX, maxX, minY, maxY;

            if (viewWidth < rect.width()) {
                minX = 0;
                maxX = Math.round(rect.width() - viewWidth);
            } else {
                minX = maxX = startX;
            }

            final int startY = Math.round(-rect.top);
            if (viewHeight < rect.height()) {
                minY = 0;
                maxY = Math.round(rect.height() - viewHeight);
            } else {
                minY = maxY = startY;
            }

            mCurrentX = startX;
            mCurrentY = startY;

            if (DEBUG) {
                LogManager.getLogger().d(
                        LOG_TAG,
                        "fling. StartX:" + startX + " StartY:" + startY
                                + " MaxX:" + maxX + " MaxY:" + maxY);
            }

            // If we actually can move, fling the scroller
            if (startX != maxX || startY != maxY) {
                mScroller.fling(startX, startY, velocityX, velocityY, minX,
                        maxX, minY, maxY, 0, 0);
            }
        }

        @Override
        public void run() {
            if (mScroller.isFinished()) {
                return; // remaining post that should not be handled
            }

            ImageView imageView = getImageView();
            if (null != imageView && mScroller.computeScrollOffset()) {

                final int newX = mScroller.getCurrX();
                final int newY = mScroller.getCurrY();

                if (DEBUG) {
                    LogManager.getLogger().d(
                            LOG_TAG,
                            "fling run(). CurrentX:" + mCurrentX + " CurrentY:"
                                    + mCurrentY + " NewX:" + newX + " NewY:"
                                    + newY);
                }

                mSuppMatrix.postTranslate(mCurrentX - newX, mCurrentY - newY);
                setImageViewMatrix(getDrawMatrix());

                mCurrentX = newX;
                mCurrentY = newY;

                // Post On animation
                Compat.postOnAnimation(imageView, this);
            }
        }
    }
}
