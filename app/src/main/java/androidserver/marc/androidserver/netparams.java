package androidserver.marc.androidserver;

/**
 * Created by MARC on 7/16/2017.
 */


class netparams {

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

    byte netopcode;
    byte rsvd0;
    byte rsvd1;
    byte rsvd2;
    int runtime_seconds;
    int demo;
    int rows;
    int chain;
    int scroll_ms;
    int pwm_bits;
    int large_display; //bool
    int do_luminance_correct; //bool
    int value;

    netparams() {

    }

    void init_params() {
        netopcode = NET_INIT_PARAMS;
        runtime_seconds = -1;
        demo = -1;
        rows = 32;
        chain = 1;
        scroll_ms = 1000;
        pwm_bits = -1;
        large_display = 0;
        do_luminance_correct = 1;
        rsvd0 = 0;
        rsvd1 = 0;
        rsvd2 = 0;
    }

    void disconnet() {
        netopcode = NET_CLIENT_DC;
        rsvd0 = 0;
        rsvd1 = 0;
        rsvd2 = 0;
        value = 0;
    }

    void kill() {
        netopcode = NET_KILLSRVR;
        rsvd0 = 0;
        rsvd1 = 0;
        rsvd2 = 0;
        value = 0;
    }

    void tcpstop() {
        netopcode = NET_TCPSTOP;
        rsvd0 = 0;
        rsvd1 = 0;
        rsvd2 = 0;
        value = 0;
    }


}