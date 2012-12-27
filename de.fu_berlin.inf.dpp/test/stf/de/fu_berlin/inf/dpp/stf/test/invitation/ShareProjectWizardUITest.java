package de.fu_berlin.inf.dpp.stf.test.invitation;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.MENU_SAROS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ResourceSelectionComposite_delete_dialog_title;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ResourceSelectionComposite_overwrite_dialog_title;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHARE_PROJECTS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SHARE_PROJECT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.YES;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.Random;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.test.Constants;

/*
 * 1. open the invitation wizard
 * 2. make a selection
 * 3. save a selection
 * 4. undo all selections  until no undo is possible any longer
 * 5. redo all selections until no redo possible
 * 6. undo again
 * 7. restore saved selection.
 * 8. check if selection is restored successfully
 * 
 */
public class ShareProjectWizardUITest extends StfTestCase {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * </ol>
     * 
     */

    private static final int UNDO_BUTTON = 0;
    private static final int REDO_BUTTON = 1;
    private static final int RESTORE_SELECTION_BUTTON = 3;
    private static final int SAVE_SELECTION_BUTTON = 4;
    private static final int DELETE_SELECTION_BUTTON = 5;

    private static final Random RANDOM = new Random();

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE);
    }

    @Before
    public void tidyUp() throws Exception {
        closeAllShells();
        closeAllEditors();
        clearWorkspaces();

        // dummy project
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .newC()
            .javaProjectWithClasses(Constants.PROJECT2, Constants.PKG1,
                Constants.CLS1);

        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .newC()
            .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1,
                Constants.CLS1);
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .cls(Constants.PROJECT1, Constants.PKG1, Constants.CLS2);
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .cls(Constants.PROJECT1, Constants.PKG1, Constants.CLS3);
    }

    protected IRemoteBotTreeItem getClass(String name) throws RemoteException {
        IRemoteBotTreeItem projectTreeItem = ALICE.remoteBot().tree()
            .expandNode(Constants.PROJECT1, true);
        IRemoteBotTreeItem srcFolder = projectTreeItem.getNode("src");
        IRemoteBotTreeItem myPackage = srcFolder.getNode("my");
        IRemoteBotTreeItem pkgPackage = myPackage.getNode("pkg");

        IRemoteBotTreeItem cls = pkgPackage.getNode(name);
        return cls;
    }

    @Test
    public void testShareProjectsUndoRedo() throws Exception {
        ALICE.remoteBot().activateWorkbench();
        ALICE.remoteBot().menu(MENU_SAROS).menu(SHARE_PROJECTS).click();
        Thread.sleep(500);
        ALICE.remoteBot().shell(SHELL_SHARE_PROJECT).activate();

        assertNoneChecked();

        // select 3 classes in project 1.
        getClass(Constants.CLS1_SUFFIX).check();
        getClass(Constants.CLS2_SUFFIX).check();
        getClass(Constants.CLS3_SUFFIX).check();
        assertAllChecked();

        Thread.sleep(500);

        undoAll();
        assertNoneChecked();

        redoAll();
        assertAllChecked();
    }

    @Test
    public void testShareProjectsRestoreSelection() throws Exception {
        ALICE.remoteBot().activateWorkbench();
        ALICE.remoteBot().menu(MENU_SAROS).menu(SHARE_PROJECTS).click();
        Thread.sleep(500);
        ALICE.remoteBot().shell(SHELL_SHARE_PROJECT).activate();

        // unselect all..
        ALICE.remoteBot().shell(SHELL_SHARE_PROJECT).bot().tree().unselect();
        assertNoneChecked();

        // select 3 classes in project 1.
        getClass(Constants.CLS1_SUFFIX).check();
        getClass(Constants.CLS2_SUFFIX).check();
        getClass(Constants.CLS3_SUFFIX).check();
        assertAllChecked();

        Thread.sleep(500);

        String selectionName = "test";
        Long.toHexString(RANDOM.nextLong());

        // Store a selection
        storeSelection(selectionName);
        assertAllChecked();
        Thread.sleep(1000);

        // Remove it
        removeSelection(selectionName);

        // Store again
        storeSelection(selectionName);

        // unselect all items...
        getClass(Constants.CLS1_SUFFIX).uncheck();
        getClass(Constants.CLS2_SUFFIX).uncheck();
        getClass(Constants.CLS3_SUFFIX).uncheck();
        Thread.sleep(5000);
        assertNoneChecked();

        // restore selection
        restoreSelection(selectionName);
        // all items should be checked again now...
        assertAllChecked();

        // Remove it
        removeSelection(selectionName);

        // done.
    }

    private void refocusWizard() throws Exception {
        // refocus after balloon notification
        Thread.sleep(1000);
        ALICE.remoteBot().shell(SHELL_SHARE_PROJECT).activate();
        ALICE.remoteBot().shell(SHELL_SHARE_PROJECT).waitUntilActive();
    }

    private void removeSelection(String name) throws Exception {
        ALICE.remoteBot().shell(SHELL_SHARE_PROJECT).bot().comboBox(0)
            .setText(name);
        ALICE.remoteBot().shell(SHELL_SHARE_PROJECT).bot()
            .button(DELETE_SELECTION_BUTTON).click();

        Thread.sleep(500);

        // confirm deleting
        ALICE.remoteBot().shell(ResourceSelectionComposite_delete_dialog_title)
            .bot().button(YES).click();
        refocusWizard();
        assertTrue(
            "Could not remove selection ",
            ArrayUtils.indexOf(ALICE.remoteBot().shell(SHELL_SHARE_PROJECT)
                .bot().comboBox(0).items(), name) == ArrayUtils.INDEX_NOT_FOUND);
    }

    private void restoreSelection(String name) throws Exception {
        ALICE.remoteBot().shell(SHELL_SHARE_PROJECT).bot().comboBox(0)
            .setText(name);
        ALICE.remoteBot().shell(SHELL_SHARE_PROJECT).bot()
            .button(RESTORE_SELECTION_BUTTON).click();

        Thread.sleep(1000);

        refocusWizard();
    }

    private void storeSelection(String name) throws Exception {
        ALICE.remoteBot().shell(SHELL_SHARE_PROJECT).bot().comboBox(0)
            .setText(name);
        ALICE.remoteBot().shell(SHELL_SHARE_PROJECT).bot()
            .button(SAVE_SELECTION_BUTTON).click();

        Thread.sleep(500);

        // optionally confirm overwriting
        if (ALICE.remoteBot().isShellOpen(
            ResourceSelectionComposite_overwrite_dialog_title)) {
            ALICE.remoteBot()
                .shell(ResourceSelectionComposite_overwrite_dialog_title).bot()
                .button(YES).click();
        }

        refocusWizard();

        assertTrue(
            "Could not store selection",
            ArrayUtils.indexOf(ALICE.remoteBot().shell(SHELL_SHARE_PROJECT)
                .bot().comboBox(0).items(), name) != ArrayUtils.INDEX_NOT_FOUND);
    }

    protected void undoAll() throws RemoteException {
        while (ALICE.remoteBot().shell(SHELL_SHARE_PROJECT).bot()
            .button(UNDO_BUTTON).isEnabled()) {
            pressUndo();
        }
    }

    protected void redoAll() throws RemoteException {
        while (ALICE.remoteBot().shell(SHELL_SHARE_PROJECT).bot()
            .button(REDO_BUTTON).isEnabled()) {
            pressRedo();
        }
    }

    protected void pressUndo() throws RemoteException {
        ALICE.remoteBot().shell(SHELL_SHARE_PROJECT).bot().button(UNDO_BUTTON)
            .click();
    }

    protected void pressRedo() throws RemoteException {
        ALICE.remoteBot().shell(SHELL_SHARE_PROJECT).bot().button(REDO_BUTTON)
            .click();
    }

    protected void pressDeleteSelection() throws RemoteException {
        ALICE.remoteBot().shell(SHELL_SHARE_PROJECT).bot()
            .button(DELETE_SELECTION_BUTTON).click();
    }

    protected void assertAllChecked() throws RemoteException {
        assertTrue(Constants.CLS1_SUFFIX + " is checked",
            getClass(Constants.CLS1_SUFFIX).isChecked());
        assertTrue(Constants.CLS2_SUFFIX + " is checked",
            getClass(Constants.CLS2_SUFFIX).isChecked());
        assertTrue(Constants.CLS3_SUFFIX + " is checked",
            getClass(Constants.CLS3_SUFFIX).isChecked());
    }

    protected void assertNoneChecked() throws RemoteException {
        assertTrue(Constants.CLS1_SUFFIX + " is  checked",
            !getClass(Constants.CLS1_SUFFIX).isChecked());
        assertTrue(Constants.CLS2_SUFFIX + " is checked",
            !getClass(Constants.CLS2_SUFFIX).isChecked());
        assertTrue(Constants.CLS3_SUFFIX + " is checked",
            !getClass(Constants.CLS3_SUFFIX).isChecked());
    }

}