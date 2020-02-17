/**
 * Database is a class containing data and the associated sequence number 
 * indicating its version. This will be instanciated for my own machine and
 * all the peers in my PeerTable.
 * The database will then be a part of the class "Info" as well as "PeerRecord".
 */
public class Database
{
    public Database()
    {
        this.data = new String[0];
        this.sequenceNo = -1;
    }
    
    /**
     * data is a table of strings where each string contains [0;255] characters.
     */
    private String[] data;

    /**
     * sequenceNo will be incremented whenever the table is updated.
     */
    private int sequenceNo;

    public int getDatabaseSequenceNo()
    {
        return this.sequenceNo;
    }

    public String[] getData()
    {
        return this.data;
    }

    public void updateDatabase(String[] newData, int newSequenceNo)
    {
        this.data = newData;
        this.sequenceNo = newSequenceNo;
    }
}