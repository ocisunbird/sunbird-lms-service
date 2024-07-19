package org.sunbird.service.chatwithbooks.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.sunbird.dao.chatwithbooks.ChatWithBooksDao;
import org.sunbird.dao.chatwithbooks.impl.ChatWithBooksDaoImpl;
import org.sunbird.exception.ProjectCommonException;
import org.sunbird.exception.ResponseCode;
import org.sunbird.keys.JsonKey;
import org.sunbird.logging.LoggerUtil;
import org.sunbird.model.chatwithbooks.ChatReadData;
import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;
import org.sunbird.service.chatwithbooks.ChatWithBooksService;

import java.text.MessageFormat;
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
        logger.info("195124 Save Data " + bookSaveData.toString());
        return chatWithBooksDao.chatWithBooksSave(bookSaveData, context);
    }

    @Override
    public List<ChatReadData> readChatWithBookRecords(String userId, RequestContext context) {
        try {
            List<ChatReadData> chatReadDataList = new ArrayList<>();
            Response response = chatWithBooksDao.chatWithBooksRead(userId, context);
            logger.info("ChatWithBooksServiceImpl response String  : " + response.toString());
            logger.info("ChatWithBooksServiceImpl response ID : " + response.getId());
            logger.info("ChatWithBooksServiceImpl response Result : " + response.getResult().toString());
            logger.info("ChatWithBooksServiceImpl response TS : " + response.getTs());
            logger.info("ChatWithBooksServiceImpl response Version : " + response.getVer());
            logger.info("ChatWithBooksServiceImpl response Code : " + response.getResponseCode());
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
        } catch (Exception e) {
            logger.error("Error : ", e);
            return null;
        }
    }

    @Override
    public Response readChatWithBookRecordsNew(String userId, RequestContext context) {
        logger.info("195124 ChatWithBooksServiceImpl readChatWithBookRecordsNew UserId :"+userId+" Request : "+context);
        try {
            Response response = new Response();
            Map<String, Object> readChat = chatWithBooksDao.chatWithBooksReadNew(userId, context);
            if (MapUtils.isEmpty(readChat)) {
                ProjectCommonException.throwResourceNotFoundException(
                        ResponseCode.resourceNotFound,
                        MessageFormat.format(ResponseCode.resourceNotFound.getErrorMessage(), JsonKey.USER));
            }
            response.put(JsonKey.RESPONSE, readChat);
            logger.info("195124 ChatWithBooksServiceImpl readChatWithBookRecordsNew Response :"+response);
            logger.info("195124 ChatWithBooksServiceImpl readChatWithBookRecordsNew ReadChat :"+readChat);
            return response;
        } catch (Exception e) {
            logger.error("195124 error ", e);
            return null;
        }
    }
}
