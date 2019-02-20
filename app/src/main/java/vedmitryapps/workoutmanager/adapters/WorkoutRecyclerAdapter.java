package vedmitryapps.workoutmanager.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import vedmitryapps.workoutmanager.Events;
import vedmitryapps.workoutmanager.Mode;
import vedmitryapps.workoutmanager.R;
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

    // data is passed into the constructor
    public WorkoutRecyclerAdapter(RealmList<WorkOut> data, Map stepMap, OnStartDragListener onStartDragListener) {
        this.workOuts = data;
        this.stepMap = stepMap;
        this.onStartDragListener = onStartDragListener;
        realm = Realm.getDefaultInstance();
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

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.workoutName.setText( workOuts.get(position).getName());

        if(stepMap.containsKey(workOuts.get(position).getId())){

            Events.WorkoutStep workoutStep = stepMap.get(workOuts.get(position).getId());

            if(!workoutStep.isFinished()){
                holder.workoutTotalTime.setText(Util.secondsToTime(workoutStep.getTime()) + "/" + Util.secondsToTime(Util.totalTime(workOuts.get(position))));

                holder.exerciseName.setText(Util.getCurrentExercise(workOuts.get(position), stepMap.get(workOuts.get(position).getId()).getTime()).getName());
                holder.progressLayout.setVisibility(View.VISIBLE);
                holder.progressLayout.setMaxProgress(Util.totalTime(workOuts.get(position)));
                holder.progressLayout.setCurrentProgress(stepMap.get(workOuts.get(position).getId()).getTime());

                holder.mainContainer.setBackgroundColor(Color.TRANSPARENT);


                if(workoutStep.isPaused()){
                    holder.buttonPause.setVisibility(View.GONE);
                    holder.buttonPlay.setVisibility(View.VISIBLE);
                } else {
                    holder.buttonPause.setVisibility(View.VISIBLE);
                    holder.buttonPlay.setVisibility(View.GONE);
                }

                if(workoutStep.isItterapted()){
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
                holder.exerciseName.setText("Finished");
                holder.progressLayout.setVisibility(View.GONE);
                holder.workoutTotalTime.setText(Util.secondsToTime(workoutStep.getTime()) + "/" + Util.secondsToTime(Util.totalTime(workOuts.get(position))));

                holder.mainContainer.setBackgroundColor(Color.parseColor("#662b56c6"));
                holder.buttonPause.setVisibility(View.GONE);
                holder.buttonPlay.setVisibility(View.VISIBLE);
                holder.repeating.setVisibility(View.GONE);

            }
        } else {
            //Log.d("TAG21", "not contains ");
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

            /*holder.replaceIcon.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Log.d("TAG21", "action down");
                        onStartDragListener.onStartDrag(holder);
                    }
                    return false;
                }
            });*/
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
                Log.d("TAG21", "click start");
                if(workOuts.get(position).getExcersices().size()!=0)
                    EventBus.getDefault().post(new Events.StartWorkout(workOuts.get(position).getId(), 0));
                else
                    Toast.makeText(holder.itemView.getContext(), "Нечего проигрывать", Toast.LENGTH_SHORT).show();
            }
        });

        holder.buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TAG21", "click pause");
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
        Log.d("TAG21", "Ws.From - " + fromPosition + " to - " + toPosition);
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
        Log.d("TAG21", "onItemDismiss");

    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        Log.d("TAG21", "onStartDrag");

    }

    // stores and recycles views as they are scrolled off screen
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

        @BindView(R.id.mainContainer)
        ConstraintLayout mainContainer;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onItemSelected() {
            Log.d("TAG21", "onItemSelected");
            workoutName.setTextColor(Color.RED);
            mode = Mode.DRAG_AND_DROP;
        }

        @Override
        public void onItemClear() {
            Log.d("TAG21", "onItemClear");
            workoutName.setTextColor(Color.WHITE);
            mode = Mode.NORMAL;
            notifyItemChanged(fromPosition);
            notifyItemChanged(toPosition);
        }
    }
}