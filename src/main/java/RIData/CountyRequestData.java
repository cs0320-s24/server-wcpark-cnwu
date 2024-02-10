package RIData;


public class CountyRequestData {
  private String state;
  private String stateCode;
  private String county;
  private String countyCode;
  private String percentage;
  public CountyRequestData(){

  }
  @Override
  public String toString(){
    return "Percentage of households with broadband access in " + county + ", " + state + ": " + percentage + "%";
  }

  public String getCountyCode(){
    return this.countyCode;
  }
}
