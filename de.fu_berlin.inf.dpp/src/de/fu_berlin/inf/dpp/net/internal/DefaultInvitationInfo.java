package de.fu_berlin.inf.dpp.net.internal;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

@Component(module = "net")
public class DefaultInvitationInfo extends DefaultSessionInfo {
    public String invitationID;

    public DefaultInvitationInfo(SessionIDObservable sessionID,
        String invitationID) {
        super(sessionID);
        this.invitationID = invitationID;
    }

    /*
     * Simple provider classes which do not have any special information
     * content, but are based on the DefaultInvitationInfo e.g. simple requests
     */

    public static class FileListRequestExtensionProvider extends
        XStreamExtensionProvider<DefaultInvitationInfo> {
        public FileListRequestExtensionProvider() {
            super("fileListRequest", DefaultInvitationInfo.class);
        }
    }

    public static class InvitationCompleteExtensionProvider extends
        XStreamExtensionProvider<DefaultInvitationInfo> {
        public InvitationCompleteExtensionProvider() {
            super("invitationComplete", String.class);
        }
    }
}