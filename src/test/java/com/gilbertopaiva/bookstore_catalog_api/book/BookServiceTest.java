package com.gilbertopaiva.bookstore_catalog_api.book;

import com.gilbertopaiva.bookstore_catalog_api.book.dto.BookFilter;
import com.gilbertopaiva.bookstore_catalog_api.book.dto.BookRequest;
import com.gilbertopaiva.bookstore_catalog_api.book.dto.BookResponse;
import com.gilbertopaiva.bookstore_catalog_api.book.exception.BookNotFoundException;
import com.gilbertopaiva.bookstore_catalog_api.category.Category;
import com.gilbertopaiva.bookstore_catalog_api.category.CategoryRepository;
import com.gilbertopaiva.bookstore_catalog_api.category.exception.CategoryNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService — unit tests")
class BookServiceTest {

    @Mock BookRepository bookRepository;
    @Mock CategoryRepository categoryRepository;

    @InjectMocks BookService bookService;

    private Category category;
    private Book book;
    private BookRequest bookRequest;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Fiction")
                .description("Fiction books")
                .build();

        book = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .price(new BigDecimal("59.90"))
                .stockQuantity(10)
                .description("A book about clean code")
                .publishedYear(2008)
                .category(category)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        bookRequest = new BookRequest(
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                new BigDecimal("59.90"),
                10,
                "A book about clean code",
                2008,
                1L
        );
    }


    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("deve retornar BookResponse quando livro existe")
        void shouldReturnBookResponse_whenBookExists() {
            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

            BookResponse response = bookService.findById(1L);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.title()).isEqualTo("Clean Code");
            assertThat(response.categoryName()).isEqualTo("Fiction");
            verify(bookRepository).findById(1L);
        }

        @Test
        @DisplayName("deve lançar BookNotFoundException quando livro não existe")
        void shouldThrowBookNotFoundException_whenBookNotFound() {
            when(bookRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.findById(99L))
                    .isInstanceOf(BookNotFoundException.class)
                    .hasMessageContaining("99");

            verify(bookRepository).findById(99L);
        }
    }


    @Nested
    @DisplayName("listBooks")
    class ListBooks {

        @Test
        @DisplayName("deve retornar página de livros com filtros vazios")
        void shouldReturnPageOfBooks_whenNoFiltersApplied() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("title"));
            Page<Book> bookPage = new PageImpl<>(List.of(book), pageable, 1);

            when(bookRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(bookPage);

            BookFilter filter = new BookFilter(null, null, null, null);
            Page<BookResponse> result = bookService.listBooks(filter, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).title()).isEqualTo("Clean Code");
            verify(bookRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("deve retornar página vazia quando nenhum livro combina com o filtro")
        void shouldReturnEmptyPage_whenNoBooksMatchFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Book> emptyPage = Page.empty(pageable);

            when(bookRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(emptyPage);

            BookFilter filter = new BookFilter("inexistente", null, null, null);
            Page<BookResponse> result = bookService.listBooks(filter, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }


    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("deve criar e retornar livro quando dados são válidos")
        void shouldCreateAndReturnBook_whenDataIsValid() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(bookRepository.save(any(Book.class))).thenReturn(book);

            BookResponse response = bookService.create(bookRequest);

            assertThat(response).isNotNull();
            assertThat(response.title()).isEqualTo("Clean Code");
            assertThat(response.categoryId()).isEqualTo(1L);
            verify(categoryRepository).findById(1L);
            verify(bookRepository).save(any(Book.class));
        }

        @Test
        @DisplayName("deve lançar CategoryNotFoundException quando categoria não existe")
        void shouldThrowCategoryNotFoundException_whenCategoryNotFound() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.create(bookRequest))
                    .isInstanceOf(CategoryNotFoundException.class)
                    .hasMessageContaining("1");

            verify(bookRepository, never()).save(any());
        }
    }


    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("deve atualizar e retornar livro quando dados são válidos")
        void shouldUpdateAndReturnBook_whenDataIsValid() {
            BookRequest updateRequest = new BookRequest(
                    "Clean Code 2nd Ed",
                    "Robert C. Martin",
                    "9780132350884",
                    new BigDecimal("69.90"),
                    5,
                    "Updated description",
                    2024,
                    1L
            );

            Book updatedBook = Book.builder()
                    .id(1L).title("Clean Code 2nd Ed").author("Robert C. Martin")
                    .isbn("9780132350884").price(new BigDecimal("69.90"))
                    .stockQuantity(5).category(category)
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                    .build();

            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

            BookResponse response = bookService.update(1L, updateRequest);

            assertThat(response.title()).isEqualTo("Clean Code 2nd Ed");
            assertThat(response.price()).isEqualByComparingTo("69.90");
            verify(bookRepository).save(any(Book.class));
        }

        @Test
        @DisplayName("deve lançar BookNotFoundException quando livro não existe no update")
        void shouldThrowBookNotFoundException_whenBookNotFoundOnUpdate() {
            when(bookRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.update(99L, bookRequest))
                    .isInstanceOf(BookNotFoundException.class)
                    .hasMessageContaining("99");

            verify(bookRepository, never()).save(any());
        }
    }


    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deve deletar livro quando ele existe")
        void shouldDeleteBook_whenBookExists() {
            when(bookRepository.existsById(1L)).thenReturn(true);
            doNothing().when(bookRepository).deleteById(1L);

            assertThatCode(() -> bookService.delete(1L)).doesNotThrowAnyException();

            verify(bookRepository).existsById(1L);
            verify(bookRepository).deleteById(1L);
        }

        @Test
        @DisplayName("deve lançar BookNotFoundException quando livro não existe no delete")
        void shouldThrowBookNotFoundException_whenBookNotFoundOnDelete() {
            when(bookRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> bookService.delete(99L))
                    .isInstanceOf(BookNotFoundException.class)
                    .hasMessageContaining("99");

            verify(bookRepository, never()).deleteById(any());
        }
    }
}

