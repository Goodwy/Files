/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.goodwy.files.R;

public abstract class AnimatedListAdapter<T, VH extends RecyclerView.ViewHolder>
        extends ListAdapter<T, VH> {

    private static final int ANIMATION_STAGGER_MILLIS = 20;

    private boolean mShouldStartAnimation;
    private int mAnimationStartOffset;

    @NonNull
    private final Handler mStopAnimationHandler = new Handler(Looper.getMainLooper());
    @NonNull
    private final Runnable mStopAnimationRunnable = this::stopAnimation;

    @NonNull
    private RecyclerView mRecyclerView;
    @NonNull
    private final RecyclerView.OnScrollListener mClearAnimationListener =
            new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    clearAnimation();
                }
            };

    public AnimatedListAdapter(@NonNull DiffUtil.ItemCallback<T> callback) {
        super(callback);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
        mRecyclerView.addOnScrollListener(mClearAnimationListener);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        mRecyclerView.removeOnScrollListener(mClearAnimationListener);
        mRecyclerView = null;
    }

    @Override
    public void refresh() {
        resetAnimation();
        super.refresh();
    }

    @Override
    public void replace(@NonNull List<T> list, boolean clear) {
        if (clear) {
            resetAnimation();
        }
        super.replace(list, clear);
    }

    @Override
    public void clear() {
        resetAnimation();
        super.clear();
    }

    protected void bindViewHolderAnimation(@NonNull VH holder) {
        holder.itemView.clearAnimation();
        if (mShouldStartAnimation) {
            Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(),
                    R.anim.list_item);
            animation.setStartOffset(mAnimationStartOffset);
            mAnimationStartOffset += ANIMATION_STAGGER_MILLIS;
            holder.itemView.startAnimation(animation);
            postStopAnimation();
        }
    }

    private void stopAnimation() {
        mStopAnimationHandler.removeCallbacks(mStopAnimationRunnable);
        mShouldStartAnimation = false;
        mAnimationStartOffset = 0;
    }

    private void postStopAnimation() {
        mStopAnimationHandler.removeCallbacks(mStopAnimationRunnable);
        mStopAnimationHandler.post(mStopAnimationRunnable);
    }

    private void clearAnimation() {
        stopAnimation();
        for (int i = 0, count = mRecyclerView.getChildCount(); i < count; ++i) {
            View child = mRecyclerView.getChildAt(i);
            child.clearAnimation();
        }
    }

    private void resetAnimation() {
        clearAnimation();
        mShouldStartAnimation = isAnimationEnabled();
    }

    protected boolean isAnimationEnabled() {
        return true;
    }
}
