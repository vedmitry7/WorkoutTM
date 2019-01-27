package vedmitryapps.workoutmanager;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.IOException;

import io.realm.RealmList;
import vedmitryapps.workoutmanager.models.Exercise;
import vedmitryapps.workoutmanager.models.WorkOut;


public class Util {

    public static int timeToSeconds(int min, int sec){
        return min * 60 + sec;
    }

    public static String secondsToTime(int time){
        int min = time/60;
        int sec = time%60;

        String seconds = sec == 0 ? "00": sec < 10 ? "0" + sec : "" + sec;
        return min + ":" + seconds;
    }


    public static int totalTime(WorkOut workOut){

        RealmList<Exercise> periods = workOut.getExcersices();
        int totalTime = 0;
        for (Exercise period:periods){
            totalTime += period.getTimeInSeconds();
        }
        return totalTime;
    }

    public static int getStartingTime(WorkOut workOut, int position){
        Log.d("TAG21", "getStartingTime - " + position);

        RealmList<Exercise> periods = workOut.getExcersices();

        int excludeTime = 0;

        for (int i = 0; i < position; i++) {
            excludeTime+=periods.get(i).getTimeInSeconds();
            Log.d("TAG21", "getStartingTime step - " + excludeTime );
        }

        Log.d("TAG21", "getStartingTime - result - " + excludeTime);

        return excludeTime;
    }


    public static Exercise getCurrentExercise(WorkOut workOut, int progress){
        Exercise exercise = null;

        int exTime;
        int sum = 0;
        for (int i = 0; i < workOut.getExcersices().size(); i++) {
            exTime = workOut.getExcersices().get(i).getTimeInSeconds();
            sum += exTime;
            if(sum > progress){
                exercise = workOut.getExcersices().get(i);
                break;
            }
        }

        if(exercise==null){
            //return last exercise if progress more then total workout time
            //func just for last second of workout progress
            exercise = workOut.getExcersices().get(workOut.getExcersices().size()-1);
        }
        Log.d("TAG21", "Util - " + exercise.getName());
        return exercise;
    }

    public static String getCurrentExerciseProgressString(WorkOut workOut, int progress){
        Exercise exercise = null;

        int exTime;
        int sum = 0;

        for (int i = 0; i < workOut.getExcersices().size(); i++) {
            exTime = workOut.getExcersices().get(i).getTimeInSeconds();
            sum += exTime;
            Log.d("TAG21", "progress - " + progress + " exTime - " + exTime + " sum - " + sum);
            if(sum > progress){
                Log.d("TAG21", "sum > progress");
                exercise = workOut.getExcersices().get(i);
                 sum=sum-exTime;
                Log.d("TAG21", "return " + sum);
                break;
            }
        }

        if(exercise==null){
            //return last exercise if progress more then total workout time
            //func just for last second of workout progress
            exercise = workOut.getExcersices().get(workOut.getExcersices().size()-1);
            sum=sum-exercise.getTimeInSeconds();
        }

        String s = secondsToTime(progress-sum);
        Log.d("TAG22", "pr - sum " + (progress-sum) + " Ttt  - " + s);
        return s;
    }

    public static int getCurrentExerciseProgress(WorkOut workOut, int progress){
        Exercise exercise = null;

        int exTime;
        int sum = 0;

        for (int i = 0; i < workOut.getExcersices().size(); i++) {
            exTime = workOut.getExcersices().get(i).getTimeInSeconds();
            sum += exTime;
            Log.d("TAG21", "progress - " + progress + " exTime - " + exTime + " sum - " + sum);
            if(sum > progress){
                Log.d("TAG21", "sum > progress");
                exercise = workOut.getExcersices().get(i);
                sum=sum-exTime;
                Log.d("TAG21", "return " + sum);
                break;
            }
        }

        if(exercise==null){
            //return last exercise if progress more then total workout time
            //func just for last second of workout progress
            exercise = workOut.getExcersices().get(workOut.getExcersices().size()-1);
            sum=sum-exercise.getTimeInSeconds();
        }

        Log.d("TAG21", "Util - " + exercise.getName());
        return progress-sum;
    }

    public static boolean isLastSecond(final Context context, WorkOut workOut, int progress){


       RealmList<Exercise> exercises = workOut.getExcersices();


        int[] mas = new int[exercises.size()];

        int sum = 0;
        for (int i = 0; i < exercises.size(); i++) {
            sum = sum + exercises.get(i).getTimeInSeconds();
            mas[i] = sum;
        }

        for (int i = 0; i < mas.length; i++) {
            Log.d("TAG24", "mas " + mas[i] + "progress - " + progress);
            if(mas[i] == progress){
                if(exercises.get(i).getSound()!=0){
                    Log.d("TAG24", "SOUND! " + exercises.get(i).getSound());
                    MediaPlayer mp;
                    mp = new MediaPlayer();
                    AssetFileDescriptor afd = null;
                    try {
                        afd = context.getAssets().openFd(Constants.soundsName[exercises.get(i).getSound()]);
                        mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        //mp.setVolume(1f, 1f);
                        mp.prepare();
                        mp.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(exercises.get(i).isVibration()){
                    Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, 200};
                    v.vibrate(pattern, -1);
                }
            }
        }



        /*final Exercise exercise1 = getCurrentExercise(workOut, progress);

        Exercise exercise = null;
        int exTime;
        int sum = 0;

        for (int i = 0; i < workOut.getExcersices().size(); i++) {
            exTime = workOut.getExcersices().get(i).getTimeInSeconds();
            sum += exTime;
            Log.d("TAG21", "progress - " + progress + " exTime - " + exTime + " sum - " + sum);
            if(sum > progress){
                Log.d("TAG21", "sum > progress");
                exercise = workOut.getExcersices().get(i);
                sum=sum-exTime;
                Log.d("TAG21", "return " + sum);
                break;
            }
        }

        if(exercise==null){
            //return last exercise if progress more then total workout time
            //func just for last second of workout progress
            exercise = workOut.getExcersices().get(workOut.getExcersices().size()-1);
            sum=sum-exercise.getTimeInSeconds();
        }

        Log.d("TAG22", "Util - " + exercise.getName() + " progress - " + progress);
       // return secondsToTime(progress-sum);
        Log.d("TAG22", "pr - sum : " + (progress-sum));
        Log.d("TAG22", "ex time : " + exercise1.getTimeInSeconds());


        final int sound = exercise1.getSound();

   */



   /*     if(progress-sum == exercise1.getTimeInSeconds()-1){
            if(exercise1.getSound()!=0){
                        MediaPlayer mp;
                        mp = new MediaPlayer();
                        AssetFileDescriptor afd = null;
                        try {
                            afd = context.getAssets().openFd(soundsName[sound]);
                            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            //mp.setVolume(1f, 1f);
                            mp.prepare();
                            mp.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            }*/
        return false;
    }



    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
