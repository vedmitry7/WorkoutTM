package vedmitryapps.workoutmanager.fragments;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shawnlin.numberpicker.NumberPicker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import vedmitryapps.workoutmanager.App;
import vedmitryapps.workoutmanager.Constants;
import vedmitryapps.workoutmanager.Events;
import vedmitryapps.workoutmanager.SharedManager;
import vedmitryapps.workoutmanager.Storage;
import vedmitryapps.workoutmanager.adapters.ExerciseTouchHelperCallback;
import vedmitryapps.workoutmanager.Mode;
import vedmitryapps.workoutmanager.adapters.ExerciseRecyclerAdapter;
import vedmitryapps.workoutmanager.adapters.OnStartDragListener;
import vedmitryapps.workoutmanager.R;
import vedmitryapps.workoutmanager.Util;
import vedmitryapps.workoutmanager.adapters.SoundRecyclerAdapter;
import vedmitryapps.workoutmanager.models.Exercise;
import vedmitryapps.workoutmanager.models.WorkOut;

public class WorkOutFragment extends Fragment  {

    @BindView(R.id.mainRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.exerciseName)
    TextView exerciseName;

    @BindView(R.id.totalProgress)
    TextView totalTime;

    @BindView(R.id.workoutPanel)
    ConstraintLayout panel;

    @BindView(R.id.mainContainer)
    ConstraintLayout mainContainer;

    @BindView(R.id.bottomButton)
    ConstraintLayout bottomButton;

    @BindView(R.id.bottomButtonIcon)
    ImageView bottomButtonIcon;

    @BindView(R.id.bottomButtonText)
    TextView bottomButtonText;

    @BindView(R.id.buttonPlay)
    ImageView buttonPlay;

    @BindView(R.id.buttonPause)
    ImageView buttonPause;

    @BindView(R.id.workoutSettings)
    ImageView workoutSettings;

    @BindView(R.id.exerciseCurrentTime)
    TextView exerciseCurrentTime;

    @BindView(R.id.exerciseTotalTime)
    TextView exerciseTotalTime;

    TextView soundName;

    Realm mRealm;

    WorkOut workOut;
    RealmResults<Exercise> exercises;

    ExerciseRecyclerAdapter adapter;

    Mode mode = Mode.NORMAL;

    ItemTouchHelper itemTouchHelper;

    Storage storage;

    Map<Long, Events.WorkoutStep> stepMap = new HashMap();

    int soundPosition = 0;

    public static WorkOutFragment createInstance(long id) {
        WorkOutFragment fragment = new WorkOutFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("id", id);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.workout_fragment, container, false);

        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedManager.init(getContext());

        Log.d("TAG21", "Workout onViewCreated ");
        mRealm = Realm.getDefaultInstance();
        workOut = mRealm.where(WorkOut.class).equalTo("id", getArguments().getLong("id")).findFirst();

        title.setText(workOut.getName());
        totalTime.setText("Общее время: " + Util.secondsToTime(Util.totalTime(workOut)));

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        adapter = new ExerciseRecyclerAdapter(workOut.getExcersices(), new OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                itemTouchHelper.startDrag(viewHolder);
            }
        });
        ItemTouchHelper.Callback callback = new ExerciseTouchHelperCallback(adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);

        if(stepMap.containsKey(workOut.getId())){
            Log.d("TAG21", "Workout Step contains it");
            Events.WorkoutStep workoutStep = stepMap.get(workOut.getId());
            setStepInfo(workoutStep);
            adapter.update(workOut, stepMap.get(workoutStep.getId()));
        } else {
            if(workOut.getExcersices().size()==0){
                Log.d("TAG21", "workOut.getExcersices().size() - " + workOut.getExcersices().size());
                onClickExercise(new Events.ClickExercise(-1));
            }
            else {
                Log.d("TAG21", "workOut.getExcersices().size() - " + workOut.getExcersices().size());
                onClickExercise(new Events.ClickExercise(0));
            }
            buttonPlay.setVisibility(View.VISIBLE);
        }

    }

    private void setStepInfo(Events.WorkoutStep workoutStep) {
        if(workoutStep.isFinished() || workoutStep.isItterapted()){
            mode = Mode.NORMAL;
            setBottomButtonParams(mode);

            onClickExercise(new Events.ClickExercise(adapter.getStartPosition()));

            showPlay();
            Log.d("TAG21", "buttonPause gone");
        } else {
            if(workoutStep.isPaused()){
                showPlay();
            } else {
                showPause();
            }
            mode = Mode.PLAYING;
            setBottomButtonParams(mode);
            totalTime.setText(Util.secondsToTime(stepMap.get(workoutStep.getId()).getTime()) + "/" + Util.secondsToTime(Util.totalTime(workOut)));
            exerciseName.setText(Util.getCurrentExercise(workOut, stepMap.get(workoutStep.getId()).getTime()).getName());
            exerciseCurrentTime.setText("" + Util.getCurrentExerciseProgressString(workOut, stepMap.get(workoutStep.getId()).getTime()));
        }
        exerciseTotalTime.setText("/" + Util.secondsToTime(Util.getCurrentExercise(workOut, stepMap.get(workoutStep.getId()).getTime()).getTimeInSeconds()));
    }

    void showPause(){
        Log.d("TAG21", "buttonPause vis");
        buttonPlay.setVisibility(View.GONE);
        buttonPause.setVisibility(View.VISIBLE);
    }
    void showPlay(){
        Log.d("TAG21", "buttonPlay vis");
        buttonPlay.setVisibility(View.VISIBLE);
        buttonPause.setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStep(Events.UpdateWorkout workout) {
        Log.d("TAG21", "UpdateWorkout from WF- " + workout.getId());

        if(workout.getId()==workOut.getId()){
            stepMap = storage.getState();

            setStepInfo(stepMap.get(workout.getId()));
            adapter.update(workOut, stepMap.get(workout.getId()));
        }


    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onStep(Events.UpdateWorkoutSticky workout) {
        Log.d("TAG21", "STICKY " + workout.getId());
        Events.UpdateWorkoutSticky stickyEvent = EventBus.getDefault().getStickyEvent(Events.UpdateWorkoutSticky.class);
        if(stickyEvent != null) {
            EventBus.getDefault().removeStickyEvent(stickyEvent);
            Log.d("TAG21", "delete sticky");
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onClickExercise(Events.ClickExercise event) {
        Log.d("TAG21", "Click Ex " + event.getPosition());
        totalTime.setText("Общее время: " + Util.secondsToTime(Util.totalTime(workOut)));
        exerciseCurrentTime.setText("00:00");
        if(event.getPosition()!=-1){
            exerciseName.setText(workOut.getExcersices().get(event.getPosition()).getName());
            exerciseTotalTime.setText("/" + Util.secondsToTime(workOut.getExcersices().get(event.getPosition()).getTimeInSeconds()));
        } else {
            totalTime.setText("Общее время: 00:00");
            exerciseName.setText("");
            exerciseTotalTime.setText("/00:00");
        }

        totalTime.setText("Общее время: " + Util.secondsToTime(Util.totalTime(workOut)));

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        storage = (Storage) context;
        stepMap = storage.getState();
    }


    void updateRecycler(){
        adapter.update(workOut.getExcersices());
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @OnClick(R.id.bottomButton)
    public void onClick(View v){
        Log.d("TAG21", "Click");

        if(mode.equals(Mode.NORMAL)){
            showCreateOrChangeExerciseDialog(null);
        }
        if(mode == Mode.SETTINGS) {
            mode = Mode.NORMAL;
            adapter.setReplaceMode(false);
            setBottomButtonParams(mode);

            ViewGroup.LayoutParams layoutParams = panel.getLayoutParams();
            panel.setVisibility(View.VISIBLE);
            Log.d("TAG21", "h - " + layoutParams.height);
            panel.setTranslationY(0);
            Log.d("TAG21", "commitTransaction bottomButton");
            mRealm.commitTransaction();
            workoutSettings.setVisibility(View.VISIBLE);
        }

        if(mode == Mode.PLAYING){
            showStopTrainingDialog();
        }

    }

    private void showStopTrainingDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setMessage("Остановить тренировку?");

        dialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                EventBus.getDefault().post(new Events.StopWorkout(workOut.getId()));
                dialog.dismiss();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @OnClick(R.id.buttonPlay)
    public void onClickPlay(View v){
        if(workOut.getExcersices()!=null && workOut.getExcersices().size()!=0){
            EventBus.getDefault().post(new Events.StartWorkout(workOut.getId(), Util.getStartingTime(workOut, adapter.getStartPosition())));
            showPause();
        } else {
            Toast.makeText(getContext(), "Нечего проигрывать", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.buttonPause)
    public void onClickPause(View v){
        EventBus.getDefault().post(new Events.PauseWorkout(workOut.getId()));
        showPlay();
    }

    @OnClick(R.id.backButton)
    public void backButton(View v){
      getActivity().onBackPressed();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showCreateOrChangeExerciseDialog(final Exercise exercise) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View dialogView = inflater.inflate(R.layout.create_period_dialog_layout, null);
        dialogBuilder.setView(dialogView);

        final EditText editText = dialogView.findViewById(R.id.editText);

        final NumberPicker numberPickerMinutes = dialogView.findViewById(R.id.numberPicker);
        final NumberPicker numberPickerSeconds = dialogView.findViewById(R.id.numberPicker2);
        final CheckBox vibrationCheckBox = dialogView.findViewById(R.id.vibrationCheckbBox);

        ConstraintLayout vibrationContainer = dialogView.findViewById(R.id.vibroContainer);

        vibrationContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vibrationCheckBox.isChecked()){
                    vibrationCheckBox.setChecked(false);
                } else {
                    vibrationCheckBox.setChecked(true);
                }
            }
        });

        ConstraintLayout soundContainer = dialogView.findViewById(R.id.soundContainer1);

        soundName = dialogView.findViewById(R.id.soundName);
        if(exercise!=null){
            editText.setText(exercise.getName());
            int minutes = exercise.getTimeInSeconds()/60;
            int seconds = exercise.getTimeInSeconds()%60;
            numberPickerMinutes.setValue(minutes);
            numberPickerSeconds.setValue(seconds);
            soundPosition = exercise.getSound();
            vibrationCheckBox.setChecked(exercise.isVibration());
        } else {

            //set default values
        }

        soundName.setText(Constants.soundsTitle[soundPosition]);

        final Exercise finalExercise = exercise;
        soundContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TAG21", "click show ");
                if(finalExercise ==null){
                    showChooseSoundDialog(null);
                }
                else {
                    showChooseSoundDialog(new Events.ChooseSound(finalExercise));
                }
            }
        });

        dialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(mode == Mode.NORMAL){
                    Log.d("TAG21", "beginTransaction pos button");
                    mRealm.beginTransaction();
                    String name = editText.getText().toString();
                    int timeInSeconds = numberPickerMinutes.getValue()*60 + numberPickerSeconds.getValue();
                    Exercise e = new Exercise();
                    e.setId(App.getNextPeriodKey(mRealm));
                    e.setName(name);
                    e.setTimeInSeconds(timeInSeconds);
                    e.setSound(soundPosition);
                    e.setVibration(vibrationCheckBox.isChecked());
                    if(workOut.getExcersices()==null){
                        Log.d("TAG21", "Workout list = null. Add new one.");
                        workOut.setExcersices(new RealmList<Exercise>());
                    } else {
                        Log.d("TAG21", "Workout list not null");
                    }
                    workOut.getExcersices().add(e);
                    Log.d("TAG21", "commitTransaction pos button");
                    mRealm.commitTransaction();
                    if(workOut.getExcersices().size()==1){
                        onClickExercise(new Events.ClickExercise(0));
                    }
                    updateRecycler();
                    dialog.dismiss();
                }
                if(mode == Mode.SETTINGS){
                    String name = editText.getText().toString();
                    exercise.setName(name);
                    int timeInSeconds = numberPickerMinutes.getValue()*60 + numberPickerSeconds.getValue();
                    exercise.setTimeInSeconds(timeInSeconds);
                    exercise.setSound(soundPosition);
                    exercise.setVibration(vibrationCheckBox.isChecked());

                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
                soundPosition = 0;

            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showChooseSoundDialog(Events.ChooseSound event) {

        Log.d("TAG21", "click show sound dialog");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View dialogView = inflater.inflate(R.layout.choose_sound_dialog_layout, null);
        dialogBuilder.setView(dialogView);

        final RecyclerView recyclerView = dialogView.findViewById(R.id.soundRecyclerView);

        final SoundRecyclerAdapter adapter = new SoundRecyclerAdapter();


        if(event!=null){
            soundPosition = event.getExercise().getSound();
            soundName.setText(adapter.getSoundNameByPosition(soundPosition));
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        adapter.setSelectedPosition(soundPosition);

        dialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                soundName.setText(adapter.getSoundName());
                soundPosition = adapter.getSoundPosition();
                dialog.dismiss();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @OnClick(R.id.workoutSettings)
    public void settings(View v){
        final PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.workout_settings, popupMenu.getMenu());

        final boolean showNumber = SharedManager.getProperty("showNumber");
        Log.d("TAG21", "show - " + showNumber);

        final MenuItem showNumberMenuItem = popupMenu.getMenu().findItem(R.id.showPosition);

        showNumberMenuItem.setChecked(showNumber);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    // Handle the non group menu items here

                    case R.id.showPosition:
                        Log.d("TAG21", "click show ");

                        if(showNumber){
                            showNumberMenuItem.setChecked(false);
                            adapter.setShowNumber(false);
                            SharedManager.addProperty("showNumber", false);
                        } else {
                            showNumberMenuItem.setChecked(true);
                            adapter.setShowNumber(true);
                            SharedManager.addProperty("showNumber", true);
                        }
                        adapter.notifyDataSetChanged();

                        break;
                    case R.id.exercise_replace:

                        ViewGroup.LayoutParams layoutParams = panel.getLayoutParams();

                        recyclerView.animate().translationY(-layoutParams.height).setDuration(200).setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {
                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                recyclerView.setTranslationY(0);
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {
                            }
                        });
                        panel.animate().translationY(-layoutParams.height).setDuration(200).setListener(new Animator.AnimatorListener() {
                           @Override
                           public void onAnimationStart(Animator animator) {
                           }
                           @Override
                           public void onAnimationEnd(Animator animator) {
                                panel.setVisibility(View.GONE);
                           }

                           @Override
                           public void onAnimationCancel(Animator animator) {
                           }

                           @Override
                           public void onAnimationRepeat(Animator animator) {
                           }
                       });

                        adapter.setReplaceMode(true);
                        mode = Mode.SETTINGS;
                        setBottomButtonParams(mode);
                        Log.d("TAG21", "beginTransaction replaceMode");
                        mRealm.beginTransaction();
                        workoutSettings.setVisibility(View.GONE);

                        break;

                    case R.id.rename:
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                        dialogBuilder.setTitle("Переименовать");

                        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                        final View dialogView = inflater.inflate(R.layout.new_workout, null);

                        final EditText editText = dialogView.findViewById(R.id.workoutName);
                        editText.setText(workOut.getName());

                        dialogBuilder.setView(dialogView);

                        dialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        });
                        dialogBuilder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String name = editText.getText().toString();
                                if(!name.equals("")){
                                    Log.d("TAG21", "beginTransaction rename");
                                    mRealm.beginTransaction();
                                    workOut.setName(name);
                                    Log.d("TAG21", "commitTransaction rename");
                                    mRealm.commitTransaction();
                                    title.setText(name);
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(getContext(), "Name can not be empty", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                        AlertDialog b = dialogBuilder.create();
                        b.show();
                        break;
                    default:
                        return false;
                }

                return true;
            }
        });
        popupMenu.show();
    }

    private void setBottomButtonParams(Mode mode) {
        if(mode == Mode.PLAYING){
            bottomButtonText.setText("Отсановить тренировку");
            bottomButtonIcon.setImageResource(R.drawable.ic_delete_cross);
        }
        if(mode == Mode.NORMAL){
            bottomButtonText.setText("Добавить новое упражнение");
            bottomButtonIcon.setImageResource(R.drawable.ic_add);
        }
        if(mode == Mode.SETTINGS){
            bottomButtonText.setText("Готово");
            bottomButtonIcon.setImageResource(R.drawable.ic_check_mark);
        }
    }

    @Override
    public void onResume() {
        Log.d("TAG21", "on resume");

        Events.UpdateWorkoutSticky stickyEvent = EventBus.getDefault().getStickyEvent(Events.UpdateWorkoutSticky.class);
        if(stickyEvent != null) {

            if(stickyEvent.getId()==workOut.getId()){
                Log.d("TAG21", "update with sticky");
                stepMap = storage.getState();

                setStepInfo(stepMap.get(stickyEvent.getId()));
                adapter.update(workOut, stepMap.get(stickyEvent.getId()));
            }

            EventBus.getDefault().removeStickyEvent(stickyEvent);
            Log.d("TAG21", "delete sticky");
        } else {
            Log.d("TAG21", "sticky null");
        }

        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);


    }

    @Override
    public void onStop() {
        if(mRealm.isInTransaction()){
            mRealm.commitTransaction();
        }
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

}
