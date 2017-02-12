package codepath.com.cn.imrecord.media;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * 主要管理语音的播放、暂停、恢复、停止
 * <p>
 * Created by admin on 2017/2/11.
 */

public final class MediaManager {

    public static final String TAG = MediaManager.class.getSimpleName();

    private static MediaPlayer sMediaPlayer;
    private static boolean sIsPause;

    public static void playSound(String audioFile,
                                 MediaPlayer.OnCompletionListener onCompletionListenter) {

        if (sMediaPlayer == null) {
            sMediaPlayer = new MediaPlayer();

            sMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    sMediaPlayer.reset();
                    return false;
                }

            });

        } else {
            sMediaPlayer.reset();
        }

        try {
            sMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            sMediaPlayer.setOnCompletionListener(onCompletionListenter);
            sMediaPlayer.setDataSource(audioFile);

            sMediaPlayer.prepare();
            sMediaPlayer.start();

        } catch (RuntimeException | IOException e) {
            Log.e(TAG, "播放音频失败。", e);
        }

    }

    public static void pause() {
        if (sMediaPlayer !=null && sMediaPlayer.isPlaying()){
            sMediaPlayer.pause();
            sIsPause =true;
        }
    }

    public static void resume(){
        if (sMediaPlayer !=null && sIsPause){
            sMediaPlayer.start();
            sIsPause =false;
        }
    }

    public static void release(){
        if(sMediaPlayer !=null){
            sMediaPlayer.release();
            sMediaPlayer =null;
        }
    }
}
