package org.sunbird.dao.chatwithbooks;

import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;

import java.util.List;
import java.util.Map;

public interface ChatWithBooksDao {

    /**
     * This method will save the user input data in the database and return userId as success response or throw
     * ProjectCommonException.
     *
     * @param bookSaveData User Details.
     * @param context
     * @return User ID.
     */
    Response chatWithBooksSave(Map<String, Object> bookSaveData, RequestContext context);

    /**
     * This method will read the user input data from the database and return search results as success response or throw
     * ProjectCommonException.
     *
     * @param userId
     * @param context User Details.
     * @return search details.
     */
    List<Map<String, Object>> chatWithBooksRead(String userId, RequestContext context);
}
