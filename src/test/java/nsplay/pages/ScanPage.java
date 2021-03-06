package nsplay.pages;

import functional.tests.core.mobile.basepage.BasePage;
import functional.tests.core.mobile.element.UIElement;
import org.testng.Assert;

public class ScanPage extends BasePage {

    public ScanPage() {
        super();
        UIElement browse = wait.waitForVisible(locators.findByTextLocator("Scan", true));
        Assert.assertNotNull(browse, "Scan page not loaded!");
        log.info("Scan page loaded.");
    }

    /**
     * Verify home page loaded.
     */
    public void navigate(String button) {
        find.byText(button).click();
        log.info("Navigate to " + button);
    }

    public boolean checkIfElementisShown(String elementText) {
        boolean isElementFound = false;
        UIElement element = find.byText(elementText);
        if(element != null)
        {
            isElementFound = true;
            log.info("Item " + elementText + " found!");
        }
        else
        {
            log.info("Item " + elementText + " not found!");
        }
        return isElementFound;
    }

    public void waitForElement(int time) throws InterruptedException {
        synchronized(wait) {
            wait.wait(time);
        }
    }
}
