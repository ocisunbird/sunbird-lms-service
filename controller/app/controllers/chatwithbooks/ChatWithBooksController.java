package controllers.chatwithbooks;

import akka.actor.ActorRef;
import controllers.BaseController;
import org.sunbird.keys.JsonKey;
import org.sunbird.operations.ActorOperations;
import org.sunbird.request.Request;
import org.sunbird.util.ProjectUtil;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletionStage;

public class ChatWithBooksController extends BaseController {

    @Inject
    @Named("chat_with_books_actor")
    private ActorRef chatWithBooksActor;

    public CompletionStage<Result> saveChatBookData(Http.Request httpRequest) {
        return handleRequest(
                chatWithBooksActor,
                ActorOperations.CHAT_WITH_BOOKS_SAVE.getValue(),
                httpRequest.body().asJson(),
                req -> {
                    Request request = (Request) req;
                    request.getContext().put(JsonKey.VERSION, JsonKey.VERSION_1);
                    return null;
                },
                null,
                null,
                true,
                httpRequest
        );
    }

    public CompletionStage<Result> readChatBookData(String userId, Http.Request httpRequest) {
        System.out.println("ChatWithBooksController UserID : "+userId+" Request : "+httpRequest.toString());
        return handleReadChatBookData(
                ActorOperations.CHAT_WITH_BOOKS_READ.getValue(),
                userId,
                httpRequest);
    }

    private CompletionStage<Result> handleReadChatBookData(
            String operation, String userId, Http.Request httpRequest) {
        final boolean isPrivate = httpRequest.path().contains(JsonKey.PRIVATE) ? true : false;
        final String requestedFields = httpRequest.getQueryString(JsonKey.FIELDS);
        final String provider = httpRequest.getQueryString(JsonKey.PROVIDER);
        final String idType = httpRequest.getQueryString(JsonKey.ID_TYPE);
        final String withTokens = httpRequest.getQueryString(JsonKey.WITH_TOKENS);
        System.out.println("ChatWithBooksController UserID : "+userId);
        return handleRequest(
                chatWithBooksActor,
                operation,
                null,
                req -> {
                    Request request = (Request) req;
                    request.getContext().put(JsonKey.FIELDS, requestedFields);
                    request.getContext().put(JsonKey.PRIVATE, isPrivate);
                    request.getContext().put(JsonKey.WITH_TOKENS, withTokens);
                    request.getContext().put(JsonKey.PROVIDER, provider);
                    request.getContext().put(JsonKey.ID_TYPE, idType);
                    request.getContext().put(JsonKey.VERSION, JsonKey.VERSION_1);
                    return null;
                },
                userId,
                JsonKey.USER_ID,
                false,
                httpRequest);
    }
}
