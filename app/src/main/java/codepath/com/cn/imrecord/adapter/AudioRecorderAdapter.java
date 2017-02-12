package codepath.com.cn.imrecord.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import codepath.com.cn.imrecord.MainActivity;
import codepath.com.cn.imrecord.R;

/**
 * Created by admin on 2017/2/11.
 */

public class AudioRecorderAdapter extends ArrayAdapter<MainActivity.Recorder> {

    private int mMinItemWidth;
    private int mMaxItemWidth;
    private LayoutInflater mLayoutInflater;

    public AudioRecorderAdapter(Context context) {
        super(context, R.layout.item_recorder);

        mLayoutInflater = LayoutInflater.from(context);

        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);

        mMaxItemWidth = (int) (outMetrics.widthPixels * 0.7f);
        mMinItemWidth = (int) (outMetrics.widthPixels * 0.15f);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_recorder, parent, false);
            viewHolder = new ViewHolder(convertView, mMinItemWidth, mMaxItemWidth);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.bindView(getItem(position));

        return convertView;
    }

    static class ViewHolder {

//        @BindView(R.id.voice_robot)
//        ImageView mVoiceRobot;
//        @BindView(R.id.recorder_anim)
//        View mRecorderAnim;

        @BindView(R.id.recorder_length)
        FrameLayout mRecorderLength;
        @BindView(R.id.recorder_time)
        TextView mRecorderTime;

        @BindView(R.id.recorder_send_time)
        TextView mRecorderSendTime;

        private int mMinItemWidth;
        private int mMaxItemWidth;

        ViewHolder(View view, int minItemWidth, int maxItemWidth) {
            mMinItemWidth = minItemWidth;
            mMaxItemWidth = maxItemWidth;
            ButterKnife.bind(this, view);
        }

        void bindView(MainActivity.Recorder recorder) {
            mRecorderTime.setText(String.format("%.2f\"", recorder.getTime()));

            mRecorderSendTime.setText(recorder.getmCurrentTime());

            ViewGroup.LayoutParams layoutParams = mRecorderLength.getLayoutParams();
            layoutParams.width= Math.min(
                    (int) (mMinItemWidth + (mMaxItemWidth / 60f * recorder.getTime()))
                    , mMaxItemWidth);
        }
    }
}
