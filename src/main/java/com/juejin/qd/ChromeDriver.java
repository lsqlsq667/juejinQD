package com.juejin.qd;

import com.alibaba.fastjson.JSONObject;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChromeDriver {


    @Value("${sourcePath.cookieJson}")
    String cookiePath;
    @Value("${sourcePath.chromedriver}")
    String chromedriverPath;

    public WebDriver get() throws IOException {
        String rootUrl = "https://juejin.cn";
        System.setProperty("webdriver.chrome.driver", chromedriverPath);
        ChromeOptions options = new ChromeOptions();
        // 不显示浏览器
        options.addArguments("--headless");
        WebDriver driver = new org.openqa.selenium.chrome.ChromeDriver(options);
        driver.get(rootUrl);
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
            // 如果没有cookie文件，断点这里，然后手动登录，再获取cookie保存到cookiePath路径
            List<Cookie> cookieList = driver.manage().getCookies().stream().collect(Collectors.toList());
            writeObjectToFile(JSONObject.toJSONString(cookieList), cookiePath);
        }
        return driver;
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
}
