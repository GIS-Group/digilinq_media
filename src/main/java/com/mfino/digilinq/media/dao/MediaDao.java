package com.mfino.digilinq.media.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.mfino.digilinq.media.dto.MediaDto;

@Component
public class MediaDao {

	private static final String INSERT_FILE = "INSERT INTO `digilinq_files` (`fileUnqId`, `module`, `fileName`, `fileSize`, `filePath`, `contentType`, `createdBy`, `createdTime`) VALUES (?,?,?,?,?,?,?,?)";
	private static final String GET_FILE = "SELECT * FROM `digilinq_files` WHERE `fileId` = ?";
	private static final String GET_FILE_BY_UNIQID = "SELECT * FROM `digilinq_files` WHERE `fileUnqId` = ?";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public MediaDto save(MediaDto fileDto) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(INSERT_FILE, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, fileDto.getFileUnqId());
				ps.setLong(2, fileDto.getModule());
				ps.setString(3, fileDto.getFileName());
				ps.setLong(4, fileDto.getFileSize());
				ps.setString(5, fileDto.getFilePath());
				ps.setString(6, fileDto.getContentType());
				ps.setLong(7, fileDto.getCreatedBy());
				ps.setString(8, fileDto.getCreatedTime());
				return ps;
			}
		}, keyHolder);
		fileDto.setFileId(keyHolder.getKey().longValue());
		return fileDto;
	}

	public MediaDto getByFileId(Long fileId) {
		return jdbcTemplate.queryForObject(GET_FILE, new BeanPropertyRowMapper<MediaDto>(MediaDto.class), fileId);
	}
	
	public MediaDto getByUniqId(String uniqId) {
		return jdbcTemplate.queryForObject(GET_FILE_BY_UNIQID, new BeanPropertyRowMapper<MediaDto>(MediaDto.class), uniqId);
	}
	
}
