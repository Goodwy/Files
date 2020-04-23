/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import com.goodwy.files.R;

public class CreateFileDialogFragment extends FileNameDialogFragment {

    @NonNull
    public static CreateFileDialogFragment newInstance() {
        //noinspection deprecation
        return new CreateFileDialogFragment();
    }

    public static void show(@NonNull Fragment fragment) {
        CreateFileDialogFragment.newInstance()
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public CreateFileDialogFragment() {}

    @Override
    @StringRes
    protected int getTitleRes() {
        return R.string.file_create_file_title;
    }

    @Override
    protected void onOk(@NonNull String name) {
        getListener().createFile(name);
    }

    @NonNull
    @Override
    protected Listener getListener() {
        return (Listener) requireParentFragment();
    }

    public interface Listener extends FileNameDialogFragment.Listener {
        void createFile(@NonNull String name);
    }
}
