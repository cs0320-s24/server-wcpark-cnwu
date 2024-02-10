package edu.brown.cs.student.UserFacing;

import edu.brown.cs.student.CreatorFromRowTypes.CreatorFromRow;
import edu.brown.cs.student.Parser.CSVParser;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to allow the user to search through the CSV data parsed through by the CSVParser.
 *
 * @param <T> A generic for the class of the term the user is searching for
 * @param <C> A generic for the class of the given column identifier
 * @param <V> A generic for the class the data should be parsed into
 */
public class CSVSearcher<T, C, V> {
  private List<CreatorFromRow<V>> parsed;

  /**
   * Constructor for CSVSearcher
   *
   * @param parser - parses the data into a list of V
   */
  public CSVSearcher(CSVParser<CreatorFromRow<V>> parser) {
    // Pass in the type of Reader to use for parsing
    this.parsed = parser.getParsed();
  }

  /**
   * Searches for the item requested by the user in the parsed list
   *
   * @param item - to be searched for
   * @param colIdentifier - given to specific the column the item should be found in
   * @param headers - headers in the CSV
   * @return
   */
  public List<Integer> search(T item, C colIdentifier, List<String> headers) {
    String colId = colIdentifier + "";
    int rowNum = 1;
    int index = -1;

    if (colIdentifier != null) {
      try {
        // Is an integer
        index = Integer.parseInt(colId);
        if (index < 0 && colIdentifier != null) {
          System.err.println("Index out of bounds");
          return List.of();
        }

        // Is not an integer (String)
      } catch (NumberFormatException e) {
        if (headers != null) {
          if (!this.checkInList(headers, colId)) {
            System.err.println("Header does not exist");
            return List.of();
          }
          index = headers.indexOf(colId);
        } else {
          return List.of();
        }
      }
    }
    List<Integer> rows = new ArrayList<>();
    for (CreatorFromRow<V> row : this.parsed) {
      try {
        if (row.inRow(item, index)) {
          rows.add(rowNum);
        }
      } catch (IndexOutOfBoundsException e) {
        System.err.println("Index out of bounds");
        return List.of();
      }
      rowNum += 1;
    }
    return rows;
  }

  /**
   * Helper to check if a column identifier is in headers
   *
   * @param list
   * @param item
   * @return
   */
  private boolean checkInList(List<String> list, String item) {
    for (String word : list) {
      if (item.equalsIgnoreCase(word)) {
        return true;
      }
    }
    return false;
  }
}
