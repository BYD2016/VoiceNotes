package codepath.com.cn.imrecord;

import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import codepath.com.cn.imrecord.adapter.AudioRecorderAdapter;
import codepath.com.cn.imrecord.media.MediaManager;
import codepath.com.cn.imrecord.view.AudioRecordButton;

public class MainActivity extends AppCompatActivity
        implements AudioRecordButton.AudioFinishRecorderListenter, AdapterView.OnItemClickListener {

    @BindView(R.id.lvMessages)
    ListView mLvMessages;
    @BindView(R.id.btnRecordAudio)
    AudioRecordButton mBtnRecordAudio;

    private ArrayAdapter<Recorder> mAdapter;
    private View mAnimView;

    private SimpleDateFormat mSimpleDateFormat  =
            new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");

    private ExecutorService mMediaExecutorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mBtnRecordAudio.setMediaExecutorService(mMediaExecutorService);

        mAdapter = new AudioRecorderAdapter(this);
        mLvMessages.setAdapter(mAdapter);
        mLvMessages.setOnItemClickListener(this);

        mBtnRecordAudio.setAudioFinishRecorderListenter(this);

        mMediaExecutorService = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onDestroy() {
        mMediaExecutorService.shutdownNow();
        super.onDestroy();
    }

    @Override
    public void onFinish(float seconds, String filePath) {
        Date curDate =  new Date(System.currentTimeMillis());
        Recorder recorder = new Recorder(seconds, filePath,
                mSimpleDateFormat.format(curDate));
        mAdapter.add(recorder);
        mLvMessages.setSelection(mAdapter.getCount() - 1);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mAnimView != null) {
            mAnimView.setBackgroundResource(R.drawable.adj);
            mAnimView = null;
        }

        // 播放动画
        mAnimView = view.findViewById(R.id.recorder_anim);
        mAnimView.setBackgroundResource(R.drawable.audio_play_anim);
        AnimationDrawable anim = (AnimationDrawable) mAnimView
                .getBackground();
        anim.start();

        final int currentPos = position;
        final AudioRecorderAdapter arrayAdapter = (AudioRecorderAdapter) mAdapter;

        // 播放音频
        mMediaExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                MediaManager.playSound(arrayAdapter.getItem(currentPos).filePath,
                        new MediaPlayer.OnCompletionListener() {

                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mAnimView.setBackgroundResource(R.drawable.adj);
                            }
                        });
            }
        });

    }

    /**
     * 录音记录类（备忘信息）
     */
    public static class Recorder {
        float time;         //备忘录音时间长度
        String filePath;    //备忘录音文件路径
        String mCurrentTime;//备忘录音时的系统时间

        public Recorder(float time, String filePath, String currentTime) {
            super();
            this.time = time;
            this.filePath = filePath;
            this.mCurrentTime = currentTime;
        }

        public float getTime() {
            return time;
        }

        public void setTime(float time) {
            this.time = time;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getmCurrentTime() {
            return mCurrentTime;
        }

        public void setmCurrentTime(String mCurrentTime) {
            this.mCurrentTime = mCurrentTime;
        }

    }
}
