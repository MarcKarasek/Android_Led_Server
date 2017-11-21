package androidserver.marc.androidserver;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by MARC on 10/15/2017.
 */

class send_srvr {

    private int betole(int val) throws IOException {
        return (((val & 0xff000000) >> 24) | ((val & 0x00ff0000) >> 8) | ((val & 0x0000ff00) << 8) | ((val & 0x000000ff) << 24));
    }

    private byte[] net_parameters() throws IOException {
        netparams default_params = new netparams();
        default_params.init_params();
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        dataOut.writeByte(default_params.netopcode);
        dataOut.writeByte(default_params.rsvd0);
        dataOut.writeByte(default_params.rsvd1);
        dataOut.writeByte(default_params.rsvd2);
        dataOut.writeInt(betole(default_params.runtime_seconds));
        dataOut.writeInt(betole(default_params.demo));
        dataOut.writeInt(betole(default_params.rows));
        dataOut.writeInt(betole(default_params.chain));
        dataOut.writeInt(betole(default_params.scroll_ms));
        dataOut.writeInt(betole(default_params.pwm_bits));
        dataOut.writeInt(betole(default_params.large_display));
        dataOut.writeInt(betole(default_params.do_luminance_correct));
        return byteOut.toByteArray();
    }

    private byte[] srvr_disconnect() throws IOException {
        netparams disconnect_params = new netparams();
        disconnect_params.disconnet();
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        dataOut.writeByte(disconnect_params.netopcode);
        dataOut.writeByte(disconnect_params.rsvd0);
        dataOut.writeByte(disconnect_params.rsvd1);
        dataOut.writeByte(disconnect_params.rsvd2);
        dataOut.writeInt(betole(disconnect_params.value));
        return byteOut.toByteArray();
    }

    private byte[] srvr_kill() throws IOException {
        netparams disconnect_params = new netparams();
        disconnect_params.kill();
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        dataOut.writeByte(disconnect_params.netopcode);
        dataOut.writeByte(disconnect_params.rsvd0);
        dataOut.writeByte(disconnect_params.rsvd1);
        dataOut.writeByte(disconnect_params.rsvd2);
        dataOut.writeInt(betole(disconnect_params.value));
        return byteOut.toByteArray();
    }

    private byte[] srvr_tcpstop() throws IOException {
        netparams disconnect_params = new netparams();
        disconnect_params.tcpstop();
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        dataOut.writeByte(disconnect_params.netopcode);
        dataOut.writeByte(disconnect_params.rsvd0);
        dataOut.writeByte(disconnect_params.rsvd1);
        dataOut.writeByte(disconnect_params.rsvd2);
        dataOut.writeInt(betole(disconnect_params.value));
        return byteOut.toByteArray();
    }

    send_srvr() {

    }
    // This is where we will connect to the server and send cmds in
    void send_initparams(String ipaddr, int port) throws IOException
    {
        byte[] initparams = net_parameters();

        DatagramSocket client_socket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(ipaddr);
        DatagramPacket send_packet = new DatagramPacket(initparams, initparams.length, IPAddress, port);
        client_socket.send(send_packet);
    }

    void send_disconnect(String ipaddr, int port) throws IOException
    {
        byte[] initparams = srvr_disconnect();

        DatagramSocket client_socket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(ipaddr);
        DatagramPacket send_packet = new DatagramPacket(initparams, initparams.length, IPAddress, port);
        client_socket.send(send_packet);
    }

    void srvr_kill(String ipaddr, int port) throws IOException
    {
        byte[] initparams = srvr_kill();

        DatagramSocket client_socket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(ipaddr);
        DatagramPacket send_packet = new DatagramPacket(initparams, initparams.length, IPAddress, port);
        client_socket.send(send_packet);
    }

    void tcpstop(String ipaddr, int port) throws IOException
    {
        byte[] initparams = srvr_tcpstop();

        DatagramSocket client_socket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(ipaddr);
        DatagramPacket send_packet = new DatagramPacket(initparams, initparams.length, IPAddress, port);
        client_socket.send(send_packet);
    }


}
