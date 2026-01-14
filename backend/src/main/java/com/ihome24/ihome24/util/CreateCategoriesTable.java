package com.ihome24.ihome24.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Утилита для создания таблицы categories вручную
 * Запустить: java CreateCategoriesTable
 */
public class CreateCategoriesTable {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5434/ihome24";
        String user = "ihome24";
        String password = "ihome242026";

        String sql = """
            CREATE TABLE IF NOT EXISTS categories (
                id BIGSERIAL PRIMARY KEY NOT NULL,
                name VARCHAR(255) NOT NULL,
                slug VARCHAR(255) NOT NULL UNIQUE,
                description TEXT,
                parent_id BIGINT,
                image_url VARCHAR(500),
                is_active BOOLEAN DEFAULT true NOT NULL,
                sort_order INTEGER DEFAULT 0 NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
            );
            
            DO $$
            BEGIN
                IF NOT EXISTS (
                    SELECT 1 FROM pg_constraint 
                    WHERE conname = 'fk_categories_parent'
                ) THEN
                    ALTER TABLE categories 
                    ADD CONSTRAINT fk_categories_parent 
                    FOREIGN KEY (parent_id) 
                    REFERENCES categories(id) 
                    ON DELETE SET NULL;
                END IF;
            END $$;
            
            CREATE INDEX IF NOT EXISTS idx_categories_slug ON categories(slug);
            CREATE INDEX IF NOT EXISTS idx_categories_parent_id ON categories(parent_id);
            CREATE INDEX IF NOT EXISTS idx_categories_is_active ON categories(is_active);
            """;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            String[] statements = sql.split(";");
            for (String statement : statements) {
                statement = statement.trim();
                if (!statement.isEmpty()) {
                    stmt.execute(statement);
                    System.out.println("Выполнено: " + statement.substring(0, Math.min(50, statement.length())) + "...");
                }
            }
            System.out.println("Таблица categories успешно создана!");
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
