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
            return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnTHJ0B+iTzKoHHlVc5TVkcF2PPrf9SBFocA4tI9MgDj0ODPH7PsgNYR745wc1Y8nYzBB7eBdG0BkA0dkHfg2ENq5S3VIwUoZiI3KZyw5H4DwDYylEdY1LpFgZsu57ICuGyxp5WPB5Y7WM8yBo2zV4Dl4DtW8XuwFZjlbC2c8v+f1jghv9Bq5OspWOCHDJawzMuGyc+etCv5RC6psoIfdpbUVDTZxXPfF07KHN75Wf26YaMuwwmQX04E6zxVruGSJ/S15lQo76KEWIzDOh+9ucpik/260dWuk+HVJW7GdqNS1+VNqs9eX/a9Vbmgpb+kGd+hAU8JhHR6FbMfDGDOMQQIDAQAB";
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
