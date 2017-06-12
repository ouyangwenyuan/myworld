package com.fotoable.piano;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.fotoable.piano.midi.MidiDecodeActivity;
import com.fotoable.piano.utils.ResourceUtils;
import com.fotoable.piano.vary.VaryActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bt_jump_play).setOnClickListener(this);
        findViewById(R.id.bt_jump_opengl).setOnClickListener(this);

        String fileName = "twinkle_tutorial_1.mid";
        File file = new File(ResourceUtils.INSTANCE.applicationFilesDir(this) + "/" + fileName);
        ResourceUtils.INSTANCE.extractAsset(this, fileName, file);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_jump_play:
                startActivity(new Intent(this, MidiDecodeActivity.class));
                break;
            case R.id.bt_jump_opengl:
                startActivity(new Intent(this, VaryActivity.class));
                break;
            default:
                break;
        }
    }
}
