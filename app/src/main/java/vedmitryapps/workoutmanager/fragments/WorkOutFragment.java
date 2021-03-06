package vedmitryapps.workoutmanager.fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import vedmitryapps.workoutmanager.adapters.RecyclerViewBottomMargin;
import vedmitryapps.workoutmanager.adapters.SoundRecyclerAdapter;
import vedmitryapps.workoutmanager.models.Exercise;
import vedmitryapps.workoutmanager.models.WorkOut;

public class WorkOutFragment extends Fragment  {

    @BindView(R.id.mainRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.repeatingCount)
    TextView repeatingTextView;

    @BindView(R.id.exerciseName)
    TextView exerciseName;

    @BindView(R.id.totalProgress)
    TextView totalTime;

    @BindView(R.id.workoutPanel)
    ConstraintLayout panel;

    @BindView(R.id.mainContainer)
    ConstraintLayout mainContainer;

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

    @BindView(R.id.plusButton)
    ImageView plusButton;

    @BindView(R.id.minusButton)
    ImageView minusButton;

    @BindView(R.id.exerciseCurrentTime)
    TextView exerciseCurrentTime;

    @BindView(R.id.exerciseTotalTime)
    TextView exerciseTotalTime;

    @BindView(R.id.toolbarLayout)
    ConstraintLayout toolbar;
    @BindView(R.id.view)
    View view;

    TextView soundName;

    Realm mRealm;

    WorkOut workOut;

    ExerciseRecyclerAdapter adapter;
    ExerciseTouchHelperCallback callback;

    Mode mode = Mode.NORMAL;

    ItemTouchHelper itemTouchHelper;

    Storage storage;

    Map<Long, Events.WorkoutStep> stepMap = new HashMap();

    int soundPosition = 0;
    private int repeatingCount;
    private boolean countdownExercise;
    private boolean countdownTotal;

    LinearLayoutManager layoutManager;

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
        initColors();
        countdownExercise = !SharedManager.getProperty(Constants.KEY_COUNTDOWN_EX_DISABLED);
        countdownTotal = !SharedManager.getProperty(Constants.KEY_COUNTDOWN_TOTAL_DISABLED);

        SharedManager.init(getContext());

        mRealm = Realm.getDefaultInstance();
        workOut = mRealm.where(WorkOut.class).equalTo("id", getArguments().getLong("id")).findFirst();

        title.setText(workOut.getName());

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new ExerciseRecyclerAdapter(workOut.getExcersices(), new OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                itemTouchHelper.startDrag(viewHolder);
            }
        });

        RecyclerViewBottomMargin decoration = new RecyclerViewBottomMargin(64);
        recyclerView.addItemDecoration(decoration);

        callback = new ExerciseTouchHelperCallback(adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);

        if(stepMap.containsKey(workOut.getId())){
            Events.WorkoutStep workoutStep = stepMap.get(workOut.getId());
            setStepInfo(workoutStep);
            adapter.update(workOut, stepMap.get(workoutStep.getId()));
        } else {
            if(workOut.getExcersices().size()==0){
                onClickExercise(new Events.ClickExercise(-1));
            }
            else {
                onClickExercise(new Events.ClickExercise(0));
            }
            buttonPlay.setVisibility(View.VISIBLE);
        }

    }

    private void initColors() {
        if(!SharedManager.getProperty(Constants.KEY_BLACK_DISABLED)){
            int color = getContext().getResources().getColor(R.color.white_95);

            toolbar.setBackgroundColor(getResources().getColor(R.color.colorToolbar));
            mainContainer.setBackgroundColor(getResources().getColor(R.color.colorBackground));
            totalTime.setTextColor(color);
            exerciseCurrentTime.setTextColor(color);
            exerciseName.setTextColor(color);
            exerciseTotalTime.setTextColor(color);
            repeatingTextView.setTextColor(color);
            buttonPlay.setColorFilter(color);
            buttonPause.setColorFilter(color);
            plusButton.setColorFilter(color);
            minusButton.setColorFilter(color);
            view.setBackgroundColor(color);

        } else {
            int color = getContext().getResources().getColor(R.color.black_78);
            toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_new));
            mainContainer.setBackgroundColor(getResources().getColor(R.color.background_new));
            totalTime.setTextColor(color);
            totalTime.setTextColor(color);
            exerciseCurrentTime.setTextColor(color);
            exerciseName.setTextColor(color);
            exerciseTotalTime.setTextColor(color);
            repeatingTextView.setTextColor(color);
            buttonPlay.setColorFilter(color);
            buttonPause.setColorFilter(color);
            plusButton.setColorFilter(color);
            minusButton.setColorFilter(color);
            view.setBackgroundColor(color);
        }
    }

    private void setStepInfo(Events.WorkoutStep workoutStep) {
        if(workoutStep.isFinished() || workoutStep.isInterrupted()){
            mode = Mode.NORMAL;
            setBottomButtonParams(mode);
            callback.setDragEnabled(true);

            onClickExercise(new Events.ClickExercise(adapter.getStartPosition()));

            showPlay();
        } else {
            if(workoutStep.isPaused()){
                showPlay();
            } else {
                showPause();
            }
            mode = Mode.PLAYING;
            setBottomButtonParams(mode);
            callback.setDragEnabled(false);
            if(countdownTotal){
                totalTime.setText(Util.secondsToTime(Util.totalTime(workOut)-stepMap.get(workoutStep.getId()).getTime()) + "/" + Util.secondsToTime(Util.totalTime(workOut)));
            } else {
                totalTime.setText(Util.secondsToTime(stepMap.get(workoutStep.getId()).getTime()) + "/" + Util.secondsToTime(Util.totalTime(workOut)));
            }
            exerciseName.setText(Util.getCurrentExercise(workOut, stepMap.get(workoutStep.getId()).getTime()).getName());
            if(countdownExercise){
                int res = Util.getCurrentExercise(workOut, stepMap.get(workoutStep.getId()).getTime()).getTimeInSeconds()
                        - Util.getCurrentExerciseProgress(workOut, stepMap.get(workoutStep.getId()).getTime());
                exerciseCurrentTime.setText(Util.secondsToTime(res));
            } else {
                exerciseCurrentTime.setText("" + Util.getCurrentExerciseProgressString(workOut, stepMap.get(workoutStep.getId()).getTime()));
            }
        }
        exerciseTotalTime.setText("/" + Util.secondsToTime(Util.getCurrentExercise(workOut, stepMap.get(workoutStep.getId()).getTime()).getTimeInSeconds()));
        repeatingCount = workoutStep.getRepeating();
        repeatingTextView.setText("R" + workoutStep.getRepeating());
    }

    void showPause(){
        buttonPlay.setVisibility(View.GONE);
        buttonPause.setVisibility(View.VISIBLE);
    }
    void showPlay(){
        buttonPlay.setVisibility(View.VISIBLE);
        buttonPause.setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStep(Events.UpdateWorkout workout) {
        if(workout.getId()==workOut.getId()){
            stepMap = storage.getState();
            setStepInfo(stepMap.get(workout.getId()));
            adapter.update(workOut, stepMap.get(workout.getId()));
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onStep(Events.UpdateWorkoutSticky workout) {
        Events.UpdateWorkoutSticky stickyEvent = EventBus.getDefault().getStickyEvent(Events.UpdateWorkoutSticky.class);
        if(stickyEvent != null) {
            EventBus.getDefault().removeStickyEvent(stickyEvent);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onClickExercise(Events.ClickExercise event) {
        String commonTime = getString(R.string.total_time) + " ";
        if(event.getPosition()!=-1){
            exerciseName.setText(workOut.getExcersices().get(event.getPosition()).getName());
            exerciseTotalTime.setText("/" + Util.secondsToTime(workOut.getExcersices().get(event.getPosition()).getTimeInSeconds()));

            totalTime.setText(commonTime + Util.secondsToTime(Util.totalTime(workOut)));
            if(countdownExercise){
                exerciseCurrentTime.setText(Util.secondsToTime(workOut.getExcersices().get(event.getPosition()).getTimeInSeconds()));
            } else {
                exerciseCurrentTime.setText("00:00");
            }

        } else {
            totalTime.setText(commonTime + "00:00");
            exerciseName.setText("");
            exerciseTotalTime.setText("/00:00");
        }
        totalTime.setText(commonTime + Util.secondsToTime(Util.totalTime(workOut)));
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


    @OnClick(R.id.bottomButton)
    public void onClickBottomButton(View v){
        if(mode.equals(Mode.NORMAL)){
            showCreateOrChangeExerciseDialog(null);
        }
        if(mode == Mode.PLAYING){
            showStopTrainingDialog();
        }

    }

    private void showStopTrainingDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setMessage(R.string.q_stop_workout);

        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                EventBus.getDefault().post(new Events.StopWorkout(workOut.getId()));
                NotificationManager notificationManager =
                        (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel((int) workOut.getId());
                dialog.dismiss();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @OnClick(R.id.buttonPlay)
    public void onClickPlay(View v){
        if(workOut.getExcersices()!=null && workOut.getExcersices().size()!=0){
            EventBus.getDefault().post(new Events.StartWorkout(workOut.getId(),
                    Util.getStartingTime(workOut, adapter.getStartPosition())));
            showPause();
        } else {
            Toast.makeText(getContext(), getString(R.string.nothing_to_play), Toast.LENGTH_SHORT).show();
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

        App.showKeyboard(getContext());
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService( Context.LAYOUT_INFLATER_SERVICE );
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
            editText.setSelection(editText.getText().length());
            int minutes = exercise.getTimeInSeconds()/60;
            int seconds = exercise.getTimeInSeconds()%60;
            numberPickerMinutes.setValue(minutes);
            numberPickerSeconds.setValue(seconds);
            soundPosition = exercise.getSound();
            vibrationCheckBox.setChecked(exercise.isVibration());
            soundName.setText(Constants.soundsTitle[soundPosition]);
        } else {

            //set default values
            vibrationCheckBox.setChecked(SharedManager.getProperty(Constants.KEY_DEF_VIBRATION));
            soundName.setText(Constants.soundsTitle[SharedManager.getIntProperty(Constants.KEY_SOUND_POSITION)]);
            soundPosition = SharedManager.getIntProperty(Constants.KEY_SOUND_POSITION);
        }


        final Exercise finalExercise = exercise;
        soundContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(finalExercise ==null){
                    showChooseSoundDialog(null);
                }
                else {
                    showChooseSoundDialog(new Events.ChooseSound(finalExercise));
                }
            }
        });

        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                App.closeKeyboard(getContext());
                dialog.dismiss();
            }
        });
        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                mRealm.beginTransaction();

                if(exercise == null){
                    String name = editText.getText().toString();
                    int timeInSeconds = numberPickerMinutes.getValue()*60 + numberPickerSeconds.getValue();
                    Exercise e = new Exercise();
                    e.setId(App.getNextPeriodKey(mRealm));
                    e.setName(name);
                    e.setTimeInSeconds(timeInSeconds);
                    e.setSound(soundPosition);
                    e.setVibration(vibrationCheckBox.isChecked());
                    if(workOut.getExcersices()==null){
                        workOut.setExcersices(new RealmList<Exercise>());
                    } else {
                    }
                    workOut.getExcersices().add(e);
                    if(workOut.getExcersices().size()==1){
                        onClickExercise(new Events.ClickExercise(0));
                    }
                    updateRecycler();
                    dialog.dismiss();
                }
                if(exercise!=null){
                    String name = editText.getText().toString();
                    exercise.setName(name);
                    int timeInSeconds = numberPickerMinutes.getValue()*60 + numberPickerSeconds.getValue();
                    exercise.setTimeInSeconds(timeInSeconds);
                    exercise.setSound(soundPosition);
                    exercise.setVibration(vibrationCheckBox.isChecked());
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
                mRealm.commitTransaction();
                soundPosition = 0;
                App.closeKeyboard(getContext());
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showChooseSoundDialog(Events.ChooseSound event) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View dialogView = inflater.inflate(R.layout.choose_sound_dialog_layout, null);
        dialogBuilder.setView(dialogView);

        final RecyclerView recyclerView = dialogView.findViewById(R.id.soundRecyclerView);
        final SoundRecyclerAdapter adapter = new SoundRecyclerAdapter();

        if(event!=null){
            soundPosition = event.getExercise().getSound();
            soundName.setText(adapter.getSoundNameByPosition(soundPosition));
        } else {
            soundPosition = SharedManager.getIntProperty(Constants.KEY_SOUND_POSITION);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        adapter.setSelectedPosition(soundPosition);

        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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

        final boolean showNumber = SharedManager.getProperty(Constants.KEY_SHOW_POSITION);
        final MenuItem showNumberMenuItem = popupMenu.getMenu().findItem(R.id.showPosition);
        showNumberMenuItem.setChecked(showNumber);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.delete:
                        AlertDialog.Builder dialogBuilder2 = new AlertDialog.Builder(getContext());

                        dialogBuilder2.setMessage(getResources().getString(R.string.delete) + " " + workOut.getName() +  "?");

                        dialogBuilder2.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        });
                        dialogBuilder2.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mRealm.beginTransaction();
                                workOut.deleteFromRealm();
                                mRealm.commitTransaction();
                                getActivity().onBackPressed();
                                dialog.dismiss();
                            }
                        });
                        dialogBuilder2.create().show();
                        break;

                    case R.id.showPosition:

                        if(showNumber){
                            showNumberMenuItem.setChecked(false);
                            adapter.setShowNumber(false);
                            SharedManager.addProperty(Constants.KEY_SHOW_POSITION, false);
                        } else {
                            showNumberMenuItem.setChecked(true);
                            adapter.setShowNumber(true);
                            SharedManager.addProperty(Constants.KEY_SHOW_POSITION, true);
                        }
                        adapter.notifyDataSetChanged();
                        break;
                    case R.id.rename:
                        App.showKeyboard(getContext());
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                        dialogBuilder.setTitle(R.string.rename);

                        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                        final View dialogView = inflater.inflate(R.layout.new_workout, null);

                        final EditText editText = dialogView.findViewById(R.id.workoutName);
                        editText.setText(workOut.getName());
                        editText.setSelection(workOut.getName().length());

                        dialogBuilder.setView(dialogView);

                        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                App.closeKeyboard(getContext());
                                dialog.dismiss();
                            }
                        });
                        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String name = editText.getText().toString();
                                if(!name.equals("")){
                                    mRealm.beginTransaction();
                                    workOut.setName(name);
                                    mRealm.commitTransaction();
                                    title.setText(name);
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(getContext(), getString(R.string.name_cant_be_empty), Toast.LENGTH_SHORT).show();
                                }

                                App.closeKeyboard(getContext());
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


    @OnClick(R.id.plusButton)
    public void plus(View v){
        repeatingCount++;
        repeatingTextView.setText("R" + repeatingCount);
        EventBus.getDefault().post(new Events.Repeating(workOut.getId(), repeatingCount));
    }

    @OnClick(R.id.minusButton)
    public void minus(View v){
        if(repeatingCount==0){
            return;
        }
        repeatingCount--;
        repeatingTextView.setText("R" + repeatingCount);
        EventBus.getDefault().post(new Events.Repeating(workOut.getId(), repeatingCount));
    }

    @OnClick(R.id.exerciseCurrentTime)
    public void exerciseCurrentTimeClick(View v){

        if(countdownExercise){
            countdownExercise = false;
            SharedManager.addProperty(Constants.KEY_COUNTDOWN_EX_DISABLED, true);
        }
        else {
            countdownExercise = true;
            SharedManager.addProperty(Constants.KEY_COUNTDOWN_EX_DISABLED, false);
        }

        if(stepMap.get(workOut.getId())!=null){
            setStepInfo(stepMap.get(workOut.getId()));
        } else {
            if(workOut.getExcersices()!=null && workOut.getExcersices().size()!=0){
                onClickExercise(new Events.ClickExercise(adapter.getSelectedItemPos()));
            }
        }
    }

    @OnClick(R.id.totalProgress)
    public void totalTimeClick(View v){

        if(countdownTotal){
            countdownTotal = false;
            SharedManager.addProperty(Constants.KEY_COUNTDOWN_TOTAL_DISABLED, true);
        }
        else{
            countdownTotal = true;
            SharedManager.addProperty(Constants.KEY_COUNTDOWN_TOTAL_DISABLED, false);
        }

        if(stepMap.get(workOut.getId())!=null){
            setStepInfo(stepMap.get(workOut.getId()));
        } else {
            if(workOut.getExcersices()!=null && workOut.getExcersices().size()!=0){
                onClickExercise(new Events.ClickExercise(adapter.getSelectedItemPos()));
            }
        }
    }

    private void setBottomButtonParams(Mode mode) {
        if(mode == Mode.PLAYING){
            bottomButtonText.setText(getString(R.string.stop_workout));
            bottomButtonIcon.setImageResource(R.drawable.ic_stop);
        }
        if(mode == Mode.NORMAL){
            bottomButtonText.setText(getString(R.string.add_new_exercise));
            bottomButtonIcon.setImageResource(R.drawable.ic_add);
        }
    }

    @Override
    public void onResume() {
        Events.UpdateWorkoutSticky stickyEvent = EventBus.getDefault().getStickyEvent(Events.UpdateWorkoutSticky.class);
        if(stickyEvent != null) {

            if(stickyEvent.getId()==workOut.getId()){
                stepMap = storage.getState();
                setStepInfo(stepMap.get(stickyEvent.getId()));
                adapter.update(workOut, stepMap.get(stickyEvent.getId()));
            }

            EventBus.getDefault().removeStickyEvent(stickyEvent);
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
