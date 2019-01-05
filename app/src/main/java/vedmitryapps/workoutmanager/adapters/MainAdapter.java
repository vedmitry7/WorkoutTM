package vedmitryapps.workoutmanager.adapters;

import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.mobiwise.library.ProgressLayout;
import io.realm.RealmResults;
import vedmitryapps.workoutmanager.Events;
import vedmitryapps.workoutmanager.R;
import vedmitryapps.workoutmanager.Util;
import vedmitryapps.workoutmanager.models.WorkOut;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private RealmResults<WorkOut> workOuts;

    Map<Long, Events.WorkoutStep> stepMap = new HashMap();


    // data is passed into the constructor
    public MainAdapter(RealmResults<WorkOut> data, Map stepMap) {
        this.workOuts = data;
        this.stepMap = stepMap;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workout_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.myTextView.setText( workOuts.get(position).getName());
        if(stepMap.containsKey(workOuts.get(position).getId())){

            Events.WorkoutStep workoutStep = stepMap.get(workOuts.get(position).getId());

            if(!workoutStep.isFinished()){
                holder.workoutTotalTime.setText(Util.secondsToTime(workoutStep.getTime()) + "/" + Util.secondsToTime(Util.totalTime(workOuts.get(position))));

                holder.exerciseName.setText(Util.getCurrentExercise(workOuts.get(position), stepMap.get(workOuts.get(position).getId()).getTime()).getName());
                holder.progressLayout.setVisibility(View.VISIBLE);
                holder.progressLayout.setMaxProgress(Util.totalTime(workOuts.get(position)));
                holder.progressLayout.setCurrentProgress(stepMap.get(workOuts.get(position).getId()).getTime());

                if(workoutStep.isPaused()){
                    holder.buttonPause.setVisibility(View.GONE);
                    holder.buttonPlay.setVisibility(View.VISIBLE);
                } else {
                    holder.buttonPause.setVisibility(View.VISIBLE);
                    holder.buttonPlay.setVisibility(View.GONE);
                }

            } else {
                holder.exerciseName.setText("Finished");
                holder.progressLayout.setVisibility(View.GONE);
                //  holder.workoutTotalTime.setText(Util.secondsToTime(Util.totalTime(workOuts.get(position))));
                holder.workoutTotalTime.setText(Util.secondsToTime(workoutStep.getTime()) + "/" + Util.secondsToTime(Util.totalTime(workOuts.get(position))));

                holder.mainContainer.setBackgroundColor(Color.parseColor("#662b56c6"));

                holder.buttonPause.setVisibility(View.GONE);
                holder.buttonPlay.setVisibility(View.VISIBLE);
            }

        } else {
            //Log.d("TAG21", "not contains ");
            holder.progressLayout.setVisibility(View.GONE);
            holder.workoutTotalTime.setText(Util.secondsToTime(Util.totalTime(workOuts.get(position))));
            holder.exerciseName.setText("");
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return workOuts.size();
    }

    public void updateItem2(int position) {
        Log.d("TAG21", "Update item - " + position);
        notifyItemChanged(position);
    }

    public void update(Map<Long, Events.WorkoutStep> stepMap) {
        this.stepMap = stepMap;
        notifyDataSetChanged();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView myTextView;

        @BindView(R.id.buttonPlay)
        ImageView buttonPlay;

        @BindView(R.id.buttonPause)
        ImageView buttonPause;

        @BindView(R.id.progressLayout)
        ProgressLayout progressLayout;

        @BindView(R.id.workoutTotalTime)
        TextView workoutTotalTime;

        @BindView(R.id.exerciseName)
        TextView exerciseName;

        @BindView(R.id.mainContainer)
        ConstraintLayout mainContainer;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            myTextView = itemView.findViewById(R.id.text);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventBus.getDefault().post(new Events.OpenWorkout(workOuts.get(getAdapterPosition()).getId()));

                }
            });

            buttonPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("TAG21", "click start");
                    EventBus.getDefault().post(new Events.StartWorkout(workOuts.get(getAdapterPosition()).getId(), 0));
                }
            });

            buttonPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("TAG21", "click pause");
                    EventBus.getDefault().post(new Events.PauseWorkout(workOuts.get(getAdapterPosition()).getId()));
                }
            });


        }

    }
}