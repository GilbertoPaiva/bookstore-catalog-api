package com.gilbertopaiva.bookstore_catalog_api.book;

import com.gilbertopaiva.bookstore_catalog_api.category.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "books")
/*
 * SOLUÇÃO N+1: sem @NamedEntityGraph, ao buscar N livros o Hibernate disparava
 * 1 query para listar os livros + N queries adicionais para carregar a category
 * de cada um (pois o relacionamento é LAZY). Com @NamedEntityGraph declaramos
 * um "plano de carregamento" que o repositório usa para fazer um único
 * JOIN FETCH, independentemente do FetchType definido no campo.
 */
@NamedEntityGraph(
        name = "Book.category",
        attributeNodes = @NamedAttributeNode("category")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String title;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String author;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer stockQuantity;

    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    @Column
    private Integer publishedYear;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ManyToOne: cada livro pertence a uma categoria
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}

