
package pydave.audiolib;

import java.io.File;

import pydave.engoid.sys.ExternalStorage;
import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;

public class AudioPlay extends Activity {
    static final String TAG = "AudioPlay";

    MediaPlayer player;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playback);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        
        play();
    }

    /**
     * {@inheritDoc}
     * @see Activity#onStop()
     */
    protected void onStop()
    {
        if (player != null && player.isPlaying()) {
            player.stop();
        }

        super.onStop();
    }

    public void play() {
        final ExternalStorage ext = new ExternalStorage();

        // I've placed some test mp3s in:
        // /sdcard/Android/data/pydave.demo/files/
        final File f = ext.getFile("pydave.demo",
        // "music.mp3");
        // "podcast.mp3");
              "shortpod.mp3");

        if (f == null) {
            // TODO: error message
            return;
        }

        final Uri uri = Uri.fromFile(f);
        player = MediaPlayer.create(this, uri);
        player.start();
    }
}
