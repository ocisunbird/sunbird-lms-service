package org.sunbird.service.chatwithbooks;

import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;

import java.util.Map;

public interface ChatWithBooksService {
    Response chatWithBookSave(Map<String, Object> chatData, RequestContext context);
    Response chatWithBookRead(String userId, RequestContext context);
    Response chatWithBookUpdate(Map<String, Object> chatData, RequestContext context);
}
