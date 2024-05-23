package org.sunbird.util;

import java.io.StringWriter;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.sunbird.dao.notification.EmailTemplateDao;
import org.sunbird.dao.notification.impl.EmailTemplateDaoImpl;
import org.sunbird.keys.JsonKey;
import org.sunbird.logging.LoggerUtil;
import org.sunbird.request.RequestContext;

public class SMSTemplateProvider {
  private static final LoggerUtil logger = new LoggerUtil(SMSTemplateProvider.class);
  public static final String SMS_PROVIDER =
      ProjectUtil.getConfigValue(JsonKey.SMS_GATEWAY_PROVIDER);
  private static final EmailTemplateDao emailTemplateDao = EmailTemplateDaoImpl.getInstance();

  private SMSTemplateProvider() {}

  private static String getTemplate(String templateId, RequestContext context) {
    String defaultTemplate = templateId;
    if (StringUtils.isNotBlank(templateId) && JsonKey.NIC.equalsIgnoreCase(SMS_PROVIDER)) {
      defaultTemplate = templateId + "_nic";
    }
    logger.info("SMSTemplateProvider defaultTemplate "+defaultTemplate);
    return emailTemplateDao.getTemplate(defaultTemplate, context);
  }

  public static String getSMSBody(
      String smsTemplate, Map<String, String> templateConfig, RequestContext requestContext) {
    try {
      String template = getTemplate(smsTemplate, requestContext);
      logger.info("SMSTemplateProvider template "+template);
      RuntimeServices rs = RuntimeSingleton.getRuntimeServices();
      SimpleNode sn = rs.parse(template, "Sms Information");
      Template t = new Template();
      t.setRuntimeServices(rs);
      t.setData(sn);
      t.initDocument();
      VelocityContext context = new VelocityContext(templateConfig);
      logger.info("SMSTemplateProvider context "+context);
      StringWriter writer = new StringWriter();
      t.merge(context, writer);
      logger.info("SMSTemplateProvider writer.toString() "+writer.toString());
      return writer.toString();
    } catch (Exception ex) {
      logger.error("Exception occurred while formatting SMS ", ex);
    }
    return "";
  }
}
