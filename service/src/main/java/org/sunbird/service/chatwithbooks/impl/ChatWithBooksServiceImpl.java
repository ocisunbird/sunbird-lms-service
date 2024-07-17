package org.sunbird.service.chatwithbooks.impl;

import org.sunbird.dao.chatwithbooks.ChatWithBooksDao;
import org.sunbird.dao.chatwithbooks.impl.ChatWithBooksDaoImpl;
import org.sunbird.logging.LoggerUtil;
import org.sunbird.request.Request;
import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;
import org.sunbird.service.chatwithbooks.ChatWithBooksService;

import java.util.Map;

public class ChatWithBooksServiceImpl implements ChatWithBooksService {
    private final LoggerUtil logger = new LoggerUtil(ChatWithBooksServiceImpl.class);
    private static ChatWithBooksService chatWithBooksService = null;
    private final ChatWithBooksDao chatWithBooksDao = ChatWithBooksDaoImpl.getInstance();
    public static ChatWithBooksService getInstance() {
        if (chatWithBooksService == null) {
            chatWithBooksService = new ChatWithBooksServiceImpl();
        }
        return chatWithBooksService;
    }

    @Override
    public Response chatWithBookSave(Map<String, Object> bookSaveData, RequestContext context) {
        logger.info("195124 Save Data "+bookSaveData.toString());
        return chatWithBooksDao.chatWithBooksSave(bookSaveData,context);
    }

    @Override
    public Response chatWithBooksRead(Request actorMessage) {
        logger.info("195124 Read Data "+actorMessage.toString());
        return chatWithBooksDao.chatWithBooksRead(actorMessage);
    }
}
