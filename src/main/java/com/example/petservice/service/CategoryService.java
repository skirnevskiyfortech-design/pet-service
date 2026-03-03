package com.example.petservice.service;

import com.example.petservice.exception.ValidationException;
import com.example.petservice.model.Category;
import com.example.petservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.openapi.example.model.CategoryDto;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category findOrCreateCategory(CategoryDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ValidationException("Category name cannot be empty");
        }

        return categoryRepository.findByNameIgnoreCase(dto.getName())
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName(dto.getName());
                    return categoryRepository.save(category);
                });
    }

}
