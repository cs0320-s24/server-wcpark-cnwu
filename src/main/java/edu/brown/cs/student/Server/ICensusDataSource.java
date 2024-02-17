package edu.brown.cs.student.Server;

/**
 * Represents a data source for fetching census data.
 * This interface defines the contract for any class that intends to provide
 * access to census data based on state and county parameters.
 */
public interface ICensusDataSource {

    /**
     * Fetches census data for a specified state and county.
     *
     * Implementations of this method are expected to retrieve data related to
     * broadband internet access or other census information, depending on the
     * specific implementation details of the class that implements this interface.
     *
     * @param state The state for which data is being requested. It is expected
     *              to be a valid state identifier that the underlying data source
     *              can recognize and process.
     * @param county The county within the specified state for which data is being requested.
     *               Similar to the state parameter, it should be a valid county identifier.
     * @return An Object containing the fetched data. The actual type and structure
     *         of this object depend on the implementation. It might be a raw JSON
     *         string, a Map, or a custom POJO that models the census data.
     * @throws Exception if there is any issue in fetching the data. This might include
     *                   network issues, data parsing errors, or invalid parameter values
     *                   that the data source cannot process.
     */
    Object fetchData(String state, String county) throws Exception;
}
