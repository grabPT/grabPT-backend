package com.grabpt.service.PdfService;


import com.microsoft.playwright.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class PdfGenerateService {

	private Playwright playwright;
	private Browser browser;
	// PDF 생성을 순차적으로 처리하거나, 브라우저 스레드 안전성을 보장하기 위한 단일 스레드 실행자
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	@PostConstruct
	public void init() {
		executorService.submit(() -> {
			try {
				log.info("Playwright 및 브라우저 인스턴스 초기화 시작..");
				playwright = Playwright.create();
				browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
				log.info("Playwright 브라우저 초기화 완료");
			} catch (Exception e) {
				log.error("Playwright 초기화 실패");
			}
		});
	}

	@PreDestroy
	public void cleanup() {
		executorService.submit(() -> {
			if(browser != null) {browser.close();}
			if(playwright != null) {playwright.close();}
		});
		executorService.shutdown();
	}

	public ByteArrayInputStream generatePdfFromHtml(String htmlContent) throws IOException {
		if(browser == null){
			throw new IOException("PDF 엔진이 아직 준비되지 않았습니다.");
		}

		try {
			byte[] pdfBytes = CompletableFuture.supplyAsync(() -> {
				try(BrowserContext context = browser.newContext()) {
					Page page = context.newPage();
					page.setContent(htmlContent);

					Page.PdfOptions pdfOptions = new Page.PdfOptions()
						.setFormat("A4")
						.setPrintBackground(true);

					return page.pdf(pdfOptions);
				}
			}, executorService).get();

			return new ByteArrayInputStream(pdfBytes);
		}  catch (Exception e) {
			log.error("PDF 생성 중 오류 발생",e);
			throw new IOException("PDF 변환 실패",e);
		}

	}
}
