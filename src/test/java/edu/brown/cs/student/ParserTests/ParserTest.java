package edu.brown.cs.student.ParserTests;

import static org.testng.AssertJUnit.assertEquals;

import edu.brown.cs.student.CreatorFromRowTypes.CSVStar;
import edu.brown.cs.student.CreatorFromRowTypes.CSVString;
import edu.brown.cs.student.CreatorFromRowTypes.CreatorFromRow;
import edu.brown.cs.student.Parser.CSVParser;
import edu.brown.cs.student.UserFacing.CSVSearcher;
import edu.brown.cs.student.UserFacing.FactoryFailureException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tester class for the CSVParser Class */
public class ParserTest {

  /**
   * Tests if the file is parsed through correctly and searcher is able to locate requested items
   *
   * @throws FileNotFoundException
   * @throws FactoryFailureException
   */
  @Test
  public void ParseValid() throws FileNotFoundException, FactoryFailureException {
    CSVParser<CreatorFromRow<CSVString>> parser =
        new CSVParser<CreatorFromRow<CSVString>>(
            true,
            new CSVString(),
            new FileReader(
                "C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\csv-willimprkBrown\\data"
                    + "\\census\\dol_ri_earnings_disparity.csv"));
    CSVSearcher<String, String, CSVString> searcher = new CSVSearcher(parser);
    assertEquals(searcher.search("395773.6521", "3", parser.getHeaders()), List.of(2));
    assertEquals(searcher.search("6%", "Employed Percent", parser.getHeaders()), List.of(3));
    assertEquals(searcher.search("RI", null, parser.getHeaders()), List.of(2, 3, 4, 5, 6, 7));

    for(CreatorFromRow<CSVString> s : parser){
      System.out.println(s);
    }

  }
  /**
   * Tests if the parser can handle malformed data
   *
   * @throws FileNotFoundException
   * @throws FactoryFailureException
   */
  @Test
  public void ParseMalformed() throws FileNotFoundException, FactoryFailureException {
    assertEquals(
        new CSVParser<>(
                false,
                new CSVString(),
                new FileReader(
                    "C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\csv-willimprkBrown\\data"
                        + "\\malformed\\malformed_signs.csv"))
            .getErrors(),
        List.of(
            "Row 4 has missing/extra values, skipping line",
            "Row 7 has missing/extra values, skipping line",
            "Row 8 has missing/extra values, skipping line",
            "Row 12 has missing/extra values, skipping line"));
  }

  /**
   * Tests if the parser can handle multiple classes that extend Reader
   *
   * @throws FileNotFoundException
   * @throws FactoryFailureException
   */
  @Test
  void testReaders() throws FileNotFoundException, FactoryFailureException {
    CSVParser<CreatorFromRow<CSVString>> parser =
        new CSVParser<CreatorFromRow<CSVString>>(
            true,
            new CSVString(),
            new FileReader(
                "C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\csv-willimprkBrown\\data"
                    + "\\census\\postsecondary_education.csv"));
    CSVSearcher<String, String, CSVString> searcher = new CSVSearcher<>(parser);
    assertEquals(
        searcher.search("Asian", "IPEDS Race", parser.getHeaders()),
        new ArrayList<>(List.of(2, 10)));
    parser =
        new CSVParser<CreatorFromRow<CSVString>>(
            true,
            new CSVString(),
            new StringReader(
                "IPEDS Race,ID Year,Year,ID University,University,Completions,Slug University,share,Sex,ID Sex\n"
                    + "Asian,2020,2020,217156,Brown University,214,brown-university,0.069233258,Men,1\n"
                    + "Black or African American,2020,2020,217156,Brown University,77,brown-university,0.024911032,Men,1\n"
                    + "Native Hawaiian or Other Pacific Islanders,2020,2020,217156,Brown University,3,brown-university,0.00097056,Men,1\n"
                    + "Hispanic or Latino,2020,2020,217156,Brown University,143,brown-university,0.046263345,Men,1\n"
                    + "Two or More Races,2020,2020,217156,Brown University,58,brown-university,0.018764154,Men,1\n"
                    + "American Indian or Alaska Native,2020,2020,217156,Brown University,4,brown-university,0.00129408,Men,1\n"
                    + "Non-resident Alien,2020,2020,217156,Brown University,327,brown-university,0.105791006,Men,1\n"
                    + "White,2020,2020,217156,Brown University,691,brown-university,0.223552248,Men,1\n"
                    + "Asian,2020,2020,217156,Brown University,235,brown-university,0.076027176,Women,2\n"
                    + "Black or African American,2020,2020,217156,Brown University,95,brown-university,0.03073439,Women,2\n"
                    + "Native Hawaiian or Other Pacific Islanders,2020,2020,217156,Brown University,4,brown-university,0.00129408,Women,2\n"
                    + "Hispanic or Latino,2020,2020,217156,Brown University,207,brown-university,0.066968619,Women,2\n"
                    + "Two or More Races,2020,2020,217156,Brown University,85,brown-university,0.027499191,Women,2\n"
                    + "American Indian or Alaska Native,2020,2020,217156,Brown University,7,brown-university,0.002264639,Women,2\n"
                    + "Non-resident Alien,2020,2020,217156,Brown University,281,brown-university,0.090909091,Women,2\n"
                    + "White,2020,2020,217156,Brown University,660,brown-university,0.213523132,Women,2"));
    searcher = new CSVSearcher<>(parser);
    assertEquals(
        searcher.search("Asian", "IPEDS Race", parser.getHeaders()),
        new ArrayList<>(List.of(2, 10)));
  }

  /**
   * Tests that the regex in CSVParser can handle special cases
   *
   * @throws FileNotFoundException
   * @throws FactoryFailureException
   */
  @Test
  void testRegex() throws FileNotFoundException, FactoryFailureException {
    CSVParser<CreatorFromRow<CSVString>> parser =
        new CSVParser<CreatorFromRow<CSVString>>(
            false,
            new CSVString(),
            new FileReader(
                "C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\csv-willimprkBrown\\data"
                    + "\\special cases\\regex_test.csv"));
    CSVSearcher<String, String, CSVString> searcher = new CSVSearcher<>(parser);
    assertEquals(searcher.search("veni, vidi, vici", null, null), List.of(1));
    assertEquals(searcher.search("\"hello\"", null, null), List.of(2));
  }

  @Test
  void testFactoryFailure() throws FileNotFoundException {
    try {
      CSVParser<CreatorFromRow<CSVStar>> parser =
          new CSVParser<CreatorFromRow<CSVStar>>(
              false,
              new CSVStar(),
              new FileReader(
                  "C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\csv-willimprkBrown\\data"
                      + "\\malformed\\malformed_stars.csv"));
    } catch (FactoryFailureException e) {
      assertEquals(
          e.getMessage(), "Row 3 contains extra Star data that cannot be processed into CSVStar");
    }
  }
}
