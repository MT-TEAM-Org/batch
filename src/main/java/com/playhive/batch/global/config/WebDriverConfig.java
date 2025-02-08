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
		// System.setProperty("webdriver.chrome.driver", "./driver/chromedriver"); // chromedriver 경로 설정
		System.setProperty("webdriver.chrome.driver", "/play-hive-batch/chromedriver"); // chromedriver 경로 설정
		return new ChromeDriver(options);
	}
}
