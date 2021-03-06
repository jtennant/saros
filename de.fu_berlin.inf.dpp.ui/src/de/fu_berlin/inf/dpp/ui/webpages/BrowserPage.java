package de.fu_berlin.inf.dpp.ui.webpages;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.ui.renderer.Renderer;

import java.util.List;

/**
 * A browser page encapsulates the location of the HTML page as well as the needed
 * browsers functions and renderers.
 * The browser functions are the Java methods that the webpage calls inside Javascript.
 * The renderers transfer application state from Java to the webpage.
 *
 * @JTourBusStop 2, Extending the HTML GUI, Creating a Java abstraction for a page:
  *
  *              Each webpage in Saros has a corresponding implementation of this
 *               interface. There is one for the Saros main view and one for each
 *               dialog. So if you add a new .html file, you should add a suitable
 *               BrowserPage implementation as well.
 *
 *               The BrowserPage encapsulates the location of the .html file,
 *               which is the relative location inside the resource folder.
 *
 *               It further creates the list of renderers and browser functions
 *               needed for this webpage.
 *
 */
public interface BrowserPage {

    /**
     * Returns the resource name of this <code>BowserPage</code> or <code>null</code> if there is no resource associated with this page.
     * <p/>
     * E.g: html/index.html
     * <p/>
     * It is up to the caller to resolve the absolute physical location.
     *
     * @return the resource name or <code>null</code>
     * @see ClassLoader#getResource(String name)
     */
    String getWebpage();

    /**
     * Creates the needed {@link org.eclipse.swt.browser.BrowserFunction}s for
     * the webpage.
     */
    List<JavascriptFunction> getJavascriptFunctions();

    /**
     * Gets the list of renderers that can display application state in this
     * webpage.
     *
     * @return the list of renderers for this page
     */
    List<Renderer> getRenderer();
}
