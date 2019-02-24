package vedmitryapps.workoutmanager;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        long id = intent.getLongExtra("id", -1);

        if (intent.getAction().equalsIgnoreCase("pause")) {
            EventBus.getDefault().post(new Events.PauseWorkout(id));
        }
        if (intent.getAction().equalsIgnoreCase("continue")) {
            EventBus.getDefault().post(new Events.StartWorkout(id, 0));

        }
    }
}