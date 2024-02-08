package android.example.awcvv4;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TTSClass {
    String fileName;
    MediaPlayer mp;
    public void textToSpeech(String endName, String text, String lang,  Context context, final CompletionCallback callback) {
        fileName=context.getExternalCacheDir().getAbsolutePath()+endName;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                String speedText = "0.7";
                String url = TTSEndpoint;
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.get("application/json; charset=utf-8");
                String json = "{\"input\":\"" + text + "\",\"gender\":\"female\",\"lang\":\"" + lang + "\",\"alpha\":\"" + speedText + "\",\"segmentwise\":\"true\"}";
                Log.e("TTSInput",json);
                Log.e("filename in tts", fileName.toString());
                RequestBody body = RequestBody.create(json, JSON);
                okhttp3.Request request = new okhttp3.Request.Builder().url(url).post(body).build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            throw new IOException("Unexpected code " + response);
                        }
                        String responseData = response.body().string();
                        Log.e("Response of TTS",responseData);
                        JSONObject jObj = null;
                        try {
                            jObj = new JSONObject(responseData);
                            String resAudio = jObj.getString("audio");
                            Log.e("Response success", resAudio);
                            byte[] decodedBytes = Base64.getDecoder().decode(resAudio);
                            saveAudio(decodedBytes);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("ResponseFail", "Check if API is returning value");
                            /*runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Response Json error", Toast.LENGTH_LONG).show();
                                }});*/
                        }
                    }

                    @Override
                    public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                        Log.e("ResponseError", e.toString());
                        //Toast.makeText(context, "API error", Toast.LENGTH_LONG).show();
                    }

                    public void saveAudio(byte[] audioFile) {

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

                    }

                });
            }
        });
        thread.start();
        callback.onCompletion();
    }



}
