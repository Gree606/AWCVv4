package android.example.awcvv4;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

public class PlayAudio {
    private String fileName;
    MediaPlayer mp;
    public void playAudio(String fName) throws IOException {
        mp=new MediaPlayer();
//    try {
//        if (mp.isPlaying()) {
//            mp.stop();
//            mp.release();
//            mp.setDataSource(fileName);
//            mp.prepare();
//        }
//    }
//    catch (IOException e){
//    Log.e("Mediaplayer error","No input file ");
//    }
        fileName=fName;
        while(!mp.isPlaying()){
            mp.setDataSource(fileName);
            mp.prepare();
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                        mp.stop();
                    }
                });
            }
    }
}
