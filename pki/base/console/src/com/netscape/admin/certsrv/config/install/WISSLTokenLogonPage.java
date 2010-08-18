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
package com.netscape.admin.certsrv.config.install;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import com.netscape.admin.certsrv.*;
import com.netscape.admin.certsrv.connection.*;
import com.netscape.admin.certsrv.wizard.*;
import com.netscape.certsrv.common.*;
import com.netscape.admin.certsrv.task.*;
import com.netscape.management.client.console.*;

/**
 * This panel asks for the information of the current internal database.
 *
 * @author Christine Ho
 * @version $Revision$, $Date$
 * @see com.netscape.admin.certsrv.config.install
 */
class WISSLTokenLogonPage extends WITokenLogonPage implements IWizardPanel {

    private static final String HELPINDEX = "install-ssltoken-logon-wizard-help";
    private static final String PANELNAME = "SSLTOKENLOGONWIZARD";

    WISSLTokenLogonPage(JDialog dialog) {
        super(PANELNAME);
        mHelpIndex = HELPINDEX;
        mParent = dialog;
    }

    WISSLTokenLogonPage(JDialog dialog, JFrame adminFrame) {
        super(PANELNAME);
        mHelpIndex = HELPINDEX;
        mParent = dialog;
        mAdminFrame = adminFrame;
    }

    public boolean initializePanel(WizardInfo info) {
        InstallWizardInfo wizardInfo = (InstallWizardInfo)info;
        String tokenname = wizardInfo.getSSLTokenName();
        String pwd = (String)wizardInfo.get("TOKEN:"+tokenname);
        if (wizardInfo.isCloning())
            return false;
        if (wizardInfo.isSSLCertLocalCA() || !wizardInfo.isInstallCertNow() ||
          wizardInfo.isMigrationEnable() || wizardInfo.isSSLCertInstalledDone())
            return false;
        if (pwd != null)
            return false;

        mTokenName = tokenname;
        mTokenText.setText(tokenname);
        return super.initializePanel(info);
    }
}

