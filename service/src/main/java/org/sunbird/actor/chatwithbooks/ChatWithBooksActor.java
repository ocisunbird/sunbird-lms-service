package org.sunbird.actor.chatwithbooks;

import org.sunbird.actor.core.BaseActor;
import org.sunbird.keys.JsonKey;
import org.sunbird.request.Request;
import org.sunbird.service.chatwithbooks.ChatWithBooksService;
import org.sunbird.service.chatwithbooks.impl.ChatWithBooksServiceImpl;
import org.sunbird.telemetry.dto.TelemetryEnvKey;
import org.sunbird.util.Util;

import java.util.Map;

public class ChatWithBooksActor extends BaseActor {

    private final ChatWithBooksService userService = ChatWithBooksServiceImpl.getInstance();

    public void onReceive(Request request) throws Throwable {
        Util.initializeContext(request, TelemetryEnvKey.USER);
        String operation = request.getOperation();
        switch (operation) {
            case "chatWithBooksSave":
                chatWithBooksSave(request);
                break;
            case "chatWithBooksRead":
                chatWithBooksRead(request);
                break;
            default:
                onReceiveUnsupportedOperation();
        }
    }

    private void chatWithBooksSave(Request actorMessage) {
        actorMessage.toLower();
        Map<String, Object> chatMapWithBooksMap = actorMessage.getRequest();
        logger.info("Request Body : "+actorMessage.getRequestContext());
        String userId = (String) actorMessage.getContext().get(JsonKey.REQUESTED_BY);
        logger.info("User ID : "+userId);
        chatMapWithBooksMap.put(JsonKey.CREATED_BY, userId);
        chatMapWithBooksMap.put("updatedOn", System.currentTimeMillis());
        //Saving the chat query in DB
        logger.info("Insert Query :"+chatMapWithBooksMap.toString());
        userService.chatWithBookSave(chatMapWithBooksMap,actorMessage.getRequestContext());
    }

    private void chatWithBooksRead(Request actorMessage) {

    }
}
