package de.fu_berlin.inf.dpp.ui.webpages;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.ui.browser_functions.SarosMainPageBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.ContactListRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.Renderer;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the Saros main view.
 */
public class SarosMainPage implements BrowserPage {

    private final AccountRenderer accountRenderer;

    private final ContactListRenderer contactListRenderer;

    private final SarosMainPageBrowserFunctions browserFunctions;

    public SarosMainPage(AccountRenderer accountRenderer,
        ContactListRenderer contactListRenderer,
        SarosMainPageBrowserFunctions browserFunctions) {
        this.accountRenderer = accountRenderer;
        this.contactListRenderer = contactListRenderer;
        this.browserFunctions = browserFunctions;
    }

    @Override
    public String getWebpage() {
        return "html/saros-angular.html";
    }

    @Override
    public List<JavascriptFunction> getJavascriptFunctions() {
        return  browserFunctions.getJavascriptFunctions();
    }

    @Override
    public List<Renderer> getRenderer() {
        return Arrays.asList(accountRenderer, contactListRenderer);
    }

}
