/*
 * Copyright (c) 2020 Goodwy <goodwy.dev@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.about;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.goodwy.files.R;
import me.zhanghai.android.files.ui.LicensesDialogFragment;
import me.zhanghai.android.files.util.AppUtils;
import me.zhanghai.android.files.util.IntentUtils;

public class AboutFragment extends Fragment {

    private static final Uri GITHUB_URI = Uri.parse(
            "");

    private static final Uri VERSION_URI = Uri.parse("https://play.google.com/store/apps/details?id=com.goodwy.files");

    private static final Uri CHANGELOG_URI = Uri.parse("https://github.com/Goodwy/Files/blob/master/CHANGELOG.md");

    private static final Uri LICENSES_URI = Uri.parse("https://github.com/Goodwy/Files"
            + "/blob/master/app/src/main/res/raw/licenses.xml");

    private static final Uri PRIVACY_POLICY_URI = Uri.parse(
            "https://github.com/Goodwy/Files/blob/master/PRIVACY.md");

    private static final Uri AUTHOR_RESUME_URI = Uri.parse("https://play.google.com/store/apps/dev?id=8268163890866913014");

    private static final Uri AUTHOR_RESUME2_URI = Uri.parse("https://github.com/zhanghai/MaterialFiles");

    private static final Uri AUTHOR_GITHUB_URI = Uri.parse("");

    private static final Uri AUTHOR_GOOGLE_PLUS_URI = Uri.parse(
            "");

    private static final Uri AUTHOR_TWITTER_URI = Uri.parse("");

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.github)
    ViewGroup mGitHubLayout;
    @BindView(R.id.version)
    ViewGroup mVersionLayout;
    @BindView(R.id.changelog)
    ViewGroup mchangelogLayout;
    @BindView(R.id.licenses)
    ViewGroup mLicensesLayout;
    @BindView(R.id.privacy_policy)
    ViewGroup mPrivacyPolicyLayout;
    @BindView(R.id.author_name)
    ViewGroup mAuthorNameLayout;
    @BindView(R.id.author_name2)
    ViewGroup mAuthorNameLayout2;
    @BindView(R.id.author_github)
    ViewGroup mAuthorGitHubLayout;
    @BindView(R.id.author_google_plus)
    ViewGroup mAuthorGooglePlusLayout;
    @BindView(R.id.author_twitter)
    ViewGroup mAuthorTwitterLayout;

    @NonNull
    public static AboutFragment newInstance() {
        //noinspection deprecation
        return new AboutFragment();
    }

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public AboutFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(mToolbar);

        mGitHubLayout.setOnClickListener(view -> AppUtils.startActivity(IntentUtils.makeView(
                GITHUB_URI), this));
        mVersionLayout.setOnClickListener(view -> AppUtils.startActivity(IntentUtils.makeView(
                VERSION_URI), this));
        mchangelogLayout.setOnClickListener(view -> AppUtils.startActivity(IntentUtils.makeView(
                CHANGELOG_URI), this));
        mLicensesLayout.setOnClickListener(view -> {
            // @see https://github.com/zhanghai/MaterialFiles/issues/161
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                LicensesDialogFragment.show(this);
            } else {
                AppUtils.startActivity(IntentUtils.makeView(LICENSES_URI), this);
            }
        });
        mPrivacyPolicyLayout.setOnClickListener(view -> AppUtils.startActivity(IntentUtils.makeView(
                PRIVACY_POLICY_URI), this));
        mAuthorNameLayout.setOnClickListener(view -> AppUtils.startActivity(IntentUtils.makeView(
                AUTHOR_RESUME_URI), this));
        mAuthorNameLayout2.setOnClickListener(view -> AppUtils.startActivity(IntentUtils.makeView(
                AUTHOR_RESUME2_URI), this));
        mAuthorGitHubLayout.setOnClickListener(view -> AppUtils.startActivity(IntentUtils.makeView(
                AUTHOR_GITHUB_URI), this));
        mAuthorGooglePlusLayout.setOnClickListener(view -> AppUtils.startActivity(
                IntentUtils.makeView(AUTHOR_GOOGLE_PLUS_URI), this));
        mAuthorTwitterLayout.setOnClickListener(view -> AppUtils.startActivity(IntentUtils.makeView(
                AUTHOR_TWITTER_URI), this));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                AppCompatActivity activity = (AppCompatActivity) requireActivity();
                // This recreates MainActivity but we cannot have singleTop as launch mode along
                // with document launch mode.
                //activity.onSupportNavigateUp();
                activity.finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
