package com.example.petservice.mapper;

import com.example.petservice.model.Category;
import org.mapstruct.Mapper;
import org.openapi.example.model.CategoryDto;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryDto dto);
    CategoryDto toCategoryDto(Category category);
}