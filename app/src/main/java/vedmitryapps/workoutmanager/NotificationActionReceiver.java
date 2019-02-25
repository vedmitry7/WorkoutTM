package vedmitryapps.workoutmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
         Log.d("TAG25", " receive " );

        long id = intent.getLongExtra("id", -1);

        if (intent.getAction().equalsIgnoreCase("pause")) {
            Log.d("TAG25", " pause " );
            EventBus.getDefault().post(new Events.PauseWorkout(id));
        }
        if (intent.getAction().equalsIgnoreCase("continue")) {
            Log.d("TAG25", " play " );
            EventBus.getDefault().post(new Events.StartWorkout(id, 0));

        }
    }
}