/*$Header: XXSTSuppSummCO.java 002.000.000 2021-03-09 RPATHANIA$*/
package xxst.oracle.apps.pos.supplier.webui;

import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.HashMap;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.StringTokenizer;
import oracle.apps.fnd.common.VersionInfo;
import oracle.apps.fnd.framework.*;
import oracle.apps.fnd.framework.server.OADBTransaction;
import oracle.apps.fnd.framework.webui.*;
import oracle.apps.fnd.framework.webui.beans.OAWebBean;
import oracle.apps.fnd.framework.webui.beans.OAWebBeanTextInput;
import oracle.apps.fnd.framework.webui.beans.form.OASubmitButtonBean;
import oracle.apps.fnd.framework.webui.beans.layout.OAMessageComponentLayoutBean;
import oracle.apps.fnd.framework.webui.beans.message.*;
import oracle.apps.fnd.framework.webui.beans.table.OATableBean;
import oracle.apps.pos.sdh.ext.common.util.SuppSearchActionUtil;
import oracle.apps.pos.sdh.ext.security.webui.SuppPrivilegeContext;

import oracle.apps.pos.supplier.webui.SuppSummCO;
import oracle.apps.fnd.framework.webui.beans.nav.OAButtonBean;

public class XXSTSuppSummCO extends SuppSummCO
{
  public XXSTSuppSummCO()
  {
    super();
  }
  
  public void processRequest(OAPageContext oapagecontext, OAWebBean oawebbean)
  {
    super.processRequest(oapagecontext, oawebbean);
    
    String profileSupCreatBtn = oapagecontext.getProfile("XXST_AP_PO_VENDOR_ACCESS");
    
    if("N".equals(profileSupCreatBtn))
    {
        OAButtonBean oab = (OAButtonBean)oawebbean.findChildRecursive("supCreatBtn");
        if(oab != null)
        {
          oab.setRendered(false); 
        }
    }
  }
  
  public void processFormRequest(OAPageContext oapagecontext, OAWebBean oawebbean)
  {
    super.processFormRequest(oapagecontext, oawebbean);
  }

  public static final String RCS_ID = "$Header: XXSTSuppSummCO.java 120.13.12010000.22 2010/03/08 23:41:39 ankohli ship $";
  public static final boolean RCS_ID_RECORDED = VersionInfo.recordClassVersion("$Header: XXSTSuppSummCO.java 120.13.12010000.22 2010/03/08 23:41:39 ankohli ship $", "xxst.oracle.apps.pos.supplier.webui");
}
