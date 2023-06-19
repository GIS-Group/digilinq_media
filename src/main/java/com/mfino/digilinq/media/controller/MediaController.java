package com.mfino.digilinq.media.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mfino.digilinq.media.dto.MediaDto;
import com.mfino.digilinq.media.service.MediaService;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

	@Autowired
	private MediaService mediaService;
	
	@PostMapping("/{createdBy}/{module}")
	public ResponseEntity<MediaDto> upload(@PathVariable Long createdBy, @PathVariable Integer module, @RequestParam("file") MultipartFile multipartFile) {
		return ResponseEntity.ok(mediaService.upload(multipartFile, createdBy, module));
	}
	
	@GetMapping("/{fileId}")
	public ResponseEntity<Resource> download(@PathVariable Long fileId) throws IOException {
		return mediaService.download(fileId);
	}
	
	@GetMapping("/uniqid/{uniqId}")
	public ResponseEntity<Resource> downloadByUniqId(@PathVariable String uniqId) throws IOException {
		return mediaService.downloadByUniqId(uniqId);
	}
}
