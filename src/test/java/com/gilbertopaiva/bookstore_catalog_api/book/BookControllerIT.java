package com.gilbertopaiva.bookstore_catalog_api.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gilbertopaiva.bookstore_catalog_api.book.dto.BookRequest;
import com.gilbertopaiva.bookstore_catalog_api.category.Category;
import com.gilbertopaiva.bookstore_catalog_api.category.CategoryRepository;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("BookController — integration tests")
class BookControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired BookRepository bookRepository;
    @Autowired CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        category = categoryRepository.save(
                Category.builder().name("Technology").description("Tech books").build());
    }

    private BookRequest buildRequest(String title, String isbn) {
        return new BookRequest(
                title,
                "Some Author",
                isbn,
                new BigDecimal("49.90"),
                5,
                "A great book",
                2023,
                category.getId()
        );
    }


    @Test
    @DisplayName("Fluxo completo: criar → buscar → filtrar → deletar")
    void fullFlow_createSearchFilterDelete() throws Exception {
        String body = objectMapper.writeValueAsString(buildRequest("Clean Code", "9780132350884"));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.categoryName").value("Technology"));

        mockMvc.perform(get("/books").param("title", "clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"));

        mockMvc.perform(get("/books").param("title", "clean"))
                .andExpect(jsonPath("$.content[0].id").isString());

        mockMvc.perform(get("/books")
                        .param("minPrice", "10.00")
                        .param("maxPrice", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(get("/books")
                        .param("minPrice", "500.00")
                        .param("maxPrice", "999.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }


    @Test
    @DisplayName("POST /books → 201 e retorna livro criado")
    void shouldCreateBook() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("Refactoring", "9780201485677"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Refactoring"))
                .andExpect(jsonPath("$.isbn").value("9780201485677"))
                .andExpect(jsonPath("$.categoryId").value(category.getId().toString()));
    }

    @Test
    @DisplayName("POST /books → 422 quando título está em branco")
    void shouldReturn422_whenTitleIsBlank() throws Exception {
        BookRequest invalid = new BookRequest(
                "", "Author", "1234567890", new BigDecimal("10.00"), 1, null, null, category.getId());

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fields.title").exists());
    }

    @Test
    @DisplayName("POST /books → 404 quando categoria não existe")
    void shouldReturn404_whenCategoryNotFound() throws Exception {
        BookRequest request = new BookRequest(
                "Title", "Author", "1234567890", new BigDecimal("10.00"), 1, null, null,
                UUID.randomUUID());

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /books → 409 quando ISBN já existe")
    void shouldReturn409_whenIsbnAlreadyExists() throws Exception {
        String body = objectMapper.writeValueAsString(buildRequest("Clean Code", "9780132350884"));

        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }


    @Test
    @DisplayName("GET /books → 200 com paginação padrão")
    void shouldReturnPagedBooks() throws Exception {
        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildRequest("Book A", "ISBN-001"))));
        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildRequest("Book B", "ISBN-002"))));

        mockMvc.perform(get("/books").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @DisplayName("GET /books → filtra por categoryId corretamente")
    void shouldFilterByCategoryId() throws Exception {
        Category other = categoryRepository.save(
                Category.builder().name("Fantasy").description("Fantasy books").build());

        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildRequest("Tech Book", "ISBN-T01"))));

        BookRequest fantasyBook = new BookRequest(
                "Fantasy Book", "Author", "ISBN-F01",
                new BigDecimal("39.90"), 3, null, 2020, other.getId());
        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fantasyBook)));

        mockMvc.perform(get("/books").param("categoryId", category.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Tech Book"));
    }

    @Test
    @DisplayName("GET /books/{id} → 200 quando livro existe")
    void shouldReturnBookById() throws Exception {
        String response = mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("Clean Code", "9780132350884"))))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/books/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @DisplayName("GET /books/{id} → 404 quando livro não existe")
    void shouldReturn404_whenBookNotFound() throws Exception {
        mockMvc.perform(get("/books/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }


    @Test
    @DisplayName("PUT /books/{id} → 200 e retorna livro atualizado")
    void shouldUpdateBook() throws Exception {
        String response = mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("Clean Code", "9780132350884"))))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        BookRequest updateRequest = new BookRequest(
                "Clean Code — Updated", "Robert C. Martin",
                "9780132350884", new BigDecimal("79.90"), 3, "Updated desc", 2024, category.getId());

        mockMvc.perform(put("/books/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code — Updated"))
                .andExpect(jsonPath("$.price").value(79.90));
    }


    @Test
    @DisplayName("DELETE /books/{id} → 204 e livro não é mais encontrado")
    void shouldDeleteBook() throws Exception {
        String response = mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("Clean Code", "9780132350884"))))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(delete("/books/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/books/{id}", id))
                .andExpect(status().isNotFound());
    }
}

