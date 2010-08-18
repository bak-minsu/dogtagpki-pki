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
package com.netscape.cmscore.dbs;


import java.util.*;
import java.math.*;
import netscape.ldap.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.dbs.*;


/**
 * A class represent mapper for metainfo attribute. Metainfo
 * is in format of the following:
 *
 * <PRE>
 * metaInfoType:metaInfoValue
 * metaInfoType:metaInfoValue
 * metaInfoType:metaInfoValue
 * metaInfoType:metaInfoValue
 * </PRE>
 *
 * @author thomask
 * @version $Revision$, $Date$ 
 */
public class MetaInfoMapper implements IDBAttrMapper {

    public static final String SEP = ":";

    private String mLdapName = null;
    private Vector v = new Vector();

    /**
     * Constructs a metainfo object.
     */
    public MetaInfoMapper(String ldapName) {
        mLdapName = ldapName;
        v.addElement(mLdapName);
    }

    /**
     * Returns a list of supported ldap attribute names.
     */
    public Enumeration getSupportedLDAPAttributeNames() {
        return v.elements();
    }

    /**
     * Maps object into ldap attribute set.
     */
    public void mapObjectToLDAPAttributeSet(IDBObj parent,
        String name, Object obj, LDAPAttributeSet attrs)
        throws EBaseException {
        MetaInfo info = (MetaInfo) obj;
        Enumeration e = info.getElements();

        if (!e.hasMoreElements())
            return; // dont add anything
        LDAPAttribute attr = new LDAPAttribute(mLdapName);

        while (e.hasMoreElements()) {
            String s = null;
            String attrName = (String) e.nextElement();
            String value = (String) info.get(attrName);

            s = attrName + SEP + value;
            attr.addValue(s);
        }
        attrs.add(attr);
    }

    /**
     * Maps LDAP attributes into object, and put the object into
     * 'parent'.
     */
    public void mapLDAPAttributeSetToObject(LDAPAttributeSet attrs,
        String name, IDBObj parent) throws EBaseException {
        LDAPAttribute attr = attrs.getAttribute(mLdapName);

        if (attr == null)
            return;
        Enumeration values = attr.getStringValues();
        MetaInfo info = new MetaInfo();

        while (values.hasMoreElements()) {
            String s = (String) values.nextElement();
            StringTokenizer st = new StringTokenizer(s, SEP);

            info.set(st.nextToken(), st.nextToken());
        }
        parent.set(name, info);
    }

    /**
     * Map search filters into LDAP search filter.
     * Possible search filter:
     * (&(metaInfo=reserver0:value0)(metaInfo=reserved1:value1))
     */
    public String mapSearchFilter(String name, String op,
        String value) throws EBaseException {
        return mLdapName + op + value;
    }
}
