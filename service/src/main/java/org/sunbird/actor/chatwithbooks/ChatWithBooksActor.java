package org.sunbird.actor.chatwithbooks;

import org.sunbird.actor.core.BaseActor;
import org.sunbird.keys.JsonKey;
import org.sunbird.logging.LoggerUtil;
import org.sunbird.request.Request;
import org.sunbird.service.chatwithbooks.ChatWithBooksService;
import org.sunbird.service.chatwithbooks.impl.ChatWithBooksServiceImpl;
import org.sunbird.service.feed.impl.FeedServiceImpl;
import org.sunbird.telemetry.dto.TelemetryEnvKey;
import org.sunbird.util.Util;

import java.time.Instant;
import java.util.Map;

public class ChatWithBooksActor extends BaseActor {

    private final LoggerUtil logger = new LoggerUtil(ChatWithBooksActor.class);
    private final ChatWithBooksService userService = ChatWithBooksServiceImpl.getInstance();

    public void onReceive(Request request) throws Throwable {
        Util.initializeContext(request, TelemetryEnvKey.USER);
        String operation = request.getOperation();
        logger.info("Operation ChatWithBooksActor : "+operation);
        logger.info("Request ChatWithBooksActor : "+request);
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
        chatMapWithBooksMap.put("updatedOn", Instant.now());
        //Saving the chat query in DB
        logger.info("Insert Query :"+chatMapWithBooksMap.toString());
        userService.chatWithBookSave(chatMapWithBooksMap,actorMessage.getRequestContext());
        logger.info("Success Data inserted into DB");
    }

    private void chatWithBooksRead(Request actorMessage) {

    }
}
