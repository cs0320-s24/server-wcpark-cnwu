package RIData;


public class CensusData {
  private String state;
  private String stateCode;
  private String county;
  private String countyCode;
  private String percentage;
  public CensusData(){

  }
  @Override
  public String toString(){
    return "Percentage of households with broadband access in " + county + ", " + state + ": " + percentage + "%";
  }
}
