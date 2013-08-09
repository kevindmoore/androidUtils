package com.mastertechsoftware.views;

import com.mastertechsoftware.AndroidUtil.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;

import java.io.IOException;
import java.util.Map;

/**
 * User: kevin.moore
 * Copied straight from VideoView
 */
public class MasterVideoView extends SurfaceView implements MediaController.MediaPlayerControl {

	protected String TAG = "MasterVideoView";
	// settable by the client
	protected Uri mUri;
	protected Map<String, String> mHeaders;
	protected int mDuration;

	// all possible internal states
	protected static final int STATE_ERROR = -1;
	protected static final int STATE_IDLE = 0;
	protected static final int STATE_PREPARING = 1;
	protected static final int STATE_PREPARED = 2;
	protected static final int STATE_PLAYING = 3;
	protected static final int STATE_PAUSED = 4;
	protected static final int STATE_PLAYBACK_COMPLETED = 5;
	protected static final int STATE_SUSPEND = 6;
	protected static final int STATE_RESUME = 7;
	protected static final int STATE_SUSPEND_UNSUPPORTED = 8;

	// mCurrentState is a VideoView object's current state.
	// mTargetState is the state that a method caller intends to reach.
	// For instance, regardless the VideoView object's current state,
	// calling pause() intends to bring the object to a target state
	// of STATE_PAUSED.
	protected int mCurrentState = STATE_IDLE;
	protected int mTargetState = STATE_IDLE;

	// All the stuff we need for playing and showing a video
	protected SurfaceHolder mSurfaceHolder = null;
	protected MediaPlayer mMediaPlayer = null;
	protected int mVideoWidth;
	protected int mVideoHeight;
	protected int mSurfaceWidth;
	protected int mSurfaceHeight;
	protected MediaController mMediaController;
	protected MediaPlayer.OnCompletionListener mOnCompletionListener;
	protected MediaPlayer.OnPreparedListener mOnPreparedListener;
	private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
	protected int mCurrentBufferPercentage;
	protected MediaPlayer.OnErrorListener mOnErrorListener;
	protected int mSeekWhenPrepared;  // recording the seek position while preparing
	protected boolean mCanPause;
	protected boolean mCanSeekBack;
	protected boolean mCanSeekForward;
	protected boolean screenSuspended = false;
	protected int mStateWhenSuspended;  //state before calling suspend()
	private boolean videoLoadingNeeded = false;
	private boolean screenNeedsReloading = false;
	private boolean useAndroidAspectRatio = false;

	public MasterVideoView(Context context) {
		super(context);
		initVideoView();
	}

	public MasterVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initVideoView();
	}

	public MasterVideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initVideoView();
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (useAndroidAspectRatio) {
            int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
            int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
            if (mVideoWidth > 0 && mVideoHeight > 0) {
                if ( mVideoWidth * height  > width * mVideoHeight ) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                } else if ( mVideoWidth * height  < width * mVideoHeight ) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else {
                    //Log.i("@@@", "aspect ratio is correct: " +
                    //width+"/"+height+"="+
                    //mVideoWidth+"/"+mVideoHeight);
                }
            }
            //Log.i("@@@@@@@@@@", "setting size: " + width + 'x' + height);
            setMeasuredDimension(width, height);

        } else {
            int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
            int height = getDefaultSize(mVideoHeight, heightMeasureSpec);

            width = (width / 16) * 16;
            height = ((height) / 9) * 9;
            setMeasuredDimension(width, height);
        }
	}

    /**
     * Set this flag to use Android's Aspect ratio sizing, not the full screen
     * @param useAndroidAspectRatio
     */
    public void setUseAndroidAspectRatio(boolean useAndroidAspectRatio) {
        this.useAndroidAspectRatio = useAndroidAspectRatio;
        invalidate();
        requestLayout();
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
		int result = desiredSize;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		switch (specMode) {
			case MeasureSpec.UNSPECIFIED:
				/* Parent says we can be as big as we want. Just don't be larger
				 * than max size imposed on ourselves.
				 */
				result = desiredSize;
				break;

			case MeasureSpec.AT_MOST:
				/* Parent says we can be as big as we want, up to specSize.
				 * Don't be larger than specSize, and don't be larger than
				 * the max size imposed on ourselves.
				 */
				result = Math.min(desiredSize, specSize);
				break;

			case MeasureSpec.EXACTLY:
				// No choice. Do what we are told.
				result = specSize;
				break;
		}
		return result;
	}

	protected void initVideoView() {
		mVideoWidth = 0;
		mVideoHeight = 0;
		getHolder().addCallback(mSHCallback);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
		mCurrentState = STATE_IDLE;
		mTargetState = STATE_IDLE;
	}

	public void setVideoPath(String path) {
		setVideoURI(Uri.parse(path));
	}

	public void setVideoURI(Uri uri) {
		setVideoURI(uri, null);
	}

	/**
	 * Set the VIdeo URI
	 * @param uri
	 * @param headers
	 */
	public void setVideoURI(Uri uri, Map<String, String> headers) {
		mUri = uri;
		mHeaders = headers;
		mSeekWhenPrepared = 0;
		openVideo();
		requestLayout();
		invalidate();
	}

	public void stopPlayback() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			mTargetState = STATE_IDLE;
		}
	}

	protected void openVideo() {
		if (mUri == null || mSurfaceHolder == null) {
			videoLoadingNeeded = true;
			// not ready for playback just yet, will try again later
			return;
		}
		// Tell the music playback service to pause
		// TODO: these constants need to be published somewhere in the framework.
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		getContext().sendBroadcast(i);

		// we shouldn't clear the target state, because somebody might have
		// called start() previously
		release(false);
		screenNeedsReloading = false;

		try {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
			mDuration = -1;
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mCurrentBufferPercentage = 0;
			mMediaPlayer.setDataSource(getContext(), mUri);
			//			mMediaPlayer.setDataSource(getContext(), mUri, mHeaders);
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepareAsync();
			// we don't set the target state here either, but preserve the
			// target state that was there before.
			mCurrentState = STATE_PREPARING;
			attachMediaController();
		} catch (IOException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
		} catch (IllegalArgumentException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
		}
	}

	public void setMediaController(MediaController controller) {
		if (mMediaController != null) {
			mMediaController.hide();
		}
		mMediaController = controller;
		attachMediaController();
	}

	protected void attachMediaController() {
		if (mMediaPlayer != null && mMediaController != null) {
			mMediaController.setMediaPlayer(this);
			View anchorView = this.getParent() instanceof View ?
				(View) this.getParent() : this;
			mMediaController.setAnchorView(anchorView);
			mMediaController.setEnabled(isInPlaybackState());
		}
	}

	MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
		new MediaPlayer.OnVideoSizeChangedListener() {
			public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
				mVideoWidth = mp.getVideoWidth();
				mVideoHeight = mp.getVideoHeight();
				if (mVideoWidth != 0 && mVideoHeight != 0) {
					getHolder().setFixedSize(mVideoWidth, mVideoHeight);
				}
			}
		};

	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(MediaPlayer mp) {
			mCurrentState = STATE_PREPARED;

			/*
			   // Get the capabilities of the player for this stream
			   Metadata data = mp.getMetadata(MediaPlayer.METADATA_ALL,
										 MediaPlayer.BYPASS_METADATA_FILTER);

			   if (data != null) {
				   mCanPause = !data.has(Metadata.PAUSE_AVAILABLE)
						   || data.getBoolean(Metadata.PAUSE_AVAILABLE);
				   mCanSeekBack = !data.has(Metadata.SEEK_BACKWARD_AVAILABLE)
						   || data.getBoolean(Metadata.SEEK_BACKWARD_AVAILABLE);
				   mCanSeekForward = !data.has(Metadata.SEEK_FORWARD_AVAILABLE)
						   || data.getBoolean(Metadata.SEEK_FORWARD_AVAILABLE);
			   } else {
				   mCanPause = mCanSeekBack = mCanSeekForward = true;
			   }
   */
			mCanPause = mCanSeekBack = mCanSeekForward = true;

			if (mOnPreparedListener != null) {
				mOnPreparedListener.onPrepared(mMediaPlayer);
			}
			if (mMediaController != null) {
				mMediaController.setEnabled(true);
			}
			if (mSurfaceHolder == null || mSurfaceWidth == 0) {
				mVideoWidth = 0;
				mVideoHeight = 0;
			} else {
				mVideoWidth = mp.getVideoWidth();
				mVideoHeight = mp.getVideoHeight();
			}

			int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
			if (seekToPosition != 0) {
				seekTo(seekToPosition);
			}
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				//Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
				if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
					// We didn't actually change the size (it was already at the size
					// we need), so we won't get a "surface changed" callback, so
					// start the video here instead of in the callback.
					if (mTargetState == STATE_PLAYING) {
						start();
						if (mMediaController != null) {
							mMediaController.show();
						}
					} else if (!isPlaying() &&
						(seekToPosition != 0 || getCurrentPosition() > 0)) {
						if (mMediaController != null) {
							// Show the media controls when we're paused into a video and make 'em stick.
							mMediaController.show(0);
						}
					}
				}
			} else {
				// We don't know the video size yet, but should start anyway.
				// The video size might be reported to us later.
				if (mTargetState == STATE_PLAYING) {
					start();
				}
			}
		}
	};

	protected MediaPlayer.OnCompletionListener mCompletionListener =
		new MediaPlayer.OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				mCurrentState = STATE_PLAYBACK_COMPLETED;
				mTargetState = STATE_PLAYBACK_COMPLETED;
				if (mMediaController != null) {
					mMediaController.hide();
				}
				if (mOnCompletionListener != null) {
					mOnCompletionListener.onCompletion(mMediaPlayer);
				}
			}
		};

	protected MediaPlayer.OnErrorListener mErrorListener =
		new MediaPlayer.OnErrorListener() {
			public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
				Log.d(TAG, "Error: " + framework_err + "," + impl_err);
				mCurrentState = STATE_ERROR;
				mTargetState = STATE_ERROR;
				if (mMediaController != null) {
					mMediaController.hide();
				}

				/* If an error handler has been supplied, use it and finish. */
				if (mOnErrorListener != null) {
					if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
						return true;
					}
				}

				/* Otherwise, pop up an error dialog so the user knows that
							 * something bad has happened. Only try and pop up the dialog
							 * if we're attached to a window. When we're going away and no
							 * longer have a window, don't bother showing the user an error.
							 */
				if (getWindowToken() != null) {
					Resources r = getContext().getResources();
					int messageId;

					if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
						messageId = R.string.VideoView_error_text_invalid_progressive_playback;
					} else {
						messageId = android.R.string.VideoView_error_text_unknown;
					}

					new AlertDialog.Builder(getContext())
						.setTitle(R.string.VideoView_error_title)
						.setMessage(messageId)
						.setPositiveButton(R.string.VideoView_error_button,
										   new DialogInterface.OnClickListener() {
											   public void onClick(DialogInterface dialog, int whichButton) {
												   /* If we get here, there is no onError listener, so
																							* at least inform them that the video is over.
																							*/
												   if (mOnCompletionListener != null) {
													   mOnCompletionListener.onCompletion(mMediaPlayer);
												   }
											   }
										   })
						.setCancelable(false)
						.show();
				}
				return true;
			}
		};

	protected MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
		new MediaPlayer.OnBufferingUpdateListener() {
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
				mCurrentBufferPercentage = percent;
			}
		};

	/**
	 * Register a callback to be invoked when the media file
	 * is loaded and ready to go.
	 *
	 * @param l The callback that will be run
	 */
	public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
		mOnPreparedListener = l;
	}

	/**
	 * Register a callback to be invoked when the end of a media file
	 * has been reached during playback.
	 *
	 * @param l The callback that will be run
	 */
	public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
		mOnCompletionListener = l;
	}

	/**
	 * Register a callback to be invoked when a seek operation has been
	 * completed.
	 *
	 * @param listener the callback that will be run
	 */
	public void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener listener)
	{
		mOnSeekCompleteListener = listener;
	}
	/**
	 * Register a callback to be invoked when an error occurs
	 * during playback or setup.  If no listener is specified,
	 * or if the listener returned false, VideoView will inform
	 * the user of any errors.
	 *
	 * @param l The callback that will be run
	 */
	public void setOnErrorListener(MediaPlayer.OnErrorListener l) {
		mOnErrorListener = l;
	}

	/**
	 * SurfaceHolder callback. Handle state changes
	 */
	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {

		public void surfaceChanged(SurfaceHolder holder, int format,
								   int w, int h) {
			mSurfaceWidth = w;
			mSurfaceHeight = h;
			boolean isValidState = (mTargetState == STATE_PLAYING);
			boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
			if (mMediaPlayer != null && isValidState && hasValidSize) {
				if (mSeekWhenPrepared != 0) {
					seekTo(mSeekWhenPrepared);
				}
				start();
				if (mMediaController != null) {
					mMediaController.show();
				}
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			mSurfaceHolder = holder;
			if (videoLoadingNeeded) {
				videoLoadingNeeded = false;
				openVideo();
				return;
			}
			//resume() was called before surfaceCreated()
			if (mMediaPlayer != null) {
				mMediaPlayer.setDisplay(mSurfaceHolder);
				if ((mCurrentState == STATE_PAUSED) && (mTargetState == STATE_RESUME)) {
					resume();
				} else if (mTargetState == STATE_RESUME) {
					mSeekWhenPrepared = getCurrentPosition();
					openVideo();
				}
			} else {
				if (mCurrentState == STATE_PLAYING) {
					mSeekWhenPrepared = getCurrentPosition();
				}
				if (mCurrentState == STATE_PAUSED && mTargetState == STATE_RESUME) {
					openVideo();
				}
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// after we return from this we can't use the surface any more
			mSurfaceHolder = null;
			if (mMediaPlayer != null) {
				mMediaPlayer.setDisplay(null);
			}
			if (mMediaController != null)
				mMediaController.hide();
//			if (mCurrentState != STATE_SUSPEND) {
//				release(true);
//			}
		}
	};

	/*
	 * release the media player in any state
	 */
	protected void release(boolean cleartargetstate) {
		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			if (cleartargetstate) {
				mTargetState = STATE_IDLE;
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isInPlaybackState() && mMediaController != null) {
			toggleMediaControlsVisiblity();
		}
		return false;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent ev) {
		if (isInPlaybackState() && mMediaController != null) {
			toggleMediaControlsVisiblity();
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
			keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
			keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
			keyCode != KeyEvent.KEYCODE_MENU &&
			keyCode != KeyEvent.KEYCODE_CALL &&
			keyCode != KeyEvent.KEYCODE_ENDCALL;
		if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
			if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
				keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
				if (mMediaPlayer.isPlaying()) {
					pause();
					mMediaController.show();
				} else {
					start();
					mMediaController.hide();
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
				&& mMediaPlayer.isPlaying()) {
				pause();
				mMediaController.show();
			} else {
				toggleMediaControlsVisiblity();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	protected void toggleMediaControlsVisiblity() {
		if (mMediaController.isShowing()) {
			mMediaController.hide();
		} else {
			mMediaController.show();
		}
	}

	public void start() {
		if (isInPlaybackState() && mCurrentState != STATE_PLAYING) {
			mMediaPlayer.start();
			mCurrentState = STATE_PLAYING;
		}
		mTargetState = STATE_PLAYING;
	}

	public void pause() {
		if (isInPlaybackState()) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				mCurrentState = STATE_PAUSED;
			}
		}
		mTargetState = STATE_PAUSED;
	}

	public void suspend() {
		if (mMediaPlayer == null) {
			return;
		}
		if (isInPlaybackState() && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			mCurrentState = STATE_PAUSED;
			mTargetState = STATE_PAUSED;
		}
	}

	public void resume() {
		if (mSurfaceHolder == null && mCurrentState == STATE_PAUSED) {
			mTargetState = STATE_RESUME;
			return;
		}
		if (mCurrentState == STATE_PREPARED) {
			start();
			return;
		}
		if (screenNeedsReloading) {
			mSeekWhenPrepared = getCurrentPosition();
			openVideo();
			return;
		}
		if (mCurrentState == STATE_PLAYING && screenSuspended) {
			mTargetState = STATE_RESUME;
			return;
		}
		if (mMediaPlayer != null && (mCurrentState == STATE_PAUSED || mCurrentState == STATE_SUSPEND)) {
			mMediaPlayer.start();
			mTargetState = STATE_RESUME;
			mCurrentState = STATE_RESUME;
		}
		if (mCurrentState == STATE_SUSPEND_UNSUPPORTED) {
			openVideo();
		}
	}

	/**
	 * Suspend the screen while playing
	 */
	public void suspendScreen() {
		if (mMediaPlayer != null) {
			mMediaPlayer.setScreenOnWhilePlaying(false);
		}
		screenSuspended = true;
		screenNeedsReloading = true;
	}

	/**
	 * Resume the screen while playing
	 */
	public void resumeScreen() {
		if (mMediaPlayer != null) {
			mMediaPlayer.setScreenOnWhilePlaying(true);
		}
		screenSuspended = false;
	}


	// cache duration as mDuration for faster access
	public int getDuration() {
		if (isInPlaybackState()) {
			if (mDuration > 0) {
				return mDuration;
			}
			mDuration = mMediaPlayer.getDuration();
			return mDuration;
		}
		mDuration = -1;
		return mDuration;
	}

	public int getCurrentPosition() {
		if (isInPlaybackState()) {
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	public void seekTo(int msec) {
		if (isInPlaybackState()) {
			mMediaPlayer.seekTo(msec);
			mSeekWhenPrepared = 0;
		} else {
			mSeekWhenPrepared = msec;
		}
	}

	public boolean isPlaying() {
		return isInPlaybackState() && mMediaPlayer.isPlaying();
	}

	public int getBufferPercentage() {
		if (mMediaPlayer != null) {
			return mCurrentBufferPercentage;
		}
		return 0;
	}

	protected boolean isInPlaybackState() {
		return (mMediaPlayer != null &&
			mCurrentState != STATE_ERROR &&
			mCurrentState != STATE_IDLE &&
			mCurrentState != STATE_PREPARING);
	}

	public boolean canPause() {
		return mCanPause;
	}

	public boolean canSeekBackward() {
		return mCanSeekBack;
	}

	public boolean canSeekForward() {
		return mCanSeekForward;
	}

	@Override
	public int getAudioSessionId() {
		if (mMediaPlayer != null) {
			return mMediaPlayer.getAudioSessionId();
		}
		return 0;
	}
}
