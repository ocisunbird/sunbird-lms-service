package org.sunbird.service.chatwithbooks;

import org.sunbird.model.chatwithbooks.ChatReadData;
import org.sunbird.request.Request;
import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;

import java.util.List;
import java.util.Map;

public interface ChatWithBooksService {
    Response chatWithBookSave(Map<String, Object> user, RequestContext context);
    List<ChatReadData> readChatWithBookRecords(String userId, RequestContext context);
}
