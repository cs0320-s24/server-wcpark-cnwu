package edu.brown.cs.student.CreatorFromRowTypes;

import java.util.List;

/** Structures the data into a List<String> */
public class CSVString implements CreatorFromRow {
  private List<String> row;

  /**
   * Creates and returns a CSVString after setting its List<String> value
   *
   * @param row
   * @return
   */
  @Override
  public CSVString create(List row) {
    CSVString res = new CSVString();
    res.setRow(row);
    return res;
  }

  /**
   * Finds an item at a specific index, or finds it within the entire row
   *
   * @param item
   * @param index
   * @return
   */
  @Override
  public Boolean inRow(Object item, int index) {
    try {
      if (index != -1) {
        return this.row.get(index).equalsIgnoreCase(item + "");
      } else
        for (String cur : this.row) {
          if (cur.equalsIgnoreCase(item + "")) {
            return true;
          }
        }
      return false;
    } catch (IndexOutOfBoundsException e) {
      throw new IndexOutOfBoundsException();
    }
  }

  /**
   * Setter method for the row
   *
   * @param row
   */
  private void setRow(List<String> row) {
    this.row = row;
  }
}
