package org.sunbird.service.chatwithbooks.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.sunbird.dao.chatwithbooks.ChatWithBooksDao;
import org.sunbird.dao.chatwithbooks.impl.ChatWithBooksDaoImpl;
import org.sunbird.exception.ProjectCommonException;
import org.sunbird.exception.ResponseCode;
import org.sunbird.keys.JsonKey;
import org.sunbird.logging.LoggerUtil;
import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;
import org.sunbird.service.chatwithbooks.ChatWithBooksService;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

public class ChatWithBooksServiceImpl implements ChatWithBooksService {
    private final LoggerUtil logger = new LoggerUtil(ChatWithBooksServiceImpl.class);
    private static ChatWithBooksService chatWithBooksService = null;
    private final ChatWithBooksDao chatWithBooksDao = ChatWithBooksDaoImpl.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    public static ChatWithBooksService getInstance() {
        if (chatWithBooksService == null) {
            chatWithBooksService = new ChatWithBooksServiceImpl();
        }
        return chatWithBooksService;
    }

    @Override
    public Response chatWithBookSave(Map<String, Object> bookSaveData, RequestContext context) {
        logger.info("195124 Save Data " + bookSaveData.toString());
        return chatWithBooksDao.chatWithBooksSave(bookSaveData, context);
    }
    @Override
    public Response readChatWithBookRecords(String userId, RequestContext context) {
        logger.info("195124 ChatWithBooksServiceImpl readChatWithBookRecordsNew UserId :"+userId+" Request : "+context);
        try {
            Response response = new Response();
            List<Map<String, Object>> readChat = chatWithBooksDao.chatWithBooksRead(userId, context);
            if (CollectionUtils.isEmpty(readChat)) {
                ProjectCommonException.throwResourceNotFoundException(
                        ResponseCode.resourceNotFound,
                        MessageFormat.format(ResponseCode.resourceNotFound.getErrorMessage(), JsonKey.USER));
            }
            response.put(JsonKey.RESPONSE, readChat);
            return response;
        } catch (Exception e) {
            logger.error("195124 error ", e);
            return null;
        }
    }
}
