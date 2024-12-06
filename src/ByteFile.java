
// import static org.junit.Assume.assumeThat;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import student.TestableRandom;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;

/**
 * Basic handling of binary data files.
 * Uses a single byte array as a buffer for disc operations
 * Each record is one long, and one double. Sorting key is the double.
 * A record is 16 bytes long, and there are 512 records per block.
 * 
 * Can be extended in several ways (writeSortedRecords()? readBlock(int)?)
 * 
 * @author CS Staff
 * @version Fall 2024
 */
public class ByteFile {

    /**
     * the number of bytes in one record
     */
    private static final int RECORD_SIZE = 16;
    // public final static int BYTES_PER_RECORD = 16;
    /**
     * the number of bytes in one block
     */
    private static final int BLOCK_SIZE = 8192;
// public final static int BYTES_PER_BLOCK = BYTES_PER_RECORD
// * RECORDS_PER_BLOCK;

    /**
     * the number of records in one block
     */
    private static final int RECORDS_PER_BLOCK = BLOCK_SIZE / RECORD_SIZE;
    // public final static int RECORDS_PER_BLOCK = 512;

    /**
     * number of blocks for heap
     */
    private static final int HEAP_SIZE = 8;

    private String filename;
    private int numBlocks;

    // ----------------------------------------------------------
    /**
     * Create a new ByteFile object.
     *
     * @param filename
     *            file name
     * @param numBlocks
     *            the number of blocks in this file
     */
    public ByteFile(String filename, int numBlocks) {
        this.filename = filename;
        this.numBlocks = numBlocks;
    }


    /**
     * create sorted runs using replacement selection
     * 
     * @param inputFile
     *            input file object
     * @param runFile
     *            run file object
     * 
     * @throws IOException
     */

    public void createSortedRuns(
        RandomAccessFile inputFile,
        RandomAccessFile runFile)
        throws IOException {

        // Initialize the heap and other variables

        // Array to hold the heap
        Record[] heap = new Record[HEAP_SIZE * RECORDS_PER_BLOCK];
        MinHeap<Record> minHeap = new MinHeap<>(heap, 0, HEAP_SIZE
            * RECORDS_PER_BLOCK);

        int runFilesLength = 0;

        long heapSize = 0;
        int fileRecords = (int)inputFile.length() / BLOCK_SIZE
            * RECORDS_PER_BLOCK;

        if (fileRecords / (HEAP_SIZE * RECORDS_PER_BLOCK) >= 1)
            heapSize = HEAP_SIZE * RECORDS_PER_BLOCK;
        else
            heapSize = fileRecords;

        // Fill the heap with initial records from the input file
        for (int i = 0; i < heapSize; i++) {
            if (inputFile.getFilePointer() < inputFile.length()) {
                // Assume this method reads a record from the input file
                Record record = readRecord(inputFile);
                minHeap.insert(record);
            }
        }

        while (true) {

            int otherHeapCount = 0;
            int runFileLength = 0;

            // Array to hold the heap
            Record[] otherHeap = new Record[HEAP_SIZE * RECORDS_PER_BLOCK];

            // number of file's records
            if (runFilesLength == fileRecords)
                break;

            if ((fileRecords - runFilesLength) / (HEAP_SIZE
                * RECORDS_PER_BLOCK) >= 1)
                heapSize = HEAP_SIZE * RECORDS_PER_BLOCK;
            else
                heapSize = fileRecords - runFilesLength;

            Record minRecord = null;
            // Process the heap until it is empty
            while (runFileLength < heapSize) {

                // Step 3b: Read the next record from the input file
                Record nextRecord = null;
                if (inputFile.getFilePointer() < inputFile.length()) {
                    nextRecord = readRecord(inputFile);
                }

                minRecord = minHeap.removeMin();

                runFileLength++;

                writeRecord(runFile, minRecord);
                // Step 3b: Check if the next record can be added to the heap
                if (nextRecord != null && minRecord.compareTo(
                    nextRecord) == -1) {

                    // Insert the next record into the heap
                    minHeap.insert(nextRecord);
                }
                else {
                    if (nextRecord != null) {
                        otherHeap[otherHeapCount] = nextRecord;
                        otherHeapCount++;
                    }
                }
            }
            if (otherHeapCount > 0) {
                for (int i = 0; i < otherHeapCount; i++) {
                    minHeap.insert(otherHeap[i]);
                }
            }

            runFilesLength += runFileLength;
        }
    }


    /**
     * reads a record from the input file
     * 
     * @param file
     *            input file object
     * 
     * @return a new Record object
     * 
     * @throws IOException
     */
    public Record readRecord(RandomAccessFile file) throws IOException {
        long id = file.readLong(); // Read the long ID
        double key = file.readDouble(); // Read the double key
        return new Record(id, key); // Return a new Record object
    }


    /**
     * writes a record to the run file
     * 
     * @param file
     *            run file object
     * @param record
     *            record to write
     * 
     * @throws IOException
     */
    public void writeRecord(RandomAccessFile file, Record record)
        throws IOException {
        file.writeLong(record.getID()); // Write the long ID
        file.writeDouble(record.getKey()); // Write the double key
    }


    /**
     * print the first record from each block
     * 
     * @param outFileName
     *            output file name
     * 
     * @throws IOException
     */
    public void printFirstRecords(String outFileName) throws IOException {
        try (RandomAccessFile sortedFile = new RandomAccessFile(outFileName,
            "r")) {

            byte[] buffer = new byte[BLOCK_SIZE];
            int count = 0;

            // Format without + sign
            DecimalFormat df = new DecimalFormat("0.################E0");

            while (sortedFile.read(buffer) != -1) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                long id = byteBuffer.getLong();
                double key = byteBuffer.getDouble();

                // Use DecimalFormat to format the key without the + sign
                String keyAsString = df.format(key);
                System.out.printf("%d %s ", id, keyAsString);
                count++;
                if (count % 5 == 0) {
                    System.out.println();
                }
            }
        }
    }


    /**
     * merge the sorted runs
     * 
     * @param runFile
     *            run file object
     * @param outputFilename
     *            output file name
     * 
     * @throws IOException
     */
    public void mergeSortedRuns(RandomAccessFile runFile, String outputFilename)
        throws IOException {

        // Create a MinHeap to hold the current minimum elements from each run
        int heapCount = (int)runFile.length() / BLOCK_SIZE * RECORDS_PER_BLOCK;

        Record[] heap = new Record[heapCount]; // Array to hold the heap
        MinHeap<Record> minHeap = new MinHeap<>(heap, 0, heap.length); // Initialize
                                                                       // the
                                                                       // MinHeap

        int runFileCount = 0;
        if (runFile.length() % (BLOCK_SIZE * HEAP_SIZE) == 0)
            runFileCount = (int)runFile.length() / (BLOCK_SIZE * HEAP_SIZE);
        else
            runFileCount = (int)runFile.length() / (BLOCK_SIZE * HEAP_SIZE) + 1;

        long[] currentPosition = new long[runFileCount];
        Record[][] block = new Record[runFileCount][RECORDS_PER_BLOCK
            * HEAP_SIZE];

        for (int i = 0; i < runFileCount; i++) {
            // Seek to the beginning of the runFile to read the sorted runs
            currentPosition[i] = i * RECORDS_PER_BLOCK * HEAP_SIZE;
            int recordsRead = 0;

            // Seek to the current position
            runFile.seek(currentPosition[i] * RECORD_SIZE);
            int heapPos = 0;
            if (i == runFileCount - 1) {
                heapPos = (int)runFile.length() / RECORD_SIZE;
            }
            else {
                heapPos = (i + 1) * HEAP_SIZE * RECORDS_PER_BLOCK;
            }

            // Read records into the block
            while (currentPosition[i] < heapPos) {
                block[i][recordsRead] = readRecord(runFile); // Read a record
                recordsRead++;
                currentPosition[i]++; // Move to the next record
            }
        }
        int[] readedRecords = new int[runFileCount];
        for (int i = 0; i < runFileCount; i++) {
            readedRecords[i] = 0;
        }
        int curPosition = 0;
        while (curPosition < (int)runFile.length() / RECORD_SIZE) {
            Record insertRecord = null;
            int insertIndex = 0;
            int count = 0;
            for (int i = 0; i < runFileCount; i++) {
                int recodeCount = 0;
                if (i == runFileCount - 1) {
                    recodeCount = (int)runFile.length() / RECORD_SIZE - i
                        * HEAP_SIZE * RECORDS_PER_BLOCK;
                }
                else {
                    recodeCount = HEAP_SIZE * RECORDS_PER_BLOCK;
                }

                if (readedRecords[i] < recodeCount) {
                    count++;
                    if (count == 1) {
                        insertRecord = block[i][readedRecords[i]];
                        insertIndex = i;
                    }
                    if (insertRecord.compareTo(
                        block[i][readedRecords[i]]) == 1) {

                        insertRecord = block[i][readedRecords[i]];
                        insertIndex = i;
                    }
                }
            }
            minHeap.insert(insertRecord);
            readedRecords[insertIndex]++;
            curPosition++;
        }

        // Merge the runs

        // Output buffer for writing
        Record[] outputBuffer = new Record[(int)runFile.length() / RECORD_SIZE];
        int outputIndex = 0; // Current index in the output buffer
        while (minHeap.heapSize() > 0) {
            // Get the smallest record from the heap
            Record minRecord = minHeap.removeMin(); // Remove the minimum record
            // Add it to the output buffer
            outputBuffer[outputIndex++] = minRecord;
        }

        RandomAccessFile outputFile = new RandomAccessFile(outputFilename,
            "rw");
        // Write the output buffer to the output file when it's full
        for (int j = 0; j < outputIndex; j++) {
            // Write records to the output file
            writeRecord(outputFile, outputBuffer[j]);
        }

    }
}
