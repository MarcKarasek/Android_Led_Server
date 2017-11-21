package androidserver.marc.androidserver;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;

public class DemoCode extends AppCompatActivity {

    public static int LEDDEMO_PORT = 300;

    String ipaddr;
    int port;
    int delay_ms = 30;

    private Thread t1;
    private Thread t2;
    private Thread t3;
    private Thread t4;
    private Thread t5;
    private Thread t6;
    private Thread t7;
    private Thread t8;

    private static final int MSG_DEMO_STOP = 0x0001;
    private static final int MSG_DEMO_STARTED = 0x0002;
    private static final int MSG_DEMO_STOPPED = 0x0003;

    private int columns;

    private Handler demo_handler;
    private Handler demo_ui_handler;

    private UIThreadHandler demo_ui_thandler;
    private DemoThreadHandler demo_thanlder;

    @Override
    public void onBackPressed() {
        demo_ui_thandler.quitSafely();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_code);

        Intent intent = getIntent();
        ipaddr = intent.getStringExtra(MainActivity.IPADDR);
        port = intent.getIntExtra(MainActivity.UDPPORT, LEDDEMO_PORT);

        TextView ip_addr = (TextView) findViewById(R.id.ipaddr);
        ip_addr.setText(ipaddr);

        TextView port_numb = (TextView) findViewById(R.id.port);
        port_numb.setText(String.valueOf(port));

        demo_ui_thandler = new UIThreadHandler("DEMOUI");
        demo_ui_thandler.start();
        demo_ui_thandler.prepareHandler();
    }

    /* Called when the user taps the Disconnect button */
    public void server_disconnect(View view) throws IOException {
        send_srvr srvr_snd = new send_srvr();
        // Send the Connect Stop
        srvr_snd.tcpstop(ipaddr, port);
        // Send the Disconnect Command to the Server.
        srvr_snd.send_disconnect(ipaddr, port);
    }

    /* Called when the user taps the Kill Srvr button */
    public void server_kill(View view) throws IOException {
        send_srvr srvr_snd = new send_srvr();
        // Send the Connect Stop
        srvr_snd.tcpstop(ipaddr, port);
        // send the Serer kill
        srvr_snd.srvr_kill(ipaddr, port);
    }

    // Send the stop message to the demo_handler Handler.
    public void demo_stop(View view){
        demo_handler.sendEmptyMessage(MSG_DEMO_STOP);
    }

    public void on_start(View view){
        netparams default_params = new netparams();
        default_params.init_params();

        columns = default_params.chain * 32;

        RadioGroup DemoSelected;
        DemoSelected = (RadioGroup) findViewById(R.id.RGroup1);
        int demo_checked;

        demo_checked = DemoSelected.getCheckedRadioButtonId();

        switch (demo_checked){
            case (R.id.select_demo1): // Ant
                t1 = new Thread(new Ant(default_params.rows, columns, ipaddr, port, delay_ms));
                t1.start();
                break;
            case (R.id.select_demo2): //Color Pulse Generator
                t2 = new Thread(new ColorPulseGenerator(ipaddr, port));
                t2.start();
                break;
            case (R.id.select_demo3): // Game of Life
                t3 = new Thread(new GameLife(default_params.rows, columns, ipaddr, port, delay_ms, true));
                t3.start();
                break;
            case (R.id.select_demo4): // Gray Scale Block
                t4 = new Thread(new GrayScaleBlock(default_params.rows, columns, ipaddr, port, delay_ms));
                t4.start();
                break;
            case (R.id.select_demo5): // Rotating Block
                t5 = new Thread(new Rotating_Block(default_params.rows, columns, ipaddr, port));
                t5.start();
                break;
            case (R.id.select_demo6): // Sandpile
                t6 = new Thread(new Sandpile(default_params.rows, columns, ipaddr, port, delay_ms));
                t6.start();
                break;
            case (R.id.select_demo7): // Simple Square
                t7 = new Thread(new SimpleSquare(default_params.rows, columns, ipaddr, port));
                t7.start();
                break;
            case (R.id.select_demo8): // Volume Bars
                t8 = new Thread(new VolumeBars(default_params.rows, columns, ipaddr, port, delay_ms));
                t8.start();
                break;
            default :
                // should not happen.. error window?
                break;
        }
    }

    // Thread Handler Classes used to communicate between UI and Demo Threads

    // extending handler class to handle messages sent to Demo thread
    private class DemoThreadHandler extends HandlerThread {

        // Public variable to set/check on while() loop in demo code
        private boolean demoRunning = true;

        void demoRun(){
            demoRunning = true;
        }

        void demoStop(){
            demoRunning = false;
        }

        boolean getRunning(){
            return demoRunning;
        }

        synchronized void waitForready() {
            demo_handler = new Handler(getLooper()) {
                @Override
                public void handleMessage(Message msg) {

                    switch (msg.what) {
                        case MSG_DEMO_STOP:
                            demoStop();
                            demo_ui_handler.sendEmptyMessage(MSG_DEMO_STOPPED);
                            break;
                        default:
                            super.handleMessage(msg);
                    }
                }
            };
        }

        DemoThreadHandler(String name) {
            super(name);
        }

    }

    // extending handler class to handle messages sent to UI thread from Demo Threads
    private class UIThreadHandler extends HandlerThread {

        UIThreadHandler(String name) {
            super(name);
        }

        public void prepareHandler() {
            demo_ui_handler = new Handler(getLooper()) {
                @Override
                public void handleMessage(Message msg) {

                    switch (msg.what) {
                        case MSG_DEMO_STOPPED:
                            Toast.makeText(DemoCode.this, "Demo has Stopped!",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case  MSG_DEMO_STARTED:
                            Toast.makeText(DemoCode.this, "Demo has Started!",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            super.handleMessage(msg);
                    }
                }
            };
        }
    }

    //^^^^^^^^^Demo Classes Start here
    //**********************************
    //   ANT Demo
    //**********************************
    private class Ant implements Runnable  {

        private int height;
        private int width;
        private String ipaddr_;
        private int port_;
        private int numColors_ = 4;
        private int[][] values_;
        private int antX_;
        private int antY_;
        private int antDir_; // 0 right, 1 up, 2 left, 3 down
        private  int delay_ms_ = 500;

        private Framebuffer Frame_Ant = new Framebuffer();

        Ant (int rows, int columns, String ipaddr, int port, int delay) {

            height = rows;
            width = columns;
            port_= port;
            ipaddr_ = ipaddr;
            values_ = new int[width][height];
            delay_ms_ = delay;

            demo_thanlder = new DemoThreadHandler("ANT");
            demo_thanlder.start();
            demo_thanlder.waitForready();

            demo_ui_handler.sendEmptyMessage(MSG_DEMO_STARTED);
        }



        // Langton's ant
        public void run() {

            antX_ = width/2;
            antY_ = height/2-3;
            antDir_ = 0;
            for (int x=0; x<width; ++x) {
                for (int y=0; y<height; ++y) {
                    values_[x][y] = 0;
                    updatePixel(x, y);
                }
            }

            // Set the flag to turn on the demo
            demo_thanlder.demoRun();

            while(demo_thanlder.getRunning()) {
                // LLRR
                switch (values_[antX_][antY_]) {
                    case 0:
                    case 1:
                        antDir_ = (antDir_+1+4) % 4;
                        break;
                    case 2:
                    case 3:
                        antDir_ = (antDir_-1+4) % 4;
                        break;
                }

                values_[antX_][antY_] = (values_[antX_][antY_] + 1) % numColors_;
                int oldX = antX_;
                int oldY = antY_;
                switch (antDir_) {
                    case 0:
                        antX_++;
                        break;
                    case 1:
                        antY_++;
                        break;
                    case 2:
                        antX_--;
                        break;
                    case 3:
                        antY_--;
                        break;
                }
                updatePixel(oldX, oldY);
                if (antX_ < 0 || antX_ >= width || antY_ < 0 || antY_ >= height)
                    return;
                updatePixel(antX_, antY_);
                try {
                    Thread.sleep(delay_ms_ * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Frame_Ant.DumpToWeb(ipaddr_, port_);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Frame_Ant.CloseTCP(ipaddr_, port_);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Exit the HandlerThread for this Demo
            demo_thanlder.quitSafely();

        }

        private void updatePixel(int x, int y) {
            switch (values_[x][y]) {
                case 0:
                    Frame_Ant.SetPixel(x, y, 200, 0, 0);
                    break;
                case 1:
                    Frame_Ant.SetPixel(x, y, 0, 200, 0);
                    break;
                case 2:
                    Frame_Ant.SetPixel(x, y, 0, 0, 200);
                    break;
                case 3:
                    Frame_Ant.SetPixel(x, y, 150, 100, 0);
                    break;
            }
            if (x == antX_ && y == antY_)
                Frame_Ant.SetPixel(x, y, 0, 0, 0);
        }
    }


    //**********************************
    //   Color Pulse Generator Demo
    //**********************************
    private class ColorPulseGenerator implements Runnable {

        private String ipaddr_;
        private int port_;

        private Framebuffer Frame_ColorPulseGenerator = new Framebuffer();

        ColorPulseGenerator (String ipaddr, int port) {
            port_= port;
            ipaddr_ = ipaddr;

            demo_thanlder = new DemoThreadHandler("COLORPULSE");
            demo_thanlder.start();
            demo_thanlder.waitForready();

            demo_ui_handler.sendEmptyMessage(MSG_DEMO_STARTED);
        }

        public void run() {
            int continuum = 0;

            // Set the flag to turn on the demo
            demo_thanlder.demoRun();

            while(demo_thanlder.getRunning()) {
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continuum += 1;
                continuum %= 3 * 255;
                int r = 0, g = 0, b = 0;
                if (continuum <= 255) {
                    b = 255 - continuum;
                    r = continuum;
                } else if (continuum > 255 && continuum <= 511) {
                    int c = continuum - 256;
                    r = 255 - c;
                    g = c;
                } else {
                    int c = continuum - 512;
                    g = 255 - c;
                    b = c;
                }
                Frame_ColorPulseGenerator.Fill((byte)r, (byte)g, (byte)b);

                try {
                    Frame_ColorPulseGenerator.DumpToWeb(ipaddr_, port_);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Frame_ColorPulseGenerator.CloseTCP(ipaddr_, port_);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Exit the HandlerThread for this Demo
            demo_thanlder.quitSafely();

        }
    }

    //**********************************
    //   Game of Life Demo
    //**********************************
    private class GameLife implements Runnable {

        private int height_;
        private int width_;
        private String ipaddr_;
        private int port_;
        private int numColors_ = 4;
        private int[][] values_;
        private int[][] newValues_;
        private int antX_;
        private int antY_;
        private int antDir_; // 0 right, 1 up, 2 left, 3 down
        private  int delay_ms;
        private boolean torus_;
        private int r_;
        private int g_;
        private int b_;

        private Random rnd = new Random();


        private Framebuffer Frame_GameLife = new Framebuffer();

        GameLife (int rows, int columns, String ipaddr, int port, int delay, boolean torus) {

            height_ = rows;
            width_ = columns;
            port_ = port;
            ipaddr_ = ipaddr;
            values_ = new int[width_][height_];
            if (delay == 0)
                delay_ms = delay;
            else
                delay_ms = 500;
            torus_ = torus;

            // Init values randomly
            rnd.setSeed(System.currentTimeMillis());
            for (int x = 0; x < width_; ++x) {
                for (int y = 0; y < height_; ++y) {
                    values_[x][y] = rnd.nextInt() % 2;
                }
            }
            r_ = rnd.nextInt() % 255;
            g_ = rnd.nextInt() % 255;
            b_ = rnd.nextInt() % 255;

            if (r_ < 150 && g_ < 150 && b_ < 150) {
                int c = rnd.nextInt() % 3;
                switch (c) {
                    case 0:
                        r_ = 200;
                        break;
                    case 1:
                        g_ = 200;
                        break;
                    case 2:
                        b_ = 200;
                        break;
                }
            }

            demo_thanlder = new DemoThreadHandler("GAMELIFE");
            demo_thanlder.start();
            demo_thanlder.waitForready();

            demo_ui_handler.sendEmptyMessage(MSG_DEMO_STARTED);
        }

        public void run() {

            // Set the flag to turn on the demo
            demo_thanlder.demoRun();

            while(demo_thanlder.getRunning()) {
                updateValues();

                for (int x=0; x<width_; ++x) {
                    for (int y=0; y<height_; ++y) {
                        if (values_[x][y] != 0)
                            Frame_GameLife.SetPixel(x, y, r_, g_, b_);
                        else
                            Frame_GameLife.SetPixel(x, y, 0, 0, 0);
                    }
                }
                try {
                    Thread.sleep(delay_ms * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Frame_GameLife.DumpToWeb(ipaddr_, port_);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Frame_GameLife.CloseTCP(ipaddr_, port_);
            } catch (IOException e) {
                e.printStackTrace();
            }

            demo_thanlder.quitSafely();
        }


        private int numAliveNeighbours(int x, int y) {
            int num=0;
            if (torus_) {
                // Edges are connected (torus)
                num += values_[(x-1+width_)%width_][(y-1+height_)%height_];
                num += values_[(x-1+width_)%width_][y                    ];
                num += values_[(x-1+width_)%width_][(y+1        )%height_];
                num += values_[(x+1       )%width_][(y-1+height_)%height_];
                num += values_[(x+1       )%width_][y                    ];
                num += values_[(x+1       )%width_][(y+1        )%height_];
                num += values_[x                  ][(y-1+height_)%height_];
                num += values_[x                  ][(y+1        )%height_];
            }
            else {
                // Edges are not connected (no torus)
                if (x>0) {
                    if (y>0)
                        num += values_[x-1][y-1];
                    if (y<height_-1)
                        num += values_[x-1][y+1];
                    num += values_[x-1][y];
                }
                if (x<width_-1) {
                    if (y>0)
                        num += values_[x+1][y-1];
                    if (y<31)
                        num += values_[x+1][y+1];
                    num += values_[x+1][y];
                }
                if (y>0)
                    num += values_[x][y-1];
                if (y<height_-1)
                    num += values_[x][y+1];
            }
            return num;
        }

        private void updateValues() {
            // Copy values to newValues
            for (int x=0; x<width_; ++x) {
                System.arraycopy(values_[x], 0, newValues_[x], 0, height_);
            }
            // update newValues based on values
            for (int x=0; x<width_; ++x) {
                for (int y=0; y<height_; ++y) {
                    int num = numAliveNeighbours(x,y);
                    if (values_[x][y] != 0) {
                        // cell is alive
                        if (num < 2 || num > 3)
                            newValues_[x][y] = 0;
                    }
                    else {
                        // cell is dead
                        if (num == 3)
                            newValues_[x][y] = 1;
                    }
                }
            }
            // copy newValues to values
            for (int x=0; x<width_; ++x) {
                System.arraycopy(newValues_[x], 0, values_[x], 0, height_);
            }
        }
    }

    //**********************************
    //   Gray Scale Block Demo
    //**********************************
    private class GrayScaleBlock implements Runnable {

        private int height;
        private int width;
        private String ipaddr_;
        private int port_;

        private Framebuffer Frame_GrayScaleBlock = new Framebuffer();

        GrayScaleBlock (int rows, int columns, String ipaddr, int port, int delay) {

            height = rows;
            width = columns;
            port_= port;
            ipaddr_ = ipaddr;

            demo_thanlder = new DemoThreadHandler("GRAYSCALE");
            demo_thanlder.start();
            demo_thanlder.waitForready();

            demo_ui_handler.sendEmptyMessage(MSG_DEMO_STARTED);
        }

        public void run() {
            int sub_blocks = 16;
            int x_step = Math.max(1, width / sub_blocks);
            int y_step = Math.max(1, height / sub_blocks);
            byte count = 0;

            // Set the flag to turn on the demo
            demo_thanlder.demoRun();

            while(demo_thanlder.getRunning()) {
                for (int y = 0; y < height; ++y) {
                    for (int x = 0; x < width; ++x) {
                        int c = sub_blocks * (y / y_step) + x / x_step;
                        switch (count % 4) {
                            case 0:
                                Frame_GrayScaleBlock.SetPixel(x, y, c, c, c);
                                break;
                            case 1:
                                Frame_GrayScaleBlock.SetPixel(x, y, c, 0, 0);
                                break;
                            case 2:
                                Frame_GrayScaleBlock.SetPixel(x, y, 0, c, 0);
                                break;
                            case 3:
                                Frame_GrayScaleBlock.SetPixel(x, y, 0, 0, c);
                                break;
                        }
                    }
                }
                count++;
                try {
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Frame_GrayScaleBlock.DumpToWeb(ipaddr_, port_);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Frame_GrayScaleBlock.CloseTCP(ipaddr_, port_);
            } catch (IOException e) {
                e.printStackTrace();
            }

            demo_thanlder.quitSafely();
        }
    }

    //**********************************
    //   Rotating Block Demo  --- Simple class that generates a rotating block on the screen.
    //**********************************
    private class Rotating_Block implements Runnable  {

        private int height;
        private int width;
        private String ipaddr_;
        private int port_;
        private int cent_x;
        private int cent_y;
        private float[] rot_x = new float [1];
        private float[] rot_y = new float [1];

        private Framebuffer Frame_RBlock = new Framebuffer();

        Rotating_Block (int rows, int columns, String ipaddr, int port) {
            cent_y = rows / 2;
            cent_x = columns / 2;
            port_= port;
            ipaddr_ = ipaddr;

            demo_thanlder = new DemoThreadHandler("RBLOCK");
            demo_thanlder.start();
            demo_thanlder.waitForready();

            demo_ui_handler.sendEmptyMessage(MSG_DEMO_STARTED);
        }

        private void Rotate(int x, int y, float angle) {
            rot_x[0] = (float)(x * Math.cos(angle) - y * Math.sin(angle));
            rot_y[0] = (float)(x * Math.sin(angle) + y * Math.cos(angle));
        }

        private int scale_col(int val, int lo, int hi) {
            if (val < lo) return (byte)0;
            if (val > hi) return (byte)255;
            return (255 * (val - lo) / (hi - lo));
        }


        public void run()  {
            // The square to rotate (inner square + black frame) needs to cover the
            // whole area, even if diagnoal. Thus, when rotating, the outer pixels from
            // the previous frame are cleared.
            int rotate_square = (int) (Math.min(cent_x, cent_y) * 1.41);
            int min_rotate = (cent_x - rotate_square / 2);
            int max_rotate = (cent_x + rotate_square / 2);

            // The square to display is within the visible area.
            int display_square = (int) (Math.min(cent_x, cent_y) * 0.7);
            int min_display = (cent_x - display_square / 2);
            int max_display = (cent_x + display_square / 2);

            float deg_to_rad = (float)(2 * 3.14159265 / 360);
            int rotation = 0;

            // Set the flag to turn on the demo
            demo_thanlder.demoRun();

            while(demo_thanlder.getRunning()) {
                ++rotation;
                try {
                    Thread.sleep(15* 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                rotation %= 360;
                for (int x = min_rotate; x < max_rotate; ++x) {
                    for (int y = min_rotate; y < max_rotate; ++y) {
                        Rotate((x - cent_x), (y - cent_y), deg_to_rad * rotation);
                        if (x >= min_display && x < max_display && y >= min_display && y < max_display) { // within display square
                            Frame_RBlock.SetPixel((int)(rot_x[0] + cent_x), (int)(rot_y[0] + cent_y),
                                    scale_col(x, min_display, max_display),
                                    (255 - scale_col(y, min_display, max_display)),
                                    scale_col(y, min_display, max_display));
                        } else {
                            // black frame.
                            Frame_RBlock.SetPixel((int)rot_x[0] + cent_x, (int)(rot_y[0] + cent_y), 0, 0, 0);
                        }
                    }
                }
                try {
                    Frame_RBlock.DumpToWeb(ipaddr_, port_);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Frame_RBlock.CloseTCP(ipaddr_, port_);
            } catch (IOException e) {
                e.printStackTrace();
            }

            demo_thanlder.quitSafely();
        }
    }

    //**********************************
    //   Abelian sandpile  :  Contributed by: Vliedel
    //**********************************
    private class Sandpile implements Runnable {

        private int height;
        private int width;
        private String ipaddr_;
        private int port_;
        private int[][] values_;
        private int[][] newValues_;
        private int delay_ms_ = 500;

        private Random rnd = new Random();

        private Framebuffer Frame_Sandpile = new Framebuffer();

        Sandpile(int rows, int columns, String ipaddr, int port, int delay) {

            height = rows;
            width = columns;
            port_ = port;
            ipaddr_ = ipaddr;
            values_ = new int[width][height];
            newValues_ = new int[width][height];
            delay_ms_ = delay;

            // Init values
            rnd.setSeed(System.currentTimeMillis());
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    values_[x][y] = 0;
                }
            }

            demo_thanlder = new DemoThreadHandler("SANDPILE");
            demo_thanlder.start();
            demo_thanlder.waitForready();

            demo_ui_handler.sendEmptyMessage(MSG_DEMO_STARTED);
        }

        public void run() {
            // Set the flag to turn on the demo
            demo_thanlder.demoRun();

            while(demo_thanlder.getRunning()) {
                // Drop a sand grain in the centre
                values_[width / 2][height / 2]++;
                updateValues();

                for (int x = 0; x < width; ++x) {
                    for (int y = 0; y < height; ++y) {
                        switch (values_[x][y]) {
                            case 0:
                                Frame_Sandpile.SetPixel(x, y,  0,  0,  0);
                                break;
                            case 1:
                                Frame_Sandpile.SetPixel(x, y,  0,  0,  200);
                                break;
                            case 2:
                                Frame_Sandpile.SetPixel(x, y,  0,  200,  0);
                                break;
                            case 3:
                                Frame_Sandpile.SetPixel(x, y,  150,  100,  0);
                                break;
                            default:
                                Frame_Sandpile.SetPixel(x, y,  200,  0,  0);
                        }
                    }
                }
                try {
                    Thread.sleep(delay_ms_ * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Frame_Sandpile.DumpToWeb(ipaddr_, port_);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Frame_Sandpile.CloseTCP(ipaddr_, port_);
            } catch (IOException e) {
                e.printStackTrace();
            }

            demo_thanlder.quitSafely();
        }

        private void updateValues() {
            // Copy values to newValues
            for (int x = 0; x < width; ++x)
                System.arraycopy(values_[x], 0, newValues_[x], 0, height);

            // Update newValues based on values
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    if (values_[x][y] > 3) {
                        // Collapse
                        if (x > 0)
                            newValues_[x - 1][y]++;
                        if (x < width - 1)
                            newValues_[x + 1][y]++;
                        if (y > 0)
                            newValues_[x][y - 1]++;
                        if (y < height - 1)
                            newValues_[x][y + 1]++;
                        newValues_[x][y] -= 4;
                    }
                }
            }
            // Copy newValues to values
            for (int x = 0; x < width; ++x) {
                System.arraycopy(newValues_[x], 0, values_[x], 0, height);
            }
        }
    }

    //**********************************
    //   Simple Square Demo
    //**********************************
    private class SimpleSquare implements Runnable {

        private int height;
        private int width;
        private String ipaddr_;
        private int port_;

        SimpleSquare(int rows, int columns, String ipaddr, int port) {

            height = rows;
            width = columns;
            port_= port;
            ipaddr_ = ipaddr;

            demo_thanlder = new DemoThreadHandler("SSQUARE");
            demo_thanlder.start();
            demo_thanlder.waitForready();

            demo_ui_handler.sendEmptyMessage(MSG_DEMO_STARTED);
        }

        private boolean runonce_ = true;

        private Framebuffer Frame_SimpleSquare = new Framebuffer();

        public void run() {

            // Set the flag to turn on the demo
            demo_thanlder.demoRun();

            while(demo_thanlder.getRunning()) {
                // Diagonal
                int x;
                for (x = 0; x < width; ++x) {
                    Frame_SimpleSquare.SetPixel(x, x, 255, 255, 255);               // white
                    Frame_SimpleSquare.SetPixel(height - 1 - x, x, 255, 0, 255); // magenta
                }
                for (x = 0; x < width; ++x) {
                    Frame_SimpleSquare.SetPixel(x, 0, 255, 0, 0);               // top line: red
                    Frame_SimpleSquare.SetPixel(x, height - 1, 255, 255, 0);    // bottom line: yellow
                }
                int y;
                for (y = 0; y < height; ++y) {
                    Frame_SimpleSquare.SetPixel(0, y, 0, 0, 255);              // left line: blue
                    Frame_SimpleSquare.SetPixel(width - 1, y, 0, 255, 0);      // right line: green
                }

                if (runonce_) {
                    try {
                        Frame_SimpleSquare.DumpToWeb(ipaddr_, port_);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    runonce_ = false;
                }
            }
            try {
                Frame_SimpleSquare.CloseTCP(ipaddr_, port_);
            } catch (IOException e) {
                e.printStackTrace();
            }

            demo_thanlder.quitSafely();

        }

    }

    //**********************************
    //   Imitation of volume bars  Contributed by: Vliedel
    //**********************************
    private class VolumeBars implements Runnable  {

        private int height;
        private int width;
        private String ipaddr_;
        private int port_;
        private  int delay_ms_ = 500;

        private int numBars_;
        private int[] barHeights_;
        private int barWidth_;
        private int heightGreen_;
        private int heightYellow_;
        private int heightOrange_;
        private int heightRed_;
        private int[] barFreqs_;
        private int[] barMeans_;
        private int t_;
        int[] means;

        private Random rnd = new Random();

        private Framebuffer Frame_VolumeBars = new Framebuffer();

        VolumeBars (int rows, int columns, String ipaddr, int port, int delay) {

            height = rows;
            width = columns;
            port_= port;
            ipaddr_ = ipaddr;
            delay_ms_ = delay;

            demo_thanlder = new DemoThreadHandler("VBARS");
            demo_thanlder.start();
            demo_thanlder.waitForready();

            demo_ui_handler.sendEmptyMessage(MSG_DEMO_STARTED);
        }

        public void run() {

            barWidth_ = width/numBars_;
            barHeights_ = new int[numBars_];
            barMeans_ = new int[numBars_];
            barFreqs_ = new int[numBars_];
            heightGreen_  = height*4/12;
            heightYellow_ = height*8/12;
            heightOrange_ = height*10/12;
            heightRed_    = height*12/12;

            // Array of possible bar means
            int numMeans = 10;
            means = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 16, 32};
            for (int i=0; i<numMeans; ++i) {
                means[i] = height - means[i]*height/8;
            }
            // Initialize bar means randomly
            rnd.setSeed(System.currentTimeMillis());
            for (int i=0; i<numBars_; ++i) {
                barMeans_[i] = rnd.nextInt()%numMeans;
                barFreqs_[i] = 1<<(rnd.nextInt()%3);
            }

            // Set the flag to turn on the demo
            demo_thanlder.demoRun();

            while(demo_thanlder.getRunning()) {
                if (t_ % 8 == 0) {
                    // Change the means
                    for (int i=0; i<numBars_; ++i) {
                        barMeans_[i] += rnd.nextInt()%3 - 1;
                        if (barMeans_[i] >= numMeans)
                            barMeans_[i] = numMeans-1;
                        if (barMeans_[i] < 0)
                            barMeans_[i] = 0;
                    }
                }

                // Update bar heights
                t_++;
                for (int i=0; i<numBars_; ++i) {
                    barHeights_[i] = (int) ((height - means[barMeans_[i]]) * Math.sin(0.1*t_*barFreqs_[i]) + means[barMeans_[i]]);
                    if (barHeights_[i] < height/8)
                        barHeights_[i] = rnd.nextInt() % (height/8) + 1;
                }

                for (int i=0; i<numBars_; ++i) {
                    int y;
                    for (y=0; y<barHeights_[i]; ++y) {
                        if (y<heightGreen_) {
                            drawBarRow(i, y, 0, 200, 0);
                        }
                        else if (y<heightYellow_) {
                            drawBarRow(i, y, 150, 150, 0);
                        }
                        else if (y<heightOrange_) {
                            drawBarRow(i, y, 250, 100, 0);
                        }
                        else {
                            drawBarRow(i, y, 200, 0, 0);
                        }
                    }
                    // Anything above the bar should be black
                    for (; y<height; ++y) {
                        drawBarRow(i, y, 0, 0, 0);
                    }
                }
                try {
                    Thread.sleep(delay_ms_ * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Frame_VolumeBars.DumpToWeb(ipaddr_, port_);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Frame_VolumeBars.CloseTCP(ipaddr_, port_);
            } catch (IOException e) {
                e.printStackTrace();
            }

            demo_thanlder.quitSafely();
        }

        private void drawBarRow(int bar, int y, int r, int g, int b) {
            for (byte x = (byte) (bar*barWidth_); x<(bar+1)*barWidth_; ++x) {
                Frame_VolumeBars.SetPixel(x, height-1-y, r, g, b);
            }
        }
    }


}