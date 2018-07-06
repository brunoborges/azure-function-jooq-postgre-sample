package com.microsoft.azure;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jooq.DSLContext;

import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.using;
import static org.jooq.SQLDialect.POSTGRES;

public class DataStore {

    private String username = System.getenv("DatabaseUsername");
    private String password = System.getenv("DatabasePassword");
    private String host = System.getenv("DatabaseHost");
    private String dbname = System.getenv("DatabaseName");
    private String urlTemplate = "jdbc:postgresql://%s/%s?ssl=true";
    private String url = String.format(urlTemplate, host, dbname);
    private Logger logger;

    public DataStore(Logger logger) {
        this.logger = logger;
    }

    private <R> Optional<R> connectAndRun(Function<DSLContext, Optional<R>> func) {
        logger.info("Connecting to url: "+url);
        try (Connection conn = DriverManager.getConnection(url, username + "@" + host, password)) {
            return func.apply(using(conn, POSTGRES));
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }

        return Optional.empty();
    }

    public void save(String name) {
        connectAndRun(jooq -> Optional.of(jooq.insertInto(table("greeted"), field("name")).values(name).execute()));
    }

    public List<String> greetedPeople() {
        return this
                .<List<String>>connectAndRun(jooq -> Optional.of(
                        jooq.select().from(table("greeted")).fetch().getValues(field("name", String.class)))).get();
    }

}