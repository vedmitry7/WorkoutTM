package vedmitryapps.workoutmanager.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.provider.SyncStateContract;
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
import vedmitryapps.workoutmanager.Constants;
import vedmitryapps.workoutmanager.Events;
import vedmitryapps.workoutmanager.Mode;
import vedmitryapps.workoutmanager.R;
import vedmitryapps.workoutmanager.SharedManager;
import vedmitryapps.workoutmanager.Util;
import vedmitryapps.workoutmanager.models.Exercise;
import vedmitryapps.workoutmanager.models.WorkOut;

public class SoundRecyclerAdapter extends RecyclerView.Adapter<SoundRecyclerAdapter.ViewHolder>{


    Context context;
    int selectedPosition = 0;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(context==null){
            context = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sound_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.soundName.setText(Constants.soundsTitle[position]);
        if(selectedPosition == position){
            holder.indicator.setVisibility(View.VISIBLE);
        } else {
            holder.indicator.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return Constants.soundsTitle.length;
    }

    public String getSoundName() {
        return Constants.soundsTitle[selectedPosition];
    }

    public String getSoundNameByPosition(int position) {
        return Constants.soundsTitle[position];
    }

    public int getSoundPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

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
                        afd = context.getAssets().openFd(Constants.soundsName[selectedPosition]);
                        mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
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