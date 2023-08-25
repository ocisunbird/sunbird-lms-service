package org.sunbird.notification.sms.providerimpl;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.sunbird.logging.LoggerUtil;
import org.sunbird.notification.sms.provider.ISmsProvider;
import org.sunbird.notification.utils.JsonUtil;
import org.sunbird.notification.utils.PropertiesCache;
import org.sunbird.request.RequestContext;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class CDACGatewaySmsProvider implements ISmsProvider {

    private static final LoggerUtil logger = new LoggerUtil(CDACGatewaySmsProvider.class);

    private static String baseUrl = null;
    private static String senderId = null;
    private static String userName = null;
    private static String password = null;
    private static String deptSecureKey = null;

    static {
        boolean response = init();
        logger.info("SMS configuration values are set ==" + response);
    }
    public static boolean init() {
        baseUrl = System.getenv("cdac_sms_gateway_provider_base_url");
        logger.info("baseUrl from env ::::::"+baseUrl);
        if (JsonUtil.isStringNullOREmpty(baseUrl)) {
            baseUrl = PropertiesCache.getInstance().getProperty("cdac_sms_gateway_provider_base_url");
//            baseUrl = "https://msdgweb.mgov.gov.in/esms/sendsmsrequestDLT";
            logger.info("baseUrl hardcoded ::::::"+baseUrl);
        }
        senderId = System.getenv("cdac_sms_gateway_provider_senderid");
        if (JsonUtil.isStringNullOREmpty(senderId)) {
            senderId = PropertiesCache.getInstance().getProperty("cdac_sms_gateway_provider_senderid");
//            senderId = "IDKSHA";
        }
        userName = System.getenv("cdac_sms_gateway_provider_username");
        if (JsonUtil.isStringNullOREmpty(userName)) {
            userName = PropertiesCache.getInstance().getProperty("cdac_sms_gateway_provider_username");
//            userName ="mlasia-diksha";
        }
        password = System.getenv("cdac_sms_gateway_provider_password");
//        password = "dikshasp@Dic812sp";
        if (JsonUtil.isStringNullOREmpty(password)) {
            password = PropertiesCache.getInstance().getProperty("cdac_sms_gateway_provider_password");
//            password = "dikshasp@Dic812sp";
        }
        deptSecureKey = System.getenv("cdac_sms_gateway_provider_secure_key");
        if (JsonUtil.isStringNullOREmpty(deptSecureKey)) {
            deptSecureKey = PropertiesCache.getInstance().getProperty("cdac_sms_gateway_provider_secure_key");
//            deptSecureKey = "67ad2244-58be-4762-be38-96d18ee2cf74";
        }
        return validateSettings();
    }

    private static boolean validateSettings() {
        if (!JsonUtil.isStringNullOREmpty(senderId)
                && !JsonUtil.isStringNullOREmpty(userName)
                && !JsonUtil.isStringNullOREmpty(password)
                && !JsonUtil.isStringNullOREmpty(deptSecureKey)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean send(String phoneNumber, String smsText, RequestContext context) {
        return sendSms(phoneNumber, smsText, context);
    }

    @Override
    public boolean send(String phoneNumber, String countryCode, String smsText, RequestContext context) {
        return sendSms(phoneNumber, smsText, context);
    }

    @Override
    public boolean send(List<String> phoneNumber, String smsText, RequestContext context) {
        phoneNumber
                .stream()
                .forEach(
                        phone -> {
                            sendSms(phone, smsText, context);
                        });
        return true;
    }

    public boolean sendSms(String mobileNumber, String smsText, RequestContext context) {

        String recipient = mobileNumber;
        if (recipient.length() == 10) {
            // add country code to mobile number
            recipient = "91" + recipient;
        }

        String encPassword = sha1(password);

        String dltTemplateId = getTemplateId(smsText, CDAC_PROVIDER);
        if (StringUtils.isBlank(dltTemplateId)) {
            logger.info(context, "dlt template id is empty for sms : " + smsText);
        }
        String finalMessage = convert(smsText);

        String keyOne = sha512(userName.trim() + senderId.trim() + finalMessage.trim() + deptSecureKey.trim());

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(baseUrl);

        try {
            String jsonData = "{" +
                    "\"username\":\"" + userName + "\"," +
                    "\"password\":\"" + encPassword + "\"," +
                    "\"senderid\":\"" + senderId + "\"," +
                    "\"content\":\"" + finalMessage + "\"," +
                    "\"smsservicetype\":\"unicodemsg\"," +
                    "\"bulkmobno\":\"" + recipient + "\"," +
                    "\"key\":\"" + keyOne + "\"," +
                    "\"templateid\":\"" + dltTemplateId + "\"" +
                    "}";

            httpPost.setEntity(new StringEntity(jsonData));
            httpPost.setHeader("Content-type", "application/json");

            HttpEntity entity = httpClient.execute(httpPost).getEntity();
            String response = EntityUtils.toString(entity);

            System.out.println(response);
            if (StringUtils.isNotBlank(response)) {
                logger.info(context, "CDACGatewaySmsProvider:Result:" + response);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            logger.error(context, "Exception occurred while sending sms.", e);
            return false;
        }
    }

    public static String sha1(String input) {
        // Implement SHA-1 hashing here
//        return input; // Replace with actual implementation
        try {
            // Create a MessageDigest object for SHA-1
            MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");

            // Update the digest with the input bytes
            byte[] bytes = input.getBytes();
            sha1Digest.update(bytes);

            // Get the computed hash
            byte[] sha1Hash = sha1Digest.digest();

            // Convert the byte array to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : sha1Hash) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String sha512(String input) {
        try {
            // Create a MessageDigest object for SHA-512
            MessageDigest sha512Digest = MessageDigest.getInstance("SHA-512");

            // Update the digest with the input bytes
            byte[] bytes = input.getBytes();
            sha512Digest.update(bytes);

            // Get the computed hash
            byte[] sha512Hash = sha512Digest.digest();

            // Convert the byte array to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : sha512Hash) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null; // Handle the error case here
    }

    public static String convert(String body) {
        StringBuilder finalMessage = new StringBuilder();
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            int code = (int) c;
            finalMessage.append("&#").append(code).append(";");
        }
        return finalMessage.toString();
    }
}
