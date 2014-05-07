package org.gulkily.selenium;

import com.lazerycode.selenium.BrowserType;
import com.mysql.jdbc.ResultSetRow;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeSuite;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class SeleniumSnapshot {

    //private static ResourceBundle _prop = ResourceBundle.getBundle("dev");

    private static Connection conn;

    @BeforeSuite
    private static void setupDbConnection() {

        System.out.println("Setting up database connection...");

        try {
//            String mysqlHost = _prop.getString("mysqlhost");
//            String mysqlDb = _prop.getString("mysqldatabase");
//            String mysqlUsername = _prop.getString("root");
//            String mysqlPassword = _prop.getString("mysqlpassword");
            // @todo fix ResourceBundle issue and make this come from properties again

            String mysqlHost = ("127.0.0.1");
            String mysqlDb = ("testdata");
            String mysqlUsername = ("root");
            String mysqlPassword = ("admin");

            conn = DriverManager.getConnection("jdbc:mysql://"+mysqlHost+"/"+mysqlDb+"?user="+mysqlUsername+"&password="+mysqlPassword);
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

    }
//
//    private static void takeScreenshot() {
//        takeScreenshot("");
//    }
//
//    private static void takeScreenshot(String screenshotName) {
//        File screenshot = new File("screenshots" + File.separator + System.currentTimeMillis() + "_" + screenshotName + ".png");
//        if (!screenshot.exists()) {
//            new File(screenshot.getParent()).mkdirs();
//            try {
//                screenshot.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        try {
//            new FileOutputStream(screenshot).write(((TakesScreenshot) SeleniumBase.getDriver()).getScreenshotAs(OutputType.BYTES));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private static int createNewSnapshot(String testName, String testState, String pageUrl, String browserName, String browserVersion, Integer browserWidth, Integer browserHeight) {
        int snapshotId = 0;

        if (conn == null) {
            setupDbConnection();
        }

        try {

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

    private static void getPreviousElementState(WebElement element, String testName, String testState, int browserWidth, int browserHeight, String browserName) {
        if (conn == null) {
            setupDbConnection();
        }

        try {
            String elementTag = element.getTagName();
            String elementDomId = element.getAttribute("id");
            String elementDomClass = element.getAttribute("class");

            HashMap<String, String> elementState = new HashMap<String, String>();

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT elementstate.* FROM elementstate, snapshot WHERE " +
                            "elementstate.snapshot_id = snapshot.id " +
                            "AND snapshot.test_name = ? " +
                            "AND snapshot.test_state = ? " +
                            "AND snapshot.browser_width = ? " +
                            "AND snapshot.browser_height = ? " +
                            "AND snapshot.browser = ? " +
                            "AND elementstate.tag = ? " +
                            "AND elementstate.dom_id = ? " +
                            "AND elementstate.dom_class = ? "
            );

            stmt.setString(1, testName);
            stmt.setString(2, testState);
            stmt.setInt(   3, browserWidth);
            stmt.setInt(   4, browserHeight);
            stmt.setString(5, browserName);
            stmt.setString(6, elementTag);
            stmt.setString(7, elementDomId);
            stmt.setString(8, elementDomClass);

            ResultSet rsElementDataPoints = stmt.executeQuery();

            while (rsElementDataPoints.next()) {

                System.out.println(rsElementDataPoints.getString(1));
            }






        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    private static HashMap<String, String> getElementState(WebElement element) {
        HashMap<String, String> elementState = new HashMap<String, String>();

        if (element.isDisplayed()) {

            elementState.put("height", String.valueOf(element.getSize().getHeight()));
            elementState.put("width", String.valueOf(element.getSize().getWidth()));
            elementState.put("x", String.valueOf(element.getLocation().getX()));
            elementState.put("y", String.valueOf(element.getLocation().getY()));
            elementState.put("class", element.getAttribute("class"));
            elementState.put("id", element.getAttribute("id"));
            elementState.put("border", element.getCssValue("border"));
            elementState.put("font", element.getCssValue("font"));
            elementState.put("padding", element.getCssValue("padding"));

            if (element.getTagName() == "a") {
                elementState.put("href", element.getAttribute("href"));
            }
        } else {
            elementState.put("displayed", "false");
        }

        return elementState;

    }

    private static void recordElementState(WebElement element, Integer elementId, Integer snapshotId, String testName) {
        if (conn == null) {
            setupDbConnection();
        }

        try {

            String elementTag = element.getTagName();
            String elementDomId = element.getAttribute("id");
            String elementDomClass = element.getAttribute("class");

            HashMap <String, String> elementState = getElementState(element);

            for (Map.Entry<String, String> elementStateItem : elementState.entrySet()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO elementstate (snapshot_id, element_id, tag, datapoint, value, dom_id, dom_class) VALUES(?, ?, ?, ?, ?, ?, ?)"
                );

                stmt.setInt(   1, snapshotId);
                stmt.setInt(   2, elementId);
                stmt.setString(3, elementTag);
                stmt.setString(4, elementStateItem.getKey());
                stmt.setString(5, elementStateItem.getValue());
                stmt.setString(6, elementDomId);
                stmt.setString(7, elementDomClass);

                stmt.execute();

                //getPreviousElementState(element, testName, "init", 800, 600, "firefox");


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

        int elementCount = allPageElements.size();

        for (WebElement el : allPageElements) {
            elementId++;

            System.out.print("Recording element " + elementId + "/" + elementCount + "... ");

            recordElementState(el, elementId, snapshotId, testName);
        }
    }

}
