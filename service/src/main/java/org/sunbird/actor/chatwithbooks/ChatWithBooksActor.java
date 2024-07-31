package org.sunbird.actor.chatwithbooks;

import org.sunbird.actor.core.BaseActor;
import org.sunbird.keys.JsonKey;
import org.sunbird.logging.LoggerUtil;
import org.sunbird.request.Request;
import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;
import org.sunbird.service.chatwithbooks.ChatWithBooksService;
import org.sunbird.service.chatwithbooks.impl.ChatWithBooksServiceImpl;
import org.sunbird.telemetry.dto.TelemetryEnvKey;
import org.sunbird.util.Util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ChatWithBooksActor extends BaseActor {

    private final LoggerUtil logger = new LoggerUtil(ChatWithBooksActor.class);
    private final ChatWithBooksService chatWithBooksService = ChatWithBooksServiceImpl.getInstance();

    public void onReceive(Request request) throws Throwable {
        Util.initializeContext(request, TelemetryEnvKey.USER);
        String operation = request.getOperation();
        logger.info("195124 ChatWithBooksActor onReceive Operation : " + operation);
        RequestContext context = request.getRequestContext();
        switch (operation) {
            case "chatWithBooksSave":
                chatWithBooksSave(request);
                break;
            case "chatWithBooksRead":
                String userId = (String) request.getRequest().get(JsonKey.USER_ID);
                chatWithBooksRead(userId, context);
                break;
            case "chatWithBooksUpdate":
                chatWithBooksUpdate(request);
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
        logger.info("195124 Input Json :" + chatMapWithBooksMap);
        Response response = chatWithBooksService.chatWithBookSave(chatMapWithBooksMap, actorMessage.getRequestContext());
        response.put(JsonKey.ID, chatMapWithBooksMap.get(JsonKey.ID));
        response.setId(JsonKey.CHAT_WITH_BOOKS_SAVE);
        response.setVer(JsonKey.VERSION_1);
        if (JsonKey.SUCCESS.equalsIgnoreCase((String) response.get(JsonKey.RESPONSE))) {
            logger.info(actorMessage.getRequestContext(), "195124 Search query inserted successfully for userid "+chatMapWithBooksMap.get(JsonKey.ID));
            sender().tell(response, self());
        } else {
            logger.info(actorMessage.getRequestContext(), "195124 DB Status Failed");
        }
    }

    private void chatWithBooksRead(String userId, RequestContext context) {
        logger.info("195124 ChatWithBooksActor chatWithBooksRead userId : " + userId);
        Response response = chatWithBooksService.chatWithBookRead(userId, context);
        logger.info("195124 ChatWithBooksActor chatWithBooksRead Response : " + response.toString());
        sender().tell(response, self());
    }

    private void chatWithBooksUpdate(Request request) {
        request.toLower();
        Map<String, Object> chatMapWithBooksMap = request.getRequest();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        chatMapWithBooksMap.put("userFeedBackStatusDate", LocalDateTime.now().format(format));
        chatMapWithBooksMap.put("userFeedBackStatus", "Negative");
        //Update the chat query in DB
        logger.info("195124 Update Json :" + chatMapWithBooksMap);
        Response response = chatWithBooksService.chatWithBookUpdate(chatMapWithBooksMap, request.getRequestContext());
        response.put(JsonKey.ID, chatMapWithBooksMap.get(JsonKey.ID));
        response.setId(JsonKey.CHAT_WITH_BOOKS_UPDATE);
        response.setVer(JsonKey.VERSION_1);
        if (JsonKey.SUCCESS.equalsIgnoreCase((String) response.get(JsonKey.RESPONSE))) {
            logger.info(request.getRequestContext(), "195124 Updated successfully in Database for userid "+chatMapWithBooksMap.get(JsonKey.ID));
            sender().tell(response, self());
        } else {
            logger.info(request.getRequestContext(), "195124 DB Status Failed");
        }
    }
}
