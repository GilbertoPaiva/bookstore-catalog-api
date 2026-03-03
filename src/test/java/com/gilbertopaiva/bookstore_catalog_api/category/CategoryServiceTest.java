package com.gilbertopaiva.bookstore_catalog_api.category;

import com.gilbertopaiva.bookstore_catalog_api.category.dto.CategoryRequest;
import com.gilbertopaiva.bookstore_catalog_api.category.dto.CategoryResponse;
import com.gilbertopaiva.bookstore_catalog_api.category.exception.CategoryNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService — unit tests")
class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;

    @InjectMocks CategoryService categoryService;

    private Category category;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Fiction")
                .description("Fiction books")
                .build();

        categoryRequest = new CategoryRequest("Fiction", "Fiction books");
    }


    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("deve retornar lista de categorias")
        void shouldReturnListOfCategories() {
            when(categoryRepository.findAll()).thenReturn(List.of(category));

            List<CategoryResponse> result = categoryService.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Fiction");
            verify(categoryRepository).findAll();
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há categorias")
        void shouldReturnEmptyList_whenNoCategories() {
            when(categoryRepository.findAll()).thenReturn(List.of());

            List<CategoryResponse> result = categoryService.findAll();

            assertThat(result).isEmpty();
        }
    }


    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("deve retornar CategoryResponse quando categoria existe")
        void shouldReturnCategoryResponse_whenCategoryExists() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

            CategoryResponse response = categoryService.findById(1L);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("Fiction");
            verify(categoryRepository).findById(1L);
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando categoria não existe")
        void shouldThrowCategoryNotFoundException_whenCategoryNotFound() {
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.findById(99L))
                    .isInstanceOf(CategoryNotFoundException.class)
                    .hasMessageContaining("99");

            verify(categoryRepository).findById(99L);
        }
    }


    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("deve criar e retornar categoria quando dados são válidos")
        void shouldCreateAndReturnCategory_whenDataIsValid() {
            when(categoryRepository.save(any(Category.class))).thenReturn(category);

            CategoryResponse response = categoryService.create(categoryRequest);

            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("Fiction");
            assertThat(response.description()).isEqualTo("Fiction books");
            verify(categoryRepository).save(any(Category.class));
        }
    }


    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("deve atualizar e retornar categoria quando dados são válidos")
        void shouldUpdateAndReturnCategory_whenDataIsValid() {
            CategoryRequest updateRequest = new CategoryRequest("Science Fiction", "Sci-Fi books");
            Category updatedCategory = Category.builder()
                    .id(1L).name("Science Fiction").description("Sci-Fi books").build();

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

            CategoryResponse response = categoryService.update(1L, updateRequest);

            assertThat(response.name()).isEqualTo("Science Fiction");
            assertThat(response.description()).isEqualTo("Sci-Fi books");
            verify(categoryRepository).findById(1L);
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando categoria não existe no update")
        void shouldThrowCategoryNotFoundException_whenCategoryNotFoundOnUpdate() {
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.update(99L, categoryRequest))
                    .isInstanceOf(CategoryNotFoundException.class)
                    .hasMessageContaining("99");

            verify(categoryRepository, never()).save(any());
        }
    }


    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deve deletar categoria quando ela existe")
        void shouldDeleteCategory_whenCategoryExists() {
            when(categoryRepository.existsById(1L)).thenReturn(true);
            doNothing().when(categoryRepository).deleteById(1L);

            assertThatCode(() -> categoryService.delete(1L)).doesNotThrowAnyException();

            verify(categoryRepository).existsById(1L);
            verify(categoryRepository).deleteById(1L);
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando categoria não existe no delete")
        void shouldThrowCategoryNotFoundException_whenCategoryNotFoundOnDelete() {
            when(categoryRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> categoryService.delete(99L))
                    .isInstanceOf(CategoryNotFoundException.class)
                    .hasMessageContaining("99");

            verify(categoryRepository, never()).deleteById(any());
        }
    }
}

