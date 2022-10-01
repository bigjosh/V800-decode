package com.josh.Velodyne;

/*

    Read realtime packets from a Velodyne V800 LiDAR scanner

    Note that for my unit, the destination IP and MAC address were hard coded, so you must set up
    the computer that you run this program on to match.

    These values on my unit were:
    IP : 169.254.0.12
    MAC: 0E1FC69BE4DE

    To find these, I used Wireshark to capture some packets on the interface connected to the V800 (there are plenty!)
    and then just looked at the destination for some UDP packets sent to port 2368 (the Velodyne standard port)

    To set the IP address on windows, you can go into Control Panel->View Network Connections and then right click on
    "Properties->IP Version 4->Properties" and then "Us the following".

    To set the MAC address on Windows, you can go into Control Panel->View Network Connections and then right click on
    "Configure->Advanced" and look for something like "Network Address" or "MAC Address" or "Hardware Address.
    Some cards might want colons in the address like 0E:1F:C6:9B:E4:DE

    Finally, you need to disable any firewall that might block these packets. On windows, go into "Start->Firewall"
    and display the firewall for the adapter that is connected to the V800 (or all). Nore that windows has a way of
    turning the firewall back on without telling you, so check this in case everything stops working!

 */

import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("SpellCheckingInspection")
public class VelodyneReader
{

    static long packet_count=0;

    public static void main(String args[]) throws IOException
    {

        var datagramsocket = new DatagramSocket( 2368, Inet4Address.getByName("169.254.0.12"));
        byte buf[] = new byte[8192];
        DatagramPacket p = new DatagramPacket(buf, buf.length);

        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                System.out.printf("%02d:packet_count: %-8d\n",
                        java.time.LocalTime.now().getSecond() , packet_count);
            }
        };

        new Timer().scheduleAtFixedRate(repeatedTask, 0 , 1000L);   // Print stats once a second

        while (true) {

            datagramsocket.receive(p);

            packet_count++;

        }
    }
}