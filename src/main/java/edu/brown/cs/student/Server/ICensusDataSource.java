package edu.brown.cs.student.Server;

import spark.Request;
import spark.Response;

public interface ICensusDataSource {
    Object fetchData(String state, String county) throws Exception;
}
