
package pydave.lib.listenscreen;

import java.io.FileNotFoundException;

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
import android.widget.Toast;

/**
 * A simple UI for playing audio files.
 * Currently provides these features:
 * - Accept a song, descriptive text, and start time
 * - so it can start in the middle of a song/podcast
 * - Immediately begin playing the song
 * - Provide a user interface like the Android Music app
 * - Return the button pressed (if applicable) and the current song position
 * - Return to caller once the song ends
 * - Show in front of the lock screen
 */
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

        setupText();

        // create the MediaPlayer and connect it to the UI
        try {
            setupPlayback(getUriToPlay());
        }
        catch (FileNotFoundException ex) {
            // If the file didn't exist, then we didn't create the MediaPlayer.
            // We can't do anything at this point, so quit this Activity and
            // return to caller.
            Toast.makeText(this, ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();

            // we can't even use returnResult because nothing is initialized
            // set the error code and bail!
            setResult(Result.ERROR);
            finish();
            return;
        }

        player.seekTo(getStartTimeCode());

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

    void setupText() {
        final Intent received = getIntent();
        final Bundle data = received.getExtras();

        setTextViewFromBundle(data, Keys.HEADER, R.id.title);
        setTextViewFromBundle(data, Keys.DESCRIPTION, R.id.description);
    }

    void setTextViewFromBundle(Bundle data, String key, int textViewId) {
        String text = data.getString(key);
        if (text == null) {
            text = "";
        }
        final TextView view = (TextView) findViewById(textViewId);
        view.setText(text);
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
    void setupPlayback(Uri uri) throws FileNotFoundException {
        // TODO: if we don't stop before we end, will the player still be
        // playing when we resume?
        player = MediaPlayer.create(this, uri);
        if (player == null) {
            final String errMsg_fmt = getString(R.string.file_no_exist_fmt);
            throw new FileNotFoundException(String.format(errMsg_fmt, uri.toString()));
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                returnResult(Result.COMPLETE);
            }
        });

        setupTimeCodes();
    }

    /**
     * Determine the uri that we should play from the Intent. The calling
     * Activity needs to tell us what file to play.
     * 
     * @return A uri for an audio file.
     */
    public Uri getUriToPlay() throws FileNotFoundException {
        final Intent received = getIntent();
        final Bundle data = received.getExtras();

        // we must receive the URI value or we can't do anything
        final String uriText = data.getString(Keys.URI);
        if (uriText == null) {
            final String errMsg_fmt = getString(R.string.missing_parameter_fmt);
            throw new FileNotFoundException(String.format(errMsg_fmt, Keys.URI));
        }

        return Uri.parse(uriText);
    }

    /**
     * Determine where in the song we should start playing from the Intent.
     * The calling Activity can tell us where to start. If not, we start at
     * the beginning.
     * 
     * @return A time in the audio file. Does not validate that the time fits
     *         in the length of the duration.
     */
    public int getStartTimeCode() {
        final Intent received = getIntent();
        final Bundle data = received.getExtras();

        // this value is optional. if not received, we assume the start.
        return data.getInt(Keys.START_TIME, 0);
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
        // ////
        // INPUT
        //
        // A uri describing an audio file. Required.
        static final String URI = "URI";

        // Title information for the audio file: artist and track name.
        // Optional.
        static final String HEADER = "HEADER";

        // Long form text description of the audio file. Optional.
        static final String DESCRIPTION = "DESCRIPTION";

        // When in the song to start playing. Time in milliseconds. Optional.
        static final String START_TIME = "START_TIME";

        // ////
        // OUTPUT
        //
        // Where in the song we stopped playing. Time in milliseconds.
        static final String CURRENT_TIME = "CURRENT_TIME";
    }

    static class Result {
        static final int ERROR;

        static final int SKIP_BACK;

        static final int SKIP_FORWARD;

        static final int COMPLETE;
        static {
            int i = RESULT_FIRST_USER;
            ERROR = i++;
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
