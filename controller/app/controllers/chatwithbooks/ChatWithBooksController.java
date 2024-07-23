package controllers.chatwithbooks;

import akka.actor.ActorRef;
import controllers.BaseController;
import org.sunbird.keys.JsonKey;
import org.sunbird.operations.ActorOperations;
import org.sunbird.request.Request;
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
        System.out.println("ChatWithBooksController saveChatBookData Request : "+httpRequest.toString());
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
        System.out.println("ChatWithBooksController readChatBookData UserID : "+userId+" Request : "+httpRequest.toString());
        return handleRequest(
                chatWithBooksActor,
                ActorOperations.CHAT_WITH_BOOKS_READ.getValue(),
                null,
                req -> {
                    Request request = (Request) req;
                    request.getContext().put(JsonKey.VERSION, JsonKey.VERSION_1);
                    return null;
                },
                userId,
                JsonKey.USER_ID,
                false,
                httpRequest
        );
    }
}
