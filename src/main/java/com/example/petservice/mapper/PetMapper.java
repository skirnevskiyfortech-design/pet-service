package com.example.petservice.mapper;

import com.example.petservice.model.Pet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapi.example.model.PetRequest;
import org.openapi.example.model.PetResponse;

@Mapper(componentModel = "spring")
public interface PetMapper {

    @Mapping(source = "category", target = "category")
    Pet toPet(PetRequest request);

    PetResponse toPetResponse(Pet pet);
}