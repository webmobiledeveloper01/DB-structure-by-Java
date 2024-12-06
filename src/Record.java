import java.math.BigInteger;

/**
 * Holds a single record
 *
 * @author CS Staff
 * @version Fall 2024
 */
public class Record implements Comparable<Record> {
    /**
     * 16 bytes per record
     */
    public static final int BYTES = 16;

    private long recID;
    private double key;

    /**
     * The constructor for the Record class
     *
     * @param recID
     *            record ID
     * @param key
     *            record key
     */
    public Record(long recID, double key) {
        this.recID = recID;
        this.key = key;
    }


    // ----------------------------------------------------------
    /**
     * Return the ID value from the record
     *
     * @return record ID
     */
    public long getID() {
        return recID;
    }


    // ----------------------------------------------------------
    /**
     * Return the key value from the record
     *
     * @return record key
     */
    public double getKey() {
        return key;
    }


    // ----------------------------------------------------------
    /**
     * Compare two records based on their keys
     *
     * @return int
     */
    @Override
    public int compareTo(Record toBeCompared) {
        String str1;
        String str2;
        str1 = Long.toHexString(Double.doubleToLongBits(this.key));
        str2 = Long.toHexString(Double.doubleToLongBits(toBeCompared.key));

        // Convert str1 to BigInteger
        BigInteger num1 = new BigInteger(str1, 16);

        // Convert str2 to BigInteger
        BigInteger num2 = new BigInteger(str2, 16);
        return num1.compareTo(num2); // Compare the two BigIntegers
    }
}
