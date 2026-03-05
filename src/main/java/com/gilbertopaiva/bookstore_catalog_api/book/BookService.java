package com.gilbertopaiva.bookstore_catalog_api.book;

import com.gilbertopaiva.bookstore_catalog_api.book.dto.BookFilter;
import com.gilbertopaiva.bookstore_catalog_api.book.dto.BookRequest;
import com.gilbertopaiva.bookstore_catalog_api.book.dto.BookResponse;
import com.gilbertopaiva.bookstore_catalog_api.book.exception.BookAlreadyExistsException;
import com.gilbertopaiva.bookstore_catalog_api.book.exception.BookNotFoundException;
import com.gilbertopaiva.bookstore_catalog_api.category.Category;
import com.gilbertopaiva.bookstore_catalog_api.category.CategoryRepository;
import com.gilbertopaiva.bookstore_catalog_api.category.exception.CategoryNotFoundException;
import com.gilbertopaiva.bookstore_catalog_api.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;


    @Cacheable(value = CacheConfig.CACHE_BOOKS, key = "#filter.toString() + #pageable.toString()")
    @Transactional(readOnly = true)
    public Page<BookResponse> listBooks(BookFilter filter, Pageable pageable) {
        return bookRepository.findAll(BookSpecification.withFilters(filter), pageable)
                .map(BookResponse::from);
    }


    @Cacheable(value = CacheConfig.CACHE_BOOK, key = "#id")
    @Transactional(readOnly = true)
    public BookResponse findById(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        return BookResponse.from(book);
    }


    @CacheEvict(value = CacheConfig.CACHE_BOOKS, allEntries = true)
    @Transactional
    public BookResponse create(BookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new BookAlreadyExistsException(request.isbn());
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .description(request.description())
                .publishedYear(request.publishedYear())
                .category(category)
                .build();

        return BookResponse.from(bookRepository.save(book));
    }


    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_BOOK,  key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_BOOKS, allEntries = true)
    })
    @Transactional
    public BookResponse update(UUID id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        if (bookRepository.existsByIsbnAndIdNot(request.isbn(), id)) {
            throw new BookAlreadyExistsException(request.isbn());
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setPrice(request.price());
        book.setStockQuantity(request.stockQuantity());
        book.setDescription(request.description());
        book.setPublishedYear(request.publishedYear());
        book.setCategory(category);

        return BookResponse.from(bookRepository.save(book));
    }


    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_BOOK,  key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_BOOKS, allEntries = true)
    })
    @Transactional
    public void delete(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }
}
