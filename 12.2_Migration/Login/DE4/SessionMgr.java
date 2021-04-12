/*      */ package xxst.oracle.apps.fnd.sso;
		   import java.math.BigDecimal;
/*      */ import java.sql.CallableStatement;
/*      */ import java.sql.Connection;
/*      */ import java.sql.PreparedStatement;
/*      */ import java.sql.ResultSet;
/*      */ import java.sql.SQLException;
/*      */ import java.sql.Statement;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Vector;
/*      */ import javax.servlet.http.Cookie;
/*      */ import javax.servlet.http.HttpServletRequest;
/*      */ import javax.servlet.http.HttpServletResponse;
/*      */ import javax.servlet.http.HttpSession;
/*      */ import oracle.apps.fnd.common.AppsContext;
/*      */ import oracle.apps.fnd.common.AppsEnvironmentStore;
/*      */ import oracle.apps.fnd.common.AppsLog;
/*      */ import oracle.apps.fnd.common.ErrorStack;
/*      */ import oracle.apps.fnd.common.LangInfo;
/*      */ import oracle.apps.fnd.common.Message;
/*      */ import oracle.apps.fnd.common.ProfileStore;
/*      */ import oracle.apps.fnd.common.VersionInfo;
/*      */ import oracle.apps.fnd.common.WebAppsContext;
/*      */ import oracle.apps.fnd.framework.webui.OAPageContext;
/*      */ import oracle.apps.fnd.login.lang.LanguageContext;
/*      */ import oracle.apps.fnd.profiles.Profiles;
/*      */ import oracle.apps.fnd.proxy.ProxyUserUtil;
/*      */ import oracle.apps.fnd.security.HTMLProcessor;
/*      */ import oracle.apps.fnd.security.SessionManager;
/*      */ import oracle.apps.fnd.security.UserPwd;
/*      */ import oracle.apps.fnd.sso.AppsAgent;
/*      */ import oracle.apps.fnd.sso.AuthenticationException;
/*      */ import oracle.apps.fnd.sso.Authenticator;
/*      */ import oracle.apps.fnd.sso.SSOAppsUser;
/*      */ import oracle.apps.fnd.sso.SSOCommon;
/*      */ import oracle.apps.fnd.sso.SSOManager;
/*      */ import oracle.apps.fnd.sso.SSOUtil;
/*      */ import oracle.apps.fnd.sso.SessionMgr;
/*      */ import oracle.apps.fnd.sso.URLHelper;
/*      */ import oracle.apps.fnd.sso.Utils;
/*      */ import oracle.apps.fnd.util.JDBC;
/*      */ import oracle.apps.fnd.util.URLEncoder;
/*      */ import oracle.cabo.ui.data.DataObject;
/*      */ import oracle.jdbc.OracleCallableStatement;
/*      */ import oracle.jdbc.OracleConnection;
/*      */ import oracle.jdbc.OraclePreparedStatement;
/*      */ 
/*      */ public class SessionMgr {
/*      */   public static final String RCS_ID = "$Header: SessionMgr.java 120.58.12020000.13 2017/05/19 17:09:14 ctilley ship $";
/*      */   
/*  119 */   public static final boolean RCS_ID_RECORDED = VersionInfo.recordClassVersion("$Header: SessionMgr.java 120.58.12020000.13 2017/05/19 17:09:14 ctilley ship $", "oracle.apps.fnd.sso");
/*      */   
/*      */   private static final String className = "oracle.apps.fnd.sso.SessionMgr[$Revision: 115.112.2.10].";
/*      */   
/*      */   private static final int PV_USER_ID = 10;
/*      */   
/*      */   private static final int PV_SESSION_ID = 23;
/*      */   
/*      */   private static final int PV_USER_NAME = 99;
/*      */   
/*      */   private static final String INVALID_SESSION = "-1";
/*      */   
/*      */   private static final String DEFAULT_CHARSET = "ISO-8859-1";
/*      */   
/*  146 */   private static Vector installedLanguagesInfo = new Vector();
/*      */   
/*  150 */   private static Vector installedLanguagesCode = new Vector();
/*      */   
/*  155 */   private static Vector installedLanguagesDesc = new Vector();
/*      */   
/*      */   private static boolean initLang = false;
/*      */   
/*  164 */   private static String sessionCookieName = null;
/*      */   
/*  169 */   private static String loadedLang = null;
/*      */   
/*  174 */   private static Vector loadedLanguages = new Vector();
/*      */   
/*  179 */   private static Hashtable installedSpecificLangInfoTable = new Hashtable<Object, Object>();
/*      */   
/*  183 */   private static Hashtable installedSpecificLangDescTable = new Hashtable<Object, Object>();
/*      */   
/*  188 */   private static String protocol = null;
/*      */   
/*      */   public static String getAppsCookie(HttpServletRequest paramHttpServletRequest) {
/*  198 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.getAppsCookie(HttpServletRequest)";
/*  200 */     WebAppsContext webAppsContext = null;
/*  201 */     boolean bool = false;
/*  202 */     String str2 = null;
/*  204 */     if (Utils.isAppsContextAvailable()) {
/*  205 */       webAppsContext = Utils.getAppsContext();
/*  206 */       bool = true;
/*      */     } else {
/*  208 */       webAppsContext = Utils.getAppsContext();
/*      */     } 
/*  210 */     if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/*  211 */       Utils.writeToLog(str1, "BEGIN", webAppsContext, 2); 
/*      */     try {
/*  213 */       Cookie[] arrayOfCookie = paramHttpServletRequest.getCookies();
/*  214 */       String str = getSessionCookieName(webAppsContext);
/*  216 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/*  217 */         Utils.writeToLog(str1, "Cookie Name: " + str, webAppsContext); 
/*  219 */       for (byte b = 0; arrayOfCookie != null && b < arrayOfCookie.length; b++) {
/*  220 */         if (arrayOfCookie[b].getName().equals(str)) {
/*  221 */           str2 = arrayOfCookie[b].getValue();
/*      */           break;
/*      */         } 
/*      */       } 
/*      */     } finally {
/*  227 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/*  228 */         Utils.writeToLog(str1, "END return Value: " + str2, webAppsContext, 2); 
/*  229 */       if (!bool)
/*  230 */         Utils.releaseAppsContext(); 
/*      */     } 
/*  233 */     return str2;
/*      */   }
/*      */   
/*      */   protected static String getServerDomain(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse) {
/*  248 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.getServerDomain(HttpServletRequest,  HttpServletResponse)";
/*  251 */     String str2 = null;
/*  252 */     WebAppsContext webAppsContext = null;
/*  253 */     boolean bool = false;
/*  254 */     if (Utils.isAppsContextAvailable()) {
/*  255 */       webAppsContext = Utils.getAppsContext();
/*  256 */       bool = true;
/*      */     } else {
/*  258 */       webAppsContext = Utils.getAppsContext();
/*      */     } 
/*      */     try {
/*  262 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/*  263 */         Utils.writeToLog(str1, "BEGIN", webAppsContext, 2); 
/*  264 */       str2 = webAppsContext.getSessionCookieDomain();
/*  265 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/*  266 */         Utils.writeToLog(str1, "domain: " + str2, webAppsContext); 
/*  268 */       if (str2 != null)
/*  269 */         return str2; 
/*      */     } finally {
/*  273 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/*  274 */         Utils.writeToLog(str1, "END returnValue: " + str2, webAppsContext, 2); 
/*  275 */       if (!bool)
/*  276 */         Utils.releaseAppsContext(); 
/*      */     } 
/*  280 */     return str2;
/*      */   }
/*      */   
/*      */   public static boolean changePassword(WebAppsContext paramWebAppsContext, String paramString, HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse) {
/*  297 */     boolean bool = false;
/*  298 */     Connection connection = null;
/*  299 */     boolean bool1 = false;
/*  300 */     String str = "oracle.apps.fnd.sso.SessionMgr.changePassword(WebAppsContext, String, HttpServletRequest, HttpServletResponse)";
/*  302 */     boolean bool2 = Utils.isAppsContextAvailable();
/*      */     try {
/*  305 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/*  306 */         Utils.writeToLog(str, "BEGIN", paramWebAppsContext, 2); 
/*  307 */       connection = Utils.getConnection(paramWebAppsContext);
/*  308 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  309 */         Utils.writeToLog(str, "username: " + paramString, paramWebAppsContext); 
/*  311 */       Utils.setRequestCharacterEncoding(paramHttpServletRequest);
/*  313 */       if (paramString != null && !paramString.equals("")) {
/*  314 */         String str1 = paramHttpServletRequest.getParameter("password");
/*  315 */         String str2 = paramHttpServletRequest.getParameter("newPassword");
/*  316 */         String str3 = paramHttpServletRequest.getParameter("newPassword2");
/*  318 */         if ((str1 == null || str1.equals("")) && (
/*  319 */           (AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  320 */           Utils.writeToLog(str, "Current password is NULL", paramWebAppsContext); 
/*  323 */         if ((str2 == null || str2.equals("")) && (
/*  324 */           (AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  325 */           Utils.writeToLog(str, "New password is NULL", paramWebAppsContext); 
/*  328 */         if ((str3 == null || str3.equals("")) && (
/*  329 */           (AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  330 */           Utils.writeToLog(str, "New password2 is NULL", paramWebAppsContext); 
/*  335 */         int i = Utils.getUserId(paramString, paramWebAppsContext);
/*  337 */         if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  338 */           Utils.writeToLog(str, "uid: " + i, paramWebAppsContext); 
/*  340 */         bool1 = paramWebAppsContext.getSessionManager().changePassword(paramString, str1, str2, str3, i);
/*  343 */         if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  344 */           Utils.writeToLog(str, "After calling changePassword: " + bool1, paramWebAppsContext); 
/*  346 */         if (bool1) {
/*  347 */           String str4 = paramHttpServletRequest.getParameter("langCode");
/*  349 */           if (str4 == null || "".equals(str4))
/*  351 */             str4 = paramWebAppsContext.getCurrLangCode(); 
/*  353 */           if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  354 */             Utils.writeToLog(str, "Attempting to create session for user: " + paramString + " in ::" + str4, paramWebAppsContext); 
/*  356 */           createSession(paramString, paramWebAppsContext, paramHttpServletRequest, paramHttpServletResponse, false, null, str4);
/*  357 */           if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  358 */             Utils.writeToLog(str, "After calling createSession: ", paramWebAppsContext); 
/*  359 */           bool = true;
/*      */         } else {
/*  361 */           bool = false;
/*      */         } 
/*      */       } 
/*  364 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/*  365 */         Utils.writeToLog(str, "END: return: " + bool, paramWebAppsContext, 2); 
/*      */     } finally {
/*  369 */       if (!bool2)
/*  369 */         Utils.releaseAppsContext(); 
/*      */     } 
/*  371 */     return bool;
/*      */   }
/*      */   
/*      */   static boolean validatePassword(String paramString1, String paramString2, String paramString3, WebAppsContext paramWebAppsContext) {
/*  393 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.validatePassword(String, String, String, WebAppsContext)";
/*  395 */     boolean bool = true;
/*  396 */     String str2 = null;
/*  397 */     int i = 5;
/*  398 */     Message message = null;
/*  400 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/*  401 */       Utils.writeToLog(str1, "BEGIN", paramWebAppsContext, 2); 
/*      */     try {
/*  403 */       str2 = paramWebAppsContext.getProfileStore().getProfile("SIGNON_PASSWORD_LENGTH");
/*  404 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  405 */         Utils.writeToLog(str1, "passwordLengthStr: " + str2, paramWebAppsContext); 
/*  407 */       if (str2 != null && !str2.equals("")) {
/*  408 */         i = Integer.parseInt(str2);
/*  409 */         if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  410 */           Utils.writeToLog(str1, "passwordLength: " + i, paramWebAppsContext); 
/*  412 */       } else if (str2 == null) {
/*  413 */         i = 5;
/*      */       } 
/*  415 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  416 */         Utils.writeToLog(str1, "passwordLength: " + i, paramWebAppsContext); 
/*  418 */       if (paramString2.length() < i) {
/*  419 */         message = new Message("FND", "FND_SSO_PASSWORD_TOO_SHORT");
/*  420 */         paramWebAppsContext.getErrorStack().addMessage(message);
/*  421 */         if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  422 */           Utils.writeToLog(str1, "New password lenth is too short", paramWebAppsContext); 
/*  423 */         return false;
/*      */       } 
/*  425 */       if (!paramString2.equals(paramString3)) {
/*  426 */         message = new Message("FND", "FND_USERADMIN_PASSWORD_DIFFER");
/*  427 */         paramWebAppsContext.getErrorStack().addMessage(message);
/*  428 */         if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  429 */           Utils.writeToLog(str1, "New password and confirmation differ", paramWebAppsContext); 
/*  431 */         return false;
/*      */       } 
/*  433 */     } catch (NumberFormatException numberFormatException) {
/*  434 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/*  435 */         Utils.writeToLog(str1, "Catching NumberFormatException" + Utils.getExceptionStackTrace(numberFormatException), paramWebAppsContext, 4); 
/*      */     } finally {}
/*  443 */     return bool;
/*      */   }
/*      */   
/*      */   static void createSession(String paramString1, WebAppsContext paramWebAppsContext, HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse, boolean paramBoolean, String paramString2) {
/*  459 */     createSession(paramString1, paramWebAppsContext, paramHttpServletRequest, paramHttpServletResponse, paramBoolean, paramString2, null);
/*      */   }
/*      */   
/*      */   static void createSession(String paramString1, WebAppsContext paramWebAppsContext, HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse, boolean paramBoolean, String paramString2, String paramString3) {
/*  477 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.createSession(String, WebAppsContext, HttpServletRequest, HttpServletResponse, boolean, String, String)";
/*  480 */     String str2 = null;
/*  482 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/*  483 */       Utils.writeToLog(str1, "BEGIN: Paramlist:  username: " + paramString1 + " wctx: " + paramWebAppsContext + " request: " + paramHttpServletRequest + " response: " + paramHttpServletResponse + " ssoMode: " + paramBoolean + " guid: " + paramString2 + " langCode: " + paramString3, paramWebAppsContext, 2); 
/*  493 */     String str3 = paramHttpServletRequest.getParameter("langCode");
/*  494 */     if (isInstalledLanguage(str3, paramWebAppsContext)) {
/*  496 */       paramString3 = str3;
/*  497 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  498 */         Utils.writeToLog(str1, "using lancode from request : " + paramString3, paramWebAppsContext); 
/*      */     } else {
/*  501 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  502 */         Utils.writeToLog(str1, "Before checking langCode: " + paramString3, paramWebAppsContext); 
/*  503 */       LanguageContext.getThreadLangContext().putUsername(paramString1);
/*  504 */       paramString3 = checkLanguage(paramString1, paramWebAppsContext, paramString3);
/*  505 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  506 */         Utils.writeToLog(str1, "After checking langCode: " + paramString3, paramWebAppsContext); 
/*      */     } 
/*  508 */     boolean bool1 = false;
/*  509 */     boolean bool2 = false;
/*  510 */     Object object1 = null;
/*  511 */     Object object2 = null;
/*  512 */     OracleCallableStatement oracleCallableStatement = null;
/*  513 */     OracleConnection oracleConnection = (OracleConnection)Utils.getConnection(paramWebAppsContext);
/*  514 */     boolean bool3 = false;
/*  519 */     String str4 = getAppsCookie(paramHttpServletRequest);
/*  520 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  521 */       Utils.writeToLog(str1, "Cookie Value: " + str4, paramWebAppsContext); 
/*  523 */     if (str4 != null && !str4.equals("-1")) {
/*  524 */       String str = paramWebAppsContext.checkSession(str4);
/*  525 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  526 */         Utils.writeToLog(str1, "CheckSession check: " + str, paramWebAppsContext); 
/*  528 */       if (str != null && str.equals("VALID")) {
/*  529 */         boolean bool = paramWebAppsContext.validateSession(str4);
/*  530 */         if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  531 */           Utils.writeToLog(str1, "ValidateSession check1: " + bool, paramWebAppsContext); 
/*  532 */         if (bool == true)
/*      */           try {
/*  534 */             String str5 = paramWebAppsContext.getID(99);
/*  535 */             int i = Integer.parseInt(paramWebAppsContext.getEnvStore().getEnv("ICX_SESSION_ID"));
/*  537 */             if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  538 */               Utils.writeToLog(str1, "After validation susername: " + str5 + " username: " + paramString1, paramWebAppsContext); 
/*  542 */             String str6 = paramWebAppsContext.getEnvStore().getEnv("ICX_PV_SESSION_MODE");
/*  543 */             if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  544 */               Utils.writeToLog(str1, "origSessionModeCode: " + str6, paramWebAppsContext); 
/*  553 */             BigDecimal bigDecimal1 = new BigDecimal(paramWebAppsContext.getUserIdFromName(str5));
/*  555 */             BigDecimal bigDecimal2 = new BigDecimal(paramWebAppsContext.getUserIdFromName(paramString1));
/*  558 */             String str7 = paramWebAppsContext.getSessionAttribute("FND_PROXY_USER", i);
/*  559 */             if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  560 */               Utils.writeToLog(str1, "FND_PROXY_USER in valid Session" + str7, paramWebAppsContext); 
/*  562 */             BigDecimal bigDecimal3 = null;
/*  563 */             if (str7 != null && !str7.equals("") && !str7.equals("FND_PROXY_USER"))
/*  565 */               bigDecimal3 = new BigDecimal(str7); 
/*  567 */             String str8 = Utils.getGuestUserName();
/*  569 */             boolean bool4 = (str5 != null && str5.equalsIgnoreCase(paramString1) && (str7 == null || str7.equals("") || str7.equals("FND_PROXY_USER"))) ? true : false;
/*  574 */             boolean bool5 = false;
/*  575 */             boolean bool6 = false;
/*  576 */             if (!bool4) {
/*  578 */               if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  579 */                 Utils.writeToLog(str1, "Not Same user coming in", paramWebAppsContext); 
/*  580 */               bool6 = (str5 != null && !str5.equals("")) ? true : false;
/*  582 */               boolean bool7 = (bool6 && str7 != null && !str7.equals("") && !str7.equals("FND_PROXY_USER")) ? true : false;
/*  584 */               if (bool7) {
/*  585 */                 boolean bool8 = (bigDecimal3.equals(bigDecimal2) && ProxyUserUtil.isProxyAllowed(bigDecimal1, bigDecimal2)) ? true : false;
/*  587 */                 if (!bool8) {
/*  589 */                   String str9 = Utils.getUserGuid(bigDecimal3, true);
/*  590 */                   String str10 = Utils.getUserGuid(bigDecimal2, true);
/*  591 */                   if (str9 != null && !str9.equals("") && str10 != null && !str10.equals("") && str9.equals(str10) && ProxyUserUtil.isProxyAllowed(bigDecimal1, bigDecimal3)) {
/*  595 */                     bool5 = true;
/*      */                   } else {
/*  598 */                     bool5 = false;
/*      */                   } 
/*      */                 } else {
/*  602 */                   bool5 = true;
/*      */                 } 
/*      */               } else {
/*  606 */                 bool5 = false;
/*      */               } 
/*      */             } 
/*  610 */             if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1)) {
/*  612 */               Utils.writeToLog(str1, "Valid Proxy Session:: " + bool5, paramWebAppsContext);
/*  613 */               Utils.writeToLog(str1, "sameUserLoggingIn:: " + bool4, paramWebAppsContext);
/*      */             } 
/*  615 */             if (bool4 || bool5) {
/*  617 */               if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  618 */                 Utils.writeToLog(str1, "Same session user or valid proxy", paramWebAppsContext); 
/*  619 */               bool1 = true;
/*  620 */               bool2 = true;
/*  621 */               validateSession(paramWebAppsContext);
/*  622 */               setLang(paramString1, paramWebAppsContext, paramHttpServletRequest, paramString3);
/*  623 */               bool3 = true;
/*  625 */               if (str6 != null && str6.equals("115X")) {
/*  628 */                 if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  629 */                   Utils.writeToLog(str1, "115X session begin promoted hence setting home page icx session profile", paramWebAppsContext); 
/*  632 */                 str2 = SSOUtil.getPortalUrl();
/*  633 */                 saveSessionAttribute((Connection)oracleConnection, paramWebAppsContext, "FND_HOME_PAGE_URL", str2);
/*      */               } 
/*  636 */               updateSessionModeCode(paramWebAppsContext, i, paramBoolean);
/*  637 */               saveSessionAttribute((Connection)oracleConnection, paramWebAppsContext, "SSO_HINT_SESSION", "SUCCESS");
/*  645 */               if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  646 */                 Utils.writeToLog(str1, "Login audit should be restored ", paramWebAppsContext); 
/*  650 */               reauthFndSignOn(paramWebAppsContext, i);
/*  653 */               if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  654 */                 Utils.writeToLog(str1, "Before storePostLogoutUrl, user:" + paramWebAppsContext.getUserName() + ", resp:" + paramWebAppsContext.getRespId() + ", appId:" + paramWebAppsContext.getRespApplId(), paramWebAppsContext); 
/*  658 */               paramWebAppsContext.getProfileStore().clear();
/*  659 */               SSOManager.storePostLogoutUrl(paramWebAppsContext, str4, paramBoolean, paramString3, str2);
/*      */               return;
/*      */             } 
/*  662 */             if (str8 != null && str5 != null && !str5.equalsIgnoreCase(str8)) {
/*  666 */               logoutUser(paramHttpServletRequest, paramHttpServletResponse);
/*  667 */               bool1 = false;
/*      */             } 
/*  669 */             if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  670 */               Utils.writeToLog(str1, "gusername: " + str8 + " susername: " + str5, paramWebAppsContext); 
/*  673 */             if (str8 != null && str5 != null && str5.equalsIgnoreCase(str8)) {
/*  677 */               paramWebAppsContext.getEnvStore().setEnv("ICX_PV_SESSION_MODE", getModeCodeForSetting(paramWebAppsContext, paramBoolean));
/*  680 */               boolean bool7 = paramWebAppsContext.convertGuestSession(paramString1, paramString3);
/*  689 */               bool3 = true;
/*  690 */               if (bool7) {
/*  691 */                 if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  692 */                   Utils.writeToLog(str1, "Successfully merged the Existing GUEST session to => " + paramString1, paramWebAppsContext); 
/*  703 */                 str4 = paramWebAppsContext.getSessionCookieValue();
/*  705 */                 String str9 = getSessionCookieName(paramWebAppsContext);
/*  706 */                 Cookie cookie = new Cookie(str9, str4);
/*  707 */                 boolean bool8 = false;
/*  708 */                 if (bool8) {
/*  709 */                   cookie.setPath("/; HTTPOnly");
/*      */                 } else {
/*  712 */                   cookie.setPath("/");
/*      */                 } 
/*  715 */                 String str10 = null;
/*      */                 try {
/*  719 */                   str10 = getServerDomain(paramHttpServletRequest, paramHttpServletResponse);
/*  720 */                   if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  721 */                     Utils.writeToLog(str1, "sso ssoCookieDomain: " + str10, paramWebAppsContext); 
/*  724 */                 } catch (Exception exception) {}
/*  727 */                 if (str10 != null && !"NONE".equals(str10))
/*  728 */                   cookie.setDomain(str10); 
/*  731 */                 if (isSSLMode(paramHttpServletRequest))
/*  732 */                   cookie.setSecure(true); 
/*  735 */                 paramHttpServletResponse.addCookie(cookie);
/*  738 */                 if (paramBoolean)
/*  739 */                   saveSessionUserInfo(paramWebAppsContext, paramString1, paramString2); 
/*  741 */                 bool1 = true;
/*  742 */                 paramWebAppsContext.setSessionAttribute("SSO_HINT_SESSION", "SUCCESS");
/*      */               } else {
/*  748 */                 Message message = paramWebAppsContext.getErrorStack().nextMessageObject();
/*  749 */                 String str9 = message.getName();
/*  750 */                 String str10 = message.getMessageText(paramWebAppsContext.getResourceStore());
/*  751 */                 if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  752 */                   Utils.writeToLog(str1, "Unable to merge the Existing GUEST session to => " + paramString1, paramWebAppsContext); 
/*  754 */                 if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/*  755 */                   Utils.writeToLog(str1, "Message from stack msgName :: " + str9 + " msgText :: " + str10, paramWebAppsContext, 4); 
/*  757 */                 throw new RuntimeException(str10);
/*      */               } 
/*      */             } 
/*  760 */           } catch (Exception exception) {
/*  767 */             if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/*  768 */               Utils.writeToLog(str1, "Exception Occurred. : " + Utils.getExceptionStackTrace(exception), paramWebAppsContext, 4); 
/*  772 */             logoutUser(paramHttpServletRequest, paramHttpServletResponse);
/*  773 */             bool1 = false;
/*      */           } finally {
/*  776 */             if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  777 */               Utils.writeToLog(str1, "Done Executing Valid Path : " + str + " Session Cookie Val : " + str4, paramWebAppsContext); 
/*      */           }  
/*  783 */       } else if (str != null && str.equals("EXPIRED")) {
/*  784 */         String[] arrayOfString = null;
/*  785 */         BigDecimal bigDecimal = null;
/*  786 */         String str5 = null;
/*      */         try {
/*  789 */           if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  790 */             Utils.writeToLog(str1, "Executing EXPIRED PATH.. : " + str, paramWebAppsContext); 
/*  793 */           arrayOfString = new String[1];
/*  794 */           bigDecimal = getICXSessionInfo(str4, arrayOfString);
/*  795 */           str5 = arrayOfString[0];
/*  797 */           String str6 = null;
/*  798 */           Hashtable hashtable = getICXSessionInfo(str4);
/*  799 */           if (hashtable != null)
/*  800 */             str6 = (String)hashtable.get("MODE_CODE"); 
/*  803 */           if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  804 */             Utils.writeToLog(str1, "Executing EXPIRED PATH. After calling getICXSessionInfo osid: " + bigDecimal + " username in session: " + str5, paramWebAppsContext); 
/*  812 */           BigDecimal bigDecimal1 = new BigDecimal(paramWebAppsContext.getUserIdFromName(str5));
/*  814 */           BigDecimal bigDecimal2 = new BigDecimal(paramWebAppsContext.getUserIdFromName(paramString1));
/*  817 */           String str7 = paramWebAppsContext.getSessionAttribute("FND_PROXY_USER", bigDecimal.intValue());
/*  818 */           if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  819 */             Utils.writeToLog(str1, "FND_PROXY_USER in Expired Session:: " + str7, paramWebAppsContext); 
/*  821 */           BigDecimal bigDecimal3 = null;
/*  822 */           if (str7 != null && !str7.equals("") && !str7.equals("FND_PROXY_USER"))
/*  824 */             bigDecimal3 = new BigDecimal(str7); 
/*  826 */           boolean bool4 = (str5 != null && str5.equalsIgnoreCase(paramString1) && (str7 == null || str7.equals("") || str7.equals("FND_PROXY_USER"))) ? true : false;
/*  832 */           boolean bool5 = false;
/*  833 */           boolean bool6 = false;
/*  834 */           if (!bool4) {
/*  836 */             bool6 = (str5 != null && !str5.equals("")) ? true : false;
/*  838 */             boolean bool = (bool6 && str7 != null && !str7.equals("") && !str7.equals("FND_PROXY_USER")) ? true : false;
/*  841 */             if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  842 */               Utils.writeToLog(str1, "isCurrentSessionProxy :: " + bool, paramWebAppsContext); 
/*  843 */             if (bool) {
/*  844 */               boolean bool7 = (bigDecimal3.equals(bigDecimal2) && ProxyUserUtil.isProxyAllowed(bigDecimal1, bigDecimal2)) ? true : false;
/*  846 */               if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  847 */                 Utils.writeToLog(str1, "sameProxyUser :: " + bool7, paramWebAppsContext); 
/*  848 */               if (!bool7) {
/*  850 */                 String str8 = Utils.getUserGuid(bigDecimal3, true);
/*  851 */                 if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  852 */                   Utils.writeToLog(str1, "proxyUserGuid :: " + str8, paramWebAppsContext); 
/*  853 */                 String str9 = Utils.getUserGuid(bigDecimal2, true);
/*  854 */                 if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  855 */                   Utils.writeToLog(str1, "userGuid :: " + str9, paramWebAppsContext); 
/*  856 */                 if (str8 != null && !str8.equals("") && str9 != null && !str9.equals("") && str8.equals(str9) && ProxyUserUtil.isProxyAllowed(bigDecimal1, bigDecimal3)) {
/*  860 */                   bool5 = true;
/*      */                 } else {
/*  863 */                   bool5 = false;
/*      */                 } 
/*      */               } else {
/*  867 */                 bool5 = true;
/*      */               } 
/*      */             } else {
/*  871 */               bool5 = false;
/*      */             } 
/*      */           } 
/*  875 */           if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1)) {
/*  877 */             Utils.writeToLog(str1, "Valid Proxy Session:: " + bool5, paramWebAppsContext);
/*  878 */             Utils.writeToLog(str1, "sameUserLoggingIn:: " + bool4, paramWebAppsContext);
/*      */           } 
/*  880 */           if (bool4 || bool5) {
/*  884 */             String str8 = getModeCodeForSetting(paramWebAppsContext, paramBoolean);
/*  890 */             boolean bool = false;
/*  892 */             if (paramWebAppsContext.getHijackSession())
/*  894 */               bool = true; 
/*  897 */             Utils.writeToLog(str1, "Update session", paramWebAppsContext);
/*  899 */             String str9 = "declare PRAGMA AUTONOMOUS_TRANSACTION;l_XSID varchar2(32);begin l_XSID := fnd_session_management.NewXSID;UPDATE icx_sessions SET first_connect = SYSDATE, last_connect = SYSDATE, counter = 1, xsid = l_XSID, MODE_CODE = :1 WHERE session_id = :2; :3 := l_XSID;commit;exception when others then fnd_log.string(FND_LOG.LEVEL_UNEXPECTED,'SessionMgr:update1','Exception: '||sqlerrm);rollback;end;";
/*  927 */             String str10 = "declare PRAGMA AUTONOMOUS_TRANSACTION;begin UPDATE icx_sessions SET first_connect = SYSDATE, last_connect = SYSDATE, counter = 1, MODE_CODE = :1 WHERE session_id = :2;commit;exception when others then rollback;end;";
/*  943 */             if (!str6.equals("115X") && bool) {
/*  944 */               if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  945 */                 Utils.writeToLog(str1, "SQL update: " + str9, paramWebAppsContext); 
/*  947 */               oracleCallableStatement = (OracleCallableStatement)oracleConnection.prepareCall(str9);
/*  948 */               oracleCallableStatement.setString(1, str8);
/*  949 */               oracleCallableStatement.setBigDecimal(2, bigDecimal);
/*  950 */               oracleCallableStatement.registerOutParameter(3, 12, 0, 32);
/*  951 */               oracleCallableStatement.execute();
/*  953 */               String str11 = oracleCallableStatement.getString(3);
/*  954 */               if (str11 != null) {
/*  956 */                 str4 = str11;
/*  957 */                 paramWebAppsContext.getEnvStore().setEnv("ICX_SESSION_COOKIE_VALUE", str4);
/*      */               } 
/*  959 */               JDBC.close((Statement)oracleCallableStatement);
/*  960 */               if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  961 */                 Utils.writeToLog(str1, "Executed the update", paramWebAppsContext); 
/*  963 */               String str12 = getSessionCookieName(paramWebAppsContext);
/*  964 */               Cookie cookie = new Cookie(str12, str4);
/*  965 */               boolean bool8 = false;
/*  966 */               if (bool8) {
/*  967 */                 cookie.setPath("/; HTTPOnly");
/*      */               } else {
/*  970 */                 cookie.setPath("/");
/*      */               } 
/*  973 */               String str13 = null;
/*      */               try {
/*  977 */                 str13 = getServerDomain(paramHttpServletRequest, paramHttpServletResponse);
/*  978 */                 if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  979 */                   Utils.writeToLog(str1, "sso ssoCookieDomain: " + str13, paramWebAppsContext); 
/*  982 */               } catch (Exception exception) {}
/*  985 */               if (str13 != null && !"NONE".equals(str13))
/*  986 */                 cookie.setDomain(str13); 
/*  989 */               if (isSSLMode(paramHttpServletRequest))
/*  990 */                 cookie.setSecure(true); 
/*  993 */               paramHttpServletResponse.addCookie(cookie);
/*      */             } else {
/*  996 */               if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/*  997 */                 Utils.writeToLog(str1, "SQL update: " + str10, paramWebAppsContext); 
/*  999 */               oracleCallableStatement = (OracleCallableStatement)oracleConnection.prepareCall(str10);
/* 1000 */               oracleCallableStatement.setString(1, str8);
/* 1001 */               oracleCallableStatement.setBigDecimal(2, bigDecimal);
/* 1002 */               oracleCallableStatement.execute();
/* 1006 */               if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1007 */                 Utils.writeToLog(str1, "Executed the update", paramWebAppsContext); 
/*      */             } 
/* 1015 */             boolean bool7 = paramWebAppsContext.validateSession(str4);
/* 1016 */             if (paramBoolean) {
/* 1017 */               if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1018 */                 Utils.writeToLog(str1, "SSO Mode hence setting username and guid as session attributes", paramWebAppsContext); 
/* 1020 */               paramWebAppsContext.getEnvStore().setEnv("ICX_SESSION_ID", bigDecimal.toString());
/* 1021 */               saveSessionUserInfo(paramWebAppsContext, paramString1, paramString2);
/*      */             } 
/* 1025 */             if (str6 != null && str6.equals("115X")) {
/* 1028 */               if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1029 */                 Utils.writeToLog(str1, "115X session begin promoted hence setting home page icx session profile", paramWebAppsContext); 
/* 1032 */               str2 = SSOUtil.getPortalUrl();
/* 1033 */               saveSessionAttribute((Connection)oracleConnection, paramWebAppsContext, "FND_HOME_PAGE_URL", str2);
/*      */             } 
/* 1036 */             if (bool7 == true) {
/* 1037 */               bool1 = true;
/* 1038 */               bool2 = true;
/*      */             } 
/* 1047 */             if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1048 */               Utils.writeToLog(str1, "Login audit should be restored ", paramWebAppsContext); 
/* 1051 */             reauthFndSignOn(paramWebAppsContext, bigDecimal.intValue());
/* 1053 */             bool1 = true;
/*      */           } else {
/* 1057 */             if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1058 */               Utils.writeToLog(str1, "User entered a different user name on Login at expiry username entered => " + paramString1 + " username expected => " + str5, paramWebAppsContext); 
/* 1062 */             logoutUser(paramHttpServletRequest, paramHttpServletResponse);
/* 1066 */             bool1 = false;
/*      */           } 
/* 1069 */         } catch (Exception exception) {
/* 1071 */           if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 1072 */             Utils.writeToLog(str1, "In the Expired Path..Exception Occurred : " + Utils.getExceptionStackTrace(exception), paramWebAppsContext, 4); 
/* 1074 */           logoutUser(paramHttpServletRequest, paramHttpServletResponse);
/* 1075 */           bool1 = false;
/*      */         } finally {
/* 1078 */           if (oracleCallableStatement != null)
/*      */             try {
/* 1080 */               oracleCallableStatement.close();
/* 1081 */             } catch (Exception exception) {} 
/*      */         } 
/*      */       } 
/*      */     } 
/* 1089 */     if (!bool1)
/*      */       try {
/* 1091 */         if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1092 */           Utils.writeToLog(str1, "Inside if(!done) Creating a new Session!!: " + bool1, paramWebAppsContext); 
/* 1096 */         paramWebAppsContext.getErrorStack().clear();
/* 1097 */         paramWebAppsContext.getEnvStore().setEnv("ICX_PV_SESSION_MODE", getModeCodeForSetting(paramWebAppsContext, paramBoolean));
/* 1099 */         if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1100 */           Utils.writeToLog(str1, "Checking the mode_code mode_code(env) => " + paramWebAppsContext.getEnvStore().getEnv("ICX_PV_SESSION_MODE"), paramWebAppsContext); 
/* 1103 */         if (paramString3 == null || paramString3.equals(""))
/* 1105 */           paramString3 = paramHttpServletRequest.getParameter("langCode"); 
/* 1107 */         Cookie cookie = createSession(paramString1, paramHttpServletRequest, paramHttpServletResponse, paramString3);
/* 1108 */         bool2 = true;
/* 1109 */         bool3 = true;
/* 1110 */         if (paramBoolean)
/* 1111 */           saveSessionUserInfo(paramWebAppsContext, paramString1, paramString2); 
/* 1114 */         String str = paramWebAppsContext.getProfileStore().getProfile("APPS_SSO");
/* 1115 */         if (paramBoolean == true)
/* 1117 */           if (str != null && str.equals("SSO_SDK")) {
/* 1118 */             if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1119 */               Utils.writeToLog(str1, "Deployment is set to SSO_SDK hence setting home page icx session profile", paramWebAppsContext); 
/* 1122 */             str2 = SSOUtil.getPortalUrl();
/* 1123 */             paramWebAppsContext.setSessionAttribute("FND_HOME_PAGE_URL", str2);
/*      */           }  
/* 1126 */         paramHttpServletResponse.addCookie(cookie);
/* 1128 */       } catch (Exception exception) {
/* 1129 */         if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 1130 */           Utils.writeToLog(str1, "Exception Occurred: " + Utils.getExceptionStackTrace(exception), paramWebAppsContext, 4); 
/*      */       }  
/* 1139 */     if (!bool2)
/* 1140 */       validateSession(paramWebAppsContext); 
/* 1142 */     if (!bool3)
/* 1144 */       setLang(paramString1, paramWebAppsContext, paramHttpServletRequest, paramString3); 
/* 1147 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1148 */       Utils.writeToLog(str1, "Before storePostLogoutUrl, user:" + paramWebAppsContext.getUserName() + ", resp:" + paramWebAppsContext.getRespId() + ", appId:" + paramWebAppsContext.getRespApplId(), paramWebAppsContext); 
/* 1152 */     paramWebAppsContext.getProfileStore().clear();
/* 1153 */     SSOManager.storePostLogoutUrl(paramWebAppsContext, str4, paramBoolean, paramString3, str2);
/*      */   }
/*      */   
/*      */   public static void validateSession(WebAppsContext paramWebAppsContext) {
/* 1165 */     String str = "oracle.apps.fnd.sso.SessionMgr.validateSession(WebAppsContext)";
/* 1168 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 1169 */       Utils.writeToLog(str, "BEGIN", paramWebAppsContext, 2); 
/* 1170 */     boolean bool = paramWebAppsContext.validateSession(paramWebAppsContext.getEnvStore().getEnv("ICX_SESSION_COOKIE_VALUE"));
/* 1173 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1174 */       Utils.writeToLog(str, "Session Validation : " + bool + " Session id: " + paramWebAppsContext.getEnvStore().getEnv("ICX_SESSION_COOKIE_VALUE"), paramWebAppsContext); 
/* 1177 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 1178 */       Utils.writeToLog(str, "END", paramWebAppsContext, 2); 
/*      */   }
/*      */   
/*      */   static String getUserProfile(String paramString1, String paramString2, WebAppsContext paramWebAppsContext) {
/* 1197 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.getUserProfile";
/* 1198 */     AppsLog appsLog = (AppsLog)paramWebAppsContext.getLog();
/* 1199 */     boolean bool1 = appsLog.isEnabled(str1, 2);
/* 1200 */     boolean bool2 = appsLog.isEnabled(str1, 1);
/* 1202 */     if (bool1)
/* 1202 */       appsLog.write(str1, "BEGIN", 2); 
/* 1203 */     ProfileStore profileStore = paramWebAppsContext.getProfileStore();
/* 1204 */     String str2 = profileStore.getSpecificProfile(paramString2, paramString1, null, null);
/* 1205 */     if (str2 == null || "".equals(str2)) {
/* 1207 */       str2 = profileStore.getProfile(paramString2);
/* 1208 */       if (bool2)
/* 1208 */         appsLog.write(str1, paramString2 + "=" + str2 + "[SITE]", 1); 
/* 1212 */     } else if (bool2) {
/* 1212 */       appsLog.write(str1, paramString2 + "=" + str2 + "[" + paramString1 + "]", 1);
/*      */     } 
/* 1215 */     if (bool1)
/* 1215 */       appsLog.write(str1, "END", 2); 
/* 1216 */     return str2;
/*      */   }
/*      */   
/*      */   static String checkLanguage(String paramString1, WebAppsContext paramWebAppsContext, String paramString2) {
/* 1231 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.checkLanguage(username, WebAppsContext, String)";
/* 1232 */     boolean bool1 = ((AppsLog)paramWebAppsContext.getLog()).isEnabled(2);
/* 1233 */     boolean bool2 = ((AppsLog)paramWebAppsContext.getLog()).isEnabled(1);
/* 1234 */     if (bool1)
/* 1235 */       Utils.writeToLog(str1, "BEGIN", paramWebAppsContext, 2); 
/* 1236 */     String str2 = paramString2;
/* 1242 */     String str3 = Integer.toString(Utils.getUserId(paramString1.toUpperCase(), paramWebAppsContext));
/* 1243 */     if (bool2)
/* 1243 */       Utils.writeToLog(str1, "username=" + paramString1 + " userid=" + str3, paramWebAppsContext, 1); 
/* 1245 */     String str4 = getUserProfile(str3, "FND_OVERRIDE_SSO_LANG", paramWebAppsContext);
/* 1247 */     if (!"ENABLED".equals(str4)) {
/* 1249 */       if (bool1)
/* 1250 */         Utils.writeToLog(str1, "END " + str2 + " FND_OVERRIDE_SSO_LANG is disabled or null", paramWebAppsContext, 2); 
/*      */     } else {
/* 1254 */       if (bool2)
/* 1255 */         Utils.writeToLog(str1, "External language override: " + str4, paramWebAppsContext); 
/* 1256 */       str2 = getUserProfile(str3, "ICX_LANGUAGE", paramWebAppsContext);
/* 1257 */       if (str2 == null || "".equals(str2)) {
/* 1259 */         str2 = paramString2;
/* 1260 */         if (bool2)
/* 1261 */           Utils.writeToLog(str1, "NO ICX_LANGUAGE, fallback to  => " + paramString2, paramWebAppsContext); 
/*      */       } else {
/* 1264 */         if (bool2)
/* 1265 */           Utils.writeToLog(str1, "Language Value Before :" + str2, paramWebAppsContext); 
/* 1267 */         str2 = paramWebAppsContext.getLangCode(str2.toUpperCase());
/* 1269 */         if (bool2)
/* 1270 */           Utils.writeToLog(str1, "Language Value After :" + str2, paramWebAppsContext); 
/*      */       } 
/* 1273 */       if (bool2)
/* 1274 */         Utils.writeToLog(str1, "Overriding External language with ICX_LANGUAGE => " + str2, paramWebAppsContext); 
/* 1275 */       LanguageContext languageContext = LanguageContext.getThreadLangContext();
/* 1276 */       String str = str2;
/* 1277 */       languageContext.setLegacyValue(str2);
/* 1278 */       str2 = languageContext.calculate();
/* 1279 */       if (bool2)
/* 1280 */         Utils.writeToLog(str1, " legacy= " + str + " caluted from rule=" + str2, paramWebAppsContext); 
/* 1281 */       if (bool1)
/* 1282 */         Utils.writeToLog(str1, "END returnValue: " + str2, paramWebAppsContext, 2); 
/*      */     } 
/* 1284 */     return str2;
/*      */   }
/*      */   
/*      */   public static void setLang(String paramString1, WebAppsContext paramWebAppsContext, HttpServletRequest paramHttpServletRequest, String paramString2) {
/* 1301 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.setLang(String, WebAppsContext, HttpServletRequest, String)";
/* 1303 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2)) {
/* 1305 */       Utils.writeToLog(str1, "BEGIN", paramWebAppsContext, 2);
/* 1306 */       Utils.writeToLog(str1, "Paramlist  username: " + paramString1 + " wctx: " + paramWebAppsContext + " request: " + paramHttpServletRequest + " langCode: " + paramString2, paramWebAppsContext, 2);
/*      */     } 
/* 1314 */     String str2 = null;
/* 1315 */     if (paramHttpServletRequest != null) {
/* 1316 */       str2 = paramHttpServletRequest.getParameter("langCode");
/* 1317 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1318 */         Utils.writeToLog(str1, "request langCode:" + str2, paramWebAppsContext, 1); 
/* 1319 */       if (!isInstalledLanguage(str2, paramWebAppsContext)) {
/* 1320 */         if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1321 */           Utils.writeToLog(str1, str2 + " not installed", paramWebAppsContext, 1); 
/* 1322 */         str2 = null;
/*      */       } 
/*      */     } 
/* 1326 */     if (paramString2 == null && str2 != null)
/* 1327 */       paramString2 = str2; 
/* 1330 */     if (paramWebAppsContext == null || paramString2 == null) {
/* 1331 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 1332 */         Utils.writeToLog(str1, "END Unable to determine langCode", paramWebAppsContext, 2); 
/*      */       return;
/*      */     } 
/* 1337 */     if (str2 == null) {
/* 1338 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1339 */         Utils.writeToLog(str1, "Before checking langCode: " + paramString2, paramWebAppsContext); 
/* 1340 */       LanguageContext languageContext = LanguageContext.getThreadLangContext();
/* 1341 */       languageContext.putUsername(paramString1);
/* 1342 */       languageContext.putAPIParameter(paramString2);
/* 1343 */       paramString2 = checkLanguage(paramString1, paramWebAppsContext, paramString2);
/* 1344 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1345 */         Utils.writeToLog(str1, "After checking langCode: " + paramString2, paramWebAppsContext); 
/*      */     } 
/* 1348 */     if (!isInstalledLanguage(paramString2.toUpperCase())) {
/* 1349 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 1350 */         Utils.writeToLog(str1, "END lang requested to be set is not installed", paramWebAppsContext, 2); 
/*      */       return;
/*      */     } 
/* 1354 */     OraclePreparedStatement oraclePreparedStatement = null;
/* 1355 */     ResultSet resultSet = null;
/*      */     try {
/* 1358 */       Connection connection = Utils.getConnection(paramWebAppsContext);
/* 1359 */       LangInfo langInfo = paramWebAppsContext.getLangInfo(paramString2.toUpperCase(), null, connection);
/* 1361 */       if (langInfo == null) {
/* 1362 */         if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 1363 */           Utils.writeToLog(str1, "END wctx.getLangInfo returned Null ", paramWebAppsContext, 2); 
/*      */         return;
/*      */       } 
/* 1367 */       String str3 = langInfo.getNLSLanguage();
/* 1368 */       String str4 = " SELECT DISTINCT LANGUAGE_CODE, NLS_TERRITORY, NLS_DATE_LANGUAGE  FROM (SELECT LANGUAGE_CODE, NLS_TERRITORY,  UTF8_DATE_LANGUAGE NLS_DATE_LANGUAGE FROM FND_LANGUAGES       WHERE NLS_CHARSET_NAME(NLS_CHARSET_ID('CHAR_CS')) in ('UTF8', 'AL32UTF8')            AND INSTALLED_FLAG <>'D'       UNION       SELECT LANGUAGE_CODE, NLS_TERRITORY, LOCAL_DATE_LANGUAGE NLS_DATE_LANGUAGE       FROM FND_LANGUAGES       WHERE NLS_CHARSET_NAME(NLS_CHARSET_ID('CHAR_CS')) not in ('UTF8', 'AL32UTF8') AND INSTALLED_FLAG <>'D')  WHERE upper(LANGUAGE_CODE) = upper(:1) ";
/* 1377 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1378 */         Utils.writeToLog(str1, "Executing query: " + str4, paramWebAppsContext); 
/* 1380 */       oraclePreparedStatement = (OraclePreparedStatement)connection.prepareStatement(str4);
/* 1381 */       oraclePreparedStatement.setString(1, paramString2);
/* 1383 */       resultSet = oraclePreparedStatement.executeQuery();
/* 1384 */       String str5 = null;
/* 1385 */       String str6 = null;
/* 1387 */       while (resultSet.next()) {
/* 1388 */         str5 = resultSet.getString(2);
/* 1389 */         str6 = resultSet.getString(3);
/*      */       } 
/* 1391 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1392 */         Utils.writeToLog(str1, "From query nlsTerritory: " + str5 + " nlsLangDate: " + str6, paramWebAppsContext); 
/* 1396 */       str5 = paramWebAppsContext.getProfileStore().getProfile("ICX_TERRITORY");
/* 1397 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1398 */         Utils.writeToLog(str1, "From profile nlsTerritory: " + str5, paramWebAppsContext); 
/* 1404 */       AppsEnvironmentStore appsEnvironmentStore = (AppsEnvironmentStore)paramWebAppsContext.getEnvStore();
/* 1406 */       String str7 = appsEnvironmentStore.getEnv("NLS_DATE_FORMAT");
/* 1407 */       String str8 = appsEnvironmentStore.getEnv("NLS_NUMERIC_CHARACTERS");
/* 1408 */       String str9 = appsEnvironmentStore.getEnv("NLS_SORT");
/* 1410 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1412 */         Utils.writeToLog(str1, "Before calling wctx.setNLSContext:  langCode: " + paramString2 + " pNlsLanguage: " + str3 + " NLS_DATE_FORMAT: " + str7 + " nlsLangDate: " + str6 + " NLS_NUMERIC_CHARACTERS: " + str8 + " NLS_SORT: " + str9 + " nlsTerritory: " + str5, paramWebAppsContext); 
/* 1421 */       boolean bool = paramWebAppsContext.setNLSContext(paramString2, str3, str7, str6, str8, str9, str5);
/* 1424 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1425 */         Utils.writeToLog(str1, "After calling wctx.setNLSContext check: " + bool, paramWebAppsContext); 
/* 1428 */     } catch (Exception exception) {
/* 1429 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 1430 */         Utils.writeToLog(str1, "Exception: " + Utils.getExceptionStackTrace(exception), paramWebAppsContext, 4); 
/* 1434 */       throw new RuntimeException(getMessage(paramWebAppsContext, "FND-9914"));
/*      */     } finally {
/*      */       try {
/* 1438 */         if (oraclePreparedStatement != null)
/* 1439 */           oraclePreparedStatement.close(); 
/* 1441 */       } catch (Exception exception) {
/* 1442 */         if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 1443 */           Utils.writeToLog(str1, "Exception close block: " + Utils.getExceptionStackTrace(exception), paramWebAppsContext, 4); 
/*      */       } 
/*      */       try {
/* 1448 */         if (resultSet != null)
/* 1449 */           resultSet.close(); 
/* 1451 */       } catch (Exception exception) {
/* 1452 */         if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 1453 */           Utils.writeToLog(str1, "Exception close block: " + Utils.getExceptionStackTrace(exception), paramWebAppsContext, 4); 
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   public static void saveSessionUserInfo(WebAppsContext paramWebAppsContext, String paramString1, String paramString2) {
/* 1473 */     String str = "oracle.apps.fnd.sso.SessionMgr.saveSessionUserInfo(WebAppsContext, String, String)";
/* 1476 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 1477 */       Utils.writeToLog(str, "BEGIN", paramWebAppsContext, 2); 
/* 1478 */     paramWebAppsContext.getErrorStack().clear();
/* 1479 */     paramWebAppsContext.setSessionAttribute("authUser", paramString1);
/* 1480 */     paramWebAppsContext.setSessionAttribute("authUserGuid", paramString2);
/* 1481 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 1482 */       Utils.writeToLog(str, "END", paramWebAppsContext, 2); 
/*      */   }
/*      */   
/*      */   public static String getSessionAttribute(Connection paramConnection, WebAppsContext paramWebAppsContext, String paramString) {
/* 1494 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.getSessionAttribute(Connection, WebAppsContext, String)";
/* 1497 */     PreparedStatement preparedStatement = null;
/* 1498 */     ResultSet resultSet = null;
/* 1499 */     String str2 = null;
/*      */     try {
/* 1502 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2)) {
/* 1504 */         Utils.writeToLog(str1, "BEGIN", paramWebAppsContext, 2);
/* 1505 */         Utils.writeToLog(str1, "ParamList:  conn: " + paramConnection + " wctx: " + paramWebAppsContext + " name: " + paramString, paramWebAppsContext, 2);
/*      */       } 
/* 1511 */       String str3 = "select value from icx_session_attributes where session_id=:1 and name=:2";
/* 1515 */       String str4 = paramWebAppsContext.getEnvStore().getEnv("ICX_SESSION_ID");
/* 1518 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1519 */         Utils.writeToLog(str1, "Try to get icxsession attribute name=" + paramString + " sessionID=" + str4, paramWebAppsContext); 
/* 1522 */       int i = Integer.parseInt(str4);
/* 1524 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1525 */         Utils.writeToLog("oracle.apps.fnd.sso.SessionMgr.getSessionAttribute", "After parsing the sessionID string sessionID=" + i, paramWebAppsContext, 1); 
/* 1528 */       preparedStatement = paramConnection.prepareStatement(str3);
/* 1529 */       preparedStatement.setInt(1, i);
/* 1530 */       preparedStatement.setString(2, paramString);
/* 1531 */       resultSet = preparedStatement.executeQuery();
/* 1532 */       if (resultSet.next())
/* 1533 */         str2 = resultSet.getString(1); 
/* 1536 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1537 */         Utils.writeToLog("oracle.apps.fnd.sso.SessionMgr.getSessionAttribute", "Returning sessionAttr=" + str2, paramWebAppsContext, 1); 
/* 1539 */       return str2;
/* 1540 */     } catch (SQLException sQLException) {
/* 1541 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 1542 */         Utils.writeToLog("oracle.apps.fnd.sso.SessionMgr.getSessionAttribute", "Catching the SQL exception " + Utils.getExceptionStackTrace(sQLException), paramWebAppsContext, 4); 
/* 1544 */       throw new RuntimeException(getMessage(paramWebAppsContext, "FND-9907"));
/* 1545 */     } catch (Exception exception) {
/* 1547 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 1548 */         Utils.writeToLog(str1, "Catching the Generic exception " + Utils.getExceptionStackTrace(exception), paramWebAppsContext, 4); 
/* 1550 */       throw new RuntimeException(getMessage(paramWebAppsContext, "FND-9907"));
/*      */     } finally {
/* 1553 */       if (preparedStatement != null)
/*      */         try {
/* 1555 */           preparedStatement.close();
/* 1556 */         } catch (Exception exception) {} 
/* 1558 */       if (resultSet != null)
/*      */         try {
/* 1560 */           resultSet.close();
/* 1561 */         } catch (Exception exception) {} 
/*      */     } 
/*      */   }
/*      */   
/*      */   static void saveSessionAttribute(Connection paramConnection, WebAppsContext paramWebAppsContext, String paramString1, String paramString2) {
/* 1576 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.saveSessionAttribute(Connection, WebAppsContext, String, String)";
/* 1579 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2)) {
/* 1581 */       Utils.writeToLog(str1, "BEGIN", paramWebAppsContext, 2);
/* 1582 */       Utils.writeToLog(str1, "ParamList  conn: " + paramConnection + " wctx: " + paramWebAppsContext + " name: " + paramString1 + " value: " + paramString2, paramWebAppsContext, 2);
/*      */     } 
/* 1588 */     String str2 = "declare PRAGMA AUTONOMOUS_TRANSACTION;begin delete ICX_SESSION_ATTRIBUTES where SESSION_ID = :1 and    NAME = :2; insert into ICX_SESSION_ATTRIBUTES (SESSION_ID,NAME,VALUE)values(:3,:4,:5);commit;exception when others then rollback;end;";
/* 1599 */     OracleCallableStatement oracleCallableStatement = null;
/*      */     try {
/* 1602 */       int i = Integer.parseInt(paramWebAppsContext.getEnvStore().getEnv("ICX_SESSION_ID"));
/* 1604 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1605 */         Utils.writeToLog(str1, "sesid: " + i, paramWebAppsContext); 
/* 1607 */       oracleCallableStatement = (OracleCallableStatement)paramConnection.prepareCall(str2);
/* 1608 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1609 */         Utils.writeToLog(str1, "Executing update " + str2, paramWebAppsContext); 
/* 1611 */       oracleCallableStatement.setInt(1, i);
/* 1612 */       oracleCallableStatement.setString(2, paramString1);
/* 1613 */       oracleCallableStatement.setInt(3, i);
/* 1614 */       oracleCallableStatement.setString(4, paramString1);
/* 1615 */       oracleCallableStatement.setString(5, paramString2);
/* 1617 */       oracleCallableStatement.execute();
/* 1618 */     } catch (Exception exception) {
/* 1619 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 1620 */         Utils.writeToLog(str1, "Exception Occurred " + Utils.getExceptionStackTrace(exception), paramWebAppsContext, 4); 
/* 1622 */       throw new RuntimeException(getMessage(paramWebAppsContext, "FND-9906"));
/*      */     } finally {
/* 1625 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 1626 */         Utils.writeToLog(str1, "END", paramWebAppsContext, 2); 
/*      */       try {
/* 1628 */         if (oracleCallableStatement != null)
/* 1629 */           oracleCallableStatement.close(); 
/* 1631 */       } catch (Exception exception) {}
/*      */     } 
/*      */   }
/*      */   
/*      */   public static BigDecimal fndSignOnNewICXSession(BigDecimal paramBigDecimal) {
/* 1642 */     String str = "oracle.apps.fnd.sso.SessionMgr.fndSignOnNewICXSession(BigDecimal)";
/* 1645 */     BigDecimal bigDecimal = null;
/* 1646 */     WebAppsContext webAppsContext = null;
/* 1647 */     boolean bool = false;
/* 1649 */     if (Utils.isAppsContextAvailable()) {
/* 1650 */       webAppsContext = Utils.getAppsContext();
/* 1651 */       bool = true;
/*      */     } else {
/* 1653 */       webAppsContext = Utils.getAppsContext();
/*      */     } 
/*      */     try {
/* 1657 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2)) {
/* 1659 */         Utils.writeToLog(str, "BEGIN", webAppsContext, 2);
/* 1660 */         Utils.writeToLog(str, "ParamList userId: " + paramBigDecimal, webAppsContext, 2);
/*      */       } 
/* 1662 */       if (paramBigDecimal != null) {
/* 1663 */         bigDecimal = fndSignOnNewICXSession(paramBigDecimal, webAppsContext);
/* 1666 */       } else if (((AppsLog)webAppsContext.getLog()).isEnabled(1)) {
/* 1667 */         Utils.writeToLog(str, "Input user id is null returning null", webAppsContext);
/*      */       } 
/*      */     } finally {
/* 1672 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 1673 */         Utils.writeToLog(str, "END :: " + bigDecimal, webAppsContext, 2); 
/* 1674 */       if (!bool)
/* 1675 */         Utils.releaseAppsContext(); 
/*      */     } 
/* 1678 */     return bigDecimal;
/*      */   }
/*      */   
/*      */   public static BigDecimal fndSignOnNewICXSession(BigDecimal paramBigDecimal, WebAppsContext paramWebAppsContext) {
/* 1684 */     String str = "oracle.apps.fnd.sso.SessionMgr.fndSignOnNewICXSession(BigDecimal, WebAppsContext)";
/* 1687 */     BigDecimal bigDecimal = null;
/* 1688 */     OracleCallableStatement oracleCallableStatement = null;
/* 1689 */     OracleConnection oracleConnection = null;
/* 1691 */     if (paramWebAppsContext == null)
/* 1692 */       throw new RuntimeException("Web Apps Context is null"); 
/*      */     try {
/* 1696 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2)) {
/* 1697 */         Utils.writeToLog(str, "BEGIN", paramWebAppsContext, 2);
/* 1698 */         Utils.writeToLog(str, "ParamList userId: " + paramBigDecimal, paramWebAppsContext, 2);
/*      */       } 
/* 1700 */       if (paramBigDecimal != null) {
/* 1701 */         String str1 = " declare PRAGMA AUTONOMOUS_TRANSACTION;  l_uid number := :1;  l_login_id number; begin  fnd_signon.new_icx_session(UID => l_uid, login_id => l_login_id);  :2 :=  l_login_id;  commit; exception   when others then    rollback; end;";
/* 1714 */         oracleConnection = (OracleConnection)Utils.getConnection(paramWebAppsContext);
/* 1715 */         oracleCallableStatement = (OracleCallableStatement)oracleConnection.prepareCall(str1);
/* 1716 */         oracleCallableStatement.registerOutParameter(2, 2);
/* 1717 */         oracleCallableStatement.setBigDecimal(1, paramBigDecimal);
/* 1719 */         if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1720 */           Utils.writeToLog(str, "Executing pl/sql: " + str1, paramWebAppsContext); 
/* 1721 */         oracleCallableStatement.execute();
/* 1722 */         bigDecimal = oracleCallableStatement.getBigDecimal(2);
/* 1725 */       } else if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1)) {
/* 1726 */         Utils.writeToLog(str, "Input user id is null returning null", paramWebAppsContext);
/*      */       } 
/* 1729 */     } catch (Exception exception) {
/* 1731 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 1732 */         Utils.writeToLog(str, "Exception Occurred :: " + Utils.getExceptionStackTrace(exception), paramWebAppsContext, 4); 
/*      */     } finally {
/* 1735 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 1736 */         Utils.writeToLog(str, "END :: " + bigDecimal, paramWebAppsContext, 2); 
/* 1737 */       if (oracleCallableStatement != null)
/*      */         try {
/* 1739 */           oracleCallableStatement.close();
/* 1740 */         } catch (Exception exception) {} 
/*      */     } 
/* 1743 */     return bigDecimal;
/*      */   }
/*      */   
/*      */   public static void updateSession(int paramInt1, int paramInt2) {
/* 1754 */     updateSession(paramInt1, paramInt2, null);
/*      */   }
/*      */   
/*      */   public static void updateSession(int paramInt1, int paramInt2, String paramString) {
/* 1771 */     String str = "oracle.apps.fnd.sso.SessionMgr.updateSession(int, int, String)";
/* 1773 */     WebAppsContext webAppsContext = null;
/* 1774 */     boolean bool = false;
/* 1775 */     if (Utils.isAppsContextAvailable()) {
/* 1776 */       webAppsContext = Utils.getAppsContext();
/* 1777 */       bool = true;
/*      */     } else {
/* 1779 */       webAppsContext = Utils.getAppsContext();
/*      */     } 
/*      */     try {
/* 1783 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2)) {
/* 1785 */         Utils.writeToLog(str, "BEGIN", webAppsContext, 2);
/* 1786 */         Utils.writeToLog(str, "ParamList  isessionid: " + paramInt1 + " userid: " + paramInt2 + " sesCookieValue: " + paramString, webAppsContext, 2);
/*      */       } 
/* 1791 */       updateSession(paramInt1, paramInt2, paramString, webAppsContext);
/*      */     } finally {
/* 1795 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 1796 */         Utils.writeToLog(str, "END", webAppsContext, 2); 
/* 1797 */       if (!bool)
/* 1798 */         Utils.releaseAppsContext(); 
/*      */     } 
/*      */   }
/*      */   
/*      */   public static void updateSession(int paramInt1, int paramInt2, String paramString, WebAppsContext paramWebAppsContext) {
/* 1805 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.updateSession(int, int, String, WebAppsContext)";
/* 1807 */     String str2 = null;
/* 1808 */     String str3 = null;
/* 1809 */     String str4 = null;
/* 1810 */     String str5 = null;
/* 1811 */     String str6 = null;
/* 1812 */     String str7 = null;
/* 1813 */     String str8 = null;
/* 1814 */     BigDecimal bigDecimal1 = null;
/* 1815 */     BigDecimal bigDecimal2 = null;
/* 1816 */     BigDecimal bigDecimal3 = null;
/* 1817 */     String str9 = null;
/* 1818 */     OracleCallableStatement oracleCallableStatement1 = null;
/* 1819 */     OracleCallableStatement oracleCallableStatement2 = null;
/* 1820 */     OracleConnection oracleConnection = null;
/* 1821 */     Object object = new Object();
/* 1823 */     if (paramWebAppsContext == null)
/* 1824 */       throw new RuntimeException("Web Apps Context is null"); 
/*      */     try {
/* 1828 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2)) {
/* 1830 */         Utils.writeToLog(str1, "BEGIN", paramWebAppsContext, 2);
/* 1831 */         Utils.writeToLog(str1, "ParamList  isessionid: " + paramInt1 + " userid: " + paramInt2 + " sesCookieValue: " + paramString, paramWebAppsContext, 2);
/*      */       } 
/* 1836 */       if (paramString != null)
/* 1837 */         paramWebAppsContext.validateSession(paramString); 
/* 1838 */       String str10 = new String("begin icx_sec.setUserNLS(:1,:2,:3,:4,:5,:6,:7,:8,:9,:10,:11,:12); end;");
/* 1842 */       oracleConnection = (OracleConnection)Utils.getConnection(paramWebAppsContext);
/* 1843 */       oracleCallableStatement1 = (OracleCallableStatement)oracleConnection.prepareCall(str10);
/* 1844 */       oracleCallableStatement1.registerOutParameter(2, 12, 0, 256);
/* 1845 */       oracleCallableStatement1.registerOutParameter(3, 12, 0, 256);
/* 1846 */       oracleCallableStatement1.registerOutParameter(4, 12, 0, 256);
/* 1847 */       oracleCallableStatement1.registerOutParameter(5, 12, 0, 256);
/* 1848 */       oracleCallableStatement1.registerOutParameter(6, 12, 0, 256);
/* 1849 */       oracleCallableStatement1.registerOutParameter(7, 12, 0, 256);
/* 1850 */       oracleCallableStatement1.registerOutParameter(8, 12, 0, 256);
/* 1851 */       oracleCallableStatement1.registerOutParameter(9, 2);
/* 1852 */       oracleCallableStatement1.registerOutParameter(10, 2);
/* 1853 */       oracleCallableStatement1.registerOutParameter(11, 12, 0, 256);
/* 1854 */       oracleCallableStatement1.registerOutParameter(12, 2);
/* 1855 */       oracleCallableStatement1.setBigDecimal(1, new BigDecimal(paramInt2));
/* 1857 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1858 */         Utils.writeToLog(str1, "Executing pl/sql: " + str10, paramWebAppsContext); 
/* 1859 */       oracleCallableStatement1.execute();
/* 1860 */       str2 = oracleCallableStatement1.getString(2);
/* 1861 */       str3 = oracleCallableStatement1.getString(3);
/* 1862 */       str4 = oracleCallableStatement1.getString(4);
/* 1863 */       str5 = oracleCallableStatement1.getString(5);
/* 1864 */       str6 = oracleCallableStatement1.getString(6);
/* 1865 */       str7 = oracleCallableStatement1.getString(7);
/* 1866 */       str8 = oracleCallableStatement1.getString(8);
/* 1867 */       bigDecimal1 = oracleCallableStatement1.getBigDecimal(9, 5);
/* 1868 */       bigDecimal2 = oracleCallableStatement1.getBigDecimal(10, 5);
/* 1869 */       str9 = oracleCallableStatement1.getString(11);
/* 1870 */       bigDecimal3 = oracleCallableStatement1.getBigDecimal(12);
/* 1872 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 1873 */         Utils.writeToLog(str1, "Executed pl/sql:  l_language: " + str2 + " l_language_code: " + str3 + " l_date_format: " + str4 + " l_date_language: " + str5 + " l_numeric_characters: " + str6 + " l_nls_sort: " + str7 + " l_nls_territory: " + str8 + " l_limit_time: " + bigDecimal1 + " l_limit_connects: " + bigDecimal2 + " l_org_id: " + str9 + " l_timeout: " + bigDecimal3, paramWebAppsContext); 
/* 1882 */       boolean bool = Utils.getGuestUserName(paramWebAppsContext).equalsIgnoreCase(paramWebAppsContext.getID(99));
/* 1886 */       StringBuffer stringBuffer = new StringBuffer("declare PRAGMA AUTONOMOUS_TRANSACTION;");
/* 1887 */       stringBuffer.append("l_user_id                 icx_sessions.user_id%type;");
/* 1888 */       stringBuffer.append("l_session_id              icx_sessions.session_id%type;");
/* 1889 */       stringBuffer.append("l_nls_language            icx_sessions.nls_language%type;");
/* 1890 */       stringBuffer.append("l_language_code           icx_sessions.language_code%type;");
/* 1891 */       stringBuffer.append("l_date_format_mask        icx_sessions.date_format_mask%type;");
/* 1892 */       stringBuffer.append("l_nls_date_language       icx_sessions.nls_date_language%type;");
/* 1893 */       stringBuffer.append("l_nls_numeric_characters  icx_sessions.nls_numeric_characters%type;");
/* 1894 */       stringBuffer.append("l_nls_sort                icx_sessions.nls_sort%type;");
/* 1895 */       stringBuffer.append("l_nls_territory           icx_sessions.nls_territory%type;");
/* 1896 */       stringBuffer.append("l_limit_time              icx_sessions.limit_time%type;");
/* 1897 */       stringBuffer.append("l_limit_connects          icx_sessions.limit_connects%type;");
/* 1898 */       stringBuffer.append("l_org_id                  icx_sessions.org_id%type;");
/* 1899 */       stringBuffer.append("l_time_out                icx_sessions.time_out%type;");
/* 1900 */       stringBuffer.append("l_xsid                    icx_sessions.xsid%type;");
/* 1901 */       stringBuffer.append("begin ");
/* 1902 */       stringBuffer.append(" l_user_id                := :1;");
/* 1903 */       stringBuffer.append(" l_nls_language           := :2;");
/* 1904 */       stringBuffer.append(" l_language_code          := :3;");
/* 1905 */       stringBuffer.append(" l_date_format_mask       := :4;");
/* 1906 */       stringBuffer.append(" l_nls_date_language      := :5;");
/* 1907 */       stringBuffer.append(" l_nls_numeric_characters := :6;");
/* 1908 */       stringBuffer.append(" l_nls_sort               := :7;");
/* 1909 */       stringBuffer.append(" l_nls_territory          := :8;");
/* 1910 */       stringBuffer.append(" l_limit_time             := :9;");
/* 1911 */       stringBuffer.append(" l_limit_connects         := :10;");
/* 1912 */       stringBuffer.append(" l_org_id                 := :11;");
/* 1913 */       stringBuffer.append(" l_time_out               := :12;");
/* 1914 */       stringBuffer.append(" l_session_id             := :13;");
/* 1915 */       stringBuffer.append(" l_xsid                   := fnd_session_management.NewXSID;");
/* 1916 */       stringBuffer.append(" UPDATE icx_sessions SET ");
/* 1917 */       stringBuffer.append("  user_id = l_user_id, ");
/* 1918 */       stringBuffer.append("  first_connect = SYSDATE,");
/* 1919 */       stringBuffer.append("  last_connect = SYSDATE, ");
/* 1920 */       stringBuffer.append("  counter =1,");
/* 1921 */       stringBuffer.append("  nls_language = l_nls_language,");
/* 1922 */       stringBuffer.append("  language_code = l_language_code,");
/* 1923 */       stringBuffer.append("  date_format_mask = l_date_format_mask,");
/* 1924 */       stringBuffer.append("  nls_date_language = l_nls_date_language,");
/* 1925 */       stringBuffer.append("  nls_numeric_characters = l_nls_numeric_characters,");
/* 1926 */       stringBuffer.append("  nls_sort = l_nls_sort,");
/* 1927 */       stringBuffer.append("  nls_territory = l_nls_territory,");
/* 1928 */       stringBuffer.append("  limit_time = l_limit_time, ");
/* 1929 */       stringBuffer.append("  limit_connects = l_limit_connects,");
/* 1930 */       stringBuffer.append("  org_id = l_org_id,");
/* 1931 */       stringBuffer.append("  time_out = l_time_out ");
/* 1932 */       if (bool)
/* 1932 */         stringBuffer.append(", GUEST = 'N'"); 
/* 1934 */       if (paramWebAppsContext.getHijackSession())
/* 1938 */         if (bool)
/* 1938 */           stringBuffer.append(", xsid = l_xsid");  
/* 1940 */       stringBuffer.append(" WHERE ");
/* 1941 */       stringBuffer.append("  session_id = l_session_id;");
/* 1942 */       stringBuffer.append("  :14 := l_xsid;");
/* 1943 */       stringBuffer.append(" commit;");
/* 1944 */       stringBuffer.append(" exception when others then ");
/* 1945 */       stringBuffer.append("rollback;");
/* 1946 */       stringBuffer.append("end;");
/* 1948 */       oracleCallableStatement2 = (OracleCallableStatement)oracleConnection.prepareCall(stringBuffer.toString());
/* 1950 */       oracleCallableStatement2.setBigDecimal(1, new BigDecimal(paramInt2));
/* 1951 */       if (str2 != null) {
/* 1952 */         oracleCallableStatement2.setString(2, str2);
/*      */       } else {
/* 1954 */         oracleCallableStatement2.setNull(2, 12);
/*      */       } 
/* 1956 */       if (str3 != null) {
/* 1957 */         oracleCallableStatement2.setString(3, str3);
/*      */       } else {
/* 1959 */         oracleCallableStatement2.setNull(3, 12);
/*      */       } 
/* 1961 */       if (str4 != null) {
/* 1962 */         oracleCallableStatement2.setString(4, str4);
/*      */       } else {
/* 1964 */         oracleCallableStatement2.setNull(4, 12);
/*      */       } 
/* 1966 */       if (str5 != null) {
/* 1967 */         oracleCallableStatement2.setString(5, str5);
/*      */       } else {
/* 1969 */         oracleCallableStatement2.setNull(5, 12);
/*      */       } 
/* 1971 */       if (str6 != null) {
/* 1972 */         oracleCallableStatement2.setString(6, str6);
/*      */       } else {
/* 1974 */         oracleCallableStatement2.setNull(6, 12);
/*      */       } 
/* 1976 */       if (str7 != null) {
/* 1977 */         oracleCallableStatement2.setString(7, str7);
/*      */       } else {
/* 1979 */         oracleCallableStatement2.setNull(7, 12);
/*      */       } 
/* 1981 */       if (str8 != null) {
/* 1982 */         oracleCallableStatement2.setString(8, str8);
/*      */       } else {
/* 1984 */         oracleCallableStatement2.setNull(8, 12);
/*      */       } 
/* 1986 */       if (bigDecimal1 != null) {
/* 1987 */         oracleCallableStatement2.setBigDecimal(9, bigDecimal1);
/*      */       } else {
/* 1989 */         oracleCallableStatement2.setNull(9, 2);
/*      */       } 
/* 1991 */       if (bigDecimal2 != null) {
/* 1992 */         oracleCallableStatement2.setBigDecimal(10, bigDecimal2);
/*      */       } else {
/* 1994 */         oracleCallableStatement2.setNull(10, 2);
/*      */       } 
/* 1996 */       if (str9 != null) {
/* 1997 */         oracleCallableStatement2.setBigDecimal(11, new BigDecimal(Integer.parseInt(str9)));
/*      */       } else {
/* 1999 */         oracleCallableStatement2.setNull(11, 2);
/*      */       } 
/* 2001 */       if (bigDecimal3 != null) {
/* 2002 */         oracleCallableStatement2.setBigDecimal(12, bigDecimal3);
/*      */       } else {
/* 2004 */         oracleCallableStatement2.setNull(12, 2);
/*      */       } 
/* 2007 */       oracleCallableStatement2.setBigDecimal(13, new BigDecimal(paramInt1));
/* 2008 */       oracleCallableStatement2.registerOutParameter(14, 12, 0, 32);
/* 2010 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 2011 */         Utils.writeToLog(str1, "Executing 2. update: " + stringBuffer, paramWebAppsContext); 
/* 2012 */       oracleCallableStatement2.execute();
/* 2013 */       String str11 = oracleCallableStatement2.getString(14);
/* 2016 */       if (paramWebAppsContext.getHijackSession())
/* 2020 */         if (bool && str11 != null)
/* 2022 */           paramWebAppsContext.getEnvStore().setEnv("ICX_SESSION_COOKIE_VALUE", str11);  
/* 2025 */     } catch (Exception exception) {
/* 2028 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 2029 */         Utils.writeToLog(str1, "Exception Occurred : " + Utils.getExceptionStackTrace(exception), paramWebAppsContext, 4); 
/* 2031 */       throw new RuntimeException(getMessage(paramWebAppsContext, "FND-9910"));
/*      */     } finally {
/* 2034 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 2035 */         Utils.writeToLog(str1, "END", paramWebAppsContext, 2); 
/* 2036 */       if (oracleCallableStatement2 != null)
/*      */         try {
/* 2038 */           oracleCallableStatement2.close();
/* 2039 */         } catch (Exception exception) {} 
/* 2041 */       if (oracleCallableStatement1 != null)
/*      */         try {
/* 2043 */           oracleCallableStatement1.close();
/* 2044 */         } catch (Exception exception) {} 
/*      */     } 
/*      */   }
/*      */   
/*      */   static String getModeCodeForSetting(WebAppsContext paramWebAppsContext, boolean paramBoolean) {
/* 2066 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.getModeCodeForSetting.(WebAppsContext, boolean)";
/* 2069 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2)) {
/* 2071 */       Utils.writeToLog(str1, "BEGIN", paramWebAppsContext, 2);
/* 2072 */       Utils.writeToLog(str1, "ParamList wctx: " + paramWebAppsContext + " ssoMode: " + paramBoolean, paramWebAppsContext, 2);
/*      */     } 
/* 2075 */     String str2 = paramWebAppsContext.getProfileStore().getProfile("APPS_SSO");
/* 2076 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 2077 */       Utils.writeToLog(str1, "sMode(APPS_SSO): " + str2, paramWebAppsContext); 
/* 2079 */     String str3 = null;
/* 2081 */     if (paramBoolean == true) {
/* 2082 */       if (str2.equals("SSWA_SSO") || str2.equals("SSO_SDK")) {
/* 2083 */         str3 = "115J";
/* 2084 */       } else if (str2.equals("PORTAL")) {
/* 2085 */         str3 = "115X";
/*      */       } else {
/* 2087 */         str3 = "115P";
/*      */       } 
/*      */     } else {
/* 2090 */       str3 = "115P";
/*      */     } 
/* 2092 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 2093 */       Utils.writeToLog(str1, "END returning: " + str3, paramWebAppsContext, 2); 
/* 2094 */     return str3;
/*      */   }
/*      */   
/*      */   public static void updateSessionModeCode(WebAppsContext paramWebAppsContext, int paramInt, boolean paramBoolean) {
/* 2105 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.updateSessionModeCode(WebAppsContext, int, boolean)";
/* 2107 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2)) {
/* 2109 */       Utils.writeToLog(str1, "BEGIN", paramWebAppsContext, 2);
/* 2110 */       Utils.writeToLog(str1, "Paramlist  wctx: " + paramWebAppsContext + " icxsessionid: " + paramInt + " ssoMode: " + paramBoolean, paramWebAppsContext, 2);
/*      */     } 
/* 2115 */     String str2 = getModeCodeForSetting(paramWebAppsContext, paramBoolean);
/* 2116 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 2117 */       Utils.writeToLog(str1, "sesMode: " + str2, paramWebAppsContext); 
/* 2118 */     updateSessionModeCode(paramWebAppsContext, paramInt, str2);
/* 2120 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 2121 */       Utils.writeToLog(str1, "END", paramWebAppsContext, 2); 
/*      */   }
/*      */   
/*      */   public static void updateSessionModeCode(WebAppsContext paramWebAppsContext, int paramInt, String paramString) {
/* 2133 */     String str = "oracle.apps.fnd.sso.SessionMgr.updateSessionModeCode(WebAppsContext, int, String)";
/* 2135 */     OracleCallableStatement oracleCallableStatement = null;
/* 2136 */     OracleConnection oracleConnection = null;
/*      */     try {
/* 2139 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2)) {
/* 2141 */         Utils.writeToLog(str, "BEGIN", paramWebAppsContext, 2);
/* 2142 */         Utils.writeToLog(str, "ParamList  wctx: " + paramWebAppsContext + " icxsessionid: " + paramInt + " sesMode: " + paramString, paramWebAppsContext, 2);
/*      */       } 
/* 2148 */       oracleConnection = (OracleConnection)paramWebAppsContext.getJDBCConnection();
/* 2150 */       String str1 = "declare PRAGMA AUTONOMOUS_TRANSACTION;begin UPDATE icx_sessions SET mode_code = :1 WHERE session_id = :2;commit;exception when others then rollback;end;";
/* 2158 */       oracleCallableStatement = (OracleCallableStatement)oracleConnection.prepareCall(str1);
/* 2159 */       oracleCallableStatement.setString(1, paramString);
/* 2160 */       oracleCallableStatement.setBigDecimal(2, new BigDecimal(paramInt));
/* 2161 */       oracleCallableStatement.execute();
/* 2163 */     } catch (Exception exception) {
/* 2164 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 2165 */         Utils.writeToLog(str, "Exception Occurred : " + Utils.getExceptionStackTrace(exception), paramWebAppsContext, 4); 
/* 2169 */       throw new RuntimeException(Utils.getExceptionStackTrace(exception));
/*      */     } finally {
/* 2171 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 2172 */         Utils.writeToLog(str, "END", paramWebAppsContext, 2); 
/* 2173 */       if (oracleCallableStatement != null)
/*      */         try {
/* 2175 */           oracleCallableStatement.close();
/* 2176 */         } catch (Exception exception) {} 
/*      */     } 
/*      */   }
/*      */   
/*      */   static void updateSessionCounters(BigDecimal paramBigDecimal) {
/* 2196 */     String str = "oracle.apps.fnd.sso.SessionMgr.updateSessionCounters(BigDecimal)";
/* 2197 */     WebAppsContext webAppsContext = null;
/* 2198 */     boolean bool = false;
/* 2199 */     if (Utils.isAppsContextAvailable()) {
/* 2200 */       webAppsContext = Utils.getAppsContext();
/* 2201 */       bool = true;
/*      */     } else {
/* 2203 */       webAppsContext = Utils.getAppsContext();
/*      */     } 
/* 2206 */     if (((AppsLog)webAppsContext.getLog()).isEnabled(2)) {
/* 2208 */       Utils.writeToLog(str, "BEGIN", webAppsContext, 2);
/* 2209 */       Utils.writeToLog(str, "ParamList  icxsessionid: " + paramBigDecimal, webAppsContext, 2);
/*      */     } 
/* 2213 */     if (paramBigDecimal == null)
/* 2214 */       throw new RuntimeException(getMessage(webAppsContext, "FND-9912")); 
/* 2216 */     Object object = null;
/* 2217 */     OracleCallableStatement oracleCallableStatement = null;
/* 2218 */     OracleConnection oracleConnection = null;
/*      */     try {
/* 2221 */       oracleConnection = (OracleConnection)Utils.getConnection(webAppsContext);
/* 2224 */       String str1 = "declare PRAGMA AUTONOMOUS_TRANSACTION;begin UPDATE icx_sessions SET first_connect = SYSDATE, last_connect = SYSDATE, counter =1 WHERE session_id = :1;commit;exception when others then rollback;end;";
/* 2233 */       oracleCallableStatement = (OracleCallableStatement)oracleConnection.prepareCall(str1);
/* 2234 */       oracleCallableStatement.setBigDecimal(1, paramBigDecimal);
/* 2235 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 2236 */         Utils.writeToLog(str, "Executing update: " + str1, webAppsContext); 
/* 2237 */       oracleCallableStatement.execute();
/* 2240 */     } catch (Exception exception) {
/* 2244 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(4))
/* 2245 */         Utils.writeToLog(str, "Exception Occurred : " + Utils.getExceptionStackTrace(exception), webAppsContext, 4); 
/* 2248 */       throw new RuntimeException(getMessage(webAppsContext, "FND-9910"));
/*      */     } finally {
/* 2251 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 2252 */         Utils.writeToLog(str, "END", webAppsContext, 2); 
/* 2253 */       if (oracleCallableStatement != null)
/*      */         try {
/* 2255 */           oracleCallableStatement.close();
/* 2256 */         } catch (Exception exception) {} 
/* 2258 */       if (!bool)
/* 2259 */         Utils.releaseAppsContext(); 
/*      */     } 
/*      */   }
/*      */   
/*      */   public static String createGuestSession(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse, boolean paramBoolean) {
/* 2276 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.createGuestSession(HttpServletRequest, HttpServletResponse, boolean)";
/* 2278 */     WebAppsContext webAppsContext = null;
/* 2279 */     boolean bool = false;
/* 2280 */     String str2 = null;
/* 2282 */     if (Utils.isAppsContextAvailable()) {
/* 2283 */       webAppsContext = Utils.getAppsContext();
/* 2284 */       bool = true;
/*      */     } else {
/* 2286 */       webAppsContext = Utils.getAppsContext();
/*      */     } 
/*      */     try {
/* 2290 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2)) {
/* 2292 */         Utils.writeToLog(str1, "BEGIN", webAppsContext, 2);
/* 2293 */         Utils.writeToLog(str1, "Paramlist: request: " + paramHttpServletRequest + " response: " + paramHttpServletResponse + " check: " + paramBoolean, webAppsContext, 2);
/*      */       } 
/* 2297 */       if (paramBoolean == true) {
/* 2298 */         String str = getAppsCookie(paramHttpServletRequest);
/* 2299 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 2300 */           Utils.writeToLog(str1, "Cookie Value: " + str, webAppsContext); 
/* 2301 */         if (str != null) {
/* 2302 */           boolean bool1 = webAppsContext.validateSession(str);
/* 2303 */           if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 2304 */             Utils.writeToLog(str1, "validateSession test: " + bool1, webAppsContext); 
/* 2305 */           if (bool1 == true) {
/* 2306 */             str2 = webAppsContext.getEnvStore().getEnv("ICX_SESSION_ID");
/* 2307 */             if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 2308 */               Utils.writeToLog(str1, "END: returnValue: " + str2, webAppsContext, 2); 
/* 2309 */             return str2;
/*      */           } 
/*      */         } 
/*      */       } 
/* 2314 */       Cookie cookie = createGuestSession(paramHttpServletRequest, paramHttpServletResponse);
/* 2315 */       paramHttpServletResponse.addCookie(cookie);
/* 2316 */       str2 = webAppsContext.getEnvStore().getEnv("ICX_SESSION_ID");
/* 2317 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 2318 */         Utils.writeToLog(str1, "END: returnValue: " + str2, webAppsContext, 2); 
/* 2319 */       return str2;
/*      */     } finally {
/* 2322 */       if (!bool)
/* 2323 */         Utils.releaseAppsContext(); 
/*      */     } 
/*      */   }
/*      */   
/*      */   public static String createGuestSession(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse, boolean paramBoolean, String paramString) {
/* 2341 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.createGuestSession(HttpServletRequest, HttpServletResponse, boolean)";
/* 2343 */     WebAppsContext webAppsContext = null;
/* 2344 */     boolean bool = false;
/* 2345 */     String str2 = null;
/* 2347 */     if (Utils.isAppsContextAvailable()) {
/* 2348 */       webAppsContext = Utils.getAppsContext();
/* 2349 */       bool = true;
/*      */     } else {
/* 2351 */       webAppsContext = Utils.getAppsContext();
/*      */     } 
/*      */     try {
/* 2355 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2)) {
/* 2357 */         Utils.writeToLog(str1, "BEGIN", webAppsContext, 2);
/* 2358 */         Utils.writeToLog(str1, "Paramlist: request: " + paramHttpServletRequest + " response: " + paramHttpServletResponse + " check: " + paramBoolean, webAppsContext, 2);
/*      */       } 
/* 2362 */       if (paramBoolean == true) {
/* 2363 */         String str = getAppsCookie(paramHttpServletRequest);
/* 2364 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 2365 */           Utils.writeToLog(str1, "Cookie Value: " + str, webAppsContext); 
/* 2366 */         if (str != null) {
/* 2367 */           boolean bool1 = webAppsContext.validateSession(str);
/* 2368 */           if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 2369 */             Utils.writeToLog(str1, "validateSession test: " + bool1, webAppsContext); 
/* 2370 */           if (bool1 == true) {
/* 2371 */             str2 = webAppsContext.getEnvStore().getEnv("ICX_SESSION_ID");
/* 2372 */             if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 2373 */               Utils.writeToLog(str1, "END: returnValue: " + str2, webAppsContext, 2); 
/* 2374 */             return str2;
/*      */           } 
/*      */         } 
/*      */       } 
/* 2379 */       Cookie cookie = createGuestSession(paramHttpServletRequest, paramHttpServletResponse, paramString);
/* 2380 */       paramHttpServletResponse.addCookie(cookie);
/* 2381 */       str2 = webAppsContext.getEnvStore().getEnv("ICX_SESSION_ID");
/* 2382 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 2383 */         Utils.writeToLog(str1, "END: returnValue: " + str2, webAppsContext, 2); 
/* 2384 */       return str2;
/*      */     } finally {
/* 2387 */       if (!bool)
/* 2388 */         Utils.releaseAppsContext(); 
/*      */     } 
/*      */   }
/*      */   
/*      */   static Cookie createGuestSession(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse) {
/* 2404 */     String str = Utils.getGuestUserName();
/* 2406 */     return createSession(str, paramHttpServletRequest, paramHttpServletResponse);
/*      */   }
/*      */   
/*      */   static Cookie createGuestSession(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse, String paramString) {
/* 2420 */     String str = Utils.getGuestUserName();
/* 2422 */     return createSession(str, paramHttpServletRequest, paramHttpServletResponse, paramString);
/*      */   }
/*      */   
/*      */   static Cookie createSession(String paramString1, HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse, String paramString2) {
/* 2439 */     String str = "oracle.apps.fnd.sso.SessionMgr.createSession(String, HttpServletRequest, HttpServletResponse, String)";
/* 2441 */     WebAppsContext webAppsContext = null;
/* 2442 */     boolean bool = false;
/* 2443 */     Connection connection = null;
/* 2444 */     Cookie cookie = null;
/* 2446 */     if (Utils.isAppsContextAvailable()) {
/* 2447 */       webAppsContext = Utils.getAppsContext();
/* 2448 */       bool = true;
/*      */     } else {
/* 2450 */       webAppsContext = Utils.getAppsContext();
/*      */     } 
/*      */     try {
/* 2453 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2)) {
/* 2455 */         Utils.writeToLog(str, "BEGIN", webAppsContext, 2);
/* 2456 */         Utils.writeToLog(str, "Paramlist:  username: " + paramString1 + " request: " + paramHttpServletRequest + " response: " + paramHttpServletResponse + " langCode: " + paramString2, webAppsContext, 2);
/*      */       } 
/* 2463 */       webAppsContext.getErrorStack().clear();
/* 2464 */       connection = Utils.getConnection(webAppsContext);
/* 2475 */       LanguageContext languageContext = LanguageContext.getThreadLangContext();
/* 2476 */       if (paramString2 != null)
/* 2476 */         languageContext.putAPIParameter(paramString2); 
/* 2477 */       String str1 = languageContext.calculate();
/* 2478 */       boolean bool1 = webAppsContext.createSession(paramString1, str1);
/* 2479 */       if (bool1)
/* 2479 */         languageContext.recordLastSessionLanguage(paramString1, str1); 
/* 2486 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 2487 */         Utils.writeToLog(str, "wctx.createSession createFlag: " + bool1, webAppsContext); 
/* 2490 */       boolean bool2 = false;
/* 2492 */       for (byte b = 0; b < webAppsContext.getErrorStack().getMessageCount(); b++) {
/* 2493 */         bool2 = true;
/* 2494 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(4))
/* 2495 */           Utils.writeToLog(str, "Exception Occurred : " + webAppsContext.getErrorStack().nextMessage(), webAppsContext, 4); 
/*      */       } 
/* 2499 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 2500 */         Utils.writeToLog(str, "icx session id : " + webAppsContext.getEnvStore().getEnv("ICX_SESSION_ID"), webAppsContext); 
/* 2503 */       if (!bool1 || bool2 == true)
/* 2504 */         throw new RuntimeException(getMessage(webAppsContext, "FND-9911")); 
/* 2507 */       String str2 = getSessionCookieName(webAppsContext);
/* 2508 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 2509 */         Utils.writeToLog(str, "ssoCookieName: " + str2, webAppsContext); 
/* 2512 */       if (str2 == null)
/* 2513 */         throw new RuntimeException(getMessage(webAppsContext, "FND-9914")); 
/* 2516 */       String str3 = webAppsContext.getSessionCookieValue();
/* 2517 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 2518 */         Utils.writeToLog(str, "ssoCookieValue: " + str3, webAppsContext); 
/* 2520 */       if (str3 == null)
/* 2521 */         throw new RuntimeException(getMessage(webAppsContext, "FND-9914")); 
/* 2523 */       cookie = new Cookie(str2, str3);
/* 2524 */       String str4 = null;
/* 2527 */       str4 = getServerDomain(paramHttpServletRequest, paramHttpServletResponse);
/* 2528 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 2529 */         Utils.writeToLog(str, "ssoCookieDomain from sso: " + str4, webAppsContext); 
/* 2533 */       if (str4 != null && !"NONE".equals(str4))
/* 2534 */         cookie.setDomain(str4); 
/* 2541 */       boolean bool3 = false;
/* 2543 */       if (bool3) {
/* 2544 */         cookie.setPath("/; HTTPOnly");
/*      */       } else {
/* 2547 */         cookie.setPath("/");
/*      */       } 
/* 2551 */       if (isSSLMode(paramHttpServletRequest))
/* 2552 */         cookie.setSecure(true); 
/*      */     } finally {
/* 2556 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 2557 */         Utils.writeToLog(str, "END: return: " + cookie, webAppsContext, 2); 
/* 2558 */       if (!bool)
/* 2560 */         Utils.releaseAppsContext(); 
/*      */     } 
/* 2563 */     return cookie;
/*      */   }
/*      */   
/*      */   static Cookie createSession(String paramString, HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse) {
/* 2576 */     String str = "oracle.apps.fnd.sso.SessionMgr.createSession(String, HttpServletRequest, HttpServletResponse)";
/* 2578 */     WebAppsContext webAppsContext = null;
/* 2579 */     boolean bool = false;
/* 2580 */     Cookie cookie = null;
/* 2581 */     if (Utils.isAppsContextAvailable()) {
/* 2582 */       webAppsContext = Utils.getAppsContext();
/* 2583 */       bool = true;
/*      */     } else {
/* 2585 */       webAppsContext = Utils.getAppsContext();
/*      */     } 
/*      */     try {
/* 2588 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2)) {
/* 2590 */         Utils.writeToLog(str, "BEGIN", webAppsContext, 2);
/* 2591 */         Utils.writeToLog(str, "Paramlist:  username: " + paramString + " request: " + paramHttpServletRequest + " response: " + paramHttpServletResponse, webAppsContext, 2);
/*      */       } 
/* 2596 */       webAppsContext.getErrorStack().clear();
/* 2599 */       boolean bool1 = webAppsContext.createSession(Utils.getUserId(paramString));
/* 2600 */       boolean bool2 = false;
/* 2602 */       for (byte b = 0; b < webAppsContext.getErrorStack().getMessageCount(); b++) {
/* 2603 */         bool2 = true;
/* 2604 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(4))
/* 2605 */           Utils.writeToLog(str, "Exception Occurred : " + webAppsContext.getErrorStack().nextMessage(), webAppsContext, 4); 
/*      */       } 
/* 2609 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 2610 */         Utils.writeToLog(str, "icx session id : " + webAppsContext.getEnvStore().getEnv("ICX_SESSION_ID"), webAppsContext); 
/* 2613 */       if (!bool1 || bool2 == true)
/* 2614 */         throw new RuntimeException(getMessage(webAppsContext, "FND-9911")); 
/* 2617 */       String str1 = getSessionCookieName(webAppsContext);
/* 2618 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 2619 */         Utils.writeToLog(str, "ssoCookieName: " + str1, webAppsContext); 
/* 2621 */       if (str1 == null)
/* 2622 */         throw new RuntimeException(getMessage(webAppsContext, "FND-9914")); 
/* 2625 */       String str2 = webAppsContext.getSessionCookieValue();
/* 2626 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 2627 */         Utils.writeToLog(str, "ssoCookieValue: " + str2, webAppsContext); 
/* 2629 */       if (str2 == null)
/* 2630 */         throw new RuntimeException(getMessage(webAppsContext, "FND-9914")); 
/* 2632 */       cookie = new Cookie(str1, str2);
/* 2633 */       String str3 = null;
/* 2638 */       str3 = getServerDomain(paramHttpServletRequest, paramHttpServletResponse);
/* 2639 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 2640 */         Utils.writeToLog(str, "ssoCookieDomain from sso: " + str3, webAppsContext); 
/* 2644 */       if (str3 != null && !"NONE".equals(str3))
/* 2645 */         cookie.setDomain(str3); 
/* 2652 */       boolean bool3 = false;
/* 2654 */       if (bool3) {
/* 2655 */         cookie.setPath("/; HTTPOnly");
/*      */       } else {
/* 2658 */         cookie.setPath("/");
/*      */       } 
/* 2662 */       if (isSSLMode(paramHttpServletRequest))
/* 2663 */         cookie.setSecure(true); 
/*      */     } finally {
/* 2667 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 2668 */         Utils.writeToLog(str, "END: return: " + cookie, webAppsContext, 2); 
/* 2669 */       if (!bool)
/* 2670 */         Utils.releaseAppsContext(); 
/*      */     } 
/* 2673 */     return cookie;
/*      */   }
/*      */   
/*      */   static boolean isSSLMode(HttpServletRequest paramHttpServletRequest) {
/* 2683 */     String str = getProtocol(paramHttpServletRequest);
/* 2685 */     if (str != null && str.equalsIgnoreCase("https:"))
/* 2686 */       return true; 
/* 2689 */     return false;
/*      */   }
/*      */   
/*      */   static String getProtocol(HttpServletRequest paramHttpServletRequest) {
/* 2701 */     String str = "oracle.apps.fnd.sso.SessionMgr.getProtocol(HttpServletRequest)";
/* 2703 */     if (protocol != null)
/* 2704 */       return protocol; 
/* 2707 */     synchronized (SessionMgr.class) {
/* 2708 */       if (protocol != null)
/* 2709 */         return protocol; 
/* 2712 */       WebAppsContext webAppsContext = null;
/* 2713 */       boolean bool = false;
/* 2715 */       if (Utils.isAppsContextAvailable()) {
/* 2716 */         webAppsContext = Utils.getAppsContext();
/* 2717 */         bool = true;
/*      */       } else {
/* 2719 */         webAppsContext = Utils.getAppsContext();
/*      */       } 
/* 2722 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2)) {
/* 2724 */         Utils.writeToLog(str, "BEGIN", webAppsContext, 2);
/* 2725 */         Utils.writeToLog(str, "Paramlist:  request: " + paramHttpServletRequest, webAppsContext, 2);
/*      */       } 
/* 2729 */       String str1 = "begin :1 := FND_WEB_CONFIG.PROTOCOL; end;";
/* 2730 */       OracleCallableStatement oracleCallableStatement = null;
/* 2731 */       Connection connection = null;
/* 2732 */       ResultSet resultSet = null;
/*      */       try {
/* 2736 */         connection = Utils.getConnection(webAppsContext);
/* 2737 */         oracleCallableStatement = (OracleCallableStatement)connection.prepareCall(str1);
/* 2738 */         oracleCallableStatement.registerOutParameter(1, 12, 0, 64);
/* 2739 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 2740 */           Utils.writeToLog(str, "Executing pls/sql: " + str1, webAppsContext); 
/* 2741 */         oracleCallableStatement.execute();
/* 2743 */         protocol = oracleCallableStatement.getString(1);
/* 2744 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 2745 */           Utils.writeToLog(str, "Output of pls/sql protocol: " + protocol, webAppsContext); 
/* 2747 */       } catch (Exception exception) {
/* 2748 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(4))
/* 2749 */           Utils.writeToLog(str, "Exception Occurred: " + Utils.getExceptionStackTrace(exception), webAppsContext, 4); 
/* 2752 */         throw new RuntimeException(getMessage(webAppsContext, "FND-9914"));
/*      */       } finally {
/* 2755 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 2756 */           Utils.writeToLog(str, "END returning: " + protocol, webAppsContext, 2); 
/*      */         try {
/* 2758 */           if (resultSet != null)
/* 2759 */             resultSet.close(); 
/* 2761 */           if (oracleCallableStatement != null)
/* 2762 */             oracleCallableStatement.close(); 
/* 2764 */         } catch (Exception exception) {}
/* 2768 */         if (!bool)
/* 2769 */           Utils.releaseAppsContext(); 
/*      */       } 
/*      */     } 
/* 2775 */     return protocol;
/*      */   }
/*      */   
/*      */   public static BigDecimal getICXSessionInfo(String paramString, String[] paramArrayOfString) {
/* 2790 */     String str = "oracle.apps.fnd.sso.SessionMgr.getICXSessionInfo(String, String[])";
/* 2793 */     WebAppsContext webAppsContext = null;
/* 2794 */     boolean bool = false;
/* 2795 */     if (Utils.isAppsContextAvailable()) {
/* 2796 */       webAppsContext = Utils.getAppsContext();
/* 2797 */       bool = true;
/*      */     } else {
/* 2799 */       webAppsContext = Utils.getAppsContext();
/*      */     } 
/* 2801 */     if (((AppsLog)webAppsContext.getLog()).isEnabled(2)) {
/* 2803 */       Utils.writeToLog(str, "BEGIN", webAppsContext, 2);
/* 2804 */       Utils.writeToLog(str, "Paramlist icxsession: " + paramString + " name: " + paramArrayOfString, webAppsContext, 2);
/*      */     } 
/* 2807 */     if (paramString == null)
/* 2808 */       return null; 
/* 2810 */     if (paramArrayOfString == null || paramArrayOfString.length < 1) {
/* 2811 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(4))
/* 2812 */         Utils.writeToLog(str, "name parameter is not set correctly", webAppsContext, 4); 
/* 2814 */       throw new RuntimeException("Invalid function inputs. Parameter name should not be null and should be a 1 element array.");
/*      */     } 
/* 2818 */     BigDecimal bigDecimal = null;
/*      */     try {
/* 2822 */       bigDecimal = getICXSessionInfo(paramString, paramArrayOfString, webAppsContext);
/*      */     } finally {
/* 2825 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 2826 */         Utils.writeToLog(str, "END: return: " + bigDecimal, webAppsContext, 2); 
/* 2827 */       if (!bool)
/* 2828 */         Utils.releaseAppsContext(); 
/*      */     } 
/* 2832 */     return bigDecimal;
/*      */   }
/*      */   
/*      */   public static BigDecimal getICXSessionInfo(String paramString, String[] paramArrayOfString, WebAppsContext paramWebAppsContext) {
/* 2838 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.getICXSessionInfo(String, String[], WebAppsContext)";
/* 2841 */     if (paramWebAppsContext == null)
/* 2842 */       throw new RuntimeException("Web Apps Context is null"); 
/* 2845 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2)) {
/* 2847 */       Utils.writeToLog(str1, "BEGIN", paramWebAppsContext, 2);
/* 2848 */       Utils.writeToLog(str1, "Paramlist icxsession: " + paramString + " name: " + paramArrayOfString, paramWebAppsContext, 2);
/*      */     } 
/* 2851 */     if (paramString == null)
/* 2852 */       return null; 
/* 2854 */     if (paramArrayOfString == null || paramArrayOfString.length < 1) {
/* 2855 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 2856 */         Utils.writeToLog(str1, "name parameter is not set correctly", paramWebAppsContext, 4); 
/* 2858 */       throw new RuntimeException("Invalid function inputs. Parameter name should not be null and should be a 1 element array.");
/*      */     } 
/* 2862 */     OracleCallableStatement oracleCallableStatement = null;
/* 2863 */     OracleConnection oracleConnection = null;
/* 2864 */     BigDecimal bigDecimal = null;
/* 2865 */     String str2 = "DECLARE username varchar2(100); l_session_id number; BEGIN  l_session_id := icx_call.decrypt3(:1); username := icx_sec.getid(icx_sec.PV_USERNAME,null,  l_session_id); :2 := username; :3 := l_session_id; END; ";
/* 2871 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 2872 */       Utils.writeToLog(str1, "Entering try/catch ", paramWebAppsContext); 
/*      */     try {
/* 2874 */       oracleConnection = (OracleConnection)Utils.getConnection(paramWebAppsContext);
/* 2875 */       oracleCallableStatement = (OracleCallableStatement)oracleConnection.prepareCall(str2);
/* 2876 */       oracleCallableStatement.setString(1, paramString);
/* 2877 */       oracleCallableStatement.registerOutParameter(2, 12, 0, 256);
/* 2878 */       oracleCallableStatement.registerOutParameter(3, 2);
/* 2879 */       oracleCallableStatement.execute();
/* 2880 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 2881 */         Utils.writeToLog(str1, "Executed query: " + str2, paramWebAppsContext); 
/* 2882 */       paramArrayOfString[0] = oracleCallableStatement.getString(2);
/* 2883 */       bigDecimal = oracleCallableStatement.getBigDecimal(3, 5);
/* 2884 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 2885 */         Utils.writeToLog(str1, "name[0]: " + paramArrayOfString[0] + " osid: " + bigDecimal, paramWebAppsContext); 
/* 2886 */     } catch (Exception exception) {
/* 2889 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 2890 */         Utils.writeToLog(str1, "Exception Occured: " + Utils.getExceptionStackTrace(exception), paramWebAppsContext, 4); 
/* 2892 */       return null;
/*      */     } finally {
/* 2895 */       if (oracleCallableStatement != null)
/*      */         try {
/* 2897 */           oracleCallableStatement.close();
/* 2898 */         } catch (Exception exception) {} 
/* 2900 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 2901 */         Utils.writeToLog(str1, "END: return: " + bigDecimal, paramWebAppsContext, 2); 
/*      */     } 
/* 2904 */     return bigDecimal;
/*      */   }
/*      */   
/*      */   protected static void printICXSessionInfo(String paramString) {
/* 2915 */     Hashtable hashtable = getICXSessionInfo(paramString);
/* 2919 */     if (hashtable != null) {
/* 2924 */       Enumeration<String> enumeration = hashtable.keys();
/* 2926 */       while (enumeration.hasMoreElements()) {
/* 2927 */         String str1 = enumeration.nextElement();
/* 2928 */         String str2 = (String)hashtable.get(str1);
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   public static Hashtable getICXSessionInfo(String paramString, WebAppsContext paramWebAppsContext) {
/* 2949 */     if (paramString == null || paramString.equals("-1"))
/* 2950 */       return null; 
/* 2952 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.getICXSessionInfo(String)";
/* 2957 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 2958 */       Utils.writeToLog(str1, "BEGIN", paramWebAppsContext, 2); 
/* 2959 */     OracleCallableStatement oracleCallableStatement = null;
/* 2960 */     OracleConnection oracleConnection = null;
/* 2961 */     BigDecimal bigDecimal = null;
/* 2962 */     Hashtable<String, String> hashtable = null;
/* 2965 */     String str2 = "DECLARE username varchar2(100); l_session_id number; BEGIN  l_session_id := icx_call.decrypt3(:1); username := icx_sec.getid(icx_sec.PV_USERNAME,null, l_session_id); :2 := username; :3 := l_session_id; END; ";
/*      */     try {
/* 2972 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 2973 */         Utils.writeToLog(str1, "Trying to execute: " + str2, paramWebAppsContext); 
/* 2974 */       oracleConnection = (OracleConnection)Utils.getConnection(paramWebAppsContext);
/* 2975 */       oracleCallableStatement = (OracleCallableStatement)oracleConnection.prepareCall(str2);
/* 2976 */       oracleCallableStatement.setString(1, paramString);
/* 2977 */       oracleCallableStatement.registerOutParameter(2, 12, 0, 256);
/* 2978 */       oracleCallableStatement.registerOutParameter(3, 2);
/* 2979 */       oracleCallableStatement.execute();
/* 2980 */       String str = oracleCallableStatement.getString(2);
/* 2981 */       bigDecimal = oracleCallableStatement.getBigDecimal(3, 5);
/* 2982 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 2983 */         Utils.writeToLog(str1, "Executed query name: " + str + " osid: " + bigDecimal, paramWebAppsContext); 
/* 2985 */       if (bigDecimal == null)
/* 2986 */         return null; 
/* 2989 */       int i = bigDecimal.intValue();
/* 2991 */       hashtable = getICXSessionInfo(i, oracleConnection);
/* 2992 */       if (hashtable != null)
/* 2993 */         hashtable.put("USER_NAME", str); 
/* 2995 */     } catch (Exception exception) {
/* 3002 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 3003 */         Utils.writeToLog(str1, "Exception Occurred : " + Utils.getExceptionStackTrace(exception), paramWebAppsContext, 4); 
/* 3005 */       return null;
/*      */     } finally {
/* 3008 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 3009 */         Utils.writeToLog(str1, "END", paramWebAppsContext, 2); 
/* 3010 */       if (oracleCallableStatement != null)
/*      */         try {
/* 3012 */           oracleCallableStatement.close();
/* 3013 */         } catch (Exception exception) {} 
/*      */     } 
/* 3017 */     return hashtable;
/*      */   }
/*      */   
/*      */   public static Hashtable getICXSessionInfo(String paramString) {
/* 3021 */     boolean bool = Utils.isAppsContextAvailable();
/*      */     try {
/* 3024 */       return getICXSessionInfo(paramString, Utils.getAppsContext());
/*      */     } finally {
/* 3028 */       if (!bool)
/* 3028 */         Utils.releaseAppsContext(); 
/*      */     } 
/*      */   }
/*      */   
/*      */   private static Hashtable getICXSessionInfo(int paramInt, OracleConnection paramOracleConnection) {
/* 3041 */     if (paramOracleConnection == null)
/* 3042 */       return null; 
/* 3044 */     String str1 = Utils.release12 ? "PROXY_USER_ID" : " NULL \"PROXY_USER_ID\" ";
/* 3045 */     String str2 = "select SESSION_ID, USER_ID, MODE_CODE, RESPONSIBILITY_ID, ORG_ID, FUNCTION_ID, NLS_LANGUAGE, DATE_FORMAT_MASK, COUNTER, FIRST_CONNECT, LAST_CONNECT, LIMIT_TIME, LIMIT_CONNECTS, DISABLED_FLAG, RESPONSIBILITY_APPLICATION_ID, LANGUAGE_CODE, NLS_DATE_LANGUAGE, NLS_NUMERIC_CHARACTERS, NLS_SORT, NLS_TERRITORY, SECURITY_GROUP_ID, HOME_URL," + str1 + " from icx_sessions where session_id = :1";
/* 3052 */     OraclePreparedStatement oraclePreparedStatement = null;
/* 3053 */     ResultSet resultSet = null;
/* 3054 */     Hashtable<Object, Object> hashtable = null;
/*      */     try {
/* 3057 */       oraclePreparedStatement = (OraclePreparedStatement)paramOracleConnection.prepareStatement(str2);
/* 3058 */       oraclePreparedStatement.defineColumnType(1, 2);
/* 3059 */       oraclePreparedStatement.defineColumnType(2, 2);
/* 3060 */       oraclePreparedStatement.defineColumnType(3, 12, 30);
/* 3061 */       oraclePreparedStatement.defineColumnType(4, 2);
/* 3062 */       oraclePreparedStatement.defineColumnType(5, 2);
/* 3063 */       oraclePreparedStatement.defineColumnType(6, 2);
/* 3064 */       oraclePreparedStatement.defineColumnType(7, 12, 30);
/* 3065 */       oraclePreparedStatement.defineColumnType(8, 12, 100);
/* 3066 */       oraclePreparedStatement.defineColumnType(9, 2);
/* 3067 */       oraclePreparedStatement.defineColumnType(10, 91);
/* 3068 */       oraclePreparedStatement.defineColumnType(11, 91);
/* 3069 */       oraclePreparedStatement.defineColumnType(12, 2);
/* 3070 */       oraclePreparedStatement.defineColumnType(13, 2);
/* 3071 */       oraclePreparedStatement.defineColumnType(14, 12, 1);
/* 3072 */       oraclePreparedStatement.defineColumnType(15, 2);
/* 3073 */       oraclePreparedStatement.defineColumnType(16, 12, 30);
/* 3074 */       oraclePreparedStatement.defineColumnType(17, 12, 30);
/* 3075 */       oraclePreparedStatement.defineColumnType(18, 12, 30);
/* 3076 */       oraclePreparedStatement.defineColumnType(19, 12, 30);
/* 3077 */       oraclePreparedStatement.defineColumnType(20, 12, 30);
/* 3078 */       oraclePreparedStatement.defineColumnType(21, 2);
/* 3079 */       oraclePreparedStatement.defineColumnType(22, 12, 240);
/* 3080 */       oraclePreparedStatement.defineColumnType(23, 2);
/* 3081 */       oraclePreparedStatement.setInt(1, paramInt);
/* 3082 */       resultSet = oraclePreparedStatement.executeQuery();
/* 3083 */       while (resultSet.next()) {
/* 3084 */         hashtable = new Hashtable<Object, Object>(25);
/* 3085 */         hashtable.put("SESSION_ID", resultSet.getString(1));
/* 3086 */         hashtable.put("USER_ID", resultSet.getString(2));
/* 3087 */         hashtable.put("MODE_CODE", resultSet.getString(3));
/* 3088 */         hashtable.put("RESPONSIBILITY_ID", (resultSet.getString(4) == null) ? "NULL" : resultSet.getString(4));
/* 3090 */         hashtable.put("ORG_ID", (resultSet.getString(5) == null) ? "NULL" : resultSet.getString(5));
/* 3092 */         hashtable.put("FUNCTION_ID", (resultSet.getString(6) == null) ? "NULL" : resultSet.getString(6));
/* 3094 */         hashtable.put("NLS_LANGUAGE", (resultSet.getString(7) == null) ? "NULL" : resultSet.getString(7));
/* 3096 */         hashtable.put("DATE_FORMAT_MASK", (resultSet.getString(8) == null) ? "NULL" : resultSet.getString(8));
/* 3098 */         hashtable.put("COUNTER", (resultSet.getString(9) == null) ? "NULL" : resultSet.getString(9));
/* 3100 */         hashtable.put("FIRST_CONNECT", (resultSet.getString(10) == null) ? "NULL" : resultSet.getString(10));
/* 3102 */         hashtable.put("LAST_CONNECT", (resultSet.getString(11) == null) ? "NULL" : resultSet.getString(11));
/* 3104 */         hashtable.put("LIMIT_TIME", (resultSet.getString(12) == null) ? "NULL" : resultSet.getString(12));
/* 3106 */         hashtable.put("LIMIT_CONNECTS", (resultSet.getString(13) == null) ? "NULL" : resultSet.getString(13));
/* 3108 */         hashtable.put("DISABLED_FLAG", (resultSet.getString(14) == null) ? "NULL" : resultSet.getString(14));
/* 3110 */         hashtable.put("RESPONSIBILITY_APPLICATION_ID", (resultSet.getString(15) == null) ? "NULL" : resultSet.getString(15));
/* 3112 */         hashtable.put("LANGUAGE_CODE", (resultSet.getString(16) == null) ? "NULL" : resultSet.getString(16));
/* 3114 */         hashtable.put("NLS_DATE_LANGUAGE", (resultSet.getString(17) == null) ? "NULL" : resultSet.getString(17));
/* 3116 */         hashtable.put("NLS_NUMERIC_CHARACTERS", (resultSet.getString(18) == null) ? "NULL" : resultSet.getString(18));
/* 3118 */         hashtable.put("NLS_SORT", (resultSet.getString(19) == null) ? "NULL" : resultSet.getString(19));
/* 3120 */         hashtable.put("NLS_TERRITORY", (resultSet.getString(20) == null) ? "NULL" : resultSet.getString(20));
/* 3122 */         hashtable.put("SECURITY_GROUP_ID", (resultSet.getString(21) == null) ? "NULL" : resultSet.getString(21));
/* 3124 */         hashtable.put("HOME_URL", (resultSet.getString(22) == null) ? "NULL" : resultSet.getString(22));
/* 3126 */         if (Utils.release12)
/* 3127 */           hashtable.put("PROXY_USER_ID", (resultSet.getString(23) == null) ? "NULL" : resultSet.getString(23)); 
/*      */       } 
/* 3131 */     } catch (Exception exception) {
/* 3136 */       return null;
/*      */     } finally {
/*      */       try {
/* 3140 */         if (resultSet != null)
/* 3141 */           resultSet.close(); 
/* 3143 */       } catch (Exception exception) {}
/*      */       try {
/* 3145 */         if (oraclePreparedStatement != null)
/* 3146 */           oraclePreparedStatement.close(); 
/* 3148 */       } catch (Exception exception) {}
/*      */     } 
/* 3151 */     return hashtable;
/*      */   }
/*      */   
/*      */   public static void logoutUser(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse) {
/* 3165 */     String str = "oracle.apps.fnd.sso.SessionMgr.logoutUser(HttpServletRequest, HttpServletResponse)";
/* 3167 */     WebAppsContext webAppsContext = null;
/* 3168 */     boolean bool = false;
/* 3170 */     if (Utils.isAppsContextAvailable()) {
/* 3171 */       webAppsContext = Utils.getAppsContext();
/* 3172 */       bool = true;
/*      */     } else {
/* 3174 */       webAppsContext = Utils.getAppsContext();
/*      */     } 
/*      */     try {
/* 3178 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 3179 */         Utils.writeToLog(str, "BEGIN", webAppsContext, 2); 
/* 3181 */       String str1 = getAppsCookie(paramHttpServletRequest);
/* 3182 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 3183 */         Utils.writeToLog(str, "Cookie value: " + str1, webAppsContext); 
/* 3185 */       if (str1 != null && !str1.equals("") && !str1.equals("-1")) {
/* 3186 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 3187 */           Utils.writeToLog(str, "Session not INVALID", webAppsContext); 
/* 3188 */         boolean bool1 = webAppsContext.disableUserSession(str1);
/* 3194 */         HttpSession httpSession = paramHttpServletRequest.getSession(false);
/* 3195 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 3196 */           Utils.writeToLog(str, "Invalidating the HttpSession", webAppsContext); 
/* 3197 */         if (httpSession != null) {
/* 3198 */           httpSession.invalidate();
/* 3199 */           httpSession.setMaxInactiveInterval(1);
/*      */         } 
/* 3202 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 3203 */           Utils.writeToLog(str, "disableUserSession chk: " + bool1, webAppsContext); 
/* 3206 */         for (byte b = 0; b < webAppsContext.getErrorStack().getMessageCount(); b++);
/* 3209 */         String str2 = getSessionCookieName(webAppsContext);
/* 3210 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 3211 */           Utils.writeToLog(str, "ssoCookieName: " + str2, webAppsContext); 
/* 3213 */         if (str2 == null)
/* 3214 */           throw new RuntimeException(getMessage(webAppsContext, "FND-9914")); 
/* 3217 */         Cookie cookie = new Cookie(str2, "-1");
/* 3224 */         boolean bool2 = false;
/* 3226 */         if (bool2) {
/* 3227 */           cookie.setPath("/; HTTPOnly");
/*      */         } else {
/* 3230 */           cookie.setPath("/");
/*      */         } 
/* 3236 */         String str3 = null;
/*      */         try {
/* 3240 */           str3 = getServerDomain(paramHttpServletRequest, paramHttpServletResponse);
/* 3241 */           if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 3242 */             Utils.writeToLog(str, "sso ssoCookieDomain: " + str3, webAppsContext); 
/* 3245 */         } catch (Exception exception) {}
/* 3248 */         if (str3 != null && !"NONE".equals(str3))
/* 3249 */           cookie.setDomain(str3); 
/* 3252 */         if (isSSLMode(paramHttpServletRequest))
/* 3253 */           cookie.setSecure(true); 
/* 3256 */         paramHttpServletResponse.addCookie(cookie);
/*      */       } 
/*      */     } finally {
/* 3260 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 3261 */         Utils.writeToLog(str, "END", webAppsContext, 2); 
/* 3262 */       if (!bool)
/* 3263 */         Utils.releaseAppsContext(); 
/*      */     } 
/*      */   }
/*      */   
/*      */   static WebAppsContext getValidAppsContext(HttpServletRequest paramHttpServletRequest) {
/* 3277 */     String str = getAppsCookie(paramHttpServletRequest);
/* 3279 */     if (str == null || str.equals("-1") || str.equals(""))
/* 3280 */       return null; 
/* 3283 */     WebAppsContext webAppsContext = null;
/* 3284 */     boolean bool = false;
/* 3285 */     if (Utils.isAppsContextAvailable()) {
/* 3286 */       webAppsContext = Utils.getAppsContext();
/* 3287 */       bool = true;
/*      */     } else {
/* 3289 */       webAppsContext = Utils.getAppsContext();
/*      */     } 
/*      */     try {
/* 3293 */       boolean bool1 = webAppsContext.validateSession(str);
/* 3294 */       if (!bool1) {
/* 3295 */         for (byte b = 0; b < webAppsContext.getErrorStack().getMessageCount(); b++) {
/* 3296 */           if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 3297 */             Utils.writeToLog("oracle.apps.fnd.sso.SessionMgr.getValidAppsContext", "Invalid ICX Session. : " + webAppsContext.getErrorStack().nextMessage(), webAppsContext, 1); 
/*      */         } 
/* 3302 */         return null;
/*      */       } 
/* 3304 */       return webAppsContext;
/*      */     } finally {}
/*      */   }
/*      */   
/*      */   public static boolean xssViolation(HttpServletRequest paramHttpServletRequest, WebAppsContext paramWebAppsContext) {
/* 3320 */     boolean bool = false;
/* 3321 */     String str = "oracle.apps.fnd.sso.SessionMgr.xssViolation(HttpServletRequest, WebAppsContext)";
/* 3323 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 3324 */       Utils.writeToLog(str, "BEGIN", paramWebAppsContext, 2); 
/*      */     try {
/* 3327 */       Utils.setRequestCharacterEncoding(paramHttpServletRequest);
/* 3328 */       Enumeration<String> enumeration = paramHttpServletRequest.getParameterNames();
/* 3329 */       HTMLProcessor hTMLProcessor = new HTMLProcessor(1);
/* 3330 */       while (enumeration.hasMoreElements()) {
/* 3332 */         int i = -1;
/* 3333 */         String str1 = enumeration.nextElement();
/* 3334 */         String str2 = paramHttpServletRequest.getParameter(str1);
/* 3335 */         if (str2 != null && !str2.equals("")) {
/* 3336 */           if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 3337 */             Utils.writeToLog(str, "XSS Check parameterName: " + str1, paramWebAppsContext); 
/* 3338 */           i = HTMLProcessor.processInput(str2);
/* 3339 */           if (i > -1) {
/* 3340 */             if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 3341 */               Utils.writeToLog(str, "XSS Check failed for parameterName: " + str1, paramWebAppsContext); 
/* 3343 */             bool = true;
/*      */             break;
/*      */           } 
/*      */         } 
/*      */       } 
/* 3348 */     } catch (Exception exception) {
/* 3350 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 3351 */         Utils.writeToLog(str, "XSS Check failed due to exceptione: " + Utils.getExceptionStackTrace(exception), paramWebAppsContext, 4); 
/* 3353 */       bool = true;
/*      */     } 
/* 3355 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 3356 */       Utils.writeToLog(str, "END:returning: " + bool, paramWebAppsContext, 2); 
/* 3357 */     return bool;
/*      */   }
/*      */   
/*      */   public static String createAppsSession(UserPwd paramUserPwd, HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse) {
/* 3374 */     boolean bool1 = Utils.isAppsContextAvailable();
/* 3375 */     WebAppsContext webAppsContext = Utils.getAppsContext();
/* 3376 */     Connection connection = null;
/* 3378 */     String str1 = "oracle.apps.fnd.sso.SessionMgr[$Revision: 115.112.2.10].createAppsSession(UserPwd, HttpServletRequest, HttpServletResponse)";
/* 3379 */     String str2 = "";
/* 3380 */     AppsLog appsLog = Utils.getLog(webAppsContext);
/* 3381 */     boolean bool2 = appsLog.isEnabled(str1, 2);
/* 3382 */     boolean bool3 = appsLog.isEnabled(str1, 1);
/* 3383 */     boolean bool4 = appsLog.isEnabled(str1, 6);
/*      */     try {
/* 3387 */       webAppsContext.getErrorStack().clear();
/* 3388 */       if (bool2) {
/* 3390 */         appsLog.write(str1, "BEGIN", 2);
/* 3391 */         appsLog.write(str1, "Paramlist:  request: " + paramHttpServletRequest + " response: " + paramHttpServletResponse, 2);
/*      */       } 
/* 3396 */       Utils.setRequestCharacterEncoding(paramHttpServletRequest);
/* 3397 */       if (bool3)
/* 3397 */         appsLog.write(str1, "After setting Character encoding", 1); 
/* 3398 */       String str3 = paramHttpServletRequest.getParameter("requestUrl");
/* 3399 */       if (bool3)
/* 3399 */         appsLog.write(str1, "Got requestUrl: " + str3, 1); 
/* 3400 */       if (str3 == null || str3.equals("")) {
/* 3401 */         str3 = "APPSHOMEPAGE";
/* 3402 */         if (bool3)
/* 3402 */           appsLog.write(str1, "Defaulting to requestUrl: " + str3, 1); 
/*      */       } 
/* 3405 */       String str4 = paramHttpServletRequest.getParameter("cancelUrl");
/* 3406 */       if (bool3)
/* 3406 */         appsLog.write(str1, "Got cancelUrl: " + str4, 1); 
/* 3408 */       if (str4 == null || str4.equals("")) {
/* 3409 */         if (bool3)
/* 3409 */           appsLog.write(str1, "cancelUrl not set", 1); 
/* 3410 */         str4 = SSOUtil.getLocalLoginUrl();
/* 3411 */         if (bool3)
/* 3411 */           appsLog.write(str1, "after substring " + str4, 1); 
/*      */       } 
/* 3413 */       String str5 = paramHttpServletRequest.getParameter("home_url");
/* 3414 */       if (bool3)
/* 3414 */         appsLog.write(str1, "Got home_url: " + str5, 1); 
/* 3415 */       String str6 = paramHttpServletRequest.getParameter("langCode");
/* 3416 */       if (bool3) {
/* 3418 */         appsLog.write(str1, "Got langCode: " + str6, 1);
/* 3419 */         appsLog.write(str1, "Got Url params requestUrl: " + str3 + " cancelUrl: " + str4 + " home_url: " + str5 + " langCode: " + str6, 1);
/*      */       } 
/* 3426 */       if (xssViolation(paramHttpServletRequest, webAppsContext)) {
/* 3427 */         if (bool3)
/* 3427 */           appsLog.write(str1, "Url Parameter validation Failed!", 1); 
/* 3429 */         String str = "requestUrl=" + URLEncoder.encode(str3, getCharSet()) + "&cancelUrl=" + URLEncoder.encode(str4, getCharSet()) + "&errCode=FND_SSO_PARAMVAL_SCAN_FAILED";
/* 3433 */         if (str6 != null && !str6.equals("") && str6.equals(URLEncoder.encode(str6, getCharSet())))
/* 3434 */           str = str + "&langCode=" + str6; 
/* 3436 */         if (str5 != null && !str5.equals(""))
/* 3437 */           str = str + "&home_url=" + URLEncoder.encode(str5, getCharSet()); 
/* 3440 */         str2 = SSOUtil.getLocalLoginUrl(str);
/* 3441 */         if (bool3)
/* 3441 */           appsLog.write(str1, "(XSS) returnUrl=" + str2, 1); 
/*      */       } else {
/*      */         SessionManager.AuthStatusCode authStatusCode;
/* 3443 */         if (bool3)
/* 3443 */           appsLog.write(str1, "XSS check passed before calling validateLogin", 1); 
/* 3445 */         String str = paramUserPwd.getUsername();
/* 3446 */         SessionManager sessionManager = webAppsContext.getSessionManager();
/* 3451 */         if (allowLocalLogin(str, webAppsContext)) {
/* 3453 */           authStatusCode = sessionManager.validateLogin(paramUserPwd);
/*      */         } else {
/* 3457 */           if (bool3)
/* 3457 */             appsLog.write(str1, "Local login not allowed", 1); 
/* 3458 */           authStatusCode = SessionManager.AuthStatusCode.INVALID;
/*      */         } 
/* 3461 */         if (bool3)
/* 3461 */           appsLog.write(str1, "After Calling SessionManager.validateLogin validationStatus: " + authStatusCode, 1); 
/* 3464 */         if (authStatusCode == null || authStatusCode.equals(SessionManager.AuthStatusCode.INVALID)) {
/* 3467 */           if (bool3)
/* 3467 */             appsLog.write(str1, "validateLogin Status:NULL or INVALID", 1); 
/* 3468 */           Message message = webAppsContext.getErrorStack().nextMessageObject();
/* 3469 */           String str7 = null;
/* 3470 */           String str8 = "";
/* 3471 */           String str9 = "&errCode=";
/* 3473 */           if (message != null) {
/* 3474 */             str7 = message.getName();
/* 3475 */             str8 = message.getMessageText(webAppsContext.getResourceStore());
/* 3476 */             str9 = str9 + URLEncoder.encode(str7, getCharSet());
/*      */           } 
/* 3479 */           String str10 = "requestUrl=" + URLEncoder.encode(str3, getCharSet()) + "&cancelUrl=" + URLEncoder.encode(str4, getCharSet()) + str9;
/* 3484 */           if (str6 != null && !str6.equals("") && str6.equals(URLEncoder.encode(str6, getCharSet())))
/* 3485 */             str10 = str10 + "&langCode=" + str6; 
/* 3487 */           if (str5 != null && !str5.equals(""))
/* 3488 */             str10 = str10 + "&home_url=" + URLEncoder.encode(str5, getCharSet()); 
/* 3492 */           if (str != null && !"".equals(str))
/* 3493 */             str10 = str10 + "&username=" + URLEncoder.encode(str, getCharSet()); 
/* 3496 */           if (bool3)
/* 3496 */             appsLog.write(str1, " INVALID or NULL status returnUrl=" + str2, 1); 
/* 3497 */           str2 = SSOUtil.getLocalLoginUrl(str10);
/* 3498 */           if (bool3)
/* 3498 */             appsLog.write(str1, "returnUrl ::" + str2, 1); 
/* 3500 */         } else if (authStatusCode.equals(SessionManager.AuthStatusCode.VALID)) {
/* 3502 */           if (bool3)
/* 3502 */             appsLog.write(str1, "validateLogin Successful:VALID", 1); 
/* 3503 */           connection = Utils.getConnection();
/* 3504 */           createSession(str, webAppsContext, paramHttpServletRequest, paramHttpServletResponse, false, null, str6);
/* 3505 */           webAppsContext.stampForcedAuthLabels();
/* 3508 */           String str7 = paramHttpServletRequest.getParameter("_lAccessibility");
/* 3509 */           if (str7 != null && (str7.equals("Y") || str7.equals("S")))
/* 3511 */             webAppsContext.getProfileStore().saveSpecificProfile("ICX_ACCESSIBILITY_FEATURES", str7, "USER", String.valueOf(Utils.getUserId(str)), null); 
/* 3515 */           if (str3 == null || str3.equals("") || str3.equals("APPSHOMEPAGE")) {
/* 3517 */             str3 = SSOUtil.getStartPageUrl(webAppsContext, URLHelper.isPDARequest(paramHttpServletRequest));
/* 3519 */             if (bool3)
/* 3519 */               appsLog.write(str1, " setting returnUrl=" + str3, 1); 
/* 3520 */             if (str5 != null) {
/* 3521 */               if (str3.indexOf("OracleMyPage.home") != -1) {
/* 3522 */                 if (str3.indexOf("?") != -1) {
/* 3523 */                   str3 = str3 + "&home_url=" + URLEncoder.encode(str5, getCharSet());
/*      */                 } else {
/* 3527 */                   str3 = str3 + "?home_url=" + URLEncoder.encode(str5, getCharSet());
/*      */                 } 
/* 3530 */                 if (bool3)
/* 3530 */                   appsLog.write(str1, "added home_url", 1); 
/*      */               } 
/* 3534 */             } else if (bool3) {
/* 3534 */               appsLog.write(str1, "home_url=NULL", 1);
/*      */             } 
/*      */           } 
/* 3537 */           str2 = str3;
/* 3538 */         } else if (authStatusCode.equals(SessionManager.AuthStatusCode.EXPIRED)) {
/* 3540 */           if (bool3)
/* 3540 */             appsLog.write(str1, "User password has expired:EXPIRED, creating Guest Session", 1); 
/* 3541 */           createSession(Utils.getGuestUserName(), webAppsContext, paramHttpServletRequest, paramHttpServletResponse, false, null, str6);
/* 3543 */           connection = Utils.getConnection(webAppsContext);
/* 3544 */           webAppsContext.setSessionAttribute("$FND$USER$NAME$", str);
/* 3546 */           String str7 = str3;
/* 3548 */           String str8 = null;
/* 3549 */           if (str6 != null && !"".equals(str6))
/* 3551 */             str8 = "langCode=" + URLEncoder.encode(str6, getCharSet()); 
/* 3553 */           String str9 = SSOUtil.getLocalLoginUrl(str8);
/* 3555 */           str8 = "returnUrl=" + URLEncoder.encode(str7, getCharSet());
/* 3557 */           str8 = str8 + "&cancelUrl=" + URLEncoder.encode(str9, getCharSet());
/* 3560 */           if (bool3)
/* 3560 */             appsLog.write(str1, "[CHGPWD]requestUrl=" + str3, 1); 
/* 3562 */           str2 = SSOUtil.getLocalPwdChangeUrl(str8);
/*      */         } 
/*      */       } 
/* 3565 */       if (bool2)
/* 3565 */         appsLog.write(str1, "END: returnUrl: " + str3, 2); 
/* 3567 */     } catch (Throwable throwable) {
/* 3568 */       if (bool4) {
/* 3569 */         appsLog.write(str1, "Exception" + throwable.toString(), 6);
/* 3570 */         appsLog.write(str1, throwable, 6);
/*      */       } 
/* 3573 */       throw new RuntimeException(throwable.toString());
/*      */     } finally {
/* 3576 */       if (!bool1)
/* 3576 */         Utils.releaseAppsContext(); 
/*      */     } 
/* 3578 */     return str2;
/*      */   }
/*      */   
/*      */   public static void createAppsSession(String paramString1, String paramString2, HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse) throws AuthenticationException {
/* 3598 */     WebAppsContext webAppsContext = null;
/* 3599 */     boolean bool = false;
/* 3600 */     Connection connection = null;
/* 3601 */     String str = "oracle.apps.fnd.sso.SessionMgr.createAppsSession(String, String, HttpServletRequest, HttpServletResponse)";
/* 3604 */     if (Utils.isAppsContextAvailable()) {
/* 3605 */       webAppsContext = Utils.getAppsContext();
/* 3606 */       bool = true;
/*      */     } else {
/* 3608 */       webAppsContext = Utils.getAppsContext();
/*      */     } 
/* 3610 */     webAppsContext.getErrorStack().clear();
/*      */     try {
/* 3612 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2)) {
/* 3614 */         Utils.writeToLog(str, "BEGIN", webAppsContext, 2);
/* 3615 */         Utils.writeToLog(str, "Before Calling validateLogin", webAppsContext, 2);
/*      */       } 
/* 3618 */       SessionManager sessionManager = webAppsContext.getSessionManager();
/* 3621 */       boolean bool1 = (allowLocalLogin(paramString1.trim(), webAppsContext) && sessionManager.validateLogin(paramString1.trim(), paramString2.trim())) ? true : false;
/* 3625 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 3626 */         Utils.writeToLog(str, "After Calling validateLogin check: " + bool1, webAppsContext); 
/* 3628 */       if (!bool1)
/* 3629 */         throw new AuthenticationException(); 
/* 3632 */       boolean bool2 = sessionManager.passwordExpired();
/* 3633 */       if (bool2) {
/* 3635 */         createSession(Utils.getGuestUserName(), webAppsContext, paramHttpServletRequest, paramHttpServletResponse, false, null);
/* 3636 */         connection = Utils.getConnection(webAppsContext);
/* 3637 */         saveSessionAttribute(connection, webAppsContext, "$FND$USER$NAME$", paramString1);
/*      */       } else {
/* 3640 */         createSession(paramString1, webAppsContext, paramHttpServletRequest, paramHttpServletResponse, false, null);
/*      */       } 
/*      */     } finally {
/* 3645 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 3646 */         Utils.writeToLog(str, "END", webAppsContext, 2); 
/* 3647 */       if (!bool)
/* 3647 */         Utils.releaseAppsContext(); 
/*      */     } 
/*      */   }
/*      */   
/*      */   public static boolean isPHPMode(HttpServletRequest paramHttpServletRequest) {
/* 3659 */     WebAppsContext webAppsContext = null;
/* 3660 */     boolean bool1 = false;
/* 3661 */     boolean bool2 = false;
/* 3663 */     String str = "oracle.apps.fnd.sso.SessionMgr.isPHPMode(HttpServletRequest)";
/* 3665 */     if (Utils.isAppsContextAvailable()) {
/* 3666 */       webAppsContext = Utils.getAppsContext();
/* 3667 */       bool1 = true;
/*      */     } else {
/* 3669 */       webAppsContext = Utils.getAppsContext();
/*      */     } 
/*      */     try {
/* 3672 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 3673 */         Utils.writeToLog(str, "BEGIN", webAppsContext, 2); 
/* 3674 */       Hashtable hashtable = getICXSessionInfo(getAppsCookie(paramHttpServletRequest), webAppsContext);
/* 3676 */       if (hashtable == null) {
/* 3677 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 3678 */           Utils.writeToLog(str, "No existing session", webAppsContext); 
/* 3679 */         bool2 = !Utils.isSSOMode() ? true : false;
/*      */       } else {
/* 3682 */         String str1 = (String)hashtable.get("MODE_CODE");
/* 3684 */         if (str1 == null || str1.equals("NULL") || str1.equals("115P"))
/* 3686 */           bool2 = true; 
/*      */       } 
/* 3689 */     } catch (Exception exception) {
/* 3690 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(4))
/* 3691 */         Utils.writeToLog(str, "Exception Occured: " + Utils.getExceptionStackTrace(exception), webAppsContext, 4); 
/*      */     } finally {
/* 3695 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 3696 */         Utils.writeToLog(str, "END: returnValue: " + bool2, webAppsContext, 2); 
/* 3697 */       if (!bool1)
/* 3698 */         Utils.releaseAppsContext(); 
/*      */     } 
/* 3701 */     return bool2;
/*      */   }
/*      */   
/*      */   public static boolean isValidGuestSession(HttpServletRequest paramHttpServletRequest) {
/* 3712 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.isValidGuestSession(HttpServletRequest request)";
/* 3715 */     String str2 = getAppsCookie(paramHttpServletRequest);
/* 3716 */     WebAppsContext webAppsContext = null;
/* 3717 */     boolean bool = false;
/* 3719 */     if (Utils.isAppsContextAvailable()) {
/* 3720 */       webAppsContext = Utils.getAppsContext();
/* 3721 */       bool = true;
/*      */     } else {
/* 3723 */       webAppsContext = Utils.getAppsContext();
/*      */     } 
/* 3725 */     if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 3726 */       Utils.writeToLog(str1, "BEGIN", webAppsContext, 2); 
/* 3728 */     if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 3729 */       Utils.writeToLog(str1, "Cookie Value: " + str2, webAppsContext); 
/* 3730 */     boolean bool1 = webAppsContext.validateSession(str2);
/* 3731 */     if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 3732 */       Utils.writeToLog(str1, "validateSession check: " + bool1, webAppsContext); 
/* 3733 */     if (!bool1) {
/* 3734 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 3735 */         Utils.writeToLog(str1, "END return false", webAppsContext, 2); 
/* 3736 */       return false;
/*      */     } 
/*      */     try {
/* 3740 */       String str3 = Utils.getGuestUserName();
/* 3741 */       String str4 = webAppsContext.getID(99);
/* 3743 */       if (str4 != null && str4.equalsIgnoreCase(str3)) {
/* 3744 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 3745 */           Utils.writeToLog(str1, "END return true", webAppsContext, 2); 
/* 3746 */         return true;
/*      */       } 
/* 3748 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 3749 */         Utils.writeToLog(str1, "END return false", webAppsContext, 2); 
/* 3750 */       return false;
/*      */     } finally {
/* 3753 */       if (!bool)
/* 3754 */         Utils.releaseAppsContext(); 
/*      */     } 
/*      */   }
/*      */   
/*      */   static void setPasswordExternal(String paramString1, String paramString2) {
/* 3768 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.setPasswordExternal(String, String)";
/* 3770 */     WebAppsContext webAppsContext = null;
/* 3771 */     boolean bool = false;
/* 3773 */     if (Utils.isAppsContextAvailable()) {
/* 3774 */       webAppsContext = Utils.getAppsContext();
/* 3775 */       bool = true;
/*      */     } else {
/* 3777 */       webAppsContext = Utils.getAppsContext();
/*      */     } 
/* 3779 */     if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 3780 */       Utils.writeToLog(str1, "BEGIN", webAppsContext, 2); 
/* 3782 */     ProfileStore profileStore = webAppsContext.getProfileStore();
/* 3783 */     String str2 = profileStore.getSpecificProfile("APPS_SSO_LOCAL_LOGIN", String.valueOf(Utils.getUserId(paramString1)), null, null);
/* 3786 */     if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 3787 */       Utils.writeToLog(str1, "APPS_SSO_LOCAL_LOGIN for user " + paramString1 + ": " + str2, webAppsContext); 
/* 3789 */     if (str2 == null || !str2.equals("SSO")) {
/* 3790 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 3791 */         Utils.writeToLog(str1, "Cannot set password to EXTERNAL", webAppsContext); 
/*      */       return;
/*      */     } 
/* 3797 */     String str3 = "update fnd_user set ENCRYPTED_USER_PASSWORD = 'EXTERNAL', ENCRYPTED_FOUNDATION_PASSWORD='EXTERNAL' where user_name = :1 and user_guid=:2";
/* 3799 */     PreparedStatement preparedStatement = null;
/* 3800 */     Connection connection = null;
/* 3801 */     ResultSet resultSet = null;
/* 3802 */     Object object = null;
/*      */     try {
/* 3805 */       if (paramString2 == null) {
/* 3806 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(4))
/* 3807 */           Utils.writeToLog(str1, "GUID is NULL", webAppsContext, 4); 
/* 3808 */         throw new RuntimeException(getMessage(webAppsContext, "FND-9916"));
/*      */       } 
/* 3810 */       connection = Utils.getConnection(webAppsContext);
/* 3811 */       preparedStatement = connection.prepareStatement(str3);
/* 3812 */       preparedStatement.setString(1, paramString1.toUpperCase());
/* 3813 */       preparedStatement.setString(2, paramString2);
/* 3814 */       int i = preparedStatement.executeUpdate();
/* 3816 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 3817 */         Utils.writeToLog(str1, "Executed update: " + str3, webAppsContext); 
/* 3818 */       if (i != 1) {
/* 3819 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 3820 */           Utils.writeToLog("oracle.apps.fnd.sso.SessionMgr.setPasswordExternal", "count is : " + i + " username : " + paramString1 + " guid: " + paramString2, webAppsContext, 1); 
/* 3824 */         throw new RuntimeException("Cannot update User Information. Multiple Accounts detected.");
/*      */       } 
/* 3827 */       if (!bool)
/* 3828 */         connection.commit(); 
/* 3830 */     } catch (Exception exception) {
/* 3831 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(4))
/* 3832 */         Utils.writeToLog(str1, "Exception occurred : " + Utils.getExceptionStackTrace(exception) + "\n username: " + paramString1 + " guid: " + paramString2, webAppsContext, 4); 
/* 3835 */       throw new RuntimeException(getMessage(webAppsContext, "FND-9916"));
/*      */     } finally {
/*      */       try {
/* 3839 */         if (resultSet != null)
/* 3840 */           resultSet.close(); 
/* 3842 */         if (preparedStatement != null)
/* 3843 */           preparedStatement.close(); 
/* 3846 */       } catch (Exception exception) {}
/* 3847 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 3848 */         Utils.writeToLog(str1, "END", webAppsContext, 2); 
/* 3849 */       if (!bool);
/*      */     } 
/* 3854 */     if (!bool)
/* 3855 */       Utils.releaseAppsContext(); 
/*      */   }
/*      */   
/* 3859 */   private static String site_char_set = null;
/*      */   
/*      */   static final String set_nls = "DECLARE\n   v varchar2(100);\nBEGIN\nselect ''''||min(NLS_LANGUAGE)||'''' into v from fnd_languages\n   where LANGUAGE_CODE=(select min(LANGUAGE) from  FND_LANGUAGES_TL);\n  dbms_session.set_nls('NLS_LANGUAGE',v);\n :1 := userenv('LANG'); end;\n";
/*      */   
/*      */   public static String getCharSet(WebAppsContext paramWebAppsContext) {
/* 3869 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.getCharSet(wctx)";
/* 3870 */     AppsLog appsLog = Utils.getLog(paramWebAppsContext);
/* 3871 */     boolean bool1 = appsLog.isEnabled(str1, 2);
/* 3872 */     boolean bool2 = appsLog.isEnabled(str1, 1);
/* 3874 */     if (bool1)
/* 3874 */       appsLog.write(str1, "BEGIN", 2); 
/* 3876 */     String str2 = null;
/*      */     try {
/* 3881 */       ProfileStore profileStore = paramWebAppsContext.getProfileStore();
/* 3885 */       str2 = profileStore.getProfile("ICX_CLIENT_IANA_ENCODING");
/* 3887 */       if (bool2)
/* 3887 */         appsLog.write(str1, "ICX_CLIENT_IANA_ENCODING: " + str2, 1); 
/* 3889 */       if (str2 == null)
/* 3890 */         str2 = "ISO-8859-1"; 
/*      */     } finally {
/* 3894 */       if (bool1)
/* 3894 */         appsLog.write(str1, "END: returnValue: " + str2, 2); 
/*      */     } 
/* 3897 */     return str2;
/*      */   }
/*      */   
/*      */   public static String getCharSet() {
/* 3901 */     boolean bool = Utils.isAppsContextAvailable();
/*      */     try {
/* 3904 */       return getCharSet(Utils.getAppsContext());
/*      */     } finally {
/* 3908 */       if (!bool)
/* 3908 */         Utils.releaseAppsContext(); 
/*      */     } 
/*      */   }
/*      */   
/*      */   public static Vector getInstalledLanguages() {
/* 3918 */     loadInstalledLanguages();
/* 3919 */     return installedLanguagesInfo;
/*      */   }
/*      */   
/*      */   public static Vector getInstalledLangCodes() {
/* 3928 */     loadInstalledLanguages();
/* 3929 */     return installedLanguagesCode;
/*      */   }
/*      */   
/*      */   public static boolean isInstalledLanguage(String paramString, WebAppsContext paramWebAppsContext) {
/* 3938 */     String str = "oracle.apps.fnd.sso.SessionMgr.isInstalledLanguage";
/* 3939 */     AppsLog appsLog = Utils.getLog(paramWebAppsContext);
/* 3940 */     boolean bool1 = appsLog.isEnabled(str, 2);
/* 3941 */     boolean bool2 = appsLog.isEnabled(str, 1);
/* 3943 */     if (bool1)
/* 3943 */       appsLog.write(str, "BEGIN", 2); 
/*      */     try {
/* 3945 */       if (bool2)
/* 3945 */         appsLog.write(str, "langCode=" + paramString, 2); 
/* 3946 */       if (paramString == null) {
/* 3947 */         if (bool1)
/* 3947 */           appsLog.write(str, "END->false", 2); 
/* 3949 */         return false;
/*      */       } 
/* 3951 */       loadInstalledLanguages();
/* 3952 */       boolean bool = installedLanguagesCode.contains(paramString.toUpperCase());
/* 3954 */       if (bool1)
/* 3954 */         appsLog.write(str, "END->" + bool, 2); 
/* 3955 */       return bool;
/* 3956 */     } catch (Exception exception) {
/* 3958 */       if (appsLog.isEnabled(str, 6))
/* 3959 */         appsLog.write(str, exception, 6); 
/* 3960 */       if (bool1)
/* 3960 */         appsLog.write(str, "END with exceptions", 2); 
/* 3962 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   public static boolean isInstalledLanguage(String paramString) {
/* 3967 */     boolean bool = Utils.isAppsContextAvailable();
/*      */     try {
/* 3970 */       return isInstalledLanguage(paramString, Utils.getAppsContext());
/*      */     } finally {
/* 3974 */       if (!bool)
/* 3974 */         Utils.releaseAppsContext(); 
/*      */     } 
/*      */   }
/*      */   
/*      */   private static boolean isLangLoaded(String paramString) {
/* 3984 */     boolean bool = false;
/* 3985 */     bool = loadedLanguages.contains(paramString);
/* 3986 */     return bool;
/*      */   }
/*      */   
/*      */   private static void loadSpecificInstalledLanguages() {
/* 3999 */     String str = "oracle.apps.fnd.sso.SessionMgr.loadSpecificInstalledLanguages()";
/* 4002 */     synchronized (SessionMgr.class) {
/* 4003 */       WebAppsContext webAppsContext = null;
/* 4004 */       boolean bool = false;
/* 4007 */       if (Utils.isAppsContextAvailable()) {
/* 4008 */         webAppsContext = Utils.getAppsContext();
/* 4009 */         bool = true;
/*      */       } else {
/* 4011 */         webAppsContext = Utils.getAppsContext();
/*      */       } 
/* 4013 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 4014 */         Utils.writeToLog(str, "BEGIN", webAppsContext, 2); 
/* 4015 */       String str1 = webAppsContext.getCurrLangCode();
/* 4017 */       if (isLangLoaded(str1)) {
/* 4018 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 4019 */           Utils.writeToLog(str, "Specific info for lang already loaded: " + str1, webAppsContext); 
/*      */         return;
/*      */       } 
/* 4024 */       loadedLanguages.addElement(str1);
/* 4029 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 4030 */         Utils.writeToLog(str, "Loading specific into for a lang  for first time wctx.getCurrLangCode(): " + str1, webAppsContext); 
/* 4034 */       String str2 = "SELECT B.LANGUAGE_CODE, B.NLS_LANGUAGE, B.NLS_TERRITORY, B.NLS_CODESET,B.INSTALLED_FLAG, T.DESCRIPTION FROM FND_LANGUAGES_TL T, FND_LANGUAGES B WHERE B.LANGUAGE_CODE = T.LANGUAGE_CODE AND T.LANGUAGE = :1  AND B.INSTALLED_FLAG IN ('I', 'B') ORDER BY B.LANGUAGE_CODE";
/* 4042 */       OraclePreparedStatement oraclePreparedStatement = null;
/* 4043 */       Connection connection = null;
/* 4044 */       ResultSet resultSet = null;
/* 4045 */       String str3 = null;
/*      */       try {
/* 4048 */         connection = Utils.getConnection(webAppsContext);
/* 4049 */         oraclePreparedStatement = (OraclePreparedStatement)connection.prepareStatement(str2);
/* 4050 */         oraclePreparedStatement.setString(1, webAppsContext.getCurrLangCode());
/* 4051 */         oraclePreparedStatement.defineColumnType(1, 12, 4);
/* 4052 */         oraclePreparedStatement.defineColumnType(2, 12, 30);
/* 4054 */         oraclePreparedStatement.defineColumnType(3, 12, 30);
/* 4055 */         oraclePreparedStatement.defineColumnType(4, 12, 30);
/* 4056 */         oraclePreparedStatement.defineColumnType(5, 12, 1);
/* 4057 */         oraclePreparedStatement.defineColumnType(6, 12, 255);
/* 4059 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 4060 */           Utils.writeToLog(str, "Executing query: " + str2, webAppsContext); 
/* 4061 */         resultSet = oraclePreparedStatement.executeQuery();
/* 4063 */         while (resultSet.next()) {
/* 4064 */           StringBuffer stringBuffer = new StringBuffer(5);
/* 4065 */           stringBuffer.append(str1);
/* 4066 */           stringBuffer.append("_");
/* 4067 */           stringBuffer.append(resultSet.getString(1));
/* 4069 */           LangInfo langInfo = new LangInfo(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), resultSet.getString(5), resultSet.getString(6));
/* 4073 */           str3 = resultSet.getString(6);
/* 4074 */           if (((AppsLog)webAppsContext.getLog()).isEnabled(1))
/* 4075 */             Utils.writeToLog(str, "Lang Desc => " + str3, webAppsContext); 
/* 4077 */           installedSpecificLangInfoTable.put(stringBuffer.toString(), langInfo);
/* 4078 */           installedSpecificLangDescTable.put(stringBuffer.toString(), str3);
/*      */         } 
/* 4080 */       } catch (Exception exception) {
/* 4081 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(4))
/* 4082 */           Utils.writeToLog(str, "Exception occurred: " + Utils.getExceptionStackTrace(exception), webAppsContext, 4); 
/* 4084 */         throw new RuntimeException(getMessage(webAppsContext, "FND-9914"));
/*      */       } finally {
/* 4087 */         if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 4088 */           Utils.writeToLog(str, "END", webAppsContext, 2); 
/*      */         try {
/* 4090 */           if (resultSet != null)
/* 4091 */             resultSet.close(); 
/* 4093 */           if (oraclePreparedStatement != null)
/* 4094 */             oraclePreparedStatement.close(); 
/* 4096 */         } catch (Exception exception) {}
/* 4097 */         if (!bool);
/* 4100 */         if (!bool)
/* 4101 */           Utils.releaseAppsContext(); 
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void loadInstalledLanguages() {
/* 4122 */     if (initLang == true)
/*      */       return; 
/* 4125 */     String str = "oracle.apps.fnd.sso.SessionMgr.loadInstalledLanguages()";
/* 4126 */     synchronized (SessionMgr.class) {
/* 4127 */       if (initLang == true)
/*      */         return; 
/* 4131 */       boolean bool1 = Utils.isAppsContextAvailable();
/* 4132 */       WebAppsContext webAppsContext = Utils.getAppsContext();
/* 4133 */       AppsLog appsLog = Utils.getLog(webAppsContext);
/* 4134 */       boolean bool2 = appsLog.isEnabled(str, 2);
/* 4135 */       boolean bool3 = appsLog.isEnabled(str, 1);
/* 4137 */       if (bool2)
/* 4137 */         appsLog.write(str, "BEGIN", 2); 
/* 4139 */       String str1 = "SELECT LANGUAGE_CODE, NLS_LANGUAGE, NLS_TERRITORY, NLS_CODESET,INSTALLED_FLAG, DESCRIPTION FROM FND_LANGUAGES_VL WHERE installed_flag IN ('I', 'B') ORDER BY language_code";
/* 4152 */       OraclePreparedStatement oraclePreparedStatement = null;
/* 4153 */       Connection connection = null;
/* 4154 */       ResultSet resultSet = null;
/* 4156 */       byte b = 0;
/*      */       try {
/* 4159 */         connection = Utils.getConnection(webAppsContext);
/* 4160 */         if (bool3)
/* 4160 */           appsLog.write(str, "got Connection", 1); 
/* 4162 */         if (bool3)
/* 4162 */           appsLog.write(str, "Seting lang" + str1, 1); 
/* 4163 */         CallableStatement callableStatement = connection.prepareCall("DECLARE\n   v varchar2(100);\nBEGIN\nselect ''''||min(NLS_LANGUAGE)||'''' into v from fnd_languages\n   where LANGUAGE_CODE=(select min(LANGUAGE) from  FND_LANGUAGES_TL);\n  dbms_session.set_nls('NLS_LANGUAGE',v);\n :1 := userenv('LANG'); end;\n");
/* 4164 */         callableStatement.registerOutParameter(1, 12);
/* 4165 */         callableStatement.execute();
/* 4166 */         String str2 = callableStatement.getString(1);
/* 4167 */         callableStatement.close();
/* 4168 */         if (bool3)
/* 4169 */           appsLog.write(str, "userenv('LANG')=" + str2, 1); 
/* 4171 */         oraclePreparedStatement = (OraclePreparedStatement)connection.prepareStatement(str1);
/* 4172 */         if (bool3)
/* 4172 */           appsLog.write(str, "SQL=" + str1, 1); 
/* 4174 */         oraclePreparedStatement.defineColumnType(1, 12, 4);
/* 4175 */         oraclePreparedStatement.defineColumnType(2, 12, 30);
/* 4176 */         oraclePreparedStatement.defineColumnType(3, 12, 30);
/* 4177 */         oraclePreparedStatement.defineColumnType(4, 12, 30);
/* 4178 */         oraclePreparedStatement.defineColumnType(5, 12, 1);
/* 4179 */         oraclePreparedStatement.defineColumnType(6, 12, 255);
/* 4181 */         resultSet = oraclePreparedStatement.executeQuery();
/* 4183 */         while (resultSet.next()) {
/* 4184 */           b++;
/* 4185 */           String str3 = resultSet.getString(1);
/* 4186 */           String str4 = resultSet.getString(2);
/* 4187 */           String str5 = resultSet.getString(3);
/* 4188 */           String str6 = resultSet.getString(4);
/* 4189 */           String str7 = resultSet.getString(5);
/* 4190 */           String str8 = resultSet.getString(6);
/* 4192 */           if (bool3)
/* 4193 */             appsLog.write(str, "Loading: " + str3 + " " + str4 + " " + str5 + " " + str6 + " " + str7 + " " + str8, 1); 
/* 4200 */           LangInfo langInfo = new LangInfo(str3, str4, str5, str6, str7, str8);
/* 4203 */           installedLanguagesInfo.addElement(langInfo);
/* 4204 */           installedLanguagesCode.addElement(resultSet.getString(1));
/* 4205 */           installedLanguagesDesc.addElement(resultSet.getString(6));
/*      */         } 
/* 4207 */         if (bool3)
/* 4207 */           appsLog.write(str, "Loaded  " + b + " languages", 1); 
/* 4208 */         if (b == 0) {
/* 4210 */           if (appsLog.isEnabled(str, 6))
/* 4211 */             appsLog.write(str, "NO language found", 6); 
/* 4212 */           initLang = false;
/* 4213 */           throw new RuntimeException(getMessage(webAppsContext, "FND-9914"));
/*      */         } 
/* 4217 */         initLang = true;
/* 4218 */       } catch (Exception exception) {
/* 4219 */         if (appsLog.isEnabled(str, 6))
/* 4220 */           appsLog.write(str, exception, 6); 
/* 4221 */         if (bool2)
/* 4221 */           appsLog.write(str, "END with exception", 2); 
/* 4222 */         throw new RuntimeException(getMessage(webAppsContext, "FND-9914"));
/*      */       } finally {
/* 4225 */         if (bool2)
/* 4225 */           appsLog.write(str, "END", 2); 
/*      */         try {
/* 4227 */           if (resultSet != null)
/* 4228 */             resultSet.close(); 
/* 4230 */           if (oraclePreparedStatement != null)
/* 4231 */             oraclePreparedStatement.close(); 
/* 4233 */         } catch (Exception exception) {}
/* 4234 */         if (!bool1);
/* 4237 */         if (!bool1)
/* 4238 */           Utils.releaseAppsContext(); 
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   public static boolean isRtl(String paramString) {
/* 4251 */     if (paramString == null)
/* 4252 */       return false; 
/* 4255 */     if (paramString.equalsIgnoreCase("AR") || paramString.equalsIgnoreCase("IW"))
/* 4256 */       return true; 
/* 4259 */     return false;
/*      */   }
/*      */   
/*      */   public static Vector getInstalledLanguageImages(HttpServletRequest paramHttpServletRequest, WebAppsContext paramWebAppsContext) {
/* 4272 */     return getInstalledLanguageImages(paramHttpServletRequest, paramWebAppsContext, false);
/*      */   }
/*      */   
/*      */   public static Vector getInstalledLanguageImages(HttpServletRequest paramHttpServletRequest, WebAppsContext paramWebAppsContext, boolean paramBoolean) {
/* 4285 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.getInstalledLanguageImages(HttpServletRequest, WebAppsContext, boolean)";
/* 4288 */     byte b = 0;
/* 4289 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 4290 */       Utils.writeToLog(str1, "BEGIN", paramWebAppsContext, 2); 
/* 4292 */     loadInstalledLanguages();
/* 4293 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 4294 */       Utils.writeToLog(str1, "Loaded installed languages", paramWebAppsContext); 
/* 4295 */     Vector<String> vector = new Vector();
/* 4297 */     if (installedLanguagesCode.size() <= 1) {
/* 4298 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 4299 */         Utils.writeToLog(str1, "END no installed langs", paramWebAppsContext, 2); 
/* 4300 */       return vector;
/*      */     } 
/* 4303 */     int i = installedLanguagesCode.size();
/* 4304 */     String str2 = paramWebAppsContext.getCurrLangCode();
/* 4305 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 4306 */       Utils.writeToLog(str1, "size: " + i + " currLang: " + str2, paramWebAppsContext); 
/* 4308 */     while (b < i) {
/* 4309 */       String str3 = installedLanguagesCode.elementAt(b);
/* 4312 */       String str4 = installedLanguagesDesc.elementAt(b);
/* 4313 */       String str5 = "";
/* 4315 */       if (str3.equals(str2)) {
/* 4316 */         str5 = "<A HREF=\"" + getLink(paramWebAppsContext, paramHttpServletRequest, str3, paramBoolean) + "\"> <IMG SRC=\"/OA_MEDIA/nls" + str3.toLowerCase() + "_a.gif\" ALT=\"" + str4 + "\" border=\"0\"></A>";
/*      */       } else {
/* 4322 */         str5 = "<A HREF=\"" + getLink(paramWebAppsContext, paramHttpServletRequest, str3, paramBoolean) + "\"> <IMG SRC=\"/OA_MEDIA/nls" + str3.toLowerCase() + ".gif\" ALT=\"" + str4 + "\" border=\"0\"></A>";
/*      */       } 
/* 4328 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 4329 */         Utils.writeToLog(str1, "Link: " + str5, paramWebAppsContext); 
/* 4330 */       vector.addElement(str5);
/* 4331 */       b++;
/*      */     } 
/* 4333 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 4334 */       Utils.writeToLog(str1, "END returning langImages: " + vector, paramWebAppsContext, 2); 
/* 4335 */     return vector;
/*      */   }
/*      */   
/*      */   public static Vector getInstalledLanguageImgInfo(HttpServletRequest paramHttpServletRequest, WebAppsContext paramWebAppsContext) {
/* 4345 */     return getInstalledLanguageImgInfo(paramHttpServletRequest, paramWebAppsContext, false);
/*      */   }
/*      */   
/*      */   public static Vector getInstalledLanguageImgInfo(HttpServletRequest paramHttpServletRequest, WebAppsContext paramWebAppsContext, boolean paramBoolean) {
/* 4360 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.getInstalledLanguageImgInfo(HttpServletRequest, WebAppsContext, boolean)";
/* 4363 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 4364 */       Utils.writeToLog(str1, "BEGIN", paramWebAppsContext, 2); 
/* 4366 */     loadSpecificInstalledLanguages();
/* 4367 */     Vector<Vector<String>> vector = new Vector();
/* 4369 */     if (installedLanguagesCode.size() <= 1)
/* 4372 */       return vector; 
/* 4377 */     int i = installedLanguagesCode.size();
/* 4378 */     byte b = 0;
/* 4379 */     String str2 = paramWebAppsContext.getCurrLangCode();
/* 4381 */     while (b < i) {
/* 4382 */       String str3 = installedLanguagesCode.elementAt(b);
/* 4386 */       StringBuffer stringBuffer = new StringBuffer(5);
/* 4387 */       stringBuffer.append(str2);
/* 4388 */       stringBuffer.append("_");
/* 4389 */       stringBuffer.append(str3);
/* 4390 */       String str4 = (String)installedSpecificLangDescTable.get(stringBuffer.toString());
/* 4392 */       Vector<String> vector1 = new Vector(3);
/* 4394 */       String str5 = "";
/* 4396 */       if (str3.equals(str2)) {
/* 4397 */         vector1.addElement(str3);
/* 4398 */         vector1.addElement("/OA_MEDIA/nls" + str3.toLowerCase() + "_a.gif");
/* 4399 */         vector1.addElement(str4);
/* 4400 */         vector1.addElement(getLink(paramWebAppsContext, paramHttpServletRequest, str3, paramBoolean));
/*      */       } else {
/* 4402 */         vector1.addElement(str3);
/* 4403 */         vector1.addElement("/OA_MEDIA/nls" + str3.toLowerCase() + ".gif");
/* 4404 */         if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 4405 */           Utils.writeToLog(str1, "i => " + b + " langDesc => " + str4, paramWebAppsContext); 
/* 4407 */         vector1.addElement(str4);
/* 4408 */         vector1.addElement(getLink(paramWebAppsContext, paramHttpServletRequest, str3, paramBoolean));
/*      */       } 
/* 4411 */       vector.addElement(vector1);
/* 4412 */       b++;
/*      */     } 
/* 4414 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 4415 */       Utils.writeToLog(str1, "END: return: " + vector, paramWebAppsContext, 2); 
/* 4416 */     return vector;
/*      */   }
/*      */   
/*      */   private static String getLink(WebAppsContext paramWebAppsContext, HttpServletRequest paramHttpServletRequest, String paramString, boolean paramBoolean) {
/* 4432 */     String str1 = "oracle.apps.fnd.sso.SessionMgr[$Revision: 115.112.2.10].getLink(WebAppsContext, HttpServletRequest, String, boolean)";
/* 4433 */     AppsLog appsLog = Utils.getLog(paramWebAppsContext);
/* 4434 */     boolean bool1 = appsLog.isEnabled(str1, 2);
/* 4435 */     boolean bool2 = appsLog.isEnabled(str1, 1);
/* 4436 */     boolean bool3 = appsLog.isEnabled(str1, 4);
/* 4438 */     if (bool1)
/* 4438 */       appsLog.write(str1, "BEGIN", 2); 
/* 4440 */     String str2 = null;
/* 4442 */     if (bool2) {
/* 4443 */       appsLog.write(str1, "\trequest=" + ((paramHttpServletRequest != null) ? paramHttpServletRequest.toString() : "*NUL*"), 1);
/* 4444 */       appsLog.write(str1, "\tlangCode=" + ((paramString != null) ? paramString : "*NULL*"), 1);
/* 4445 */       appsLog.write(str1, "\tisSSO=" + (paramBoolean ? "true" : "false"), 1);
/*      */     } 
/* 4448 */     StringBuffer stringBuffer1 = getRequestURL(paramWebAppsContext, paramHttpServletRequest);
/* 4449 */     stringBuffer1.append("?langCode=");
/* 4450 */     stringBuffer1.append(paramString);
/* 4455 */     StringBuffer stringBuffer2 = new StringBuffer(128);
/* 4457 */     if (paramBoolean) {
/* 4462 */       String str3 = paramHttpServletRequest.getParameter("p_submit_url");
/* 4463 */       StringBuffer stringBuffer3 = new StringBuffer(str3.substring(0, str3.indexOf("/ORASSO.")));
/* 4464 */       String str4 = paramHttpServletRequest.getParameter("site2pstoretoken");
/* 4465 */       String str5 = paramHttpServletRequest.getParameter("ssousername");
/* 4466 */       String str6 = paramHttpServletRequest.getParameter("p_error_code");
/* 4467 */       String str7 = paramHttpServletRequest.getParameter("p_cancel_url");
/* 4468 */       if (bool2)
/* 4469 */         appsLog.write(str1, "SSO URL:\tp_submit_url=" + str3 + "\n" + "\tsite2pstoretoken=" + str4 + "\n" + "\tssousername=" + str5 + "\n" + "\tp_error_code=" + str6 + "\n" + "\tp_cancel_url=" + str7, 1); 
/* 4479 */       stringBuffer3.append("ORASSO.wwctx_app_language.set_language?p_http_language=");
/* 4481 */       stringBuffer3.append(Authenticator.lmap.getHttpLangFromOracle(paramString));
/* 4482 */       stringBuffer3.append("&p_nls_language=");
/* 4483 */       stringBuffer3.append(paramString);
/* 4484 */       stringBuffer3.append("&p_nls_territory=");
/* 4486 */       stringBuffer3.append(Authenticator.lmap.getTerritoryCode(paramString));
/* 4487 */       if (bool2)
/* 4487 */         appsLog.write(str1, "p_http_language=" + Authenticator.lmap.getHttpLangFromOracle(paramString) + "\np_nls_language=" + paramString + "\np_nls_territory=" + Authenticator.lmap.getTerritoryCode(paramString), 1); 
/* 4491 */       stringBuffer3.append("&p_requested_url=");
/* 4493 */       StringBuffer stringBuffer4 = new StringBuffer(128);
/* 4494 */       stringBuffer4.append(stringBuffer1.toString() + "&site2pstoretoken=");
/* 4495 */       stringBuffer4.append(str4);
/* 4496 */       stringBuffer4.append("&ssousername=");
/* 4497 */       stringBuffer4.append(str5);
/* 4498 */       stringBuffer4.append("&p_cancel_url=");
/* 4499 */       stringBuffer4.append(str7);
/* 4500 */       stringBuffer3.append(URLEncoder.encode(stringBuffer4.toString(), getCharSet()));
/* 4502 */       str2 = stringBuffer3.toString();
/*      */     } else {
/* 4506 */       Enumeration<String> enumeration = paramHttpServletRequest.getParameterNames();
/* 4507 */       stringBuffer2.append("langCode=" + URLEncoder.encode(paramString, getCharSet()));
/* 4509 */       while (enumeration != null && enumeration.hasMoreElements()) {
/* 4510 */         String str = enumeration.nextElement();
/* 4511 */         if (!str.equals("langCode") && !str.equals("errCode") && !str.equals("errText")) {
/* 4513 */           String str3 = paramHttpServletRequest.getParameter(str);
/* 4514 */           stringBuffer2.append("&");
/* 4515 */           stringBuffer2.append(URLEncoder.encode(str, getCharSet()));
/* 4517 */           stringBuffer2.append("=");
/* 4518 */           stringBuffer2.append(URLEncoder.encode(str3, getCharSet()));
/*      */           continue;
/*      */         } 
/* 4522 */         if (bool2)
/* 4522 */           appsLog.write(str1, "Not copying " + str, 1); 
/*      */       } 
/* 4525 */       str2 = SSOUtil.getLocalLoginUrl(stringBuffer2.toString());
/*      */     } 
/* 4527 */     if (bool1)
/* 4527 */       appsLog.write(str1, "END: return: " + str2, 2); 
/* 4528 */     return str2;
/*      */   }
/*      */   
/*      */   public static StringBuffer getRequestURL(WebAppsContext paramWebAppsContext, HttpServletRequest paramHttpServletRequest) {
/* 4539 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.getRequestURL(WebAppsContext, HttpServletRequest)";
/* 4541 */     StringBuffer stringBuffer = new StringBuffer();
/* 4543 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 4544 */       Utils.writeToLog(str1, "BEGIN", paramWebAppsContext, 2); 
/* 4546 */     String str2 = paramHttpServletRequest.getRequestURI();
/* 4547 */     String str3 = paramHttpServletRequest.getServletPath();
/* 4548 */     String str4 = paramHttpServletRequest.getPathInfo();
/* 4551 */     String str5 = AppsAgent.getServer();
/* 4552 */     stringBuffer.append(str5.substring(0, str5.length() - 1));
/* 4554 */     if (str3 != null) {
/* 4555 */       stringBuffer.append(str3);
/* 4556 */       if (str4 != null)
/* 4557 */         stringBuffer.append(str4); 
/* 4561 */     } else if (str2 != null) {
/* 4562 */       stringBuffer.append(str2);
/*      */     } 
/* 4565 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 4566 */       Utils.writeToLog(str1, "END: return: " + stringBuffer, paramWebAppsContext, 2); 
/* 4567 */     return stringBuffer;
/*      */   }
/*      */   
/*      */   public static String getConnLang(Connection paramConnection) {
/* 4576 */     if (paramConnection == null)
/* 4577 */       return null; 
/* 4579 */     String str1 = "select userenv('LANG') from dual";
/* 4580 */     Statement statement = null;
/* 4581 */     String str2 = null;
/* 4582 */     ResultSet resultSet = null;
/*      */     try {
/* 4585 */       statement = paramConnection.createStatement();
/* 4586 */       resultSet = statement.executeQuery(str1);
/* 4587 */       while (resultSet.next())
/* 4588 */         str2 = resultSet.getString(1); 
/* 4590 */     } catch (Exception exception) {
/* 4591 */       throw new RuntimeException(Utils.getExceptionStackTrace(exception));
/*      */     } finally {
/*      */       try {
/* 4595 */         if (resultSet != null)
/* 4596 */           resultSet.close(); 
/* 4598 */       } catch (Exception exception) {}
/*      */       try {
/* 4600 */         if (statement != null)
/* 4601 */           statement.close(); 
/* 4603 */       } catch (Exception exception) {}
/*      */     } 
/* 4605 */     return str2;
/*      */   }
/*      */   
/*      */   public static void setUserLanguage(HttpServletRequest paramHttpServletRequest, WebAppsContext paramWebAppsContext) {
/* 4617 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.setUserLanguage(HttpServletRequest, WebAppsContext)";
/* 4620 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 4621 */       Utils.writeToLog(str1, "BEGIN", paramWebAppsContext, 2); 
/* 4623 */     String str2 = paramHttpServletRequest.getParameter("langCode");
/* 4625 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 4626 */       Utils.writeToLog(str1, "checking langCode: " + str2, paramWebAppsContext); 
/* 4629 */     if (str2 == null) {
/* 4630 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 4631 */         Utils.writeToLog(str1, "END not set", paramWebAppsContext, 2); 
/*      */       return;
/*      */     } 
/* 4635 */     String str3 = paramWebAppsContext.getID(99);
/* 4636 */     String str4 = Utils.getGuestUserName();
/* 4638 */     if (str3.equalsIgnoreCase(str4)) {
/* 4639 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 4640 */         Utils.writeToLog(str1, "END lang not set for GUEST user", paramWebAppsContext, 2); 
/*      */       return;
/*      */     } 
/* 4644 */     ProfileStore profileStore = paramWebAppsContext.getProfileStore();
/* 4645 */     String str5 = profileStore.getSpecificProfile("ICX_LANGUAGE", null, null, null);
/* 4647 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 4648 */       Utils.writeToLog(str1, "sitelang: " + str5, paramWebAppsContext); 
/* 4649 */     String str6 = null;
/*      */     try {
/* 4652 */       Connection connection = Utils.getConnection(paramWebAppsContext);
/* 4653 */       str6 = getUserLanguage("ICX_LANGUAGE", paramWebAppsContext.getUserId(), connection);
/* 4654 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 4655 */         Utils.writeToLog(str1, "userlang: " + str6, paramWebAppsContext); 
/* 4656 */     } catch (Exception exception) {
/* 4658 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 4659 */         Utils.writeToLog(str1, "Exception occurred getting user lang. returning...user is : " + str3 + " User Id : " + paramWebAppsContext.getUserId() + "Selected Language is : " + str2 + " User Language is : " + str6, paramWebAppsContext, 4); 
/*      */       return;
/*      */     } 
/* 4668 */     if (str6 != null && str5 != null && str6.equals(str5)) {
/* 4670 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 4671 */         Utils.writeToLog(str1, "user is : " + str3 + " User Id : " + paramWebAppsContext.getUserId() + " selected Language is : " + str2 + " User Language is : " + str6 + " sitelang is =>" + str5, paramWebAppsContext); 
/*      */       return;
/*      */     } 
/* 4679 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 4680 */       Utils.writeToLog(str1, "user is : " + str3 + " User Id : " + paramWebAppsContext.getUserId() + " Selected Language is : " + str2 + " User Language is : " + str6, paramWebAppsContext); 
/* 4685 */     String str7 = paramWebAppsContext.getNLSLanguage(str2.toUpperCase());
/* 4687 */     if (str6 == null && str7 != null && str5 != null && !str7.equals(str5))
/* 4690 */       profileStore.saveSpecificProfile("ICX_LANGUAGE", str7, "USER", String.valueOf(Utils.getUserId(str3)), null); 
/* 4693 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 4694 */       Utils.writeToLog(str1, "END", paramWebAppsContext, 2); 
/*      */   }
/*      */   
/*      */   private static String getUserLanguage(String paramString, int paramInt, Connection paramConnection) throws Exception {
/* 4709 */     String str1 = "select V.PROFILE_OPTION_VALUE from  FND_PROFILE_OPTIONS O, FND_PROFILE_OPTION_VALUES V where O.PROFILE_OPTION_NAME = :1 and   O.START_DATE_ACTIVE <= SYSDATE and    (nvl(O.END_DATE_ACTIVE,SYSDATE) >= SYSDATE) and    (V.LEVEL_ID = :2 and         V.LEVEL_VALUE = :3) and    O.PROFILE_OPTION_ID = V.PROFILE_OPTION_ID and    O.APPLICATION_ID    = V.APPLICATION_ID";
/* 4719 */     if (paramConnection == null)
/* 4720 */       return null; 
/* 4723 */     OraclePreparedStatement oraclePreparedStatement = null;
/* 4724 */     ResultSet resultSet = null;
/* 4725 */     String str2 = null;
/* 4726 */     long l1 = 10004L;
/* 4727 */     long l2 = paramInt;
/*      */     try {
/* 4730 */       oraclePreparedStatement = (OraclePreparedStatement)paramConnection.prepareStatement(str1);
/* 4731 */       oraclePreparedStatement.defineColumnType(1, 12, 240);
/* 4732 */       oraclePreparedStatement.setString(1, paramString);
/* 4733 */       oraclePreparedStatement.setLong(2, l1);
/* 4734 */       oraclePreparedStatement.setLong(3, l2);
/* 4735 */       resultSet = oraclePreparedStatement.executeQuery();
/* 4737 */       if (resultSet.next())
/* 4738 */         str2 = resultSet.getString(1); 
/* 4740 */     } catch (Exception exception) {
/* 4741 */       throw exception;
/*      */     } finally {
/*      */       try {
/* 4745 */         if (oraclePreparedStatement != null)
/* 4746 */           oraclePreparedStatement.close(); 
/* 4748 */         if (resultSet != null)
/* 4749 */           resultSet.close(); 
/* 4751 */       } catch (Exception exception) {}
/*      */     } 
/* 4754 */     return str2;
/*      */   }
/*      */   
/*      */   private static String getSessionCookieName(WebAppsContext paramWebAppsContext) {
/* 4764 */     if (sessionCookieName != null)
/* 4765 */       return sessionCookieName; 
/* 4768 */     synchronized (SessionMgr.class) {
/* 4769 */       if (sessionCookieName != null)
/* 4770 */         return sessionCookieName; 
/* 4773 */       sessionCookieName = paramWebAppsContext.getSessionCookieName();
/*      */     } 
/* 4775 */     return sessionCookieName;
/*      */   }
/*      */   
/*      */   static String getMessage(WebAppsContext paramWebAppsContext, String paramString) {
/* 4785 */     Message message = new Message("FND", paramString);
/* 4786 */     return message.getMessageText(paramWebAppsContext.getResourceStore());
/*      */   }
/*      */   
/*      */   static String getUserInAppsSession(HttpServletRequest paramHttpServletRequest, WebAppsContext paramWebAppsContext) {
/* 4800 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.getUserInAppsSession(HttpServletRequest, WebAppsContext)";
/* 4803 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 4804 */       Utils.writeToLog(str1, "BEGIN", paramWebAppsContext, 2); 
/* 4805 */     String str2 = null;
/* 4806 */     String str3 = getAppsCookie(paramHttpServletRequest);
/* 4808 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 4809 */       Utils.writeToLog(str1, "Cookie Value: " + str3, paramWebAppsContext); 
/* 4811 */     if (str3 != null && !str3.equals("-1")) {
/* 4812 */       String str = paramWebAppsContext.checkSession(str3);
/* 4813 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 4814 */         Utils.writeToLog(str1, "Check Session check: " + str, paramWebAppsContext); 
/* 4815 */       if (str != null && "VALID".equals(str)) {
/* 4816 */         boolean bool = paramWebAppsContext.validateSession(str3);
/* 4817 */         if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 4818 */           Utils.writeToLog(str1, "isSessionValidated: " + bool, paramWebAppsContext); 
/* 4819 */         if (bool)
/* 4820 */           str2 = paramWebAppsContext.getID(99); 
/* 4822 */       } else if (str != null && "EXPIRED".equals(str)) {
/* 4823 */         String[] arrayOfString = new String[1];
/* 4824 */         BigDecimal bigDecimal = getICXSessionInfo(str3, arrayOfString);
/* 4826 */         if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 4827 */           Utils.writeToLog(str1, "EXPIRED PATH.. sessionUsername = " + arrayOfString + "  sessionId : " + bigDecimal, paramWebAppsContext); 
/* 4830 */         str2 = arrayOfString[0];
/*      */       } 
/*      */     } 
/* 4833 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 4834 */       Utils.writeToLog(str1, "END: returnValue: " + str2, paramWebAppsContext, 2); 
/* 4835 */     return str2;
/*      */   }
/*      */   
/*      */   public static String getLoadedLangTable() {
/* 4844 */     Enumeration<String> enumeration = installedSpecificLangDescTable.keys();
/* 4845 */     StringBuffer stringBuffer = new StringBuffer(10000);
/* 4846 */     String str = null;
/* 4847 */     stringBuffer.append("<table border=6>\n");
/* 4848 */     stringBuffer.append("<caption><b>Specific Language Descriptions</b></caption>\n");
/* 4849 */     stringBuffer.append("<tr><td>Key</td><td>Description</td>></tr>\n");
/* 4850 */     while (enumeration.hasMoreElements()) {
/* 4851 */       str = enumeration.nextElement();
/* 4852 */       stringBuffer.append("<tr><td>" + str + "</td><td>" + (String)installedSpecificLangDescTable.get(str) + "</td>></tr>\n");
/*      */     } 
/* 4854 */     return stringBuffer.toString();
/*      */   }
/*      */   
/*      */   public static boolean isEnabled(String paramString) {
/* 4864 */     boolean bool = false;
/* 4865 */     String str = "oracle.apps.fnd.sso.SessionMgr.isEnabled(String)";
/* 4867 */     WebAppsContext webAppsContext = null;
/* 4868 */     boolean bool1 = false;
/* 4870 */     if (Utils.isAppsContextAvailable()) {
/* 4871 */       webAppsContext = Utils.getAppsContext();
/* 4872 */       bool1 = true;
/*      */     } else {
/* 4874 */       webAppsContext = Utils.getAppsContext();
/*      */     } 
/* 4876 */     if (((AppsLog)webAppsContext.getLog()).isEnabled(2)) {
/* 4878 */       Utils.writeToLog(str, "BEGIN", webAppsContext, 2);
/* 4879 */       Utils.writeToLog(str, "Paramlist username: " + paramString, webAppsContext, 2);
/*      */     } 
/*      */     try {
/* 4883 */       bool = isEnabled(paramString, webAppsContext);
/*      */     } finally {
/* 4886 */       if (((AppsLog)webAppsContext.getLog()).isEnabled(2))
/* 4887 */         Utils.writeToLog(str, "END: returnValue: " + bool, webAppsContext, 2); 
/* 4888 */       if (!bool1)
/* 4889 */         Utils.releaseAppsContext(); 
/*      */     } 
/* 4892 */     return bool;
/*      */   }
/*      */   
/*      */   public static boolean isEnabled(String paramString, WebAppsContext paramWebAppsContext) {
/* 4898 */     boolean bool = false;
/* 4899 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.isEnabled(String, WebAppsContext)";
/* 4901 */     String str2 = "SELECT ENCRYPTED_FOUNDATION_PASSWORD,ENCRYPTED_USER_PASSWORD,USER_ID   FROM FND_USER WHERE USER_NAME=:1    AND (START_DATE <= SYSDATE)    AND (END_DATE IS NULL OR END_DATE > SYSDATE)";
/* 4905 */     OraclePreparedStatement oraclePreparedStatement = null;
/* 4906 */     ResultSet resultSet = null;
/* 4907 */     Connection connection = null;
/* 4909 */     if (paramWebAppsContext == null)
/* 4910 */       throw new RuntimeException("Web Apps Context is null"); 
/* 4913 */     if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2)) {
/* 4914 */       Utils.writeToLog(str1, "BEGIN", paramWebAppsContext, 2);
/* 4915 */       Utils.writeToLog(str1, "Paramlist username: " + paramString, paramWebAppsContext, 2);
/*      */     } 
/*      */     try {
/* 4919 */       connection = Utils.getConnection(paramWebAppsContext);
/* 4920 */       oraclePreparedStatement = (OraclePreparedStatement)connection.prepareStatement(str2);
/* 4921 */       oraclePreparedStatement.defineColumnType(1, 12, 100);
/* 4922 */       oraclePreparedStatement.defineColumnType(2, 12, 100);
/* 4923 */       oraclePreparedStatement.defineColumnType(3, 12, 15);
/* 4924 */       oraclePreparedStatement.setString(1, paramString);
/* 4925 */       resultSet = oraclePreparedStatement.executeQuery();
/* 4926 */       if (resultSet.next())
/* 4927 */         bool = true; 
/* 4930 */     } catch (Exception exception) {
/* 4931 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 4932 */         Utils.writeToLog(str1, "Exception occurred: " + Utils.getExceptionStackTrace(exception), paramWebAppsContext, 4); 
/*      */     } finally {
/* 4936 */       if (oraclePreparedStatement != null)
/*      */         try {
/* 4938 */           oraclePreparedStatement.close();
/* 4939 */         } catch (Exception exception) {} 
/* 4941 */       if (resultSet != null)
/*      */         try {
/* 4943 */           resultSet.close();
/* 4944 */         } catch (Exception exception) {} 
/* 4946 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 4947 */         Utils.writeToLog(str1, "END: returnValue: " + bool, paramWebAppsContext, 2); 
/*      */     } 
/* 4949 */     return bool;
/*      */   }
/*      */   
/*      */   public static void forwardToHomePage(OAPageContext paramOAPageContext, WebAppsContext paramWebAppsContext) {
/* 4960 */     String str = "oracle.apps.fnd.sso.SessionMgr.forwardToHomePage(OAPageContext, WebAppsContext)";
/*      */     try {
/* 4964 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 4965 */         Utils.writeToLog(str, "BEGIN", paramWebAppsContext, 2); 
/* 4967 */       DataObject dataObject = paramOAPageContext.getNamedDataObject("_SessionParameters");
/* 4968 */       HttpServletRequest httpServletRequest = (HttpServletRequest)dataObject.selectValue(null, "HttpServletRequest");
/* 4970 */       HttpServletResponse httpServletResponse = (HttpServletResponse)dataObject.selectValue(null, "HttpServletResponse");
/* 4973 */       String str1 = getSessionCookieName(paramWebAppsContext);
/* 4974 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 4975 */         Utils.writeToLog(str, "ssoCookieName: " + str1, paramWebAppsContext); 
/* 4978 */       if (str1 == null)
/* 4979 */         throw new RuntimeException(getMessage(paramWebAppsContext, "FND-9914")); 
/* 4982 */       String str2 = paramWebAppsContext.getSessionCookieValue();
/* 4983 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 4984 */         Utils.writeToLog(str, "From proxy ssoCookieValue: " + str2, paramWebAppsContext); 
/* 4986 */       String str3 = paramWebAppsContext.getEnvStore().getEnv("ICX_PV_SESSION_MODE");
/* 4987 */       boolean bool1 = (paramWebAppsContext.isProxySession() != null) ? true : false;
/* 4988 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 4989 */         Utils.writeToLog(str, "Is proxy: " + bool1, paramWebAppsContext); 
/* 4991 */       if (!bool1)
/* 4992 */         if ((str3 != null && str3.equals("115P")) || !Utils.isSSOMode()) {
/* 4995 */           if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 4996 */             Utils.writeToLog(str, "Local session : " + str2, paramWebAppsContext); 
/* 4997 */           if (str2 == null)
/* 4998 */             throw new RuntimeException(getMessage(paramWebAppsContext, "FND-9914")); 
/*      */         } else {
/* 5003 */           if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 5004 */             Utils.writeToLog(str, "SSO Enabled hence not using session", paramWebAppsContext); 
/* 5005 */           if (str2 != null && !str2.equals("") && !str2.equals("-1")) {
/* 5008 */             if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 5009 */               Utils.writeToLog(str, "Session not INVALID", paramWebAppsContext); 
/* 5010 */             boolean bool = paramWebAppsContext.disableUserSession(str2);
/* 5011 */             if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 5012 */               Utils.writeToLog(str, "Session disabled:: " + bool, paramWebAppsContext); 
/*      */           } 
/* 5015 */           str2 = "-1";
/*      */         }  
/* 5018 */       Cookie cookie = new Cookie(str1, str2);
/* 5019 */       String str4 = null;
/* 5022 */       str4 = getServerDomain(httpServletRequest, httpServletResponse);
/* 5023 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 5024 */         Utils.writeToLog(str, "ssoCookieDomain from sso: " + str4, paramWebAppsContext); 
/* 5028 */       if (str4 != null && !"NONE".equals(str4))
/* 5029 */         cookie.setDomain(str4); 
/* 5036 */       boolean bool2 = false;
/* 5038 */       if (bool2) {
/* 5039 */         cookie.setPath("/; HTTPOnly");
/*      */       } else {
/* 5042 */         cookie.setPath("/");
/*      */       } 
/* 5046 */       if (isSSLMode(httpServletRequest))
/* 5047 */         cookie.setSecure(true); 
/* 5050 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 5051 */         Utils.writeToLog(str, "Adding Cookie to resp object", paramWebAppsContext); 
/* 5052 */       httpServletResponse.addCookie(cookie);
/* 5053 */       String str5 = SSOManager.getHomePageURL(paramWebAppsContext);
/* 5054 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(1))
/* 5055 */         Utils.writeToLog(str, "Homepage URl:: " + str5, paramWebAppsContext); 
/* 5056 */       paramOAPageContext.sendRedirect(str5);
/* 5057 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 5058 */         Utils.writeToLog(str, "END", paramWebAppsContext, 2); 
/* 5059 */     } catch (Exception exception) {
/* 5061 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 5062 */         Utils.writeToLog(str, "Exception Occurred:: " + Utils.getExceptionStackTrace(exception), paramWebAppsContext, 4); 
/*      */     } finally {}
/*      */   }
/*      */   
/*      */   public static String findGuid(String paramString, WebAppsContext paramWebAppsContext) {
/* 5078 */     OracleCallableStatement oracleCallableStatement = null;
/* 5079 */     String str = "oracle.apps.fnd.sso.SessionMgr.findGuid";
/* 5080 */     AppsLog appsLog = null;
/*      */     try {
/* 5082 */       appsLog = (AppsLog)paramWebAppsContext.getLog();
/* 5083 */       Connection connection = paramWebAppsContext.getJDBCConnection();
/* 5084 */       if (appsLog.isEnabled(2))
/* 5085 */         appsLog.write(str, " BEGIN : ssoUserName=" + paramString, 2); 
/* 5087 */       String str1 = "BEGIN :1 := FND_LDAP_USER.GET_USER_GUID_AND_COUNT(:2,:3); END; ";
/* 5088 */       if (appsLog.isEnabled(1))
/* 5089 */         appsLog.write(str, " Will execute:" + str1, 1); 
/* 5091 */       oracleCallableStatement = (OracleCallableStatement)connection.prepareCall(str1);
/* 5092 */       oracleCallableStatement.setString(2, paramString);
/* 5093 */       oracleCallableStatement.registerOutParameter(1, 12, 0, 100);
/* 5094 */       oracleCallableStatement.registerOutParameter(3, 2);
/* 5095 */       oracleCallableStatement.execute();
/* 5096 */       int i = oracleCallableStatement.getInt(3);
/* 5097 */       String str2 = oracleCallableStatement.getString(1);
/* 5098 */       oracleCallableStatement.close();
/* 5099 */       oracleCallableStatement = null;
/* 5100 */       if (appsLog.isEnabled(1))
/* 5101 */         appsLog.write(str, " PL returned guid:" + str2 + " n:" + i, 1); 
/* 5103 */       if (i != 1) {
/* 5104 */         if (appsLog.isEnabled(1))
/* 5105 */           appsLog.write(str, " END : return null (wasn't exactly 1 found)", 1); 
/* 5107 */         return null;
/*      */       } 
/* 5110 */       if (appsLog.isEnabled(1))
/* 5111 */         appsLog.write(str, " END : return GUID= " + str2, 1); 
/* 5113 */       return str2;
/* 5115 */     } catch (Exception exception) {
/* 5117 */       paramWebAppsContext.getErrorStack().addStackTrace(exception);
/* 5118 */       if (appsLog.isEnabled(6))
/* 5119 */         appsLog.write(str, exception, 6); 
/* 5121 */       if (appsLog.isEnabled(1))
/* 5122 */         appsLog.write(str, " END : Finish with errors, returning null", 1); 
/* 5124 */       return null;
/*      */     } finally {
/* 5128 */       if (oracleCallableStatement != null)
/*      */         try {
/* 5128 */           oracleCallableStatement.close();
/* 5128 */         } catch (Exception exception) {} 
/*      */     } 
/*      */   }
/*      */   
/* 5136 */   static String autoLinkEnabled = null;
/*      */   
/*      */   public static String createPortalSession(String paramString1, String paramString2, WebAppsContext paramWebAppsContext) {
/* 5157 */     String str1 = "oracle.apps.fnd.sso.SessionMgr.createPortalSession";
/* 5158 */     AppsLog appsLog = null;
/* 5159 */     WebAppsContext webAppsContext = Utils.swapContext(paramWebAppsContext);
/* 5162 */     paramString1 = paramString1.toUpperCase();
/* 5163 */     String str2 = null;
/*      */     try {
/* 5166 */       if (paramWebAppsContext == null)
/* 5168 */         throw new RuntimeException("Internal error: got null WebAppsContext at " + str1); 
/* 5170 */       if (paramString2 == null)
/* 5172 */         throw new RuntimeException("Internal error: got null ssoUserguid at " + str1); 
/* 5174 */       appsLog = (AppsLog)paramWebAppsContext.getLog();
/* 5176 */       Connection connection = paramWebAppsContext.getJDBCConnection();
/* 5177 */       String str3 = Utils.getAppsUser(paramString2, connection);
/* 5179 */       if (str3 != null) {
/* 5180 */         if (appsLog.isEnabled(1))
/* 5181 */           appsLog.write(str1, " apps Username found=" + str3, 1); 
/*      */       } else {
/* 5185 */         if (appsLog.isEnabled(1))
/* 5186 */           appsLog.write(str1, " no linked Apps User found", 1); 
/* 5188 */         SSOAppsUser sSOAppsUser = new SSOAppsUser(paramString1, paramString2);
/* 5190 */         Profiles profiles = new Profiles((AppsContext)paramWebAppsContext);
/* 5191 */         autoLinkEnabled = profiles.getSiteLevelProfile("APPS_SSO_AUTO_LINK_USER");
/* 5192 */         if (autoLinkEnabled == null || "".equals(autoLinkEnabled))
/* 5193 */           autoLinkEnabled = "N"; 
/* 5196 */         if ("Y".equals(autoLinkEnabled) && SSOCommon.hasLinkableAppsUser(paramString1)) {
/* 5197 */           str3 = paramString1;
/* 5198 */           if (appsLog.isEnabled(1)) {
/* 5199 */             appsLog.write(str1, " user with same name exists ", 1);
/* 5200 */             appsLog.write(str1, "APPS_SSO_LOCAL_LOGIN=" + SSOCommon.getUserLevelLoginMode(str3), 1);
/*      */           } 
/* 5204 */           sSOAppsUser.updateUserGuid(str3, SSOCommon.getUserLevelLoginMode(str3));
/* 5205 */           connection.commit();
/* 5206 */           if (appsLog.isEnabled(1))
/* 5207 */             appsLog.write(str1, "AUTOLINK: GUID updated and commited ", 1); 
/* 5209 */         } else if ("CREATE_LINK".equals(autoLinkEnabled)) {
/* 5210 */           str3 = paramString1;
/* 5211 */           if (appsLog.isEnabled(1))
/* 5212 */             appsLog.write(str1, " CREATE_LINK: Create and Link " + str3 + " with guid=" + paramString2, 1); 
/* 5214 */           SSOUtil.createUserOnDemand(str3, paramString2, connection);
/* 5215 */           connection.commit();
/* 5216 */           if (appsLog.isEnabled(1))
/* 5217 */             appsLog.write(str1, " CREATE_LINK: Created and commited", 1); 
/*      */         } else {
/* 5223 */           ErrorStack errorStack = paramWebAppsContext.getErrorStack();
/* 5224 */           errorStack.clear();
/* 5225 */           errorStack.addMessage("FND", "FND_SSO_ACCOUNT_NOT_SETUP");
/* 5226 */           if (appsLog.isEnabled(4)) {
/* 5227 */             appsLog.write(str1, "End with FND_SSO_ACCOUNT_NOT_SETUP ", 4);
/* 5228 */             return null;
/*      */           } 
/*      */         } 
/*      */       } 
/* 5232 */       if (appsLog.isEnabled(1))
/* 5233 */         appsLog.write(str1, " About to create a session for " + str3, 1); 
/* 5236 */       String str4 = paramWebAppsContext.getProfileStore().getSpecificProfile("ICX_LANGUAGE", str3, null, null);
/* 5240 */       if (appsLog.isEnabled(1)) {
/* 5241 */         appsLog.write(str1, " About to create a session for " + str3, 1);
/* 5242 */         appsLog.write(str1, "         langCode: " + str4, 1);
/* 5243 */         appsLog.write(str1, "         HOME_URL: " + SSOManager.getPostLogoutHomeUrl(paramWebAppsContext), 1);
/*      */       } 
/* 5246 */       str4 = paramWebAppsContext.getLangCode(str4.toUpperCase());
/* 5247 */       if (appsLog.isEnabled(1))
/* 5248 */         appsLog.write(str1, "langCode value for Lang : " + str4, 1); 
/* 5252 */       paramWebAppsContext.getEnvStore().setEnv("ICX_PV_SESSION_MODE", "115X");
/* 5255 */       boolean bool = paramWebAppsContext.createSession(str3, str4);
/* 5256 */       if (bool) {
/* 5257 */         if (appsLog.isEnabled(1))
/* 5258 */           appsLog.write(str1, " session created, who knows if commited", 1); 
/* 5261 */         str2 = SSOUtil.getPortalUrl();
/* 5262 */         paramWebAppsContext.setSessionAttribute("FND_HOME_PAGE_URL", str2);
/* 5263 */         String str = paramWebAppsContext.getSessionCookieValue();
/* 5264 */         if (appsLog.isEnabled(2))
/* 5265 */           appsLog.write(str1, " END : session encrypted id=" + str, 2); 
/* 5268 */         if (appsLog.isEnabled(1))
/* 5269 */           appsLog.write(str1, "Before storePostLogoutUrl [4924], user:" + paramWebAppsContext.getUserName() + ", resp:" + paramWebAppsContext.getRespId() + ", appId:" + paramWebAppsContext.getRespApplId(), 1); 
/* 5272 */         paramWebAppsContext.getProfileStore().clear();
/* 5273 */         SSOManager.storePostLogoutUrl(paramWebAppsContext, str, true, str4, str2);
/* 5274 */         return str;
/*      */       } 
/* 5278 */       if (appsLog.isEnabled(2))
/* 5279 */         appsLog.write(str1, " END : failed at WebAppContext, see stack for details.Returning NULL", 2); 
/* 5281 */       return null;
/* 5284 */     } catch (Exception exception) {
/* 5285 */       if (paramWebAppsContext != null) {
/* 5286 */         paramWebAppsContext.getErrorStack().addStackTrace(exception);
/* 5287 */         appsLog = (AppsLog)paramWebAppsContext.getLog();
/* 5288 */         if (appsLog.isEnabled(6))
/* 5289 */           appsLog.write(str1, exception, 6); 
/* 5291 */         return null;
/*      */       } 
/* 5294 */       throw (RuntimeException)exception;
/*      */     } finally {
/* 5298 */       Utils.swapContext(webAppsContext);
/*      */     } 
/*      */   }
/*      */   
/*      */   public static String createPortalSession(String paramString, WebAppsContext paramWebAppsContext) {
/* 5314 */     String str = "oracle.apps.fnd.sso.SessionMgr.createPortalSession(no giud)";
/* 5315 */     AppsLog appsLog = null;
/*      */     try {
/* 5318 */       if (paramWebAppsContext == null)
/* 5320 */         throw new RuntimeException("Internal error: got null WebAppsContext at " + str); 
/* 5323 */       ErrorStack errorStack = paramWebAppsContext.getErrorStack();
/* 5324 */       errorStack.clear();
/* 5325 */       appsLog = (AppsLog)paramWebAppsContext.getLog();
/* 5326 */       if (appsLog.isEnabled(2)) {
/* 5327 */         appsLog.write(str, "BEGIN", 2);
/* 5328 */         appsLog.write(str, " username=" + ((paramString != null) ? paramString : "**NULL**"), 2);
/* 5329 */         appsLog.write(str, " context=" + paramWebAppsContext.toString(), 2);
/*      */       } 
/* 5332 */       if (paramString == null) {
/* 5333 */         if (appsLog.isEnabled(1))
/* 5334 */           appsLog.write(str, " Cannot proceed with username=null", 1); 
/* 5337 */         errorStack.addMessage("FND", "FND_SSO_CL_USER_NULL");
/* 5338 */         if (appsLog.isEnabled(4)) {
/* 5339 */           appsLog.write(str, "End with FND_SSO_CL_USER_NULL ", 4);
/* 5340 */           return null;
/*      */         } 
/*      */       } 
/* 5344 */       String str1 = findGuid(paramString, paramWebAppsContext);
/* 5345 */       if (str1 != null)
/* 5346 */         return createPortalSession(paramString, str1, paramWebAppsContext); 
/* 5349 */       paramWebAppsContext.getErrorStack().addMessage("FND", "FND_SSO_USER_NOT_FOUND");
/* 5350 */       if (appsLog.isEnabled(4))
/* 5351 */         appsLog.write(str, "End with FND_SSO_USER_NOT_FOUND ", 4); 
/* 5353 */       return null;
/* 5355 */     } catch (Exception exception) {
/* 5356 */       if (paramWebAppsContext != null) {
/* 5357 */         paramWebAppsContext.getErrorStack().addStackTrace(exception);
/* 5358 */         appsLog = (AppsLog)paramWebAppsContext.getLog();
/* 5359 */         if (appsLog.isEnabled(6))
/* 5360 */           appsLog.write(str, exception, 6); 
/* 5362 */         return null;
/*      */       } 
/* 5365 */       throw (RuntimeException)exception;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static boolean allowLocalLogin(String paramString, WebAppsContext paramWebAppsContext) {
/* 5390 */     boolean bool = true;
/* 5391 */     String str = "oracle.apps.fnd.sso.SessionMgr.allowLocalLogin";
/* 5393 */     boolean bool1 = false;
/* 5394 */     boolean bool2 = false;
/* 5395 */     boolean bool3 = Utils.isSSOMode();
/* 5397 */     if (paramWebAppsContext == null)
/* 5398 */       throw new RuntimeException("WebAppsContext object is null"); 
/* 5400 */     AppsLog appsLog = (AppsLog)paramWebAppsContext.getLog();
/* 5401 */     bool1 = appsLog.isEnabled(str, 1);
/* 5402 */     bool2 = appsLog.isEnabled(str, 2);
/* 5404 */     if (bool2)
/* 5404 */       appsLog.write(str, "BEGIN, is sso enabled? " + bool3 + ", username:" + paramString, 2); 
/* 5406 */     if (bool3 && !"SYSADMIN".equalsIgnoreCase(paramString))
/* 5407 */       if (paramString != null && !paramString.equals("")) {
/* 5408 */         String str1 = Integer.toString(Utils.getUserId(paramString.toUpperCase()));
/* 5410 */         if (bool1)
/* 5410 */           Utils.writeToLog(str, "username=" + paramString + ", userid=" + str1, paramWebAppsContext, 1); 
/* 5412 */         String str2 = paramWebAppsContext.getProfileStore().getSpecificProfile("APPS_SSO_LOCAL_LOGIN", str1, "-1", "-1");
/* 5414 */         if (str2 != null && "SSO".equals(str2)) {
/* 5415 */           bool = false;
/* 5417 */           Message message = new Message("FND", "FND_SSO_NOT_AUTHENTICATED");
/* 5418 */           paramWebAppsContext.getErrorStack().addMessage(message);
/* 5420 */           if (bool1)
/* 5420 */             Utils.writeToLog(str, "username " + paramString + " should not use the LOCAL login page per the APPS_SSO_LOCAL_LOGIN profile value (" + str2 + ")", paramWebAppsContext, 1); 
/*      */         } 
/*      */       } else {
/* 5425 */         bool = false;
/*      */       }  
/* 5428 */     if (bool2)
/* 5428 */       appsLog.write(str, "END return:" + bool, 2); 
/* 5430 */     return bool;
/*      */   }
/*      */   
/*      */   public static void reauthFndSignOn(WebAppsContext paramWebAppsContext, int paramInt) {
/* 5441 */     String str = "oracle.apps.fnd.sso.SessionMgr.reauthFndSignOn(WebAppsContext, int, String)";
/* 5443 */     OracleCallableStatement oracleCallableStatement = null;
/* 5444 */     OracleConnection oracleConnection = null;
/*      */     try {
/* 5447 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 5449 */         Utils.writeToLog(str, "BEGIN", paramWebAppsContext, 2); 
/* 5452 */       oracleConnection = (OracleConnection)paramWebAppsContext.getJDBCConnection();
/* 5454 */       String str1 = "declare PRAGMA AUTONOMOUS_TRANSACTION;l_session_id number := :1;begin fnd_signon.audit_user_reauth(l_session_id); commit;exception when others then rollback;end;";
/* 5462 */       oracleCallableStatement = (OracleCallableStatement)oracleConnection.prepareCall(str1);
/* 5463 */       oracleCallableStatement.setBigDecimal(1, new BigDecimal(paramInt));
/* 5464 */       oracleCallableStatement.execute();
/* 5466 */     } catch (Exception exception) {
/* 5467 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(4))
/* 5468 */         Utils.writeToLog(str, "Exception Occurred : " + Utils.getExceptionStackTrace(exception), paramWebAppsContext, 4); 
/* 5472 */       throw new RuntimeException(Utils.getExceptionStackTrace(exception));
/*      */     } finally {
/* 5475 */       if (((AppsLog)paramWebAppsContext.getLog()).isEnabled(2))
/* 5476 */         Utils.writeToLog(str, "END", paramWebAppsContext, 2); 
/* 5478 */       if (oracleCallableStatement != null)
/*      */         try {
/* 5480 */           oracleCallableStatement.close();
/* 5481 */         } catch (Exception exception) {} 
/*      */     } 
/*      */   }
/*      */ }


/* Location:              C:\Users\rpathania\Desktop\Monthly Work\OFA\12.2_Migration\De4\Sess\!\SessionMgr.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */