package com.playhive.batch.global.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebDriverConfig {

	@Bean
	public WebDriver webDriver() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless"); // 헤드리스 모드

		// 운영체제에 따른 ChromeDriver 경로 설정
		String os = System.getProperty("os.name").toLowerCase();
		String chromeDriverPath;

		if (os.contains("win") || os.contains("mac")) {
			chromeDriverPath = "./driver/chromedriver"; // Windows 경로
		} else { // Linux 포함
			chromeDriverPath = "/play-hive-batch/chromedriver"; // Linux 경로
		}

		System.setProperty("webdriver.chrome.driver", chromeDriverPath);

		return new ChromeDriver(options);
	}
}
