package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.ContactInfoRepository;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import com.dynamiccarsharing.carsharing.util.FilterUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContactInfoDao implements ContactInfoRepository {
    private final DatabaseUtil databaseUtil;

    public ContactInfoDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
    }

    @Override
    public ContactInfo save(ContactInfo entity) {
        try {
            if (entity.getId() == null) {
                String insertSql = "INSERT INTO contact_infos (email, phone_number, first_name, last_name) VALUES (?, ?, ?, ?)";
                final Long[] newId = new Long[1];
                databaseUtil.executeWithGeneratedKeys(insertSql, statement -> {
                    try {
                        statement.setString(1, entity.getEmail());
                        statement.setString(2, entity.getPhoneNumber());
                        statement.setString(3, entity.getFirstName());
                        statement.setString(4, entity.getLastName());
                        statement.executeUpdate();
                        ResultSet generatedKeys = statement.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            newId[0] = generatedKeys.getLong(1);
                        } else {
                            throw new SQLException("Failed to retrieve generated ID");
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                return new ContactInfo(newId[0], entity.getFirstName(), entity.getLastName(), entity.getEmail(), entity.getPhoneNumber());
            } else {
                String updateSql = "UPDATE contact_infos SET email = ?, phone_number = ?, first_name = ?, last_name = ? WHERE id = ?";
                databaseUtil.execute(updateSql, entity.getEmail(), entity.getPhoneNumber(), entity.getFirstName(), entity.getLastName(), entity.getId());
                return entity;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save ContactInfo", e);
        }
    }

    @Override
    public Optional<ContactInfo> findById(Long id) {
        String query = "SELECT * FROM contact_infos WHERE id = ?";
        try {
            ContactInfo contactInfo = databaseUtil.findOne(query, rs -> {
                try {
                    return mapToContactInfo(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, id);
            return Optional.ofNullable(contactInfo);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find ContactInfo by ID", e);
        }
    }

    @Override
    public Iterable<ContactInfo> findAll() {
        String query = "SELECT * FROM contact_infos";
        try {
            return databaseUtil.findMany(query, rs -> {
                try {
                    return mapToContactInfo(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all ContactInfos", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM contact_infos WHERE id = ?";
        try {
            databaseUtil.execute(deleteSql, id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete ContactInfo", e);
        }
    }

    @Override
    public List<ContactInfo> findByFilter(Filter<ContactInfo> filter) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT * FROM contact_infos WHERE 1=1");
        List<Object> params = new ArrayList<>();
        try {
            FilterUtil.buildQuery(filter, "contact_infos", query, params, "email", "phoneNumber", "firstName", "lastName");
        } catch (IllegalAccessException e) {
            throw new SQLException("Failed to build filter query", e);
        }
        Object[] processedParams = params.stream().map(param -> param instanceof Enum<?> ? ((Enum<?>) param).name() : param).toArray();

        return databaseUtil.findMany(query.toString(), rs -> {
            try {
                return mapToContactInfo(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, processedParams);
    }

    private ContactInfo mapToContactInfo(ResultSet rs) throws SQLException {
        return new ContactInfo(
                rs.getLong("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone_number")
        );
    }
}