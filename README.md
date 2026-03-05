# 📚 Bookstore Catalog API

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-brightgreen?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![Caffeine Cache](https://img.shields.io/badge/Cache-Caffeine-yellow)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![Tests](https://img.shields.io/badge/Tests-20%20passing-brightgreen)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

API REST de catálogo de livros construída como **Projeto #3 do portfólio**. Foco em
Cache com Caffeine, paginação e filtros avançados com Spring Data, resolução do problema
N+1 com `@EntityGraph`, IDs como **UUID**, Docker Compose e testes de integração com `@SpringBootTest`.

---

## 🗂️ Stack

| Camada         | Tecnologia                              |
|----------------|-----------------------------------------|
| Linguagem      | Java 21                                 |
| Framework      | Spring Boot 3.2.5                       |
| Banco de dados | PostgreSQL 16                           |
| Cache          | Caffeine (via Spring Cache)             |
| Persistência   | Spring Data JPA / Hibernate 6           |
| Documentação   | SpringDoc OpenAPI 3 / Swagger UI        |
| Testes         | JUnit 5 + Mockito + MockMvc (H2)        |
| Containerização| Docker + Docker Compose                 |
| Deploy         | Railway                                 |
| Produção (Swagger) | [Swagger UI](http://209.97.147.6/bookstore/swagger-ui/index.html) |

---

## 🗄️ Diagrama ER

```
┌──────────────────────────┐          ┌─────────────────────────────┐
│         categories       │          │           books             │
├──────────────────────────┤          ├─────────────────────────────┤
│ id           UUID PK     │◄────┐    │ id            UUID PK       │
│ name         VARCHAR(100)│     └────│ category_id   UUID FK       │
│ description  VARCHAR(500)│          │ title         VARCHAR(255)  │
└──────────────────────────┘          │ author        VARCHAR(255)  │
                                      │ isbn          VARCHAR(20) UQ│
                                      │ price         DECIMAL(10,2) │
                                      │ stock_quantity INTEGER      │
                                      │ description   VARCHAR(1000) │
                                      │ published_year INTEGER      │
                                      │ created_at    TIMESTAMP     │
                                      │ updated_at    TIMESTAMP     │
                                      └─────────────────────────────┘
```

Relacionamento: `Book` **N → 1** `Category` (ManyToOne, LAZY + `@EntityGraph` para evitar N+1).

---

## 🔌 Endpoints

### Categories

| Método | Endpoint            | Descrição                    | Status de sucesso |
|--------|---------------------|------------------------------|-------------------|
| GET    | `/categories`       | Lista todas as categorias    | 200               |
| GET    | `/categories/{id}`  | Busca categoria por ID       | 200               |
| POST   | `/categories`       | Cria nova categoria          | 201               |
| PUT    | `/categories/{id}`  | Atualiza categoria           | 200               |
| DELETE | `/categories/{id}`  | Remove categoria             | 204               |

### Books

| Método | Endpoint          | Descrição                                        | Status de sucesso |
|--------|-------------------|--------------------------------------------------|-------------------|
| GET    | `/books`          | Lista livros paginados com filtros opcionais     | 200               |
| GET    | `/books/{id}`     | Busca livro por ID (cacheado)                    | 200               |
| POST   | `/books`          | Cria novo livro                                  | 201               |
| PUT    | `/books/{id}`     | Atualiza livro                                   | 200               |
| DELETE | `/books/{id}`     | Remove livro                                     | 204               |
| GET    | `/books/cache/stats` | Estatísticas do cache (hits, misses, hit rate) | 200               |

#### Filtros disponíveis em `GET /books`

| Parâmetro    | Tipo       | Descrição                              |
|--------------|------------|----------------------------------------|
| `title`      | String     | Filtra por título (like, case-insensitive) |
| `categoryId` | UUID       | Filtra por ID de categoria             |
| `minPrice`   | BigDecimal | Preço mínimo (inclusive)               |
| `maxPrice`   | BigDecimal | Preço máximo (inclusive)               |
| `page`       | int        | Número da página (padrão: 0)           |
| `size`       | int        | Tamanho da página (padrão: 10)         |
| `sort`       | String     | Campo e direção (ex: `title,asc`)      |

---

## 🚀 Como rodar

### Pré-requisitos
- Docker e Docker Compose instalados, **ou**
- Java 21 + PostgreSQL 16 instalados localmente

### Com Docker Compose (recomendado)

```bash
# 1. Clone o repositório
git clone https://github.com/seu-usuario/bookstore-catalog-api.git
cd bookstore-catalog-api

# 2. Configure as variáveis de ambiente
cp .env.example .env
# Edite .env com suas preferências (opcional — os defaults já funcionam)

# 3. Suba a aplicação + banco
docker-compose up --build

# A API estará disponível em:
# http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### Sem Docker (local)

```bash
# 1. Crie o banco PostgreSQL
createdb bookstore_db

# 2. Configure as variáveis de ambiente
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=bookstore_db
export DB_USER=postgres
export DB_PASSWORD=postgres

# 3. Execute a aplicação
./mvnw spring-boot:run

# 4. Rode os testes
./mvnw test
```

---

## ⚙️ Variáveis de Ambiente

| Variável      | Padrão         | Descrição                    |
|---------------|----------------|------------------------------|
| `DB_HOST`     | `localhost`    | Host do PostgreSQL           |
| `DB_PORT`     | `5432`         | Porta do PostgreSQL          |
| `DB_NAME`     | `bookstore_db` | Nome do banco de dados       |
| `DB_USER`     | `postgres`     | Usuário do banco             |
| `DB_PASSWORD` | —              | Senha do banco               |
| `PORT`        | `8080`         | Porta HTTP da aplicação      |

Copie `.env.example` para `.env` e preencha os valores antes de rodar.

---

## 📋 Exemplos de Request / Response

### Criar categoria

**POST** `/categories`
```json
{
  "name": "Technology",
  "description": "Books about software, hardware and computing"
}
```
**Response 201**
```json
{
  "id": "ac120002-9cbe-1640-819c-be89b6790000",
  "name": "Technology",
  "description": "Books about software, hardware and computing"
}
```

### Criar livro

**POST** `/books`
```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "9780132350884",
  "price": 59.90,
  "stockQuantity": 10,
  "description": "A handbook of agile software craftsmanship",
  "publishedYear": 2008,
  "categoryId": "ac120002-9cbe-1640-819c-be89b6790000"
}
```
**Response 201**
```json
{
  "id": "bc230003-adcf-2751-920d-cf9a07801111",
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "9780132350884",
  "price": 59.90,
  "stockQuantity": 10,
  "description": "A handbook of agile software craftsmanship",
  "publishedYear": 2008,
  "categoryId": "ac120002-9cbe-1640-819c-be89b6790000",
  "categoryName": "Technology",
  "createdAt": "2026-03-02T10:00:00",
  "updatedAt": "2026-03-02T10:00:00"
}
```

### Listar livros com filtros e paginação

**GET** `/books?title=clean&minPrice=10&maxPrice=100&page=0&size=5&sort=title,asc`

**Response 200**
```json
{
  "content": [
    {
      "id": "bc230003-adcf-2751-920d-cf9a07801111",
      "title": "Clean Code",
      "author": "Robert C. Martin",
      "isbn": "9780132350884",
      "price": 59.90,
      "categoryName": "Technology"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 5,
  "number": 0
}
```

### Estatísticas do cache

**GET** `/books/cache/stats`
```json
{
  "books": {
    "hitCount": 42,
    "missCount": 5,
    "hitRate": "0,8936",
    "evictionCount": 0
  },
  "book": {
    "hitCount": 18,
    "missCount": 3,
    "hitRate": "0,8571",
    "evictionCount": 0
  }
}
```

### Erro de validação (422)

**POST** `/books` com `title: ""`
```json
{
  "timestamp": "2026-03-02T10:00:00",
  "status": 422,
  "error": "Unprocessable Entity",
  "path": "/books",
  "fields": {
    "title": "Title is required"
  }
}
```

---

## ⚡ Performance — Cache com Caffeine

O cache é configurado com TTL de **10 minutos** e capacidade máxima de **500 entradas**.

| Operação               | Sem cache | Com cache (hit) | Melhoria |
|------------------------|-----------|-----------------|----------|
| `GET /books/{id}`      | ~12 ms    | ~0,3 ms         | ~40×     |
| `GET /books` (listagem)| ~35 ms    | ~0,5 ms         | ~70×     |

> Valores medidos localmente com banco PostgreSQL em Docker. Latências reais variam com hardware e tamanho do dataset.

**Como o cache funciona nesta API:**

- `@Cacheable("book")` — `findById`: armazena o resultado por ID. Requisições subsequentes para o mesmo ID retornam direto do cache sem tocar o banco.
- `@Cacheable("books")` — `listBooks`: armazena páginas de listagem. A chave inclui os parâmetros de filtro e paginação.
- `@CacheEvict` em `create`, `update` e `delete` — invalida as entradas afetadas, garantindo consistência.
- `GET /books/cache/stats` — endpoint manual para inspecionar **hit rate**, misses e evictions em tempo real.

---

## 🧪 Testes

```bash
# Todos os testes (unit + integração)
./mvnw test

# Apenas testes de integração
./mvnw test -Dtest="BookControllerIT,CategoryControllerIT"

# Apenas testes unitários
./mvnw test -Dtest="BookServiceTest,CategoryServiceTest"
```

| Suite                   | Tipo        | Cobertura principal                               |
|-------------------------|-------------|---------------------------------------------------|
| `BookServiceTest`       | Unitário    | findById, create, update, delete, listBooks       |
| `CategoryServiceTest`   | Unitário    | findById, create, update, delete, findAll         |
| `BookControllerIT`      | Integração  | Fluxo completo, filtros, paginação, erros HTTP    |
| `CategoryControllerIT`  | Integração  | CRUD completo, validações, erros HTTP             |

Os testes de integração sobem o contexto completo do Spring (`@SpringBootTest`) com banco
H2 in-memory (perfil `test`), sem dependência de PostgreSQL ou Docker.

---

## 🏗️ Arquitetura

```
src/main/java/com/gilbertopaiva/bookstore_catalog_api/
├── book/
│   ├── Book.java                  # Entidade JPA com @NamedEntityGraph (N+1 fix)
│   ├── BookController.java        # REST endpoints + Swagger @Operation
│   ├── BookService.java           # Lógica de negócio + @Cacheable/@CacheEvict
│   ├── BookRepository.java        # JpaRepository + @EntityGraph
│   ├── BookSpecification.java     # Filtros dinâmicos via Specification
│   ├── CacheStatsController.java  # GET /books/cache/stats
│   └── dto/
│       ├── BookRequest.java       # Payload de entrada com Bean Validation
│       ├── BookResponse.java      # Payload de saída (inclui categoryName)
│       └── BookFilter.java        # Record para encapsular filtros
├── category/
│   ├── Category.java
│   ├── CategoryController.java
│   ├── CategoryService.java
│   ├── CategoryRepository.java
│   └── dto/
├── config/
│   ├── CacheConfig.java           # CaffeineCacheManager com TTL + recordStats()
│   └── OpenApiConfig.java         # Bean OpenAPI com metadados
└── exception/
    ├── GlobalExceptionHandler.java  # @ControllerAdvice
    ├── ErrorResponse.java
    └── ValidationErrorResponse.java
```

---

## 📖 Aprendizados deste projeto

1. **Cache com Caffeine** — configuração programática com `recordStats()`, uso de `@Cacheable`/`@CacheEvict` e endpoint de observabilidade manual.
2. **Paginação e filtros** — `Pageable` via `@PageableDefault`, `Specification` para predicados dinâmicos combinados com AND.
3. **Problema N+1** — diagnosticado com `show-sql: true`, resolvido com `@NamedEntityGraph` + `@EntityGraph` no repository.
4. **Testes de integração** — `@SpringBootTest` + `MockMvc` + H2 in-memory com perfil `test`, cobrindo fluxos end-to-end sem mocks.
5. **Docker Compose** — build multi-stage, healthcheck no postgres, variáveis via `.env`.

---

## 📄 Licença

MIT — sinta-se livre para usar, estudar e modificar.

