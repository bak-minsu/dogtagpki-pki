// --- BEGIN COPYRIGHT BLOCK ---
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; version 2 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
// (C) 2007 Red Hat, Inc.
// All rights reserved.
// --- END COPYRIGHT BLOCK ---
package com.netscape.cms.servlet.key;


import com.netscape.cms.servlet.common.*;
import com.netscape.cms.servlet.base.*;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.security.*;
import javax.servlet.*;
import javax.servlet.http.*;
import netscape.security.x509.*;
import com.netscape.certsrv.common.*;
import com.netscape.certsrv.authority.*;
import com.netscape.certsrv.base.*;
 
import com.netscape.cms.servlet.*;
import com.netscape.certsrv.dbs.*;
import com.netscape.certsrv.dbs.keydb.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.kra.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.authentication.*;
import com.netscape.certsrv.authorization.*;


/**
 * Display a specific Key Archival Request
 * <P>
 *
 * @version $Revision$, $Date$
 */
public class DisplayBySerial extends CMSServlet {

    private final static String INFO = "displayBySerial";
    private final static String TPL_FILE = "displayBySerial.template";

    private final static String IN_SERIALNO = "serialNumber";
    private final static String OUT_OP = "op";
    private final static String OUT_SERVICE_URL = "serviceURL";
    private final static String OUT_ERROR = "errorDetails";

    private IKeyRepository mKeyDB = null;
    private String mFormPath = null;

    /**
     * Constructs displayBySerial servlet.
     */
    public DisplayBySerial() {
        super();
    }

    /**
     * initialize the servlet. This servlet uses the template file
     * "displayBySerial.template" to process the response.
     *
     * @param sc servlet configuration, read from the web.xml file
     */
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
        mFormPath = "/" + mAuthority.getId() + "/" + TPL_FILE;
        mKeyDB = ((IKeyRecoveryAuthority) mAuthority).getKeyRepository();

        mTemplates.remove(CMSRequest.SUCCESS);
        if (mOutputTemplatePath != null)
            mFormPath = mOutputTemplatePath;
    }

    /**
     * Returns serlvet information.
     */
    public String getServletInfo() { 
        return INFO; 
    }

    /**
     * Process the HTTP request.
     * <ul>
     * <li>http.param serialNumber serial number of the key archival request
     * </ul>
     *
     * @param cmsReq the object holding the request and response information
     */
    public void process(CMSRequest cmsReq) throws EBaseException {

        HttpServletRequest req = cmsReq.getHttpReq();
        HttpServletResponse resp = cmsReq.getHttpResp();

        IAuthToken authToken = authenticate(cmsReq);
        AuthzToken authzToken = null;

        try {
            authzToken = authorize(mAclMethod, authToken,
                        mAuthzResourceName, "read");
        } catch (EAuthzAccessDenied e) {
            log(ILogger.LL_FAILURE,
                CMS.getLogMessage("ADMIN_SRVLT_AUTH_FAILURE", e.toString()));
        } catch (Exception e) {
            log(ILogger.LL_FAILURE,
                CMS.getLogMessage("ADMIN_SRVLT_AUTH_FAILURE", e.toString()));
        }

        if (authzToken == null) {
            cmsReq.setStatus(CMSRequest.UNAUTHORIZED);
            return;
        }

        CMSTemplate form = null;
        Locale[] locale = new Locale[1];

        try {
            form = getTemplate(mFormPath, req, locale);
        } catch (IOException e) {
            log(ILogger.LL_FAILURE,
                CMS.getLogMessage("CMSGW_ERR_GET_TEMPLATE", mFormPath, e.toString()));
            throw new ECMSGWException(
              CMS.getUserMessage("CMS_GW_DISPLAY_TEMPLATE_ERROR"));
        }

        // Note that we should try to handle all the exceptions
        // instead of passing it up back to the servlet 
        // framework.

        IArgBlock header = CMS.createArgBlock();
        IArgBlock fixed = CMS.createArgBlock();
        CMSTemplateParams argSet = new CMSTemplateParams(header, fixed);
        int seqNum = -1;

        try {
            if (req.getParameter(IN_SERIALNO) != null) {
                seqNum = Integer.parseInt(
                            req.getParameter(IN_SERIALNO));
            }
            process(argSet, header, seqNum, req, resp, locale[0]);
        } catch (NumberFormatException e) {
            header.addStringValue(OUT_ERROR,
                CMS.getUserMessage(locale[0], "CMS_BASE_INTERNAL_ERROR", e.toString()));
        }

        try {
            ServletOutputStream out = resp.getOutputStream();

            resp.setContentType("text/html");
            form.renderOutput(out, argSet);
        } catch (IOException e) {
            log(ILogger.LL_FAILURE,
                CMS.getLogMessage("CMSGW_ERR_STREAM_TEMPLATE", e.toString()));
            throw new ECMSGWException(
              CMS.getUserMessage("CMS_GW_DISPLAY_TEMPLATE_ERROR"));
        }
    }

    /**
     * Display information about a particular key.
     */
    private void process(CMSTemplateParams argSet,
        IArgBlock header, int seq, 
        HttpServletRequest req, HttpServletResponse resp,
        Locale locale) {
        try {
            header.addStringValue(OUT_OP,
                req.getParameter(OUT_OP));
            header.addStringValue(OUT_SERVICE_URL,
                req.getRequestURI());
            IKeyRecord rec = (IKeyRecord) mKeyDB.readKeyRecord(new 
                    BigInteger(Integer.toString(seq)));

            KeyRecordParser.fillRecordIntoArg(rec, header);
        } catch (EBaseException e) {
            header.addStringValue(OUT_ERROR, e.toString(locale));
        }
    }
}
