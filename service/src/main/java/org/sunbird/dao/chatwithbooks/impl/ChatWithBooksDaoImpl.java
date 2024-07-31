package org.sunbird.dao.chatwithbooks.impl;

import org.apache.commons.collections.CollectionUtils;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.dao.chatwithbooks.ChatWithBooksDao;
import org.sunbird.helper.ServiceFactory;
import org.sunbird.keys.JsonKey;
import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ChatWithBooksDaoImpl implements ChatWithBooksDao {

    private static final String TABLE_NAME = JsonKey.CHAT_WITH_BOOKS;
    private static final String KEY_SPACE_NAME = JsonKey.SUNBIRD;
    private static ChatWithBooksDao chatWithBooksDao = null;
    private final CassandraOperation cassandraOperation = ServiceFactory.getInstance();
    private final List<String> RESPONSE_COLUMN = Arrays.asList("id", "userId", "searchQuery", "searchQueryDate");

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
    public List<Map<String, Object>> chatWithBooksRead(String userId, RequestContext context) {
        Response response = cassandraOperation.getRecordById(KEY_SPACE_NAME, TABLE_NAME, userId, RESPONSE_COLUMN, context);
        List<Map<String, Object>> responseList = (List<Map<String, Object>>) response.get(JsonKey.RESPONSE);
        if (CollectionUtils.isNotEmpty(responseList)) {
            return responseList;
        }
        return null;
    }

    @Override
    public Response chatWithBooksUpdate(Map<String, Object> bookUpdateData, RequestContext context) {
        return cassandraOperation.upsertRecord(KEY_SPACE_NAME, TABLE_NAME, bookUpdateData, context);
    }
}
