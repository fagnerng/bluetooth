package com.bluetooth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class MainActivity extends Activity implements RecognitionListener,
		OnInitListener {

	protected static final String TAG = "main activity";
	private MeuBluetooth mBlue;
	private BluetoothListener mListerner;
	private TextToSpeech mtts;
	private SpeechRecognizer mSpeechRecognizer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null)
			init();
	}

	private void init() {

		mListerner = new BluetoothListener() {

			@Override
			public void onScoAudioDisconnected() {
				Toast.makeText(MainActivity.this, "Audio Disconectado",
						Toast.LENGTH_SHORT).show();

			}

			@Override
			public void onScoAudioConnected() {
				Toast.makeText(MainActivity.this, "Audio Conectado",
						Toast.LENGTH_SHORT).show();

			}

			@Override
			public void onHeadsetDisconnected() {
				// Toast.makeText(MainActivity.this,
				// "Audio e Voz disconectado", Toast.LENGTH_SHORT).show();
				String msg = mBlue.stopVoiceBluetoothRecognition() ? "parou o voice recognition"
						: "nao ha headsetconectado ou nao foi possivel disconectar";
				Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT)
						.show();
			}

			@Override
			public void onHeadsetConnected() {
				// Toast.makeText(MainActivity.this,
				// "Audio e Voz Conectado", Toast.LENGTH_SHORT).show();
				String msg = mBlue.startVoiceBluetoothRecognition() ? "iniciado voice recognition"
						: "nao foi possivel inicializar voice recognitio";
				Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT)
						.show();
				//$NON-NLS-1$
				Log.d(TAG, "\nonFinish fail to connect to headset audio");

			}
		};
		mBlue = new MeuBluetooth(this, mListerner);
		mBlue.start();
		inits();

	}

	@Override
	public void onWindowAttributesChanged(LayoutParams params) {
		// TODO Auto-generated method stub
		super.onWindowAttributesChanged(params);
	}

	@Override
	public void onBackPressed() {
		try {
			mBlue.stop();
			mSpeechRecognizer.stopListening();
			mSpeechRecognizer.destroy();
			mtts.stop();
			mtts.shutdown();
		} finally {
			super.onBackPressed();
		}
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBeginningOfSpeech() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRmsChanged(float rmsdB) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEndOfSpeech() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(int error) {
		String errorMsg = "error";
		switch (error) {
		case SpeechRecognizer.ERROR_AUDIO:
			errorMsg += " audio";
			break;
		case SpeechRecognizer.ERROR_CLIENT:
			errorMsg += " client";
			break;
		case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
			errorMsg += " permission";
			break;
		case SpeechRecognizer.ERROR_NETWORK:

			errorMsg += " network";
			break;
		case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
			errorMsg += " network timeout";
			break;
		case SpeechRecognizer.ERROR_NO_MATCH:
			errorMsg += " no match";
			break;
		case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
			errorMsg += " busy";
			break;
		case SpeechRecognizer.ERROR_SERVER:
			errorMsg += " server";
			break;

		case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
			errorMsg += " speech timeout";
			break;

		default:
			// will NOT execute because of the line preceding the switch.
			break;
		}
		Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();

	}

	@Override
	public void onResults(Bundle results) {
		String str = "";
		ArrayList<String> data = new ArrayList<String>();
		if (results != null) {
			data = results
					.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		}

		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.size(); i++) {
			buf.append(data.get(i));
		}
		str += buf.toString();
		str = str.toLowerCase(Locale.getDefault());
		int yes = 0;
		for (String r : str.split("")) {
			yes += "yes".toLowerCase(Locale.getDefault()).indexOf(r) + 1;
		}

		int no = 0;
		for (String r : str.split("")) {
			no += "no".toLowerCase(Locale.getDefault()).indexOf(r) + 1;
		}
		System.out.println("YES: " + yes);
		System.out.println("NO: " + no);
		mBlue.stopVoiceBluetoothRecognition();
		if (yes > 0) {
			if (yes > no) {
				say("yes");
			} else {
				say("no");
			}
		} else if (no > 0) {
			say("no");
		} else {
			say("not found");
		}

	}

	@Override
	public void onPartialResults(Bundle partialResults) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		// TODO Auto-generated method stub

	}

	public void inits() {
		if (SpeechRecognizer.isRecognitionAvailable(this)) {

			mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

			mSpeechRecognizer.setRecognitionListener(this);
			Intent intent;
			intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
					"org.embeddedlabs.alertdrive.view");

			intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

			intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);

			mSpeechRecognizer.startListening(intent);
		}
		mtts = new TextToSpeech(this, this);
	}

	public final void say(final String text) {
		final HashMap<String, String> params = new HashMap<String, String>();
		params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "1");
		params.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
				String.valueOf(AudioManager.STREAM_MUSIC));
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "meuid");
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				mtts.speak(text, TextToSpeech.QUEUE_FLUSH, params);

			}
		}, 3000);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.speech.tts.TextToSpeech.OnInitListener#onInit(int)
	 */
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			Locale mlocale = Locale.getDefault();
			int result = mtts.setLanguage(mlocale);

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				System.out.println("ERR TTS");
			}

		}

	}
}
