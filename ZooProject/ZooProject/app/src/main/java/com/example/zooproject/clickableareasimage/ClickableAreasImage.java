package com.example.zooproject.clickableareasimage;

import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import com.example.zooproject.photoview.PhotoViewAttacher;

import java.util.ArrayList;
import java.util.List;


public class ClickableAreasImage implements PhotoViewAttacher.OnPhotoTapListener {

    private PhotoViewAttacher attacher;
    private OnClickableAreaClickedListener listener;

    private List<ClickableArea> clickableAreas;

    private int imageWidthInPx;
    private int imageHeightInPx;

    public ClickableAreasImage(ImageView imageView, OnClickableAreaClickedListener listener) {
        this.attacher = new PhotoViewAttacher(imageView);
        this.listener = listener;
        getImageDimensions(imageView);
        attacher.setOnPhotoTapListener(this);
    }

    private void getImageDimensions(ImageView imageView) {

        BitmapDrawable drawable2 = (BitmapDrawable) imageView.getDrawable();
        if (Build.VERSION.SDK_INT > 27) {
            imageWidthInPx = (int) (drawable2.getBitmap().getWidth());
            imageHeightInPx = (int) (drawable2.getBitmap().getHeight());
        } else {
            imageWidthInPx = (int) (drawable2.getBitmap().getWidth() / Resources.getSystem().getDisplayMetrics().density);
            imageHeightInPx = (int) (drawable2.getBitmap().getHeight() / Resources.getSystem().getDisplayMetrics().density);
        }

    }

    private List<ClickableArea> getClickAbleAreas(int x, int y) {
        List<ClickableArea> clickableAreas = new ArrayList<>();
        for (ClickableArea ca : getClickableAreas()) {
            if (isBetween(ca.getX(), (ca.getX() + ca.getWidth()), x)) {
                if (isBetween(ca.getY(), (ca.getY() + ca.getHeight()), y)) {
                    clickableAreas.add(ca);
                }
            }
        }
        return clickableAreas;
    }

    private boolean isBetween(int start, int end, int actual) {
        return (start <= actual && actual <= end);
    }

    public void setClickableAreas(List<ClickableArea> clickableAreas) {
        this.clickableAreas = clickableAreas;
    }

    public List<ClickableArea> getClickableAreas() {
        return clickableAreas;
    }

    @Override
    public void onPhotoTap(View view, float x, float y) {
        PixelPosition pixel = ImageUtils.getPixelPosition(x, y, imageWidthInPx, imageHeightInPx);
        List<ClickableArea> clickableAreas = getClickAbleAreas(pixel.getX(), pixel.getY());
        for (ClickableArea ca : clickableAreas) {
            listener.onClickableAreaTouched(ca.getItem());
        }
    }
}
