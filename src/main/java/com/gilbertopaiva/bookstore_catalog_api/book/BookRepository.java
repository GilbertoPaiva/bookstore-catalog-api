package com.gilbertopaiva.bookstore_catalog_api.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID>, JpaSpecificationExecutor<Book> {

    /*
     * SOLUÇÃO N+1: @EntityGraph("Book.category") instrui o Hibernate a fazer
     * um JOIN FETCH na tabela categories sempre que este método for chamado,
     * garantindo que todos os livros e suas categorias sejam carregados em
     * uma única query SQL — independentemente do FetchType.LAZY do campo.
     */
    @EntityGraph("Book.category")
    Page<Book> findAll(Specification<Book> spec, Pageable pageable);

    @EntityGraph("Book.category")
    Optional<Book> findById(UUID id);
}

