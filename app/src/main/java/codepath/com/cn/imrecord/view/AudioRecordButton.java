package codepath.com.cn.imrecord.view;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.ExecutorService;

import codepath.com.cn.imrecord.R;
import codepath.com.cn.imrecord.media.IMAudioManager;

/**
 * 控制录音Button
 * @author
 * 1、重写onTouchEvent；（changeState方法、wantToCancel方法、reset方法）；
 * 2、编写AudioDialogManage、并与该类AudioRecorderButton进行整合；
 * 3、编写AudioManage、并与该类AudioRecorderButton进行整合；
 */

public final class AudioRecordButton extends Button implements IMAudioManager.AudioStateListenter {

    private static final String TAG = AudioRecordButton.class.getSimpleName();

    public static final int STATE_NORMAL = 0;
    public static final int STATE_RECORDING = 1;
    public static final int STATE_WANT_TO_CANCEL = 2;
    public static final float TOO_SHORT_TIME_SECONDS = 1.5f;



    @IntDef({STATE_NORMAL, STATE_RECORDING, STATE_WANT_TO_CANCEL})
    @Retention(RetentionPolicy.SOURCE)
    @interface ButtonState {}

    private int mcurrentState = STATE_NORMAL;
    private RecordAdioDialogManager mRecordAdioDialogManager;

    private IMAudioManager mIMAudioManager;

    private boolean mIsRecording = false;

    private float mTime;  //开始录音时，计时；（在reset()中置空）

    /**
     * 获取音量大小的Runnable
     */
    private Runnable mGetVoiceLevelRunnable = new Runnable() {

        @Override
        public void run() {

            while (mIsRecording) {

                try {
                    Thread.sleep(100); // ms
                    mTime += 0.1f; // 计时

                    if (mcurrentState == STATE_RECORDING) {
                        mHandler.sendEmptyMessage(MSG_VOICE_CHANGE);
                    }

                } catch (InterruptedException e) {
                    Log.e(TAG, "线程中断。", e);
                }

            }
        }
    };

    private static final int MSG_AUDIO_PREPARED = 0x110;   //准备完全
    private static final int MSG_VOICE_CHANGE = MSG_AUDIO_PREPARED + 1;     //声音改变
    private static final int MSG_DIALOG_DIMISS = MSG_AUDIO_PREPARED + 2; //销毁对话框

    /**
     * 接收子线程数据，并用此数据配合主线程更新UI
     * Handler运行在主线程（UI线程）中，它与子线程通过Message对象传递数据。
     * Handler接受子线程传过来的(子线程用sedMessage()方法传弟)Message对象，把这些消息放入主线程队列中，配合主线程进行更新UI。
     */
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUDIO_PREPARED:
                    mRecordAdioDialogManager.showRecordingDialog();

                    mIsRecording = true;

                    //已经在录制，同时开启一个获取音量、并且计时的线程
                    new Thread(mGetVoiceLevelRunnable).start();

                    break;

                case MSG_VOICE_CHANGE:
                    mRecordAdioDialogManager.updateVoiceLeve(mIMAudioManager
                            .getVoiceLevel(7));

                    break;

                //这里在Handler里面处理DIALOG_DIMISS，是因为想让该对话框显示一段时间，延迟关闭，——详见125行
                case MSG_DIALOG_DIMISS:
                    mRecordAdioDialogManager.dismissDialog();
                    reset();
                    break;
            }
        }

    };

    /**
     * 正常录音完成后的回调
     * @author songshi
     *
     */
    public interface AudioFinishRecorderListenter{
        void onFinish(float seconds, String filePath);
    }

    private AudioFinishRecorderListenter mAudioFinishRecorderListenter;

    private ExecutorService mMediaExecutorService;

    public void setAudioFinishRecorderListenter(AudioFinishRecorderListenter listenter){
        this.mAudioFinishRecorderListenter = listenter;
    }

    public void setMediaExecutorService(ExecutorService mediaExecutorService) {
        mMediaExecutorService = mediaExecutorService;
    }

    void setCurrentState(@ButtonState int mcurrentState) {

        if (this.mcurrentState != mcurrentState) {

            this.mcurrentState = mcurrentState;

            switch (this.mcurrentState) {
                case STATE_NORMAL:
                    setBackgroundResource(R.drawable.button_record_audio_normal);
                    setText(R.string.record_audio_normal);

                    if (isRecording()) {
                        mRecordAdioDialogManager.dismissDialog();
                    }

                    break;

                case STATE_RECORDING:
                    setBackgroundResource(R.drawable.button_record_audio_recording);
                    setText(R.string.record_audio_recording);

                    if (isRecording()) {
                        mRecordAdioDialogManager.toRecording();
                    }

                    break;

                case STATE_WANT_TO_CANCEL:
                    setBackgroundResource(R.drawable.button_record_audio_recording);
                    setText(R.string.record_audio_want_to_cancel);

                    if (isRecording()) {
                        mRecordAdioDialogManager.toWantToCancel();
                    }

                    break;

                default:
                    break;
            }

        }
    }

    /**
     * 是否正在录音
     * @return boolean
     */
    boolean isRecording() {
        return mIsRecording;
    }

    public AudioRecordButton(Context context) {
        this(context, null);
    }

    public AudioRecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRecordAdioDialogManager = new RecordAdioDialogManager(context);

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Log.d(TAG, "LongClick, call IMAudioManager#prepareAudio()");

                if (mMediaExecutorService != null) {
                    mMediaExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            // 真正显示应该在audio end prepared以后
                            mIMAudioManager.prepareAudio();
                        }
                    });

                } else {
                    mIMAudioManager.prepareAudio();
                }

                return true;
            }
        });

        String audioDir = Environment.getExternalStorageDirectory() + "/VoiceRecorder";
        mIMAudioManager = IMAudioManager.getInstance(audioDir);
        mIMAudioManager.setOnAudioStateListenter(this);
    }

    @Override
    public void recordingPrepared() {
        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                setCurrentState(STATE_RECORDING);

                break;

            case MotionEvent.ACTION_MOVE:

                if (isRecording()) {

                    if (isWantToCancel(x, y)) {
                        setCurrentState(STATE_WANT_TO_CANCEL);
                    } else {
                        setCurrentState(STATE_RECORDING);
                    }

                }

                break;

            case MotionEvent.ACTION_UP:


                Log.d(TAG, "录音时间" + mTime + "s.");

                //录音时间过短
                if (mIsRecording && mTime < TOO_SHORT_TIME_SECONDS) {

                    Log.d(TAG, "录音时间过短," + mTime + "s.");

                    mRecordAdioDialogManager.toTooShort();
                    mIMAudioManager.cancel();

                    // 延迟，1.3秒以后关闭“时间过短对话框”
                    mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 1300);

                } else if (mcurrentState == STATE_RECORDING) { //正常录制结束

                    mHandler.sendEmptyMessage(MSG_DIALOG_DIMISS);

                    // release
                    if (mIsRecording) {
                        mIMAudioManager.release();

                        // 正常录制结束，回调录音时间和录音文件完整路径——在播放的时候需要使用

                        if(mAudioFinishRecorderListenter != null){
                            mAudioFinishRecorderListenter.onFinish(mTime,
                                    mIMAudioManager.getCurrentFilePath());
                        }

                    } else {
                        mIMAudioManager.cancel();
                    }

                    // callbackToAct


                } else if (mcurrentState == STATE_WANT_TO_CANCEL) {
                    // cancel
                    mHandler.sendEmptyMessage(MSG_DIALOG_DIMISS);
                    mIMAudioManager.cancel();
                }

                break;

            case MotionEvent.ACTION_CANCEL:
                mHandler.sendEmptyMessage(MSG_DIALOG_DIMISS);
                mIMAudioManager.cancel();
                break;

            default:

                break;
        }

        return super.onTouchEvent(event);
    }

    private void reset() {
        setCurrentState(STATE_NORMAL);
        mIsRecording = false;
        mTime = 0;
    }

    private boolean isWantToCancel(int x, int y) {

        final int DISTANCE_Y_CANCEL = 50;

        if ( x < 0 || x > getWidth() ) return true;

        if ( y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) return true;

        return false;
    }
}
