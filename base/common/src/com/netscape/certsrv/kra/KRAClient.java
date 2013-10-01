package com.netscape.certsrv.kra;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;

import org.jboss.resteasy.client.ClientResponse;

import com.netscape.certsrv.cert.CertData;
import com.netscape.certsrv.client.PKIClient;
import com.netscape.certsrv.client.SubsystemClient;
import com.netscape.certsrv.dbs.keydb.KeyId;
import com.netscape.certsrv.group.GroupClient;
import com.netscape.certsrv.key.KeyArchivalRequest;
import com.netscape.certsrv.key.KeyData;
import com.netscape.certsrv.key.KeyDataInfo;
import com.netscape.certsrv.key.KeyDataInfos;
import com.netscape.certsrv.key.KeyRecoveryRequest;
import com.netscape.certsrv.key.KeyRequestInfo;
import com.netscape.certsrv.key.KeyRequestInfos;
import com.netscape.certsrv.key.KeyRequestResource;
import com.netscape.certsrv.key.KeyResource;
import com.netscape.certsrv.request.RequestId;
import com.netscape.certsrv.selftests.SelfTestClient;
import com.netscape.certsrv.system.SystemCertResource;
import com.netscape.certsrv.user.UserClient;
import com.netscape.cmsutil.util.Utils;

public class KRAClient extends SubsystemClient {

    private KeyResource keyClient;
    private KeyRequestResource keyRequestClient;
    private SystemCertResource systemCertClient;

    public KRAClient(PKIClient client) throws URISyntaxException {
        super(client, "kra");
        init();
    }

    public void init() throws URISyntaxException {

        addClient(new GroupClient(client, name));
        addClient(new SelfTestClient(client, name));
        addClient(new UserClient(client, name));

        systemCertClient = createProxy(SystemCertResource.class);
        keyRequestClient = createProxy(KeyRequestResource.class);
        keyClient = createProxy(KeyResource.class);
    }

    public String getTransportCert() {
        @SuppressWarnings("unchecked")
        ClientResponse<CertData> response = (ClientResponse<CertData>) systemCertClient
                .getTransportCert();
        CertData certData = client.getEntity(response);
        String transportCert = certData.getEncoded();
        return transportCert;
    }

    public Collection<KeyRequestInfo> listRequests(String requestState, String requestType) {
        KeyRequestInfos infos = keyRequestClient.listRequests(
                requestState, requestType, null, new RequestId(0), 100, 100, 10
                );
        Collection<KeyRequestInfo> list = infos.getRequests();
        return list;
    }

    public KeyRequestInfo archiveSecurityData(byte[] encoded, String clientId, String dataType) {
        // create archival request
        KeyArchivalRequest data = new KeyArchivalRequest();
        String req1 = Utils.base64encode(encoded);
        data.setWrappedPrivateData(req1);
        data.setClientId(clientId);
        data.setDataType(dataType);

        KeyRequestInfo info = keyRequestClient.archiveKey(data);
        return info;
    }

    public KeyDataInfo getKeyData(String clientId, String status) {
        KeyDataInfos infos = keyClient.listKeys(clientId, status, 100, 10);
        Collection<KeyDataInfo> list = infos.getKeyInfos();
        Iterator<KeyDataInfo> iter = list.iterator();

        while (iter.hasNext()) {
            KeyDataInfo info = iter.next();
            if (info != null) {
                // return the first one
                return info;
            }
        }
        return null;
    }

    public KeyRequestInfo requestRecovery(KeyId keyId, byte[] rpwd, byte[] rkey, byte[] nonceData) {
        // create recovery request
        KeyRecoveryRequest data = new KeyRecoveryRequest();
        data.setKeyId(keyId);
        if (rpwd != null) {
            data.setSessionWrappedPassphrase(Utils.base64encode(rpwd));
        }
        if (rkey != null) {
            data.setTransWrappedSessionKey(Utils.base64encode(rkey));
        }

        if (nonceData != null) {
            data.setNonceData(Utils.base64encode(nonceData));
        }

        KeyRequestInfo info = keyRequestClient.recoverKey(data);
        return info;
    }

    public void approveRecovery(RequestId recoveryId) {
        keyRequestClient.approveRequest(recoveryId);
    }

    public KeyData retrieveKey(KeyId keyId, RequestId requestId, byte[] rpwd, byte[] rkey, byte[] nonceData) {
        // create recovery request
        KeyRecoveryRequest data = new KeyRecoveryRequest();
        data.setKeyId(keyId);
        data.setRequestId(requestId);
        if (rkey != null) {
            data.setTransWrappedSessionKey(Utils.base64encode(rkey));
        }
        if (rpwd != null) {
            data.setSessionWrappedPassphrase(Utils.base64encode(rpwd));
        }

        if (nonceData != null) {
            data.setNonceData(Utils.base64encode(nonceData));
        }

        KeyData key = keyClient.retrieveKey(data);
        return key;
    }

    public KeyRequestInfo getRequest(RequestId id) {
        return keyRequestClient.getRequestInfo(id);
    }
}
