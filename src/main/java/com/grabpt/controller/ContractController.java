package com.grabpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.grabpt.service.PdfService.PdfGenerateService;

@Controller
public class ContractController {

	@Autowired
	private PdfGenerateService pdfGenerateService;

	@GetMapping("/contract/download/static")
	public ResponseEntity<InputStreamResource> downloadStaticContractPdf() throws IOException {

		// "resources/templates/" 폴더에 있는 "static_contract.html" 파일을 사용
		ByteArrayInputStream pdf = pdfGenerateService.generatePdfFromHtmlFile("templates/static_contract.html");

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "inline; filename=static_contract.pdf");

		return ResponseEntity
			.ok()
			.headers(headers)
			.contentType(MediaType.APPLICATION_PDF)
			.body(new InputStreamResource(pdf));
	}
}
