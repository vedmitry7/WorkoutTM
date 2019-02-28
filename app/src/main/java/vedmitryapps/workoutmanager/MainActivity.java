package vedmitryapps.workoutmanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import vedmitryapps.workoutmanager.fragments.MainFragment;
import vedmitryapps.workoutmanager.fragments.SettingsFragment;
import vedmitryapps.workoutmanager.fragments.WorkOutFragment;

public class MainActivity extends AppCompatActivity implements Storage{

    @BindView(R.id.adView)
    AdView mAdView;
    Map<Long, Events.WorkoutStep> stepMap = new HashMap();
    MainFragment mainFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setStatusBar(new Events.SetStatusBar());

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Intent intent = new Intent(this, MyService.class);
        startService(intent);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mainFragment = new MainFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragmentContainer, mainFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        if(getIntent()!=null && getIntent().getLongExtra("id", -1) != -1){
            openWorkout(new Events.OpenWorkout(getIntent().getLongExtra("id", -1)));
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setStatusBar(Events.SetStatusBar event) {

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if(SharedManager.getProperty(Constants.KEY_BLACK_ENABLED)){
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorToolbar));
        } else {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.toolbar_new));
        }
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
        for (Map.Entry item : finishedStepMap.entrySet())
        {
            stepMap.put((Long) item.getKey(), (Events.WorkoutStep) item.getValue());
            Log.d("TAG21", " -------- Finished workout " + item.getKey() + " step - " + (item.getValue()));
            EventBus.getDefault().post(new Events.UpdateWorkout((Long) item.getKey()));
            if(((Events.WorkoutStep) item.getValue()).isFinished()){
                EventBus.getDefault().post(new Events.DeleteFromFinished((Long) item.getKey()));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStart(Events.OpenSettings event) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment settingsFragment = new SettingsFragment();
        transaction.replace(R.id.fragmentContainer, settingsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdView != null)
            mAdView.pause();
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null)
            mAdView.resume();

        if(EventBus.getDefault().getStickyEvent(Map.class)!=null){
            onStart(EventBus.getDefault().getStickyEvent(Map.class));
        }
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
    protected void onDestroy() {
        super.onDestroy();
        if (mAdView != null)
            mAdView.destroy();
    }

    @Override
    public Map getState() {
        return stepMap;
    }
}
