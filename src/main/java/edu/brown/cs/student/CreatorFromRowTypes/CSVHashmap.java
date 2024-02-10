package edu.brown.cs.student.CreatorFromRowTypes;

import java.util.HashMap;
import java.util.List;

/**
 * Structures the data into a HashMap which has keys representing the index and then the value is
 * the item
 */
public class CSVHashmap implements CreatorFromRow {
  private HashMap<Integer, String> map;

  /**
   * Returns a CSVHashmap which has its map variable set
   *
   * @param row
   * @return
   */
  @Override
  public CSVHashmap create(List row) {
    HashMap<Integer, String> map = new HashMap<>();
    CSVHashmap res = new CSVHashmap();
    for (int i = 0; i < row.size(); i++) {
      map.put(i, row.get(i) + "");
    }
    res.setMap(map);
    return res;
  }

  /**
   * Finds the item in the HashMap using the index, or searches all the values in the HashMap
   *
   * @param item
   * @param index
   * @return
   */
  @Override
  public Boolean inRow(Object item, int index) {
    try {
      if (index != -1) {
        return this.map.get(index).equalsIgnoreCase(item + "");
      }
      for (int key : this.map.keySet()) {
        if (this.map.get(key).equalsIgnoreCase(item + "")) {
          return true;
        }
      }
      return false;
    } catch (IndexOutOfBoundsException e) {
      throw new IndexOutOfBoundsException();
    }
  }

  /**
   * Setter method for the map
   *
   * @param map
   */
  private void setMap(HashMap<Integer, String> map) {
    this.map = map;
  }
}
