package com.mfino.digilinq.media.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.mfino.digilinq.media.dao.MediaDao;
import com.mfino.digilinq.media.dto.MediaDto;

@Service
public class MediaService {

	Logger logger = LoggerFactory.getLogger(MediaService.class);
	
	@Value("${digilinq.system.file.base.path}")
	private String baseFilePath;
	
	private static final Map<Integer, String> FOLDER_NAME_MAP = new HashMap<>();
	
	static {
		FOLDER_NAME_MAP.put(1, File.separator + "accounts" + File.separator);
		FOLDER_NAME_MAP.put(2, File.separator + "catalogs" + File.separator);
		FOLDER_NAME_MAP.put(3, File.separator + "orders" + File.separator);
		FOLDER_NAME_MAP.put(4, File.separator + "billings" + File.separator);
		FOLDER_NAME_MAP.put(5, File.separator + "invoices" + File.separator);
	}
	
	@Autowired
	private MediaDao mediaDao;
	
	public MediaDto upload(MultipartFile multipartFile, Long createdBy, Integer module){
		MediaDto mediaDto = new MediaDto();
		
		try {
			String baseFolderName = FOLDER_NAME_MAP.containsKey(module) ? FOLDER_NAME_MAP.get(module) : "";
			String baseFolderPath = baseFilePath + baseFolderName;
			String fileName = System.currentTimeMillis() + "_" + StringUtils.cleanPath(multipartFile.getOriginalFilename());
			
			Path rootLocation = Paths.get(baseFolderPath);
			if (!rootLocation.toFile().exists()) {
				rootLocation.toFile().mkdir();
			}
			Path destinationFile = rootLocation.resolve(Paths.get(fileName)).normalize()
					.toAbsolutePath();
			Files.copy(multipartFile.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
			mediaDto.setFileUnqId(UUID.randomUUID().toString());
			mediaDto.setModule(module);
			mediaDto.setFileName(fileName);
			mediaDto.setFileSize(multipartFile.getSize());
			mediaDto.setFilePath(baseFolderName+fileName);
			mediaDto.setContentType(multipartFile.getContentType());
			mediaDto.setCreatedBy(createdBy);
			mediaDto.setCreatedTime(getNow());
			mediaDto = mediaDao.save(mediaDto);
			//TODO image scaling to get thumbnail
		} catch (Exception e) {
			logger.error("File upload operation failed", e);
		}
		
		return mediaDto;
	}
	
	public ResponseEntity<Resource> download(Long fileId) throws IOException {
		MediaDto fileDto = mediaDao.getByFileId(fileId);
		String filePath = baseFilePath + (FOLDER_NAME_MAP.containsKey(fileDto.getModule()) ? FOLDER_NAME_MAP.get(fileDto.getModule()) : "");
		Path fileStorage = Paths.get(filePath).toAbsolutePath().normalize().resolve(fileDto.getFileName());
        if(!Files.exists(fileStorage)) {
            throw new FileNotFoundException(fileId + " was not found on the server");
        }
        Resource resource = new UrlResource(fileStorage.toUri());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("File-Name", fileDto.getFileName());
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;File-Name=" + resource.getFilename());
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, fileDto.getContentType());
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(Files.probeContentType(fileStorage)))
                .headers(httpHeaders).body(resource);
	}
	
	private String getNow() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(new Date(System.currentTimeMillis()));
	}
}
