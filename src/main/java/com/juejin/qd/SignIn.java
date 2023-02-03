package com.juejin.qd;

import com.alibaba.fastjson.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SignIn implements CommandLineRunner {
    @Value("classpath:static/chromedriver.exe")
    File file;

    @Override
    public void run(String... args) throws Exception {
        one();
        //two();
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
        System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
        ChromeOptions options = new ChromeOptions();
        // 不显示浏览器
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.get(rootUrl);
        String cookiePath = "D:\\upload\\cookie.txt";
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
    public void one() throws IOException, InterruptedException {
        // https://juejin.cn
        // https://juejin.cn/user/center/signin?avatar_menu
        String rootUrl = "https://juejin.cn";
        String url = "https://juejin.cn/user/center/signin?avatar_menu";
        System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
        ChromeOptions options = new ChromeOptions();
        // 不显示浏览器
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.get(rootUrl);
        String cookiePath = "D:\\upload\\cookie.json";
        File cookie = new File(cookiePath);
        if (cookie.exists()) {
            InputStream isRptFile = new FileInputStream(cookie);
            String configContent = StreamUtils.copyToString(isRptFile, StandardCharsets.UTF_8);
            List<JSONObject> cookieList = JSONObject.parseObject(configContent, List.class);
            if (!CollectionUtils.isEmpty(cookieList)) {
                for (JSONObject object : cookieList) {
                    Cookie c = JSONObject.parseObject(String.valueOf(object), Cookie.class);
                    driver.manage().addCookie(c);
                }
            }
        } else {
            // 没有cookie文件，手动获取cookie
            List<Cookie> cookieList = driver.manage().getCookies().stream().collect(Collectors.toList());
            writeObjectToFile(JSONObject.toJSONString(cookieList), cookiePath);
        }
        driver.get(url);
        String title = driver.getTitle();
        System.out.println("标题 ===>" + title);
        // 未登录框
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
            System.out.println("【成功】连续签到天数为【" + lianxuqiandao.get(0).getText() + "】");

        }
        // //*[@id="juejin"]/div[1]/main/div[2]/div/div[1]/div[2]/div[2]/div[1]/div[1]/div[2]/span
        List<WebElement> leijiqiandao = driver.findElements(By.xpath("//*[@id=\"juejin\"]/div[1]/main/div[2]/div/div[1]/div[2]/div[2]/div[1]/div[1]/div[2]/span"));
        if (!CollectionUtils.isEmpty(leijiqiandao)) {
            System.out.println("【成功】累计签到天数为【" + leijiqiandao.get(0).getText() + "】");

        }
        // //*[@id="juejin"]/div[1]/main/div[2]/div/div[1]/div[2]/div[2]/div[1]/div[1]/div[3]/span
        List<WebElement> dangqiankuangshi = driver.findElements(By.xpath("//*[@id=\"juejin\"]/div[1]/main/div[2]/div/div[1]/div[2]/div[2]/div[1]/div[1]/div[3]/span"));
        if (!CollectionUtils.isEmpty(dangqiankuangshi)) {
            System.out.println("【成功】当前矿石数为【" + dangqiankuangshi.get(0).getText() + "】");

        }
    }
}
