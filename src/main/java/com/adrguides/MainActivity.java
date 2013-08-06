package com.adrguides;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private static final int TTS_REQUEST_CODE = 332342;

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.shutdown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    public void onCheckTTSClicked(View view) {

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_REQUEST_CODE);

        TextView msg = (TextView) this.findViewById(R.id.messageText);
        msg.setText("checking");
    }

    public void onTalkClicked(View view) {
        String myText1 = "¿Has dormido bién?";
        String myText2 = "Eso espero, porque es hora de levantarse.";

        HashMap<String, String> ttsparams = new HashMap();
        ttsparams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "end of wakeup message ID");
        tts.speak(myText1, TextToSpeech.QUEUE_FLUSH, ttsparams);
        tts.speak(myText2, TextToSpeech.QUEUE_ADD, ttsparams);
    }

    public void onInstallTTSClicked(View view) {
        Intent installIntent = new Intent();
        installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        startActivity(installIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_REQUEST_CODE) {
            TextView msg = (TextView) this.findViewById(R.id.messageText);
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                tts = new TextToSpeech(this, this);


                msg.setText("loading");
            } else {

//                // missing data, install it
//                Intent installIntent = new Intent();
//                installIntent.setAction(
//                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
//                startActivity(installIntent);
                msg.setText("error");
            }
        }
    }

    @Override
    public void onInit(int i) {
        TextView msg = (TextView) this.findViewById(R.id.messageText);
        msg.setText("success");

//        tts.setLanguage(Locale.US);
//        tts.isLanguageAvailable(new Locale("spa", "ESP"));

//        tts.setOnUtteranceProgressListener(new UtteranceListener());

        List<TextToSpeech.EngineInfo> engines = tts.getEngines();
        Log.d("com.adrguides.MainActivity", "TTS Engines >>");
        for (TextToSpeech.EngineInfo e : engines) {
            Log.d("com.adrguides.MainActivity", e.toString());
        }
        Log.d("com.adrguides.MainActivity", "<< TTS Engines");

        tts.setLanguage(new Locale("spa", "ESP"));
        if (tts.isSpeaking()) {
            msg.setText("speaking");
        }
    }

//    private class UtteranceListener extends UtteranceProgressListener {
//        @Override
//        public void onStart(String s) {
//
//        }
//        @Override
//        public void onDone(String s) {
//
//        }
//        @Override
//        public void onError(String s) {
//
//        }
//    }
}
