package vedmitryapps.workoutmanager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmList;
import vedmitryapps.workoutmanager.models.Exercise;
import vedmitryapps.workoutmanager.models.WorkOut;

public class MyService extends Service {


    Timer timer;
    TimerTask tTask;
    long interval = 1000;

    Realm mRealm = Realm.getDefaultInstance();

    Map workouts = new HashMap<Long, TimerTask>();
    Map progress = new HashMap<Long, Integer>();
    Map repeat = new HashMap<Long, Integer>();

    Context context;
    Map<Long, Events.WorkoutStep> finishedStepMap = new HashMap();


    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        timer = new Timer();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStart(Events.StartWorkout workout) {
        Log.d("TAG23", "Start - " + workout.getId() + " starting time - " + workout.getStartTime());
        if(progress.get(workout.getId())==null){
            Log.d("TAG23", "Start - pr was null. set start time");
            progress.put(workout.getId(), workout.getStartTime());
            startWorkout(workout.getId(), true);
        } else {
            Log.d("TAG23", "Start - pr was not null");
            startWorkout(workout.getId(), false);
        }


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void repeating(Events.Repeating workout) {
        repeat.put(workout.getId(), workout.repeatingCount);
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPause(Events.PauseWorkout workout) {
        Log.d("TAG21", "Pause - " + workout.getId());
        ((TimerTask)workouts.get(workout.getId())).cancel();
        int curProg = (int) progress.get(workout.getId());
        progress.put(workout.getId(), curProg-1);
        workouts.remove(workout.getId());
        Events.WorkoutStep step = new Events.WorkoutStep(workout.getId(), (int)progress.get(workout.getId()));
        step.setPaused(true);
        EventBus.getDefault().post(step);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPause(Events.DeleteFromFinished workout) {
        finishedStepMap.remove(workout.workoutId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStop(Events.StopWorkout workout) {
        Log.d("TAG21", "Stop workout");

        if(workouts.containsKey(workout.getId())) {
            ((TimerTask)workouts.get(workout.getId())).cancel();
        }

        workouts.remove(workout.getId());
        progress.remove(workout.getId());

        Events.WorkoutStep step = new Events.WorkoutStep(workout.getId(), 0);
        step.setItterapted(true);
        EventBus.getDefault().post(step);
    }

    public void onResume(Events.ResumeWorkout workout) {
    }


    void startWorkout(final long id, boolean missStartSound){
        Log.d("TAG21", "Start workout");

        final WorkOut workOut = mRealm.where(WorkOut.class).equalTo("id", id).findFirst();

        final boolean[] mas = new boolean[1];
        mas[0] = missStartSound;

        final int totalTime = Util.totalTime(workOut);
        final int currentProgress;
        if(progress.get(id)!=null){
            Log.d("TAG21", "progress not null");
            currentProgress = (int)progress.get(id);
        } else {
            Log.d("TAG21", "progress null");
            currentProgress = 0;
        }

        workouts.put(id, new TimerTask() {
            @Override
            public void run() {
                Log.d("TAG22", "Step - " + currentProgress + " workout - " + id);
                int currentProgress;

                //if progress for this workout empty
                if (progress.get(id) != null) {
                    currentProgress = (int) progress.get(id);
                } else {
                    currentProgress = 0;
                }

                int repeatCount = 0;
                if (repeat.containsKey(id)) {
                    repeatCount = (int) repeat.get(id);
                }

                Log.d("TAG23", "Step - " + currentProgress + " workout - " + id);
                Log.d("TAG21", "cur - " + currentProgress);
                Events.WorkoutStep step = new Events.WorkoutStep(id, currentProgress);
                step.setRepeating(repeatCount);
                if (currentProgress == totalTime) {
                    if(repeatCount == 0){
                        step.setFinished(true);
                        finishedStepMap.put(step.getId(), step);
                        EventBus.getDefault().postSticky(finishedStepMap);
                    }
                    //to show if app was foreground before
                }

                EventBus.getDefault().post(step);
                Realm mRealm = Realm.getDefaultInstance();
                WorkOut workOut1 = mRealm.where(WorkOut.class).equalTo("id", id).findFirst();


                for (int i = 0; i < workOut1.getExcersices().size(); i++) {
                    Log.d("TAG23", workOut1.getExcersices().get(i).getName());
                }

                if (!mas[0]) {
                    Log.d("23", " Not miss");
                    if (Util.isLastSecond(context, workOut1, currentProgress))
                        Log.d("23", " That's it!");
                } else {
                    Log.d("23", " Miss first");
                    mas[0] = false;
                }

                progress.put(id, ++currentProgress);



                    if ((int) progress.get(id) > totalTime) {

                        if (repeatCount > 0) {
                            Log.d("TAG23", " repeatCount = " + repeatCount);
                            ((TimerTask) workouts.get(id)).cancel();
                            progress.put(id, null);
                            repeat.put(id, --repeatCount);
                            ((TimerTask) workouts.get(id)).cancel();
                            EventBus.getDefault().post(new Events.StartWorkout(id, 0));
                        } else {
                            Log.d("TAG23", " cancel task current Progress");
                            ((TimerTask) workouts.get(id)).cancel();
                            workouts.remove(id);
                            progress.remove(id);
                        }
                    }
                    mRealm.close();
                }

        });

        timer.schedule((TimerTask) workouts.get(id), 0, interval);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
