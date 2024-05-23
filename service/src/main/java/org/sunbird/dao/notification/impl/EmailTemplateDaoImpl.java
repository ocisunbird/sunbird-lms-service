package org.sunbird.dao.notification.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.dao.notification.EmailTemplateDao;
import org.sunbird.helper.ServiceFactory;
import org.sunbird.keys.JsonKey;
import org.sunbird.request.RequestContext;
import org.sunbird.response.Response;

public class EmailTemplateDaoImpl implements EmailTemplateDao {

  private static EmailTemplateDao emailTemplateDao;

  public static EmailTemplateDao getInstance() {
    if (emailTemplateDao == null) {
      emailTemplateDao = new EmailTemplateDaoImpl();
    }
    return emailTemplateDao;
  }

  @Override
  public String getTemplate(String templateName, RequestContext context) {

    List<String> idList = new ArrayList<>();
    if (StringUtils.isBlank(templateName)) {
      String DEFAULT_EMAIL_TEMPLATE_NAME = "default";
      idList.add(DEFAULT_EMAIL_TEMPLATE_NAME);
    } else {
      idList.add(templateName);
    }
    String EMAIL_TEMPLATE = "email_template";
    Response response =
        getCassandraOperation()
            .getRecordsByPrimaryKeys(
                JsonKey.SUNBIRD, EMAIL_TEMPLATE, idList, JsonKey.NAME, context);
    List<Map<String, Object>> emailTemplateList =
        (List<Map<String, Object>>) response.get(JsonKey.RESPONSE);
    System.out.println("SMSTemplateProvider emailTemplateList "+emailTemplateList);
    Map<String, Object> map = Collections.emptyMap();
    if (CollectionUtils.isNotEmpty(emailTemplateList)) {
      map = emailTemplateList.get(0);
    }
    String TEMPLATE = "template";
    System.out.println("SMSTemplateProvider (String) map.get(TEMPLATE) "+(String) map.get(TEMPLATE));
    return (String) map.get(TEMPLATE);
  }

  private CassandraOperation getCassandraOperation() {
    return ServiceFactory.getInstance();
  }
}
