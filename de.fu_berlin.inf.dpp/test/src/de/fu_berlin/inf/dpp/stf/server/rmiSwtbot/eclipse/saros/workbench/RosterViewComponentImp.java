package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosStateImp;
import de.fu_berlin.inf.dpp.ui.RosterView;

/**
 * This implementation of {@link RosterViewComponent}
 * 
 * @author Lin
 */
public class RosterViewComponentImp extends EclipseComponent implements
    RosterViewComponent {

    // public static RosterViewObjectImp classVariable;

    private static transient RosterViewComponentImp self;

    /*
     * View infos
     */
    private final static String VIEWNAME = SarosConstant.VIEW_TITLE_ROSTER;
    private final static String VIEWID = SarosConstant.ID_ROSTER_VIEW;

    /*
     * title of shells which are pop up by performing the actions on the session
     * view.
     */
    private final static String CONTACTALREADYADDED = "Contact already added";
    private final static String CREATEXMPPACCOUNT = SarosConstant.SHELL_TITLE_CREATE_XMPP_ACCOUNT;
    private final static String NEWCONTACT = SarosConstant.SHELL_TITLE_NEW_CONTACT;
    private final static String CONFIRMDELETE = SarosConstant.SHELL_TITLE_CONFIRM_DELETE;
    private final static String CONTACTLOOKUPFAILED = "Contact look-up failed";
    private final static String REMOVELOFSUBSCRIPTION = SarosConstant.SHELL_TITLE_REMOVAL_OF_SUBSCRIPTION;

    /*
     * Tool tip text of toolbar buttons on the session view
     */
    private final static String DISCONNECT = SarosConstant.TOOL_TIP_TEXT_DISCONNECT;
    private final static String ADDANEWCONTACT = SarosConstant.TOOL_TIP_TEXT_ADD_A_NEW_CONTACT;
    private final static String CONNECT = SarosConstant.TOOL_TIP_TEXT_CONNECT;

    // Context menu of the table on the view
    private final static String DELETE = SarosConstant.CONTEXT_MENU_DELETE;
    private final static String RENAME = "Rename...";
    private final static String SKYPETHISUSER = "Skype this user";
    private final static String INVITEUSER = "Invite user...";
    private final static String TESTDATATRANSFER = "Test data transfer connection...";

    private final static String BUDDIES = "Buddies";

    private final static String SERVER = SarosConstant.TEXT_LABEL_JABBER_SERVER;
    private final static String USERNAME = SarosConstant.TEXT_LABEL_USER_NAME;
    private final static String PASSWORD = SarosConstant.TEXT_LABEL_PASSWORD;
    private final static String JABBERID = SarosConstant.TEXT_LABEL_JABBER_ID;
    private final static String CONFIRM = "Confirm:";

    /**
     * {@link RosterViewComponentImp} is a singleton, but inheritance is
     * possible.
     */
    public static RosterViewComponentImp getInstance() {
        if (self != null)
            return self;
        self = new RosterViewComponentImp();
        return self;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    public void openRosterView() throws RemoteException {
        if (!isRosterViewOpen())
            viewPart.openViewById(VIEWID);
    }

    public boolean isRosterViewOpen() throws RemoteException {
        return viewPart.isViewOpen(VIEWNAME);
    }

    public void setFocusOnRosterView() throws RemoteException {
        viewPart.setFocusOnViewByTitle(VIEWNAME);
    }

    public void closeRosterView() throws RemoteException {
        viewPart.closeViewById(VIEWID);
    }

    public void disconnect() throws RemoteException {
        precondition();
        if (isConnected()) {
            clickToolbarButtonWithTooltip(DISCONNECT);
            waitUntilIsDisConnected();
        }
    }

    public SWTBotTreeItem selectBuddy(String baseJID) throws RemoteException {
        return viewPart.selectTreeWithLabelsInView(VIEWNAME, BUDDIES, baseJID);
    }

    public boolean isBuddyExist(String baseJID) throws RemoteException {
        precondition();
        SWTBotTree tree = viewPart.getTreeInView(VIEWNAME);
        return treePart
            .isTreeItemWithMatchTextExist(tree, BUDDIES, baseJID + ".*");
    }

    public boolean isConnectedGUI() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(DISCONNECT);
    }

    /**
     * This method returns true if {@link SarosStateImp} and the GUI
     * {@link RosterView} having the connected state.
     */
    public boolean isConnected() throws RemoteException {
        return state.isConnected() && isConnectedGUI();
    }

    public void clickAddANewContactToolbarButton() throws RemoteException {
        precondition();
        clickToolbarButtonWithTooltip(ADDANEWCONTACT);
    }

    public void waitUntilIsConnected() throws RemoteException {
        waitUntil(SarosConditions.isConnect(getToolbarButtons(), DISCONNECT));
    }

    public void waitUntilIsDisConnected() throws RemoteException {
        waitUntil(SarosConditions.isDisConnected(getToolbarButtons(), CONNECT));
    }

    public void addANewContact(JID jid) throws RemoteException {
        if (!hasContactWith(jid)) {
            clickAddANewContactToolbarButton();
            confirmNewContactWindow(jid.getBase());
        }
    }

    public void confirmNewContactWindow(String baseJID) {
        windowPart.waitUntilShellActive(NEWCONTACT);
        basicPart.setTextInTextWithLabel(baseJID, JABBERID);
        basicPart.waitUntilButtonIsEnabled(FINISH);
        basicPart.clickButton(FINISH);
    }

    public boolean hasContactWith(JID jid) throws RemoteException {
        return state.hasContactWith(jid) && isBuddyExist(jid.getBase());
    }

    /**
     * Remove given contact from Roster, if contact was added before.
     */
    public void deleteContact(JID jid) throws RemoteException {
        if (!hasContactWith(jid))
            return;
        try {
            clickContextMenuOfBuddy(DELETE, jid.getBase());
            confirmDeleteWindow();
        } catch (WidgetNotFoundException e) {
            log.info("Contact not found: " + jid.getBase(), e);
        }
    }

    public void clickContextMenuOfBuddy(String context, String baseJID)
        throws RemoteException {
        viewPart.clickContextMenuOfTreeInView(VIEWNAME, DELETE, BUDDIES, baseJID);
    }

    public void confirmDeleteWindow() throws RemoteException {
        windowPart.waitUntilShellActive(CONFIRMDELETE);
        windowPart.confirmWindow(CONFIRMDELETE, YES);
    }

    public void confirmContactLookupFailedWindow(String buttonType)
        throws RemoteException {
        windowPart.confirmWindow(CONTACTLOOKUPFAILED, buttonType);
    }

    public boolean isWindowContactLookupFailedActive() throws RemoteException {
        return windowPart.isShellActive(CONTACTLOOKUPFAILED);
    }

    public boolean isWindowContactAlreadyAddedActive() throws RemoteException {
        return windowPart.isShellActive(CONTACTALREADYADDED);
    }

    public void renameContact(String contact, String newName)
        throws RemoteException {
        SWTBotTree tree = viewPart.getTreeInView(VIEWNAME);
        SWTBotTreeItem item = treePart.getTreeItemWithMatchText(tree, BUDDIES
            + ".*", contact + ".*");
        item.contextMenu(RENAME).click();
        windowPart.waitUntilShellActive("Set new nickname");
        bot.text(contact).setText(newName);
        bot.button(OK).click();
    }

    public void connect(JID jid, String password) throws RemoteException {
        precondition();
        log.trace("connectedByXMPP");
        if (!isConnected()) {
            log.trace("clickTBConnectInRosterView");
            clickToolbarButtonWithTooltip(CONNECT);
            bot.sleep(100);
            if (isCreateXMPPAccountWindowActive()) {
                log.trace("confirmSarosConfigurationWindow");
                confirmSarosConfigurationWizard(jid.getDomain(), jid.getName(),
                    password);
            }
            waitUntilIsConnected();
        }
    }

    public boolean isCreateXMPPAccountWindowActive() throws RemoteException {
        return windowPart.isShellActive(CREATEXMPPACCOUNT);
    }

    /**
     * Fill up the configuration wizard with title "Saros Configuration".
     */
    public void confirmSarosConfigurationWizard(String xmppServer, String jid,
        String password) {
        windowPart.activateShellWithText(CREATEXMPPACCOUNT);
        basicPart.setTextInTextWithLabel(xmppServer, SERVER);
        bot.sleep(sleepTime);
        basicPart.setTextInTextWithLabel(jid, USERNAME);
        bot.sleep(sleepTime);
        basicPart.setTextInTextWithLabel(password, PASSWORD);
        basicPart.setTextInTextWithLabel(password, CONFIRM);
        basicPart.clickButton(FINISH);
    }

    public void confirmRemovelOfSubscriptionWindow() throws RemoteException {
        windowPart.waitUntilShellActive(REMOVELOFSUBSCRIPTION);
        windowPart.confirmWindow(REMOVELOFSUBSCRIPTION, OK);
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    /**
     * 
     * Define the precondition which should be guaranteed when you want to
     * perform actions within the roster view.
     * 
     * @throws RemoteException
     */
    @Override
    protected void precondition() throws RemoteException {
        openRosterView();
        setFocusOnRosterView();
    }

    public void waitUntilContactLookupFailedIsActive() throws RemoteException {
        windowPart.waitUntilShellActive(CONTACTLOOKUPFAILED);
    }

    public void waitUntilWindowContactAlreadyAddedIsActive()
        throws RemoteException {
        windowPart.waitUntilShellActive(CONTACTALREADYADDED);
    }

    public void closeWindowContactAlreadyAdded() throws RemoteException {
        windowPart.closeShell(CONTACTALREADYADDED);
    }

    public void confirmRequestOfSubscriptionReceivedWindow()
        throws RemoteException {
        windowPart
            .waitUntilShellActive(SarosConstant.SHELL_TITLE_REQUEST_OF_SUBSCRIPTION_RECEIVED);
        windowPart.confirmWindow(
            SarosConstant.SHELL_TITLE_REQUEST_OF_SUBSCRIPTION_RECEIVED,
            SarosConstant.BUTTON_OK);
    }

    protected boolean isToolbarButtonEnabled(String tooltip) {
        return viewPart.isToolbarInViewEnabled(VIEWNAME, tooltip);
    }

    protected void clickToolbarButtonWithTooltip(String tooltipText) {
        viewPart.clickToolbarButtonWithTooltipInView(VIEWNAME, tooltipText);
    }

    protected List<SWTBotToolbarButton> getToolbarButtons() {
        return viewPart.getToolbarButtonsOnView(VIEWNAME);
    }

}