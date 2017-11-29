package com.ifeng.hippo.core;

import com.ifeng.configurable.Context;
import com.ifeng.hippo.exceptions.NoProxyAvilableException;
import com.ifeng.hippo.entity.TaskFragment;
import com.ifeng.hippo.entity.UserAgentInfo;
import com.ifeng.hippo.proxy.*;
import com.ifeng.hippo.proxy.Proxy;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhanglr on 2016/7/7.
 */
public class WebDriverPool {
    private Logger logger = Logger.getLogger(getClass());

    private final static int DEFAULT_CAPACITY = 1;

    private final static int STAT_RUNNING = 1;

    private final static int STAT_CLODED = 2;

    private AtomicInteger stat = new AtomicInteger(STAT_RUNNING);

    private String binPath = "";
    private int capacity = 10;
    /*
     * new fields for configuring phantomJS
     */
    private static final String CONFIG_FILE = "config.ini";
    private static final String DRIVER_FIREFOX = "firefox";
    private static final String DRIVER_CHROME = "chrome";
    private static final String DRIVER_PHANTOMJS = "phantomjs.binary.path";

    protected static Properties sConfig;
    protected static DesiredCapabilities sCaps;
    private List<IFilter> filters = new ArrayList<>();

    public void registFilter(IFilter filter){
        filters.add(filter);
    }

    /**
     * Configure the GhostDriver, and initialize a WebDriver instance. This part
     * of code comes from GhostDriver.
     * https://github.com/detro/ghostdriver/tree/master/test/java/src/test/java/ghostdriver
     *
     * @throws IOException
     * @author bob.li.0718@gmail.com
     */
    public WebDriver newDriver(TaskFragment tf) throws Exception {
        WebDriver mDriver = null;
        sConfig = new Properties();
        sCaps = DesiredCapabilities.phantomjs();
//        sCaps.setJavascriptEnabled(true);
        sCaps.setPlatform(Platform.LINUX);
//        sCaps.setCapability("takesScreenshot", false);

        sCaps.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                binPath);

        Context context = new Context();
        context.put("taskFragment",tf);

        filters.forEach(r->r.filter(context));

        String ua = ((UserAgentInfo)context.getObject("userAgent")).getUserAgent();
        sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX + "User-Agent", ua);
        sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX+"Cache-Control","no-store, " +
                "no-cache, must-revalidate, post-check=0, pre-check=0");
        try {
            Proxy proxy = (Proxy) context.getObject("proxy");

            ArrayList<String> cliArgsCap = new ArrayList<>();

            cliArgsCap.add("--web-security=false");
            cliArgsCap.add("--ssl-protocol=any");
            cliArgsCap.add("--ignore-ssl-errors=true");
            cliArgsCap.add("--disk-cache=false");
////            cliArgsCap.add("--disk-cache-path="+"/data/cache/"+String.valueOf(System.currentTimeMillis()) + (Math.random() * 10000));
            cliArgsCap.add("--local-to-remote-url-access=false");
//            cliArgsCap.add("--load-images=false");
            cliArgsCap.add("--max-disk-cache-size=0");
            cliArgsCap.add("--offline-storage-quota=0");
            cliArgsCap.add("--offline-storage-path=/data/offline");
//            cliArgsCap.add("--webdriver=127.0.0.1:8910");

            if (proxy != null){
                org.openqa.selenium.Proxy sp = new org.openqa.selenium.Proxy();
                if (proxy.getProxyType() == ProxyType.HTTP){
                    cliArgsCap.add("--proxy-type=http");
                    sp.setHttpProxy(proxy.getProxyIp());
                }else{
                    cliArgsCap.add("--proxy-type=socks5");
                    sp.setSocksProxy(proxy.getProxyIp());
                }
                sCaps.setCapability(CapabilityType.PROXY, sp);
            }else{
                return null;
            }

            sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,
                    cliArgsCap);

            mDriver = new PhantomJSDriver(sCaps);
        } catch (Exception er) {
            logger.error(er);
        } finally {

        }
        return mDriver;
    }

    /**
     * check whether input is a valid URL
     *
     * @param urlString urlString
     * @return true means yes, otherwise no.
     * @author bob.li.0718@gmail.com
     */
    private boolean isUrl(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (MalformedURLException mue) {
            return false;
        }
    }

    /**
     * store webDrivers created
     */
//    private List<WebDriver> webDriverList = Collections
//            .synchronizedList(new ArrayList<WebDriver>());

    /**
     * store webDrivers available
     */
    private BlockingQueue<WebDriver> innerQueue = new LinkedBlockingQueue<>();

    public WebDriverPool(Context context) {
        int taskParallel = context.getInt("task.parallel");
        capacity = taskParallel * 50;
        this.binPath = context.getString("driver.path");
    }

    public WebDriverPool() {
        this(DEFAULT_CAPACITY,"/tmp/phantomjs");
    }

    public WebDriverPool(int capacity,String path) {
        this.capacity = capacity;
        this.binPath = path;
    }



    /**
     * @return
     * @throws InterruptedException
     */
    public WebDriver get() throws Exception {
//        checkRunning();
        WebDriver poll = innerQueue.poll();

        if (poll != null) {

            return poll;
        }
//        if (webDriverList.size() < capacity) {
//            synchronized (webDriverList) {
//                if (webDriverList.size() < capacity) {
        try {
            if (innerQueue.size() < capacity) {
                innerQueue.add(newDriver(null));
            }
//                        webDriverList.add(mDriver);
        } catch (IOException e) {
            logger.error(e);
        } catch (NoProxyAvilableException e) {
            throw e;
        }
//                }
//            }
//        }
        poll = innerQueue.poll();
        return poll;
    }

    /**
     * @return
     * @throws InterruptedException
     */
    public WebDriver getForTask(TaskFragment tf) throws Exception {
        WebDriver poll = innerQueue.poll();

        if (poll != null) {
            return poll;
        }
        try {
            if (innerQueue.size() < capacity) {
                WebDriver  wd = newDriver(tf);
                if (wd != null) {
                    innerQueue.offer(wd);
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (NoProxyAvilableException e) {
            throw e;
        }
        return innerQueue.poll();
    }

    public void returnToPool(WebDriver webDriver) {
        innerQueue.add(webDriver);
    }

    public void returnToBrokenPool(WebDriver webDriver) {

        try {
            webDriver.quit();
        }catch (Exception er){
            er.printStackTrace();
            logger.error("quit error webDriver info :");
            logger.error("returnToBrokenPool webdriver quit error "+webDriver + er);
        }
    }


    public void closeAllPurseProcess() {
//        try {
//            innerQueue.clear();
//            logger.info("closeAllPurseProcess webdriver! killprocess.sh");
//            Runtime.getRuntime().exec("/data/programs/hippo/bin/killprocess.sh");
//        } catch (IOException e) {
//            logger.error(e);
//        }
        closeAll();
    }

    public void closeAll() {
        try {
            innerQueue.clear();
            logger.info("closeAll webdriver! killall phantomjs");
            Runtime.getRuntime().exec("killall phantomjs");
        } catch (IOException e) {
            logger.error(e);
        }
    }
}