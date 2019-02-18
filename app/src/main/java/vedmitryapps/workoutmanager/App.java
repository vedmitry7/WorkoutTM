package vedmitryapps.workoutmanager;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;

import com.google.android.gms.ads.MobileAds;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import vedmitryapps.workoutmanager.models.Exercise;
import vedmitryapps.workoutmanager.models.WorkOut;


public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
        SharedManager.init(this);
    }

    public static long getNextPeriodKey(Realm mRealm) {
        try {
            Number number = mRealm.where(Exercise.class).max("id");
            if (number != null) {
                return number.longValue() + 1;
            } else {
                return 0;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    public static long getNextWorkoutId(Realm mRealm) {
        try {
            Number number = mRealm.where(WorkOut.class).max("id");
            if (number != null) {
                return number.longValue() + 1;
            } else {
                return 0;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }
}
