import java.util.HashMap;
import java.util.Set;

/**
 * The "static" class Info contains 
 *  - all useful information about THIS machine 
 *  - methods so that the message handlers will be able to modify or get 
 *    these information (ID, sequenceNo, helloInterval, peerTable).
 */
public class Info
{
    public final static boolean DEBUG = true;
    
    private final static String TAG = "Info          - ";
    private final static Object lock = new Object();

    private final static String myID = "TzuyiDell"; //I tested with my another machine "TzuyiHP"
    private final static int myHelloInterval = 30;

    /** 
     * Our peerTable is a dictionnary (HashMap) of peerRecords.
     * We use the peers' ID to map the corresponding peerRecord.
     */
    public static HashMap<String, PeerRecord> myPeerTable = 
        new HashMap<String, PeerRecord>();

    private static Database myDatabase = new Database();

    /**
     * private constructor because it makes no sense to instantiate this class.
     */
    private Info(){};

    public static String getMyID()
    {
        return myID;
    }

    public static int getMyHelloInterval()
    {
        return myHelloInterval;
    }

    public static int getMySequenceNo()
    {
        return myDatabase.getDatabaseSequenceNo();
    }

    public static String[] getMyData()
    {
        return myDatabase.getData();
    }
    
    public static void updateMyDatabase(String[] newData, int newSequenceNo)
    {
        myDatabase.updateDatabase(newData, newSequenceNo);
    }

    /**
     * verifyRecordValid is a mandatory method that verifies that peers aren't
     * expired. Called whenever we try to access our PeerTable (actually in 
     * every methods below).
     */
    private static void verifyRecordValid(String peerID)
    {
        synchronized (lock)
        {
            PeerRecord peerRecord = myPeerTable.get(peerID);

            if (peerRecord == null)
                return;
            
            long peerExpirationTime = peerRecord.getExpirationTime();
            if (System.currentTimeMillis() > peerExpirationTime)
            {
                myPeerTable.remove(peerID);
                if (Info.DEBUG)
                    System.out.println(TAG + peerID + ": PeerRecord deleted");
            }
        }
    }
    
    public static PeerRecord getPeerRecord(String peerID)
    {
        synchronized (lock)
        {    
            verifyRecordValid(peerID);
            return myPeerTable.get(peerID);
        }
    }

    public static Set<String> getPeerSet()
    {
        synchronized (lock)
        {
            Set<String> peerIDs = myPeerTable.keySet();
        
            for (String peerID : peerIDs)
                verifyRecordValid(peerID);
        
            return myPeerTable.keySet();
        }
    }

    public static boolean inPeers(String peerID)
    {
        Set<String> peerSet = getPeerSet();
        for (String peer : peerSet)
        {
            if (peer.equals(peerID))
                return true;
        }
        return false;
    }

    public static void updatePeerTable(HelloMessage hm, String ip)
    {
        String senderID = hm.getSenderID();
        int senderSequenceNo = hm.getSequenceNo();
        int senderHelloInterval = hm.getHelloInterval();

        synchronized (lock)
        {
            PeerRecord peerRecord = getPeerRecord(senderID);
            if (peerRecord == null)
            {
                peerRecord = new PeerRecord(senderID, ip, senderHelloInterval);
                myPeerTable.put(senderID, peerRecord);
                if (DEBUG)
                    System.out.println(TAG + senderID + ": PeerRecord created");
            }

            if (hm.inPeers(myID))
            {
                PeerState peerState = peerRecord.getPeerState();
                int peerSequenceNo  = peerRecord.getPeerSequenceNo();
                
                if (peerSequenceNo != senderSequenceNo)
                {
                    if (peerState != PeerState.INCONSISTENT)
                    {
                        peerRecord.setPeerState(PeerState.INCONSISTENT);
                        if (Info.DEBUG)
                            System.out.println(TAG + senderID + ": " + 
                                peerState + " -> INCONSISTENT");
                    }
                }
                else
                {
                    if (peerState != PeerState.SYNCHRONIZED)
                    {
                        peerRecord.setPeerState(PeerState.SYNCHRONIZED);
                        if (Info.DEBUG)
                            System.out.println(TAG + senderID + ": " + 
                                peerState + " -> SYNCHRONIZED");
                    }
                }
            }
        }
    }

    public static void updatePeerDatabase(String peerID, String[] newData, 
                                            int newSequenceNo)
    {
        synchronized (lock)
        {
            PeerRecord peerRecord = getPeerRecord(peerID);
            if (peerRecord != null)
                peerRecord.updatePeerDatabase(newData, newSequenceNo);
        }   
    }
}