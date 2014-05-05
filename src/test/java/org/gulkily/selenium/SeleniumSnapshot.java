package org.gulkily.selenium;

import com.lazerycode.selenium.BrowserType;
import com.lazerycode.selenium.SeleniumBase;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class SeleniumSnapshot {

    private static ResourceBundle _prop = ResourceBundle.getBundle("dev");
    private static BrowserType browserType;
    private static List<WebDriver> webDrivers = Collections.synchronizedList(new ArrayList<WebDriver>());

    private static Connection conn;

    @BeforeSuite
    private static void setupDbConnection() {

        System.out.println("Setting up database connection...");

        try {
            conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1/testdata?user=root&password=admin");
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

    }

    private static void takeScreenshot() {
        takeScreenshot("");
    }

    private static void takeScreenshot(String screenshotName) {
        File screenshot = new File("screenshots" + File.separator + System.currentTimeMillis() + "_" + screenshotName + ".png");
        if (!screenshot.exists()) {
            new File(screenshot.getParent()).mkdirs();
            try {
                screenshot.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            new FileOutputStream(screenshot).write(((TakesScreenshot) SeleniumBase.getDriver()).getScreenshotAs(OutputType.BYTES));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int createNewSnapshot(String testName, String testState, String pageUrl, String browserName, String browserVersion, Integer browserWidth, Integer browserHeight) {
        int snapshotId = 0;

        try {
            if (conn == null) {
                setupDbConnection();
            }

            Date curDate = new Date();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO snapshot (test_name, browser, browser_version, test_state, url, snapshot_time, browser_width, browser_height) VALUES(?, ?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS
            );
            stmt.setString(1, testName);
            stmt.setString(2, browserName);
            stmt.setString(3, browserVersion);
            stmt.setString(4, testState);
            stmt.setString(5, pageUrl);
            stmt.setString(6, sdf.format(curDate));
            stmt.setInt(7, browserWidth);
            stmt.setInt(8, browserHeight);

            stmt.execute();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()){
                snapshotId = rs.getInt(1);
            }
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

        return snapshotId;
    }

    private static void checkPreviousElementStates(WebElement element, String testName, String testState, int browserWidth, int browserHeight) {
        
    }

    private static void recordElementState(WebElement element, Integer elementId, Integer snapshotId) {
        try {
            if (conn == null) {
                setupDbConnection();
            }

            String elementTag = element.getTagName();
            String elementDomId = element.getAttribute("id");
            String elementDomClass = element.getAttribute("class");

            HashMap<String, String> elementState = new HashMap<String, String>();

            elementState.put("height", String.valueOf(element.getSize().getHeight()));
            elementState.put("width", String.valueOf(element.getSize().getWidth()));
            elementState.put("x", String.valueOf(element.getLocation().getX()));
            elementState.put("y", String.valueOf(element.getLocation().getY()));
            elementState.put("class", element.getAttribute("class"));
            elementState.put("id", element.getAttribute("id"));
            elementState.put("border", element.getCssValue("border"));
            elementState.put("font", element.getCssValue("font"));
            elementState.put("padding", element.getCssValue("padding"));

            if (elementTag == "a") {
                elementState.put("href", element.getAttribute("href"));
            }

            for (Map.Entry<String, String> elementStateItem : elementState.entrySet()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO elementstate (snapshot_id, element_id, tag, datapoint, value, dom_id, dom_class) VALUES(?, ?, ?, ?, ?, ?, ?)"
                );

                stmt.setInt(1, snapshotId);
                stmt.setInt(2, elementId);
                stmt.setString(3, elementTag);
                stmt.setString(4, elementStateItem.getKey());
                stmt.setString(5, elementStateItem.getValue());
                stmt.setString(6, elementDomId);
                stmt.setString(7, elementDomClass);

                stmt.execute();
            }
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    public static void takePageSnapshot(WebDriver driver, String testName) {

        List <WebElement> allPageElements = driver.findElements(By.xpath("//*"));

        Capabilities caps = ((RemoteWebDriver) driver).getCapabilities();
        String browserName = caps.getBrowserName();
        String browserVersion = caps.getVersion();

        int snapshotId = createNewSnapshot(
                testName,
                "init",
                driver.getCurrentUrl(),
                browserName,
                browserVersion,
                driver.manage().window().getSize().getWidth(),
                driver.manage().window().getSize().getHeight()
        );
        int elementId = 0;

        for (WebElement el : allPageElements) {
            if (el.isDisplayed()) {
                elementId++;

                recordElementState(el, elementId, snapshotId);
            }
        }
    }

}
