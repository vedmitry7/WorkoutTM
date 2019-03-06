package vedmitryapps.workoutmanager.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;


import com.google.android.gms.ads.AdRequest;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vedmitryapps.workoutmanager.Constants;
import vedmitryapps.workoutmanager.Events;
import vedmitryapps.workoutmanager.R;
import vedmitryapps.workoutmanager.SharedManager;
import vedmitryapps.workoutmanager.adapters.SoundRecyclerAdapter;


public class SettingsFragment extends Fragment {

    @BindView(R.id.switchVibration) Switch switchVibration;
    @BindView(R.id.switchNotification) Switch switchNotification;
    @BindView(R.id.switchNotificationTime) Switch switchNotificationTime;
    @BindView(R.id.switchBlack) Switch switchBlack;
    @BindView(R.id.defaultSoundContainer) ConstraintLayout sound;
    @BindView(R.id.toolbarLayout) ConstraintLayout toolbar;
    @BindView(R.id.mainContainer) ConstraintLayout mainContainer;
    @BindView(R.id.removeIds) ConstraintLayout removeIds;
    @BindView(R.id.soundName) TextView soundName;
    @BindView(R.id.textView) TextView textView;
    @BindView(R.id.textView2) TextView textView2;
    @BindView(R.id.textView3) TextView textView3;
    @BindView(R.id.textView4) TextView textView4;
    @BindView(R.id.textView5) TextView textView5;
    @BindView(R.id.textAdFree) TextView textAdFree;
    @BindView(R.id.rateText) TextView rateText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, v);
        soundName.setText(Constants.soundsTitle[SharedManager.getIntProperty(Constants.KEY_SOUND_POSITION)]);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        initColors();
        switchVibration.setChecked(SharedManager.getProperty(Constants.KEY_DEF_VIBRATION));
        switchVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedManager.addProperty(Constants.KEY_DEF_VIBRATION, isChecked);
            }
        });

        switchNotification.setChecked(!SharedManager.getProperty(Constants.KEY_NOTIFICATION_DISABLED));
        switchNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedManager.addProperty(Constants.KEY_NOTIFICATION_DISABLED, !isChecked);
            }
        });
        switchNotificationTime.setChecked(!SharedManager.getProperty(Constants.KEY_NOTIFICATION_TIME_DISABLED));
        switchNotificationTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedManager.addProperty(Constants.KEY_NOTIFICATION_TIME_DISABLED, !isChecked);
            }
        });

        switchBlack.setChecked(!SharedManager.getProperty(Constants.KEY_BLACK_DISABLED));
        switchBlack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedManager.addProperty(Constants.KEY_BLACK_DISABLED, !isChecked);
                initColors();
            }
        });

        if(SharedManager.getProperty(Constants.KEY_ADS_DISABLED)){
            removeIds.setVisibility(View.GONE);
        }
    }

    private void initColors() {
        EventBus.getDefault().post(new Events.SetStatusBar());
        if(!SharedManager.getProperty(Constants.KEY_BLACK_DISABLED)){
            int color = getContext().getResources().getColor(R.color.white_95);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorToolbar));
            mainContainer.setBackgroundColor(getResources().getColor(R.color.colorBackground));
            textView.setTextColor(color);
            textView2.setTextColor(color);
            textView3.setTextColor(color);
            textView4.setTextColor(color);
            textView5.setTextColor(color);
            soundName.setTextColor(color);
            textAdFree.setTextColor(color);
            rateText.setTextColor(color);
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_new));
            mainContainer.setBackgroundColor(getResources().getColor(R.color.background_new));
            textView.setTextColor(Color.BLACK);
            textView2.setTextColor(Color.BLACK);
            textView3.setTextColor(Color.BLACK);
            textView4.setTextColor(Color.BLACK);
            textView5.setTextColor(Color.BLACK);
            soundName.setTextColor(Color.BLACK);
            textAdFree.setTextColor(Color.BLACK);
            rateText.setTextColor(Color.BLACK);
        }
    }

    @OnClick(R.id.defaultSoundContainer)
    public void dsf(View v){

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            final View dialogView = inflater.inflate(R.layout.choose_sound_dialog_layout, null);
            dialogBuilder.setView(dialogView);

            final RecyclerView recyclerView = dialogView.findViewById(R.id.soundRecyclerView);

            final SoundRecyclerAdapter adapter = new SoundRecyclerAdapter();

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);

            adapter.setSelectedPosition(SharedManager.getIntProperty(Constants.KEY_SOUND_POSITION));

            dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });
            dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    soundName.setText(adapter.getSoundName());
                    SharedManager.addIntProperty(Constants.KEY_SOUND_POSITION, adapter.getSoundPosition());
                    dialog.dismiss();
                }
            });
            AlertDialog b = dialogBuilder.create();
            b.show();

    }
    @OnClick(R.id.backButton)
    public void backButton(View v){
        getActivity().onBackPressed();
    }

    @OnClick(R.id.removeIds)
    public void removeIds(View v){
        EventBus.getDefault().post(new Events.RemoveAds());
    }

    @OnClick(R.id.rate)
    public void rate(View v){
        Uri uri = Uri.parse("market://details?id=" + getContext().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getContext().getPackageName())));
        }

    }
}
