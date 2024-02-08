package android.example.awcvv4;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StartJourneyActivity extends AppCompatActivity {

    VideoView videoView;
    //RelativeLayout mapLayout;
    //MapWC wcLoc;
    AnimatorXY animate;
    AnimatorXYS anime;
    TextView botAns, botAnsHeadingView, micInstructionView;
    String route;
    EditText jumpDist;
    ImageButton recordButton;
    ProgressBar progressBar;
    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    String recordFile, fileName, endName, lang, chatHeading, micInstructions;
    MediaPlayer mp=new MediaPlayer();
    MediaRecorder recorder;
    Float density;
    ImageView mapImage;
    Boolean image1Showing=true;
    Button btnCancel,btnProceed;

    AlertDialog dialog;


    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(socketEndpoint);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_journey);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        botAns=(TextView)findViewById(R.id.BotAnswer);
        botAns.setVisibility(View.INVISIBLE);
        videoView = findViewById(R.id.DestVideo);
        botAnsHeadingView=(TextView)findViewById(R.id.ChatHeading);
        micInstructionView=(TextView)findViewById(R.id.MicInstructions);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);
        //mapImage=(ImageView)findViewById(R.id.myImageView);
//        jumpDist=(EditText)findViewById(R.id.JumpDist);
//        density= getResources().getDisplayMetrics().density;
         WebView webview = (WebView) findViewById(R.id.webView);
        webview.loadUrl(getResources().getString(R.string.mapUrl));
   //     webview.loadUrl("https://hauntedhouse.webflow.io");
        WebSettings webset = webview.getSettings();
        webset.setSupportZoom(true);
        webset.setJavaScriptEnabled(true);
        webset.setBuiltInZoomControls(true);
        webset.setDisplayZoomControls(false);



        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lang = extras.getString("lang");
            chatHeading=extras.getString("chatHeading");
            micInstructions=extras.getString("micInstructions");
        }

        botAnsHeadingView.setText(chatHeading);
        micInstructionView.setText(micInstructions);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.iitm_walkthrough);
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // Start the video again when it is completed
                videoView.start();
            }
        });

        recordButton = (ImageButton) findViewById(R.id.RecordButton);
        //debugButton=(ImageButton)findViewById(R.id.DebugMap);

//        debugButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(image1Showing){
//                    mapImage.setImageResource(R.drawable.map_point_mapping);
//                    image1Showing=false;
//                }
//                else{
//                    mapImage.setImageResource(R.drawable.map5g);
//                    image1Showing=true;
//                }
//            }
//        });
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

       // wcLoc = (MapWC) findViewById(R.id.LiveLocView);
        //mapLayout = findViewById(R.id.MapView);
//        anime = new AnimatorXYS();
//        animate=new AnimatorXY();
        controlMoveWC(route);
//        moveWC();

    }

    //////////////////////////////////////MAP CONTROL////////////////////////////////////////
    public void controlMoveWC(String path){
        mSocket.connect();

        mSocket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = new JSONObject();
                try{
                    data.put("id","AWCV_Tablet");
//                    data.put("path","E");
                }
                catch (Exception e){
                    Log.e("Error","Input id JSON Error");
                }
                Log.e("SocketConnection status","Socket connection made");
                sendMessageToServer(data.toString());
                Toast.makeText(StartJourneyActivity.this, "Socket.IO connected", Toast.LENGTH_SHORT).show();
            }
        }));
        ///////////////////////////////////////////////////////////
        mSocket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            Log.e("SocketConnection status", "Socket connection failed");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(StartJourneyActivity.this, "Socket.IO connect error", Toast.LENGTH_SHORT).show();
                    // You may add additional error handling logic here, like displaying an error message or allowing the user to retry the connection.
                }
            });
        });
        ////////////////////////////////////////////////////////
        mSocket.on("route", args -> runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("response method call...");
                JSONObject data = (JSONObject) args[0];
                String message;
                try {
                    message = data.getString("message_route");

                    if(message.equalsIgnoreCase("ic")) {
                        videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.iitm_ic);
                        videoView.start();
                        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                // Start the video again when it is completed
                                videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.iitm_walkthrough);
                                videoView.start();
                                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mediaPlayer) {
                                        // Start the video again when it is completed
                                        videoView.start();
                                    }
                                });
                            }
                        });
                    }


                    if(message.contains("5g")) {
                        videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.fiveg_lab);
                        videoView.start();
                        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                // Start the video again when it is completed
                                videoView.start();
                                showConfirmationDialog();
                            }
                        });
                    }

                    if(message.contains("TakeFB")) {
                        showConfirmationDialog();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(StartJourneyActivity.this, "Json is not recieved", Toast.LENGTH_SHORT).show();
                }
            }

        }));

    }


    ///////////////////////////////////Recorder Functions////////////////////////////////////////
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
    ///////////////////////////SPEECH TO ACTIVITY////////////////////////////////////
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


    //////////////////////////////////PAGE EXIT///////////////////////////////////////
    @Override
    public void onBackPressed() {
        // Disconnect the Socket.IO connection when going back from the activity
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
            Log.e("socketio disconnect","Disconnected");
        }

        super.onBackPressed();
    }
    private void sendMessageToServer(String message){
        mSocket.emit("userConnected", message);
        Toast.makeText(StartJourneyActivity.this, message, Toast.LENGTH_SHORT).show();
    }


    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StartJourneyActivity.this);
        View dialogView = LayoutInflater.from(StartJourneyActivity.this).inflate(R.layout.video_alert, null);

        builder.setView(dialogView);
        dialog = builder.create();

        btnCancel = dialogView.findViewById(R.id.btnCancel);
        btnProceed = dialogView.findViewById(R.id.btnProceed);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(StartJourneyActivity.this, "Thank you for taking the tour. Hope to see you again! :)", Toast.LENGTH_SHORT).show();
            }
        });

        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // Navigate to the next page/activity
                Intent intent = new Intent(StartJourneyActivity.this, videoRecord.class);
                startActivity(intent);
                //finish(); // Optional: Finish the current activity if you don't want to go back to it
            }
        });

        dialog.show();
    }
}