package vedmitryapps.workoutmanager.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.shawnlin.numberpicker.NumberPicker;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.mobiwise.library.ProgressLayout;
import io.realm.Realm;
import io.realm.RealmList;
import vedmitryapps.workoutmanager.App;
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


    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

        Log.d("TAG21", "From - " + fromPosition + " to - " + toPosition);
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

    public int getCurExPos() {
        return curExPos;
    }

    public int getSelectedItemPos() {
        return selectedItemPos;
    }

    // data is passed into the constructor
    public ExerciseRecyclerAdapter(RealmList<Exercise> data, OnStartDragListener onStartDragListener) {
        this.exercises = data;
        this.onStartDragListener = onStartDragListener;
        showNumber = SharedManager.getProperty("showNumber");
        realm = Realm.getDefaultInstance();
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


            holder.progressLayout.setVisibility(View.GONE);
            holder.mainContainer.setBackgroundColor(Color.TRANSPARENT);

            holder.exerciseName.setTextColor(Color.parseColor("#7d8e98"));
            holder.exerciseTime.setTextColor(Color.parseColor("#7d8e98"));
            holder.settingsButton.setVisibility(View.VISIBLE);
        }

        if(mode.equals(Mode.PLAYING)){
            holder.settingsButton.setVisibility(View.GONE);
            holder.selectedIcon.setVisibility(View.GONE);
            holder.exerciseNamePadding.setVisibility(View.GONE);

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

        if(showNumber){
            holder.exerciseName.setText((position+1) + ". " + exercises.get(position).getName());
        } else {
            holder.exerciseName.setText(exercises.get(position).getName());
        }
        holder.exerciseTime.setText(Util.secondsToTime(exercises.get(position).getTimeInSeconds()));
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

    public void setShowNumber(boolean showNumber) {
        this.showNumber = showNumber;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener, ItemTouchHelperViewHolder{
        TextView exerciseName;
        TextView exerciseTime;

        @BindView(R.id.selectedItem)
        ImageView selectedIcon;

        @BindView(R.id.settingsButton)
        ImageView settingsButton;

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

            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, v);

                    popupMenu.inflate(R.menu.exercise_menu);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.duplicate:
                                    Log.d("TAG21", "Ex s = 0 " + getAdapterPosition());

                                    realm.beginTransaction();
                                    Exercise prototipe = exercises.get(getAdapterPosition());
                                    Exercise exercise = new Exercise();
                                    exercise.setId(App.getNextPeriodKey(realm));
                                    exercise.setName(prototipe.getName());
                                    exercise.setVibration(prototipe.isVibration());
                                    exercise.setSound(prototipe.getSound());
                                    exercise.setTimeInSeconds(prototipe.getTimeInSeconds());
                                    exercises.add(getAdapterPosition(), exercise);
                                    realm.commitTransaction();
                                    notifyItemInserted(getAdapterPosition()+1);


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
                                            exercises.remove(getAdapterPosition());
                                            realm.commitTransaction();
                                            notifyItemRemoved(getAdapterPosition());
                                            if(exercises.size()==0){
                                                Log.d("TAG21", "Ex s = 0 ");
                                                EventBus.getDefault().post(new Events.ClickExercise(-1));
                                            } else {
                                                Log.d("TAG21", "Ex s not 0 ");
                                                if(selectedItemPos < exercises.size()){
                                                    Log.d("TAG21", "selectedItemPos < exercises.size() - " + exercises.size() + " a p " + getAdapterPosition());

                                                    EventBus.getDefault().post(new Events.ClickExercise(selectedItemPos));
                                                } else {
                                                    Log.d("TAG21", "selectedItemPos >= exercises.size()");
                                                    selectedItemPos=0;
                                                    EventBus.getDefault().post(new Events.ClickExercise(0));
                                                }
                                            }
                                            dialog.dismiss();
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
        }

        @Override
        public void onItemClear() {
        }
    }

    private void showChooseSoundDialog() {
        Log.d("TAG21", "click show sound dialog");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View dialogView = inflater.inflate(R.layout.choose_sound_dialog_layout, null);
        dialogBuilder.setView(dialogView);

        final RecyclerView recyclerView = dialogView.findViewById(R.id.soundRecyclerView);

        final SoundRecyclerAdapter adapter = new SoundRecyclerAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }
}