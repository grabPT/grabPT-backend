package com.grabpt.service.PdfService;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Service
public class PdfGenerateService {
	/**
	 * 동적으로 생성된 HTML "콘텐츠 문자열"을 파라미터로 받아 PDF 데이터 스트림을 생성합니다.
	 * @param htmlContent Thymeleaf를 통해 완성된 HTML 코드
	 * @return 생성된 PDF의 ByteArrayInputStream
	 * @throws IOException 폰트 파일을 찾지 못하거나 처리 중 오류 발생 시
	 */
	public ByteArrayInputStream generatePdfFromHtml(String htmlContent) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfRendererBuilder builder = new PdfRendererBuilder();

		// 1. 한글 폰트 파일을 로드합니다.
		File fontFile = new ClassPathResource("static/fonts/NanumGothic.ttf").getFile();
		builder.useFont(fontFile, "Nanum Gothic"); // CSS에서 폰트를 사용하게 됩니다.

		// 2. HTML 콘텐츠와 BaseURL(CSS, 이미지 등 상대 경로의 기준점)을 설정합니다.
		String baseUrl = new ClassPathResource("templates/").getURL().toString();
		builder.withHtmlContent(htmlContent, baseUrl);

		// 3. PDF를 메모리 스트림에 렌더링(생성)합니다.
		builder.toStream(outputStream);
		builder.run();

		// 4. 생성된 PDF 데이터를 반환합니다.
		return new ByteArrayInputStream(outputStream.toByteArray());
	}
}
