package org.sunbird.notification.sms.providerimpl;

import org.sunbird.notification.sms.provider.ISmsProvider;
import org.sunbird.notification.sms.provider.ISmsProviderFactory;

public class CDACGatewayProviderFactory implements ISmsProviderFactory {

    private static CDACGatewaySmsProvider cdacGatewaySmsProvider=null;

    @Override
    public ISmsProvider create() {
        if(cdacGatewaySmsProvider == null){
            cdacGatewaySmsProvider = new CDACGatewaySmsProvider();
        }
        return cdacGatewaySmsProvider;
    }
}
