package com.playhive.batch.global.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class WebDriverConfig {

	public static WebDriver createDriver() {
		ChromeOptions options = new ChromeOptions();
		// 헤드리스 모드 설정: 브라우저 UI 없이 실행
		options.addArguments("--headless");
		// Linux 환경에서 Chrome을 사용할 때 문제가 발생할 수 있는 사항을 우회
		options.addArguments("--no-sandbox");
		// /dev/shm이 접근할 수 없을 때의 문제를 피하기 위한 설정
		options.addArguments("--disable-dev-shm-usage");
		// GPU 사용 비활성화: 리소스 절약을 위해 GPU 가속을 사용하지 않음
		options.addArguments("--disable-gpu");
		// SSL 오류 무시 설정: SSL 인증서를 무시하고 브라우저를 실행
		options.addArguments("--ignore-ssl-errors=yes");
		// 인증서 오류 무시 설정: 모든 인증서 오류를 무시하고 브라우저를 실행
		options.addArguments("--ignore-certificate-errors");

		return new ChromeDriver(options);
	}
}
