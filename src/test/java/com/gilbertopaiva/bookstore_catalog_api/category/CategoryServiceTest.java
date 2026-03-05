package com.gilbertopaiva.bookstore_catalog_api.category;

import com.gilbertopaiva.bookstore_catalog_api.category.dto.CategoryRequest;
import com.gilbertopaiva.bookstore_catalog_api.category.dto.CategoryResponse;
import com.gilbertopaiva.bookstore_catalog_api.category.exception.CategoryAlreadyExistsException;
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
import java.util.UUID;

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

    private static final UUID CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID UNKNOWN_ID  = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(CATEGORY_ID)
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
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));

            CategoryResponse response = categoryService.findById(CATEGORY_ID);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(CATEGORY_ID);
            assertThat(response.name()).isEqualTo("Fiction");
            verify(categoryRepository).findById(CATEGORY_ID);
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando categoria não existe")
        void shouldThrowCategoryNotFoundException_whenCategoryNotFound() {
            when(categoryRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.findById(UNKNOWN_ID))
                    .isInstanceOf(CategoryNotFoundException.class)
                    .hasMessageContaining(UNKNOWN_ID.toString());

            verify(categoryRepository).findById(UNKNOWN_ID);
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

        @Test
        @DisplayName("deve lançar CategoryAlreadyExistsException quando categoria com mesmo nome já existe")
        void shouldThrowCategoryAlreadyExistsException_whenNameAlreadyExists() {
            when(categoryRepository.existsByNameIgnoreCase(categoryRequest.name())).thenReturn(true);

            assertThatThrownBy(() -> categoryService.create(categoryRequest))
                    .isInstanceOf(CategoryAlreadyExistsException.class)
                    .hasMessageContaining(categoryRequest.name());

            verify(categoryRepository, never()).save(any());
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
                    .id(CATEGORY_ID).name("Science Fiction").description("Sci-Fi books").build();

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
            when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

            CategoryResponse response = categoryService.update(CATEGORY_ID, updateRequest);

            assertThat(response.name()).isEqualTo("Science Fiction");
            assertThat(response.description()).isEqualTo("Sci-Fi books");
            verify(categoryRepository).findById(CATEGORY_ID);
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("deve lançar CategoryAlreadyExistsException quando tenta atualizar para um nome que já existe em outra categoria")
        void shouldThrowCategoryAlreadyExistsException_whenUpdatingToExistingName() {
            CategoryRequest updateRequest = new CategoryRequest("Existing Category", "Description");
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Existing Category", CATEGORY_ID)).thenReturn(true);

            assertThatThrownBy(() -> categoryService.update(CATEGORY_ID, updateRequest))
                    .isInstanceOf(CategoryAlreadyExistsException.class)
                    .hasMessageContaining("Existing Category");

            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando categoria não existe no update")
        void shouldThrowCategoryNotFoundException_whenCategoryNotFoundOnUpdate() {
            when(categoryRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.update(UNKNOWN_ID, categoryRequest))
                    .isInstanceOf(CategoryNotFoundException.class)
                    .hasMessageContaining(UNKNOWN_ID.toString());

            verify(categoryRepository, never()).save(any());
        }
    }


    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deve deletar categoria quando ela existe")
        void shouldDeleteCategory_whenCategoryExists() {
            when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(true);
            doNothing().when(categoryRepository).deleteById(CATEGORY_ID);

            assertThatCode(() -> categoryService.delete(CATEGORY_ID)).doesNotThrowAnyException();

            verify(categoryRepository).existsById(CATEGORY_ID);
            verify(categoryRepository).deleteById(CATEGORY_ID);
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando categoria não existe no delete")
        void shouldThrowCategoryNotFoundException_whenCategoryNotFoundOnDelete() {
            when(categoryRepository.existsById(UNKNOWN_ID)).thenReturn(false);

            assertThatThrownBy(() -> categoryService.delete(UNKNOWN_ID))
                    .isInstanceOf(CategoryNotFoundException.class)
                    .hasMessageContaining(UNKNOWN_ID.toString());

            verify(categoryRepository, never()).deleteById(any());
        }
    }
}

