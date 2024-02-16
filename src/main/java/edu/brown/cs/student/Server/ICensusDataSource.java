package edu.brown.cs.student.Server;
public interface ICensusDataSource {
    Object fetchData(String state, String county) throws Exception;
}
