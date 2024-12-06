import java.io.RandomAccessFile;

/**
 * {Project Description Here}
 */

/**
 * The class containing the main method.
 *
 * @author {Your Name Here}
 * @version {Put Something Here}
 */

// On my honor:
//
// - I have not used source code obtained from another student,
// or any other unauthorized source, either modified or
// unmodified.
//
// - All source code and documentation used in my program is
// either my original work, or was derived by me from the
// source code published in the textbook for this course.
//
// - I have not discussed coding details about this project with
// anyone other than my partner (in the case of a joint
// submission), instructor, ACM/UPE tutors or the TAs assigned
// to this course. I understand that I may discuss the concepts
// of this program with other students, and that another student
// may help me debug my program so long as neither of us writes
// anything during the discussion or modifies any computer file
// during the discussion. I have violated neither the spirit nor
// letter of this restriction.

public class Externalsort {

    /**
     * @param args
     *            Command line parameters
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Externalsort <filename>");
            return;
        }

        String filename = args[0];
        ByteFile byteFile = new ByteFile(filename, 16);

        try (RandomAccessFile inputFile = new RandomAccessFile(filename, "rwd");
            RandomAccessFile runFile = new RandomAccessFile(filename
                + "_runfile.dat", "rw")) {
            String outputFileName = filename.substring(0, filename.length() - 4)
                + "_sorted.bin";
            // Step 1: Create sorted runs in a single run file
            byteFile.createSortedRuns(inputFile, runFile);

            // Step 2: Merge the sorted runs
            byteFile.mergeSortedRuns(runFile, outputFileName);

            // Step 3: Print the first record from each block
            byteFile.printFirstRecords(outputFileName);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
