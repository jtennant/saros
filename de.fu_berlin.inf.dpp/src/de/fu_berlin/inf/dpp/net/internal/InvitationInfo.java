/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.net.internal;

import org.joda.time.DateTime;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

@Component(module = "net")
public class InvitationInfo extends DefaultInvitationInfo {
    public String projectName;
    public String projectDesc;
    public int colorID;
    public VersionInfo versionInfo;
    public DateTime sessionStart;

    public InvitationInfo(SessionIDObservable sessionID, String invitationID,
        String projectName, String projectDesc, int colorID,
        VersionInfo versionInfo, DateTime sessionStart) {
        super(sessionID, invitationID);
        this.projectName = projectName;
        this.projectDesc = projectDesc;
        this.colorID = colorID;
        this.versionInfo = versionInfo;
        this.sessionStart = sessionStart;
    }

    public static class InvitationExtensionProvider extends
        XStreamExtensionProvider<InvitationInfo> {

        public InvitationExtensionProvider() {
            super("invitation", InvitationInfo.class);
        }
    }
}