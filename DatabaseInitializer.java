package com.elitemart.util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {

        try {
            Files.createDirectories(Paths.get("data"));

            String schemaSql = Files.readString(
                    Paths.get("src/main/resources/schema.sql")
            );

            String dataSql = Files.readString(
                    Paths.get("src/main/resources/data.sql")
            );

            try (Connection connection = DBConnection.getConnection();
                 Statement statement = connection.createStatement()) {

                // Create tables
                for (String query : schemaSql.split(";")) {
                    if (!query.trim().isEmpty()) {
                        statement.execute(query);
                    }
                }

                // Insert sample data
                for (String query : dataSql.split(";")) {
                    if (!query.trim().isEmpty()) {
                        statement.execute(query);
                    }
                }

                System.out.println("Database tables and sample data created successfully!");
            }

        } catch (Exception e) {
            System.out.println("Database initialization failed!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        initialize();
    }
}