Here are the connections on the back of the V800...

![image](https://user-images.githubusercontent.com/5520281/193774603-1e8e27ee-3c1f-4c74-a0e3-1cd586091bce.png)

The unit needs 10-12VDC and draws ~1.5A at startup and then ~800mA thereafter. I will give you a power brick with a connector that will fit. Note that the connector 
is very homemade and not keyed for polarity. Make sure that you match the markings on the connector to the back of the unit and do not trust anyone who tells you different (espcialy [Velodyne](power-traceout.png))! If you unplug the connector and then plug it back
in wrong and the unit starts smoking then I will not feel bad for you. 

The data connector is similarly janky and you should try not to unplug it. If you do, note that it does not really fit so you have to be very careful not to bend the 
little pins inside the socket on the unit when you insert the plug. 

The data lines goes to this handy board...

![image](https://user-images.githubusercontent.com/5520281/193776864-2ee2d379-407a-4aad-9287-d51d039ad0e0.png)

This board is set up to be a transparent PHY media converter. Do not change any of the jumpers or download new software into this or it will probably not work anymore. 
The USB port is just for power and can go into a computer or a wall wort. The `LED0` will light when the board sees the V800 connected at the other end, and the `LED1` will blink when the board recieves packets from the V800 (which is ALWAYS, from the moment that it powers up). The network port is a standard RJ45 connector running 1000BaseT which you should connect to 
the host computer using a direct Cat6 cable (no switches or hubs or routers). 

The host computer really needs a full 1000BaseT (1 Gbs) network port or else it will miss packets. If the computer starts to studder when you plug into the network cable, 
then it is probably too much traffic (thuosands of packets per sec) so you will need a faster computer if you want to be able to decode the data.

The network adapter on the host computer must be set up with...

    IP: 169.254.0.12
    MASK: 255.255.255.0
    GATEWAY: 169.254.0.1

(Mask and gateway don't actually matter as long as they are valid)

To set the IP address on Windows, you can go into Control Panel->View Network Connections and then right click on
"Properties->IP Version 4->Properties" and then "Us the following".

Addiitonally you must set up the host adapter MAC address to be...

    MAC: 0E1FC69BE4DE

To set the MAC address on Windows, you can go into Control Panel->View Network Connections and then right click on
"Configure->Advanced" and look for something like "Network Address" or "MAC Address" or "Hardware Address.
Some cards might want colons in the address like 0E:1F:C6:9B:E4:DE

Finally, you need to disable any firewall that might block these packets. On Windows, go into "Start->Firewall"
and display the firewall for the adapter that is connected to the V800 (or all). Note that windows has a way of
turning the firewall back on without telling you, so check this in case everything stops working!

If you get any of these wrong, you probably will not see any packets. You can test by installing WireShark and using that to listen on the interface. 
It will see the packets as long as the unit is on and the data connections are good, so if it sees them but the java program does not, then one of these settings is probably wrong. 
