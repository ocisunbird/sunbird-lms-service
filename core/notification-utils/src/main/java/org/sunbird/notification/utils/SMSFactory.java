package org.sunbird.notification.utils;

import org.sunbird.keys.JsonKey;
import org.sunbird.notification.sms.provider.ISmsProvider;
import org.sunbird.notification.sms.provider.ISmsProviderFactory;
import org.sunbird.notification.sms.providerimpl.CDACGatewayProviderFactory;
import org.sunbird.notification.sms.providerimpl.Msg91SmsProviderFactory;
import org.sunbird.notification.sms.providerimpl.NICGatewaySmsProviderFactory;
import org.sunbird.util.ProjectUtil;

/**
 * This class will provide object of factory.
 *
 * @author Manzarul
 */
public class SMSFactory {

  public static final String SMS_PROVIDER =
      ProjectUtil.getConfigValue(JsonKey.SMS_GATEWAY_PROVIDER);

  /**
   * This method will provide SMS Provide object to trigger the SMS it will by default return
   * Msg91SmsProvider class instance
   *
   * @return ISmsProvider
   */
  public static ISmsProvider getInstance() {
    if (JsonKey.MSG_91.equalsIgnoreCase(SMS_PROVIDER)) {
      ISmsProviderFactory factory = new Msg91SmsProviderFactory();
      return factory.create();
    } else if (JsonKey.NIC.equalsIgnoreCase(SMS_PROVIDER)) {
      ISmsProviderFactory factory = new NICGatewaySmsProviderFactory();
      return factory.create();
    } else if (JsonKey.CDAC.equalsIgnoreCase(SMS_PROVIDER)) {
      ISmsProviderFactory factory = new CDACGatewayProviderFactory();
      return factory.create();
    }else {
      ISmsProviderFactory factory = new Msg91SmsProviderFactory();
      return factory.create();
    }
  }
}
