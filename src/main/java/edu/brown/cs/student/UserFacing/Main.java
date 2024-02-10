package edu.brown.cs.student.UserFacing;

import edu.brown.cs.student.CreatorFromRowTypes.CSVString;
import edu.brown.cs.student.CreatorFromRowTypes.CreatorFromRow;
import edu.brown.cs.student.Parser.CSVParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

/**
 * The edu.brown.cs.student.UserFacing.Main class of our project. This is where execution begins.
 */
public final class Main {
  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   */
  public static void main(String[] args) throws FileNotFoundException, FactoryFailureException {
    new Main(args).run();
  }

  private Main(String[] args) {}

  /**
   * Starts a REPL that allows the user to repeatedly search for value in a given file
   *
   * @throws FileNotFoundException
   * @throws FactoryFailureException
   */
  private void run() throws FileNotFoundException, FactoryFailureException {
    Scanner scanner = new Scanner(System.in);
    System.out.println(
        "Welcome! Type \"quit\" to exit the REPL, or \"restart\" to restart the REPL");
    String filepath;
    String input;
    System.out.println(
        "Please enter the absolute file path to the directory where the files "
            + " you want to search are located");
    String directory = scanner.nextLine();
    while (true) {
      System.out.println(
          "What file do you want to search? (ex. \"stardata\" to search stardata.csv)");
      input = scanner.nextLine();
      if (input.equalsIgnoreCase("quit")) {
        System.out.println("Quitting the REPL...");
        System.exit(0);
      }
      if (input.equalsIgnoreCase("restart")) {
        System.out.println("Restarting the REPL...");
        this.run();
      }
      File directoryPath = new File(directory);

      filepath = this.extractFilenames(directoryPath, input + ".csv");
      if (filepath != null) {
        System.out.println("File found in: " + filepath);
        break;
      } else {
        System.err.println("File not found");
      }
    }
    System.out.println("Parsing...");
    // Create a CSVParser with the file you want to search
    CSVParser<CreatorFromRow<CSVString>> rowParser =
        new CSVParser<CreatorFromRow<CSVString>>(true, new CSVString(), new FileReader(filepath));

    // Create a CSVSearcher that contains the type of value and column identifier you will be using
    CSVSearcher<String, String, CSVString> rowSearcher = new CSVSearcher<>(rowParser);

    System.out.println(
        "You can enter a string to search for. "
            + "Can also specify column identifier (header or index) separated by a space (ex. \"Sol 1\")");
    while (true) {
      System.out.print("Enter a string to search for: ");
      input = scanner.nextLine();

      if (input.equalsIgnoreCase("quit")) {
        System.out.println("Quitting the REPL...");
        System.exit(0);
      }
      if (input.equalsIgnoreCase("restart")) {
        System.out.println("Restarting the REPL...");
        this.run();
      }
      String[] inputs = input.split(" ");
      String secondInput = null;
      String firstInput = inputs[0];
      if (inputs.length >= 2) {
        secondInput = inputs[1];
      }
      System.out.println(
          "Found in rows: " + rowSearcher.search(firstInput, secondInput, rowParser.getHeaders()));
    }
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
