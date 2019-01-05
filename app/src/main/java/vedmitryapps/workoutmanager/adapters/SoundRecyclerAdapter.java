package vedmitryapps.workoutmanager.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.shawnlin.numberpicker.NumberPicker;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.mobiwise.library.ProgressLayout;
import io.realm.RealmList;
import vedmitryapps.workoutmanager.Events;
import vedmitryapps.workoutmanager.Mode;
import vedmitryapps.workoutmanager.R;
import vedmitryapps.workoutmanager.SharedManager;
import vedmitryapps.workoutmanager.Util;
import vedmitryapps.workoutmanager.models.Exercise;
import vedmitryapps.workoutmanager.models.WorkOut;

public class SoundRecyclerAdapter extends RecyclerView.Adapter<SoundRecyclerAdapter.ViewHolder>{


    Context context;

    String[] soundsName = {"", "03963.mp3", "03965.mp3"};
    String[] soundsTitle = {"none", "Sound 1", "Sound 2"};


    int selectedPosition = 0;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(context==null){
            context = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sound_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.soundName.setText(soundsTitle[position]);

        if(selectedPosition == position){
            holder.indicator.setVisibility(View.VISIBLE);
        } else {
            holder.indicator.setVisibility(View.GONE);
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return 3;
    }

    public String getSoundName() {
        return soundsTitle[selectedPosition];
    }

    public String getSoundNameByPosition(int position) {
        return soundsTitle[position];
    }

    public int getSoundPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.soundName)
        TextView soundName;

        @BindView(R.id.indicator)
        ImageView indicator;

        @BindView(R.id.soundContainer)
        ConstraintLayout container;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPosition = getAdapterPosition();

                    if(selectedPosition==0){
                        notifyDataSetChanged();
                        return;
                    }

                    MediaPlayer mp;
                    mp = new MediaPlayer();
                    AssetFileDescriptor afd = null;
                    try {
                        afd = context.getAssets().openFd(soundsName[selectedPosition]);
                        mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        //mp.setVolume(1f, 1f);
                        mp.prepare();
                        mp.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    notifyDataSetChanged();
                }


            });
        }
    }

}