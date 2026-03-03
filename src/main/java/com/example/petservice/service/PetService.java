package com.example.petservice.service;

import com.example.petservice.dto.FileData;
import com.example.petservice.exception.PetNotFoundException;
import com.example.petservice.mapper.PetMapper;
import com.example.petservice.model.Category;
import com.example.petservice.model.FileMetadata;
import com.example.petservice.model.Pet;
import com.example.petservice.model.Tag;
import com.example.petservice.repository.PetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.openapi.example.model.ModelApiResponse;
import org.openapi.example.model.PetRequest;
import org.openapi.example.model.PetResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class PetService {

    private final MinioService minioService;
    private final PetRepository petRepository;
    private final PhotoProcessingService photoProcessingService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final PetMapper petMapper;


    @Transactional
    public PetResponse addPet (PetRequest petRequest){

        Category category = categoryService.findOrCreateCategory(petRequest.getCategory());

        Pet pet = new Pet();
        pet.setName(petRequest.getName());
        pet.setStatus(petRequest.getStatus());
        pet.setCategory(category);

        if (petRequest.getTags() != null) {
            List<Tag> tags = petRequest.getTags().stream()
                    .map(tagService::findOrCreateTag)  // Для каждого тега вызываем findOrCreate
                    .collect(Collectors.toList());
            pet.setTags(tags);
        }
        Pet savedPet = petRepository.saveAndFlush(pet);

        for (String url : petRequest.getPhotoUrls()){
            if(url != null && !url.isBlank()){
                uploadImageFromUrl(savedPet.getId(),url);
            }
        }

        return petMapper.toPetResponse(savedPet);
    }

    public ModelApiResponse uploadImageForPet(Long petId, MultipartFile file) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File not transferred");
        }

        try {
            FileMetadata savedFile = minioService.uploadFile(file);
            pet.getPhotoFileIds().add(savedFile.getId().toString());

            ModelApiResponse response = new ModelApiResponse();
            response.setCode(HttpStatus.OK.value());
            response.setType(savedFile.getContentType());
            response.setMessage("Photo added successfully");

            if (pet.getPhotoFileIds() == null) {
                pet.setPhotoFileIds(new ArrayList<>());
            }

            petRepository.save(pet);
            return response;
        } catch (Exception e) {
            throw new IllegalArgumentException("File download error", e);
        }
    }

    @Transactional
    public ModelApiResponse uploadImageFromUrl(Long petId, String imageUrl) {
        Pet pet = petRepository.findByIdWithLock(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));

        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL is required");
        }

        try {
            // загрузка и валидация файла по url
            FileData fileData = photoProcessingService.downloadFromUrl(imageUrl);

            // сохраняем
            FileMetadata savedFile = minioService.saveFileFromData(
                    fileData.toInputStream(),
                    fileData.fileName(),
                    fileData.contentType(),
                    fileData.size()
            );

            // привязываем к питомцу
            pet.getPhotoFileIds().add(savedFile.getId().toString());

            ModelApiResponse response = new ModelApiResponse();
            response.setCode(HttpStatus.OK.value());
            response.setType(savedFile.getContentType());
            response.setMessage("Photo added successfully from URL");

            if (pet.getPhotoFileIds() == null) {
                pet.setPhotoFileIds(new ArrayList<>());
            }


            return response;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error downloading file from URL: "
                    + e.getMessage(), e);
        }
    }
}
