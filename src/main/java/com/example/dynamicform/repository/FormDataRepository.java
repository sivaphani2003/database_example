package com.example.dynamicform.repository;

import com.example.dynamicform.model.FormData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormDataRepository extends MongoRepository<FormData, String> {

    List<FormData> findByFirstNameAndEmail(String firstName, String email);
}
