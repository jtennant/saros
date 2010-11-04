package de.fu_berlin.inf.dpp.stf.client.test.rolesAndFollowmode;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.InitMusician;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.SarosConstant;

public class TestChangingDriverWhileOtherFollow {
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String CP = BotConfiguration.CONTENTPATH;
    private static final String CP_CHANGE = BotConfiguration.CONTENTCHANGEPATH;

    private static Musician alice;
    private static Musician bob;
    private static Musician carl;
    private static Musician dave;

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Carl (Observer)</li>
     * <li>Dave (Observer)</li>
     * <li>All observers enable followmode</li>
     * </ol>
     * 
     * @throws AccessException
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void initMusican() throws AccessException, RemoteException,
        InterruptedException {
        /*
         * initialize the musicians simultaneously
         */
        List<Musician> musicians = InitMusician.initMusiciansConcurrently(
            BotConfiguration.PORT_ALICE, BotConfiguration.PORT_BOB,
            BotConfiguration.PORT_CARL, BotConfiguration.PORT_DAVE);
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);
        dave = musicians.get(3);
        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);

        /*
         * build session with bob, carl and dave simultaneously
         */
        alice.buildSessionConcurrently(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob, carl, dave);
        // alice.bot.waitUntilNoInvitationProgress();

        /*
         * bob, carl and dave follow alice.
         */
        alice.followedBy(bob, carl, dave);

    }

    /**
     * make sure, all opened xmppConnects, popup windows and editor should be
     * closed. make sure, all existed projects should be deleted.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.bot.resetSaros();
        carl.bot.resetSaros();
        dave.bot.resetSaros();
        alice.bot.resetSaros();
    }

    /**
     * make sure,all opened popup windows and editor should be closed.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.bot.resetWorkbench();
        carl.bot.resetWorkbench();
        dave.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    /**
     * Steps:
     * <ol>
     * <li>alice makes carl exclusive driver.</li>
     * <li>Observer are in follow mode.</li>
     * <li>carl opens a file and edit it.</li>
     * <li>Observers leave follow mode after they saw the opened file.</li>
     * <li>carl continue to edit the opened file, but doesn't save</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li></li>
     * <li></li>
     * <li>Observers saw the opened file and the dirty flag of the file,</li>
     * <li></li>
     * <li></li>
     * <li>Edited file is opened and not saved at every observer</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     * @throws InterruptedException
     */

    @Test
    public void testChanginDriverWhileOtherFollow() throws IOException,
        CoreException, InterruptedException {
        alice.bot.giveExclusiveDriverRole(carl.getPlainJid());
        alice.bot.waitUntilFollowed(carl.getPlainJid());
        bob.bot.waitUntilFollowed(carl.getPlainJid());
        dave.bot.waitUntilFollowed(carl.getPlainJid());

        carl.bot.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS);
        String dirtyClsContentOfCarl = carl.bot.getTextOfJavaEditor(PROJECT,
            PKG, CLS);

        alice.bot.waitUntilJavaEditorContentSame(dirtyClsContentOfCarl,
            PROJECT, PKG, CLS);
        assertTrue(alice.bot.isJavaEditorActive(CLS));
        assertTrue(alice.bot.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));

        bob.bot.waitUntilJavaEditorContentSame(dirtyClsContentOfCarl, PROJECT,
            PKG, CLS);
        assertTrue(bob.bot.isJavaEditorActive(CLS));
        assertTrue(bob.bot.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));

        dave.bot.waitUntilJavaEditorContentSame(dirtyClsContentOfCarl, PROJECT,
            PKG, CLS);
        assertTrue(dave.bot.isJavaEditorActive(CLS));
        assertTrue(dave.bot.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));

        carl.stopFollowedBy(alice, bob, dave);
        carl.bot.setTextInJavaEditorWithoutSave(CP_CHANGE, PROJECT, PKG, CLS);
        carl.bot.closeJavaEditorWithSave(CLS);
        String dirtyClsChangeContentOfCarl = carl.bot.getTextOfJavaEditor(
            PROJECT, PKG, CLS);

        assertTrue(alice.bot.isJavaEditorActive(CLS));
        /*
         * TODO alice can still see the changes maded by carl, although she
         * already leave follow mode. There is a bug here (see Bug 3094186)and
         * it should be fixed, so that asserts that the following condition is
         * false
         * 
         * 
         * With Saros Version 10.10.29.DEVEL.
         */
        // assertFalse(alice.bot.getTextOfJavaEditor(PROJECT, PKG, CLS).equals(
        // dirtyClsChangeContentOfCarl));

        assertTrue(bob.bot.isJavaEditorActive(CLS));

        /*
         * TODO bob can still see the changes maded by carl, although he already
         * leave follow mode. There is a bug here (see Bug 3094186) and it
         * should be fixed, so that asserts that the following condition is
         * false.
         * 
         * With Saros Version 10.10.29.DEVEL.
         */
        // assertFalse(bob.bot.getTextOfJavaEditor(PROJECT, PKG, CLS).equals(
        // dirtyClsChangeContentOfCarl));

        assertTrue(dave.bot.isJavaEditorActive(CLS));

        /*
         * TODO dave can still see the changes , although he already leave
         * follow mode. There is a bug here (see Bug 3094186) and it should be
         * fixed, so that asserts that the following condition is false.
         * 
         * With Saros Version 10.10.29.DEVEL.
         */
        // assertFalse(dave.bot.getTextOfJavaEditor(PROJECT, PKG, CLS).equals(
        // dirtyClsChangeContentOfCarl));

    }
}