/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.FixPaddingListPopupWindow;
import androidx.appcompat.widget.ListPopupWindow;
import com.goodwy.files.R;
import me.zhanghai.android.files.util.ViewUtils;

public class DropDownView extends View {

    @NonNull
    private final ListPopupWindow mPopup;

    public DropDownView(@NonNull Context context) {
        this(context, null);
    }

    public DropDownView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DropDownView(@NonNull Context context, @Nullable AttributeSet attrs,
                        @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DropDownView(@NonNull Context context, @Nullable AttributeSet attrs,
                        @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setVisibility(INVISIBLE);

        mPopup = new FixPaddingListPopupWindow(context, attrs);
        mPopup.setModal(true);
        mPopup.setAnchorView(this);
        mPopup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
        maybeSimulateElevationOverlay();
    }

    private void maybeSimulateElevationOverlay() {
        Context context = getContext();
        boolean elevationOverlayEnabled = ViewUtils.getBooleanFromAttrRes(
                R.attr.elevationOverlayEnabled, false, context);
        if (!elevationOverlayEnabled) {
            return;
        }
        Resources resources = context.getResources();
        float elevation = resources.getDimensionPixelOffset(
                R.dimen.mtrl_exposed_dropdown_menu_popup_elevation);
        MaterialShapeDrawable background = MaterialShapeDrawable.createWithElevationOverlay(context,
                elevation);
        float cornerSize = resources.getDimensionPixelOffset(
                R.dimen.mtrl_shape_corner_size_small_component);
        ShapeAppearanceModel shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setAllCornerSizes(cornerSize)
                .build();
        background.setShapeAppearanceModel(shapeAppearanceModel);
        mPopup.setBackgroundDrawable(background);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mPopup.isShowing()) {
            mPopup.dismiss();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mPopup.setWidth(getMeasuredWidth());
    }

    @Override
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.showing = mPopup.isShowing();
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(@NonNull Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        if (savedState.showing) {
            ViewTreeObserver.OnGlobalLayoutListener listener =
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (!mPopup.isShowing()) {
                                mPopup.show();
                            }
                            getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    };
            getViewTreeObserver().addOnGlobalLayoutListener(listener);
        }
    }

    public void setAdapter(@Nullable ListAdapter adapter) {
        mPopup.setAdapter(adapter);
    }

    public void setOnItemClickListener(@Nullable AdapterView.OnItemClickListener listener) {
        mPopup.setOnItemClickListener(listener);
    }

    public boolean isShowing() {
        return mPopup.isShowing();
    }

    public void show() {
        mPopup.show();
    }

    public void dismiss() {
        mPopup.dismiss();
    }

    @NonNull
    public ListPopupWindow getPopup() {
        return mPopup;
    }

    private static class SavedState extends BaseSavedState {

        public boolean showing;

        public SavedState(@NonNull Parcelable superState) {
            super(superState);
        }


        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        public SavedState(Parcel in) {
            super(in);

            showing = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            out.writeByte((byte) (showing ? 1 : 0));
        }
    }
}
