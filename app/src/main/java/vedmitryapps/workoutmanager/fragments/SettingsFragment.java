package vedmitryapps.workoutmanager.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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


public class SettingsFragment extends Fragment {

    @BindView(R.id.switchVibration)
    Switch switchVibration;

    @BindView(R.id.soundName)
    TextView soundName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, v);
        soundName.setText(Constants.soundsTitle[SharedManager.getIntProperty(Constants.KEY_DEF_SOUND)]);
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
    }

    @OnClick(R.id.backButton)
    public void backButton(View v){
        getActivity().onBackPressed();
    }
}
