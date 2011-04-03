/**
 * Copyright (C) 2011 dbriscoe
 */

package pydave.audiolib;

import java.io.File;

import android.widget.Toast;

import pydave.engoid.sys.ExternalStorage;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Launcher extends Activity {

    /**
     * {@inheritDoc}
     * 
     * @see Activity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        final Button launch = (Button) findViewById(R.id.launch);
        launch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAudio();
            }
        });

        launchAudio();
    }

    /**
     * {@inheritDoc}
     * 
     * @see Activity#onActivityResult(int,int,Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String output = "";
        if (resultCode == RESULT_CANCELED) {
            output += "Cancel";
        }
        else if (resultCode == AudioPlay.Result.SKIP_BACK) {
            output += "Back";
        }
        else if (resultCode == AudioPlay.Result.SKIP_FORWARD) {
            output += "Forward";
        }
        else if (resultCode == AudioPlay.Result.COMPLETE) {
            output += "Complete";
        }
        else {
            output += "Unknown";
        }

        output += " (" + resultCode + ")";

        if (data != null) {
            final Bundle extras = data.getExtras();
            if (extras != null) {
                long timeCode_ms = extras.getInt(AudioPlay.Keys.CURRENT_TIME);
                output += "\n" + DateUtils.formatElapsedTime(timeCode_ms / 1000);
            }
        }

        final TextView text = (TextView) findViewById(R.id.text);
        text.setText(output);
    }

    /**
     */
    protected void launchAudio() {
        final Intent i = new Intent(this, AudioPlay.class);
        i.putExtra(AudioPlay.Keys.URI, getUriToPlay().toString());
        i.putExtra(AudioPlay.Keys.START_TIME, 0);

        startActivityForResult(i, AudioPlay.Request.START);
    }

    /**
     * Determine the uri that we should play.
     * 
     * @return null on failure. Otherwise, a uri for an audio file.
     */
    public Uri getUriToPlay() {
        final ExternalStorage ext = new ExternalStorage();

        // I've placed some test mp3s in:
        // /sdcard/Android/data/pydave.demo/files/
        final File f = ext.getFile("pydave.demo",
        // "music.mp3");
        // "podcast.mp3");
                "shortpod.mp3");

        if (f == null) {
            Toast.makeText(this, "Failed to find file", Toast.LENGTH_SHORT);
            return null;
        }

        return Uri.fromFile(f);
    }
}
