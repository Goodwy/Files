/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.NoticesXmlParser;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import com.goodwy.files.R;
import me.zhanghai.android.files.settings.Settings;
import me.zhanghai.android.files.util.BundleUtils;

public class LicensesDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = LicensesDialogFragment.class.getName() + '.';

    private static final String STATE_NOTICES = KEY_PREFIX + "NOTICES";

    @NonNull
    private Notices mNotices;

    @NonNull
    public static LicensesDialogFragment newInstance() {
        //noinspection deprecation
        return new LicensesDialogFragment();
    }

    public static void show(@NonNull Fragment fragment) {
        LicensesDialogFragment.newInstance()
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public LicensesDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mNotices = BundleUtils.getParcelable(savedInstanceState, STATE_NOTICES);
        } else {
            try {
                mNotices = NoticesXmlParser.parse(requireContext().getResources().openRawResource(
                        R.raw.licenses));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_NOTICES, mNotices);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // setIncludeOwnLicense(true) will modify our notices instance.
        Notices notices = new Notices();
        for (Notice notice : mNotices.getNotices()) {
            notices.addNotice(notice);
        }
        int htmlStyleRes = Settings.MATERIAL_DESIGN_2.getValue() ?
                R.string.about_licenses_html_style_md2 : R.string.about_licenses_html_style;
        return new LicensesDialog.Builder(requireContext())
                .setThemeResourceId(getTheme())
                .setTitle(R.string.about_licenses_title)
                .setNotices(notices)
                .setIncludeOwnLicense(true)
                .setNoticesCssStyle(htmlStyleRes)
                .setCloseText(R.string.close)
                .build()
                .create();
    }
}
