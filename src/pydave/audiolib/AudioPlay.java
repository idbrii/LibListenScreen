
package pydave.audiolib;

import java.io.File;

import pydave.engoid.sys.ExternalStorage;
import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

public class AudioPlay extends Activity {
    static final String TAG = "AudioPlay";

    MediaPlayer player;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playback);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        setupButtons();

        final Uri uri = getUriToPlay();
        setupPlayer(uri);

        // start immediately
        player.start();
    }

    /**
     * Stop the music.
     * 
     * @see Activity#onStop()
     */
    @Override
    protected void onStop() {
        // is it even possible for onStop to be called before onCreate
        // (do we need to check that player is not null?)
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
        }

        super.onStop();
    }

    /**
     * Add click listeners to the buttons.
     */
    void setupButtons() {
        final ImageButton rew = (ImageButton) findViewById(R.id.rew);
        rew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipBackward();
            }
        });
        final ImageButton playpause = (ImageButton) findViewById(R.id.playpause);
        playpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playOrPause(playpause);
            }
        });
        final ImageButton ffwd = (ImageButton) findViewById(R.id.ffwd);
        ffwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipForward();
            }
        });
    }

    /**
     * Create the MediaPlayer and set it to play the input uri. We should only
     * play one file per invocation, so this works nicely.
     * 
     * @param uri The audio file to play
     */
    void setupPlayer(Uri uri) {
        if (uri == null) {
            // TODO: error message
            return;
        }

        player = MediaPlayer.create(this, uri);
        if (player == null) {
            // TODO: error message
            finish();
            return;
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                returnResult(Result.COMPLETE);
            }
        });
    }

    /**
     * Determine the uri that we should play.
     * Currently this is a hard coded value, but later it should be retrieved
     * from the Intent.
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
            // TODO: error message
            return null;
        }

        return Uri.fromFile(f);
    }

    static class Result {
        static final int SKIP_BACK;

        static final int SKIP_FORWARD;

        static final int COMPLETE;
        static {
            int i = 0;
            SKIP_BACK = i++;
            SKIP_FORWARD = i++;
            COMPLETE = i++;
        }
    }

    void skipBackward() {
        returnResult(Result.SKIP_BACK);
    }

    void skipForward() {
        returnResult(Result.SKIP_FORWARD);
    }

    void playOrPause(ImageButton playPauseButton) {
        if (player.isPlaying()) {
            player.pause();

            playPauseButton.setImageResource(R.drawable.media_playback_start);
        }
        else {
            player.start();

            playPauseButton.setImageResource(R.drawable.media_playback_pause);
        }
    }

    void returnResult(int returnCode) {
        // TODO: return the result to the calling activity
    }
}
