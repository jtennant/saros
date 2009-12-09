package de.fu_berlin.inf.dpp.ui.wizards;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.internal.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.DiscoveryManager.CacheMissException;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.project.internal.SharedProject;
import de.fu_berlin.inf.dpp.util.Util;

public class InvitationWizardUserSelection extends WizardPage {

    private static final Logger log = Logger
        .getLogger(InvitationWizardUserSelection.class);

    protected Saros saros;
    protected Roster roster;
    protected RosterTracker rosterTracker;
    protected SharedProject sharedProject;
    protected IRosterListener rosterListener;
    protected DiscoveryManager discoveryManager;
    protected InvitationProcessObservable invitationProcesses;
    protected CheckboxTableViewer userListViewer;
    protected Table userListTable;
    protected Button onlySaros;
    protected SelectionListener onlySarosListener;
    protected ISelectionChangedListener userSelectionChangedListener;

    protected InvitationWizardUserSelection(Saros saros,
        SharedProject sharedProject, RosterTracker rosterTracker,
        DiscoveryManager discoveryManager,
        InvitationProcessObservable invitationProcesses) {
        super("Select users to invite");
        this.saros = saros;
        this.roster = saros.getRoster();
        this.sharedProject = sharedProject;
        this.rosterTracker = rosterTracker;
        this.discoveryManager = discoveryManager;
        this.invitationProcesses = invitationProcesses;
        setTitle("Pariticipant selection");
        setDescription("Select the users you would like to invite");
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label projectLabel = new Label(composite, SWT.NONE);
        projectLabel.setText("Project");

        Text projectName = new Text(composite, SWT.READ_ONLY | SWT.SINGLE
            | SWT.BORDER);
        projectName
            .setLayoutData(new GridData(SWT.FILL, SWT.LEFT, false, false));
        projectName.setText(sharedProject.getProject().getName());

        // An empty label to skip a grid.
        // TODO: what about garbage collection?
        new Label(composite, SWT.NONE);

        userListTable = new Table(composite, SWT.BORDER | SWT.CHECK | SWT.MULTI
            | SWT.FULL_SELECTION);

        TableColumn userColumn = new TableColumn(userListTable, SWT.NONE);
        userColumn.setWidth(250);
        userColumn.setText("User");

        TableColumn sarosEnabledColumn = new TableColumn(userListTable,
            SWT.CENTER);
        sarosEnabledColumn.setWidth(100);
        sarosEnabledColumn.setText("Saros support");

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.minimumHeight = 150;
        userListTable.setLayoutData(gd);
        userListTable.setHeaderVisible(true);
        userListViewer = new CheckboxTableViewer(userListTable);
        PresenceFilter presenceFilter = new PresenceFilter();
        SessionFilter sessionFilter = new SessionFilter();
        final SarosSupportFilter sarosFilter = new SarosSupportFilter();
        ViewerFilter[] filters = { presenceFilter, sessionFilter };
        userListViewer.setFilters(filters);

        rosterListener = getRosterListener(userListViewer);
        rosterTracker.addRosterListener(rosterListener);

        userListViewer.setLabelProvider(new UserListLabelProvider());
        userListViewer.setContentProvider(new ArrayContentProvider());

        userListViewer.setInput(roster.getEntries());

        // An empty label to skip a grid.
        // TODO: what about garbage collection?
        new Label(composite, SWT.NONE);

        // CheckBox to show only users with Saros support.
        onlySaros = new Button(composite, SWT.CHECK);
        onlySaros.setSelection(false);
        onlySaros.setText("Hide users without Saros support");
        onlySarosListener = new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }

            public void widgetSelected(SelectionEvent e) {
                if (((Button) e.widget).getSelection()) {
                    userListViewer.addFilter(sarosFilter);
                } else {
                    userListViewer.removeFilter(sarosFilter);
                }
            }
        };
        onlySaros.addSelectionListener(onlySarosListener);

        // Refresh the Finish button.
        userSelectionChangedListener = new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                getContainer().updateButtons();
            }
        };
        userListViewer
            .addSelectionChangedListener(userSelectionChangedListener);
        setControl(composite);
    }

    protected class UserListLabelProvider extends LabelProvider implements
        ITableLabelProvider {

        public Image getColumnImage(Object element, int columnIndex) {
            // there is no image for the columns
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            RosterEntry rosterEntry = (RosterEntry) element;
            final JID jid = new JID(rosterEntry.getUser());
            switch (columnIndex) {
            case 0:
                return rosterEntry.getUser();
            case 1:
                boolean supported;
                try {
                    supported = discoveryManager.isSupportedNonBlock(jid,
                        Saros.NAMESPACE);
                } catch (CacheMissException e) {
                    refreshSarosSupport(jid);
                    return "?";
                }
                return supported ? "Yes" : "No";
            default:
                return "default value";
            }
        }
    }

    /**
     * A filter which will only be passed if the user is available.
     */
    protected class PresenceFilter extends ViewerFilter {
        @Override
        public boolean select(Viewer viewer, Object parentElement,
            Object element) {

            RosterEntry rosterEntry = (RosterEntry) element;
            Presence presence = roster.getPresence(rosterEntry.getUser());

            if (presence.isAvailable())
                return true;
            return false;
        }
    }

    /**
     * A filter which will only be passed if the user has Saros support.
     */
    protected class SarosSupportFilter extends ViewerFilter {
        @Override
        public boolean select(Viewer viewer, Object parentElement,
            Object element) {

            RosterEntry rosterEntry = (RosterEntry) element;
            JID jid = new JID(rosterEntry.getUser());
            boolean sarosSupport;
            try {
                sarosSupport = discoveryManager.isSupportedNonBlock(jid,
                    Saros.NAMESPACE);
            } catch (CacheMissException e) {
                /**
                 * If no entry passes the filter, the viewer will be empty, and
                 * the discovery will never be triggered. So we have to trigger
                 * the discovery here.
                 */
                refreshSarosSupport(jid);
                sarosSupport = true;
            }
            return sarosSupport;
        }
    }

    /**
     * A filter which will only be passed if the user is not in the local
     * session yet and there is currently no invitation in progress.
     */
    protected class SessionFilter extends ViewerFilter {
        @Override
        public boolean select(Viewer viewer, Object parentElement,
            Object element) {

            RosterEntry rosterEntry = (RosterEntry) element;
            JID jid = new JID(rosterEntry.getUser());
            if (sharedProject.getResourceQualifiedJID(jid) != null)
                return false;
            if (invitationProcesses.getInvitationProcess(jid) != null)
                return false;
            return true;
        }
    }

    protected IRosterListener getRosterListener(
        final CheckboxTableViewer userListViewer) {
        return new IRosterListener() {
            public void rosterChanged(Roster roster) {
                refreshUserList();
            }

            public void entriesAdded(Collection<String> addresses) {
                refreshUserList();
            }

            public void entriesDeleted(Collection<String> addresses) {
                refreshUserList();
            }

            public void entriesUpdated(Collection<String> addresses) {
                refreshUserList();
            }

            public void presenceChanged(Presence presence) {
                refreshUserList();
            }
        };
    }

    @Override
    public void dispose() {
        rosterTracker.removeRosterListener(rosterListener);
        onlySaros.removeSelectionListener(onlySarosListener);
        super.dispose();
    }

    /**
     * Triggers the DiscoveryManager to discover Saros supportance and refreshes
     * the {@link #userListViewer}.
     * 
     * @param jid
     *            The JID of the user whose saros support should be discovered.
     */
    protected void refreshSarosSupport(final JID jid) {
        Util.runSafeAsync(log, new Runnable() {
            public void run() {
                boolean supported = discoveryManager.isSarosSupported(jid);
                log.debug("discovered: " + supported);
                refreshUserList();
            }
        });
    }

    protected void refreshUserList() {
        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                userListViewer.refresh();
            }
        });
    }

    public ArrayList<JID> getSelectedUsers() {
        ArrayList<JID> selectedUsers = new ArrayList<JID>();
        JID jid;
        for (Object element : userListViewer.getCheckedElements()) {
            jid = new JID(((RosterEntry) element).getUser());
            selectedUsers.add(jid);
        }
        return selectedUsers;
    }
}