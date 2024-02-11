package Server;

import static spark.Spark.after;

import spark.Spark;
public class Server {

  /**
   * 1. Start your query with the host name:
   * https://api.census.gov/data
   *
   * 2. Add the data year to the URL:
   * https://api.census.gov/data/2019
   *
   * This is the year that the data were estimated.
   *
   * 3. Add the dataset name acronym:
   * https://api.census.gov/data/2019/pep/charagegroups
   *
   * This is the base URL for this dataset. You can find dataset names by browsing the discovery tool: https://api.census.gov/data.html
   *
   * 4. Add ?get= to the query
   * https://api.census.gov/data/2019/pep/charagegroups?get=
   *
   * 5. Add your variables:
   * https://api.census.gov/data/2019/pep/charagegroups?get=NAME,POP
   * @param args
   */
  public static void main(String[] args){
    int port = 3232;
    Spark.port(port);

    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

  }
}
