package org.sunbird.actor.chatwithbooks;

import org.sunbird.actor.core.BaseActor;
import org.sunbird.keys.JsonKey;
import org.sunbird.logging.LoggerUtil;
import org.sunbird.model.chatwithbooks.ChatReadData;
import org.sunbird.request.Request;
import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;
import org.sunbird.service.chatwithbooks.ChatWithBooksService;
import org.sunbird.service.chatwithbooks.impl.ChatWithBooksServiceImpl;
import org.sunbird.telemetry.dto.TelemetryEnvKey;
import org.sunbird.util.Util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class ChatWithBooksActor extends BaseActor {

    private final LoggerUtil logger = new LoggerUtil(ChatWithBooksActor.class);
    private final ChatWithBooksService chatWithBooksService = ChatWithBooksServiceImpl.getInstance();

    public void onReceive(Request request) throws Throwable {
        Util.initializeContext(request, TelemetryEnvKey.USER);
        String operation = request.getOperation();
        logger.info("Operation ChatWithBooksActor : "+operation);
        logger.info("Request ChatWithBooksActor : "+request);
        RequestContext context = request.getRequestContext();
        switch (operation) {
            case "chatWithBooksSave":
                chatWithBooksSave(request);
                break;
            case "chatWithBooksRead":
                String userId = (String) request.getRequest().get(JsonKey.USER_ID);
                chatWithBooksRead(userId,context);
                break;
            default:
                onReceiveUnsupportedOperation();
        }
    }

    private void chatWithBooksSave(Request actorMessage) {
        actorMessage.toLower();
        Map<String, Object> chatMapWithBooksMap = actorMessage.getRequest();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        chatMapWithBooksMap.put("SearchQueryDate", LocalDateTime.now().format(format));
        //Saving the chat query in DB
        logger.info("Insert Query :"+chatMapWithBooksMap.toString());
        Response response = chatWithBooksService.chatWithBookSave(chatMapWithBooksMap, actorMessage.getRequestContext());
        response.put(JsonKey.ID, chatMapWithBooksMap.get(JsonKey.ID));
        if (JsonKey.SUCCESS.equalsIgnoreCase((String) response.get(JsonKey.RESPONSE))) {
            logger.info(actorMessage.getRequestContext(), "Search query inserted successfully in Database");
            sender().tell(response, self());
        }else {
            logger.info(actorMessage.getRequestContext(), "DB Status Failed");
        }
    }

    private void chatWithBooksRead(String userId, RequestContext context) {

        Map<String, Object> reqMap = new WeakHashMap<>(2);
        reqMap.put(JsonKey.USER_ID, userId);
        List<ChatReadData> readList = chatWithBooksService.readChatWithBookRecords(userId, context);
        logger.info("ChatWithBooksActor readList : "+readList);
        Map<String, Object> result = new HashMap<>();
        result.put(JsonKey.READ_BOOK_DATA, readList);
        Response response = new Response();
        response.put(JsonKey.RESPONSE, result);
        sender().tell(response, self());
    }
}
