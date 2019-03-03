package vedmitryapps.workoutmanager;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.EmptyRequestListener;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import vedmitryapps.workoutmanager.fragments.MainFragment;
import vedmitryapps.workoutmanager.fragments.SettingsFragment;
import vedmitryapps.workoutmanager.fragments.WorkOutFragment;

public class MainActivity extends AppCompatActivity implements Storage{

    private final ActivityCheckout mCheckout = Checkout.forActivity(this, App.getsInstance().getBilling());
    private Inventory mInventory;

    private class PurchaseListener extends EmptyRequestListener<Purchase> {
        @Override
        public void onSuccess(Purchase purchase) {
            Toast.makeText(getApplicationContext(), "success - " + purchase.sku + " " + purchase.payload, Toast.LENGTH_SHORT).show();
            Log.d("TAG21", "Success - " + purchase.data);
            Log.d("TAG21", "Success - " + purchase.payload);
            Log.d("TAG21", "Success - " + purchase.packageName);
            Log.d("TAG21", "Success - " + purchase.sku);
            // here you can process the loaded purchase
        }

        @Override
        public void onError(int response, Exception e) {
            // handle errors here
            Log.d("TAG21", "Response - " + response);
            Log.d("TAG21", "Exc - " + e.getLocalizedMessage());
            Toast.makeText(getApplicationContext(), "Exc - " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private class InventoryCallback implements Inventory.Callback {
        @Override
        public void onLoaded(Inventory.Products products) {
            final Inventory.Product product = products.get(ProductTypes.IN_APP);
            Log.d("TAG21", "Product - " + product.id);
            Log.d("TAG21", "Product - " + String.valueOf(product.supported));
            Log.d("TAG21", "Product price - " + product.isPurchased("workout_remove_ads"));
            Toast.makeText(getApplicationContext(), "success - " +  String.valueOf(product.isPurchased("workout_remove_ads")), Toast.LENGTH_SHORT).show();

            if(product.isPurchased("workout_remove_ads")){
                SharedManager.addProperty(Constants.KEY_ADS_DISABLED, true);
                if(mAdView!=null){
                    mAdView.destroy();
                    mAdView.setVisibility(View.GONE);
                }
            }
        }
    }

    @BindView(R.id.adView)
    AdView mAdView;
    Map<Long, Events.WorkoutStep> stepMap = new HashMap();
    MainFragment mainFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if(!SharedManager.getProperty(Constants.KEY_ADS_DISABLED)){
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        mCheckout.start();
        mCheckout.createPurchaseFlow(new PurchaseListener());

        mInventory = mCheckout.makeInventory();
        mInventory.load(Inventory.Request.create()
                .loadAllPurchases()
                .loadSkus(ProductTypes.IN_APP, "workout_remove_ads"), new InventoryCallback());

        setStatusBar(new Events.SetStatusBar());

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Intent intent = new Intent(this, MyService.class);
        startService(intent);

        mainFragment = new MainFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragmentContainer, mainFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        if(getIntent()!=null && getIntent().getLongExtra("id", -1) != -1){
            openWorkout(new Events.OpenWorkout(getIntent().getLongExtra("id", -1)));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCheckout.onActivityResult(requestCode, resultCode, data);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onClick(Events.RemoveAds event) {
        mCheckout.whenReady(new Checkout.EmptyListener() {
            @Override
            public void onReady(BillingRequests requests) {
                requests.purchase(ProductTypes.IN_APP, "workout_remove_ads", null, mCheckout.getPurchaseFlow());
            }
        });
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
        mCheckout.stop();
        if (mAdView != null)
            mAdView.destroy();
    }

    @Override
    public Map getState() {
        return stepMap;
    }
}
