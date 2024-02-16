package edu.brown.cs.student.Parser;

import edu.brown.cs.student.CreatorFromRowTypes.CreatorFromRow;
import edu.brown.cs.student.UserFacing.FactoryFailureException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Class to take in a Reader and parse CSV data into a chosen T class
 *
 * @param <T>
 */
public class CSVParser<T> {
  static final Pattern regexSplitCSVRow =
      Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");
  private List<String> headers = null;
  private List<String> errors = new ArrayList<>();
  private boolean header;
  private CreatorFromRow<T> type;
  public List<T> parsed;

  /**
   * Constructor to initialize variables and call parse() to parse the data
   *
   * @param header
   * @param type
   * @param reader
   * @throws FactoryFailureException
   */
  public CSVParser(boolean header, CreatorFromRow<T> type, Reader reader)
      throws FactoryFailureException {
    this.type = type;
    this.header = header;
    this.parse(reader);
  }

  /**
   * Parses through the CSV with a given Reader and tosses malformed rows. Converts row data into
   * Class T
   *
   * @param reader
   * @throws FactoryFailureException
   */
  public void parse(Reader reader) throws FactoryFailureException {
    int row = 1;
    List<T> res = new ArrayList<>();
    String line;
    try {
      boolean first = true;
      int averageLength = 0;
      BufferedReader bufferedReader = new BufferedReader(reader);
      line = bufferedReader.readLine();
      while (line != null) {
        if (first) {
          averageLength = regexSplitCSVRow.split(line).length;
        }
        try {
          if (this.header) {
            this.headers = new ArrayList<>();
            this.addToList(line, averageLength, res, this.headers, 1);
            this.header = false;
          } else {
            List<String> lineWords = new ArrayList<>();
            this.addToList(line, averageLength, res, lineWords, row);
          }
        } catch (FactoryFailureException e) {
          this.errors.add(e.getMessage());
          throw new FactoryFailureException(
              "Row " + row + e.getMessage(), Arrays.asList(regexSplitCSVRow.split(line)));
        }
        if (first) {
          first = false;
        }
        line = bufferedReader.readLine();
        row++;
      }
      bufferedReader.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.parsed = res;
  }

  /**
   * Gets the header list
   *
   * @return
   */
  public List<String> getHeaders() {
    return this.headers;
  }

  /**
   * Gets the parsed list
   *
   * @return
   */
  public List<T> getParsed() {
    return this.parsed;
  }

  /**
   * Gets the error List
   *
   * @return
   */
  public List<String> getErrors() {
    return this.errors;
  }

  /**
   * Helper method to transform CSV line data into Class T
   *
   * @param line
   * @param averageLength
   * @param res
   * @param array
   * @throws FactoryFailureException
   */
  private void addToList(String line, int averageLength, List<T> res, List<String> array, int row)
      throws FactoryFailureException {
    for (String word : regexSplitCSVRow.split(line)) {
      array.add(postprocess(word));
    }
    if (array.size() != averageLength) {
      try {
        this.type.create(array);
      } catch (FactoryFailureException e) {
        this.errors.add(e.getMessage());
        throw e;
      }
      this.errors.add("Row " + row + " has missing/extra values, skipping line");
      System.out.println("Row " + row + " has missing/extra values, skipping line");
    } else {
      res.add(this.type.create(array));
    }
  }

  /**
   * Assists the regex in accurately splitting CSV Lines
   *
   * @param arg
   * @return
   */
  public static String postprocess(String arg) {
    return arg
        // Remove extra spaces at beginning and end of the line
        .trim()
        // Remove a beginning quote, if present
        .replaceAll("^\"", "")
        // Remove an ending quote, if present
        .replaceAll("\"$", "")
        // Replace double-double-quotes with double-quotes
        .replaceAll("\"\"", "\"");
  }
}
