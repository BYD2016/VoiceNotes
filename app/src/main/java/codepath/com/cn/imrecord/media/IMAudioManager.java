package codepath.com.cn.imrecord.media;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 单例实现 AudioManage
 *
 * Created by admin on 2017/2/11.
 */

public final class IMAudioManager {

    private static final String TAG = IMAudioManager.class.getSimpleName();

    private MediaRecorder mMediaRecorder;

    // 文件夹的名称
    private String mDir;

    private String mCurrentFilePath;

    private volatile static IMAudioManager mInstance;

    private AudioStateListenter mListenter;

    // 标识MediaRecorder准备完毕
    private boolean mIsPrepared;

    private IMAudioManager(String dir) {
        mDir = dir;
    }

    public static IMAudioManager getInstance(String dir) {
        if (mInstance == null) {
            synchronized (IMAudioManager.class) { // 同步
                if (mInstance == null) {
                    mInstance = new IMAudioManager(dir);
                }
            }
        }

        return mInstance;
    }

    public void setOnAudioStateListenter(AudioStateListenter audioStateListenter) {
        mListenter = audioStateListenter;
    }

    // 准备  
    public void prepareAudio() {

        if (mIsPrepared) return;

        try {

            File dir = new File(mDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 文件名字 
            String fileName = GenerateFileName();

            // 路径+文件名字
            Log.d(TAG, "Create file:" + dir + "/" + fileName);
            File audioFile = new File(dir, fileName);
            audioFile.createNewFile();
            mCurrentFilePath = audioFile.getAbsolutePath();

            mMediaRecorder = new MediaRecorder();

            // 设置MediaRecorder的音频源为麦克风
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            // 设置音频的格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);

            // 设置音频的编码为AMR_NB
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);

            // 设置输出文件
            mMediaRecorder.setOutputFile(mCurrentFilePath);

            mMediaRecorder.prepare();
            mMediaRecorder.start();

            // 准备结束
            mIsPrepared = true;

            Log.d(TAG, "prepareAudio ok!)");

            if (mListenter != null) {
                mListenter.recordingPrepared();
            }
            
        } catch (IllegalStateException | IOException e) {
            Log.e(TAG, "准备录音失败。", e);
        } 

    }

    private String GenerateFileName() {
        return UUID.randomUUID().toString() + ".amr"; // 音频文件格式
    }


    /**
     * 获得音量等级，在子线程运行
     * @param maxLevel
     * @return
     */
    public int getVoiceLevel(int maxLevel) {
        if (mIsPrepared) {
            try {
                return maxLevel * mMediaRecorder.getMaxAmplitude() / 32768 + 1;
            } catch (Exception e) {
                Log.e(TAG, "获得音量等级失败。", e);
            }
        }
        return 1;
    }

    // 释放
    public void release() {
        mIsPrepared = false;

        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    // 取消
    public void cancel() {

        release();

        if (mCurrentFilePath != null) {
            File file = new File(mCurrentFilePath);
            //删除录音文件
            file.delete();
            mCurrentFilePath = null;
        }
    }

    public String getCurrentFilePath() {
        return mCurrentFilePath;
    }

    // 回调准备完毕
    public interface AudioStateListenter {
        // prepared完毕
        void recordingPrepared();
    }
}
