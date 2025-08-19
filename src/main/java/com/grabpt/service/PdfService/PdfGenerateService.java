package com.grabpt.service.PdfService;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class PdfGenerateService {

	public ByteArrayInputStream generatePdfFromHtml(String htmlContent) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfRendererBuilder builder = new PdfRendererBuilder();

		// 1. 폰트 리소스를 InputStream으로 읽어 임시 파일로 복사합니다.
		File fontFile;
		try (InputStream fontStream = new ClassPathResource("static/fonts/NanumGothic.ttf").getInputStream()) {
			fontFile = File.createTempFile("NanumGothic", ".ttf");
			try (FileOutputStream out = new FileOutputStream(fontFile)) {
				FileCopyUtils.copy(fontStream, out);
			}
		}

		try {
			// 2. 복사된 임시 폰트 파일을 사용합니다.
			builder.useFont(fontFile, "Nanum Gothic");

			String baseUrl = new ClassPathResource("templates/").getURL().toString();
			builder.withHtmlContent(htmlContent, baseUrl);

			builder.toStream(outputStream);
			builder.run();
		} finally {
			// 3. PDF 생성 후 임시 폰트 파일을 삭제합니다.
			if (fontFile != null) {
				fontFile.delete();
			}
		}

		// 4. 생성된 PDF 데이터를 반환합니다.
		return new ByteArrayInputStream(outputStream.toByteArray());
	}
}
