package vedmitryapps.workoutmanager.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.mobiwise.library.ProgressLayout;
import io.realm.Realm;
import io.realm.RealmList;
import vedmitryapps.workoutmanager.Constants;
import vedmitryapps.workoutmanager.Events;
import vedmitryapps.workoutmanager.Mode;
import vedmitryapps.workoutmanager.R;
import vedmitryapps.workoutmanager.SharedManager;
import vedmitryapps.workoutmanager.Util;
import vedmitryapps.workoutmanager.models.WorkOut;

public class WorkoutRecyclerAdapter extends RecyclerView.Adapter<WorkoutRecyclerAdapter.ViewHolder> implements ItemTouchHelperAdapter, OnStartDragListener{

    Context context;
    private RealmList<WorkOut> workOuts;
    Mode mode = Mode.NORMAL;
    OnStartDragListener onStartDragListener;

    Map<Long, Events.WorkoutStep> stepMap = new HashMap();

    Realm realm;
    private int fromPosition;
    private int toPosition;

    String progress;

    boolean black;

    // data is passed into the constructor
    public WorkoutRecyclerAdapter(RealmList<WorkOut> data, Map stepMap, OnStartDragListener onStartDragListener) {
        this.workOuts = data;
        this.stepMap = stepMap;
        this.onStartDragListener = onStartDragListener;
        realm = Realm.getDefaultInstance();

        black = !SharedManager.getProperty(Constants.KEY_BLACK_DISABLED);
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(context == null){
            context = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workout_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.workoutName.setText( workOuts.get(position).getName());


        if(black){
            holder.cardView.setBackgroundResource(R.drawable.workout_bg_black);
            holder.workoutName.setTextColor(context.getResources().getColor(R.color.white_95));
            holder.exerciseName.setTextColor(context.getResources().getColor(R.color.white_95));
            holder.repeating.setTextColor(context.getResources().getColor(R.color.white_95));
            holder.workoutTotalTime.setTextColor(context.getResources().getColor(R.color.white_95));
            holder.buttonPlay.setColorFilter(context.getResources().getColor(R.color.white_95));
            holder.buttonPause.setColorFilter(context.getResources().getColor(R.color.white_95));
        } else {
            holder.cardView.setBackgroundResource(R.drawable.workout_bg);
            holder.exerciseName.setTextColor(context.getResources().getColor(R.color.black_78));
            holder.repeating.setTextColor(context.getResources().getColor(R.color.black_78));
            holder.workoutTotalTime.setTextColor(context.getResources().getColor(R.color.black_78));
            holder.buttonPlay.setColorFilter(context.getResources().getColor(R.color.black_78));
            holder.buttonPause.setColorFilter(context.getResources().getColor(R.color.black_78));
        }

        if(stepMap.containsKey(workOuts.get(position).getId())){

            Events.WorkoutStep workoutStep = stepMap.get(workOuts.get(position).getId());

            if(!SharedManager.getProperty(Constants.KEY_COUNTDOWN_TOTAL_DISABLED)){
                progress =  Util.secondsToTime(Util.totalTime(workOuts.get(position))-stepMap.get(workoutStep.getId()).getTime()) + "/" + Util.secondsToTime(Util.totalTime(workOuts.get(position)));
            } else {
                progress = Util.secondsToTime(stepMap.get(workoutStep.getId()).getTime()) + "/" + Util.secondsToTime(Util.totalTime(workOuts.get(position)));
            }


            if(!workoutStep.isFinished()){
                holder.workoutTotalTime.setText(progress);

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

                if(workoutStep.isInterrupted()){
                    holder.buttonPause.setVisibility(View.GONE);
                    holder.buttonPlay.setVisibility(View.VISIBLE);
                    holder.workoutTotalTime.setText(Util.secondsToTime(Util.totalTime(workOuts.get(position))));
                }

                if(workoutStep.getRepeating()==0){
                    holder.repeating.setVisibility(View.GONE);
                } else {
                    holder.repeating.setVisibility(View.VISIBLE);
                    holder.repeating.setText("R" + workoutStep.getRepeating());
                }
            } else {
                holder.exerciseName.setText(context.getString(R.string.finished));
                holder.progressLayout.setVisibility(View.GONE);
                holder.workoutTotalTime.setText(progress);
                holder.buttonPause.setVisibility(View.GONE);
                holder.buttonPlay.setVisibility(View.VISIBLE);
                holder.repeating.setVisibility(View.GONE);

                if(black){
                    holder.cardView.setBackgroundResource(R.drawable.workout_bg_finished_black);
                } else {
                    holder.cardView.setBackgroundResource(R.drawable.workout_bg_finished);
                }
            }
        } else {
            holder.progressLayout.setVisibility(View.GONE);
            holder.workoutTotalTime.setText(Util.secondsToTime(Util.totalTime(workOuts.get(position))));
            holder.exerciseName.setText("");
            holder.buttonPlay.setVisibility(View.VISIBLE);
            holder.buttonPause.setVisibility(View.GONE);
            holder.repeating.setVisibility(View.GONE);
        }

        if(mode == Mode.DRAG_AND_DROP){
            holder.buttonPlay.setVisibility(View.GONE);
            holder.buttonPause.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new Events.OpenWorkout(workOuts.get(position).getId()));
            }
        });

        holder.buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(workOuts.get(position).getExcersices().size()!=0)
                    EventBus.getDefault().post(new Events.StartWorkout(workOuts.get(position).getId(), 0));
                else
                    Toast.makeText(holder.itemView.getContext(), context.getResources().getString(R.string.nothing_to_play), Toast.LENGTH_SHORT).show();
            }
        });

        holder.buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new Events.PauseWorkout(workOuts.get(position).getId()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return workOuts.size();
    }


    public void update(Map<Long, Events.WorkoutStep> stepMap) {
        this.stepMap = stepMap;
        if(mode != Mode.DRAG_AND_DROP){
            notifyDataSetChanged();
        }
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);
        realm.beginTransaction();
        workOuts.move(fromPosition, toPosition);
        realm.commitTransaction();
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
    }

    public class ViewHolder extends RecyclerView.ViewHolder  implements ItemTouchHelperViewHolder{

        @BindView(R.id.text)
        TextView workoutName;

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

        @BindView(R.id.repeating)
        TextView repeating;

        @BindView(R.id.cardView)
        CardView cardView;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onItemSelected() {
            mode = Mode.DRAG_AND_DROP;
            if(black){
                cardView.setBackgroundResource(R.drawable.workout_bg_selected_black);
            } else {
                cardView.setBackgroundResource(R.drawable.workout_bg_selected);
            }
        }

        @Override
        public void onItemClear() {
            mode = Mode.NORMAL;
            cardView.setBackgroundResource(R.drawable.workout_bg);
            notifyDataSetChanged();
        }
    }
}