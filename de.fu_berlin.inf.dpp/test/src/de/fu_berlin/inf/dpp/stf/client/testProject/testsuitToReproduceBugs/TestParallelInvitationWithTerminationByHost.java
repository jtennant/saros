package de.fu_berlin.inf.dpp.stf.client.testProject.testsuitToReproduceBugs;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestParallelInvitationWithTerminationByHost extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Carl (Read-Only Access)</li>
     * <li>Dave (Read-Only Access)</li>
     * </ol>
     * 
     * @throws AccessException
     * @throws RemoteException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL,
            TypeOfTester.DAVE);
        setUpWorkbench();
        setUpSaros();
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice invites everyone else simultaneously.</li>
     * <li>Alice opens the Progress View and cancels Bob's invitation before Bob
     * accepts.</li>
     * <li>Carl accepts the invitation but does not choose a target project.</li>
     * <li>Alice opens the Progress View and cancels Carl's invitation before
     * Carl accepts</li>
     * <li>Dave accepts the invitation and chooses a target project.</li>
     * <li>Alice opens the Progress View and cancels Dave 's invitation during
     * the synchronisation.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li></li>
     * <li>Bob is notified of Alice's canceling the invitation.</li>
     * <li></li>
     * <li>Carl is notified of Alice's canceling the invitation.</li>
     * <li></li>
     * <li>Dave is notified of Alice's canceling the invitation.</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void parallelInvitationWithTerminationByHost() throws IOException,
        CoreException, InterruptedException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, PKG1, CLS1);

        /*
         * build session with bob, carl and dave simultaneously
         */
        alice
            .superBot()
            .views()
            .sarosView()
            .selectNoSessionRunning()
            .shareProjects(PROJECT1, bob.getJID(), dave.getJID(), carl.getJID());
        bob.bot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        bob.bot().shell(SHELL_SESSION_INVITATION).activate();
        alice.superBot().views().progressView().removeProcess(0);
        bob.bot().waitLongUntilShellIsOpen(SHELL_INVITATION_CANCELLED);
        bob.bot().shell(SHELL_INVITATION_CANCELLED).activate();
        bob.bot().shell(SHELL_INVITATION_CANCELLED).close();

        carl.bot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        carl.bot().shell(SHELL_SESSION_INVITATION).activate();
        carl.bot().shell(SHELL_SESSION_INVITATION).confirm(FINISH);
        alice.superBot().views().progressView().removeProcess(1);
        carl.bot().waitLongUntilShellIsOpen(SHELL_INVITATION_CANCELLED);
        assertTrue(carl.bot().shell(SHELL_INVITATION_CANCELLED).isActive());

        carl.bot().shell(SHELL_INVITATION_CANCELLED).close();

        dave.bot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        dave.bot().shell(SHELL_SESSION_INVITATION).activate();
        dave.bot().shell(SHELL_SESSION_INVITATION).confirm(FINISH);

        // dave.button.clickButton(FINISH);
        alice.superBot().views().progressView().removeProcess(3);
        // FIXME Timeout exception by MAC OS X, the building session under
        // MAS
        // is so fast that the session process is already done after
        // canceling
        // this process, so dave should never get the window
        // "Invitation canceled".
        dave.bot().waitLongUntilShellIsOpen(SHELL_INVITATION_CANCELLED);
        assertTrue(dave.bot().shell(SHELL_INVITATION_CANCELLED).isActive());

        dave.bot().shell(SHELL_INVITATION_CANCELLED).close();
    }
}