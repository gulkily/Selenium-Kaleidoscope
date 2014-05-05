package org.gulkily.selenium;

import com.lazerycode.selenium.BrowserType;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.sql.Connection;
import java.util.*;

public class SeleniumUtils {

    private static ResourceBundle _prop = ResourceBundle.getBundle("dev");
    private static BrowserType browserType;
    private static List<WebDriver> webDrivers = Collections.synchronizedList(new ArrayList<WebDriver>());

    private static Connection conn;

    private static List <WebElement> findBrokenImages(WebDriver driver) {

        List allImages = driver.findElements(By.tagName("img"));
//
//        for (WebElement image : allImages) {
//            String imageSrc = image.getAttribute("src").toString();
//
//            if (imageSrc != "") {
//                int imageResponseCode = getHttpResponseCode(imageSrc);
//            }
//        }

        return new LinkedList<WebElement>();

    }

    private static int getHttpResponseCode(String url) {
        CloseableHttpClient client = HttpClientBuilder.create().build();

        try {
            CloseableHttpResponse response = client.execute(new HttpGet(url));
            return response.getStatusLine().getStatusCode();
        } catch (Exception e) {
            return 0;
        }
    }

}
