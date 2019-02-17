package vedmitryapps.workoutmanager.fragments;

import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import vedmitryapps.workoutmanager.Events;
import vedmitryapps.workoutmanager.MainActivity;
import vedmitryapps.workoutmanager.Mode;
import vedmitryapps.workoutmanager.SettingsActivity;
import vedmitryapps.workoutmanager.Storage;
import vedmitryapps.workoutmanager.adapters.ExerciseTouchHelperCallback;
import vedmitryapps.workoutmanager.adapters.OnStartDragListener;
import vedmitryapps.workoutmanager.adapters.WorkoutRecyclerAdapter;
import vedmitryapps.workoutmanager.R;
import vedmitryapps.workoutmanager.adapters.WorkoutTouchHelperCallback;
import vedmitryapps.workoutmanager.models.GodObject;
import vedmitryapps.workoutmanager.models.WorkOut;

public class MainFragment extends Fragment {

    @BindView(R.id.mainRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.bottomButtonIcon)
    ImageView bottomButtonIcon;

    @BindView(R.id.bottomButtonText)
    TextView bottomButtonText;

    @BindView(R.id.settings)
    ImageView settings;

    Realm mRealm;

    RealmList<WorkOut> workOuts;
    WorkoutRecyclerAdapter adapter;

    Map<Long, Events.WorkoutStep> stepMap = new HashMap();

    Storage storage;
    GodObject godObject;

    ItemTouchHelper itemTouchHelper;
    private Mode mode = Mode.NORMAL;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);

        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("TAG21", "MainFragment onViewCreated ");

        mRealm = Realm.getDefaultInstance();
        WorkOut workOut;

   /*     mRealm.beginTransaction();
        for (int i = 0; i < 10; i++) {
            workOut = mRealm.createObject(WorkOut.class, App.getNextWorkoutId(mRealm));
            workOut.setName("Workout " + (i+1));
        }
        mRealm.commitTransaction();*/

        godObject = mRealm.where(GodObject.class).findFirst();
        if(godObject==null){
            Log.d("TAG21", "God object = null");
            mRealm.beginTransaction();
            godObject = mRealm.createObject(GodObject.class, 0);
            mRealm.commitTransaction();
        } else {
            Log.d("TAG21", "God object = exist");
        }

        workOuts = godObject.getWorkouts();
        //workOuts = mRealm.where(WorkOut.class).findAll();

       /* mRealm.beginTransaction();
        for (WorkOut w:workOuts
             ) {
            w.deleteFromRealm();
            //  Log.d("TAG21", "WorkOut - " + w.getName());
        }
        mRealm.commitTransaction();*/

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        adapter = new WorkoutRecyclerAdapter(workOuts, stepMap, new OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                itemTouchHelper.startDrag(viewHolder);
            }
        });

        ItemTouchHelper.Callback callback = new WorkoutTouchHelperCallback(adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);
    }

    @OnClick(R.id.bottomButton)
    public void onClick23(final View v){


        if(mode == Mode.NORMAL){
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            dialogBuilder.setTitle("Новая тренировка");
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            final View dialogView = inflater.inflate(R.layout.new_workout, null);

            final EditText editText = dialogView.findViewById(R.id.workoutName);
            final TextInputLayout container =  dialogView.findViewById(R.id.containerEditText);



            dialogBuilder.setView(dialogView);

            dialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });

            dialogBuilder.setPositiveButton("Ok", null);

            final AlertDialog b = dialogBuilder.create();
            b.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialogInterface) {


                    Button button = ((AlertDialog) b).getButton(AlertDialog.BUTTON_POSITIVE);

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String name = editText.getText().toString();

                            if(name.length()<3){
                                container.setError("Не менее трех символов");
                            } else {
                                mRealm.beginTransaction();
                                WorkOut workOut = mRealm.createObject(WorkOut.class, App.getNextWorkoutId(mRealm));
                                workOut.setName(name);
                                godObject.getWorkouts().add(workOut);
                                mRealm.commitTransaction();
                                adapter.notifyDataSetChanged();
                                Log.d("TAG21", " w s" + workOuts.size());
                                b.dismiss();
                            }
                        }
                    });

                }
            });
            b.show();
        }
        if(mode == Mode.SETTINGS){
            mode = Mode.NORMAL;
            adapter.setReplaceMode(false);
            setBottomButtonParams(mode);
            mRealm.commitTransaction();
            settings.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.settings)
    public void settings(View v){
        final PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.main_settings, popupMenu.getMenu());


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    // Handle the non group menu items here

                    case R.id.workout_replace:
                        adapter.setReplaceMode(true);
                        mode = Mode.SETTINGS;
                        setBottomButtonParams(mode);
                        mRealm.beginTransaction();
                        settings.setVisibility(View.GONE);

                        break;

                    case R.id.generalSettings:
                        Intent intent = new Intent(getContext(), SettingsActivity.class);
                        startActivity(intent);
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
        if(mode == Mode.NORMAL){
            bottomButtonText.setText("Добавить новую тренировку");
            bottomButtonIcon.setImageResource(R.drawable.ic_add);
        }
        if(mode == Mode.SETTINGS){
            bottomButtonText.setText("Готово");
            bottomButtonIcon.setImageResource(R.drawable.ic_check_mark);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateWorkout(Events.UpdateWorkout workout) {
        Log.d("TAG21", "UpdateWorkout - " + workout.getId());

        stepMap = storage.getState();

        adapter.update(stepMap);
        /*int position = 0;
        for (int i = 0; i < workOuts.size(); i++) {
            if(workOuts.get(i).getId()==workout.getId()){
                position = i;
            }
        }
        adapter.updateItem2(1);*/
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void deleteWorkout(Events.DeleteWorkout event) {
        Log.d("TAG21", "Event - " + event.getPosition());
        workOuts.remove(event.getPosition());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        storage = (Storage) context;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

}
