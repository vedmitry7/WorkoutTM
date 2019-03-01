package vedmitryapps.workoutmanager;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.ads.MobileAds;

import org.solovyev.android.checkout.Billing;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import vedmitryapps.workoutmanager.models.Exercise;
import vedmitryapps.workoutmanager.models.WorkOut;


public class App extends Application {


    private static App sInstance;

    public static App getsInstance() {
        return sInstance;
    }

    private final Billing mBilling = new Billing(this, new Billing.DefaultConfiguration() {
        @Override
        public String getPublicKey() {
            return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjEP5CDQkeJ9/s6GomdX5dPj+SbosGBY1MWMixetBM6ETDbHDu3xR6hWm7ArYO+czgJwyJYVt5OPn0cFiO77QD19Z+uXyRwHRRtHwTR1IhNvhWvLPCRfwnGp4q+rfwG9R3IOetz4/dIkh7fWVxK+1/sJA/1L9Iowlai54BRbi5M12YBFE34Zy5C8n5V48K2/1Z6X2VdIxblYb20jCEceYZq7wiO7WblJaIRXftK3p9+wDNDEXVVshMLmaXG6IqHa6rMEgD6+4w4gB5XTFIH72/IOVNFzIA7gAoRh93xhJcQLQKZcValw3it9xdhozUSk6VgBImeNcgJKCdkMFo2XGdQIDAQAB";
        }
    });

    public Billing getBilling() {
        return mBilling;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
        SharedManager.init(this);


    }

    public static boolean isAppForground(Context mContext) {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(mContext.getPackageName())) {
                return false;
            }
        }
        return true;
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void closeKeyboard(Context context){
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }


    public static void showKeyboard(Context context){
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
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
