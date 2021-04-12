package xxst.oracle.apps.hxc.selfservice.webui;

import oracle.apps.fnd.common.VersionInfo;
import oracle.apps.fnd.framework.OAApplicationModule;
import oracle.apps.fnd.framework.webui.OAPageContext;
import oracle.apps.fnd.framework.webui.beans.OAWebBean;
import oracle.apps.fnd.framework.webui.beans.layout.OAStackLayoutBean;
import oracle.apps.hxc.selfservice.timecard.webui.TimecardCO;
import oracle.cabo.ui.UINode;
import xxst.oracle.apps.hxc.selfservice.webui.TcLayoutHelper;
import xxst.oracle.apps.hxc.selfservice.webui.XXSTCustomTimecardCO;
import xxst.oracle.apps.hxc.selfservice.webui.XXSTTcActivitiesCO;
import xxst.oracle.apps.hxc.selfservice.webui.XXSTTimecardCO;

public class XXSTCustomTimecardCO extends TimecardCO {
  public static final String RCS_ID = "$Header$";
  
  public static final boolean RCS_ID_RECORDED = VersionInfo.recordClassVersion("$Header$", "%packagename%");
  
  String startTime;
  
  String resourceId;
  
  public void processRequest(OAPageContext pageContext, OAWebBean webBean) {
    super.processRequest(pageContext, webBean);
    String ntfId = pageContext.getParameter("NtfId");
    this.startTime = pageContext.getParameter(XXSTTimecardCO.START_TIME_PARAM);
    this.resourceId = pageContext.getParameter(XXSTTimecardCO.RESOURCE_ID_PARAM);
    if (ntfId != null && XXSTTcActivitiesCO.useOTLOAF(pageContext, webBean)) {
      OAApplicationModule root = pageContext.getRootApplicationModule();
      OAWebBean tt = TcLayoutHelper.findStdTimecardTable(webBean);
      if (tt != null) {
        ((OAWebBean)tt.getIndexedChild(1)).setRendered(false);
        if (root.findApplicationModule("XXSTTimecardAM") == null)
          root.createApplicationModule("XXSTTimecardAM", "xxst.oracle.apps.hxc.selfservice.server.XXSTTimecardAM"); 
        OAStackLayoutBean stack = (OAStackLayoutBean)createWebBean(pageContext, "STACK_LAYOUT", null, "xxstStack");
        stack.setApplicationModuleDefinitionName("xxst.oracle.apps.hxc.selfservice.server.XXSTTimecardAM");
        stack.setApplicationModuleUsageName("XXSTTimecardAM");
        OAStackLayoutBean custRegion = (OAStackLayoutBean)createWebBean(pageContext, "/xxst/oracle/apps/hxc/selfservice/webui/XXSTTimecardRN", null, true);
        stack.addIndexedChild((UINode)custRegion);
        tt.addIndexedChild(1, (UINode)stack);
      } 
    } 
  }
  
  public void processFormRequest(OAPageContext pageContext, OAWebBean webBean) {
    pageContext.putParameter(XXSTTimecardCO.START_TIME_PARAM, this.startTime);
    pageContext.putParameter(XXSTTimecardCO.RESOURCE_ID_PARAM, this.resourceId);
    super.processFormRequest(pageContext, webBean);
  }
}
