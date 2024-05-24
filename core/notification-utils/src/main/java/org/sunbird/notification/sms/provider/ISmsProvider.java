package org.sunbird.notification.sms.provider;

import java.util.List;
import java.util.Map;
import org.sunbird.keys.JsonKey;
import org.sunbird.notification.utils.SmsTemplateUtil;
import org.sunbird.request.RequestContext;

public interface ISmsProvider {

  String MSG_91_PROVIDER = JsonKey.MSG_91;
  String NIC_PROVIDER = JsonKey.NIC;
  String CDAC_PROVIDER = JsonKey.CDAC;

  default String getTemplateId(String sms, String provider) {
    System.out.println("ISmsProvider sms :"+sms);
    Map<String, Map<String, String>> smsTemplateConfig = SmsTemplateUtil.getSmsTemplateConfigMap();
    Map<String, String> providerTemplateConfig = smsTemplateConfig.get(provider);
    for (Map.Entry<String, String> entry : providerTemplateConfig.entrySet()) {
      System.out.println("**************Template id :"+entry.getValue());
      String pattern = entry.getKey().replaceAll("\\$[^ .]+", ".*?");
      System.out.println("**************Pattern :"+pattern);
      if (sms.matches(pattern)) {
        System.out.println("SMS Pattern matched for :"+entry.getValue());
        return entry.getValue();
      }
    }
    return "";
  }

  /**
   * This method will send the SMS with default country code. default country code value will differ
   * based on Installation, for sunbird default is 91
   *
   * @param phoneNumber String
   * @param smsText Sms text
   * @return boolean
   */
  boolean send(String phoneNumber, String smsText, RequestContext context);

  /**
   * This method will send SMS on user provider country code, basically it will override the value
   * of default country code.
   *
   * @param phoneNumber String
   * @param countryCode
   * @param smsText
   * @return boolean
   */
  boolean send(String phoneNumber, String countryCode, String smsText, RequestContext context);

  /**
   * This method will send the SMS to list of phone number at the same time. default country code
   * value will differ based on Installation, for sunbird default is 91
   *
   * @param phoneNumber List<String>
   * @param smsText Sms text
   * @return boolean
   */
  boolean send(List<String> phoneNumber, String smsText, RequestContext context);
}
