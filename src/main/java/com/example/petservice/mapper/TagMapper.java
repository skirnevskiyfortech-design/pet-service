package com.example.petservice.mapper;


import com.example.petservice.model.Tag;
import org.mapstruct.Mapper;
import org.openapi.example.model.TagDto;

@Mapper(componentModel = "spring")
public interface TagMapper {
    Tag toTag (TagDto tagDto);
    TagDto toTagDto (Tag tag);
}
