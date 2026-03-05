package com.example.petservice.service;

import com.example.petservice.exception.PetNotFoundException;
import com.example.petservice.mapper.CategoryMapper;
import com.example.petservice.mapper.PetMapper;
import com.example.petservice.model.Category;
import com.example.petservice.model.FileMetadata;
import com.example.petservice.model.Pet;
import com.example.petservice.model.Tag;
import com.example.petservice.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapi.example.model.ModelApiResponse;
import org.openapi.example.model.PetRequest;
import org.openapi.example.model.PetResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetService {

    private final MinioService minioService;
    private final PetRepository petRepository;
    private final PhotoProcessingService photoProcessingService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final PetMapper petMapper;
    private final CategoryMapper categoryMapper;

    @Transactional
    public PetResponse addPet(PetRequest petRequest) {

        Category category = categoryService
                .findOrCreateCategory(petRequest.getCategory());
        petRequest.setCategory(categoryMapper.toCategoryDto(category));

        Pet pet = petMapper.toPet(petRequest);

        if (petRequest.getTags() != null) {
            List<Tag> tags = petRequest.getTags().stream()
                    /*
                    Для каждого тега вызываем findOrCreate
                     */
                    .map(tagService::findOrCreateTag)
                    .toList();
            pet.setTags(tags);
        }

        List<String> photoIds = new ArrayList<>();
        for (String url : petRequest.getPhotoUrls()) {
            if (url != null && !url.isBlank()) {
                String fileId = photoProcessingService.uploadImageFromUrl(url);
                photoIds.add(fileId);
            }
        }

        pet.setPhotoFileIds(photoIds);

        Pet savedPet = petRepository.save(pet);

        PetResponse petResponse = petMapper.toPetResponse(savedPet);
        petResponse.setPhotoUrls(petRequest.getPhotoUrls());

        return petResponse;
    }

    @Transactional(readOnly = true)
    public List<PetResponse> findPetsByStatus(String status) {
        PetRequest.StatusEnum statusEnum;
        try {
            statusEnum = PetRequest.StatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        return petRepository.findByStatus(statusEnum)
                .stream()
                .map(petMapper::toPetResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PetResponse> findPetsByTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }

        List<Pet> pets = petRepository.findByTags(tags);
        return pets.stream()
                .map(petMapper::toPetResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PetResponse getPetById(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));
        return petMapper.toPetResponse(pet);
    }

    @Transactional
    public PetResponse updatePet(PetRequest petRequest) {
        petRepository.findById(petRequest.getId())
                .orElseThrow(() -> new PetNotFoundException(petRequest.getId()));

        Category category = categoryService.findOrCreateCategory(petRequest.getCategory());
        petRequest.setCategory(categoryMapper.toCategoryDto(category));

        Pet pet = petMapper.toPet(petRequest);

        if (petRequest.getTags() != null) {
            List<Tag> tags = petRequest.getTags().stream()
                    .map(tagService::findOrCreateTag)
                    .toList();
            pet.setTags(tags);
        }

        if (pet.getPhotoFileIds() != null) {
            for (String fileId : pet.getPhotoFileIds()) {
                try {
                    minioService.deleteFile(fileId);
                } catch (Exception e) {
                    log.warn("Failed to delete old file {} for pet {}: {}",
                            fileId, petRequest.getId(), e.getMessage(), e);
                }
            }
        }

        List<String> newPhotoIds = new ArrayList<>();
        for (String url : petRequest.getPhotoUrls()) {
            if (url != null && !url.isBlank()) {
                String fileId = photoProcessingService.uploadImageFromUrl(url);
                newPhotoIds.add(fileId);
            }
        }

        pet.setPhotoFileIds(newPhotoIds);

        Pet updatedPet = petRepository.save(pet);

        PetResponse petResponse = petMapper.toPetResponse(updatedPet);
        petResponse.setPhotoUrls(petRequest.getPhotoUrls());

        return petResponse;
    }

    @Transactional
    public PetResponse updatePetWithForm(Long petId, String name, String status) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));

        PetRequest.StatusEnum statusEnum;
        try {
            statusEnum = PetRequest.StatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        pet.setName(name);
        pet.setStatus(statusEnum);
        petRepository.save(pet);

        return petMapper.toPetResponse(pet);
    }

    @Transactional
    public void deletePet(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));

        if (pet.getPhotoFileIds() != null) {
            for (String fileId : pet.getPhotoFileIds()) {
                try {
                    minioService.deleteFile(fileId);
                } catch (Exception e) {
                    log.warn("Failed to delete file {} for pet {}: {}",
                            fileId, petId, e.getMessage(), e);
                }
            }
        }
        petRepository.delete(pet);
    }

    public ModelApiResponse uploadImageForPet(Long petId, MultipartFile file) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File not transferred");
        }

        try {
            FileMetadata savedFile = minioService.uploadFile(file);
            String newFileId = savedFile.getId().toString();

            List<String> currentIds = pet.getPhotoFileIds() != null
                    ? new ArrayList<>(pet.getPhotoFileIds())
                    : new ArrayList<>();
            currentIds.add(newFileId);

            pet.setPhotoFileIds(currentIds);
            petRepository.save(pet);

            ModelApiResponse response = new ModelApiResponse();
            response.setCode(HttpStatus.OK.value());
            response.setType(savedFile.getContentType());
            response.setMessage("Photo added successfully");

            return response;
        } catch (Exception e) {
            throw new IllegalArgumentException("File upload error", e);
        }
    }
}
