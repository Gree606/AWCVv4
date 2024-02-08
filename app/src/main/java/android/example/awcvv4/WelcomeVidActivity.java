package android.example.awcvv4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WelcomeVidActivity extends AppCompatActivity {

    VideoView videoView;
    WordAdapter itemsAdapter;
    ImageButton recordButton;
    String fileName;
    String endName;
    String recordFile;
    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    MediaRecorder recorder;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    String textFromSpeech;
    TextView userQues;
    TextView botAns;
    TTSClass tts;
    MediaPlayer mp=new MediaPlayer();
    String route;
    String lang;
    int wheelchairId=1;
    String[] locations= null;
    ProgressBar progressBar;
    String destHeading, chatHeading, micInstructions;
    TextView destHeadingView, chatHeadingView, micInstructionsView;
    Button sign,manualControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_vid);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lang = extras.getString("lang");
        }
        Log.e("language in welcome video page", lang);

        recordButton = (ImageButton) findViewById(R.id.RecordButton);
        videoView = findViewById(R.id.IntroVideo);
        botAns = (TextView) findViewById(R.id.BotAnswer);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);
        destHeadingView=(TextView)findViewById(R.id.Heading1);
        chatHeadingView=(TextView)findViewById(R.id.ChatHeading);
        micInstructionsView=(TextView)findViewById(R.id.MicInstructions);
        sign = (Button)findViewById(R.id.signlanguage);
        manualControl=(Button)findViewById(R.id.manualControls);

        manualControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent controller=new Intent(WelcomeVidActivity.this,ManualControls.class);
                startActivity(controller);
            }
        });

        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signLanguageIntent = new Intent(getApplicationContext(),SignLanguage.class);
                startActivity(signLanguageIntent);
            }
        });

        tts = new TTSClass();
        botAns.setVisibility(View.INVISIBLE);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.awcv_intro);
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // Start the video again when it is completed
                videoView.start();
            }
        });


        ////////////////////////////Thread to get the page contents//////////////////////////////////////////
        Thread getDestinations=new Thread(new Runnable() {
            @Override
            public void run() {
                String responseText;
                TrustManager[] trustAllCerts = new TrustManager[] {
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                                // No implementation needed
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                                // No implementation needed
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                };

                SSLContext sslContext = null;
                try {
                    sslContext = SSLContext.getInstance("TLS");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                try {
                    sslContext.init(null, trustAllCerts, new SecureRandom());
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                }

                OkHttpClient client = new OkHttpClient.Builder()
                        .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                        .hostnameVerifier((hostname, session) -> true)
                        .connectTimeout(4, TimeUnit.SECONDS) // connect timeout
                        .writeTimeout(4, TimeUnit.SECONDS) // write timeout
                        .readTimeout(4, TimeUnit.SECONDS)
                        .build();
                MediaType mediaType = MediaType.parse("application/json");
                String req="{\"wheelchair_id\":"+wheelchairId+",\"language_selected\":\""+lang+"\"}";
                Log.e("Request", req);
                RequestBody body = RequestBody.create(req,mediaType);
                Request request = new Request.Builder()
                        .url(getResources().getString(R.string.micnprofileapi)+"/get_destinations")
                        .method("POST", body)
                        .addHeader("Content-Type", "application/json")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String jsonData = response.body().string();
//                    Log.e("Response",jsonData);
                    JSONObject jObj = null;
                    try {
                        jObj = new JSONObject(jsonData);
                        String destList = jObj.getString("available_destinations");
                        destHeading=jObj.getString("dest_heading");
                        chatHeading=jObj.getString("chat_heading");
                        micInstructions=jObj.getString("instruction_mic");
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            locations = objectMapper.readValue(destList, String[].class);
                            Log.e("Response success",locations[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("ResponseFail", "Check if API is returning value");
                        locations= new String[]{"Default"};
                        destHeading="Available Destinations";
                        chatHeading="Answer";
                        micInstructions="Push the mic to ask a question or give destination";

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("ReponseError","Error fetching the list of destinations");
                    locations= new String[]{"Default"};
                    destHeading="Available Destinations";
                    chatHeading="Answer";
                    micInstructions="Push the mic to ask a question or give destination";
                }
            }
        });
        getDestinations.start();

        while (locations == null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        progressBar.setVisibility(View.INVISIBLE);
                //Get the route names in the desired language
//        String[] locations = {"Start trial route", "Incubation Cell", "E-Block courtiyard", "5G Test Bed", "CBEEV", "BuildLab", "Cafeteria", "Restroom"};
        destHeadingView.setText(destHeading);
        chatHeadingView.setText(chatHeading);
        micInstructionsView.setText(micInstructions);

        ArrayList<String> loc = new ArrayList<String>();
        for (int i = 0; i < locations.length; i++) {
//            TextView langOpn= new TextView(this);
//            langOpn.setText(lang[i]);
            loc.add(locations[i]);
        }
        itemsAdapter = new WordAdapter(this, R.layout.grid_item, loc);
        GridView gridView = (GridView) findViewById(R.id.locGrid);
        gridView.setAdapter(itemsAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View gridViewItem = view;
                String currentWord = (String) gridView.getItemAtPosition(position);
                String[] valueList={"5G","5.जी","5ஜி","5.ಜಿ","Default","५जी","5జి","5જી","IC","आई.सी","ஐ.சி"};
                List<String> availableDest = Arrays.asList(valueList);
                if (availableDest.contains(currentWord))
                {
//                    route=routeSel.getText().toString();
                    Intent startJourneyIntent = new Intent(getApplicationContext(), StartJourneyActivity.class);
                    startJourneyIntent.putExtra("lang",lang);
                    startJourneyIntent.putExtra("chatHeading",chatHeading);
                    startJourneyIntent.putExtra("micInstructions",micInstructions);
                    Thread thread=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            OkHttpClient client = new OkHttpClient().newBuilder()
                                    .build();
                            MediaType mediaType = MediaType.parse("application/json");
                            if(currentWord.contains("5")) {
                                RequestBody body = RequestBody.create("{\"destination\":\"5g\"}", mediaType);
                                Request request = new Request.Builder()
                                        .url(getResources().getString(R.string.wheelchair1url))
                                        .method("POST", body)
                                        .addHeader("Content-Type", "application/json")
                                        .build();
                                try {
                                    Response response = client.newCall(request).execute();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            if(currentWord.contains("IC")) {
                                RequestBody body = RequestBody.create("{\"destination\":\"IC\"}", mediaType);
                                Request request = new Request.Builder()
                                        .url(getResources().getString(R.string.wheelchair1url))
                                        .method("POST", body)
                                        .addHeader("Content-Type", "application/json")
                                        .build();
                                try {
                                    Response response = client.newCall(request).execute();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    thread.start();


                    startActivity(startJourneyIntent);
                }

            }
        });

        recordButton = (ImageButton) findViewById(R.id.RecordButton);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        recordFile = getApplicationContext().getExternalCacheDir().getAbsolutePath()+"/AWCVUserSpeech";
        fileName =getApplicationContext().getExternalCacheDir().getAbsolutePath()+ "/AWCVNoSpeech.3gp";
        endName="/AWCVNoSpeech.3gp";


        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        videoView.pause();
                        if(mp.isPlaying()) {mp.stop();
                        mp.release();
                        mp=new MediaPlayer();}
                        recorder = new MediaRecorder();
                        startRecording();
                        recordButton.setBackgroundResource(R.drawable.recordicon3);
                        recordButton.setScaleX(1.5f);
                        recordButton.setScaleY(1.5f);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // touch move code
                        break;

                    case MotionEvent.ACTION_UP:
                        stopRecording();
                        recordButton.setBackgroundResource(R.drawable.recordicon2);
                        recordButton.setScaleX(1);
                        recordButton.setScaleY(1);
//                            PlayAudio pA=new PlayAudio();
//                        try {
//                            pA.playAudio(recordFile);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                        botAns.setVisibility(View.VISIBLE);
                        sttToActivity(recordFile, botAns);
                        //Implementing Speech to text
                        break;
                }
                return true;
            }
        });

    }

    ////////////////////Recorder Functions///////////////////////////////
    public void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recordFile);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    public void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    //////////////////////////STT Function//////////////////////////

    public void sttToActivity(String fName, TextView view) {
        progressBar.setVisibility(View.VISIBLE);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                TrustManager[] trustAllCerts = new TrustManager[] {
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                                // No implementation needed
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                                // No implementation needed
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                };

                SSLContext sslContext = null;
                try {
                    sslContext = SSLContext.getInstance("TLS");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                try {
                    sslContext.init(null, trustAllCerts, new SecureRandom());
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                }

                OkHttpClient client = new OkHttpClient.Builder()
                        .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                        .hostnameVerifier((hostname, session) -> true)
                        .connectTimeout(2, TimeUnit.MINUTES) // connect timeout
                        .writeTimeout(2, TimeUnit.MINUTES) // write timeout
                        .readTimeout(2, TimeUnit.MINUTES)
                        .build();
                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("file",recordFile,
                                RequestBody.create(new File(recordFile),MediaType.parse("application/octet-stream")))
                        .addFormDataPart("wheelchair_id", null,
                                RequestBody.create(getResources().getString(R.string.wheelchairID).getBytes(),MediaType.parse("application/json")))
                        .addFormDataPart("source_language", null,
                                RequestBody.create(lang.getBytes(),MediaType.parse("application/json")))
                        .addFormDataPart("target_language", null,
                                RequestBody.create(lang.getBytes(),MediaType.parse("application/json")))
                        .build();
                Request request = new Request.Builder()
                        .url(getResources().getString(R.string.micnprofileapi)+"/api")
                        .method("POST", body)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String jsonData = response.body().string();
                    Log.e("Response",jsonData);
                    JSONObject jObj = null;
                    try {
                        jObj = new JSONObject(jsonData);
                        String resAudio = jObj.getString("audio");
                        String caption=jObj.getString("caption");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.INVISIBLE);
                                view.setText(caption);
                            }
                        });
                        Log.e("Response success", resAudio);
                        byte[] decodedBytes = Base64.getDecoder().decode(resAudio);
                        saveAudio(decodedBytes);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("ResponseFail", "Check if API is returning value");
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              progressBar.setVisibility(View.INVISIBLE);
                                              view.setText("Failed to get response");
                                          }
                                      });
                            /*runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Response Json error", Toast.LENGTH_LONG).show();
                                }});*/
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                            view.setText("Failed to get response");
                        }
                    });
                }

            }
        });
        thread.start();


    }

    public void saveAudio(byte[] audioFile) {

        fileName=getApplicationContext().getExternalCacheDir().getAbsolutePath()+"/ChatBotAnswer";
        OutputStream os;
        try {
            os = new FileOutputStream(new File(fileName));
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream outFile = new DataOutputStream(bos);
            outFile.write(audioFile);
            outFile.flush();
            outFile.close();
        } catch (IOException e) {
            //Toast.makeText(context, "Add audio file", Toast.LENGTH_LONG).show();
            Log.e("TAG", "failed to get audio output");
        }
        playAudio(fileName);
    }


    public void playAudio(String fName){
        try{
            fileName=fName;
            if(mp.isPlaying()) {mp.stop();}
            mp.setDataSource(fileName);
            Log.e("save path", fileName);
            mp.prepare();
            while(!mp.isPlaying()){
                mp.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mp.stop();
                        mp.release();
                        mp=new MediaPlayer();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("TAG", "prepare() failed for output audio");
        }
    }




    @Override
    protected void onPause() {
        super.onPause();
        if (mp != null && mp.isPlaying()) {
            mp.stop();
        }
    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (mp != null) {
//            mp.release();
//            mp = null;
//        }
//    }

    @Override
    public void onDestroy() {
        // Disconnect the Socket.IO connection when going back from the activity
        super.onDestroy();
        if (mp != null && mp.isPlaying()) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }
}