/*$Header: XXSTSessionMgr.java 010.000.000 2020-05-04 APODDAR$*/
package oracle.apps.fnd.sso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

import java.math.BigDecimal;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oracle.apps.fnd.common.AppsEnvironmentStore;
import oracle.apps.fnd.common.AppsLog;
import oracle.apps.fnd.common.ErrorStack;
import oracle.apps.fnd.common.LangInfo;
import oracle.apps.fnd.common.Log;
import oracle.apps.fnd.common.Message;
import oracle.apps.fnd.common.ProfileStore;
import oracle.apps.fnd.common.VersionInfo;
import oracle.apps.fnd.common.WebAppsContext;
import oracle.apps.fnd.framework.webui.OAPageContext;
import oracle.apps.fnd.profiles.Profiles;
import oracle.apps.fnd.security.HTMLProcessor;
import oracle.apps.fnd.security.SessionManager;
import oracle.apps.fnd.security.UserPwd;
import oracle.apps.fnd.util.URLEncoder;
import oracle.apps.fnd.util.JDBC;

import oracle.jbo.domain.Number;

import oracle.cabo.ui.data.DataObject;

import oracle.apps.fnd.proxy.ProxyUserUtil;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.OracleTypes;


import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.*;
import javax.naming.directory.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;


/**
 * Provides SSO/Apps Session Management functionality.
 * @author: Sandeep Yarramreddy
 */
public class XXSTSessionMgr extends SessionMgr {

    private static boolean isLocalLogged;
    private static String msgExpired = "XXST_LOGIN_EXPIRED";
    // _______________________________________________________________________________ createAppsSession

    /**   
     * Validates the username/password specified by the pUser object and establishes an 
     * Oracle Apps Session. if the existing user session was a guest user session, 
     * then the guest user session is merged with that of the newly logged in user. If the
     * password expired then it creates a guest session if one does not exist already
     * @param pUser -UserPwd object containing the username and password to be validated
     * @param request - Http Servlet Request object.
     * @param response - Http Servlet Response object.
     * @return String redirect url after validation
     */
    public static String createAppsSession(UserPwd pUser, 
                                           HttpServletRequest request, 
                                           HttpServletResponse response) {

        isLocalLogged = false;

        boolean alreadySet = Utils.isAppsContextAvailable();
        WebAppsContext wctx = Utils.getAppsContext();
        Connection conn = null;

        String module = 
            "XXSTSessionMgr.createAppsSession(UserPwd, HttpServletRequest, HttpServletResponse)";
        String returnUrl = "";
        AppsLog log = Utils.getLog(wctx);
        boolean log_PROCEDURE = log.isEnabled(module, log.PROCEDURE);
        boolean log_STATEMENT = log.isEnabled(module, log.STATEMENT);
        boolean log_UNEXPECTED = log.isEnabled(module, log.UNEXPECTED);

        try {

            wctx.getErrorStack().clear();
            if (log_PROCEDURE) {
                log.write(module, "BEGIN", log.PROCEDURE);
                log.write(module, 
                          "Paramlist: request: " + request + " response: " + 
                          response, log.PROCEDURE);
            }
            Utils.setRequestCharacterEncoding(request);
            if (log_STATEMENT)
                log.write(module, "After setting Character encoding", 
                          log.STATEMENT);
            String requestUrl = request.getParameter("requestUrl");
            if (log_STATEMENT)
                log.write(module, "Got requestUrl: " + requestUrl, 
                          log.STATEMENT);
            if (requestUrl == null || requestUrl.equals("")) {
                requestUrl = "APPSHOMEPAGE";
                if (log_STATEMENT)
                    log.write(module, 
                              "Defaulting to requestUrl: " + requestUrl, 
                              log.STATEMENT);
            }

            String cancelUrl = request.getParameter("cancelUrl");
            if (log_STATEMENT)
                log.write(module, "Got cancelUrl: " + cancelUrl, 
                          log.STATEMENT);

            if (cancelUrl == null || cancelUrl.equals("")) {
                if (log_STATEMENT)
                    log.write(module, "cancelUrl not set", log.STATEMENT);
                cancelUrl = SSOUtil.getLocalLoginUrl();
                if (log_STATEMENT)
                    log.write(module, "after substring " + cancelUrl, 
                              log.STATEMENT);
            }
            String home_url = request.getParameter("home_url");
            if (log_STATEMENT)
                log.write(module, "Got home_url: " + home_url, log.STATEMENT);
            String langCode = request.getParameter("langCode");
            if (log_STATEMENT) {
                log.write(module, "Got langCode: " + langCode, log.STATEMENT);
                log.write(module, 
                          "Got Url params requestUrl: " + requestUrl + " cancelUrl: " + 
                          cancelUrl + " home_url: " + home_url + 
                          " langCode: " + langCode, log.STATEMENT);
            }


            if (xssViolation(request, wctx)) {
                if (log_STATEMENT)
                    log.write(module, "Url Parameter validation Failed!", 
                              log.STATEMENT);

                String linkXSS = 
                    "requestUrl=" + URLEncoder.encode(requestUrl, SessionMgr.getCharSet()) + 
                    "&cancelUrl=" + 
                    URLEncoder.encode(cancelUrl, SessionMgr.getCharSet()) + 
                    "&errCode=FND_SSO_PARAMVAL_SCAN_FAILED";

                if (langCode != null && !langCode.equals("")) {
                    linkXSS += "&langCode=" + langCode;
                }
                if (home_url != null && !home_url.equals("")) {
                    linkXSS += 
                            "&home_url=" + oracle.apps.fnd.util.URLEncoder.encode(home_url, 
                                                                                  SessionMgr.getCharSet());
                }
                returnUrl = SSOUtil.getLocalLoginUrl(linkXSS);
                if (log_STATEMENT)
                    log.write(module, "(XSS) returnUrl=" + returnUrl, 
                              log.STATEMENT);
            } else {
                if (log_STATEMENT)
                    log.write(module, 
                              "XSS check passed before calling validateLogin", 
                              log.STATEMENT);

                String userName = pUser.getUsername();
                SessionManager sessionManager = wctx.getSessionManager();

                // Si utilisateur existe dans FND_USER
                // Alors Verifier dans AD
                // Si utilisateur ADMIN verifier dans EBS
                // *************************************************************************************************
                SessionManager.AuthStatusCode validationStatus = 
                    validateLogin(wctx, pUser);

                if (log_STATEMENT)
                    log.write(module, 
                              "After Calling SessionManager.validateLogin" + 
                              " validationStatus: " + validationStatus, 
                              log.STATEMENT);

                if ((validationStatus == null || 
                     validationStatus.equals(SessionManager.AuthStatusCode.INVALID))) {
                    if (log_STATEMENT)
                        log.write(module, 
                                  "validateLogin Status:NULL or INVALID", 
                                  log.STATEMENT);
                    Message msg = wctx.getErrorStack().nextMessageObject();
                    String msgName = null;
                    String msgText = "";
                    String errString = "&errCode=";

                    if (msg != null) {
                        msgName = msg.getName();
                        msgText = msg.getMessageText(wctx.getResourceStore());
                        errString += 
                                URLEncoder.encode(msgName, SessionMgr.getCharSet());
                    }

                    String link = 
                        "requestUrl=" + URLEncoder.encode(requestUrl, SessionMgr.getCharSet()) + 
                        "&cancelUrl=" + 
                        URLEncoder.encode(cancelUrl, SessionMgr.getCharSet()) + 
                        errString;

                    if (langCode != null && !langCode.equals("")) {
                        link += "&langCode=" + langCode;
                    }
                    if (home_url != null && !home_url.equals("")) {
                        link += 
                                "&home_url=" + URLEncoder.encode(home_url, SessionMgr.getCharSet());
                    }

                    if (userName != null && !"".equals(userName)) {
                        link += 
                                "&username=" + URLEncoder.encode(userName, SessionMgr.getCharSet());
                    }
                    if (log_STATEMENT)
                        log.write(module, 
                                  " INVALID or NULL status returnUrl=" + 
                                  returnUrl, log.STATEMENT);
                    returnUrl = SSOUtil.getLocalLoginUrl(link);
                    if (log_STATEMENT)
                        log.write(module, "returnUrl ::" + returnUrl, 
                                  log.STATEMENT);

                } else if (validationStatus.equals(SessionManager.AuthStatusCode.VALID)) {
                    if (log_STATEMENT)
                        log.write(module, "validateLogin Successful:VALID", 
                                  log.STATEMENT);
                    conn = Utils.getConnection();
                    createSession(userName, wctx, request, response, false, 
                                  null, langCode);

                    // Setting accessibility refer to 5100744
                    String accessMode = 
                        request.getParameter("_lAccessibility");
                    if (accessMode != null && 
                        (accessMode.equals("Y") || accessMode.equals("S"))) {
                        wctx.getProfileStore().saveSpecificProfile("ICX_ACCESSIBILITY_FEATURES", 
                                                                   accessMode, 
                                                                   "USER", 
                                                                   String.valueOf(Utils.getUserId(userName)), 
                                                                   null);
                    }

                    if (requestUrl == null || requestUrl.equals("") || 
                        requestUrl.equals("APPSHOMEPAGE")) {
                        requestUrl = SSOUtil.getStartPageUrl(wctx);
                        if (log_STATEMENT)
                            log.write(module, 
                                      " setting returnUrl=" + requestUrl, 
                                      log.STATEMENT);
                        if (home_url != null) {
                            if (requestUrl.indexOf("OracleMyPage.home") != 
                                -1) {
                                if (requestUrl.indexOf("?") != -1) {
                                    requestUrl += 
                                            "&home_url=" + URLEncoder.encode(home_url, 
                                                                             SessionMgr.getCharSet());

                                } else {
                                    requestUrl += 
                                            "?home_url=" + URLEncoder.encode(home_url, 
                                                                             SessionMgr.getCharSet());
                                }
                                if (log_STATEMENT)
                                    log.write(module, "added home_url", 
                                              log.STATEMENT);
                            }
                        } else {
                            if (log_STATEMENT)
                                log.write(module, "home_url=NULL", 
                                          log.STATEMENT);
                        }
                    }
                    returnUrl = requestUrl;
                } else if (validationStatus.equals(SessionManager.AuthStatusCode.EXPIRED)) {
                    if (isLocalLogged) {
                        if (log_STATEMENT)
                            log.write(module, 
                                      "User password has expired:EXPIRED, creating Guest Session", 
                                      log.STATEMENT);
                        createSession(Utils.getGuestUserName(), wctx, request, 
                                      response, false, null, langCode);
                        conn = Utils.getConnection(wctx);
                        wctx.setSessionAttribute(SSOCommon.UNAME_KEY, 
                                                 userName);

                        String tmp = requestUrl;

                        String params = null;
                        if (langCode != null && !"".equals(langCode)) {
                            params = 
                                    "langCode=" + URLEncoder.encode(langCode, SessionMgr.getCharSet());
                        }
                        String canUrl = SSOUtil.getLocalLoginUrl(params);

                        params = 
                                "returnUrl=" + oracle.apps.fnd.util.URLEncoder.encode(tmp, 
                                                                                      SessionMgr.getCharSet());
                        params += 
                                "&cancelUrl=" + oracle.apps.fnd.util.URLEncoder.encode(canUrl, 
                                                                                       SessionMgr.getCharSet());

                        if (log_STATEMENT)
                            log.write(module, 
                                      "[CHGPWD]requestUrl=" + requestUrl, 
                                      log.STATEMENT);
                        returnUrl = SSOUtil.getLocalPwdChangeUrl(params);
                    } else { //Utilisateur AD
                        returnUrl = 
                                wctx.getProfileStore().getProfile("XXST_LOGIN_EXPIRED_URL"); //SSOUtil.getLocalPwdChangeUrl(params);
                    }
                }
            }
            if (log_PROCEDURE)
                log.write(module, "END: returnUrl: " + requestUrl, 
                          log.PROCEDURE);
        } catch (Throwable e) {
            if (log_UNEXPECTED) {
                log.write(module, "Exception" + e.toString(), log.UNEXPECTED);
                log.write(module, e, log.UNEXPECTED);
            }
            //rethrow
            throw new RuntimeException(e.toString());
        } finally {
            if (!alreadySet)
                Utils.releaseAppsContext();
        }
        return returnUrl;
    }

    // ____________________________________________________________________________ getSteriaLoginMethod
    /* 1 = AD
 * 2 = LDAP
 */

    public static int getSteriaLoginMethod(WebAppsContext wctx) {
        String s = wctx.getProfileStore().getProfile("XXST_LOGIN_METHOD");
        if ("AD".equals(s))
            return 1;
        else if ("ADONE".equals(s))
            return 2;
        else if ("LDAPONE".equals(s))
            return 3;
        else
            return 4;
    }

    // ___________________________________________________________________________________ validateLogin

    public static SessionManager.AuthStatusCode validateLogin(WebAppsContext wctx, 
                                                              UserPwd userpwd) {
        if ((userpwd.getUsername() == null) || 
            "".equals(userpwd.getUsername()) || 
            (userpwd.getPassword() == null) || 
            "".equals(userpwd.getPassword())) {
            return SessionManager.AuthStatusCode.INVALID;
        }

        OracleResultSet res = null;
        OraclePreparedStatement stmt = null;

        // *** VERIFICATION EXISTANCE USER SUR LA BASE *************************************************
        try {
            stmt = 
(OraclePreparedStatement)wctx.getJDBCConnection().prepareStatement(" select 1 from fnd_user where user_name = :1 " + 
                                                                   " and trunc(sysdate) between trunc(nvl(START_DATE,SYSDATE)) and trunc(nvl(END_DATE,SYSDATE)) ");
            stmt.setString(1, userpwd.getUsername());

            res = (OracleResultSet)stmt.executeQuery();
            if (!res.next()) {
                res.close();
                stmt.close();
                wctx.getErrorStack().addMessage(new Message("XXST", 
                                                            "XXST_LOGIN_USER_NOT_FOUND"));
                return SessionManager.AuthStatusCode.INVALID;
            }
        } catch (SQLException e) {
            wctx.getErrorStack().addMessage("FND", 
                                            "Fatal exception : " + e.getMessage());
            return SessionManager.AuthStatusCode.INVALID;
        } finally {
            try {
                res.close();
                stmt.close();
            } catch (Exception ex) {
            }
        }

        // *** VERIFICATION EN NATIF *******************************************************************
        try {
            stmt = 
(OraclePreparedStatement)wctx.getJDBCConnection().prepareStatement(" select lookup_code from fnd_lookup_values_vl " + 
                                                                   " where lookup_type = 'XXST_LOGIN_NATIVE_USERS' " + 
                                                                   " and lookup_code = :1 " + 
                                                                   " and trunc(sysdate) between trunc(nvl(START_DATE_ACTIVE,SYSDATE)) and trunc(nvl(END_DATE_ACTIVE,SYSDATE)) " + 
                                                                   " and enabled_flag = 'Y' ");
            stmt.setString(1, userpwd.getUsername());

            res = (OracleResultSet)stmt.executeQuery();
            if (res.next()) {
                SessionManager sMgr = wctx.getSessionManager();
                res.close();
                stmt.close();

                isLocalLogged = true;
                return sMgr.validateLogin(userpwd);
            }
        } catch (SQLException e) {
            wctx.getErrorStack().addMessage("FND", 
                                            "Fatal exception : " + e.getMessage());
            return SessionManager.AuthStatusCode.INVALID;
        } finally {
            try {
                res.close();
                stmt.close();
            } catch (Exception ex) {
            }
        }

        // *** VERIFICATION EN NATIF SANS RETOUR INVALIDE **********************************************
        try {
            stmt = 
(OraclePreparedStatement)wctx.getJDBCConnection().prepareStatement(" select lookup_code from fnd_lookup_values_vl " + 
                                                                   " where lookup_type = 'XXST_LOGIN_NAT_AND_EXT_USERS' " + 
                                                                   " and lookup_code = :1 " + 
                                                                   " and trunc(sysdate) between trunc(nvl(START_DATE_ACTIVE,SYSDATE)) and trunc(nvl(END_DATE_ACTIVE,SYSDATE)) " + 
                                                                   " and enabled_flag = 'Y' ");
            stmt.setString(1, userpwd.getUsername());

            res = (OracleResultSet)stmt.executeQuery();
            if (res.next()) {
                SessionManager sMgr = wctx.getSessionManager();
                res.close();
                stmt.close();

                isLocalLogged = true;
                SessionManager.AuthStatusCode statusCode = 
                    sMgr.validateLogin(userpwd);
                if (statusCode.equals(SessionManager.AuthStatusCode.VALID)) {
                    return SessionManager.AuthStatusCode.VALID;
                }
                if (statusCode.equals(SessionManager.AuthStatusCode.EXPIRED)) {
                    return SessionManager.AuthStatusCode.EXPIRED;
                }
            }
        } catch (SQLException e) {
            wctx.getErrorStack().addMessage("FND", 
                                            "Fatal exception : " + e.getMessage());
            return SessionManager.AuthStatusCode.INVALID;
        } finally {
            try {
                res.close();
                stmt.close();
            } catch (Exception ex) {
            }
        }

        isLocalLogged = false;
        int loginMethod = getSteriaLoginMethod(wctx);

        String expiredAttribute = 
            wctx.getProfileStore().getProfile("XXST_LOGIN_EXPIRED_ATTRIBUTE");

        if (loginMethod == 4) {
            // *** VERIFICATION SUR LDAP *****************************************************************

            // Recuperation des valeurs des options de profil
            String providerUrl = 
                wctx.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_URL");
            String providerPath = 
                wctx.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_PATH");
            if (providerUrl == null || "".equals(providerUrl)) {
                wctx.getErrorStack().addMessage("FND", 
                                                "Provider URL is null.");
                return SessionManager.AuthStatusCode.INVALID;
            }
            if (providerPath == null || "".equals(providerPath)) {
                wctx.getErrorStack().addMessage("FND", 
                                                "Provider path is null.");
                return SessionManager.AuthStatusCode.INVALID;
            }

            // Initialisation de la connexion utilisateur
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, 
                    "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, providerUrl);
            env.put(Context.SECURITY_CREDENTIALS, 
                    "" + userpwd.getPassword() + "");
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, 
                    "uid=" + userpwd.getUsername() + "," + providerPath);

            // Connexion utilisateur
            try {
                DirContext dctx = new InitialDirContext(env);

                // Recuperation de la date de derniere mise a jour du mot de passe
                try {
                    Attributes attrs = 
                        dctx.getAttributes("uid=" + userpwd.getUsername() + 
                                           "," + providerPath);
                    Object attr_date = attrs.get(expiredAttribute).get();

                    String date_valid = attr_date.toString();
                    int ldap_year = 
                        Integer.parseInt(date_valid.substring(0, 4));
                    int ldap_month = 
                        Integer.parseInt(date_valid.substring(4, 6)) - 1;
                    int ldap_day = 
                        Integer.parseInt(date_valid.substring(6, 8));

                    Date today;
                    SimpleDateFormat formatter;
                    formatter = new SimpleDateFormat("yyyyMMdd");
                    today = new Date();
                    String date_to_comp = formatter.format(today);
                    int today_year = 
                        Integer.parseInt(date_to_comp.substring(0, 4));
                    int today_month = 
                        Integer.parseInt(date_to_comp.substring(4, 6)) - 1;
                    int today_day = 
                        Integer.parseInt(date_to_comp.substring(6, 8));

                    GregorianCalendar myCalendar = new GregorianCalendar();
                    GregorianCalendar myCalendar2 = new GregorianCalendar();

                    myCalendar.set(today_year, today_month, today_day);
                    myCalendar2.set(ldap_year, ldap_month, ldap_day);
                    myCalendar2.add(Calendar.DATE, 90);


                    // Si la mise a jour date de plus de 90 jours, 
                    // le mot de passe est expire
                    if (myCalendar.after(myCalendar2)) {
                        return SessionManager.AuthStatusCode.EXPIRED;
                    }
                } catch (Exception e) {
                }

                dctx.close();
            } catch (Exception ex) {
                wctx.getErrorStack().addMessage(new Message("FND", 
                                                            "FND_APPL_LOGIN_FAILED"));
                return SessionManager.AuthStatusCode.INVALID;
            }

            return SessionManager.AuthStatusCode.VALID;
        } else if (loginMethod == 1) {
            // *** VERIFICATION SUR AD *******************************************************************

            // Recuperation des valeurs des options de profil
            String providerUrl = 
                wctx.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_URL");
            String providerDomain = 
                wctx.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_DOMAIN");
            String domainsList = 
                wctx.getProfileStore().getProfile("XXST_LOGIN_DOMAINS_LIST");
            if (providerUrl == null || "".equals(providerUrl)) {
                wctx.getErrorStack().addMessage("FND", 
                                                "Provider URL is null.");
                return SessionManager.AuthStatusCode.INVALID;
            }
            if (providerDomain == null || "".equals(providerDomain)) {
                wctx.getErrorStack().addMessage("FND", 
                                                "Provider domain is null");
                return SessionManager.AuthStatusCode.INVALID;
            }
            if (domainsList == null || "".equals(domainsList)) {
                wctx.getErrorStack().addMessage("FND", 
                                                "Domains list is null.");
                return SessionManager.AuthStatusCode.INVALID;
            }

            // Initialisation de la connexion utilisateur
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, 
                    "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, providerUrl);
            env.put(Context.SECURITY_CREDENTIALS, 
                    "" + userpwd.getPassword() + "");

            StringTokenizer stokenz = new StringTokenizer(domainsList, "|");

            // Connexion utilisateur sur chaque domaine
            boolean connected = false;
            String domain = null;
            while (stokenz.hasMoreTokens()) {
                domain = stokenz.nextToken();
                try {
                    env.put(Context.SECURITY_PRINCIPAL, 
                            userpwd.getUsername() + "@" + domain + "." + 
                            providerDomain);

                    DirContext dctx = new InitialDirContext(env);
                    connected = true;

                    // Recuperation de la date de derniere mise a jour du mot de passe
                    try {
                        String filter = 
                            "(&(objectClass=user)(sAMAccountName=" + 
                            userpwd.getUsername() + "))";
                        String[] attsNames = { expiredAttribute };

                        SearchControls ctrl = new SearchControls();
                        ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
                        ctrl.setReturningAttributes(attsNames);

                        NamingEnumeration srch = dctx.search("", filter, ctrl);
                        if (srch.hasMoreElements()) {
                            SearchResult srs = (SearchResult)srch.next();
                            Attributes attrs = srs.getAttributes();
                            String whenChanged = null;
                            if (attrs != null) {
                                whenChanged = 
                                        (String)attrs.get(expiredAttribute).get();

                                SimpleDateFormat sdf = 
                                    new SimpleDateFormat("yyyyMMdd");
                                Date whenChangedD = 
                                    sdf.parse(whenChanged.substring(0, 8));
                                Date now = Calendar.getInstance().getTime();

                                long daysDiff = 
                                    (now.getTime() - whenChangedD.getTime());
                                daysDiff = daysDiff / 1000;
                                daysDiff = daysDiff / 3600;
                                daysDiff = daysDiff / 24;

                                // Si la mise a jour date de plus de 90 jours, 
                                // le mot de passe est expire
                                if (daysDiff > 90) {
                                    return SessionManager.AuthStatusCode.EXPIRED;
                                }
                            }
                        }
                    } catch (Exception e) {
                    }

                    dctx.close();

                    break;
                } catch (Exception e) {
                }
            }

            // Si le login a echoue sur tous les domaines AD
            if (!connected) {
                wctx.getErrorStack().addMessage(new Message("FND", 
                                                            "FND_APPL_LOGIN_FAILED"));
                return SessionManager.AuthStatusCode.INVALID;
            } else {
                return SessionManager.AuthStatusCode.VALID;
            }
        } else if (loginMethod == 2) {
            // *** VERIFICATION SUR AD ONE *******************************************************************

            // Recuperation des valeurs des options de profil
            String providerUrl = 
                wctx.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_URL");
            // String providerPath = wctx.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_PATH");
            String providerDomain = 
                wctx.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_DOMAIN");
            if (providerUrl == null || "".equals(providerUrl)) {
                wctx.getErrorStack().addMessage("FND", 
                                                "XXST_LOGIN_PROVIDER_URL is null");
                return SessionManager.AuthStatusCode.INVALID;
            }
            /*
       if (providerPath == null || "".equals(providerPath))
      {
        wctx.getErrorStack().addMessage("FND","XXST_LOGIN_PROVIDER_URL is null");
        return SessionManager.AuthStatusCode.INVALID;
      }
      */
            if (providerDomain == null || "".equals(providerDomain)) {
                wctx.getErrorStack().addMessage("FND", 
                                                "Profile XXST_LOGIN_PROVIDER_DOMAIN not provided");
                return SessionManager.AuthStatusCode.INVALID;
            }

            AppsLog log = Utils.getLog(wctx);

            // Initialisation de la connexion utilisateur
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, 
                    "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, providerUrl);
            env.put(Context.SECURITY_CREDENTIALS, 
                    "" + userpwd.getPassword() + "");
            env.put(Context.SECURITY_PRINCIPAL, 
                    "" + userpwd.getUsername() + "@" + providerDomain + "");
            log.write("validateLogin", 
                      "Principal " + userpwd.getUsername() + "@" + 
                      providerDomain, log.UNEXPECTED);
            log.write("validateLogin", "env created", log.UNEXPECTED);
            try {
                String keyStorePath = 
                    wctx.getProfileStore().getProfile("XXST_LOGIN_KEYSTORE_PATH");
                String keyStorePassword = 
                    wctx.getProfileStore().getProfile("XXST_LOGIN_KESTORE_PASSWORD");
                if (keyStorePath != null && !"".equals(keyStorePath)) {

                    System.setProperty("javax.net.ssl.trustStore", 
                                       keyStorePath);
                    System.setProperty("javax.net.ssl.trustStorePassword", 
                                       keyStorePassword);
                    log.write("validateLogin", "KeyStore Path Set ", 
                              log.UNEXPECTED);
                    if ((providerUrl.substring(0, 
                                               5)).toUpperCase().equals("LDAPS")) {
                        env.put(Context.SECURITY_PROTOCOL, "ssl");
                    }
                } else {
                    if ((providerUrl.substring(0, 
                                               5)).toUpperCase().equals("LDAPS")) {
                        wctx.getErrorStack().addMessage("FND", 
                                                        "KeyStore EMPTY");
                        return SessionManager.AuthStatusCode.INVALID;
                    }
                }

            } catch (Exception keyFail) {
                log.write("validateLogin", "Excp: " + keyFail.toString(), 
                          log.UNEXPECTED);
                wctx.getErrorStack().addMessage("FND", 
                                                "Exception Setting KeyStore");
                return SessionManager.AuthStatusCode.INVALID;
            }
            // Connexion utilisateur
            try {
                log.write("validateLogin", 
                          "Try Connection --> " + userpwd.getUsername() + "@" + 
                          providerDomain, log.UNEXPECTED);
                DirContext dctx = new InitialDirContext(env);
                dctx.close();
            } catch (Exception ex) {

                HashMap<Integer, String> codeMessage = ErrorConnexion(ex);

                // retourne : FND_APPL_LOGIN_FAILED ou FND_APPL_PWD_EXPIRED;
                log.write("validateLogin", "Excp" + ex.toString(), 
                          log.UNEXPECTED);
                wctx.getErrorStack().addMessage(codeMessage.get(1), 
                                                codeMessage.get(2));

                //wctx.getErrorStack().addMessage("FND", "FND_APPL_LOGIN_FAILED");

                if (codeMessage.get(2).equals(msgExpired)) {
                    //isLocalLogged = true;
                    wctx.getErrorStack().addMessage("FND", "Expired ..... ");
                    return SessionManager.AuthStatusCode.EXPIRED;
                }
                wctx.getErrorStack().addMessage("FND", "Excp" + ex.toString());
                return SessionManager.AuthStatusCode.INVALID;
            }

            return SessionManager.AuthStatusCode.VALID;
        } else if (loginMethod == 3) {
            // *** VERIFICATION SUR LDAP ONE *************************************************************

            // Recuperation des valeurs des options de profil
            String providerUrl = 
                wctx.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_URL");
            String providerPath = 
                wctx.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_PATH");
            String providerDomain = 
                wctx.getProfileStore().getProfile("XXST_LOGIN_PROVIDER_DOMAIN");
            String providerAdminUser = 
                wctx.getProfileStore().getProfile("XXST_LOGIN_PROV_ADMIN");
            String providerAdminPwd = 
                wctx.getProfileStore().getProfile("XXST_LOGIN_PROV_ADM_PWD");

            if (providerUrl == null || "".equals(providerUrl)) {
                wctx.getErrorStack().addMessage("FND", 
                                                "XXST_LOGIN_PROVIDER_URL is null");
                return SessionManager.AuthStatusCode.INVALID;
            }
            if (providerPath == null || "".equals(providerPath)) {
                wctx.getErrorStack().addMessage("FND", 
                                                "XXST_LOGIN_PROVIDER_PATH is null");
                return SessionManager.AuthStatusCode.INVALID;
            }
            if (providerDomain == null || "".equals(providerDomain)) {
                wctx.getErrorStack().addMessage("FND", 
                                                "XXST_LOGIN_PROVIDER_DOMAIN is null");
                return SessionManager.AuthStatusCode.INVALID;
            }
            if (providerAdminUser == null || "".equals(providerAdminUser)) {
                wctx.getErrorStack().addMessage("FND", 
                                                "XXST_LOGIN_PROV_ADMIN is null");
                return SessionManager.AuthStatusCode.INVALID;
            }
            if (providerAdminPwd == null || "".equals(providerAdminPwd)) {
                wctx.getErrorStack().addMessage("FND", 
                                                "XXST_LOGIN_PROV_ADM_PWD is null");
                return SessionManager.AuthStatusCode.INVALID;
            }


            // Recherche de userPrincipalName de l'utilisateur
            Hashtable envAdmin = new Hashtable();
            envAdmin.put(Context.INITIAL_CONTEXT_FACTORY, 
                         "com.sun.jndi.ldap.LdapCtxFactory");
            envAdmin.put(Context.PROVIDER_URL, providerUrl);
            envAdmin.put(Context.SECURITY_CREDENTIALS, providerAdminPwd);
            envAdmin.put(Context.SECURITY_PRINCIPAL, providerAdminUser);
            String userPrincipalName = null;
            try {
                String keyStorePath = 
                    wctx.getProfileStore().getProfile("XXST_LOGIN_KEYSTORE_PATH");
                String keyStorePassword = 
                    wctx.getProfileStore().getProfile("XXST_LOGIN_KESTORE_PASSWORD");
                if (keyStorePath != null && !"".equals(keyStorePath)) {
                  System.setProperty("javax.net.ssl.trustStore", 
                                       keyStorePath);
                    System.setProperty("javax.net.ssl.trustStorePassword", 
                                       keyStorePassword);
									   
					  System.setProperty("javax.net.ssl.keyStore", 
                                       keyStorePath);				   
                    System.setProperty("javax.net.ssl.keyStorePassword", 
                                       keyStorePassword);
                    if ((providerUrl.substring(0, 
                                               5)).toUpperCase().equals("LDAPS")) {
                        envAdmin.put(Context.SECURITY_PROTOCOL, "ssl");
                    }
                }

            } catch (Exception keyFail) {
            }

            // Test de connexion et d'expiration de mot de passe
            try {

                // Connexion utilisateur
                try {
                    DirContext dctx = new InitialDirContext(envAdmin);

                    //Recuperation du userPrincipalName et d'audio
                    try {
                        String filter = 
                            "(&(objectClass=user)(sAMAccountName=" + 
                            userpwd.getUsername() + 
                            "))"; /***** SAMUSER Search PPPPP ****/
                        String[] attrNames = { "userPrincipalName" };

                        SearchControls ctrl = new SearchControls();
                        ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
                        ctrl.setReturningAttributes(attrNames);

                        NamingEnumeration srch = 
                            dctx.search("ou=ofa,dc=one,dc=steria,dc=dom", 
                                        filter, ctrl);

                        // Recuperation attribut userPrincipalName
                        if (srch.hasMoreElements()) {
                            SearchResult srs = (SearchResult)srch.next();
                            Attributes attrs = srs.getAttributes();

                            if (attrs != null) {
                                userPrincipalName = 
                                        (String)attrs.get("userPrincipalName").get();
                            }
                        }

                    } catch (Exception e) {
                    }

                    dctx.close();
                } catch (Exception ex) {
                    wctx.getErrorStack().addMessage("FND", 
                                                    "FND_APPL_LOGIN_FAILED");
                    return SessionManager.AuthStatusCode.INVALID;
                }

                if (userPrincipalName == null || userPrincipalName == "") {
                    wctx.getErrorStack().addMessage("FND", 
                                                    "FND_APPL_LOGIN_FAILED");
                    return SessionManager.AuthStatusCode.INVALID;
                }


                // Initialisation de la connexion utilisateur
                Hashtable env = new Hashtable();
                env.put(Context.INITIAL_CONTEXT_FACTORY, 
                        "com.sun.jndi.ldap.LdapCtxFactory");
                env.put(Context.PROVIDER_URL, providerUrl);
                env.put(Context.SECURITY_CREDENTIALS, userpwd.getPassword());
                env.put(Context.SECURITY_PRINCIPAL, userPrincipalName);
                try {
                    String keyStorePath = 
                        wctx.getProfileStore().getProfile("XXST_LOGIN_KEYSTORE_PATH");
                    String keyStorePassword = 
                        wctx.getProfileStore().getProfile("XXST_LOGIN_KESTORE_PASSWORD");
                    if (keyStorePath != null && !"".equals(keyStorePath)) {
                      
                           System.setProperty("javax.net.ssl.trustStore",
                                       keyStorePath);
                           System.setProperty("javax.net.ssl.trustStorePassword",
                                       keyStorePassword);
                           System.setProperty("javax.net.ssl.keyStore",
                                       keyStorePath);
                           System.setProperty("javax.net.ssl.keyStorePassword",
                                       keyStorePassword);

                        env.put(Context.SECURITY_PROTOCOL, "ssl");
                    }

                } catch (Exception keyFail) {
                }
                String audio = "";
                String date_valid = "";

                // Test sur l'attribut audio afin de savoir si le mot de passe est expire
                try {
                    DirContext dctx = new InitialDirContext(env);

                    try{

                        String filter = 
                            "(&(objectClass=user)(sAMAccountName=" + 
                            userpwd.getUsername() + 
                            "))"; /***** SAMUSER Search PPPPP ****/
                        String[] attrNamesExpired = { expiredAttribute };

                        SearchControls ctrl = new SearchControls();
                        ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
                        ctrl.setReturningAttributes(attrNamesExpired);
                        NamingEnumeration srch = 
                            dctx.search("ou=ofa,dc=one,dc=steria,dc=dom", 
                                        filter, ctrl);

                        // Recuperation attribut audio
                        if (srch.hasMoreElements()) {
                            SearchResult srs = (SearchResult)srch.next();
                            Attributes attrs = srs.getAttributes();

                            if (attrs != null) {
                                Attribute attr = 
                                    attrs.get("whenChanged"); /***** audio whenchanged Search PPPPP ****/
                                if (attr != null) {
                                    Object val = attr.get();
                                    if (val instanceof String) {
                                        System.out.println(val);
                                        audio = val.toString();
                                    } else {
                                        byte[] buf = (byte[])val;
                                        for (int i = 0; i < buf.length; i++) {
                                            audio = 
                                                    audio + Integer.toHexString(buf[i]);
                                        }
                                    }
                                }
                            }
                        }


                        if (audio != "") {
                            date_valid = convert(audio);
                        } else {
                            wctx.getErrorStack().addMessage("FND", 
                                                            "FND_APPL_LOGIN_FAILED");
                            return SessionManager.AuthStatusCode.INVALID;
                        }

                        if (date_valid == "") {
                            wctx.getErrorStack().addMessage("FND", 
                                                            "FND_APPL_LOGIN_FAILED");
                            return SessionManager.AuthStatusCode.INVALID;
                        }

                        int ldap_year = 
                            Integer.parseInt(date_valid.substring(0, 4));
                        int ldap_month = 
                            Integer.parseInt(date_valid.substring(4, 6)) - 1;
                        int ldap_day = 
                            Integer.parseInt(date_valid.substring(6, 8));

                        Date today;
                        SimpleDateFormat formatter;
                        formatter = new SimpleDateFormat("yyyyMMdd");
                        today = new Date();
                        String date_to_comp = formatter.format(today);
                        int today_year = 
                            Integer.parseInt(date_to_comp.substring(0, 4));
                        int today_month = 
                            Integer.parseInt(date_to_comp.substring(4, 6)) - 1;
                        int today_day = 
                            Integer.parseInt(date_to_comp.substring(6, 8));

                        GregorianCalendar myCalendar = new GregorianCalendar();
                        GregorianCalendar myCalendar2 = 
                            new GregorianCalendar();

                        myCalendar.set(today_year, today_month, today_day);
                        myCalendar2.set(ldap_year, ldap_month, ldap_day);
                        myCalendar2.add(Calendar.DATE, 90);


                        // Si la mise a jour date de plus de 90 jours, 
                        // le mot de passe est expire
                        if (myCalendar.after(myCalendar2)) {
                            return SessionManager.AuthStatusCode.EXPIRED;
                        }

                    } catch (Exception e) {
                    }

                } catch (Exception e) {
                    wctx.getErrorStack().addMessage("FND", 
                                                    "FND_APPL_LOGIN_FAILED");
                    return SessionManager.AuthStatusCode.INVALID;
                }
            } catch (Exception ex) {
                wctx.getErrorStack().addMessage("FND", 
                                                "FND_APPL_LOGIN_FAILED");
                return SessionManager.AuthStatusCode.INVALID;
            }

            return SessionManager.AuthStatusCode.VALID;
        }

        return SessionManager.AuthStatusCode.INVALID;

    }

    // Fonction permettant de transformer un hexa en string

    public static String convert(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 transformee en char 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char)decimal);

            temp.append(decimal);
        }

        return sb.toString();
    }

    public static HashMap<Integer, String> ErrorConnexion(Exception ex) {

        //Si le code de l'exeption est 532 on est dans le cas d'un mot de passe expire
        //Sinon on est dans un cas de probleme de login mot de passe

        String attributRecherche = "data 532";
        HashMap<Integer, String> LOGIN_FAILED = new HashMap<Integer, String>();
        LOGIN_FAILED.put(1, "FND");
        LOGIN_FAILED.put(2, "FND_APPL_LOGIN_FAILED");
        HashMap<Integer, String> PWD_EXPIRED = new HashMap<Integer, String>();
        PWD_EXPIRED.put(1, "XXST");
        PWD_EXPIRED.put(2, msgExpired);


        String msg = ex.toString();
        HashMap<Integer, String> msgRetour = LOGIN_FAILED;
        Pattern p = Pattern.compile(",");
        String[] attrs = p.split(msg);

        for (int i = 0; i < attrs.length; i++) {

            String code = attrs[i].toString();

            if (code.contains(attributRecherche)) {
                msgRetour = PWD_EXPIRED;
                break;
            }
        }
        return msgRetour;
    }
}
