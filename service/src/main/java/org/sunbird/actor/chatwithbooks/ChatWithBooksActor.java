package org.sunbird.actor.chatwithbooks;

import org.sunbird.actor.core.BaseActor;
import org.sunbird.keys.JsonKey;
import org.sunbird.logging.LoggerUtil;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
import org.sunbird.service.chatwithbooks.ChatWithBooksService;
import org.sunbird.service.chatwithbooks.impl.ChatWithBooksServiceImpl;
import org.sunbird.telemetry.dto.TelemetryEnvKey;
import org.sunbird.util.ProjectUtil;
import org.sunbird.util.Util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatWithBooksActor extends BaseActor {

    private final LoggerUtil logger = new LoggerUtil(ChatWithBooksActor.class);
    private final ChatWithBooksService chatWithBooksService = ChatWithBooksServiceImpl.getInstance();

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
        chatMapWithBooksMap.put("updatedOn", new Date().getTime());
        String userId = ProjectUtil.generateUniqueId();
        //Saving the chat query in DB
        logger.info("Insert Query :"+chatMapWithBooksMap.toString());
        Response response = chatWithBooksService.chatWithBookSave(chatMapWithBooksMap, actorMessage.getRequestContext());
        logger.info("Success Data inserted into DB");
        response.put(JsonKey.ID, chatMapWithBooksMap.get(JsonKey.ID));
        Map<String, Object> esResponse = new HashMap<>();
        if (JsonKey.SUCCESS.equalsIgnoreCase((String) response.get(JsonKey.RESPONSE))) {
            logger.info(actorMessage.getRequestContext(), "Insert the data in DB successfully");
            sender().tell(response, self());
        }else {
            logger.info(actorMessage.getRequestContext(), "DB Status Failed");
        }
    }

    private void chatWithBooksRead(Request actorMessage) {

    }
}
