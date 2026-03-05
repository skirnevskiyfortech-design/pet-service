package com.example.petservice.controller;


import com.example.petservice.service.PetService;
import lombok.RequiredArgsConstructor;
import org.openapi.example.api.PetApi;
import org.openapi.example.model.ModelApiResponse;
import org.openapi.example.model.PetRequest;
import org.openapi.example.model.PetResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PetController implements PetApi {

    private final PetService petService;

    @Override
    public ResponseEntity<Void> deletePet(Long petId) {
        petService.deletePet(petId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<PetResponse> addPet(PetRequest petRequest) {
        return ResponseEntity.ok(petService.addPet(petRequest));
    }

    @Override
    public ResponseEntity<List<PetResponse>> findPetsByStatus(String status) {
        return ResponseEntity.ok(petService.findPetsByStatus(status));
    }

    @Override
    public ResponseEntity<List<PetResponse>> findPetsByTags(List<String> tags) {
        return ResponseEntity.ok(petService.findPetsByTags(tags));
    }

    @Override
    public ResponseEntity<PetResponse> getPetById(Long petId) {
        return ResponseEntity.ok(petService.getPetById(petId));
    }

    @Override
    public ResponseEntity<PetResponse> updatePet(PetRequest petRequest) {
        return ResponseEntity.ok(petService.updatePet(petRequest));
    }

    @Override
    public ResponseEntity<PetResponse> updatePetWithForm(Long petId, String name, String status) {
        return ResponseEntity.ok(petService.updatePetWithForm(petId, name, status));
    }

    @Override
    public ResponseEntity<ModelApiResponse> uploadFile(Long petId,
                                                       MultipartFile file,
                                                       String additionalMetadata) {
        return ResponseEntity.ok(petService.uploadImageForPet(petId, file));
    }
}
