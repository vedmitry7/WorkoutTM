package vedmitryapps.workoutmanager.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.mobiwise.library.ProgressLayout;
import io.realm.Realm;
import io.realm.RealmList;
import vedmitryapps.workoutmanager.App;
import vedmitryapps.workoutmanager.Constants;
import vedmitryapps.workoutmanager.Events;
import vedmitryapps.workoutmanager.Mode;
import vedmitryapps.workoutmanager.R;
import vedmitryapps.workoutmanager.SharedManager;
import vedmitryapps.workoutmanager.Util;
import vedmitryapps.workoutmanager.models.Exercise;
import vedmitryapps.workoutmanager.models.WorkOut;

public class ExerciseRecyclerAdapter extends RecyclerView.Adapter<ExerciseRecyclerAdapter.ViewHolder> implements ItemTouchHelperAdapter, OnStartDragListener {

    private RealmList<Exercise> exercises;
    Mode mode = Mode.NORMAL;
    AdapterView.OnItemClickListener mItemClickListener;
    Realm realm;
    OnStartDragListener onStartDragListener;
    Events.WorkoutStep workoutStep;
    Context context;
    private WorkOut wotkout;
    int selectedItemPos = 0;
    int curExPos = 0;
    boolean showNumber;
    boolean black;

    public ExerciseRecyclerAdapter(RealmList<Exercise> data, OnStartDragListener onStartDragListener) {
        this.exercises = data;
        this.onStartDragListener = onStartDragListener;
        showNumber = SharedManager.getProperty(Constants.KEY_SHOW_POSITION);
        realm = Realm.getDefaultInstance();
        black = !SharedManager.getProperty(Constants.KEY_BLACK_DISABLED);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);
        realm.beginTransaction();
        exercises.move(fromPosition, toPosition);
        realm.commitTransaction();
        if(selectedItemPos==fromPosition){
            selectedItemPos = toPosition;
        }
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
    }


    public int getSelectedItemPos() {
        return selectedItemPos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(context==null){
            context = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exercise_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.cardView.setBackgroundResource(R.drawable.workout_bg);

        if(black){
            holder.cardView.setBackgroundResource(R.drawable.workout_bg_black);
            holder.exerciseName.setTextColor(context.getResources().getColor(R.color.white_95));
            holder.exerciseTime.setTextColor(context.getResources().getColor(R.color.white_95));
            holder.settingsButton.setColorFilter(context.getResources().getColor(R.color.white_95));
        } else {
            holder.cardView.setBackgroundResource(R.drawable.workout_bg);
            holder.exerciseName.setTextColor(context.getResources().getColor(R.color.black_78));
            holder.settingsButton.setColorFilter(context.getResources().getColor(R.color.black_78));
        }

        if(mode.equals(Mode.NORMAL)){
            if(selectedItemPos==position){
                holder.selectedIcon.setVisibility(View.VISIBLE);
                holder.exerciseNamePadding.setVisibility(View.VISIBLE);
            } else {
                holder.selectedIcon.setVisibility(View.GONE);
                holder.exerciseNamePadding.setVisibility(View.GONE);
            }
            holder.progressLayout.setVisibility(View.GONE);
            holder.settingsButton.setVisibility(View.VISIBLE);

            if(workoutStep!=null && wotkout!=null && workoutStep.isFinished()){
                if(black){
                    holder.cardView.setBackgroundResource(R.drawable.workout_bg_finished_black);
                } else {
                    holder.cardView.setBackgroundResource(R.drawable.workout_bg_finished);
                }
            }
        }

        if(mode.equals(Mode.PLAYING)){
            holder.settingsButton.setVisibility(View.GONE);
            holder.selectedIcon.setVisibility(View.GONE);
            holder.exerciseNamePadding.setVisibility(View.GONE);

            if(position<curExPos){
                if(black){
                    holder.cardView.setBackgroundResource(R.drawable.workout_bg_finished_black);
                } else {
                    holder.cardView.setBackgroundResource(R.drawable.workout_bg_finished);
                }
            }
            if(workoutStep!=null && wotkout!=null){
                if(position==curExPos){
                    holder.progressLayout.setVisibility(View.VISIBLE);
                    holder.progressLayout.setMaxProgress(exercises.get(position).getTimeInSeconds());
                    holder.progressLayout.setCurrentProgress(Util.getCurrentExerciseProgress(wotkout, workoutStep.getTime()));

                    if(workoutStep.isFinished()){
                        holder.progressLayout.setVisibility(View.GONE);
                        if(black){
                            holder.cardView.setBackgroundResource(R.drawable.workout_bg_finished_black);
                        } else {
                            holder.cardView.setBackgroundResource(R.drawable.workout_bg_finished);
                        }
                    }
                } else {
                    holder.progressLayout.setVisibility(View.GONE);
                }
            }
        }

        if(showNumber){
            holder.exerciseName.setText((position+1) + ". " + exercises.get(position).getName());
        } else {
            holder.exerciseName.setText(exercises.get(position).getName());
        }
        holder.exerciseTime.setText(Util.secondsToTime(exercises.get(position).getTimeInSeconds()));
    }

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
        long id = Util.getCurrentExercise(workOut, workoutStep.getTime()).getId();

        if(workoutStep.isFinished() || workoutStep.isInterrupted()){
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

    public void setShowNumber(boolean showNumber) {
        this.showNumber = showNumber;
    }

    public class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener, ItemTouchHelperViewHolder{
        @BindView(R.id.exerciseName)
        TextView exerciseName;
        @BindView(R.id.exerciseTime)
        TextView exerciseTime;
        @BindView(R.id.selectedItem)
        ImageView selectedIcon;
        @BindView(R.id.settingsButton)
        ImageView settingsButton;
        @BindView(R.id.progressLayout)
        ProgressLayout progressLayout;
        @BindView(R.id.exerciseNamePadding)
        View exerciseNamePadding;
        @BindView(R.id.cardView)
        CardView cardView;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });

            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final int position = getAdapterPosition();
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    popupMenu.inflate(R.menu.exercise_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.duplicate:
                                    realm.beginTransaction();
                                    Exercise prototipe = exercises.get(getAdapterPosition());
                                    Exercise exercise = new Exercise();
                                    exercise.setId(App.getNextPeriodKey(realm));
                                    exercise.setName(prototipe.getName());
                                    exercise.setVibration(prototipe.isVibration());
                                    exercise.setSound(prototipe.getSound());
                                    exercise.setTimeInSeconds(prototipe.getTimeInSeconds());
                                    exercises.add(getAdapterPosition()+1, exercise);
                                    realm.commitTransaction();
                                    notifyDataSetChanged();
                                    EventBus.getDefault().post(new Events.ClickExercise(selectedItemPos));
                                    return true;
                                case R.id.delete:
                                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                                    LayoutInflater inflater = (LayoutInflater) itemView.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                                    dialogBuilder.setMessage(context.getString(R.string.q_delete_exercise) + " " + exercises.get(getAdapterPosition()).getName() + "?");

                                    dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.dismiss();
                                        }
                                    });
                                    dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            realm.beginTransaction();
                                            exercises.remove(position);
                                            realm.commitTransaction();
                                            notifyItemRemoved(position);
                                            if(exercises.size()==0){
                                                EventBus.getDefault().post(new Events.ClickExercise(-1));
                                            } else {
                                                if(selectedItemPos < exercises.size()){
                                                     EventBus.getDefault().post(new Events.ClickExercise(selectedItemPos));
                                                } else {
                                                    selectedItemPos=0;
                                                    EventBus.getDefault().post(new Events.ClickExercise(0));
                                                }
                                            }
                                            dialog.dismiss();

                                            if(selectedItemPos == position){
                                                selectedItemPos = 0;
                                                notifyItemChanged(selectedItemPos);
                                            }
                                        }
                                    });
                                    AlertDialog b = dialogBuilder.create();
                                    b.show();
                                    return true;
                                case R.id.edit:
                                    final Exercise exercise1 = exercises.get(getAdapterPosition());
                                    EventBus.getDefault().post(exercise1);
                                    return true;
                            }
                            return false;
                        }
                    });

                    popupMenu.show();
                }
            });
        }

        @Override
        public void onClick(View view) {
            final int itemPosition = getAdapterPosition();
            if(mode == Mode.NORMAL){
                EventBus.getDefault().post(new Events.ClickExercise(getAdapterPosition()));
                selectedItemPos = itemPosition;
                notifyDataSetChanged();
            }
        }

        @Override
        public void onItemSelected() {
            if(black){
                cardView.setBackgroundResource(R.drawable.workout_bg_selected_black);
            } else {
                cardView.setBackgroundResource(R.drawable.workout_bg_selected);
            }
        }

        @Override
        public void onItemClear() {
            cardView.setBackgroundResource(R.drawable.workout_bg);
            notifyDataSetChanged();

        }
    }
}