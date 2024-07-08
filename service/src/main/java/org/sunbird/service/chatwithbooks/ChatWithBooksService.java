package org.sunbird.service.chatwithbooks;

import org.sunbird.logging.LoggerUtil;
import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;
import org.sunbird.service.systemsettings.SystemSettingsService;

import java.util.Map;

public interface ChatWithBooksService {
    Response chatWithBookSave(Map<String, Object> user, RequestContext context);

}
