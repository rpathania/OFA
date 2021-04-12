/*     */ import com.sun.java.util.collections.ArrayList;
/*     */ import oracle.apps.ar.hz.components.account.search.webui.HzPuiAcctSearchResultsCO;
/*     */ import oracle.apps.ar.hz.components.util.webui.HzPuiWebuiUtil;
/*     */ import oracle.apps.fnd.common.VersionInfo;
/*     */ import oracle.apps.fnd.framework.webui.OAPageContext;
/*     */ import oracle.apps.fnd.framework.webui.beans.OAWebBean;
/*     */ import oracle.apps.fnd.framework.webui.beans.table.OATableBean;
/*     */ import xxst.oracle.apps.ar.hz.components.account.search.webui.XXSTHzPuiAcctSearchResultsCO;
/*     */ 
/*     */ public class XXSTHzPuiAcctSearchResultsCO extends HzPuiAcctSearchResultsCO {
/*     */   public static final String RCS_ID = "$Header: XXSTHzPuiAcctSearchResultsCO.java 120.12 2007/10/15 05:56:45 manjayar ship $";
/*     */   
/*     */   public void processRequest(OAPageContext paramOAPageContext, OAWebBean paramOAWebBean) {
/*  28 */     super.processRequest(paramOAPageContext, paramOAWebBean);
/*  30 */     String str = paramOAPageContext.getProfile("XXST_AR_CUSTOMER_FULL_ACCESS");
/*  31 */     if ("N".equals(str)) {
/*  33 */       OATableBean oATableBean = (OATableBean)paramOAWebBean.findIndexedChildRecursive("HzPuiAcctSearchResultsTable");
/*  34 */       if (oATableBean != null) {
/*  36 */         OAWebBean oAWebBean = (OAWebBean)oATableBean.getTableActions();
/*  37 */         if (oAWebBean != null) {
/*  39 */           OAWebBean oAWebBean1 = oAWebBean.findIndexedChildRecursive("HzPuiCreate");
/*  40 */           if (oAWebBean1 != null)
/*  42 */             oAWebBean1.setRendered(false); 
/*     */         } 
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   private String getViewName(OAPageContext paramOAPageContext) {
/*  51 */     if (paramOAPageContext.isLoggingEnabled(2))
/*  52 */       paramOAPageContext.writeDiagnostics(this, "HZPUI: " + getClass().getName() + ".getViewName(OAPageContext) starts.", 2); 
/*  53 */     if (this.views == null) {
/*  55 */       this.views = new ArrayList(2);
/*  56 */       this.viewParams = new ArrayList(2);
/*  57 */       this.views.add("HZ_CONTACT_FIND_V");
/*  58 */       this.viewParams.add(new String[] { "ContactFirstName", "ContactLastName", "ContactPhoneNumber" });
/*  61 */       this.views.add("HZ_CUSTOMER_PARTY_SITE_FIND_V");
/*  62 */       this.viewParams.add(new String[] { "Address1", "Address2", "City", "State", "PostalCode", "County", "Province", "Country" });
/*     */     } 
/*  66 */     String str = "HZ_CUSTOMER_PARTY_FIND_V";
/*  67 */     boolean bool = true;
/*  68 */     byte b = 0;
/*  71 */     while (b < this.views.size()) {
/*  73 */       if (paramOAPageContext.isLoggingEnabled(1))
/*  74 */         paramOAPageContext.writeDiagnostics(this, "views[" + b + "] = " + this.views.get(b), 1); 
/*  75 */       String[] arrayOfString = (String[])this.viewParams.get(b);
/*  76 */       byte b1 = 0;
/*  79 */       while (b1 < arrayOfString.length && bool) {
/*  81 */         if (paramOAPageContext.isLoggingEnabled(1))
/*  82 */           paramOAPageContext.writeDiagnostics(this, "criterias[" + b1 + "] = " + paramOAPageContext.getParameter(arrayOfString[b1]), 1); 
/*  83 */         if (!HzPuiWebuiUtil.isEmpty(paramOAPageContext.getParameter(arrayOfString[b1]))) {
/*  85 */           bool = false;
/*     */           break;
/*     */         } 
/*  88 */         b1++;
/*     */       } 
/*  90 */       if (!bool) {
/*  92 */         str = (String)this.views.get(b);
/*     */         break;
/*     */       } 
/*  95 */       b++;
/*     */     } 
/*  97 */     if (paramOAPageContext.isLoggingEnabled(1))
/*  98 */       paramOAPageContext.writeDiagnostics(this, "viewName = " + str, 1); 
/*  99 */     if (paramOAPageContext.isLoggingEnabled(2))
/* 100 */       paramOAPageContext.writeDiagnostics(this, "HZPUI: " + getClass().getName() + ".getViewName(OAPageContext) done.", 2); 
/* 101 */     return str;
/*     */   }
/*     */   
/* 105 */   public static final boolean RCS_ID_RECORDED = VersionInfo.recordClassVersion("$Header: XXSTHzPuiAcctSearchResultsCO.java 120.12 2007/10/15 05:56:45 manjayar ship $", "%packagename%");
/*     */   
/*     */   private ArrayList viewParams;
/*     */   
/*     */   private ArrayList views;
/*     */ }


/* Location:              C:\Users\rpathania\Desktop\Monthly Work\OFA\12.2_Migration\Customers\!\XXSTHzPuiAcctSearchResultsCO.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */