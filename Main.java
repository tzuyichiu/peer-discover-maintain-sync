import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Creates a broadcast socket on port 4242 in the local network and launch all
 * the handlers:
 * HelloSender, HelloReceiver, SynReceiver, ListReceiver, DubugReceiver
 * We update our database every 5 seconds.
 */
public class Main
{
    public static void main(String[] args)
    {
        DatagramSocket socket;
        try
        {
            socket = new DatagramSocket(4242);
            socket.setBroadcast(true);
        } 
        catch (SocketException e)
        {
            System.err.println(e);
            return;
        }
        
        SimpleMessageHandler[] handlers = new SimpleMessageHandler[5];
        handlers[0] = new HelloSender();
        handlers[1] = new HelloReceiver();
        handlers[2] = new SynReceiver();
        handlers[3] = new ListReceiver();
        handlers[4] = new DebugReceiver();

        MuxDemuxSimple dm = new MuxDemuxSimple(handlers, socket);
        
        for (int i=0; i<handlers.length; i++)
        {
            handlers[i].setMuxDemux(dm);
            new Thread(handlers[i]).start();
        }

        new Thread(dm).start();

        while (!Thread.interrupted())
        {
            try
            {
                Thread.sleep(5000);
            }
            catch (InterruptedException e)
            {
                System.err.println(e);
                Thread.currentThread().interrupt();
                return;
            }

            String[] oldData = Info.getMyData();
            String[] newData = new String[oldData.length+1];
            for (int i=0; i<oldData.length; i++)
                newData[i] = oldData[i];
            newData[oldData.length] = "Hey" + oldData.length;

            Info.updateMyDatabase(newData, Info.getMySequenceNo()+1);
        }
    }
}