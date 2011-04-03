
package pydave.audiolib;

import java.io.File;

import pydave.engoid.sys.ExternalStorage;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

public class AudioPlay extends Activity {
    static final String TAG = "AudioPlay";

    MediaPlayer player;

    CountDownTimer elapsedTimeCounter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playback);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        setupButtons();

        final Uri uri = getUriToPlay();
        setupPlayback(uri);

        // start immediately
        player.start();
        startTimeCodeUpdater();
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
            player = null;
        }

        if (elapsedTimeCounter != null) {
            elapsedTimeCounter.cancel();
            elapsedTimeCounter = null;
        }

        super.onStop();
    }

    /**
     * {@inheritDoc}
     * 
     * @see Activity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        // don't call super

        returnResult(RESULT_CANCELED);
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
     * Setup the text indicating the elapsed time and the total duration of the
     * audio file.
     */
    void setupTimeCodes() {
        // assert: player is a valid MediaPlayer with an audio file prepared

        final TextView duration = (TextView) findViewById(R.id.duration);
        duration.setText(timeCodeToString(player.getDuration()));

        final TextView elapsed = (TextView) findViewById(R.id.elapsed);
        elapsed.setText(timeCodeToString(player.getCurrentPosition()));
    }

    /**
     * Start a timer to keep the elapsed time up-to-date.
     * Note that we only run the timer for the duration of the audio. If we
     * were to pause, then we'd be out of whack and need to restart on play.
     * Might as well stop the timer on pause.
     */
    void startTimeCodeUpdater() {
        final TextView elapsed = (TextView) findViewById(R.id.elapsed);

        // setup the timer that will keep elapsed updated
        long ELAPSED_UPDATE_INTERVAL_ms = 500; // half a second
        long duration_ms = player.getDuration(); // duration is milliseconds
        elapsedTimeCounter = new CountDownTimer(duration_ms, ELAPSED_UPDATE_INTERVAL_ms) {
            @Override
            public void onTick(long millisUntilFinished) {
                elapsed.setText(timeCodeToString(player.getCurrentPosition()));
            }

            @Override
            public void onFinish() {
                // do nothing
            }
        };
        elapsedTimeCounter.start();
    }

    void stopTimeCodeUpdater() {
        elapsedTimeCounter.cancel();
    }

    String timeCodeToString(int timeCode_ms) {
        return DateUtils.formatElapsedTime(timeCode_ms / 1000);
    }

    /**
     * Create the MediaPlayer and set it to play the input uri. We should only
     * play one file per invocation, so this works nicely.
     * 
     * @param uri The audio file to play
     */
    void setupPlayback(Uri uri) {
        if (uri == null) {
            // TODO: error message
            return;
        }

        // TODO: if we don't stop before we end, will the player still be
        // playing when we resume?
        player = MediaPlayer.create(this, uri);
        if (player == null) {
            // TODO: error message
            finish();
            return;
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO: does this work?
                returnResult(Result.COMPLETE);
            }
        });

        setupTimeCodes();
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

    // TODO: move this constant somewhere shared
    // all request codes must be >= 0
    static final int REQUEST_FIRST_USER = 0;

    static class Request {
        static final int START;

        static {
            int i = REQUEST_FIRST_USER;
            START = i++;
        }
    }

    static class Keys {
        static final String URI = "URI";

        static final String START_TIME = "START_TIME";

        static final String CURRENT_TIME = "CURRENT_TIME";
    }

    static class Result {
        static final int SKIP_BACK;

        static final int SKIP_FORWARD;

        static final int COMPLETE;
        static {
            int i = RESULT_FIRST_USER;
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

            // don't need this if we're paused
            stopTimeCodeUpdater();

            playPauseButton.setImageResource(R.drawable.media_playback_start);
        }
        else {
            player.start();

            // gotta start updating again now that we're playing
            startTimeCodeUpdater();

            playPauseButton.setImageResource(R.drawable.media_playback_pause);
        }
    }

    void returnResult(int returnCode) {
        final Intent i = new Intent();
        i.putExtra(Keys.CURRENT_TIME, player.getCurrentPosition());
        setResult(returnCode, i);

        finish();
    }
}
