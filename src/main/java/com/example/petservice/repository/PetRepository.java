package com.example.petservice.repository;

import com.example.petservice.model.Pet;
import org.openapi.example.model.PetRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByStatus(PetRequest.StatusEnum status);

    @Query("SELECT p FROM Pet p " +
            "JOIN p.tags t " +
            "WHERE t.name IN :tagNames " +
            "GROUP BY p " +
            "ORDER BY COUNT(t) DESC")
    List<Pet> findByTags(@Param("tagNames") List<String> tagNames);
}
