package com.juejin.qd;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SignIn {

    @Value("${sourcePath.cookieJson}")
    String cookiePath;
    @Value("${sourcePath.chromedriver}")
    String chromedriverPath;
    @Autowired
    ChromeDriver chromeDriver;

    /**
     * 签到
     *
     * @throws Exception
     */
    @PostConstruct // 启动时执行一次
    @Scheduled(cron = "0 10 8 * * ?")
    public void run() throws Exception {
        one();
        //two();
    }

    /**
     * bug Fix
     */
    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void bugFix() throws IOException {
        System.out.println("【bugFix】执行 start==============================================================");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("【bugFix】执行 当前时间：" + simpleDateFormat.format(new Date()));
        WebDriver driver = chromeDriver.get();
        try {
            String url = "https://juejin.cn/user/center/bugfix?enter_from=bugFix_bar";
            driver.get(url);
            Thread.sleep(1000);
            String title = driver.getTitle();
            System.out.println("【bugFix】标题 ===>" + title);
            //
            handleElement(driver, "/html/body/div[2]/div[2]/div/div/div/div[1]/img", "style.display='none'");
            handleElement(driver, "/html/body/div[2]/div[1]", "style.zIndex=0");
            handleElement(driver, "/html/body/div[2]/div[2]", "style.zIndex=0");
            handleElement(driver, "/html/body/div[2]/div[2]/div/div", "style.display='none'");
            //
            List<WebElement> parentDiv = driver.findElements(By.xpath("//*[@id=\"juejin\"]/div[1]/main/div[2]/div/div/div[1]/div/div/div[1]/div/div[1]"));
            if (CollectionUtils.isEmpty(parentDiv)) {
                System.out.println("【bugFix】没有bug");
            } else {
                Thread.sleep(1000);
                List<WebElement> imgs = parentDiv.get(0).findElements(By.tagName("img"));
                if (CollectionUtils.isEmpty(imgs)) {
                    System.out.println("【bugFix】bug 数量" + imgs.size() + "  时间：" + simpleDateFormat.format(new Date()));
                    return;
                }
                if (!CollectionUtils.isEmpty(imgs)) {
                    for (WebElement img : imgs) {
                        Thread.sleep(500);
                        img.click();
                        Thread.sleep(500);
                    }
                }
                System.out.println("【bugFix】收集bugs数量" + imgs.size());
            }
            Thread.sleep(500);
            WebElement p1 = getIfExistElement(driver, "//*[@id=\"juejin\"]/div[1]/main/div[2]/div/div/div[1]/div/div/div[1]/div/div[2]/div[2]/div[2]/div/p[1]");
            WebElement p2 = getIfExistElement(driver, "//*[@id=\"juejin\"]/div[1]/main/div[2]/div/div/div[1]/div/div/div[1]/div/div[2]/div[2]/div[2]/div/p[2]");
            if (p1 != null && p2 != null) {
                System.out.println("【bugFix】" + p2.getText() + p1.getText());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            driver.close();
            driver.quit();
            System.out.println("【bugFix】执行 end==============================================================");
        }
    }

    /**
     * 方法二
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void two() throws IOException, InterruptedException {
        String rootUrl = "https://juejin.cn";
        String url = "https://juejin.cn/user/center/signin?avatar_menu";
        System.setProperty("webdriver.chrome.driver", chromedriverPath);
        ChromeOptions options = new ChromeOptions();
        // 不显示浏览器
        options.addArguments("--headless");
        WebDriver driver = new org.openqa.selenium.chrome.ChromeDriver(options);
        driver.get(rootUrl);
        File cookie = new File(cookiePath);
        if (!cookie.exists()) {
            System.out.println("cookie 文件不存在，结束运行");
            return;
        }
        InputStream isRptFile = new FileInputStream(cookie);
        String configContent = StreamUtils.copyToString(isRptFile, StandardCharsets.UTF_8);
        String[] strings = configContent.split(";");
        Map<String, String> cookieMap = new HashMap<>();
        for (String s : strings) {
            s = s.replace(" ", "");
            int index = s.indexOf("=");
            if (index != -1) {
                cookieMap.put(s.substring(0, index), s.substring(index + 1, s.length()));
            }

        }
        for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
            driver.manage().addCookie(new Cookie(entry.getKey(), entry.getValue()));
        }
        driver.get(url);
        String title = driver.getTitle();
        System.out.println("标题 ===>" + title);
        List<WebElement> noLogins = driver.findElements(By.xpath("//*[@id=\"juejin\"]/div[1]/main/div[1]/div[1]/div"));
        if (!CollectionUtils.isEmpty(noLogins)) {
            System.out.println("【失败】获取用户名为：" + noLogins.get(0).getText());
            System.out.println("【失败】cookie值配置失败");
        } else {
            List<WebElement> names = driver.findElements(By.xpath("//*[@id=\"juejin\"]/div[1]/main/div[1]/div[1]/a[2]/span"));
            if (!CollectionUtils.isEmpty(names)) {
                System.out.println("【成功】cookie值配置成功，当前登录用户为【" + names.get(0).getText() + "】");

            }
            basicInfoShow(driver);
            // 手动签到
            //*[@id="juejin"]/div[1]/main/div[2]/div/div[1]/div[2]/div[2]/div[2]/div[1]/button
            List<WebElement> qiandao = driver.findElements(By.xpath("//*[@id=\"juejin\"]/div[1]/main/div[2]/div/div[1]/div[2]/div[2]/div[2]/div[1]/button"));
            if (!CollectionUtils.isEmpty(qiandao)) {
                if ("今日已签到".equals(qiandao.get(0).getText())) {
                    System.out.println("【今日已签到】");
                } else {
                    System.out.println("【成功】点击签到【" + qiandao.get(0).getText() + "】");
                    qiandao.get(0).click();
                    basicInfoShow(driver);
                }
            }
        }
        driver.close();
        driver.quit();
    }

    /**
     * 方法一
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void one() throws IOException {
        System.out.println("【签到】执行 start==============================================================");
        // https://juejin.cn
        // https://juejin.cn/user/center/signin?avatar_menu
        String url = "https://juejin.cn/user/center/signin?avatar_menu";
        WebDriver driver = chromeDriver.get();
        try {
            driver.get(url);
            String title = driver.getTitle();
            System.out.println("【签到】标题 ===>" + title);
            // 未登录框
            List<WebElement> noLogins = driver.findElements(By.xpath("//*[@id=\"juejin\"]/div[1]/main/div[1]/div[1]/div"));
            if (!CollectionUtils.isEmpty(noLogins)) {
                System.out.println("【签到】获取用户名为：" + noLogins.get(0).getText());
                System.out.println("【签到】cookie值配置失败");
            } else {
                List<WebElement> names = driver.findElements(By.xpath("//*[@id=\"juejin\"]/div[1]/main/div[1]/div[1]/a[2]/span"));
                if (!CollectionUtils.isEmpty(names)) {
                    System.out.println("【签到】cookie值配置成功，当前登录用户为【" + names.get(0).getText() + "】");

                }
                basicInfoShow(driver);
                // 手动签到
                //*[@id="juejin"]/div[1]/main/div[2]/div/div[1]/div[2]/div[2]/div[2]/div[1]/button
                List<WebElement> qiandao = driver.findElements(By.xpath("//*[@id=\"juejin\"]/div[1]/main/div[2]/div/div[1]/div[2]/div[2]/div[2]/div[1]/button"));
                if (!CollectionUtils.isEmpty(qiandao)) {
                    if ("今日已签到".equals(qiandao.get(0).getText())) {
                        System.out.println("【签到】今日已签到");
                    } else {
                        System.out.println("【签到】成功点击签到【" + qiandao.get(0).getText() + "】");
                        qiandao.get(0).click();
                        basicInfoShow(driver);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            driver.close();
            driver.quit();
            System.out.println("【签到】执行 end==============================================================");
        }
    }

    public static void writeObjectToFile(String obj, String filePath) {
        File file = new File(filePath);
        try {
            Writer write = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            write.write(obj);
            write.flush();
            write.close();
            System.out.println("write object success!");
        } catch (IOException e) {
            System.out.println("write object failed");
            e.printStackTrace();
        }
    }

    /**
     * 打印签到成功的基本信息
     *
     * @param driver
     * @throws InterruptedException
     */
    public static void basicInfoShow(WebDriver driver) throws InterruptedException {
        String url = "https://juejin.cn/user/center/signin?avatar_menu";
        driver.get(url);
        Thread.sleep(1000);
        // //*[@id="juejin"]/div[1]/main/div[2]/div/div[1]/div[2]/div[2]/div[1]/div[1]/div[1]/span
        List<WebElement> lianxuqiandao = driver.findElements(By.xpath("//*[@id=\"juejin\"]/div[1]/main/div[2]/div/div[1]/div[2]/div[2]/div[1]/div[1]/div[1]/span"));
        if (!CollectionUtils.isEmpty(lianxuqiandao)) {
            System.out.println("【签到】连续签到天数为【" + lianxuqiandao.get(0).getText() + "】");

        }
        // //*[@id="juejin"]/div[1]/main/div[2]/div/div[1]/div[2]/div[2]/div[1]/div[1]/div[2]/span
        List<WebElement> leijiqiandao = driver.findElements(By.xpath("//*[@id=\"juejin\"]/div[1]/main/div[2]/div/div[1]/div[2]/div[2]/div[1]/div[1]/div[2]/span"));
        if (!CollectionUtils.isEmpty(leijiqiandao)) {
            System.out.println("【签到】累计签到天数为【" + leijiqiandao.get(0).getText() + "】");

        }
        // //*[@id="juejin"]/div[1]/main/div[2]/div/div[1]/div[2]/div[2]/div[1]/div[1]/div[3]/span
        List<WebElement> dangqiankuangshi = driver.findElements(By.xpath("//*[@id=\"juejin\"]/div[1]/main/div[2]/div/div[1]/div[2]/div[2]/div[1]/div[1]/div[3]/span"));
        if (!CollectionUtils.isEmpty(dangqiankuangshi)) {
            System.out.println("【签到】当前矿石数为【" + dangqiankuangshi.get(0).getText() + "】");

        }
    }

    public void handleElement(WebDriver driver, String xpath, String jsStr) {
        List<WebElement> webElements = driver.findElements(By.xpath(xpath));
        if (!CollectionUtils.isEmpty(webElements)) {
            System.out.println("【bugFix】隐藏页面遮挡元素");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0]." + jsStr + ";", webElements.get(0));
        }
    }

    public WebElement getIfExistElement(WebDriver driver, String xpath) {
        List<WebElement> webElements = driver.findElements(By.xpath(xpath));
        if (!CollectionUtils.isEmpty(webElements)) {
            return webElements.get(0);
        }
        return null;
    }
}
