package RIData;


public class CountyRequestData {
  private String name;
  private String percentage;
  private String state;
  private String county;
  public CountyRequestData(){

  }
  @Override
  public String toString(){
    return "Percentage of households with broadband access in " + county + ", " + state + ": " + percentage + "%";
  }

  public String getPercentage(){
    return this.percentage;
  }
}
