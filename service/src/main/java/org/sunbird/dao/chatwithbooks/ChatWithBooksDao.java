package org.sunbird.dao.chatwithbooks;

import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;

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
}
