package com.playhive.batch.global.config;

import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class WebDriverConfig {

	public static WebDriver createDriver() {
		ChromeOptions options = new ChromeOptions();
		// 헤드리스 모드 설정: 브라우저 UI 없이 실행
		options.addArguments("--headless");
		// 리소스 절약을 위한 GPU 가속 비활성화
		options.addArguments("--disable-gpu");
		// Linux 환경에서 Chrome을 사용할 때 발생할 수 있는 문제 우회
		options.addArguments("--no-sandbox");
		// /dev/shm이 접근할 수 없을 때의 문제를 피하기 위한 설정
		options.addArguments("--disable-dev-shm-usage");
		// 이미지 로딩 비활성화 (옵션)
		options.addArguments("--disable-images");
		// 확장 프로그램 비활성화
		options.addArguments("--disable-extensions");
		// SSL 인증서 무시 설정
		options.addArguments("--ignore-ssl-errors=yes");
		options.addArguments("--ignore-certificate-errors");
		// 브라우저 창 크기 설정
		options.addArguments("--window-size=1200,800");
		// 국가 언어 설정
		options.addArguments("--lang=ko"); // 한국어로 설정

		// 성능 최적화 관련 속성 추가
		options.addArguments("--disable-setuid-sandbox");
		options.addArguments("--disable-default-apps");
		options.addArguments("--no-first-run");
		options.addArguments("--no-service-autorun");

		options.addArguments("--remote-allow-origins=*");

		// Chrome의 적용 가능한 최적화
		options.setExperimentalOption("prefs", Map.of(
			"profile.default_content_setting_values.notifications", 2, // 알림 비활성화
			"profile.default_content_setting_values.images", 2 // 이미지 로딩 비활성화
		));

		return new ChromeDriver(options);
	}
}
