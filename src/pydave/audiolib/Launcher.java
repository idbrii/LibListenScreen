/**
 * Copyright (C) 2011 dbriscoe
 */

package pydave.audiolib;

import android.app.Activity;
import android.content.Intent;
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
        if (resultCode == AudioPlay.Result.SKIP_BACK) {
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
        // TODO: send actual data
        i.putExtra(AudioPlay.Keys.URI, "");
        i.putExtra(AudioPlay.Keys.START_TIME, 0);

        startActivityForResult(i, AudioPlay.Request.START);
    }
}
