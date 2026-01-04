package com.stock.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@RequiredArgsConstructor
public class TestConnectionRunner implements CommandLineRunner {

    private final DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {

        try (Connection conn = dataSource.getConnection()) {
            System.out.println("🔥 DB CONNECT SUCCESS");
            System.out.println("DB URL = " + conn.getMetaData().getURL());
            System.out.println("DB USER = " + conn.getMetaData().getUserName());
        }
    }
}
