package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.dao.jdbc.ContactInfoSqlFilterMapper;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.repository.ContactInfoRepository;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Profile("jdbc")
@Repository
public class ContactInfoDao implements ContactInfoRepository {
    private final DatabaseUtil databaseUtil;
    private final SqlFilterMapper<ContactInfo, Filter<ContactInfo>> sqlFilterMapper;

    public ContactInfoDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
        this.sqlFilterMapper = new ContactInfoSqlFilterMapper();
    }

    @Override
    public ContactInfo save(ContactInfo entity) {
        if (entity.getId() == null) {
            String insertSql = "INSERT INTO contact_infos (email, phone_number, first_name, last_name) VALUES (?, ?, ?, ?)";

            Long newId = databaseUtil.executeUpdateWithGeneratedKeys(insertSql, statement -> {
                try {
                    statement.setString(1, entity.getEmail());
                    statement.setString(2, entity.getPhoneNumber());
                    statement.setString(3, entity.getFirstName());
                    statement.setString(4, entity.getLastName());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            return entity.toBuilder().id(newId).build();
        } else {
            String updateSql = "UPDATE contact_infos SET email = ?, phone_number = ?, first_name = ?, last_name = ? WHERE id = ?";
            databaseUtil.execute(updateSql, entity.getEmail(), entity.getPhoneNumber(), entity.getFirstName(), entity.getLastName(), entity.getId());
            return entity;
        }
    }

    @Override
    public Optional<ContactInfo> findById(Long id) {
        String query = "SELECT * FROM contact_infos WHERE id = ?";
        ContactInfo contactInfo = databaseUtil.findOne(query, this::mapToContactInfo, id);
        return Optional.ofNullable(contactInfo);
    }

    @Override
    public List<ContactInfo> findAll() {
        String query = "SELECT * FROM contact_infos";
        return databaseUtil.findMany(query, this::mapToContactInfo);
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM contact_infos WHERE id = ?";
        databaseUtil.execute(deleteSql, id);
    }

    @Override
    public List<ContactInfo> findByFilter(Filter<ContactInfo> filter) throws SQLException {
        String baseQuery = "SELECT * FROM contact_infos WHERE 1=1";
        SqlFilter sqlFilter = sqlFilterMapper.toSqlFilter(filter);

        String fullQuery = baseQuery + sqlFilter.filterQuery();

        return databaseUtil.findMany(fullQuery, this::mapToContactInfo, sqlFilter.parametersArray());
    }

    private ContactInfo mapToContactInfo(ResultSet rs) throws SQLException {
        return ContactInfo.builder()
                .id(rs.getLong("id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .email(rs.getString("email"))
                .phoneNumber(rs.getString("phone_number"))
                .build();
    }

    @Override
    public Optional<ContactInfo> findByEmail(String email) {
        String query = "SELECT * FROM contact_infos WHERE email = ?";
        ContactInfo contactInfo = databaseUtil.findOne(query, this::mapToContactInfo, email);
        return Optional.ofNullable(contactInfo);
    }
}