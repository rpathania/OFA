/*=============================================================================+
 |    Copyright (c) 2002 Oracle Corporation, Redwood Shores, CA, USA
 |
 |               All rights reserved.
 |
 +=============================================================================+
 +================= COMMENTS ARE AUTOMATICALLY ADDED BY ARCS==================+
 +=============== DESCRIBE YOUR CURRENT CHANGES DURING CHECK IN ===============+
 +=============== SEE JAVADOC COMMENTS FOR DESCRIPTION OF CLASS ===============+
 +=============================================================================+
 The following comments are automatically added by ARCS using your check
 in comment so please describe your current changes during the ARCS 'in'
 +=============================================================================+

 $Log: AuthenticateUser.java,v $
 Revision 120.7.12010000.3  2010/01/19 20:29:33  rsantis
 Bug 9013958: User SecureHttpRequest to protect from phishing attacks.
 Forced checkin of dual maintained file from revision 120.7.12000000.5

 Revision 120.10  2008/12/09 19:56:40  ctilley
 Same as previous version - enabled dualchkn for 12.1.1
 Forced checkin of dual maintained file from revision 120.7.12000000.4

 Revision 120.7.12000000.3  2008/12/09 17:02:52  ctilley
 Bug 7586305: Forward port of Signon Notification fix 7169414.  Affect
 of custom login pages is unknown but for local login an alert is displayed
 if number of unsuccessful logins is greater than 0 and the SignON notification
 profile is enabled.

 Revision 120.7.12000000.2  2008/04/22 19:55:58  rsantis
 Bug 5015899: 5015899 Forward port
    Authenticate user will use java script to popup an alert when there were failed attempts
    since the last succesfull login.
*/


package xxst.oracle.apps.fnd.login;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.sql.Connection;

import oracle.apps.fnd.common.VersionInfo;
import oracle.apps.fnd.common.WebAppsContext;
import oracle.apps.fnd.common.AppsLog;
import oracle.apps.fnd.common.Log;

import oracle.apps.fnd.sso.Utils;
import oracle.apps.fnd.security.UserPwd;
import xxst.oracle.apps.fnd.sso.XXSTSessionMgr;
import oracle.apps.fnd.functionSecurity.RunFunctionUtil;
import oracle.apps.fnd.common.ThreadContext;
import oracle.apps.fnd.common.Const;
import oracle.apps.fnd.common.ResourceStore;
import oracle.apps.fnd.common.Message;
import oracle.apps.fnd.sso.SecureHttpRequest;
//import oracle.apps.fnd.sso.AuthenticationException;


/** AuthenticateUser is a servlet. It will authenticate an users credentials, username & password
 *  against FND_USER table. Upon Authentication the user will be redirected to either the Applications Home page
 *  or any other requested url based on some parameters.
 *  
 *  For developers using OA Framework, and customizing the login page, this servlet is defined as a 
 *  FND_FORM_FUNCTION with FUNCTION_NAME as APPS_VALIDATION_SERVLET
 *  <br>
 *  <br>
 *  The following example can be used to post to this servlet from an OA Controller class
 *   <i>     pageContext.setForceForwardURL("APPS_VALIDATION_SERVLET",
 *                                   KEEP_MENU_CONTEXT,null,params,false,
 *                                   ADD_BREAD_CRUMB_NO,
 *                                  OAException.ERROR)</i>;
 * Make sure you use setForceForwardURL instead of setForwarURL
 * For Developers using any other techstack, there is no restriction on how to post to the servlet.
 * @see oracle.apps.fnd.login.AuthenticateUser
 * @rep:scope public
 * @rep:product FND
 * @rep:displayname Authenticate User
 * @rep:category BUSINESS_ENTITY FND_SSO_MANAGER
 */
public class XXSTAuthenticateUser extends AuthenticateUser 
{

  /** As part of request to this method, <b>username</b>, <b> password</b> are expected. 
   *  This method also will accept a <b>langCode</b> parameter. If the langCode parameter is null,
   *  language of the user Session  will be defaulted to US-English.
   *  If the user is succesfully Authenticated, he will be redirected to homepage.
   *  If the authentication fails the response from the servlet will contain a <b>errCode</b> parameter.
   *  This will contain an appropriate FND_MESSAGE.
   * @see oracle.apps.fnd.login.AuthenticateUser
   * @param request A valid HttpServletReuest object
   * @param response A valid HttpServletResponse object
   * @throws ServletException
   * @throws IOException
   * @rep:scope  public
   * @rep:displayname Do Post method of AuthenticateUser servlet class.
   */
  public void doPost(HttpServletRequest request,HttpServletResponse response) 
  throws ServletException, IOException
  {
    boolean alreadySet = false;
    
    WebAppsContext wctx = null;
    
    PrintWriter out = response.getWriter();

    ResourceStore rStore = null;
    
    String notMsg = null;
    String saNotifyProf  = "N";
    String failedAttempts = "0";

    //Bug7169414: Initialized to ensure NullPointerException does not occur
    
    ThreadContext c = new ThreadContext();
    c.put(Const.INVALID_LOGINS, failedAttempts);
    c.put(Const.SIGNON_AUDIT_NOTIFY_PROFILE, saNotifyProf);

    if(Utils.isAppsContextAvailable())
    {
      wctx = Utils.getAppsContext();  
      alreadySet = true;
    }
    else
    {
      wctx = Utils.getAppsContext();
    }

    if(((AppsLog)wctx.getLog()).isEnabled(AppsLog.PROCEDURE))
      Utils.writeToLog("oracle.apps.fnd.login.AuthenticateUser", "Begin of doPost ", wctx, Log.PROCEDURE);    

    rStore = wctx.getResourceStore();

    Utils.setRequestCharacterEncoding(request);
    
    String forwardUrl = "";
    
    request = SecureHttpRequest.check(request,wctx);
    String username = request.getParameter("username").trim();
    String password = request.getParameter("password");
    String langCode = request.getParameter("langCode");
    
    Connection conn = null;

    try 
    {
      /** conn = Utils.getConnection(); */
      conn = wctx.getJDBCConnection();
      
      UserPwd pUser = new UserPwd(username, password);
      
      forwardUrl = null;
      forwardUrl = XXSTSessionMgr.createAppsSession(pUser, request, response);
      
      conn.commit();

      if(((AppsLog)wctx.getLog()).isEnabled(AppsLog.STATEMENT)) 
        Utils.writeToLog("oracle.apps.fnd.login.AuthenticateUser", "forwardUrlafter session creation: "+forwardUrl, wctx, Log.STATEMENT);

      failedAttempts = (String) String.valueOf(c.get(Const.INVALID_LOGINS));
      saNotifyProf   = (String) String.valueOf(c.get(Const.SIGNON_AUDIT_NOTIFY_PROFILE));

      if(((AppsLog)wctx.getLog()).isEnabled(AppsLog.STATEMENT))
        Utils.writeToLog("oracle.apps.fnd.login.AuthenticateUser", "Signon notif attributes: "+failedAttempts+" "+saNotifyProf, wctx, Log.STATEMENT);

      forwardUrl = RunFunctionUtil.processRequestURL (forwardUrl, wctx.getUserId(), wctx);

      if (Integer.parseInt(failedAttempts) > 0 && "Y".equals(saNotifyProf) && request.getParameter("nojavascript")==null)
      {
        if(((AppsLog)wctx.getLog()).isEnabled(AppsLog.STATEMENT))
          Utils.writeToLog("oracle.apps.fnd.login.AuthenticateUser", "Raise signon notification alert. Invalid logins: "+failedAttempts, wctx, Log.STATEMENT);

        Message msg = new Message("FND","SECURITY-UNSUCCESSFUL LOGINS");
        msg.setToken("NUMBER",failedAttempts,false);
        notMsg = msg.getMessageText(rStore,langCode);
        notMsg = notMsg.replaceAll("\n","\\\\n").replaceAll("'","\\\\'").replaceAll("\"","\\\\\"");

        if(((AppsLog)wctx.getLog()).isEnabled(AppsLog.STATEMENT)) 
          Utils.writeToLog("oracle.apps.fnd.login.AuthenticateUser", "Should raise signon notification alert: "+notMsg, wctx, Log.STATEMENT);

        out.println("<script>\n"
                    +"alert('"+notMsg+"');\n"
                    +"location='"+forwardUrl+"';\n"
                    +"</script>");

        if(((AppsLog)wctx.getLog()).isEnabled(AppsLog.STATEMENT))
          Utils.writeToLog("oracle.apps.fnd.login.AuthenticateUser", "After raise signon notification alert. ", wctx, Log.STATEMENT);
      }
      else
      {
        if(((AppsLog)wctx.getLog()).isEnabled(AppsLog.STATEMENT)) 
          Utils .writeToLog("oracle.apps.fnd.login.AuthenticateUser", "Not raising alert...simply redirect: "+forwardUrl, wctx, Log.STATEMENT);
        
        response.sendRedirect(forwardUrl);
      }
      if(((AppsLog)wctx.getLog()).isEnabled(AppsLog.STATEMENT)) 
        Utils.writeToLog("oracle.apps.fnd.login.AuthenticateUser", "forwardUrl after Runfunction check: "+forwardUrl, wctx, Log.STATEMENT);
    }
    catch(Exception e) 
    {
      try
      {
        conn.rollback();
      }
      catch (Exception ex)
      {
        if(((AppsLog)wctx.getLog()).isEnabled(AppsLog.EXCEPTION))
        Utils.writeToLog("oracle.apps.fnd.login.AutheticateUser", "Exception in closing connection:"+ex.toString(), wctx, Log.EXCEPTION);
      }
      if(((AppsLog)wctx.getLog()).isEnabled(AppsLog.EXCEPTION)) 
        Utils.writeToLog("oracle.apps.fnd.login.AuthenticateUser", "Exception in creating apps session:"+e.toString(), wctx, Log.EXCEPTION);
      
      throw new ServletException(Utils.getExceptionStackTrace(e));
    }
    finally
    {
      if(((AppsLog)wctx.getLog()).isEnabled(AppsLog.STATEMENT))
        Utils.writeToLog("oracle.apps.fnd.login.AuthenticateUser", "end of doPost, forwardUrl is: "+forwardUrl, wctx, Log.STATEMENT);

      /** if (conn != null) { Utils.releaseConnection(); } */
      if(alreadySet == false) Utils.releaseAppsContext();
    }
  }  
}