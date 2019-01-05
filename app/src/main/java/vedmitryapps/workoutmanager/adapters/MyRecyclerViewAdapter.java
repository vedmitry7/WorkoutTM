package vedmitryapps.workoutmanager.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.mobiwise.library.ProgressLayout;
import io.realm.RealmList;
import vedmitryapps.workoutmanager.Events;
import vedmitryapps.workoutmanager.Mode;
import vedmitryapps.workoutmanager.R;
import vedmitryapps.workoutmanager.Util;
import vedmitryapps.workoutmanager.models.Exercise;
import vedmitryapps.workoutmanager.models.WorkOut;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> implements ItemTouchHelperAdapter, OnStartDragListener {

    private RealmList<Exercise> exercises;
    Mode mode = Mode.NORMAL;
    AdapterView.OnItemClickListener mItemClickListener;

    OnStartDragListener onStartDragListener;
    Events.WorkoutStep workoutStep;
    Context context;
    private WorkOut wotkout;

    int selectedItemPos = 0;

    int curExPos = 0;

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

        Log.d("TAG21", "From - " + fromPosition + " to - " + toPosition);
        notifyItemMoved(fromPosition, toPosition);
        exercises.move(fromPosition, toPosition);
        return false;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {

    }

    public void setReplaceMode(boolean b){
        if(b){
            mode = Mode.SETTINGS;
        } else {
            mode = Mode.NORMAL;
        }
        notifyDataSetChanged();
    }

    // data is passed into the constructor
    public MyRecyclerViewAdapter(RealmList<Exercise> data, OnStartDragListener onStartDragListener) {
        this.exercises = data;
        this.onStartDragListener = onStartDragListener;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(context==null){
            context = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exercise_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        if(mode.equals(Mode.NORMAL)){
            if(selectedItemPos==position){
                holder.selectedIcon.setVisibility(View.VISIBLE);
                holder.exerciseNamePadding.setVisibility(View.VISIBLE);
            } else {
                holder.selectedIcon.setVisibility(View.GONE);
                holder.exerciseNamePadding.setVisibility(View.GONE);
            }

            holder.replaceIcon.setVisibility(View.GONE);
            holder.deleteIcon.setVisibility(View.GONE);

            holder.progressLayout.setVisibility(View.GONE);
            holder.mainContainer.setBackgroundColor(Color.TRANSPARENT);

            holder.exerciseName.setTextColor(Color.parseColor("#7d8e98"));
            holder.exerciseTime.setTextColor(Color.parseColor("#7d8e98"));
        }

        if(mode == Mode.SETTINGS){
            holder.replaceIcon.setVisibility(View.VISIBLE);
            holder.deleteIcon.setVisibility(View.VISIBLE);
            holder.exerciseName.setText(exercises.get(position).getName());
            holder.selectedIcon.setVisibility(View.GONE);
            holder.progressLayout.setVisibility(View.GONE);

            holder.exerciseNamePadding.setVisibility(View.VISIBLE);
        }

        if(mode.equals(Mode.PLAYING)){
            holder.selectedIcon.setVisibility(View.GONE);
            holder.exerciseNamePadding.setVisibility(View.GONE);
            holder.replaceIcon.setVisibility(View.GONE);
            holder.deleteIcon.setVisibility(View.GONE);

            if(position<curExPos){
                holder.mainContainer.setBackgroundColor(Color.parseColor("#662b56c6"));
            } else {
                holder.mainContainer.setBackgroundColor(Color.TRANSPARENT);
            }
            if(workoutStep!=null&& wotkout!=null){
                if(position==curExPos){
                    Log.d("TAG21", "current exercise is - " + exercises.get(position).getName());

                    holder.exerciseName.setTextColor(Color.WHITE);
                    holder.exerciseTime.setTextColor(Color.WHITE);
                    holder.progressLayout.setVisibility(View.VISIBLE);
                    holder.progressLayout.setMaxProgress(exercises.get(position).getTimeInSeconds());
                    holder.progressLayout.setCurrentProgress(Util.getCurrentExerciseProgress(wotkout, workoutStep.getTime()));

                    if(workoutStep.isFinished()){
                        holder.progressLayout.setVisibility(View.GONE);
                        holder.mainContainer.setBackgroundColor(Color.parseColor("#662b56c6"));
                    }
                } else {
                    holder.progressLayout.setVisibility(View.GONE);
                    holder.exerciseName.setTextColor(Color.parseColor("#7d8e98"));
                    holder.exerciseTime.setTextColor(Color.parseColor("#7d8e98"));
                }
            }
        }

        holder.exerciseName.setText((position+1) + ". " + exercises.get(position).getName());
        holder.exerciseTime.setText(Util.secondsToTime(exercises.get(position).getTimeInSeconds()));
      /*  if(position%2==0){
            holder.mainContainer.setBackgroundColor(Color.parseColor("#7f36474f"));
        } else {
            holder.mainContainer.setBackgroundColor(Color.TRANSPARENT);
        }*/
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void update(RealmList<Exercise> excersices) {
        this.exercises = excersices;
        notifyDataSetChanged();
    }

    public void update(WorkOut workOut, Events.WorkoutStep workoutStep) {
        this.workoutStep = workoutStep;
        this.wotkout = workOut;
        //before we should get what is current exercise
        long id = Util.getCurrentExercise(workOut, workoutStep.getTime()).getId();

        if(workoutStep.isFinished() || workoutStep.isItterapted()){
            mode = Mode.NORMAL;
        } else {
            mode = Mode.PLAYING;
        }

        for (int i = 0; i < exercises.size(); i++) {
            if(exercises.get(i).getId()==id){
                curExPos = i;
                break;
            }
        }
        notifyDataSetChanged();
    }

    public int getStartPosition() {
        return selectedItemPos;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener ,ItemTouchHelperViewHolder{
        TextView exerciseName;
        TextView exerciseTime;

        @BindView(R.id.replaceIcon)
        ImageView replaceIcon;

        @BindView(R.id.deleteIcon)
        ImageView deleteIcon;

        @BindView(R.id.selectedItem)
        ImageView selectedIcon;

        @BindView(R.id.rowContainer)
        ConstraintLayout mainContainer;

        @BindView(R.id.progressLayout)
        ProgressLayout progressLayout;

        @BindView(R.id.exerciseNamePadding)
        View exerciseNamePadding;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            exerciseName = itemView.findViewById(R.id.exerciseName);
            exerciseTime = itemView.findViewById(R.id.exerciseTime);

            itemView.setOnClickListener(this);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // show context menu..
                    return true;
                }
            });

            deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    LayoutInflater inflater = (LayoutInflater) itemView.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                    dialogBuilder.setMessage("Вы действительно хотите удалить " + exercises.get(getAdapterPosition()).getName() + "?");

                    dialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    });
                    dialogBuilder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            exercises.remove(getAdapterPosition());
                            notifyItemRemoved(getAdapterPosition());
                            if(exercises.size()==0){
                                EventBus.getDefault().post(new Events.ClickExercise(-1));
                            } else {
                                if(selectedItemPos < exercises.size()){
                                    EventBus.getDefault().post(new Events.ClickExercise(getAdapterPosition()));
                                } else {
                                    selectedItemPos=0;
                                    EventBus.getDefault().post(new Events.ClickExercise(0));
                                }
                            }

                            dialog.dismiss();
                        }
                    });
                    AlertDialog b = dialogBuilder.create();
                    b.show();
                }
            });

        }

        @Override
        public void onClick(View view) {
            if(mode == Mode.NORMAL){
                EventBus.getDefault().post(new Events.ClickExercise(getAdapterPosition()));
                selectedItemPos = getAdapterPosition();
                notifyDataSetChanged();
            }
        }

        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {

        }
    }
}