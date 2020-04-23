/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Space;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.goodwy.files.R;
import me.zhanghai.android.files.compat.AlertDialogBuilderCompat;
import me.zhanghai.android.files.provider.common.PosixFileStore;
import me.zhanghai.android.files.util.BundleBuilder;
import me.zhanghai.android.files.util.BundleUtils;
import me.zhanghai.android.files.util.RemoteCallback;
import me.zhanghai.android.files.util.StateData;
import me.zhanghai.android.files.util.ToastUtils;
import me.zhanghai.android.files.util.ViewUtils;

public class FileJobActionDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = FileJobActionDialogFragment.class.getName() + '.';

    private static final String EXTRA_TITLE = KEY_PREFIX + "TITLE";
    private static final String EXTRA_MESSAGE = KEY_PREFIX + "MESSAGE";
    private static final String EXTRA_READ_ONLY_FILE_STORE = KEY_PREFIX + "READ_ONLY_FILE_STORE";
    private static final String EXTRA_SHOW_ALL = KEY_PREFIX + "SHOW_ALL";
    private static final String EXTRA_POSITIVE_BUTTON_TEXT = KEY_PREFIX + "POSITIVE_BUTTON_TEXT";
    private static final String EXTRA_NEGATIVE_BUTTON_TEXT = KEY_PREFIX + "NEGATIVE_BUTTON_TEXT";
    private static final String EXTRA_NEUTRAL_BUTTON_TEXT = KEY_PREFIX + "NEUTRAL_BUTTON_TEXT";
    private static final String EXTRA_LISTENER = KEY_PREFIX + "LISTENER";

    private static final String STATE_ALL_CHECKED = KEY_PREFIX + "ALL_CHECKED";

    @NonNull
    private CharSequence mTitle;
    @NonNull
    private CharSequence mMessage;
    @Nullable
    private PosixFileStore mReadOnlyFileStore;
    private boolean mShowAll;
    @Nullable
    private CharSequence mPositiveButtonText;
    @Nullable
    private CharSequence mNegativeButtonText;
    @Nullable
    private CharSequence mNeutralButtonText;
    @Nullable
    private RemoteCallback mListener;

    @NonNull
    private View mView;

    @BindView(R.id.remount)
    Button mRemountButton;
    @BindView(R.id.all_space)
    Space mAllSpace;
    @BindView(R.id.all)
    CheckBox mAllCheck;

    @NonNull
    private FileJobActionViewModel mViewModel;

    public static void putArguments(@NonNull Intent intent, @NonNull CharSequence title,
                                    @NonNull CharSequence message,
                                    @Nullable PosixFileStore readOnlyFileStore, boolean showAll,
                                    @Nullable CharSequence positiveButtonText,
                                    @Nullable CharSequence negativeButtonText,
                                    @Nullable CharSequence neutralButtonText,
                                    @NonNull Listener listener) {
        intent
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_MESSAGE, message)
                .putExtra(EXTRA_READ_ONLY_FILE_STORE, (Parcelable) readOnlyFileStore)
                .putExtra(EXTRA_SHOW_ALL, showAll)
                .putExtra(EXTRA_POSITIVE_BUTTON_TEXT, positiveButtonText)
                .putExtra(EXTRA_NEGATIVE_BUTTON_TEXT, negativeButtonText)
                .putExtra(EXTRA_NEUTRAL_BUTTON_TEXT, neutralButtonText)
                .putExtra(EXTRA_LISTENER, new RemoteCallback(new ListenerAdapter(listener)));
    }

    @NonNull
    public static FileJobActionDialogFragment newInstance(@NonNull Intent intent) {
        //noinspection deprecation
        FileJobActionDialogFragment fragment = new FileJobActionDialogFragment();
        fragment.setArguments(intent.getExtras());
        return fragment;
    }

    /**
     * @deprecated Use {@link #newInstance(Intent)} instead.
     */
    public FileJobActionDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        mTitle = arguments.getCharSequence(EXTRA_TITLE);
        mMessage = arguments.getCharSequence(EXTRA_MESSAGE);
        mReadOnlyFileStore = BundleUtils.getParcelable(arguments, EXTRA_READ_ONLY_FILE_STORE);
        mShowAll = arguments.getBoolean(EXTRA_SHOW_ALL);
        mPositiveButtonText = arguments.getCharSequence(EXTRA_POSITIVE_BUTTON_TEXT);
        mNegativeButtonText = arguments.getCharSequence(EXTRA_NEGATIVE_BUTTON_TEXT);
        mNeutralButtonText = arguments.getCharSequence(EXTRA_NEUTRAL_BUTTON_TEXT);
        mListener = BundleUtils.getParcelable(arguments, EXTRA_LISTENER);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_ALL_CHECKED, mAllCheck.isChecked());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Context context = requireContext();
        int theme = getTheme();
        mView = ViewUtils.inflateWithTheme(R.layout.file_job_action_dialog_view, context, theme);
        ButterKnife.bind(this, mView);

        mViewModel = new ViewModelProvider(this).get(FileJobActionViewModel.class);

        ViewUtils.setVisibleOrGone(mRemountButton, mReadOnlyFileStore != null);
        if (mReadOnlyFileStore != null) {
            updateRemountButton();
            mRemountButton.setOnClickListener(view -> remount());
        }
        ViewUtils.setVisibleOrGone(mAllSpace, mReadOnlyFileStore == null && mShowAll);
        ViewUtils.setVisibleOrGone(mAllCheck, mShowAll);
        if (savedInstanceState != null) {
            mAllCheck.setChecked(savedInstanceState.getBoolean(STATE_ALL_CHECKED));
        }

        if (mReadOnlyFileStore != null) {
            mViewModel.getRemountStateLiveData().observe(this, this::onRemountStateChanged);
        }

        AlertDialog dialog = AlertDialogBuilderCompat.create(context, theme)
                .setTitle(mTitle)
                .setMessage(mMessage)
                .setPositiveButton(mPositiveButtonText, this::onDialogButtonClick)
                .setNegativeButton(mNegativeButtonText, this::onDialogButtonClick)
                .setNeutralButton(mNeutralButtonText, this::onDialogButtonClick)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    private void remount() {
        if (mViewModel.getRemountStateLiveData().getValue().state != StateData.State.READY
                || !mReadOnlyFileStore.isReadOnly()) {
            return;
        }
        mViewModel.getRemountStateLiveData().remount(mReadOnlyFileStore);
    }

    private void onRemountStateChanged(@NonNull StateData stateData) {
        RemountStateLiveData liveData = mViewModel.getRemountStateLiveData();
        switch (stateData.state) {
            case READY:
            case LOADING:
                updateRemountButton();
                break;
            case ERROR:
                stateData.exception.printStackTrace();
                ToastUtils.show(stateData.exception.toString(), requireContext());
                liveData.reset();
                updateRemountButton();
                break;
            case SUCCESS:
                liveData.reset();
                updateRemountButton();
                break;
            default:
                throw new AssertionError();
        }
    }

    private void updateRemountButton() {
        int textRes = mViewModel.getRemountStateLiveData().getValue().state
                == StateData.State.LOADING ? R.string.file_job_remount_loading_format
                : mReadOnlyFileStore.isReadOnly() ? R.string.file_job_remount_format
                : R.string.file_job_remount_success_format;
        mRemountButton.setText(getString(textRes, mReadOnlyFileStore.name()));
    }

    private void onDialogButtonClick(@NonNull DialogInterface dialog, int which) {
        Action action;
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                action = Action.POSITIVE;
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                action = Action.NEGATIVE;
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                action = Action.NEUTRAL;
                break;
            default:
                throw new IllegalArgumentException(Integer.toString(which));
        }
        sendResult(action, mShowAll && mAllCheck.isChecked());
        requireActivity().finish();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mView.getParent() == null) {
            AlertDialog dialog = (AlertDialog) requireDialog();
            NestedScrollView scrollView = dialog.findViewById(R.id.scrollView);
            LinearLayout linearLayout = (LinearLayout) scrollView.getChildAt(0);
            linearLayout.addView(mView);
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);

        sendResult(Action.CANCELED, false);
        requireActivity().finish();
    }

    public void onFinish() {
        sendResult(Action.CANCELED, false);
    }

    private void sendResult(@NonNull Action action, boolean all) {
        if (mListener != null) {
            ListenerAdapter.sendResult(mListener, action, all);
            mListener = null;
        }
    }

    public enum Action {
        POSITIVE,
        NEGATIVE,
        NEUTRAL,
        CANCELED
    }

    public interface Listener {

        void onAction(@NonNull Action action, boolean all);
    }

    private static class ListenerAdapter implements RemoteCallback.Listener {

        private static final String KEY_PREFIX = Listener.class.getName() + '.';

        private static final String RESULT_ACTION = KEY_PREFIX + "ACTION";
        private static final String RESULT_ALL = KEY_PREFIX + "ALL";

        @NonNull
        private final Listener mListener;

        public ListenerAdapter(@NonNull Listener listener) {
            mListener = listener;
        }

        public static void sendResult(@NonNull RemoteCallback listener, @NonNull Action action,
                                      boolean all) {
            listener.sendResult(new BundleBuilder()
                    .putSerializable(RESULT_ACTION, action)
                    .putBoolean(RESULT_ALL, all)
                    .build());
        }

        @Override
        public void onResult(Bundle result) {
            Action action = (Action) result.getSerializable(RESULT_ACTION);
            boolean all = result.getBoolean(RESULT_ALL);
            mListener.onAction(action, all);
        }
    }
}
