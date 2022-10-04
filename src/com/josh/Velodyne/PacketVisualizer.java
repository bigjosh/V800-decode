package com.josh.Velodyne;

// (C)2022 josh.com
// This class will make a 2D window to try to graphically visualize packets

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.Arrays;
import javax.swing.JFrame;

public class PacketVisualizer extends Canvas {

    BufferedImage bufferedImage;

    void makeBufferedImage() {
        bufferedImage = new BufferedImage( this.getWidth()  , this.getHeight(), BufferedImage.TYPE_INT_RGB) ;
    }
    public PacketVisualizer() {

        JFrame frame = new JFrame("Velodyne packets");
        Canvas canvas = this;
        canvas.setSize(800, 800);
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);
        makeBufferedImage();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    byte[] packBuffer;
    long packetCounter =0;
    long lastPacketFrameNumber=0;
    long lastPacketTimeStamp=0;

    void paintUnsignedLongx(Graphics g, long l, int x, int y) {
        var chars = Long.toUnsignedString(l).toCharArray();
        g.drawChars(chars,0,chars.length,x , y);
    }

    void paintUnsignedLong(Graphics g, long l, int x, int y) {
        g.drawString( NumberFormat.getIntegerInstance().format(l) , x ,y );
    }

    public void paint( Graphics g ) {

        // Draw the points in one splash, then clear out the image buffer to be fresh and accumulate next
        // ground of points until next time we paint to the screen.
        //

        BufferedImage i = bufferedImage;

        // comment out the line below to have points persist forever
        makeBufferedImage();
        g.drawImage(i,0,0 , null );

        // Draw the text over the points
        // Yes this will flicker but not worth the effort to double buffer this, sorry

        g.setColor( Color.YELLOW);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.drawString("Packet Count:", 10, 10);
        paintUnsignedLong(g, packetCounter, 10, 24);

        g.drawString("FrameNumber:", 110, 10);
        paintUnsignedLong(g, lastPacketFrameNumber, 110, 24);

        g.drawString("Timestamp:", 210, 10);
        g.drawString(Long.toString(lastPacketTimeStamp), 210, 24);

    }

    int byte2int( byte[] bytes , int offset ) {
        //System.out.println("len="+bytes.length+" offset="+offset);
        return Byte.toUnsignedInt(  bytes[offset] );
    }

    int bytes2word( byte[] bytes , int offset ) {
        int x=0;
        for( var i = 0; i<2 ;i++) {
            x = (x << 8) + byte2int(bytes, offset+i );        // Convert byte into number
        }
        return x;
    }
    int bytes2int( byte[] bytes , int offset ) {
        int x=0;
        for( var i = 0; i<4 ;i++) {
            x = (x << 8) + byte2int(bytes, offset+i );        // Convert byte into number
        }
        return x;
    }

    long bytes2long( byte[] bytes, int offset ) {
        long x=0;
        for( var i = 0; i<8 ;i++) {
            x = (x << 8) + byte2int (bytes , offset+i );        // Convert byte into number
        }
        return x;
    }

    // I don't really know the ranges for these values, so we will (sloppily) keep trace of them this
    // way so we can (sloppily) scale the data to fit into the window
    int maxX;
    int maxY;

    // This is where the meat is. Try your luck to decode these packets!

    public void renderPacket( BufferedImage i  , byte[] p) {

        // These are the beginning of each packet before the point blocks
        lastPacketTimeStamp = bytes2long(p, 8);
        lastPacketFrameNumber = bytes2int(p, 4);

        // now lets process the points

        final int endOfBlocksOffset = 0x53d -0x2a;    // Found just looking at the packets in Wireshark (the 0x2a is the UDP header len)

        final int blockOffset = 19;      // First block seems to start at 18th byte of UDP data section
        final int blocSize    = 8;       // Each block seems to be 8 bytes wide

        int o = blockOffset;    // start processing blocks here

        // keep processing blocks until we reach the end (found by looking at packets, there is some non-block data at the end)

        while (o< endOfBlocksOffset ) {

            byte[] block = Arrays.copyOfRange(p, o, o + blocSize);

            int l = bytes2word( block , 0 );        // This value is 0-7 and seems to be laster index? We probably need to apply a correct offset based on this.

            int x = bytes2word( block , 3);
            int y = bytes2word( block , 5 );
            int r = byte2int( block , 7 );


            // Don't bother adjusting existing points in imagebuffer when these change, we are getting thousands of packets a second, will fix itself soon

            if (x>maxX) maxX=x;   // +1 so we stay inside the window edge
            if (y>maxY) maxY=y;

            // Map the points to fit into the image buffer
            int mappedX = (int) ((( x * 1.0 ) / (maxX +1) ) * i.getWidth());
            int mappedY = (int) ((( y * 1.0 ) / (maxY +1)  ) * i.getHeight());

            // Draw the point onto the pending image buffer, which will get displayed at the next paint
            // It thought using a green to red heatmap would help see patterns, but does not seem to

            if (r > 0) {
                i.setRGB(mappedX, mappedY, ((0xff - r) << 8) | (r << 16));
            } else {
                // r=0 seems to have some meaning (lots of them) so show as blue (Doesn't really help)
                i.setRGB(mappedX, mappedY, 0x0000ff);
            }

            o+=blocSize;

        }


    }



    // Replace default update that clears the window each time with white since it makes everything flickery
    public void update(Graphics g) {

        paint(g);

    }


    public void showPacket(byte[] packetBuffer ){

        this.packBuffer = packetBuffer;
        this.packetCounter++;

        // This renders the new packet to the image buffer, which will periodically be painted onto the screen by paint
        renderPacket(bufferedImage, packBuffer);

        this.repaint();

    }

}
