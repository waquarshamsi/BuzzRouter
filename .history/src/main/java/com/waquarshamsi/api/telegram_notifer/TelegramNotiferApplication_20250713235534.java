package com.waquarshamsi.api.telegram_notifer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class TelegramNotiferApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelegramNotiferApplication.class, args);
	}

}
