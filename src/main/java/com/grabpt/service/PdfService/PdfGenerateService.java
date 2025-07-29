package com.grabpt.service.PdfService;

import com.lowagie.text.pdf.BaseFont;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class PdfGenerateService {
	/**
	 * 정적 HTML 파일 경로를 받아 PDF를 생성하는 메소드
	 * @param htmlFilePath 'resources' 폴더 기준의 파일 경로 (예: "templates/contract.html")
	 * @return 생성된 PDF의 ByteArrayInputStream
	 */
	public ByteArrayInputStream generatePdfFromHtmlFile(String htmlFilePath) throws IOException {

		// 1. ClassPathResource를 사용해 resources 폴더의 파일을 찾습니다.
		ClassPathResource resource = new ClassPathResource(htmlFilePath);

		// 2. 파일을 읽어 HTML 내용을 문자열로 변환합니다.
		byte[] bytes = Files.readAllBytes(resource.getFile().toPath());
		String htmlContent = new String(bytes);

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			ITextRenderer renderer = new ITextRenderer();

			// 한글 폰트 설정
			renderer.getFontResolver().addFont("static/fonts/NanumGothic.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

			// CSS 같은 외부 리소스를 찾을 기준 경로(Base-URL)를 설정해줍니다.
			String baseUrl = resource.getURL().toExternalForm();
			renderer.setDocumentFromString(htmlContent, baseUrl);

			renderer.layout();
			renderer.createPDF(outputStream);

			return new ByteArrayInputStream(outputStream.toByteArray());
		} catch (Exception e) {
			throw new RuntimeException("PDF 생성 오류", e);
		}
	}
}
