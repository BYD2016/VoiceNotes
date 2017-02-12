package codepath.com.cn.imrecord.view;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import codepath.com.cn.imrecord.R;

/**
 * Created by admin on 2017/2/11.
 */

final class RecordAdioDialogManager {

    @BindView(R.id.tv_dialog_recording_mic)
    TextView mTvDialogRecordingMic;
    @BindView(R.id.tv_dialog_recording_volume)
    TextView mTvDialogRecordingVolume;
    @BindView(R.id.tv_dialog_recording_promentMsg)
    TextView mTvDialogRecordingPromentMsg;

    private Context mContext;

    private Dialog mDialog;

    RecordAdioDialogManager(Context context) {
        mContext = context;
    }

    void showRecordingDialog() {
        mDialog = new Dialog(mContext, R.style.theme_dailog_recording);

        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.dialog_recording, null);
        mDialog.setContentView(view);

        ButterKnife.bind(this, view);

        mDialog.show();
    }

    void dismissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    void toRecording() {
        if (mDialog != null && mDialog.isShowing()) {
            mTvDialogRecordingMic.setBackgroundResource(R.drawable.recorder);
            mTvDialogRecordingVolume.setVisibility(View.VISIBLE);
            mTvDialogRecordingPromentMsg.setText(R.string.dialog_recording_prompt);
        }
    }

    void toWantToCancel() {
        if (mDialog != null && mDialog.isShowing()) {
            mTvDialogRecordingMic.setBackgroundResource(R.drawable.cancel);
            mTvDialogRecordingVolume.setVisibility(View.GONE);
            mTvDialogRecordingPromentMsg.setText(R.string.record_audio_want_to_cancel);
        }
    }

    void toTooShort() {
        if (mDialog != null && mDialog.isShowing()) {
            mTvDialogRecordingMic.setBackgroundResource(R.drawable.voice_to_short);
            mTvDialogRecordingVolume.setVisibility(View.GONE);
            mTvDialogRecordingPromentMsg.setText(R.string.dialog_recording_too_short);
        }
    }

    void updateVoiceLeve(int level) {
        if (mDialog != null && mDialog.isShowing() && mTvDialogRecordingVolume.isShown()) {
            int resId = mContext.getResources().getIdentifier("v" + level, "drawable"
                    , mContext.getPackageName());
            mTvDialogRecordingVolume.setBackgroundResource(resId);
        }
    }
}
