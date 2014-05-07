package org.gulkily.selenium;

import com.lazerycode.selenium.BrowserType;
import com.mysql.jdbc.Connection;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

//import java.sql.Connection;
import java.lang.reflect.Array;
import java.util.*;

public class SeleniumUtils {

    private static ResourceBundle _prop = ResourceBundle.getBundle("dev");
    private static BrowserType browserType;
    private static List<WebDriver> webDrivers = Collections.synchronizedList(new ArrayList<WebDriver>());

    private static Connection conn;


    public static int getHttpResponseCode(String url) {
        CloseableHttpClient client = HttpClientBuilder.create().build();

        try {
            CloseableHttpResponse response = client.execute(new HttpGet(url));
            return response.getStatusLine().getStatusCode();
        } catch (Exception e) {
            return 0;
        }
    }

    public static List findBrokenLinks(WebDriver driver) {
        List <WebElement> allLinks = driver.findElements(By.xpath("a"));

        List <String> brokenLinks = new LinkedList<String>();

        for (WebElement e : allLinks) {
            String href = e.getAttribute("href");

            if (href != null && href != "") {
                if (getHttpResponseCode(href) != 200) {
                    brokenLinks.add(href);
                }

            }
        }

        return brokenLinks;
    }

    public static List findBrokenImages(WebDriver driver) {
        List <WebElement> allLinks = driver.findElements(By.xpath("img"));

        List <String> brokenLinks = new LinkedList<String>();

        for (WebElement e : allLinks) {
            String href = e.getAttribute("src");

            if (href != null && href != "") {
                if (getHttpResponseCode(href) != 200) {
                    brokenLinks.add(href);
                }

            }
        }

        return brokenLinks;
    }

}
