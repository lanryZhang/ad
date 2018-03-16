/*
* TestSeleniumJob.java 
* Created on  202017/5/21 12:32 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.jobs;

import com.ifeng.configurable.Context;
import com.ifeng.hippo.core.WebDriverPool;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.util.Date;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class TestSeleniumJob {

    public TestSeleniumJob() {
    }

    public static void doExecute(Context context) throws Exception {
        String[] urls = {"http://ifengad.3g.ifeng.com/ad/ad.php?_if_exp=1&adid=21626&ps=0&backurl=http%3a%2f%2fat.mct01.com%2fo.htm%3fpv%3d0%26sp%3d0%2c3002337%2c3000518%2c3039220%2c0%2c1%2c1"
                , "http://ifengad.3g.ifeng.com/ad/ad.php?adid=21625&ps=0"};
        String[] cssSelector = {"div[class='pic']>a", "div[class='rec_con js_recColumnList']>div[class='con_box']>div[class='con_lis']>a"};
        String[] xpathSelector = {"//div[@class='focus']/ul/li[1]/a", "//div[@class='focus']/ul/li[2]/a"};
        WebDriverPool pool = new WebDriverPool(null);
        PhantomJSDriver driver = (PhantomJSDriver) pool.get();
        int i = 0;
        for (String url : urls)
            try {
                driver.close();

                driver.manage().addCookie(new Cookie("asd", "ddd", "/", ".ifeng.com", new Date()));
                driver.get(url);
//                driver.get(url);
                WebElement webElement = driver.findElement(By.xpath("/html"));
                String content = webElement.getAttribute("outerHTML");

                System.out.println(content);
                WebElement e1 = driver.findElement(By.xpath(xpathSelector[i++]));
                e1.click();

                System.out.println("driver.getCurrentUrl()===" + driver.getCurrentUrl());
                System.out.println("driver.getPageSource()===" + driver.getPageSource());
//                driver.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public static void main(String[] args){
        try {
            doExecute(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}