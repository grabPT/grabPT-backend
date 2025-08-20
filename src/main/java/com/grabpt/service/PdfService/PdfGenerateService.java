package com.grabpt.service.PdfService;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;

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

		// 1. ConverterProperties 객체를 생성합니다.
		ConverterProperties converterProperties = new ConverterProperties();

		// 2. 폰트 제공자(FontProvider)를 설정합니다.
		DefaultFontProvider fontProvider = new DefaultFontProvider(false, false, false);

		// 3. resources 폴더에 있는 폰트 파일을 폰트 제공자에 추가합니다.
		byte[] fontBytes = new ClassPathResource("static/fonts/NanumGothic.ttf").getInputStream().readAllBytes();
		fontProvider.addFont(fontBytes);
		converterProperties.setFontProvider(fontProvider);

		// 4. HTML을 PDF로 변환합니다.
		HtmlConverter.convertToPdf(htmlContent, outputStream, converterProperties);

		return new ByteArrayInputStream(outputStream.toByteArray());
	}
}
