package edu.brown.cs.student.CreatorFromRowTypes;

import edu.brown.cs.student.UserFacing.FactoryFailureException;
import java.util.List;

/**
 * This interface defines a method that allows your CSV parser to convert each row into an object of
 * some arbitrary passed type.
 *
 * <p>Your parser class constructor should take a second parameter of this generic interface type.
 */
public interface CreatorFromRow<T> {
  T create(List<String> row) throws FactoryFailureException;

  Boolean inRow(Object item, int index);
}
