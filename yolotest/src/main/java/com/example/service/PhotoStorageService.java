package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.entity.Album;
import com.example.entity.Diary;
import com.example.entity.Photo;
import com.example.entity.PhotoTagUser;
import com.example.entity.User;
import com.example.exception.BadRequestException;
import com.example.exception.MySelfieNotFoundException;
import com.example.repository.DiaryRepository;
import com.example.repository.PhotoRepository;
import com.example.repository.PhotoTagUserRepository;
import com.example.repository.UserRepository;

import java.io.IOException;
import java.util.Optional;

@Service
public class PhotoStorageService {

	@Autowired
	private PhotoRepository photoRepository;
	@Autowired
	private DiaryRepository diaryRepository;
	@Autowired
	private PhotoTagUserRepository photoTagUserRepository;

	public Photo storePhoto(MultipartFile photo, String diaryId) {
		// Normalize file name
		String photoName = StringUtils.cleanPath(photo.getOriginalFilename());

		try {
			// Check if the file's name contains invalid characters
			if (photoName.contains("..")) {
				throw new BadRequestException("Sorry! Filename contains invalid path sequence " + photoName);
			}
			Optional<Diary> diary = diaryRepository.findById(diaryId);

			Photo photos = new Photo(photoName, photo.getContentType(), photo.getBytes(), diary.get(),
					diary.get().getAlbum().getId());

			return diaryRepository.findById(diaryId).map(diaryy -> {
				return photoRepository.save(photos);
			}).orElseThrow(() -> new BadRequestException("DiaryId " + diaryId + "not found"));

		} catch (IOException ex) {
			throw new BadRequestException("Could not store file " + photoName + ". Please try again!", ex);
		}
	}

	public Photo getPhoto(String photoId) {
		return photoRepository.findById(photoId)
				.orElseThrow(() -> new MySelfieNotFoundException("File not found with id " + photoId));
	}

	public PhotoTagUser getPhotoFace(String faceRandom) {
		return photoTagUserRepository.findByFaceRandom(faceRandom)
				.orElseThrow(() -> new MySelfieNotFoundException("File not found with path " + faceRandom));

	}

}
