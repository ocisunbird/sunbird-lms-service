package org.sunbird.dao.chatwithbooks.impl;

import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.dao.chatwithbooks.ChatWithBooksDao;
import org.sunbird.helper.ServiceFactory;
import org.sunbird.keys.JsonKey;
import org.sunbird.logging.LoggerUtil;
import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ChatWithBooksDaoImpl implements ChatWithBooksDao {

    private final LoggerUtil logger = new LoggerUtil(ChatWithBooksDaoImpl.class);
    private static final String TABLE_NAME = JsonKey.CHAT_WITH_BOOKS;
    private static final String KEY_SPACE_NAME = JsonKey.SUNBIRD;
    private List<String> RESPONSE_COLUMN = Arrays.asList("saveQuery","SearchQueryDate","userId");
    private final CassandraOperation cassandraOperation = ServiceFactory.getInstance();
    private static ChatWithBooksDao chatWithBooksDao = null;
    public static ChatWithBooksDao getInstance() {
        if (chatWithBooksDao == null) {
            chatWithBooksDao = new ChatWithBooksDaoImpl();
        }
        return chatWithBooksDao;
    }

    @Override
    public Response chatWithBooksSave(Map<String, Object> bookSaveData, RequestContext context) {
        return cassandraOperation.insertRecord(KEY_SPACE_NAME, TABLE_NAME, bookSaveData, context);
    }

    @Override
    public Response chatWithBooksRead(String userId, RequestContext context) {
        logger.info("ChatWithBooksDaoImpl chatWithBooksRead userId :"+userId);
        Response propertiesValueById = cassandraOperation.getPropertiesValueById(KEY_SPACE_NAME, TABLE_NAME, userId, RESPONSE_COLUMN, context);
        logger.info("propertiesValueById "+propertiesValueById.toString());
        logger.info("params "+propertiesValueById.getParams().toString());
        return propertiesValueById;
    }
}
