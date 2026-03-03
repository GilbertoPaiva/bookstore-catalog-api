-- ──────────────────────────────────────────────────────────────────────────────
-- Seed data — Bookstore Catalog API
-- Executado automaticamente pelo Spring Boot na inicialização (spring.sql.init)
-- ──────────────────────────────────────────────────────────────────────────────

-- Categories
INSERT INTO categories (name, description) VALUES
    ('Technology',  'Books about software engineering, programming and computing')
  , ('Science',     'Books about physics, biology, chemistry and natural sciences')
  , ('Business',    'Books about entrepreneurship, management and economics')
  , ('Self-Help',   'Books about personal development, habits and productivity')
  , ('Fiction',     'Novels, short stories and literary fiction')
ON CONFLICT DO NOTHING;

-- Books (category IDs match the insert order above: Technology=1, Science=2, Business=3, Self-Help=4, Fiction=5)
INSERT INTO books (title, author, isbn, price, stock_quantity, description, published_year, category_id, created_at, updated_at) VALUES
    ('Clean Code',
     'Robert C. Martin',
     '9780132350884',
     59.90, 15,
     'A handbook of agile software craftsmanship. Covers naming, functions, comments, formatting, error handling and more.',
     2008,
     (SELECT id FROM categories WHERE name = 'Technology'),
     NOW(), NOW())

  , ('The Pragmatic Programmer',
     'David Thomas, Andrew Hunt',
     '9780135957059',
     69.90, 12,
     'Your journey to mastery — from journeyman to master. Covers career, philosophy and practical techniques.',
     2019,
     (SELECT id FROM categories WHERE name = 'Technology'),
     NOW(), NOW())

  , ('Design Patterns',
     'Gang of Four',
     '9780201633610',
     74.90, 8,
     'Elements of reusable object-oriented software. The classic patterns book.',
     1994,
     (SELECT id FROM categories WHERE name = 'Technology'),
     NOW(), NOW())

  , ('Refactoring',
     'Martin Fowler',
     '9780134757599',
     64.90, 10,
     'Improving the design of existing code. Second edition with examples in JavaScript.',
     2018,
     (SELECT id FROM categories WHERE name = 'Technology'),
     NOW(), NOW())

  , ('A Brief History of Time',
     'Stephen Hawking',
     '9780553380163',
     39.90, 20,
     'From the Big Bang to black holes — Hawking''s landmark exploration of the universe.',
     1988,
     (SELECT id FROM categories WHERE name = 'Science'),
     NOW(), NOW())

  , ('Sapiens: A Brief History of Humankind',
     'Yuval Noah Harari',
     '9780062316097',
     49.90, 25,
     'A sweeping narrative of humanity''s creation and evolution from Stone Age foragers to the masters of the digital age.',
     2011,
     (SELECT id FROM categories WHERE name = 'Science'),
     NOW(), NOW())

  , ('Zero to One',
     'Peter Thiel',
     '9780804139021',
     44.90, 18,
     'Notes on startups, or how to build the future. What important truth do very few people agree with you on?',
     2014,
     (SELECT id FROM categories WHERE name = 'Business'),
     NOW(), NOW())

  , ('The Lean Startup',
     'Eric Ries',
     '9780307887894',
     42.90, 22,
     'How today''s entrepreneurs use continuous innovation to create radically successful businesses.',
     2011,
     (SELECT id FROM categories WHERE name = 'Business'),
     NOW(), NOW())

  , ('Atomic Habits',
     'James Clear',
     '9780735211292',
     49.90, 30,
     'An easy and proven way to build good habits and break bad ones.',
     2018,
     (SELECT id FROM categories WHERE name = 'Self-Help'),
     NOW(), NOW())

  , ('Deep Work',
     'Cal Newport',
     '9781455586691',
     44.90, 14,
     'Rules for focused success in a distracted world.',
     2016,
     (SELECT id FROM categories WHERE name = 'Self-Help'),
     NOW(), NOW())

  , ('1984',
     'George Orwell',
     '9780451524935',
     29.90, 35,
     'A dystopian social science fiction novel and cautionary tale about the dangers of totalitarianism.',
     1949,
     (SELECT id FROM categories WHERE name = 'Fiction'),
     NOW(), NOW())

  , ('The Hitchhiker''s Guide to the Galaxy',
     'Douglas Adams',
     '9780345391803',
     34.90, 20,
     'A comedy science fiction franchise. Don''t panic — and always know where your towel is.',
     1979,
     (SELECT id FROM categories WHERE name = 'Fiction'),
     NOW(), NOW())

ON CONFLICT DO NOTHING;

