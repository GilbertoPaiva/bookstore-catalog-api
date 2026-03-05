package com.gilbertopaiva.bookstore_catalog_api.book;

import com.gilbertopaiva.bookstore_catalog_api.book.dto.BookFilter;
import com.gilbertopaiva.bookstore_catalog_api.book.dto.BookRequest;
import com.gilbertopaiva.bookstore_catalog_api.book.dto.BookResponse;
import com.gilbertopaiva.bookstore_catalog_api.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@Tag(name = "Books", description = "CRUD de livros com paginação, filtros e cache")
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Listar livros", description = "Retorna uma página de livros com filtros opcionais por título, categoria e faixa de preço.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<Page<BookResponse>> listBooks(
            @Parameter(description = "Filtro por título (case insensitive, like)") @RequestParam(required = false) String title,
            @Parameter(description = "Filtro por ID de categoria") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Preço mínimo (inclusive)") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Preço máximo (inclusive)") @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {

        BookFilter filter = new BookFilter(title, categoryId, minPrice, maxPrice);
        return ResponseEntity.ok(bookService.listBooks(filter, pageable));
    }

    @Operation(summary = "Buscar livro por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Livro encontrado"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    @Operation(summary = "Criar livro")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Livro criado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "ISBN já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request));
    }

    @Operation(summary = "Atualizar livro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Livro atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Livro ou categoria não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "ISBN já cadastrado por outro livro",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookService.update(id, request));
    }

    @Operation(summary = "Deletar livro")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Livro deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
