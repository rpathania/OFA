package xxst.oracle.apps.ar.hz.components.account.search.webui;

import oracle.apps.fnd.common.VersionInfo;
import oracle.apps.ar.hz.components.util.webui.HzPuiWebuiUtil;
import oracle.apps.fnd.framework.webui.beans.table.OATableBean;
import oracle.apps.fnd.framework.webui.beans.OAWebBean;
import oracle.apps.fnd.framework.webui.OAPageContext;
import com.sun.java.util.collections.ArrayList;
import oracle.apps.ar.hz.components.account.search.webui.HzPuiAcctSearchResultsCO;

public class XXSTHzPuiAcctSearchResultsCO extends HzPuiAcctSearchResultsCO
{
    public static final String RCS_ID = "$Header: XXSTHzPuiAcctSearchResultsCO.java 121.13 2021/03/31 05:56:45 rpathania $";
    public static final boolean RCS_ID_RECORDED;
    private ArrayList viewParams;
    private ArrayList views;
    
    public void processRequest(final OAPageContext oaPageContext, final OAWebBean oaWebBean) {
        super.processRequest(oaPageContext, oaWebBean);
        if ("N".equals(oaPageContext.getProfile("XXST_AR_CUSTOMER_FULL_ACCESS"))) {
            final OATableBean oaTableBean = (OATableBean)oaWebBean.findIndexedChildRecursive("HzPuiAcctSearchResultsTable");
            if (oaTableBean != null) {
                final OAWebBean oaWebBean2 = (OAWebBean)oaTableBean.getTableActions();
                if (oaWebBean2 != null) {
                    final OAWebBean indexedChildRecursive = oaWebBean2.findIndexedChildRecursive("HzPuiCreate");
                    if (indexedChildRecursive != null) {
                        indexedChildRecursive.setRendered(false);
                    }
                }
            }
        }
    }
    
    private String getViewName(final OAPageContext oaPageContext) {
        if (oaPageContext.isLoggingEnabled(2)) {
            oaPageContext.writeDiagnostics((Object)this, "HZPUI: " + this.getClass().getName() + ".getViewName(OAPageContext) starts.", 2);
        }
        if (this.views == null) {
            this.views = new ArrayList(2);
            this.viewParams = new ArrayList(2);
            this.views.add((Object)"HZ_CONTACT_FIND_V");
            this.viewParams.add((Object)new String[] { "ContactFirstName", "ContactLastName", "ContactPhoneNumber" });
            this.views.add((Object)"HZ_CUSTOMER_PARTY_SITE_FIND_V");
            this.viewParams.add((Object)new String[] { "Address1", "Address2", "City", "State", "PostalCode", "County", "Province", "Country" });
        }
        String str = "HZ_CUSTOMER_PARTY_FIND_V";
        int n = 1;
        while (true) {
            for (int i = 0; i < this.views.size(); ++i) {
                if (oaPageContext.isLoggingEnabled(1)) {
                    oaPageContext.writeDiagnostics((Object)this, "views[" + i + "] = " + this.views.get(i), 1);
                }
                final String[] array = (String[])this.viewParams.get(i);
                for (int j = 0; j < array.length; ++j) {
                    if (n == 0) {
                        break;
                    }
                    if (oaPageContext.isLoggingEnabled(1)) {
                        oaPageContext.writeDiagnostics((Object)this, "criterias[" + j + "] = " + oaPageContext.getParameter(array[j]), 1);
                    }
                    if (!HzPuiWebuiUtil.isEmpty(oaPageContext.getParameter(array[j]))) {
                        n = 0;
                        break;
                    }
                }
                if (n == 0) {
                    str = (String)this.views.get(i);
                    if (oaPageContext.isLoggingEnabled(1)) {
                        oaPageContext.writeDiagnostics((Object)this, "viewName = " + str, 1);
                    }
                    if (oaPageContext.isLoggingEnabled(2)) {
                        oaPageContext.writeDiagnostics((Object)this, "HZPUI: " + this.getClass().getName() + ".getViewName(OAPageContext) done.", 2);
                    }
                    return str;
                }
            }
            continue;
        }
    }
    
    static {
        RCS_ID_RECORDED = VersionInfo.recordClassVersion("$Header: XXSTHzPuiAcctSearchResultsCO.java 121.13 2021/03/31 05:56:45 rpathania $", "%packagename%");
    }
}