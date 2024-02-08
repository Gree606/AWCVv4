package android.example.awcvv4;

import android.Manifest;
import android.content.ContentValues;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignLanguage  extends AppCompatActivity {
    VideoView videoView;
    ExecutorService service;
    String responseVideo;
    private boolean isRecording = false;
    Recording recording = null;
    private ExoPlayer player;
    MediaItem mediaItem;
    Boolean playWhenReady = true;
    int currentItem = 0;
    long playbackPosition = 0L;
    String wheelchair_id="1";
//    String wheelchair_id=getResources().getString(R.string.wheelchairID);

    String filepath;
    String name;
    TextView botans;
    VideoCapture<Recorder> videoCapture = null;
    ImageButton capture, toggleFlash, flipCamera;
    Button start;
    PreviewView previewView;
    OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(1, TimeUnit.MINUTES).writeTimeout(1, TimeUnit.MINUTES) // write timeout
            .readTimeout(1, TimeUnit.MINUTES).build();
    int cameraFacing = CameraSelector.LENS_FACING_FRONT;
    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (ActivityCompat.checkSelfPermission(SignLanguage.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera(cameraFacing);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_language);
        previewView = findViewById(R.id.viewFinder);
        botans = findViewById(R.id.BotAnswer);
//        capture = findViewById(R.id.capture);
        start = findViewById(R.id.start);
        videoView = findViewById(R.id.DestVideo);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);



        toggleFlash = findViewById(R.id.toggleFlash);
        flipCamera = findViewById(R.id.flipCamera);
        start.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(SignLanguage.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(android.Manifest.permission.CAMERA);
            } else if (ActivityCompat.checkSelfPermission(SignLanguage.this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(android.Manifest.permission.RECORD_AUDIO);
            } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ActivityCompat.checkSelfPermission(SignLanguage.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                captureVideo();
            }
        });

        if (ActivityCompat.checkSelfPermission(SignLanguage.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(android.Manifest.permission.CAMERA);
        } else {
            startCamera(cameraFacing);
        }

        flipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
                    cameraFacing = CameraSelector.LENS_FACING_FRONT;
                } else {
                    cameraFacing = CameraSelector.LENS_FACING_BACK;
                }
                startCamera(cameraFacing);
            }
        });

        service = Executors.newSingleThreadExecutor();
       // initializePlayer();
    }


    private void runFileUploadInBackground(String name) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Code to be executed in the new thread
                try {


                    File file1 = new File(Environment.getExternalStorageDirectory() + "/Movies/CameraX-Video/" + name + ".mp4");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SignLanguage.this, name, Toast.LENGTH_SHORT).show();
                        }
                    });

                    //File file2 = new File("/path/to/file");

                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file", file1.getName(),
                                    RequestBody.create(MediaType.parse("application/octet-stream"), file1))
                            .addFormDataPart("wheelchair_id", null,
                                    RequestBody.create(MediaType.parse("json"), wheelchair_id.getBytes()))
                            .build();


                    Request request = new Request.Builder()
                            .url(uploaderEndPoint)
                            .method("POST", requestBody)
                            .build();

                    Response response = client.newCall(request).execute();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SignLanguage.this, response.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    if (response.isSuccessful()) {
                        // Handle the successful response here
                        String responseData = response.body().string();
                        JSONObject data = new JSONObject(responseData);
                        responseVideo = data.getString("file");
                        if (responseVideo.equals("goForward.mp4")) {
                            botans.setText("Moving Forward");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.goforward);
                                    videoView.start();

                                }
                            });
                            //initializePlayer();
                        } else if (responseVideo.equals("turnLeft.mp4")) {
                            botans.setText("Turning Left");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.turnleft);
                                    videoView.start();
                                }
                            });
                           // initializePlayer();
                        } else if (responseVideo.equals("takeRight.mp4")) {
                            botans.setText("Turning Right");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.takeright);
                                    videoView.start();

                                }
                            });

                           // initializePlayer();
                        } else if (responseVideo.equals("around.mp4")) {
                            botans.setText("Moving Around");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.around);
                                    videoView.start();

                                }
                            });

                           // initializePlayer();
                        }
                        // Use a Handler to update the UI if needed
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SignLanguage.this, responseVideo, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Handle error response here
                        String errorMessage = response.message();
                        // Use a Handler to update the UI if needed
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SignLanguage.this, "Errorrrrr", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle exception here
                    // Use a Handler to update the UI if needed
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            // Show error message to the user
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void captureVideo() {
        start.setText("Stop");
        Recording recording1 = recording;
        if (isRecording) {
            if (recording1 != null) {
                recording1.stop();
                recording = null;
            }
            start.setText("Start");
            isRecording = false;
            return;

        }
        name = "signlang";
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video");
//        Toast.makeText(this,contentValues.toString(), Toast.LENGTH_SHORT).show();
//        Log.e("storepath",contentValues.toString());

        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues).build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        recording = videoCapture.getOutput().prepareRecording(SignLanguage.this, options).withAudioEnabled().start(ContextCompat.getMainExecutor(SignLanguage.this), videoRecordEvent -> {
            if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                start.setEnabled(true);
                isRecording = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isRecording) {
                            stopRecording();
                        }
                    }
                }, 4000);
            } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                start.setEnabled(true);
                isRecording = false;
                if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                    filepath = ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri().toString();
                    Toast.makeText(this, filepath, Toast.LENGTH_SHORT).show();
                } else {
                    recording.close();
                    recording = null;
                    String err = "Error: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getError();
                    Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
                }
                start.setText("Start");



            }
        });
    }

    private void stopRecording() {
        if (recording != null) {
            recording.stop();
            recording = null;
            isRecording = false;
            start.setText("Start");
            runFileUploadInBackground(name);
        }
    }


    public void startCamera(int cameraFacing) {
        ListenableFuture<ProcessCameraProvider> processCameraProvider = ProcessCameraProvider.getInstance(SignLanguage.this);

        processCameraProvider.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = processCameraProvider.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                cameraProvider.unbindAll();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();

                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);

                toggleFlash.setOnClickListener(view -> toggleFlash(camera));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(SignLanguage.this));
    }

    private void toggleFlash(Camera camera) {
        if (camera.getCameraInfo().hasFlashUnit()) {
            if (camera.getCameraInfo().getTorchState().getValue() == 0) {
                camera.getCameraControl().enableTorch(true);
                toggleFlash.setImageResource(R.drawable.round_flash_off_24);
            } else {
                camera.getCameraControl().enableTorch(false);
                toggleFlash.setImageResource(R.drawable.round_flash_on_24);
            }
        } else {
            runOnUiThread(() -> Toast.makeText(SignLanguage.this, "Flash is not available currently", Toast.LENGTH_SHORT).show());
        }
    }

//    private void initializePlayer(){
//
//
//        player = new ExoPlayer.Builder(getApplicationContext()).build();
//        PlayerView playerView = findViewById(R.id.videoView); // Replace with your PlayerView ID
//        playerView.setPlayer(player);
//
//        ///////////////////////////////////////////////////////////////
////        TrustManager[] trustAllCerts = new TrustManager[] {
////                new X509TrustManager() {
////                    @Override
////                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
////                        // No implementation needed
////                    }
////
////                    @Override
////                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
////                        // No implementation needed
////                    }
////
////                    @Override
////                    public X509Certificate[] getAcceptedIssuers() {
////                        return new X509Certificate[0];
////                    }
////                }
////        };
////
////        SSLContext sslContext = null;
////        try {
////            sslContext = SSLContext.getInstance("TLS");
////        } catch (NoSuchAlgorithmException e) {
////            e.printStackTrace();
////        }
////        try {
////            sslContext.init(null, trustAllCerts, new SecureRandom());
////        } catch (KeyManagementException e) {
////            e.printStackTrace();
////        }
//
//
//        //////////////////////////////////////////////////////////////
//        TrustManager[] trustAllCerts = new TrustManager[] {
//                new X509TrustManager() {
//                    @Override
//                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                        // No implementation needed
//                    }
//
//                    @Override
//                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                        // No implementation needed
//                    }
//
//                    @Override
//                    public X509Certificate[] getAcceptedIssuers() {
//                        return new X509Certificate[0];
//                    }
//                }
//        };
//
//// Install the custom TrustManager
//        try {
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(null, trustAllCerts, new SecureRandom());
//            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
//            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
//        } catch (NoSuchAlgorithmException | KeyManagementException e) {
//            e.printStackTrace();
//        }
//
//// Proceed with your code
//        String mediaUrl =videoEndpoint;
//        mediaItem = MediaItem.fromUri(mediaUrl);
//        player.setMediaItem(mediaItem);
//        player.setPlayWhenReady(playWhenReady);
//        player.seekTo(currentItem, playbackPosition);
//        player.prepare();
//
//    }
//
//    public void releasePlayer(){
//
//        playWhenReady=player.getPlayWhenReady();
//        currentItem=player.getCurrentMediaItemIndex();
//        playbackPosition=player.getCurrentPosition();
//        player.release();
//        player=null;
//    }
//
//    @Override
//    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
//        switch (event) {
//            case ON_START:
//                if (Build.VERSION.SDK_INT > 23) {
//                    initializePlayer();
//                }
//                break;
//            case ON_RESUME:
//                if(Build.VERSION.SDK_INT>=23||player==null){
//                    initializePlayer();
//                }
//                break;
//            case ON_STOP:
//                if (Build.VERSION.SDK_INT>23){
//                    releasePlayer();
//                }
//                break;
//            case ON_PAUSE:
//                if (Build.VERSION.SDK_INT<=23){
//                    releasePlayer();
//                }
//                break;
//        }

//}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        service.shutdown();
    }
}