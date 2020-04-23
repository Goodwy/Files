/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.RadioGroup;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import java8.nio.file.Path;
import com.goodwy.files.R;
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.settings.Settings;
import me.zhanghai.android.files.util.BundleUtils;
import me.zhanghai.android.files.util.CollectionUtils;
import me.zhanghai.android.files.util.FragmentUtils;
import me.zhanghai.java.functional.Functional;

public class CreateArchiveDialogFragment extends FileNameDialogFragment {

    private static final String KEY_PREFIX = CreateArchiveDialogFragment.class.getName() + '.';

    private static final String EXTRA_FILES = KEY_PREFIX + "FILES";

    @BindView(R.id.type_group)
    RadioGroup mTypeGroup;

    private LinkedHashSet<FileItem> mExtraFiles;

    @NonNull
    public static CreateArchiveDialogFragment newInstance(@NonNull LinkedHashSet<FileItem> files) {
        //noinspection deprecation
        CreateArchiveDialogFragment fragment = new CreateArchiveDialogFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelableArrayList(EXTRA_FILES, new ArrayList<>(files));
        return fragment;
    }

    public static void show(@NonNull LinkedHashSet<FileItem> files, @NonNull Fragment fragment) {
        CreateArchiveDialogFragment.newInstance(files)
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance(LinkedHashSet)} instead.
     */
    public CreateArchiveDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraFiles = new LinkedHashSet<>(BundleUtils.getParcelableArrayList(getArguments(),
                EXTRA_FILES));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (savedInstanceState == null) {
            String name = null;
            if (mExtraFiles.size() == 1) {
                FileItem file = CollectionUtils.first(mExtraFiles);
                name = file.getPath().getFileName().toString();
            } else {
                Set<Path> parents = Functional.map(mExtraFiles, file -> file.getPath().getParent(),
                        new HashSet<>());
                if (parents.size() == 1) {
                    Path parent = CollectionUtils.first(parents);
                    if (parent.getNameCount() > 0) {
                        name = parent.getFileName().toString();
                    }
                }
            }
            if (name != null) {
                mNameEdit.setText(name);
                mNameEdit.setSelection(0, name.length());
            }
        }
        return dialog;
    }

    @LayoutRes
    @Override
    protected int getTitleRes() {
        return R.string.file_create_archive_title;
    }

    @Override
    protected int getLayoutRes() {
        return Settings.MATERIAL_DESIGN_2.getValue() ? R.layout.create_archive_dialog_md2
                : R.layout.create_archive_dialog;
    }

    @NonNull
    @Override
    protected String getName() {
        int typeId = mTypeGroup.getCheckedRadioButtonId();
        String extension;
        switch (typeId) {
            case R.id.type_zip:
                extension = "zip";
                break;
            case R.id.type_tar_xz:
                extension = "tar.xz";
                break;
            case R.id.type_seven_z:
                extension = "7z";
                break;
            default:
                throw new AssertionError(typeId);
        }
        return super.getName() + '.' + extension;
    }

    @Override
    protected void onOk(@NonNull String name) {
        int typeId = mTypeGroup.getCheckedRadioButtonId();
        String archiveType;
        String compressorType;
        switch (typeId) {
            case R.id.type_zip:
                archiveType = ArchiveStreamFactory.ZIP;
                compressorType = null;
                break;
            case R.id.type_tar_xz:
                archiveType = ArchiveStreamFactory.TAR;
                compressorType = CompressorStreamFactory.XZ;
                break;
            case R.id.type_seven_z:
                archiveType = ArchiveStreamFactory.SEVEN_Z;
                compressorType = null;
                break;
            default:
                throw new AssertionError(typeId);
        }
        getListener().archive(mExtraFiles, name, archiveType, compressorType);
    }

    @NonNull
    @Override
    protected Listener getListener() {
        return (Listener) requireParentFragment();
    }

    public interface Listener extends FileNameDialogFragment.Listener {
        void archive(@NonNull LinkedHashSet<FileItem> files, @NonNull String name,
                     @NonNull String archiveType, @Nullable String compressorType);
    }
}
