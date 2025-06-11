package com.dynamiccarsharing.carsharing.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DatabaseUtil {
    private final DataSource dataSource;

    public DatabaseUtil(String url, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        this.dataSource = new HikariDataSource(config);
    }

    public DatabaseUtil(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void execute(String query, Object... args) {
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            setParameters(preparedStatement, args);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Database execute failed for query: " + query, e);
        }
    }

    public void execute(String query, Consumer<PreparedStatement> consumer) {
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            consumer.accept(preparedStatement);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Database execute failed for query: " + query, e);
        }
    }

    public <T> T findOne(String query, SqlFunction<ResultSet, T> mapper, Object... args) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            setParameters(preparedStatement, args);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                T result = mapper.apply(resultSet);
                if (resultSet.next()) {
                    throw new SQLException("More than one result found");
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database findOne failed for query: " + query, e);
        }
        return null;
    }

    public <T> List<T> findMany(String query, SqlFunction<ResultSet, T> mapper, Object... args) {
        List<T> results = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            setParameters(preparedStatement, args);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                results.add(mapper.apply(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database findMany failed for query: " + query, e);
        }
        return results;
    }

    private void setParameters(PreparedStatement preparedStatement, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }

    public void executeWithGeneratedKeys(String query, Consumer<PreparedStatement> consumer) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            consumer.accept(preparedStatement);
        } catch (SQLException e) {
            throw new RuntimeException("Database executeWithGeneratedKeys failed for query: " + query, e);
        }
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }
}