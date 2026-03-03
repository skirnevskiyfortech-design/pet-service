package com.example.petservice.controller;


import com.example.petservice.service.PetService;
import lombok.RequiredArgsConstructor;
import org.openapi.example.api.PetApi;
import org.openapi.example.model.ModelApiResponse;
import org.openapi.example.model.PetRequest;
import org.openapi.example.model.PetResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class PetController implements PetApi {

    private final PetService petService;

    @Override
    public ResponseEntity<Void> deletePet(Long petId) {
        return null;
    }

    @Override
    public ResponseEntity<PetResponse> addPet(PetRequest petRequest) {
        return ResponseEntity.ok(petService.addPet(petRequest));
    }

    @Override
    public ResponseEntity<List<PetResponse>> findPetsByStatus(String status) {
        return null;
    }

    @Override
    public ResponseEntity<List<PetResponse>> findPetsByTags(List<String> tags) {
        return null;
    }

    @Override
    public ResponseEntity<PetResponse> getPetById(Long petId) {
        return null;
    }

    @Override
    public ResponseEntity<PetResponse> updatePet(PetRequest petRequest) {
        return null;
    }

    @Override
    public ResponseEntity<PetResponse> updatePetWithForm(Long petId, String name, String status) {
        return null;
    }

    @Override
    public ResponseEntity<ModelApiResponse> uploadFile(@PathVariable Long petId,
                                                       @RequestParam MultipartFile file,
                                                       @RequestParam String additionalMetadata) {
        return ResponseEntity.ok(petService.uploadImageForPet(petId, file));
    }


}
