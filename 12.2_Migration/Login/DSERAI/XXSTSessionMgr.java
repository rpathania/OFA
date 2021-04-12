/*      */ import java.sql.Connection;
/*      */ import java.sql.SQLException;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.Calendar;
/*      */ import java.util.Date;
/*      */ import java.util.GregorianCalendar;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.StringTokenizer;
/*      */ import java.util.regex.Pattern;
/*      */ import javax.naming.NamingEnumeration;
/*      */ import javax.naming.directory.Attribute;
/*      */ import javax.naming.directory.Attributes;
/*      */ import javax.naming.directory.InitialDirContext;
/*      */ import javax.naming.directory.SearchControls;
/*      */ import javax.naming.directory.SearchResult;
/*      */ import javax.servlet.http.HttpServletRequest;
/*      */ import javax.servlet.http.HttpServletResponse;
/*      */ import oracle.apps.fnd.common.AppsLog;
/*      */ import oracle.apps.fnd.common.Message;
/*      */ import oracle.apps.fnd.common.WebAppsContext;
/*      */ import oracle.apps.fnd.security.SessionManager;
/*      */ import oracle.apps.fnd.security.UserPwd;
/*      */ import oracle.apps.fnd.sso.SSOUtil;
/*      */ import oracle.apps.fnd.sso.SessionMgr;
/*      */ import oracle.apps.fnd.sso.Utils;
/*      */ import oracle.apps.fnd.sso.XXSTSessionMgr;
/*      */ import oracle.apps.fnd.util.URLEncoder;
/*      */ import oracle.jdbc.OraclePreparedStatement;
/*      */ import oracle.jdbc.OracleResultSet;
/*      */ 
/*      */ public class XXSTSessionMgr extends SessionMgr {
/*      */   private static boolean isLocalLogged;
/*      */   
/*   85 */   private static String msgExpired = "XXST_LOGIN_EXPIRED";
/*      */   
/*      */   public static String createAppsSession(UserPwd paramUserPwd, HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse) {
/*  102 */     isLocalLogged = false;
/*  104 */     boolean bool1 = Utils.isAppsContextAvailable();
/*  105 */     WebAppsContext webAppsContext = Utils.getAppsContext();
/*  106 */     Connection connection = null;
/*  108 */     String str1 = "XXSTSessionMgr.createAppsSession(UserPwd, HttpServletRequest, HttpServletResponse)";
/*  110 */     String str2 = "";
/*  111 */     AppsLog appsLog = Utils.getLog(webAppsContext);
/*  112 */     boolean bool2 = appsLog.isEnabled(str1, 2);
/*  113 */     boolean bool3 = appsLog.isEnabled(str1, 1);
/*  114 */     boolean bool4 = appsLog.isEnabled(str1, 6);
/*      */     try {
/*  118 */       webAppsContext.getErrorStack().clear();
/*  119 */       if (bool2) {
/*  120 */         appsLog.write(str1, "BEGIN", 2);
/*  121 */         appsLog.write(str1, "Paramlist: request: " + paramHttpServletRequest + " response: " + paramHttpServletResponse, 2);
/*      */       } 
/*  125 */       Utils.setRequestCharacterEncoding(paramHttpServletRequest);
/*  126 */       if (bool3)
/*  127 */         appsLog.write(str1, "After setting Character encoding", 1); 
/*  129 */       String str3 = paramHttpServletRequest.getParameter("requestUrl");
/*  130 */       if (bool3)
/*  131 */         appsLog.write(str1, "Got requestUrl: " + str3, 1); 
/*  133 */       if (str3 == null || str3.equals("")) {
/*  134 */         str3 = "APPSHOMEPAGE";
/*  135 */         if (bool3)
/*  136 */           appsLog.write(str1, "Defaulting to requestUrl: " + str3, 1); 
/*      */       } 
/*  141 */       String str4 = paramHttpServletRequest.getParameter("cancelUrl");
/*  142 */       if (bool3)
/*  143 */         appsLog.write(str1, "Got cancelUrl: " + str4, 1); 
/*  146 */       if (str4 == null || str4.equals("")) {
/*  147 */         if (bool3)
/*  148 */           appsLog.write(str1, "cancelUrl not set", 1); 
/*  149 */         str4 = SSOUtil.getLocalLoginUrl();
/*  150 */         if (bool3)
/*  151 */           appsLog.write(str1, "after substring " + str4, 1); 
/*      */       } 
/*  154 */       String str5 = paramHttpServletRequest.getParameter("home_url");
/*  155 */       if (bool3)
/*  156 */         appsLog.write(str1, "Got home_url: " + str5, 1); 
/*  157 */       String str6 = paramHttpServletRequest.getParameter("langCode");
/*  158 */       if (bool3) {
/*  159 */         appsLog.write(str1, "Got langCode: " + str6, 1);
/*  160 */         appsLog.write(str1, "Got Url params requestUrl: " + str3 + " cancelUrl: " + str4 + " home_url: " + str5 + " langCode: " + str6, 1);
/*      */       } 
/*  167 */       if (xssViolation(paramHttpServletRequest, webAppsContext)) {
/*  168 */         if (bool3)
/*  169 */           appsLog.write(str1, "Url Parameter validation Failed!", 1); 
/*  172 */         String str = "requestUrl=" + URLEncoder.encode(str3, SessionMgr.getCharSet()) + "&cancelUrl=" + URLEncoder.encode(str4, SessionMgr.getCharSet()) + "&errCode=FND_SSO_PARAMVAL_SCAN_FAILED";
/*  178 */         if (str6 != null && !str6.equals(""))
/*  179 */           str = str + "&langCode=" + str6; 
/*  181 */         if (str5 != null && !str5.equals(""))
/*  182 */           str = str + "&home_url=" + URLEncoder.encode(str5, SessionMgr.getCharSet()); 
/*  186 */         str2 = SSOUtil.getLocalLoginUrl(str);
/*  187 */         if (bool3)
/*  188 */           appsLog.write(str1, "(XSS) returnUrl=" + str2, 1); 
/*      */       } else {
/*  191 */         if (bool3)
/*  192 */           appsLog.write(str1, "XSS check passed before calling validateLogin", 1); 
/*  196 */         String str = paramUserPwd.getUsername();
/*  197 */         SessionManager sessionManager = webAppsContext.getSessionManager();
/*  203 */         SessionManager.AuthStatusCode authStatusCode = validateLogin(webAppsContext, paramUserPwd);
/*  206 */         if (bool3)
/*  207 */           appsLog.write(str1, "After Calling SessionManager.validateLogin validationStatus: " + authStatusCode, 1); 
/*  212 */         if (authStatusCode == null || authStatusCode.equals(SessionManager.AuthStatusCode.INVALID)) {
/*  214 */           if (bool3)
/*  215 */             appsLog.write(str1, "validateLogin Status:NULL or INVALID", 1); 
/*  218 */           Message message = webAppsContext.getErrorStack().nextMessageObject();
/*  219 */           String str7 = null;
/*  220 */           String str8 = "";
/*  221 */           String str9 = "&errCode=";
/*  223 */           if (message != null) {
/*  224 */             str7 = message.getName();
/*  225 */             str8 = message.getMessageText(webAppsContext.getResourceStore());
/*  226 */             str9 = str9 + URLEncoder.encode(str7, SessionMgr.getCharSet());
/*      */           } 
/*  230 */           String str10 = "requestUrl=" + URLEncoder.encode(str3, SessionMgr.getCharSet()) + "&cancelUrl=" + URLEncoder.encode(str4, SessionMgr.getCharSet()) + str9;
/*  236 */           if (str6 != null && !str6.equals(""))
/*  237 */             str10 = str10 + "&langCode=" + str6; 
/*  239 */           if (str5 != null && !str5.equals(""))
/*  240 */             str10 = str10 + "&home_url=" + URLEncoder.encode(str5, SessionMgr.getCharSet()); 
/*  244 */           if (str != null && !"".equals(str))
/*  245 */             str10 = str10 + "&username=" + URLEncoder.encode(str, SessionMgr.getCharSet()); 
/*  248 */           if (bool3)
/*  249 */             appsLog.write(str1, " INVALID or NULL status returnUrl=" + str2, 1); 
/*  252 */           str2 = SSOUtil.getLocalLoginUrl(str10);
/*  253 */           if (bool3)
/*  254 */             appsLog.write(str1, "returnUrl ::" + str2, 1); 
/*  257 */         } else if (authStatusCode.equals(SessionManager.AuthStatusCode.VALID)) {
/*  258 */           if (bool3)
/*  259 */             appsLog.write(str1, "validateLogin Successful:VALID", 1); 
/*  261 */           connection = Utils.getConnection();
/*  262 */           createSession(str, webAppsContext, paramHttpServletRequest, paramHttpServletResponse, false, null, str6);
/*  266 */           String str7 = paramHttpServletRequest.getParameter("_lAccessibility");
/*  268 */           if (str7 != null && (str7.equals("Y") || str7.equals("S")))
/*  270 */             webAppsContext.getProfileStore().saveSpecificProfile("ICX_ACCESSIBILITY_FEATURES", str7, "USER", String.valueOf(Utils.getUserId(str)), null); 
/*  277 */           if (str3 == null || str3.equals("") || str3.equals("APPSHOMEPAGE")) {
/*  279 */             str3 = SSOUtil.getStartPageUrl(webAppsContext);
/*  280 */             if (bool3)
/*  281 */               appsLog.write(str1, " setting returnUrl=" + str3, 1); 
/*  284 */             if (str5 != null) {
/*  285 */               if (str3.indexOf("OracleMyPage.home") != -1) {
/*  287 */                 if (str3.indexOf("?") != -1) {
/*  288 */                   str3 = str3 + "&home_url=" + URLEncoder.encode(str5, SessionMgr.getCharSet());
/*      */                 } else {
/*  293 */                   str3 = str3 + "?home_url=" + URLEncoder.encode(str5, SessionMgr.getCharSet());
/*      */                 } 
/*  297 */                 if (bool3)
/*  298 */                   appsLog.write(str1, "added home_url", 1); 
/*      */               } 
/*  302 */             } else if (bool3) {
/*  303 */               appsLog.write(str1, "home_url=NULL", 1);
/*      */             } 
/*      */           } 
/*  307 */           str2 = str3;
/*  308 */         } else if (authStatusCode.equals(SessionManager.AuthStatusCode.EXPIRED)) {
/*  309 */           if (isLocalLogged) {
/*  310 */             if (bool3)
/*  311 */               appsLog.write(str1, "User password has expired:EXPIRED, creating Guest Session", 1); 
/*  314 */             createSession(Utils.getGuestUserName(), webAppsContext, paramHttpServletRequest, paramHttpServletResponse, false, null, str6);
/*  316 */             connection = Utils.getConnection(webAppsContext);
/*  317 */             webAppsContext.setSessionAttribute("$FND$USER$NAME$", str);
/*  320 */             String str7 = str3;
/*  322 */             String str8 = null;
/*  323 */             if (str6 != null && !"".equals(str6))
/*  324 */               str8 = "langCode=" + URLEncoder.encode(str6, SessionMgr.getCharSet()); 
/*  327 */             String str9 = SSOUtil.getLocalLoginUrl(str8);
/*  329 */             str8 = "returnUrl=" + URLEncoder.encode(str7, SessionMgr.getCharSet());
/*  332 */             str8 = str8 + "&cancelUrl=" + URLEncoder.encode(str9, SessionMgr.getCharSet());
/*  336 */             if (bool3)
/*  337 */               appsLog.write(str1, "[CHGPWD]requestUrl=" + str3, 1); 
/*  340 */             str2 = SSOUtil.getLocalPwdChangeUrl(str8);
/*      */           } else {
/*  342 */             str2 = webAppsContext.getProfileStore().getProfile("XXST_LOGIN_EXPIRED_URL");
/*      */           } 
/*      */         } 
/*      */       } 
/*  347 */       if (bool2)
/*  348 */         appsLog.write(str1, "END: returnUrl: " + str3, 2); 
/*  350 */     } catch (Throwable throwable) {
/*  351 */       if (bool4) {
/*  352 */         appsLog.write(str1, "Exception" + throwable.toString(), 6);
/*  353 */         appsLog.write(str1, throwable, 6);
/*      */       } 
/*  356 */       throw new RuntimeException(throwable.toString());
/*      */     } finally {
/*  358 */       if (!bool1)
/*  359 */         Utils.releaseAppsContext(); 
/*      */     } 
/*  361 */     return str2;
/*      */   }
/*      */   
/*      */   public static int getSteriaLoginMethod(WebAppsContext paramWebAppsContext) {
/*  370 */     String str = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_METHOD");
/*  371 */     if ("AD".equals(str))
/*  372 */       return 1; 
/*  373 */     if ("ADONE".equals(str))
/*  374 */       return 2; 
/*  375 */     if ("LDAPONE".equals(str))
/*  376 */       return 3; 
/*  378 */     return 4;
/*      */   }
/*      */   
/*      */   public static SessionManager.AuthStatusCode validateLogin(WebAppsContext paramWebAppsContext, UserPwd paramUserPwd) {
/*  385 */     if (paramUserPwd.getUsername() == null || "".equals(paramUserPwd.getUsername()) || paramUserPwd.getPassword() == null || "".equals(paramUserPwd.getPassword()))
/*  389 */       return SessionManager.AuthStatusCode.INVALID; 
/*  392 */     OracleResultSet oracleResultSet = null;
/*  393 */     OraclePreparedStatement oraclePreparedStatement = null;
/*      */     try {
/*  397 */       oraclePreparedStatement = (OraclePreparedStatement)paramWebAppsContext.getJDBCConnection().prepareStatement(" select 1 from fnd_user where user_name = :1  and trunc(sysdate) between trunc(nvl(START_DATE,SYSDATE)) and trunc(nvl(END_DATE,SYSDATE)) ");
/*  400 */       oraclePreparedStatement.setString(1, paramUserPwd.getUsername());
/*  402 */       oracleResultSet = (OracleResultSet)oraclePreparedStatement.executeQuery();
/*  403 */       if (!oracleResultSet.next()) {
/*  404 */         oracleResultSet.close();
/*  405 */         oraclePreparedStatement.close();
/*  406 */         paramWebAppsContext.getErrorStack().addMessage(new Message("XXST", "XXST_LOGIN_USER_NOT_FOUND"));
/*  408 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/*  410 */     } catch (SQLException sQLException) {
/*  411 */       paramWebAppsContext.getErrorStack().addMessage("FND", "Fatal exception : " + sQLException.getMessage());
/*  413 */       return SessionManager.AuthStatusCode.INVALID;
/*      */     } finally {
/*      */       try {
/*  416 */         oracleResultSet.close();
/*  417 */         oraclePreparedStatement.close();
/*  418 */       } catch (Exception exception) {}
/*      */     } 
/*      */     try {
/*  424 */       oraclePreparedStatement = (OraclePreparedStatement)paramWebAppsContext.getJDBCConnection().prepareStatement(" select lookup_code from fnd_lookup_values_vl  where lookup_type = 'XXST_LOGIN_NATIVE_USERS'  and lookup_code = :1  and trunc(sysdate) between trunc(nvl(START_DATE_ACTIVE,SYSDATE)) and trunc(nvl(END_DATE_ACTIVE,SYSDATE))  and enabled_flag = 'Y' ");
/*  430 */       oraclePreparedStatement.setString(1, paramUserPwd.getUsername());
/*  432 */       oracleResultSet = (OracleResultSet)oraclePreparedStatement.executeQuery();
/*  433 */       if (oracleResultSet.next()) {
/*  434 */         SessionManager sessionManager = paramWebAppsContext.getSessionManager();
/*  435 */         oracleResultSet.close();
/*  436 */         oraclePreparedStatement.close();
/*  438 */         isLocalLogged = true;
/*  439 */         return sessionManager.validateLogin(paramUserPwd);
/*      */       } 
/*  441 */     } catch (SQLException sQLException) {
/*  442 */       paramWebAppsContext.getErrorStack().addMessage("FND", "Fatal exception : " + sQLException.getMessage());
/*  444 */       return SessionManager.AuthStatusCode.INVALID;
/*      */     } finally {
/*      */       try {
/*  447 */         oracleResultSet.close();
/*  448 */         oraclePreparedStatement.close();
/*  449 */       } catch (Exception exception) {}
/*      */     } 
/*      */     try {
/*  455 */       oraclePreparedStatement = (OraclePreparedStatement)paramWebAppsContext.getJDBCConnection().prepareStatement(" select lookup_code from fnd_lookup_values_vl  where lookup_type = 'XXST_LOGIN_NAT_AND_EXT_USERS'  and lookup_code = :1  and trunc(sysdate) between trunc(nvl(START_DATE_ACTIVE,SYSDATE)) and trunc(nvl(END_DATE_ACTIVE,SYSDATE))  and enabled_flag = 'Y' ");
/*  461 */       oraclePreparedStatement.setString(1, paramUserPwd.getUsername());
/*  463 */       oracleResultSet = (OracleResultSet)oraclePreparedStatement.executeQuery();
/*  464 */       if (oracleResultSet.next()) {
/*  465 */         SessionManager sessionManager = paramWebAppsContext.getSessionManager();
/*  466 */         oracleResultSet.close();
/*  467 */         oraclePreparedStatement.close();
/*  469 */         isLocalLogged = true;
/*  470 */         SessionManager.AuthStatusCode authStatusCode = sessionManager.validateLogin(paramUserPwd);
/*  472 */         if (authStatusCode.equals(SessionManager.AuthStatusCode.VALID))
/*  473 */           return SessionManager.AuthStatusCode.VALID; 
/*  475 */         if (authStatusCode.equals(SessionManager.AuthStatusCode.EXPIRED))
/*  476 */           return SessionManager.AuthStatusCode.EXPIRED; 
/*      */       } 
/*  479 */     } catch (SQLException sQLException) {
/*  480 */       paramWebAppsContext.getErrorStack().addMessage("FND", "Fatal exception : " + sQLException.getMessage());
/*  482 */       return SessionManager.AuthStatusCode.INVALID;
/*      */     } finally {
/*      */       try {
/*  485 */         oracleResultSet.close();
/*  486 */         oraclePreparedStatement.close();
/*  487 */       } catch (Exception exception) {}
/*      */     } 
/*  491 */     isLocalLogged = false;
/*  492 */     int i = getSteriaLoginMethod(paramWebAppsContext);
/*  494 */     String str = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_EXPIRED_ATTRIBUTE");
/*  497 */     if (i == 4) {
/*  501 */       String str1 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_URL");
/*  503 */       String str2 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_PATH");
/*  505 */       if (str1 == null || "".equals(str1)) {
/*  506 */         paramWebAppsContext.getErrorStack().addMessage("FND", "Provider URL is null.");
/*  508 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/*  510 */       if (str2 == null || "".equals(str2)) {
/*  511 */         paramWebAppsContext.getErrorStack().addMessage("FND", "Provider path is null.");
/*  513 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/*  517 */       Hashtable<Object, Object> hashtable = new Hashtable<>();
/*  518 */       hashtable.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
/*  520 */       hashtable.put("java.naming.provider.url", str1);
/*  521 */       hashtable.put("java.naming.security.credentials", "" + paramUserPwd.getPassword() + "");
/*  523 */       hashtable.put("java.naming.security.authentication", "simple");
/*  524 */       hashtable.put("java.naming.security.principal", "uid=" + paramUserPwd.getUsername() + "," + str2);
/*      */       try {
/*  529 */         InitialDirContext initialDirContext = new InitialDirContext(hashtable);
/*      */         try {
/*  533 */           Attributes attributes = initialDirContext.getAttributes("uid=" + paramUserPwd.getUsername() + "," + str2);
/*  536 */           Object object = attributes.get(str).get();
/*  538 */           String str3 = object.toString();
/*  539 */           int j = Integer.parseInt(str3.substring(0, 4));
/*  541 */           int k = Integer.parseInt(str3.substring(4, 6)) - 1;
/*  543 */           int m = Integer.parseInt(str3.substring(6, 8));
/*  548 */           SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
/*  549 */           Date date = new Date();
/*  550 */           String str4 = simpleDateFormat.format(date);
/*  551 */           int n = Integer.parseInt(str4.substring(0, 4));
/*  553 */           int i1 = Integer.parseInt(str4.substring(4, 6)) - 1;
/*  555 */           int i2 = Integer.parseInt(str4.substring(6, 8));
/*  558 */           GregorianCalendar gregorianCalendar1 = new GregorianCalendar();
/*  559 */           GregorianCalendar gregorianCalendar2 = new GregorianCalendar();
/*  561 */           gregorianCalendar1.set(n, i1, i2);
/*  562 */           gregorianCalendar2.set(j, k, m);
/*  563 */           gregorianCalendar2.add(5, 90);
/*  568 */           if (gregorianCalendar1.after(gregorianCalendar2))
/*  569 */             return SessionManager.AuthStatusCode.EXPIRED; 
/*  571 */         } catch (Exception exception) {}
/*  574 */         initialDirContext.close();
/*  575 */       } catch (Exception exception) {
/*  576 */         paramWebAppsContext.getErrorStack().addMessage(new Message("FND", "FND_APPL_LOGIN_FAILED"));
/*  578 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/*  581 */       return SessionManager.AuthStatusCode.VALID;
/*      */     } 
/*  582 */     if (i == 1) {
/*  586 */       String str1 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_URL");
/*  588 */       String str2 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_DOMAIN");
/*  590 */       String str3 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_DOMAINS_LIST");
/*  592 */       if (str1 == null || "".equals(str1)) {
/*  593 */         paramWebAppsContext.getErrorStack().addMessage("FND", "Provider URL is null.");
/*  595 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/*  597 */       if (str2 == null || "".equals(str2)) {
/*  598 */         paramWebAppsContext.getErrorStack().addMessage("FND", "Provider domain is null");
/*  600 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/*  602 */       if (str3 == null || "".equals(str3)) {
/*  603 */         paramWebAppsContext.getErrorStack().addMessage("FND", "Domains list is null.");
/*  605 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/*  609 */       Hashtable<Object, Object> hashtable = new Hashtable<>();
/*  610 */       hashtable.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
/*  612 */       hashtable.put("java.naming.provider.url", str1);
/*  613 */       hashtable.put("java.naming.security.credentials", "" + paramUserPwd.getPassword() + "");
/*  616 */       StringTokenizer stringTokenizer = new StringTokenizer(str3, "|");
/*  619 */       boolean bool = false;
/*  620 */       String str4 = null;
/*  621 */       while (stringTokenizer.hasMoreTokens()) {
/*  622 */         str4 = stringTokenizer.nextToken();
/*      */         try {
/*  624 */           hashtable.put("java.naming.security.principal", paramUserPwd.getUsername() + "@" + str4 + "." + str2);
/*  628 */           InitialDirContext initialDirContext = new InitialDirContext(hashtable);
/*  629 */           bool = true;
/*      */           try {
/*  633 */             String str5 = "(&(objectClass=user)(sAMAccountName=" + paramUserPwd.getUsername() + "))";
/*  636 */             String[] arrayOfString = { str };
/*  638 */             SearchControls searchControls = new SearchControls();
/*  639 */             searchControls.setSearchScope(2);
/*  640 */             searchControls.setReturningAttributes(arrayOfString);
/*  642 */             NamingEnumeration<SearchResult> namingEnumeration = initialDirContext.search("", str5, searchControls);
/*  643 */             if (namingEnumeration.hasMoreElements()) {
/*  644 */               SearchResult searchResult = namingEnumeration.next();
/*  645 */               Attributes attributes = searchResult.getAttributes();
/*  646 */               String str6 = null;
/*  647 */               if (attributes != null) {
/*  648 */                 str6 = (String)attributes.get(str).get();
/*  651 */                 SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
/*  653 */                 Date date1 = simpleDateFormat.parse(str6.substring(0, 8));
/*  655 */                 Date date2 = Calendar.getInstance().getTime();
/*  657 */                 long l = date2.getTime() - date1.getTime();
/*  659 */                 l /= 1000L;
/*  660 */                 l /= 3600L;
/*  661 */                 l /= 24L;
/*  665 */                 if (l > 90L)
/*  666 */                   return SessionManager.AuthStatusCode.EXPIRED; 
/*      */               } 
/*      */             } 
/*  670 */           } catch (Exception exception) {}
/*  673 */           initialDirContext.close();
/*      */           break;
/*  676 */         } catch (Exception exception) {}
/*      */       } 
/*  681 */       if (!bool) {
/*  682 */         paramWebAppsContext.getErrorStack().addMessage(new Message("FND", "FND_APPL_LOGIN_FAILED"));
/*  684 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/*  686 */       return SessionManager.AuthStatusCode.VALID;
/*      */     } 
/*  688 */     if (i == 2) {
/*  692 */       String str1 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_URL");
/*  695 */       String str2 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_DOMAIN");
/*  697 */       if (str1 == null || "".equals(str1)) {
/*  698 */         paramWebAppsContext.getErrorStack().addMessage("FND", "XXST_LOGIN_PROVIDER_URL is null");
/*  700 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/*  709 */       if (str2 == null || "".equals(str2)) {
/*  710 */         paramWebAppsContext.getErrorStack().addMessage("FND", "Profile XXST_LOGIN_PROVIDER_DOMAIN not provided");
/*  712 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/*  715 */       AppsLog appsLog = Utils.getLog(paramWebAppsContext);
/*  718 */       Hashtable<Object, Object> hashtable = new Hashtable<>();
/*  719 */       hashtable.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
/*  721 */       hashtable.put("java.naming.provider.url", str1);
/*  722 */       hashtable.put("java.naming.security.credentials", "" + paramUserPwd.getPassword() + "");
/*  724 */       hashtable.put("java.naming.security.principal", "" + paramUserPwd.getUsername() + "@" + str2 + "");
/*  726 */       appsLog.write("validateLogin", "Principal " + paramUserPwd.getUsername() + "@" + str2, 6);
/*  729 */       appsLog.write("validateLogin", "env created", 6);
/*      */       try {
/*  731 */         String str3 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_KEYSTORE_PATH");
/*  733 */         String str4 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_KESTORE_PASSWORD");
/*  735 */         if (str3 != null && !"".equals(str3)) {
/*  737 */           System.setProperty("javax.net.ssl.trustStore", str3);
/*  739 */           System.setProperty("javax.net.ssl.trustStorePassword", str4);
/*  741 */           appsLog.write("validateLogin", "KeyStore Path Set ", 6);
/*  743 */           if (str1.substring(0, 5).toUpperCase().equals("LDAPS"))
/*  745 */             hashtable.put("java.naming.security.protocol", "ssl"); 
/*  748 */         } else if (str1.substring(0, 5).toUpperCase().equals("LDAPS")) {
/*  750 */           paramWebAppsContext.getErrorStack().addMessage("FND", "KeyStore EMPTY");
/*  752 */           return SessionManager.AuthStatusCode.INVALID;
/*      */         } 
/*  756 */       } catch (Exception exception) {
/*  757 */         appsLog.write("validateLogin", "Excp: " + exception.toString(), 6);
/*  759 */         paramWebAppsContext.getErrorStack().addMessage("FND", "Exception Setting KeyStore");
/*  761 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/*      */       try {
/*  765 */         appsLog.write("validateLogin", "Try Connection --> " + paramUserPwd.getUsername() + "@" + str2, 6);
/*  768 */         InitialDirContext initialDirContext = new InitialDirContext(hashtable);
/*  769 */         initialDirContext.close();
/*  770 */       } catch (Exception exception) {
/*  772 */         HashMap<Integer, String> hashMap = ErrorConnexion(exception);
/*  775 */         appsLog.write("validateLogin", "Excp" + exception.toString(), 6);
/*  777 */         paramWebAppsContext.getErrorStack().addMessage(hashMap.get(Integer.valueOf(1)), hashMap.get(Integer.valueOf(2)));
/*  782 */         if (((String)hashMap.get(Integer.valueOf(2))).equals(msgExpired)) {
/*  784 */           paramWebAppsContext.getErrorStack().addMessage("FND", "Expired ..... ");
/*  785 */           return SessionManager.AuthStatusCode.EXPIRED;
/*      */         } 
/*  787 */         paramWebAppsContext.getErrorStack().addMessage("FND", "Excp" + exception.toString());
/*  788 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/*  791 */       return SessionManager.AuthStatusCode.VALID;
/*      */     } 
/*  792 */     if (i == 3) {
/*  796 */       String str1 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_URL");
/*  798 */       String str2 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_PATH");
/*  800 */       String str3 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_DOMAIN");
/*  802 */       String str4 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_PROV_ADMIN");
/*  804 */       String str5 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_PROV_ADM_PWD");
/*  807 */       if (str1 == null || "".equals(str1)) {
/*  808 */         paramWebAppsContext.getErrorStack().addMessage("FND", "XXST_LOGIN_PROVIDER_URL is null");
/*  810 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/*  812 */       if (str2 == null || "".equals(str2)) {
/*  813 */         paramWebAppsContext.getErrorStack().addMessage("FND", "XXST_LOGIN_PROVIDER_PATH is null");
/*  815 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/*  817 */       if (str3 == null || "".equals(str3)) {
/*  818 */         paramWebAppsContext.getErrorStack().addMessage("FND", "XXST_LOGIN_PROVIDER_DOMAIN is null");
/*  820 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/*  822 */       if (str4 == null || "".equals(str4)) {
/*  823 */         paramWebAppsContext.getErrorStack().addMessage("FND", "XXST_LOGIN_PROV_ADMIN is null");
/*  825 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/*  827 */       if (str5 == null || "".equals(str5)) {
/*  828 */         paramWebAppsContext.getErrorStack().addMessage("FND", "XXST_LOGIN_PROV_ADM_PWD is null");
/*  830 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/*  835 */       Hashtable<Object, Object> hashtable = new Hashtable<>();
/*  836 */       hashtable.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
/*  838 */       hashtable.put("java.naming.provider.url", str1);
/*  839 */       hashtable.put("java.naming.security.credentials", str5);
/*  840 */       hashtable.put("java.naming.security.principal", str4);
/*  841 */       String str6 = null;
/*      */       try {
/*  843 */         String str7 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_KEYSTORE_PATH");
/*  845 */         String str8 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_KESTORE_PASSWORD");
/*  847 */         if (str7 != null && !"".equals(str7)) {
/*  848 */           System.setProperty("javax.net.ssl.trustStore", str7);
/*  850 */           System.setProperty("javax.net.ssl.trustStorePassword", str8);
/*  853 */           System.setProperty("javax.net.ssl.keyStore", str7);
/*  855 */           System.setProperty("javax.net.ssl.keyStorePassword", str8);
/*  857 */           if (str1.substring(0, 5).toUpperCase().equals("LDAPS"))
/*  859 */             hashtable.put("java.naming.security.protocol", "ssl"); 
/*      */         } 
/*  863 */       } catch (Exception exception) {}
/*      */       try {
/*      */         try {
/*  871 */           InitialDirContext initialDirContext = new InitialDirContext(hashtable);
/*      */           try {
/*  875 */             String str9 = "(&(objectClass=user)(sAMAccountName=" + paramUserPwd.getUsername() + "))";
/*  879 */             String[] arrayOfString = { "userPrincipalName" };
/*  881 */             SearchControls searchControls = new SearchControls();
/*  882 */             searchControls.setSearchScope(2);
/*  883 */             searchControls.setReturningAttributes(arrayOfString);
/*  885 */             NamingEnumeration<SearchResult> namingEnumeration = initialDirContext.search("ou=ofa,dc=one,dc=steria,dc=dom", str9, searchControls);
/*  890 */             if (namingEnumeration.hasMoreElements()) {
/*  891 */               SearchResult searchResult = namingEnumeration.next();
/*  892 */               Attributes attributes = searchResult.getAttributes();
/*  894 */               if (attributes != null)
/*  895 */                 str6 = (String)attributes.get("userPrincipalName").get(); 
/*      */             } 
/*  900 */           } catch (Exception exception) {}
/*  903 */           initialDirContext.close();
/*  904 */         } catch (Exception exception) {
/*  905 */           paramWebAppsContext.getErrorStack().addMessage("FND", "FND_APPL_LOGIN_FAILED");
/*  907 */           return SessionManager.AuthStatusCode.INVALID;
/*      */         } 
/*  910 */         if (str6 == null || str6 == "") {
/*  911 */           paramWebAppsContext.getErrorStack().addMessage("FND", "FND_APPL_LOGIN_FAILED");
/*  913 */           return SessionManager.AuthStatusCode.INVALID;
/*      */         } 
/*  918 */         Hashtable<Object, Object> hashtable1 = new Hashtable<>();
/*  919 */         hashtable1.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
/*  921 */         hashtable1.put("java.naming.provider.url", str1);
/*  922 */         hashtable1.put("java.naming.security.credentials", paramUserPwd.getPassword());
/*  923 */         hashtable1.put("java.naming.security.principal", str6);
/*      */         try {
/*  925 */           String str9 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_KEYSTORE_PATH");
/*  927 */           String str10 = paramWebAppsContext.getProfileStore().getProfile("XXST_LOGIN_KESTORE_PASSWORD");
/*  929 */           if (str9 != null && !"".equals(str9)) {
/*  931 */             System.setProperty("javax.net.ssl.trustStore", str9);
/*  933 */             System.setProperty("javax.net.ssl.trustStorePassword", str10);
/*  935 */             System.setProperty("javax.net.ssl.keyStore", str9);
/*  937 */             System.setProperty("javax.net.ssl.keyStorePassword", str10);
/*  940 */             hashtable1.put("java.naming.security.protocol", "ssl");
/*      */           } 
/*  943 */         } catch (Exception exception) {}
/*  945 */         String str7 = "";
/*  946 */         String str8 = "";
/*      */         try {
/*  950 */           InitialDirContext initialDirContext = new InitialDirContext(hashtable1);
/*      */           try {
/*  954 */             String str9 = "(&(objectClass=user)(sAMAccountName=" + paramUserPwd.getUsername() + "))";
/*  958 */             String[] arrayOfString = { str };
/*  960 */             SearchControls searchControls = new SearchControls();
/*  961 */             searchControls.setSearchScope(2);
/*  962 */             searchControls.setReturningAttributes(arrayOfString);
/*  963 */             NamingEnumeration<SearchResult> namingEnumeration = initialDirContext.search("ou=ofa,dc=one,dc=steria,dc=dom", str9, searchControls);
/*  968 */             if (namingEnumeration.hasMoreElements()) {
/*  969 */               SearchResult searchResult = namingEnumeration.next();
/*  970 */               Attributes attributes = searchResult.getAttributes();
/*  972 */               if (attributes != null) {
/*  973 */                 Attribute attribute = attributes.get("whenChanged");
/*  975 */                 if (attribute != null) {
/*  976 */                   Object object = attribute.get();
/*  977 */                   if (object instanceof String) {
/*  978 */                     System.out.println(object);
/*  979 */                     str7 = object.toString();
/*      */                   } else {
/*  981 */                     byte[] arrayOfByte = (byte[])object;
/*  982 */                     for (byte b = 0; b < arrayOfByte.length; b++)
/*  983 */                       str7 = str7 + Integer.toHexString(arrayOfByte[b]); 
/*      */                   } 
/*      */                 } 
/*      */               } 
/*      */             } 
/*  992 */             if (str7 != "") {
/*  993 */               str8 = convert(str7);
/*      */             } else {
/*  995 */               paramWebAppsContext.getErrorStack().addMessage("FND", "FND_APPL_LOGIN_FAILED");
/*  997 */               return SessionManager.AuthStatusCode.INVALID;
/*      */             } 
/* 1000 */             if (str8 == "") {
/* 1001 */               paramWebAppsContext.getErrorStack().addMessage("FND", "FND_APPL_LOGIN_FAILED");
/* 1003 */               return SessionManager.AuthStatusCode.INVALID;
/*      */             } 
/* 1006 */             int j = Integer.parseInt(str8.substring(0, 4));
/* 1008 */             int k = Integer.parseInt(str8.substring(4, 6)) - 1;
/* 1010 */             int m = Integer.parseInt(str8.substring(6, 8));
/* 1015 */             SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
/* 1016 */             Date date = new Date();
/* 1017 */             String str10 = simpleDateFormat.format(date);
/* 1018 */             int n = Integer.parseInt(str10.substring(0, 4));
/* 1020 */             int i1 = Integer.parseInt(str10.substring(4, 6)) - 1;
/* 1022 */             int i2 = Integer.parseInt(str10.substring(6, 8));
/* 1025 */             GregorianCalendar gregorianCalendar1 = new GregorianCalendar();
/* 1026 */             GregorianCalendar gregorianCalendar2 = new GregorianCalendar();
/* 1029 */             gregorianCalendar1.set(n, i1, i2);
/* 1030 */             gregorianCalendar2.set(j, k, m);
/* 1031 */             gregorianCalendar2.add(5, 90);
/* 1036 */             if (gregorianCalendar1.after(gregorianCalendar2))
/* 1037 */               return SessionManager.AuthStatusCode.EXPIRED; 
/* 1040 */           } catch (Exception exception) {}
/* 1043 */         } catch (Exception exception) {
/* 1044 */           paramWebAppsContext.getErrorStack().addMessage("FND", "FND_APPL_LOGIN_FAILED");
/* 1046 */           return SessionManager.AuthStatusCode.INVALID;
/*      */         } 
/* 1048 */       } catch (Exception exception) {
/* 1049 */         paramWebAppsContext.getErrorStack().addMessage("FND", "FND_APPL_LOGIN_FAILED");
/* 1051 */         return SessionManager.AuthStatusCode.INVALID;
/*      */       } 
/* 1054 */       return SessionManager.AuthStatusCode.VALID;
/*      */     } 
/* 1057 */     return SessionManager.AuthStatusCode.INVALID;
/*      */   }
/*      */   
/*      */   public static String convert(String paramString) {
/* 1065 */     StringBuilder stringBuilder1 = new StringBuilder();
/* 1066 */     StringBuilder stringBuilder2 = new StringBuilder();
/* 1069 */     for (byte b = 0; b < paramString.length() - 1; b += 2) {
/* 1072 */       String str = paramString.substring(b, b + 2);
/* 1074 */       int i = Integer.parseInt(str, 16);
/* 1076 */       stringBuilder1.append((char)i);
/* 1078 */       stringBuilder2.append(i);
/*      */     } 
/* 1081 */     return stringBuilder1.toString();
/*      */   }
/*      */   
/*      */   public static HashMap<Integer, String> ErrorConnexion(Exception paramException) {
/* 1089 */     String str1 = "data 532";
/* 1090 */     HashMap<Object, Object> hashMap1 = new HashMap<>();
/* 1091 */     hashMap1.put(Integer.valueOf(1), "FND");
/* 1092 */     hashMap1.put(Integer.valueOf(2), "FND_APPL_LOGIN_FAILED");
/* 1093 */     HashMap<Object, Object> hashMap2 = new HashMap<>();
/* 1094 */     hashMap2.put(Integer.valueOf(1), "XXST");
/* 1095 */     hashMap2.put(Integer.valueOf(2), msgExpired);
/* 1098 */     String str2 = paramException.toString();
/* 1099 */     HashMap<Object, Object> hashMap3 = hashMap1;
/* 1100 */     Pattern pattern = Pattern.compile(",");
/* 1101 */     String[] arrayOfString = pattern.split(str2);
/* 1103 */     for (byte b = 0; b < arrayOfString.length; b++) {
/* 1105 */       String str = arrayOfString[b].toString();
/* 1107 */       if (str.contains(str1)) {
/* 1108 */         hashMap3 = hashMap2;
/*      */         break;
/*      */       } 
/*      */     } 
/* 1112 */     return (HashMap)hashMap3;
/*      */   }
/*      */ }


/* Location:              C:\Users\rpathania\Desktop\Monthly Work\OFA\12.2_Migration\Login\DSERAI\!\XXSTSessionMgr.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */