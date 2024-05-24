package org.sunbird.notification.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.helper.ServiceFactory;
import org.sunbird.keys.JsonKey;
import org.sunbird.logging.LoggerUtil;
import org.sunbird.response.Response;

public class SmsTemplateUtil {
  private static final LoggerUtil logger = new LoggerUtil(SmsTemplateUtil.class);
  private static final CassandraOperation cassandraOperation = ServiceFactory.getInstance();

  public static Map<String, Map<String, String>> getSmsTemplateConfigMap() {
    Response response =
        cassandraOperation.getRecordById(
            JsonKey.SUNBIRD, JsonKey.SYSTEM_SETTINGS_DB, JsonKey.SMS_TEMPLATE_CONFIG, null);
    logger.info("SmsTemplateUtil response :"+response.toString());
    List<Map<String, Object>> responseList =
        (List<Map<String, Object>>) response.get(JsonKey.RESPONSE);
    logger.info("SmsTemplateUtil responseList :"+responseList);
    if (null != responseList && !responseList.isEmpty()) {
      Map<String, Object> resultMap = responseList.get(0);
      logger.info("SmsTemplateUtil resultMap :"+resultMap);
      String smsTemplateConfigString = (String) resultMap.get(JsonKey.VALUE);
      logger.info("SmsTemplateUtil smsTemplateConfigString :"+smsTemplateConfigString);
      if (StringUtils.isNotBlank(smsTemplateConfigString)) {
        logger.info("SmsTemplateUtil status :"+StringUtils.isNotBlank(smsTemplateConfigString));
        ObjectMapper mapper = new ObjectMapper();
        try {
          return mapper.readValue(smsTemplateConfigString, Map.class);
        } catch (Exception e) {
          logger.error("Error occurred while reading sms template config" + e.getMessage(), e);
        }
      }
    }
    return Collections.emptyMap();
  }
}
