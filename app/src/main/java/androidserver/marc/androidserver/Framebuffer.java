package androidserver.marc.androidserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by MARC on 10/20/2017.
 */

public class Framebuffer {

    private int rows_;     // Number of rows. 16 or 32.
    private int columns_;  // Number of columns. Number of chained boards * 32.

    private byte pwm_bits_;   // PWM bits to display.

    private int double_rows_;
    private int row_mask_;

    private int[] bitplane_buffer_;

    private byte[] canvas;

    private int bitplane_Buffer_size;

    // maximum usable bitplanes.
    private byte kBitPlanes_ = 11;

    private boolean do_luminance_correct_ = true;

    private Socket senderSocket;

    public int running = 0;

    private boolean tcpopen_flag = false;

    // CIE1931 correction table
// Automatically generated

    short[] cie = {
            0, 1, 2, 3, 4, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26, 27,
            28, 29, 31, 32, 33, 34, 36, 37, 39, 40,
            42, 43, 45, 47, 48, 50, 52, 54, 55, 57,
            59, 61, 63, 65, 67, 70, 72, 74, 76, 79,
            81, 83, 86, 88, 91, 94, 96, 99, 102, 105,
            108, 111, 114, 117, 120, 123, 126, 129, 133, 136,
            139, 143, 146, 150, 154, 157, 161, 165, 169, 173,
            177, 181, 185, 189, 194, 198, 202, 207, 211, 216,
            221, 226, 230, 235, 240, 245, 250, 255, 261, 266,
            271, 277, 282, 288, 293, 299, 305, 311, 317, 323,
            329, 335, 341, 348, 354, 360, 367, 374, 380, 387,
            394, 401, 408, 415, 422, 430, 437, 445, 452, 460,
            467, 475, 483, 491, 499, 507, 516, 524, 532, 541,
            549, 558, 567, 576, 585, 594, 603, 612, 621, 631,
            640, 650, 660, 669, 679, 689, 699, 710, 720, 730,
            741, 751, 762, 773, 784, 795, 806, 817, 828, 840,
            851, 863, 875, 887, 898, 911, 923, 935, 947, 960,
            972, 985, 998, 1011, 1024, 1037, 1050, 1064, 1077, 1091,
            1104, 1118, 1132, 1146, 1160, 1175, 1189, 1203, 1218, 1233,
            1248, 1263, 1278, 1293, 1308, 1324, 1339, 1355, 1371, 1387,
            1403, 1419, 1435, 1452, 1469, 1485, 1502, 1519, 1536, 1553,
            1571, 1588, 1606, 1623, 1641, 1659, 1677, 1696, 1714, 1732,
            1751, 1770, 1789, 1808, 1827, 1846, 1866, 1885, 1905, 1925,
            1945, 1965, 1985, 2006, 2026, 2047,
    };


    Framebuffer( ) {
        netparams default_params = new netparams();
        default_params.init_params();

        do_luminance_correct_= (default_params.do_luminance_correct == 1);

        rows_ = default_params.rows;
        columns_ = default_params.chain;
        columns_ = columns_ * 32;

        pwm_bits_ = kBitPlanes_;

        double_rows_ = rows_ / 2;

        row_mask_ =  (double_rows_ - 1);

        bitplane_buffer_ = new int[double_rows_ * columns_ * kBitPlanes_];

        bitplane_Buffer_size = bitplane_buffer_.length *4;

        canvas = new byte[bitplane_Buffer_size];

        Clear();
    }

// _IoBits Functions and Defines

    // Adafruit Brd
    private static final int SHFT_OUTPUT_ENABLE = 4;
    private static final int SHFT_R1 = 5;
    private static final int SHFT_B1 = 6;
    private static final int SHFT_R2 = 12;
    private static final int SHFT_G1 = 13;
    private static final int SHFT_G2 = 16;
    private static final int SHFT_CLOCK = 17;
    private static final int SHFT_D = 20;
    private static final int SHFT_STROBE = 21;
    private static final int SHFT_A = 22;
    private static final int SHFT_B2 = 23;
    private static final int SHFT_B = 26;
    private static final int SHFT_C = 27;

    private static final int OUTPUT_ENABLE = 1;
    private static final int R1 = 1;
    private static final int B1 = 1;
    private static final int R2 = 1;
    private static final int G1 = 1;
    private static final int G2 = 1;
    private static final int CLOCK = 1;
    private static final int D = 1;
    private static final int STROBE = 1;
    private static final int A = 1;
    private static final int B2 = 1;
    private static final int B = 1;
    private static final int C = 1;

    // Raw GPIO
    private static final int SHFT_OUTPUT_ENABLE_REV1 = 0;
    private static final int SHFT_CLOCK_REV1 = 1;
    private static final int SHFT_OUTPUT_ENABLE_REV2 = 2;
    private static final int SHFT_CLOCK_REV2 = 3;
    private static final int SHFT_STROBE_GPIO = 4;
    private static final int SHFT_ROW_GPIO = 7;
    private static final int SHFT_R1_GPIO = 6;
    private static final int SHFT_G1_GPIO = 13;
    private static final int SHFT_B1_GPIO = 23;
    private static final int SHFT_R2_GPIO = 12;
    private static final int SHFT_G2_GPIO = 16;
    private static final int SHFT_B2_GPIO = 23;

    private static final int OUTPUT_ENABLE_REV1 = 1;
    private static final int CLOCK_REV1 = 1;
    private static final int OUTPUT_ENABLE_REV2 = 1;
    private static final int CLOCK_REV2 = 1;
    private static final int STROBE_GPIO = 1;
    private static final int ROW_GPIO = 4;
    private static final int R1_GPIO = 1;
    private static final int G1_GPIO = 1;
    private static final int B1_GPIO = 1;
    private static final int R2_GPIO = 1;
    private static final int G2_GPIO = 1;
    private static final int B2_GPIO = 1;


    //  ################################# GPIOs

    // output_enable 1
    public int get_output_enable_gpio(int bits) {
        return (bits >> SHFT_OUTPUT_ENABLE_REV1) & OUTPUT_ENABLE_REV1;
    }

    public int set_output_enable_gpio(int a, int bits) {
        bits &= ~(OUTPUT_ENABLE_REV1 << SHFT_OUTPUT_ENABLE_REV1);
        bits |= ((a & OUTPUT_ENABLE_REV1) << SHFT_OUTPUT_ENABLE_REV1);
        return bits;
    }

    // clock 1
    public int get_clock1(int bits) {
        return (bits >> SHFT_CLOCK_REV1) & CLOCK_REV1;
    }

    public int set_clock1(int a, int bits) {
        bits &= ~(CLOCK_REV1 << SHFT_CLOCK_REV1);
        bits |= ((a & CLOCK_REV1) << SHFT_CLOCK_REV1);
        return bits;
    }

    // output enable 2
    public int get_output_enable2(int bits) {
        return (bits >> SHFT_OUTPUT_ENABLE_REV2) & OUTPUT_ENABLE_REV2;
    }

    public int set_output_enable2(int a, int bits) {
        bits &= ~(OUTPUT_ENABLE_REV2 << SHFT_OUTPUT_ENABLE_REV2);
        bits |= ((a & OUTPUT_ENABLE_REV2) << SHFT_OUTPUT_ENABLE_REV2);
        return bits;
    }

    // clock 2
    public int get_clock2(int bits) {
        return (bits >> SHFT_CLOCK_REV2) & CLOCK_REV2;
    }

    public int set_clock2(int a, int bits) {
        bits &= ~(CLOCK_REV2 << SHFT_CLOCK_REV2);
        bits |= ((a & CLOCK_REV2) << SHFT_CLOCK_REV2);
        return bits;
    }

    // strobe
    public int get_strobe_gpio(int bits) {
        return (bits >> SHFT_STROBE_GPIO) & STROBE_GPIO;
    }

    public int set_strobe_gpio(int a, int bits) {
        bits &= ~(STROBE_GPIO << SHFT_STROBE_GPIO);
        bits |= ((a & STROBE_GPIO) << SHFT_STROBE_GPIO);
        return bits;
    }

    // row
    private int get_row(int bits) {
        return (bits >> SHFT_ROW_GPIO) & ROW_GPIO;
    }

    private int set_row(int a, int bits) {
        bits &= ~(ROW_GPIO << SHFT_ROW_GPIO);
        bits |= ((a & ROW_GPIO) << SHFT_ROW_GPIO);
        return bits;
    }

    // Red Bit 1
    private int get_r1_gpio(int bits) {
        return (bits >> SHFT_R1_GPIO) & R1_GPIO;
    }

    private int set_r1_gpio(int a, int bits) {
        bits &= ~(R1_GPIO << SHFT_R1_GPIO);
        bits |= ((a & R1_GPIO) << SHFT_R1_GPIO);
        return bits;
    }

    // green bit 1
    private int get_g1_gpio(int bits) {
        return (bits >> SHFT_G1_GPIO) & G1_GPIO;
    }

    private int set_g1_gpio(int a, int bits) {
        bits &= ~(G1_GPIO << SHFT_G1_GPIO);
        bits |= ((a & G1_GPIO) << SHFT_G1_GPIO);
        return bits;
    }

    // blue bit 1
    private int get_b1_gpio(int bits) {
        return (bits >> SHFT_B1_GPIO) & B1_GPIO;
    }

    private int set_b1_gpio(int a, int bits) {
        bits &= ~(B1_GPIO << SHFT_B1_GPIO);
        bits |= ((a & B1_GPIO) << SHFT_B1_GPIO);
        return bits;
    }

    // red bit 2
    private int get_r2_gpio(int bits) {
        return (bits >> SHFT_R2_GPIO) & R2_GPIO;
    }

    private int set_r2_gpio(int a, int bits) {
        bits &= ~(R2_GPIO << SHFT_R2_GPIO);
        bits |= ((a & R2_GPIO) << SHFT_R2_GPIO);
        return bits;
    }

    // green bit 2
    private int get_g2_gpio(int bits) {
        return (bits >> SHFT_G2_GPIO) & G2_GPIO;
    }

    private int set_g2_gpio(int a, int bits) {
        bits &= ~(G2_GPIO << SHFT_G2_GPIO);
        bits |= ((a & G2_GPIO) << SHFT_G2_GPIO);
        return bits;
    }

    // blue bit 2
    private int get_b2_gpio(int bits) {
        return (bits >> SHFT_B2_GPIO) & B2_GPIO;
    }

    private int set_b2_gpio(int a, int bits) {
        bits &= ~(B2_GPIO << SHFT_B2_GPIO);
        bits |= ((a & B2_GPIO) << SHFT_B2_GPIO);
        return bits;
    }

    //  #################################ADAFRUIT Board

    // output_enable
    private int get_output_enable(int bits) {
        return (bits >> SHFT_OUTPUT_ENABLE) & OUTPUT_ENABLE;
    }

    private int set_output_enable(int a, int bits) {
        bits &= ~(OUTPUT_ENABLE << SHFT_OUTPUT_ENABLE);
        bits |= ((a & OUTPUT_ENABLE) << SHFT_OUTPUT_ENABLE);
        return bits;
    }

    // red bit 1
    private int get_r1(int bits) {
        return (bits >> SHFT_R1) & R1;
    }

    private int set_r1(int a, int bits) {
        bits &= ~(R1 << SHFT_R1);
        bits |= ((a & R1) << SHFT_R1);
        return bits;
    }

    // blue bit 1
    private int get_b1(int bits) {
        return (bits >> SHFT_B1) & B1;
    }

    private int set_b1(int a, int bits) {
        bits &= ~(B1 << SHFT_B1);
        bits |= ((a & B1) << SHFT_B1);
        return bits;
    }

    // red bit 2
    private int get_r2(int bits) {
        return (bits >> SHFT_R2) & R2;
    }

    private int set_r2(int a, int bits) {
        bits &= ~(R2 << SHFT_R2);
        bits |= ((a & R2) << SHFT_R2);
        return bits;
    }

    // green bit 1
    private int get_g1(int bits) {
        return (bits >> SHFT_G1) & G1;
    }

    private int set_g1(int a, int bits) {
        bits &= ~(G1 << SHFT_G1);
        bits |= ((a & G1) << SHFT_G1);
        return bits;
    }

    // green bit 2
    private int get_g2(int bits) {
        return (bits >> SHFT_G2) & G2;
    }

    private int set_g2(int a, int bits) {
        bits &= ~(G2 << SHFT_G2);
        bits |= ((a & G2) << SHFT_G2);
        return bits;
    }

    // clock
    private int get_clock(int bits) {
        return (bits >> SHFT_CLOCK) & CLOCK;
    }

    private int set_clock(int a, int bits) {
        bits &= ~(CLOCK << SHFT_CLOCK);
        bits |= ((a & CLOCK) << SHFT_CLOCK);
        return bits;
    }

    // d bit
    private int get_d(int bits) {
        return (bits >> SHFT_D) & D;
    }

    private int set_d(int a, int bits) {
        bits &= ~(D << SHFT_D);
        bits |= ((a & D) << SHFT_D);
        return bits;
    }

    // strobe
    private int get_strobe(int bits) {
        return (bits >> SHFT_STROBE) & STROBE;
    }

    private int set_strobe(int a, int bits) {
        bits &= ~(STROBE << SHFT_STROBE);
        bits |= ((a & STROBE) << SHFT_STROBE);
        return bits;
    }

    // a bit
    private int get_a(int bits) {
        return (bits >> SHFT_A) & A;
    }

    private int set_a(int a, int bits) {
        bits &= ~(A << SHFT_A);
        bits |= ((a & A) << SHFT_A);
        return bits;
    }

    // blue bit 2
    private int get_b2(int bits) {
        return (bits >> SHFT_B2) & B2;
    }

    private int set_b2(int a, int bits) {
        bits &= ~(B2 << SHFT_B2);
        bits |= ((a & B2) << SHFT_B2);
        return bits;
    }

    // b bit
    private int get_b(int bits) {
        return (bits >> SHFT_B) & B;
    }

    private int set_b(int a, int bits) {
        bits &= ~(B << SHFT_B);
        bits |= ((a & B) << SHFT_B);
        return bits;
    }

    // c bit
    private int get_c(int bits) {
        return (bits >> SHFT_C) & C;
    }

    private int set_o(int a, int bits) {
        bits &= ~(C << SHFT_C);
        bits |= ((a & C) << SHFT_C);
        return bits;
    }


//From original driver code
// If you see that your display is inverse, you might have a matrix variant
//has uses inverse logic for the RGB bits. Attempt this
//            #DEFINES+=-DINVERSE_RGB_DISPLAY_COLORS

    private short Color_Out_Bits(short s) {
        //ifdef INVERSE_RGB_DISPLAY_COLORS
        //return s ^ 0xffff;
        // else
        return s;
    }

    private int ValueAt(int double_row, int column, int bit) {
        return bitplane_buffer_[double_row * (columns_ * kBitPlanes_) + bit * columns_ + column];
    }

    private void ValuePut(int double_row, int column, int bit, int value) {

        bitplane_buffer_[double_row * (columns_ * kBitPlanes_) + bit * columns_ + column] = value;
    }


    // We use a hardcoded cie[] table, see above.  This is generated
    // form the cie1931.py python scipt.
    private short MapColor(int c) {
        byte shift;
        if (do_luminance_correct_) {
            return Color_Out_Bits(cie[c]);
        } else {
            shift = (byte) (kBitPlanes_ - 8);
        }   //constexpr; shift to be left aligned.
        return Color_Out_Bits((short) ((shift > 0) ? (c << shift) : (c >> -shift)));
    }


    // Set the RGB for 1 LED in the Grid
    void SetPixel(int x, int y, int r, int g, int b) {
        int led_value;
        short mask;
        int z;
        int bits;
        int valueat;
        if (x < 0 || x >= columns_ || y < 0 || y >= rows_)
            return;

        short red = MapColor(r);
        short green = MapColor(g);
        short blue = MapColor(b);

        int min_bit_plane = kBitPlanes_ - pwm_bits_;

        // Grab the byte from the Canvas
        valueat = (y & row_mask_) * (columns_ * kBitPlanes_) + min_bit_plane * columns_ + x;

        if (y < double_rows_) {   // Upper sub-panel.
            for (z = min_bit_plane; z < kBitPlanes_; ++z) {
                bits = bitplane_buffer_[valueat];
                mask = (short) (1 << z);
                bits = set_r1( ( (red & mask) == mask) ? 1 : 0, bits);
                bits = set_g1( ( (green & mask) == mask) ? 1 : 0, bits);
                bits = set_b1( ( (blue & mask) == mask) ? 1 : 0, bits);
                bitplane_buffer_[valueat] = bits;
                valueat += columns_;
            }
        } else {
            for (z = min_bit_plane; z < kBitPlanes_; ++z) {
                bits = bitplane_buffer_[valueat];
                mask = (short) (1 << z);
                bits = set_r2( ( (red & mask) == mask) ? 1 : 0, bits);
                bits = set_g2( ( (green & mask) == mask) ? 1 : 0, bits);
                bits = set_b2( ( (blue & mask) == mask) ? 1 : 0, bits);
                bitplane_buffer_[valueat] = bits;
                valueat += columns_;
            }
        }
    }

    // If you see that your display is inverse, you might have a matrix variant
    //has uses inverse logic for the RGB bits. Attempt this
    //            #DEFINES+=-DINVERSE_RGB_DISPLAY_COLORS

    void Clear() {
        //#ifdef INVERSE_RGB_DISPLAY_COLORS
        //Fill(0,0,0);
        //#else
        Arrays.fill(bitplane_buffer_, 0);
        Arrays.fill(canvas, (byte)0);
    }

    void Fill(int r, int g, int b) {
        int y;
        short mask;
        int plane_bits;
        short red = MapColor((byte) r);
        short green = MapColor((byte) g);
        short blue = MapColor((byte) b);
        int bits;

        for (y = kBitPlanes_ - pwm_bits_; y < kBitPlanes_; ++y) {
            mask = (short) (1 << b);

            bits = 0;

            bits = set_r1( ( (red & mask) == mask ) ? 1 : 0, bits);
            bits = set_g1( ( (green & mask) == mask) ? 1 : 0, bits);
            bits = set_b1( ( (blue & mask) == mask) ? 1 : 0, bits);
            bits = set_r2( ( (red & mask) == mask) ? 1 : 0, bits);
            bits = set_g2( ( (green & mask) == mask) ? 1 : 0, bits);
            bits = set_b2( ( (blue & mask) == mask) ? 1 : 0, bits);

            Arrays.fill(bitplane_buffer_, bits);

            // Cycle through all rows/columns to set the values..
            //for (int row = 0; row < double_rows_; ++row) {
            //    for (int col = 0; col < columns_; ++col) {
            //        ValuePut(row, col, b, getIoBits());
            //    }
            //}
        }
    }


    void DumpToWeb(String ipaddr, int port) throws IOException {

        // Convert the int[] into a byte[] and do a bit of endianess magic along the way
        intToByteLE(bitplane_buffer_, canvas);
        // Send the canvas to the server
        sendDataTCP(ipaddr, port, canvas);
    }

    private void intToByteLE(int[] input, byte[] output)
    {
        for(int i = 0; i < input.length; i++) {
            output[i*4] = (byte)(input[i] & 0xFF);
            output[i*4 + 1] = (byte)((input[i] & 0xFF00) >>> 8);
            output[i*4 + 2] = (byte)((input[i] & 0xFF0000) >>> 16);
            output[i*4 + 3] = (byte)((input[i] & 0xFF000000) >>> 24);
        }
    }

    private void intToByteBE(int[] input, byte[] output)
    {

        for(int i = 0; i < input.length; i++) {
            output[i*4 + 3] = (byte)(input[i] & 0xFF);
            output[i*4 + 2] = (byte)((input[i] & 0xFF00) >>> 8);
            output[i*4 + 1] = (byte)((input[i] & 0xFF0000) >>> 16);
            output[i*4] = (byte)((input[i] & 0xFF000000) >>> 24);
        }
    }

    // Close the TCP connection by sending the UDP command and setting the tcpopen_flag to false.
    void CloseTCP(String ipaddr, int port) throws IOException
    {
        send_srvr srvr_snd = new send_srvr();
        srvr_snd.tcpstop(ipaddr, port);
        senderSocket.close();
        tcpopen_flag = false;
    }

    private void sendDataTCP(String ipaddr, int port,  byte[] data) throws IOException
    {
        // if we have not opened the TCP Stream yet, then open it..
        if (tcpopen_flag == false)  {
            senderSocket = new Socket(ipaddr, port);
            tcpopen_flag = true;
        }
        OutputStream os = senderSocket.getOutputStream();
        //os.write(data);
        os.write(data, 0, data.length);
        os.flush();

    }
}


