package org.sunbird.notification.sms.providerimpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.sunbird.logging.LoggerUtil;
import org.sunbird.notification.sms.provider.ISmsProvider;
import org.sunbird.notification.utils.JsonUtil;
import org.sunbird.notification.utils.PropertiesCache;
import org.sunbird.request.RequestContext;

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
        String responseString = "";
        SSLSocketFactory sf = null;
        SSLContext sslcontext = null;
        String encryptedPassword;
        try {
            String dltTemplateId = getTemplateId(smsText, CDAC_PROVIDER);
            logger.info("CDACGatewaySmsProvider dltTemplateId : "+dltTemplateId);
            sslcontext = SSLContext.getInstance("TLSv1.2");
            sslcontext.init(null, null, null);
            sf = new SSLSocketFactory(sslcontext, SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);

            Scheme scheme = new Scheme("https", 443, sf);
            HttpClient client = new DefaultHttpClient();

            client.getConnectionManager().getSchemeRegistry().register(scheme);
            HttpPost post = new HttpPost(baseUrl);
            encryptedPassword = MD5(password);
            smsText = smsText.trim();
            String genratedhashKey = hashGenerator(userName, senderId, smsText, deptSecureKey);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("bulkmobno", mobileNumber));
            nameValuePairs.add(new BasicNameValuePair("senderid", senderId));
            nameValuePairs.add(new BasicNameValuePair("content", smsText));
            nameValuePairs.add(new BasicNameValuePair("smsservicetype", "bulkmsg"));
            nameValuePairs.add(new BasicNameValuePair("username", userName));
            nameValuePairs.add(new BasicNameValuePair("password", encryptedPassword));
            nameValuePairs.add(new BasicNameValuePair("key", genratedhashKey));
            nameValuePairs.add(new BasicNameValuePair("templateid", dltTemplateId));

            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = client.execute(post);
            BufferedReader bf = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = bf.readLine()) != null) {
                responseString = responseString + line;
            }
            logger.info(context,"CDACGatewaySmsProvider responseString::::::"+responseString);

            if (StringUtils.isNotBlank(responseString)) {
                logger.info(context, "CDACGatewaySmsProvider:Result:" + responseString);
                return true;
            } else {
                return false;
            }

        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            // TODO Auto-generated catch block e.printStackTrace();
            logger.error(context, "Error occurred while sending sms: "+ e.getMessage(), e);
            return false;
        }
    }

    /****
     * Method to convert Normal Plain Text Password to MD5 encrypted password
     ***/

    private static String MD5(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        byte[] md5 = new byte[64];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        md5 = md.digest();
        return convertedToHex(md5);
    }

    private static String convertedToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfOfByte = (data[i] >>> 4) & 0x0F;
            int twoHalfBytes = 0;
            do {
                if ((0 <= halfOfByte) && (halfOfByte <= 9)) {
                    buf.append((char) ('0' + halfOfByte));
                } else {
                    buf.append((char) ('a' + (halfOfByte - 10)));
                }
                halfOfByte = data[i] & 0x0F;
            } while (twoHalfBytes++ < 1);
        }
        return buf.toString();
    }

    protected static String hashGenerator(String userName, String senderId, String content, String secureKey) {
        // TODO Auto-generated method stub
        StringBuffer finalString = new StringBuffer();
        finalString.append(userName.trim()).append(senderId.trim()).append(content.trim()).append(secureKey.trim());
//			logger.info("Parameters for SHA-512 : "+finalString);
        String hashGen = finalString.toString();
        StringBuffer sb = null;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
            md.update(hashGen.getBytes());
            byte byteData[] = md.digest();
            // convert the byte to hex format method 1
            sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block e.printStackTrace();
            e.printStackTrace();
        }
        return sb.toString();
    }
}
