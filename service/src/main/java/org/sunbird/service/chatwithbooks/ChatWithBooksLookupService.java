package org.sunbird.service.chatwithbooks;

import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;

import java.util.Map;

public interface ChatWithBooksLookupService {
    Response insertRecords(Map<String, Object> chatWithBook, RequestContext context);
}
