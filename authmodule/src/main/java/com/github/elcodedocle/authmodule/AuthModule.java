package com.github.elcodedocle.authmodule;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthModule implements ServerAuthModule {
    // shamelessly stolen from https://blog.payara.fish/using-jaspic-to-secure-a-web-application-in-payara-server (with some pertinent changes)
    static final Logger logger = Logger.getLogger(AuthModule.class.getName());
    @SuppressWarnings("rawtypes")
    protected static final Class[] supportedMessageTypes = new Class[]{
            HttpServletRequest.class, HttpServletResponse.class};

    private CallbackHandler handler;

    @Override
    public void initialize(MessagePolicy requestPolicy,
                           MessagePolicy responsePolicy, CallbackHandler handler, Map options) {
        logger.log(Level.INFO, "initialize called.");
        this.handler = handler;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class[] getSupportedMessageTypes() {
        return supportedMessageTypes;
    }

    @Override
    public AuthStatus validateRequest(MessageInfo messageInfo,
                                      Subject clientSubject, Subject serviceSubject) {
        HttpServletRequest request =
                (HttpServletRequest) messageInfo.getRequestMessage();

        String user = request.getParameter("user");
        String group = request.getParameter("group");

        logger.log(Level.INFO, "validateRequest called.");
        logger.log(Level.INFO, "User = {0}", user);
        logger.log(Level.INFO, "Group = {0}", group);

        authenticateUser(user, group, clientSubject, serviceSubject);

        return AuthStatus.SUCCESS;
    }

    @Override
    public AuthStatus secureResponse(MessageInfo messageInfo,
                                     Subject serviceSubject) {
        return AuthStatus.SEND_SUCCESS;
    }

    @Override
    public void cleanSubject(MessageInfo messageInfo, Subject subject) {
        if (subject != null) {
            subject.getPrincipals().clear();
        }
    }

    private void authenticateUser(String user, String group,
                                  Subject clientSubject, Subject serverSubject) {
        logger.log(Level.INFO, "Authenticating user {0} in group {1}", new String[]{user, group});
        CallerPrincipalCallback callerPrincipalCallback =
                new CallerPrincipalCallback(clientSubject, user);

        GroupPrincipalCallback groupPrincipalCallback =
                new GroupPrincipalCallback(clientSubject, new String[]{group});

        try {
            handler.handle(new Callback[]{callerPrincipalCallback,
                    groupPrincipalCallback});
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
