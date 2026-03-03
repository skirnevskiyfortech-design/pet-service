package com.example.petservice.service;

import com.example.petservice.model.Tag;
import com.example.petservice.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.openapi.example.model.TagDto;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public Tag findOrCreateTag(TagDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be empty");
        }

        return tagRepository.findByNameIgnoreCase(dto.getName())
                .orElseGet(() -> {
                    Tag tag = new Tag();
                    tag.setName(dto.getName());
                    return tagRepository.save(tag);
                });
    }
}
