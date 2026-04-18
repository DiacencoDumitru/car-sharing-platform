package com.dynamiccarsharing.user.mapper;

import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.user.dto.UserCreateRequestDto;
import com.dynamiccarsharing.user.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = {UserMapperImpl.class, ContactInfoMapperImpl.class})
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testToDto() {
        ContactInfo contactInfo = ContactInfo.builder().email("test@example.com").build();
        User user = User.builder()
                .id(1L)
                .role(UserRole.RENTER)
                .status(UserStatus.ACTIVE)
                .contactInfo(contactInfo)
                .build();

        UserDto dto = userMapper.toDto(user);

        assertEquals(1L, dto.getId());
        assertEquals(UserRole.RENTER, dto.getRole());
        assertEquals(UserStatus.ACTIVE, dto.getStatus());
        assertEquals("test@example.com", dto.getContactInfo().getEmail());
        assertNull(dto.getInstanceId());
    }
    
    @Test
    void testToDto_Null() {
        assertNull(userMapper.toDto(null));
    }

    @Test
    void testToEntity() {
        ContactInfoCreateRequestDto contactDto = new ContactInfoCreateRequestDto();
        contactDto.setEmail("test@example.com");

        UserCreateRequestDto createDto = new UserCreateRequestDto();
        createDto.setRole(UserRole.ADMIN);
        createDto.setContactInfo(contactDto);

        User user = userMapper.toEntity(createDto);

        assertNull(user.getId());
        assertEquals(UserRole.ADMIN, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals("test@example.com", user.getContactInfo().getEmail());
    }

    @Test
    void testToEntity_Null() {
        assertNull(userMapper.toEntity(null));
    }

    @Test
    void testUpdateFromDto() {
        User user = User.builder()
                .id(1L)
                .role(UserRole.RENTER)
                .status(UserStatus.ACTIVE)
                .build();

        UserStatusUpdateRequestDto updateDto = new UserStatusUpdateRequestDto();
        updateDto.setStatus(UserStatus.SUSPENDED);

        userMapper.updateFromDto(updateDto, user);

        assertEquals(1L, user.getId());
        assertEquals(UserRole.RENTER, user.getRole());
        assertEquals(UserStatus.SUSPENDED, user.getStatus());
    }
    
    @Test
    void testUpdateFromDto_NullDto() {
        User user = User.builder().id(1L).status(UserStatus.ACTIVE).build();
        
        userMapper.updateFromDto(null, user);
        
        assertEquals(1L, user.getId());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void testFromId() {
        User user = userMapper.fromId(10L);
        assertEquals(10L, user.getId());
        assertNull(user.getRole());
    }

    @Test
    void testFromId_Null() {
        assertNull(userMapper.fromId(null));
    }
}