package org.sunbird.service.chatwithbooks.impl;

import org.apache.commons.collections.CollectionUtils;
import org.sunbird.dao.chatwithbooks.ChatWithBooksDao;
import org.sunbird.dao.chatwithbooks.impl.ChatWithBooksDaoImpl;
import org.sunbird.keys.JsonKey;
import org.sunbird.logging.LoggerUtil;
import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;
import org.sunbird.service.chatwithbooks.ChatWithBooksService;

import java.util.List;
import java.util.Map;

public class ChatWithBooksServiceImpl implements ChatWithBooksService {
    private static ChatWithBooksService chatWithBooksService = null;
    private final LoggerUtil logger = new LoggerUtil(ChatWithBooksServiceImpl.class);
    private final ChatWithBooksDao chatWithBooksDao = ChatWithBooksDaoImpl.getInstance();

    public static ChatWithBooksService getInstance() {
        if (chatWithBooksService == null) {
            chatWithBooksService = new ChatWithBooksServiceImpl();
        }
        return chatWithBooksService;
    }

    @Override
    public Response chatWithBookSave(Map<String, Object> bookSaveData, RequestContext context) {
        return chatWithBooksDao.chatWithBooksSave(bookSaveData, context);
    }

    @Override
    public Response chatWithBookRead(String userId, RequestContext context) {
        try {
            Response response = new Response();
            List<Map<String, Object>> readChatList = chatWithBooksDao.chatWithBooksRead(userId, context);
            if (CollectionUtils.isEmpty(readChatList)) {
                response.setId(JsonKey.CHAT_WITH_BOOKS_READ);
                response.setVer(JsonKey.VERSION_1);
                response.put(JsonKey.RESPONSE, response);
            }
            response.setId(JsonKey.CHAT_WITH_BOOKS_READ);
            response.setVer(JsonKey.VERSION_1);
            response.put(JsonKey.RESPONSE, readChatList);
            return response;
        } catch (Exception e) {
            logger.error("195124 error ", e);
            return null;
        }
    }

    @Override
    public Response chatWithBookUpdate(Map<String, Object> chatData, RequestContext context) {
        return chatWithBooksDao.chatWithBooksUpdate(chatData, context);
    }

}
