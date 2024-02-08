package android.example.awcvv4;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class ManualControls extends AppCompatActivity {

    ImageButton up,down,left,right,start;
    Button rotate,square;
    Button stop;
    private Socket mSocket;
    {
        try {
//            mSocket = IO.socket(socketUrl);
            mSocket = IO.socket(socketUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_controls);

        up = (ImageButton) findViewById(R.id.forward);
        //down = (ImageButton) findViewById(R.id.backward);
        left = (ImageButton) findViewById(R.id.left);
        right = (ImageButton) findViewById(R.id.right);
        // start = (ImageButton) findViewById(R.id.start);
        stop =  findViewById(R.id.stop);
        rotate =  findViewById(R.id.rotate);
        square =  findViewById(R.id.square);

        mSocket.connect();
        mSocket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = new JSONObject();
                try{
                    data.put("id","AWCV_Tablet");
                }
                catch (Exception e){
                    Log.e("Error","Input id JSON Error");
                }
                Log.e("SocketConnection status","Socket connection made");
                Toast.makeText(ManualControls.this, "Socket.IO connected", Toast.LENGTH_SHORT).show();
            }
        }));
        mSocket.on(Socket.EVENT_CONNECT_ERROR,args -> runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ManualControls.this, "Socket Not Connected", Toast.LENGTH_SHORT).show();
            }
        }));

        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.emit("polygon","1");
                Toast.makeText(ManualControls.this, "Small Circle Sent", Toast.LENGTH_SHORT).show();
            }
        });
//        bigcircle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mSocket.emit("polygon","2");
//                Toast.makeText(MainActivity.this, "Big Circle Sent", Toast.LENGTH_SHORT).show();
//            }
//        });
        square.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.emit("polygon","2");
                Toast.makeText(ManualControls.this, "Square Sent", Toast.LENGTH_SHORT).show();
            }
        });
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.emit("polygon","8");
                Toast.makeText(ManualControls.this, "UP Sent", Toast.LENGTH_SHORT).show();
            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.emit("polygon","4");
                Toast.makeText(ManualControls.this, "Left Sent", Toast.LENGTH_SHORT).show();
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.emit("polygon","6");
                Toast.makeText(ManualControls.this, "Right Sent", Toast.LENGTH_SHORT).show();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.emit("polygon","5");
                Toast.makeText(ManualControls.this, "Stop Sent", Toast.LENGTH_SHORT).show();
            }
        });




    }
//    private void sendMessageToServer(String message){
//        mSocket.emit("polygon", message);
//        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
//    }
}