package edu.brown.cs.student.CreatorFromRowTypes;

import edu.brown.cs.student.UserFacing.FactoryFailureException;
import java.util.List;

/** Structures the data into an Array representing star data. */
public class CSVStar implements CreatorFromRow {
  private String[] data = new String[5];

  /**
   * Returns a new CSVStar that has its Array set to the updated data Array
   *
   * @param row
   * @return
   * @throws FactoryFailureException
   */
  @Override
  public CSVStar create(List row) throws FactoryFailureException {
    String[] data = new String[5];
    CSVStar star = new CSVStar();
    int index = 0;
    try {
      for (Object item : row) {
        data[index] = item + "";
        index++;
      }
    } catch (IndexOutOfBoundsException e) {
      throw new FactoryFailureException(
          " contains extra Star data that cannot be processed into CSVStar", row);
    }
    star.setArray(data);
    return star;
  }

  /**
   * Finds the item in the row in a given index, or searches the entire row for the item
   *
   * @param item
   * @param index
   * @return
   */
  @Override
  public Boolean inRow(Object item, int index) {
    try {
      if (index != -1) {
        return this.data[index].equalsIgnoreCase(item + "");
      } else {
        for (int i = 0; i < 5; i++) {
          if (this.data[i].equalsIgnoreCase(item + "")) {
            return true;
          }
        }
      }
      return false;
    } catch (IndexOutOfBoundsException e) {
      throw new IndexOutOfBoundsException();
    }
  }

  /**
   * Setter method for the data Array
   *
   * @param data
   */
  private void setArray(String[] data) {
    this.data = data;
  }
}
