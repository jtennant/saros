package de.fu_berlin.inf.dpp.ui.browser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.ui.ide_embedding.BrowserCreator;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import de.fu_berlin.inf.dpp.ui.ide_embedding.IBrowserDialog;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.webpages.BrowserPage;

/**
 * Eclipse side implementation of the IDialogManager interface
 */
public class EclipseDialogManager extends DialogManager {

    private final BrowserCreator browserCreator;

    public EclipseDialogManager(BrowserCreator browserCreator,
        UISynchronizer uiSynchronizer) {
        super(uiSynchronizer);
        this.browserCreator = browserCreator;
    }

    @Override
    protected IBrowserDialog createDialog(final BrowserPage browserPage) {
        final Shell activeShell = SWTUtils.getShell();

        final Shell browserShell = new Shell(activeShell);

        // TODO WebPage#getTitle() ?
        browserShell.setText(browserPage.getWebpage());
        browserShell.setLayout(new FillLayout());
        browserShell.setSize(640, 480);

        browserCreator.createBrowser(browserShell, SWT.NONE, browserPage);

        browserShell.addShellListener(new ShellAdapter() {

            @Override
            public void shellClosed(ShellEvent e) {
                removeDialogEntry(browserPage.getWebpage());
            }

        });

        browserShell.open();
        browserShell.pack();

        return new EclipseBrowserDialog(browserShell);
    }
}
