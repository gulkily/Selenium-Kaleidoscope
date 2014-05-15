package org.gulkily.selenium.Tests;

import com.lazerycode.selenium.ScreenshotListener;
import com.lazerycode.selenium.SeleniumBase;
import org.gulkily.selenium.SeleniumSnapshot;
import org.gulkily.selenium.SeleniumUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

@Listeners(ScreenshotListener.class)
public class SdkbMainST extends SeleniumBase {

    @Test
    public void sdkbHomeTest() {
        // Create a new WebDriver instance
        // Notice that the remainder of the code relies on the interface,
        // not the implementation.
        WebDriver driver = getDriver();

        List <Dimension> dimList = new LinkedList<Dimension>();

        dimList.add(new Dimension(1024, 768));
        dimList.add(new Dimension(1280, 1024));

        for (Dimension d : dimList) {
            driver.manage().window().setSize(d);

            driver.get("http://sdkb.ru/");

            SeleniumSnapshot.takePageSnapshot(driver, "sdkbHomeTest", "init");

            List <String> brokenLinks = SeleniumUtils.findBrokenLinks(driver);
            List <String> brokenImages = SeleniumUtils.findBrokenImages(driver);

            if (brokenLinks.size() > 0) {
                for (int i = 0; i < brokenLinks.size(); i++) {
                    String brokenLinkUrl = brokenLinks.get(i);
                    SeleniumSnapshot.logError("Broken link: " + brokenLinkUrl);
                }
            }

            if (brokenImages.size() > 0) {
                for (int i = 0; i < brokenImages.size(); i++) {
                    String brokenImageUrl = brokenImages.get(i);
                    SeleniumSnapshot.logError("Broken link: " + brokenImageUrl);
                }
            }

        }
    }
}