package org.sunbird.service.chatwithbooks.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.sunbird.dao.chatwithbooks.ChatWithBooksDao;
import org.sunbird.dao.chatwithbooks.impl.ChatWithBooksDaoImpl;
import org.sunbird.keys.JsonKey;
import org.sunbird.logging.LoggerUtil;
import org.sunbird.model.chatwithbooks.ChatReadData;
import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;
import org.sunbird.service.chatwithbooks.ChatWithBooksService;

import java.util.ArrayList;
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
        logger.info("195124 Save Data "+bookSaveData.toString());
        return chatWithBooksDao.chatWithBooksSave(bookSaveData,context);
    }

    @Override
    public List<ChatReadData> readChatWithBookRecords(String userId, RequestContext context) {
        List<ChatReadData> chatReadDataList = new ArrayList<>();
        Response response = chatWithBooksDao.chatWithBooksRead(userId,context);
        logger.info("ChatWithBooksServiceImpl response : "+response.toString());
        if (null != response) {
            List<Map<String, Object>> readResults =
                    (List<Map<String, Object>>) response.getResult().get(JsonKey.READ_BOOK_DATA);
            if (CollectionUtils.isNotEmpty(readResults)) {
                for (Map<String, Object> readData : readResults) {
                    chatReadDataList.add(mapper.convertValue(readData, ChatReadData.class));
                }
            }
        }
        return chatReadDataList;
    }
}
