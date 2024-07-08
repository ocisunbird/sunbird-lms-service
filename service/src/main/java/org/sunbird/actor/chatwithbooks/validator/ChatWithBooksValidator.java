package org.sunbird.actor.chatwithbooks.validator;

import org.sunbird.actor.user.validator.UserRequestValidator;
import org.sunbird.exception.ResponseCode;
import org.sunbird.logging.LoggerUtil;

import java.util.ArrayList;
import java.util.List;

public class ChatWithBooksValidator {

    private final int ERROR_CODE = ResponseCode.CLIENT_ERROR.getResponseCode();
    protected static List<String> typeList = new ArrayList<>();
    private static final LoggerUtil logger = new LoggerUtil(ChatWithBooksValidator.class);
}
