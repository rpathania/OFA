/*$Header: XXSTtemplateHomeCO.java 004.000.000 2021-03-09 RPATHANIA$*/
package xxst.oracle.apps.xdo.oa.template.webui;

import oracle.apps.fnd.framework.OAException;
import oracle.apps.fnd.framework.OAFwkConstants;
import oracle.apps.fnd.framework.webui.OAPageContext;
import oracle.apps.fnd.framework.webui.beans.OAWebBean;
import oracle.apps.fnd.framework.webui.beans.message.OAMessageTextInputBean;
import oracle.apps.xdo.oa.template.webui.TemplatesHomeCO;

public class XXSTtemplateHomeCO extends TemplatesHomeCO
{
    public XXSTtemplateHomeCO() {
    }
    
    public void processRequest(final OAPageContext oaPageContext, final OAWebBean oaWebBean) {
        super.processRequest(oaPageContext, oaWebBean);
        
        oaPageContext.writeDiagnostics(this, "Entered the PR method of XXSTTemplatesHomeCO",OAFwkConstants.STATEMENT);
          
        
        
          OAMessageTextInputBean fieldName =(OAMessageTextInputBean)oaWebBean.findChildRecursive("Name");

          fieldName.setText(oaPageContext,"Rishi_test");  
          
          throw new OAException("Calling the custom CO : XXSTTemplatesHomeCO", OAException.INFORMATION);
        
    }
    
    
    public void processFormRequest(final OAPageContext oaPageContext, final OAWebBean oaWebBean) {
        super.processFormRequest(oaPageContext, oaWebBean);
    }
}
