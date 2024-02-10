package edu.brown.cs.student.SearcherTests;

import static org.testng.AssertJUnit.assertEquals;

import edu.brown.cs.student.CreatorFromRowTypes.CSVHashmap;
import edu.brown.cs.student.CreatorFromRowTypes.CSVStar;
import edu.brown.cs.student.CreatorFromRowTypes.CSVString;
import edu.brown.cs.student.CreatorFromRowTypes.CreatorFromRow;
import edu.brown.cs.student.Parser.CSVParser;
import edu.brown.cs.student.UserFacing.CSVSearcher;
import edu.brown.cs.student.UserFacing.FactoryFailureException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tester class for CSVSearcher class */
public class SearcherTest {

  /**
   * Tests that the searcher can properly search for items in the parsed list and also handle taking
   * in multiple classes that implement CreatorFromRow
   *
   * @throws FileNotFoundException
   * @throws FactoryFailureException
   */
  @Test
  public void testSearchStars() throws FileNotFoundException, FactoryFailureException {
    CSVParser<CreatorFromRow<CSVString>> rowParser =
        new CSVParser<CreatorFromRow<CSVString>>(
            true,
            new CSVString(),
            new FileReader(
                "C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\csv-willimprkBrown\\data"
                    + "\\stars\\stardata.csv"));
    CSVSearcher<String, String, CSVString> rowSearcher = new CSVSearcher<>(rowParser);

    // Tests for valid info
    assertEquals(rowSearcher.search("Sol", "ProperName", rowParser.getHeaders()), List.of(2));

    // Tests for searching for the item with no other parameters
    assertEquals(rowSearcher.search("Sol", null, null), List.of(2));

    // Tests for searching within a specific column
    assertEquals(rowSearcher.search("Sol", "ProperName", rowParser.getHeaders()), List.of(2));
    assertEquals(rowSearcher.search("Sol", "X", null), List.of());

    // Tests for improperly named column identifiers
    assertEquals(rowSearcher.search("Sol", "ProperNam", rowParser.getHeaders()), List.of());

    // Tests for custom headers
    assertEquals(rowSearcher.search("Sol", "ProperName", List.of(".", "ProperName")), List.of(2));

    // Tests for leaving inputs empty/null
    assertEquals(rowSearcher.search(null, null, null), List.of());

    // Similar tests but using the CSVStar class as the CreatorFromRow
    CSVParser<CreatorFromRow<CSVStar>> starParser =
        new CSVParser<CreatorFromRow<CSVStar>>(
            true,
            new CSVStar(),
            new FileReader(
                "C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\csv-willimprkBrown\\data"
                    + "\\stars\\stardata.csv"));
    CSVSearcher<String, Integer, CSVStar> starSearcher = new CSVSearcher<>(starParser);

    assertEquals(starSearcher.search("282.43485", null, null), List.of(3));
    assertEquals(starSearcher.search("282.43485", 2, rowParser.getHeaders()), List.of(3));
    assertEquals(starSearcher.search("281.43485", null, null), List.of());
    assertEquals(starSearcher.search("282.43485", 4, null), List.of());
    assertEquals(starSearcher.search(null, null, null), List.of());
  }

  /**
   * Tests that the searcher can work with different files/parsed lists
   *
   * @throws FileNotFoundException
   * @throws FactoryFailureException
   */
  @Test
  void testSearchIncomeByRace() throws FileNotFoundException, FactoryFailureException {
    CSVParser<CreatorFromRow<CSVHashmap>> parser =
        new CSVParser<CreatorFromRow<CSVHashmap>>(
            true,
            new CSVHashmap(),
            new FileReader(
                "C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\csv-willimprkBrown\\data"
                    + "\\census\\income_by_race.csv"));
    CSVSearcher<String, String, CSVHashmap> searcher = new CSVSearcher<>(parser);
    assertEquals(
        searcher.search("Kent County, RI", "Geography", parser.getHeaders()),
        List.of(
            3, 8, 13, 19, 24, 28, 33, 38, 43, 48, 52, 59, 64, 67, 71, 76, 81, 86, 90, 97, 102, 105,
            109, 114, 119, 124, 128, 135, 140, 144, 148, 153, 158, 163, 167, 174, 179, 184, 189,
            194, 199, 204, 208, 212, 217, 221, 224, 229, 234, 239, 244, 249, 253, 258, 263, 268,
            273, 278, 283, 288, 292, 296, 301, 306, 311, 316, 321));
  }

  /**
   * Tests for different search input types - Integer vs String Also tests index out of bounds when
   * input type is Integer for the item to search
   *
   * @throws FileNotFoundException
   * @throws FactoryFailureException
   */
  @Test
  void testSearcherInputs() throws FileNotFoundException, FactoryFailureException {
    CSVParser<CreatorFromRow<CSVString>> parser =
        new CSVParser<CreatorFromRow<CSVString>>(
            true,
            new CSVString(),
            new FileReader(
                "C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\csv-willimprkBrown\\data"
                    + "\\stars\\stardata.csv"));
    CSVSearcher<String, String, CSVString> searcher = new CSVSearcher<>(parser);
    assertEquals(searcher.search("Andreas", "ProperName", parser.getHeaders()), List.of(3));

    CSVSearcher<Integer, String, CSVString> searcherInteger = new CSVSearcher<>(parser);
    assertEquals(searcherInteger.search(15, "StarID", parser.getHeaders()), List.of(17));
    assertEquals(searcherInteger.search(-100, "StarID", parser.getHeaders()), List.of());
    assertEquals(searcherInteger.search(1000000000, "StarID", parser.getHeaders()), List.of());
  }
}
