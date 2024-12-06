import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import student.TestCase;

/**
 * @author {Your Name Here}
 * @version {Put Something Here}
 */
public class ExternalsortTest extends TestCase {
    // ----------------------------------------------------------
    private Externalsort externalsort;

    /**
     * Read contents of a file into a string
     * 
     * @param path
     *            File name
     * @return the string
     * @throws IOException
     */
    static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded);
    }


    /**
     * set up for tests
     */
    public void setUp() {
        // nothing to set up.
        externalsort = new Externalsort();
    }


    /**
     * T
     */
    public void testExternalsort() throws Exception {

        // String[] args = { "solutionTestData/sampleInput16.bin" };
        String[] args = { "solutionTestData/sampleInput16.bin" };
        externalsort.main(args);

        // Actual output from your System console
        String actualOutput = systemOut().getHistory();

        // Expected output from file
        String expectedOutput = readFile(
            "solutionTestData/Expected_Std_Out.txt");

        // Compare the two outputs
        // once you have implemented your project

        assertFuzzyEquals(expectedOutput, actualOutput);
    }

}
