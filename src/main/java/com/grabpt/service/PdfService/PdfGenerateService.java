package com.grabpt.service.PdfService;


import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class PdfGenerateService {

	public ByteArrayInputStream generatePdfFromHtml(String htmlContent) throws IOException {
		try (Playwright playwright = Playwright.create()) {
			// Chromium 브라우저 인스턴스 실행
			Browser browser = playwright.chromium().launch();
			Page page = browser.newPage();

			// 페이지에 HTML 컨텐츠 설정
			page.setContent(htmlContent);

			// PDF 생성 옵션 설정 (A4 사이즈)
			Page.PdfOptions pdfOptions = new Page.PdfOptions()
				.setFormat("A4")
				.setPrintBackground(true); // 배경 그래픽 인쇄

			// HTML을 PDF로 변환하여 byte 배열로 저장
			byte[] pdfBytes = page.pdf(pdfOptions);

			// 브라우저 종료
			browser.close();

			// 생성된 PDF byte 배열을 InputStream으로 변환하여 반환
			return new ByteArrayInputStream(pdfBytes);
		} catch (Exception e) {
			// 예외 발생 시 IOException으로 감싸서 던지기
			throw new IOException("Playwright를 사용하여 PDF를 생성하는 중 오류 발생", e);
		}
	}
}
