package androidserver.marc.androidserver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;

import static androidserver.marc.androidserver.R.id.editPort;


public class MainActivity extends AppCompatActivity {

/*
    // #defines
    private static final int NET_CLRBITS = 0x10;
    private static final int NET_SETBITS = 0x11;
    private static final int NET_WRMSKBITS = 0x12;
    private static final int NET_KILLSRVR = 0x13;
    private static final int NET_INIT_PARAMS = 0x14;
    private static final int NET_SENDCANVAS = 0x15;
    private static final int NET_CLIENT_DC = 0x16;
    private static final int NET_TCPSTOP = 0x17;
    private static final int NET_BUFFER = 22528;
    private static final int NET_BUFFER_LARGE = 360448;
    // #defines
*/

//    public static String LEDDEMO_IPADDR = "192.168.1.3";
//    public static int LEDDEMO_PORT = 300;
    public static final String IPADDR = "ipaddr";
    public static final String UDPPORT = "port";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        final EditText IpAddr = (EditText) findViewById(R.id.editIpAddr);
//        final EditText Port = (EditText) findViewById(R.id.editPort);

    }

        /* Called when the user taps the Connect button */
    public void server_connect(View view) throws IOException {
        send_srvr srvr_snd = new send_srvr();
        Intent intent = new Intent(this, DemoCode.class);
        EditText IpAddr = (EditText) findViewById(R.id.editIpAddr);
        String ipaddr = IpAddr.getText().toString();
        EditText Port = (EditText) findViewById(editPort);
        int port = Integer.valueOf(Port.getText().toString());
        // Send the initial paramters to the server
        srvr_snd.send_initparams(ipaddr, port);
        intent.putExtra(IPADDR, ipaddr);
        intent.putExtra(UDPPORT, port);
        startActivity(intent);

    }
}

