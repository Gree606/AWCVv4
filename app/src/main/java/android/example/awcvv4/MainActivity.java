package android.example.awcvv4;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mp;
    Intent welcomeIntent;
    WordAdapter itemsAdapter;
    TTSClass tts;
    String fileName;
    MediaRecorder mediaRecorder;
    int wheelchairId=1;
    String[] lang=null;
    ProgressBar progressBar;
    ArrayList<String> languages=null;
    Map<String, String> langDict=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
//        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.iitm_walkthrough;
//        welcomeVideoView=(VideoView) findViewById(R.id.IITMIntroVideo);
//        welcomeVideoView.setVideoPath(videoPath);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);
//        progressBar.setVisibility(View.VISIBLE);

        String welcomeMsg=getResources().getString(R.string.botWelcome);
        fileName=getApplicationContext().getExternalCacheDir().getAbsolutePath()+"/AWCVSpeechWelcome.3gp";
        String endName="/AWCVSpeechWelcome.3gp";
        clearAppCache(getApplicationContext());
//        createEmpty3GPFile(fileName);
        tts= new TTSClass();
        tts.textToSpeech(endName, welcomeMsg, "English", getApplicationContext(), new CompletionCallback() {
            @Override
            public void onCompletion() {
                mp = new MediaPlayer();
                try {
                    Log.e("Mediaplayer starting with file", fileName.toString());
                    mp.setDataSource(fileName);
                    mp.prepare();
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mp.stop();
                        }
                    });
                    mp.start();
                } catch (IOException e) {
                    Log.e("Mediaplayer error", "No input file");
                }
            }
        });

        ///////////////////////////To get the strings for the first page/////////////////////////
        Thread getLangThread=new Thread(new Runnable() {
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
                        .connectTimeout(5, TimeUnit.SECONDS) // connect timeout
                        .writeTimeout(5, TimeUnit.SECONDS) // write timeout
                        .readTimeout(5, TimeUnit.SECONDS)
                        .build();
                MediaType mediaType = MediaType.parse("application/json");
                String req="{\"wheelchair_id\":"+wheelchairId+"}";
                Log.e("Request", req);
                RequestBody body = RequestBody.create(req,mediaType);
                Request request = new Request.Builder()
                        .url(getResources().getString(R.string.micnprofileapi)+"/get_profile")
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
                        String langList = jObj.getString("available_languages");
                        Log.e("LangRespString",langList);
                        JSONObject jObjLang=new JSONObject(langList);
                        languages=new ArrayList<String>();
                        langDict = new HashMap<>();

                        // Iterate over the keys in the JSONObject
                        Iterator<String> keysIterator = jObjLang.keys();
                        while (keysIterator.hasNext()) {
                            String key = keysIterator.next();
                            languages.add(key);
                            Log.e("langKeys",key);
                            String value = jObjLang.getString(key);
                            langDict.put(key, value);
                        }
//                        ObjectMapper objectMapper = new ObjectMapper();
//                        try {
//                            lang = objectMapper.readValue(langList, String[].class);
//                            Log.e("Response success",lang[1]);
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    progressBar.setVisibility(View.INVISIBLE);
//                                }
//                            });
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        languages=new ArrayList<String>();
                        langDict = new HashMap<>();
                        Log.e("ResponseFail", "Check if API is returning value");
                        languages.add("English");
                        langDict.put("English","english");
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    languages=new ArrayList<String>();
                    langDict = new HashMap<>();
                    Log.e("ResponseError","Error fetching the list of languages");
                    languages.add("English");
                    langDict.put("English","english");
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
        getLangThread.start();

        while(languages==null){
            progressBar.setVisibility(View.VISIBLE);
        }
//                String[] lang={"English","Hindi","Tamil","ISL"};
//        ArrayList<String> languages=new ArrayList<String>();
//        for(int i=0;i<lang.length;i++){
//            Log.e("LangVals",lang[i]);
//            String[] oneLang=null;
//                languages.add(oneLang[0]);
//        }
        progressBar.setVisibility(View.INVISIBLE);
        itemsAdapter = new WordAdapter(this, R.layout.grid_item, languages);
        GridView gridView = (GridView) findViewById(R.id.langGrid);
        gridView.setAdapter(itemsAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View gridViewItem=view;
                String langChosen=(String) gridView.getItemAtPosition(position);
                //Add the request with the selected language here
                String[] valueList={"ಕನ್ನಡ","தமிழ்","हिन्दी","English","ગુજરાતી","తెలుగు","मराठी"};
                List<String> availableLang = Arrays.asList(valueList);
                if (availableLang.contains(langChosen))
                {
                        Log.e("Check Intent", "Calling Intent");
                        welcomeIntent = new Intent(getApplicationContext(), WelcomeVidActivity.class);
                        welcomeIntent.putExtra("lang", langDict.get(langChosen));
                        Log.e("langSend",langDict.get(langChosen));
                        startActivity(welcomeIntent);
                    }
                }
        });

    }

    public void clearAppCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteDir(cacheDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deleteDir(File file) {
        if (file != null && file.isDirectory()) {
            String[] children = file.list();
            for (String child : children) {
                boolean success = deleteDir(new File(file, child));
                if (!success) {
                    return false;
                }
            }
            return file.delete();
        } else if (file != null && file.isFile()) {
            return file.delete();
        } else {
            return false;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mp != null && mp.isPlaying()) {
            mp.stop();
        }
    }

//    private void createEmpty3GPFile(String filePath) {
//        mediaRecorder = new MediaRecorder();
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        mediaRecorder.setOutputFile(filePath); // Set the desired file path
//
//        try {
//            mediaRecorder.prepare();
//            mediaRecorder.start();
//            mediaRecorder.stop();
//            mediaRecorder.release();
//            mediaRecorder = null;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

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