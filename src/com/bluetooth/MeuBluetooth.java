package com.bluetooth;

import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.util.Log;
import android.widget.Toast;

public class MeuBluetooth implements OnAudioFocusChangeListener {

	/** The m context. */
	private final Context mContext;

	/** The m bluetooth adapter. */
	private final BluetoothAdapter mBluetoothAdapter;

	/** The m bluetooth headset. */
	private BluetoothHeadset mBluetoothHeadset;

	/** The m connected headset. */
	private BluetoothDevice mConnectedHeadset;

	/** The m audio manager. */
	private final AudioManager mAudioManager;
	/** The m listerner. */
	private BluetoothListener mListerner;

	/** The Constant TAG. */
	private static final String TAG = "BluetoothHeadsetUtils"; //$NON-NLS-1$

	public MeuBluetooth(final Context context, final BluetoothListener listener) {
		mListerner = listener;
		mContext = context;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		mAudioManager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);

	}

	public boolean start() {
		if (mBluetoothAdapter != null
				&& mAudioManager.isBluetoothScoAvailableOffCall()
				&& mBluetoothAdapter.getProfileProxy(mContext,
						mHeadsetProfileListener, BluetoothProfile.HEADSET)) {
			// All the detection and audio connection are done in
			// mHeadsetProfileListener
			requestAudioFocus();
			return true;
		}
		return false;
	}

	private void requestAudioFocus() {
		if (mAudioManager != null) {
			// Request audio focus for playback
			int result = mAudioManager.requestAudioFocus(this,
			// Use the music stream.
					AudioManager.STREAM_MUSIC,
					// Request permanent focus.
					AudioManager.AUDIOFOCUS_GAIN);

			if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

				if (mAudioManager.isBluetoothA2dpOn()) {
					Log.d("Bluetooth", "BluetoothA2dpOn");
					// Adjust output for Bluetooth.
				} else if (mAudioManager.isSpeakerphoneOn()) {
					Log.d("BluetoonHeadsetConnectedoth", "SpeakerphoneOn");
					// Adjust output for Speakerphone.
				} else {
					Log.d("Bluetooth",
							"not BluetoothA2dpOn and not SpeakerphoneOn");
					// If audio plays and noone can hear it, is it still
					// playing?
				}
			}
		}
	}

	private final BluetoothProfile.ServiceListener mHeadsetProfileListener = new BluetoothProfile.ServiceListener() {

		/**
		 * This method is never called, even when we closeProfileProxy on
		 * onPause. When or will it ever be called???
		 */
		@Override
		public void onServiceDisconnected(final int profile) {
			//$NON-NLS-1$
			Log.d(TAG, "Profile listener onServiceDisconnected");
			stop();
		}

		@Override
		public void onServiceConnected(final int profile,
				final BluetoothProfile proxy) {
			//$NON-NLS-1$
			Log.d(TAG, "Profile listener onServiceConnected");

			// mBluetoothHeadset is just a headset profile,
			// it does not represent a headset device.
			mBluetoothHeadset = (BluetoothHeadset) proxy;

			// If a headset is connected before this application starts,
			// ACTION_CONNECTION_STATE_CHANGED will not be broadcast.
			// So we need to check for already connected headset.
			List<BluetoothDevice> devices = mBluetoothHeadset
					.getConnectedDevices();

			if (!devices.isEmpty()) {
				// Only one headset can be connected at a time,
				// so the connected headset is at index 0.
				mConnectedHeadset = devices.get(0);
/*				Toast.makeText(
						mContext,
						"conectou: "
								+ mBluetoothHeadset
										.startVoiceRecognition(mConnectedHeadset),
						Toast.LENGTH_SHORT).show();
				Toast.makeText(mContext, mConnectedHeadset.getName(),
						Toast.LENGTH_SHORT).show();
				Toast.makeText(mContext, mConnectedHeadset.getName(),
						Toast.LENGTH_SHORT).show();
*/
				mListerner.onHeadsetConnected();

				// Should not need count down timer, but just in case.
				// See comment below in mHeadsetBroadcastReceiver
				// onReceive()

				Log.d(TAG, "Start count down"); //$NON-NLS-1$
			}

			// During the active life time of the app, a user may turn
			// on and
			// off the headset.
			// So register for broadcast of connection states.
			mContext.registerReceiver(mHeadsetBroadcastReceiver,
					new IntentFilter(
							BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED));
			// Calling startVoiceRecognition doesnt result in
			// immediate audio
			// connection.
			// So register for broadcast of audio connection states.
			// This
			// broadcast will
			// only be sent if startVoiceRecognition returns true.
			mContext.registerReceiver(mHeadsetBroadcastReceiver,
					new IntentFilter(
							BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED));

		}
	};

	/**
	 * API >= 11 Handle headset and Sco audio connection states.
	 */
	private final BroadcastReceiver mHeadsetBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			String action = intent.getAction();
			int state;
			if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
				state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE,
						BluetoothHeadset.STATE_DISCONNECTED);
				String mensagem = "";
				switch (state) {
				case BluetoothHeadset.STATE_AUDIO_DISCONNECTED:
					mensagem = "adisconnected";
					break;

				case BluetoothHeadset.STATE_AUDIO_CONNECTED:
					mensagem = "aconnected";
					break;
				case BluetoothHeadset.STATE_AUDIO_CONNECTING:
					mensagem = "aconnecting";
					break;
				case BluetoothHeadset.STATE_CONNECTED:

					mensagem = "connected";
					mConnectedHeadset = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					mensagem += " name: " + mConnectedHeadset.getName();
					break;
				case BluetoothHeadset.STATE_CONNECTING:
					mensagem = "connecting";
					break;
				case BluetoothHeadset.STATE_DISCONNECTED:
					mensagem = "disconnected";
					break;
				case BluetoothHeadset.STATE_DISCONNECTING:
					mensagem = "disconnected";
					break;
				}
				Toast.makeText(mContext,
						mensagem + "\nnumberaction = " + state,
						Toast.LENGTH_SHORT).show();
				 if (state == BluetoothHeadset.STATE_CONNECTED) {
				 mConnectedHeadset = intent
				 .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				 // Calling startVoiceRecognition always returns
				 // false here,
				 // that why a count down timer is implemented to
				 // call
				 // startVoiceRecognition in the onTick.
				
				 // override this if you want to do other thing when
				 // the
				 // device is connected.
				 mListerner.onHeadsetConnected();
				
									Log.d(TAG, "Start count down"); //$NON-NLS-1$
				 } else if (state == BluetoothHeadset.STATE_DISCONNECTED) {
				 // Calling stopVoiceRecognition always returns false
				 // here
				 // as it should since the headset is no longer
				 // connected.
				 mConnectedHeadset = null;
				
				 // override this if you want to do other thing when
				 // the
				 // device is disconnected.
				 mListerner.onHeadsetDisconnected();
				
									Log.d(TAG, "Headset disconnected"); //$NON-NLS-1$
				 }
				 } else { // audio
				 state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE,
				 BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
				 Log.d(TAG,
								//$NON-NLS-1$ //$NON-NLS-2$
				 "\nAction = " + action + "\nState = " + state);
				 if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
									//$NON-NLS-1$
				 Log.d(TAG, "\nHeadset audio connected");
				
				 // override this if you want to do other thing when
				 // headset
				 // audio is connected.
				 mListerner.onScoAudioConnected();
				 } else if (state ==
				 BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
				 // The headset audio is disconnected, but calling
				 // stopVoiceRecognition always returns true here.
				
				 // override this if you want to do other thing when
				 // headset
				 // audio is disconnected.
				 mListerner.onScoAudioDisconnected();
									//$NON-NLS-1$
				 Log.d(TAG, "Headset audio disconnected");
				 }
			}
		}
	};

	@Override
	public void onAudioFocusChange(int arg0) {
		// TODO Auto-generated method stub

	}

	protected final void stop() {
		Log.d(TAG, "stopBluetooth11"); //$NON-NLS-1$

		if (mBluetoothHeadset != null) {
			// Need to call stopVoiceRecognition here when the app
			// change orientation or close with headset still turns on.
			mBluetoothHeadset.stopVoiceRecognition(mConnectedHeadset);
			mContext.unregisterReceiver(mHeadsetBroadcastReceiver);
			mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET,
					mBluetoothHeadset);
			mBluetoothHeadset = null;
			abandonAudioFocus();
		}
	}

	private void abandonAudioFocus() {
		if (mAudioManager != null) {
			mAudioManager.abandonAudioFocus(this);
		}
	}

	protected boolean startVoiceBluetoothRecognition() {
		try {
			return mBluetoothHeadset.startVoiceRecognition(mConnectedHeadset);
		} catch (Exception e) {
			return false;
		}
	}

	protected boolean stopVoiceBluetoothRecognition() {
		try {
			return mBluetoothHeadset.stopVoiceRecognition(mConnectedHeadset);
		} catch (Exception e) {
			return false;
		}
	}

}
