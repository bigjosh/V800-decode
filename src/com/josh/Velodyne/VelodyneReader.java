package com.josh.Velodyne;

// (C)2022 josh.com

/*

    Read realtime packets from a Velodyne V800 LiDAR scanner and display the data graphically to make it easier
    to try to figure out which fields are where.

    Note that for my unit, the destination IP and MAC address were hard coded, so you must set up
    the computer that you run this program on to match.

    These values on my unit were:
    IP : 169.254.0.12
    MAC: 0E1FC69BE4DE

    To find these, I used Wireshark to capture some packets on the interface connected to the V800 (there are plenty!)
    and then just looked at the destination for some UDP packets sent to port 2368 (the Velodyne standard port)

    To set the IP address on Windows, you can go into Control Panel->View Network Connections and then right click on
    "Properties->IP Version 4->Properties" and then "Us the following".

    To set the MAC address on Windows, you can go into Control Panel->View Network Connections and then right click on
    "Configure->Advanced" and look for something like "Network Address" or "MAC Address" or "Hardware Address.
    Some cards might want colons in the address like 0E:1F:C6:9B:E4:DE

    Finally, you need to disable any firewall that might block these packets. On Windows, go into "Start->Firewall"
    and display the firewall for the adapter that is connected to the V800 (or all). Note that windows has a way of
    turning the firewall back on without telling you, so check this in case everything stops working!

 */

import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class VelodyneReader
{

    // These params were determined empirically by looking at a Wireshark capture
    private static final int EXPECTED_PACKET_LEN = 1304;
    private static final int listenPort = 2368;
    private static String localAddressStr =  "169.254.0.12";

    static long packet_count=0;

    public static void main(String args[]) throws Exception
    {

        PacketVisualizer packetVisualizer = new PacketVisualizer();

        var datagramsocket = new DatagramSocket(listenPort, Inet4Address.getByName( localAddressStr ));
         TimerTask repeatedTask = new TimerTask() {
            public void run() {
                System.out.printf("%02d:packet_count: %-8d\n",
                        java.time.LocalTime.now().getSecond() , packet_count);
            }
        };

        new Timer().scheduleAtFixedRate(repeatedTask, 0 , 1000L);   // Print stats once a second to console so we know it is running

        while (true) {

            // I know it would be faster to reuse the datagram packet and buffer, but we are passing the buffer into the
            // visualizer which will keep a reference to it and I think it will be faster to let the GC deal with these short-lived
            // buffers than to try to make a copy on every call to the update().
            // We could also have shared the buffer and then synchonrized access to it, but then maybe we would miss two packets
            // that came in if the 2nd came while the update(or paint) was still running. :/

            byte buff[] = new byte[EXPECTED_PACKET_LEN];
            DatagramPacket p = new DatagramPacket(buff, buff.length);

            datagramsocket.receive(p);

            if ( p.getLength()!= EXPECTED_PACKET_LEN ) {
                throw new Exception("Packet len expected=" + Integer.toString(EXPECTED_PACKET_LEN) + " got=" + Integer.toString(p.getLength()));
            }

            packetVisualizer.showPacket(buff);

            packet_count++;

        }
    }
}