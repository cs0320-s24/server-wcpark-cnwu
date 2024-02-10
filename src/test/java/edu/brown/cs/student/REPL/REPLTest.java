package edu.brown.cs.student.REPL;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.io.File;
import org.junit.jupiter.api.Test;

/** Class to test the REPL in edu.brown.cs.student.UserFacing.Main */
public class REPLTest {

  /** Tests if extractFilenames() can accurately find files */
  @Test
  void testFileExtraction() {
    String filepath;
    File directoryPath =
        new File("C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\csv-willimprkBrown\\data");

    filepath = extractFilenames(directoryPath, "stardata" + ".csv");
    assertEquals(
        filepath,
        "C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\csv-willimprkBrown\\data\\stars\\stardata.csv");

    filepath = extractFilenames(directoryPath, "stardatas" + ".csv");
    assertNull(filepath);
  }

  /** Tests that the REPL can handle properly formatted inputs as instructed in the REPL. */
  @Test
  void testInput() {
    String input = "Sol 1";
    String[] inputs = input.split(" ");
    String secondInput = null;
    String firstInput = inputs[0];
    if (inputs.length >= 2) {
      secondInput = inputs[1];
    }
    assertEquals(firstInput, "Sol");
    assertEquals(secondInput, "1");

    input = "Sol 1 1";
    inputs = input.split(" ");
    secondInput = null;
    firstInput = inputs[0];
    if (inputs.length >= 2) {
      secondInput = inputs[1];
    }
    assertEquals(firstInput, "Sol");
    assertEquals(secondInput, "1");

    input = "Sol,1";
    inputs = input.split(" ");
    secondInput = null;
    firstInput = inputs[0];
    if (inputs.length >= 2) {
      secondInput = inputs[1];
    }
    assertEquals(firstInput, "Sol,1");
    assertNull(secondInput);
  }

  /**
   * Recursively goes through the protected directories and extracts the filenames
   *
   * @param directory
   * @param target
   * @return
   */
  private String extractFilenames(File directory, String target) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          String result = extractFilenames(file, target);
          if (result != null) {
            return result;
          }
        } else if (file.getName().equalsIgnoreCase(target)) {
          return file.getAbsolutePath();
        }
      }
    }
    return null;
  }
}
