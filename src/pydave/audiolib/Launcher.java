/**
 * Copyright (C) 2011 dbriscoe
 */

package pydave.audiolib;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
        final TextView text = (TextView) findViewById(R.id.text);
        // TODO: display useful text
        text.setText("" + resultCode);
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
