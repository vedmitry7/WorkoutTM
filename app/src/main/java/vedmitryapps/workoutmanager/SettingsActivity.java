package vedmitryapps.workoutmanager;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vedmitryapps.workoutmanager.adapters.SoundRecyclerAdapter;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.switchVibration)
    Switch switchVibration;


    @BindView(R.id.soundName)
    TextView soundName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        switchVibration.setChecked(SharedManager.getProperty(Constants.KEY_DEF_VIBRATION));

        switchVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedManager.addProperty(Constants.KEY_DEF_VIBRATION, isChecked);
            }
        });

        soundName.setText(Constants.soundsTitle[SharedManager.getIntProperty(Constants.KEY_DEF_SOUND)]);
    }

    @OnClick(R.id.defaultSoundContainer)
    public void showChooseSoundDialog(View view) {

        Log.d("TAG21", "click show sound dialog");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View dialogView = inflater.inflate(R.layout.choose_sound_dialog_layout, null);
        dialogBuilder.setView(dialogView);

        final RecyclerView recyclerView = dialogView.findViewById(R.id.soundRecyclerView);

        final SoundRecyclerAdapter adapter = new SoundRecyclerAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        adapter.setSelectedPosition(SharedManager.getIntProperty(Constants.KEY_DEF_SOUND));

        dialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                soundName.setText(adapter.getSoundName());
               SharedManager.addIntProperty(Constants.KEY_DEF_SOUND, adapter.getSoundPosition());
                dialog.dismiss();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }
}
