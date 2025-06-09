package com.dynamiccarsharing.carsharing.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class DatabaseUtil {
    private final DataSource dataSource;

    public DatabaseUtil(String url, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        this.dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void execute(String query, Object... args) throws SQLException {
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            setParameters(preparedStatement, args);
            preparedStatement.execute();
        }
    }

    public void execute(String query, Consumer<PreparedStatement> consumer) throws SQLException{
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            consumer.accept(preparedStatement);
            preparedStatement.execute();
        }
    }

    public <T> T findOne(String query, Function<ResultSet, T> mapper, Object... args) throws SQLException {
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            setParameters(preparedStatement, args);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                T result = mapper.apply(resultSet);
                if(resultSet.next()) {
                    throw new SQLException("More than one result found");
                }
                return result;
            }
        }
        return null;
    }

    public <T> List<T> findMany(String query, Function<ResultSet, T> mapper, Object... args) throws SQLException {
        List<T> results = new ArrayList<>();
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            setParameters(preparedStatement, args);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                results.add(mapper.apply(resultSet));
            }
        }
        return results;
    }

    private void setParameters(PreparedStatement preparedStatement, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }

    public void executeWithGeneratedKeys(String query, Consumer<PreparedStatement> consumer) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            consumer.accept(preparedStatement);
        }
    }
}