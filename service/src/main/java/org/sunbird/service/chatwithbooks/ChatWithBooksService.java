package org.sunbird.service.chatwithbooks;

import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;

import java.util.Map;

public interface ChatWithBooksService {
    Response chatWithBookSave(Map<String, Object> user, RequestContext context);

    Response readChatWithBookRecords(String userId, RequestContext context);
}
