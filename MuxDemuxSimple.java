import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.IOException;

class MuxDemuxSimple implements Runnable
{
    private DatagramSocket mySocket = null;
    private BufferedReader in;
    private SimpleMessageHandler[] myMessageHandlers;
    private SynchronizedQueue outgoing = new SynchronizedQueue(20);

    private Thread senderThread;
    private Thread receiverThread;

    MuxDemuxSimple (SimpleMessageHandler[] h, DatagramSocket s)
    {
        mySocket = s;
        myMessageHandlers = h;
    }

    public void run()
    {    
        byte[] recvedByteArray = new byte[2048];
        DatagramPacket dpRecved = new DatagramPacket(
            recvedByteArray, recvedByteArray.length);
        
        for (int i=0; i<myMessageHandlers.length; i++)
            this.myMessageHandlers[i].setMuxDemux(this);
        
        this.receiverThread = Thread.currentThread();
        
        // senderThread
        this.senderThread = new Thread(()->
        {
            while (!Thread.interrupted())
            {        
                String toSend;
                try
                {
                    toSend = this.outgoing.dequeue();
                }
                catch (InterruptedException e)
                {
                    System.err.println(e);
                    Thread.currentThread().interrupt();
                    return;
                }

                byte[] byteArray = toSend.getBytes();
                
                try
                {
                    DatagramPacket dpToSend = new DatagramPacket(
                        byteArray, byteArray.length, 
                        InetAddress.getByName("255.255.255.255"), 4242);
                    
                    this.mySocket.send(dpToSend);
                }
                catch (IOException e)
                {
                    System.err.println(e);
                    this.receiverThread.interrupt();
                    return;
                }
            }
        });
        this.senderThread.start();
        
        // receiveThread
        try
        {    
            while (!Thread.interrupted())
            {
                this.mySocket.receive(dpRecved);
                String recved = new String(
                    dpRecved.getData(), 0, dpRecved.getLength());

                String ip = dpRecved.getAddress().toString().split(":")[0].split("/")[1];
                
                this.handleMessage(recved, ip);
                
                // clear the buffer before receiving next packet
                recvedByteArray = new byte[2048];
            }
        }
        catch (IOException e)
        {
            System.err.println(e);
            senderThread.interrupt();
        }
        		
        try
        {
            in.close(); 
            mySocket.close();
        }
        catch (IOException e)
        { 
            System.err.println(e);
            senderThread.interrupt();
        }
    }

    public void handleMessage(String message, String source)
    {
        for (int i=0; i<this.myMessageHandlers.length; i++)
            this.myMessageHandlers[i].handleMessage(message, source);
    }
    
    public void send(String s)
    {
        try
        {
            outgoing.enqueue(s);
        }
        catch (InterruptedException e)
        {
            System.err.println(e);
            senderThread.interrupt();
        }
    }
}