package vedmitryapps.workoutmanager.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vedmitryapps.workoutmanager.Constants;
import vedmitryapps.workoutmanager.R;
import vedmitryapps.workoutmanager.SharedManager;
import vedmitryapps.workoutmanager.adapters.SoundRecyclerAdapter;


public class SettingsFragment extends Fragment {

    @BindView(R.id.switchVibration)
    Switch switchVibration;

    @BindView(R.id.switchNotification)
    Switch switchNotification;

    @BindView(R.id.switchNotificationTime)
    Switch switchNotificationTime;

    @BindView(R.id.defaultSoundContainer)
    ConstraintLayout sound;

    @BindView(R.id.soundName)
    TextView soundName;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, v);
        soundName.setText(Constants.soundsTitle[SharedManager.getIntProperty(Constants.KEY_SOUND_POSITION)]);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
    }

    @OnClick(R.id.defaultSoundContainer)
    public void dsf(View v){
            Log.d("TAG21", "click show sound dialog");

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
}
