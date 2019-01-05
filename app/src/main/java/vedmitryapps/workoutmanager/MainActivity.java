package vedmitryapps.workoutmanager;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import vedmitryapps.workoutmanager.fragments.MainFragment;
import vedmitryapps.workoutmanager.fragments.WorkOutFragment;

public class MainActivity extends AppCompatActivity implements Storage{

    Map<Long, Events.WorkoutStep> stepMap = new HashMap();

    MainFragment mainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, MyService.class);
        startService(intent);

        mainFragment = new MainFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, mainFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openWorkout(Events.OpenWorkout event){
        WorkOutFragment workoutFragment = WorkOutFragment.createInstance(event.getId());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, workoutFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStart(Events.WorkoutStep workout) {
        Log.d("TAG21", "MA: Step - " + workout.getId() + " step - " + workout.getTime());
        stepMap.put(workout.getId(), workout);

        for (Map.Entry item : stepMap.entrySet())
        {
            Log.d("TAG21", " --------  workout " + item.getKey() + " step - " + (item.getValue()));
        }
        EventBus.getDefault().post(new Events.UpdateWorkout(workout.getId()));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onStart(Map<Long, Events.WorkoutStep> finishedStepMap) {
        Log.d("TAG21", " -------- STICKY! Finished workout ");

        for (Map.Entry item : finishedStepMap.entrySet())
        {
            stepMap.put((Long) item.getKey(), (Events.WorkoutStep) item.getValue());
            Log.d("TAG21", " -------- Finished workout " + item.getKey() + " step - " + (item.getValue()));
            EventBus.getDefault().post(new Events.UpdateWorkout((Long) item.getKey()));
            EventBus.getDefault().post(new Events.DeleteFromFinished((Long) item.getKey()));
        }
    }

    

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        if(mainFragment.isVisible()){
            moveTaskToBack(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public Map getState() {
        return stepMap;
    }
}
