package com.gilbertopaiva.bookstore_catalog_api.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gilbertopaiva.bookstore_catalog_api.category.dto.CategoryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CategoryController — integration tests")
class CategoryControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /categories → 201 e retorna categoria criada")
    void shouldCreateCategory() throws Exception {
        CategoryRequest request = new CategoryRequest("Fiction", "Fiction books");

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Fiction"))
                .andExpect(jsonPath("$.description").value("Fiction books"));
    }

    @Test
    @DisplayName("POST /categories → 422 quando nome está em branco")
    void shouldReturn422_whenNameIsBlank() throws Exception {
        CategoryRequest request = new CategoryRequest("", "Some description");

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fields.name").exists());
    }

    @Test
    @DisplayName("GET /categories → 200 e retorna lista de categorias")
    void shouldReturnAllCategories() throws Exception {
        categoryRepository.save(Category.builder().name("Fiction").description("Fiction books").build());
        categoryRepository.save(Category.builder().name("Science").description("Science books").build());

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Fiction", "Science")));
    }

    @Test
    @DisplayName("GET /categories/{id} → 200 quando categoria existe")
    void shouldReturnCategoryById() throws Exception {
        Category saved = categoryRepository.save(
                Category.builder().name("Fiction").description("Fiction books").build());

        mockMvc.perform(get("/categories/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Fiction"));
    }

    @Test
    @DisplayName("GET /categories/{id} → 404 quando categoria não existe")
    void shouldReturn404_whenCategoryNotFound() throws Exception {
        mockMvc.perform(get("/categories/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("999")));
    }

    @Test
    @DisplayName("PUT /categories/{id} → 200 e retorna categoria atualizada")
    void shouldUpdateCategory() throws Exception {
        Category saved = categoryRepository.save(
                Category.builder().name("Fiction").description("Fiction books").build());

        CategoryRequest updateRequest = new CategoryRequest("Science Fiction", "Sci-Fi books");

        mockMvc.perform(put("/categories/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Science Fiction"));
    }

    @Test
    @DisplayName("DELETE /categories/{id} → 204 quando categoria existe")
    void shouldDeleteCategory() throws Exception {
        Category saved = categoryRepository.save(
                Category.builder().name("Fiction").description("Fiction books").build());

        mockMvc.perform(delete("/categories/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/categories/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }
}

