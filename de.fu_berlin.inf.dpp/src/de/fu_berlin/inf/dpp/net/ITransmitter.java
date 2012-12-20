/*
 * DPP - Serious Distributed Pair Programming (c) Freie Universität Berlin -
 * Fachbereich Mathematik und Informatik - 2006 (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 1, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 */

package de.fu_berlin.inf.dpp.net;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jivesoftware.smack.packet.IQ;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.serializable.FileActivityDataObject;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.invitation.InvitationProcess;
import de.fu_berlin.inf.dpp.net.internal.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationInfo;
import de.fu_berlin.inf.dpp.net.internal.extensions.XStreamExtensionProvider;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * A humble interface that is responsible for network functionality. The idea
 * behind this interface is to only encapsulates the least possible amount of
 * functionality - the one that can't be easily tested.
 * 
 * @author rdjemili
 */
@Component(module = "net")
public interface ITransmitter {

    /* ---------- invitations --------- */

    /**
     * Sends a cancellation message that tells the receiver that the invitation
     * is canceled.
     * 
     * @param jid
     *            the JID of the recipient.
     * @param errorMsg
     *            the reason why the invitation was canceled or
     *            <code>null</code>.
     */
    public void sendCancelInvitationMessage(JID jid, String errorMsg);

    /* ---------- files --------- */

    /**
     * Sends given file list to given XMPP user. This methods blocks until the
     * file transfer is done or failed.
     * 
     * @param jid
     *            the JID of the user to which the file list is to be sent.
     * 
     * @param fileLists
     *            the file lists that are to be sent.
     * 
     * 
     * @throws IOException
     *             if the operation fails because of a problem with the XMPP
     *             Connection.
     */
    public void sendFileLists(JID jid, String processID,
        List<FileList> fileLists) throws IOException;

    // FIXME Add Javadoc. Why is an invitationID needed?
    public void sendUserList(JID to, String invitationID, Collection<User> user);

    public boolean receiveUserListConfirmation(SarosPacketCollector collector,
        List<User> fromUsers, IProgressMonitor monitor)
        throws LocalCancellationException;

    /**
     * Sends a leave message to the participants of given Saros session. See
     * {@link InvitationProcess} for more information when this is supposed be
     * sent.
     * 
     * @param sarosSession
     *            the Saros session that this join message refers to.
     */
    public void sendLeaveMessage(ISarosSession sarosSession);

    /**
     * Sends given list of TimedActivities to the given recipient.
     * 
     * This list MUST not contain any {@link FileActivityDataObject}s where
     * {@link FileActivityDataObject#getType()} ==
     * {@link FileActivity.Type#Created} as binary data is not supported in
     * messages bodies.
     * 
     * TODO: Add progress.
     * 
     * @param recipient
     *            The JID of the user who is to receive the given list of timed
     *            activityDataObjects.
     * @param timedActivities
     *            The list of timed activityDataObjects to send to the user.
     * 
     * @throws IllegalArgumentException
     *             if the recipient is null, the recipient equals the local user
     *             as returned by {@link SarosNet#getMyJID()} or the given list
     *             is null or contains no activityDataObjects.
     * 
     * @throws AssertionError
     *             if the given list of timed activityDataObjects contains
     *             FileActivities of type created AND the application is run
     *             using asserts.
     */
    public void sendTimedActivities(JID recipient,
        List<TimedActivityDataObject> timedActivities);

    /**
     * Sends a query, a {@link IQ.Type} GET, to the user with given {@link JID}.
     * 
     * Example using provider:
     * <p>
     * <code>XStreamExtensionProvider<VersionInfo> versionProvider = new
     * XStreamExtensionProvider<VersionInfo>( "sarosVersion", VersionInfo.class,
     * Version.class, Compatibility.class);<br>
     * sendQuery(jid, versionProvider, 5000);
     * </code>
     * </p>
     * In this example this sends a request to the user with jid and waits 5
     * seconds for an answer. If it arrives in time, a payload of type T (in
     * this case VersionInfo) will be returned, else the result is null.
     */
    public <T> T sendQuery(JID jid, XStreamExtensionProvider<T> provider,
        T payload, long timeout);

    /**
     * @param to
     *            peer that invited this user and where to send the
     *            acknowledgment to
     * @param invitationID
     *            the ID of the invitation
     */
    public void sendInvitationAcknowledgement(JID to, String invitationID);

    public SarosPacketCollector getUserListConfirmationCollector();

    public void sendInvitationCompleteConfirmation(JID to, String invitationID);

    public void sendCancelSharingProjectMessage(JID peer, String errorMsg);

    public void sendCancelInvitationMessage(JID to, String sessionID,
        String message);

    public void sendUserListRequest(JID peer, String invitationID);

    public void sendInvitation(JID peer, InvitationInfo invInfo);
}
