package com.dynamiccarsharing.user.service;

import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.criteria.UserSearchCriteria;
import com.dynamiccarsharing.user.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.user.dto.UserCreateRequestDto;
import com.dynamiccarsharing.user.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.user.exception.UserNotFoundException;
import com.dynamiccarsharing.user.mapper.ContactInfoMapper;
import com.dynamiccarsharing.user.mapper.UserMapper;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.user.referral.ReferralCodeAllocator;
import com.dynamiccarsharing.user.repository.UserRepository;
import com.dynamiccarsharing.util.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ContactInfoMapper contactInfoMapper;

    @Mock
    private ReferralCodeAllocator referralCodeAllocator;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser = User.builder()
            .id(1L)
            .contactInfo(ContactInfo.builder().email("test@example.com").build())
            .role(UserRole.RENTER)
            .status(UserStatus.ACTIVE)
            .build();
    private UserDto mockUserDto = new UserDto();
    
    @Test
    void testRegisterUser() {
        UserCreateRequestDto createDto = new UserCreateRequestDto();
        when(userMapper.toEntity(createDto)).thenReturn(mockUser);
        when(referralCodeAllocator.allocate()).thenReturn("ADMINREF001");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userMapper.toDto(mockUser)).thenReturn(mockUserDto);

        UserDto result = userService.registerUser(createDto);

        assertNotNull(result);
        verify(userMapper).toEntity(createDto);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDto(mockUser);
    }

    @Test
    void testFindUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userMapper.toDto(mockUser)).thenReturn(mockUserDto);

        Optional<UserDto> result = userService.findUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(mockUserDto, result.get());
    }

    @Test
    void testFindUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<UserDto> result = userService.findUserById(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(mockUser));
        when(userMapper.toDto(mockUser)).thenReturn(mockUserDto);

        List<UserDto> results = userService.findAllUsers();

        assertEquals(1, results.size());
        assertEquals(mockUserDto, results.get(0));
    }

    @Test
    void testDeleteById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteById(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void testDeleteById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteById(1L));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void testUpdateUserContactInfo_Found() {
        ContactInfoUpdateRequestDto updateDto = new ContactInfoUpdateRequestDto();
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(mockUser)).thenReturn(mockUser);
        when(userMapper.toDto(mockUser)).thenReturn(mockUserDto);

        UserDto result = userService.updateUserContactInfo(1L, updateDto);

        assertNotNull(result);
        verify(contactInfoMapper).updateFromDto(eq(updateDto), any(ContactInfo.class));
        verify(userRepository).save(mockUser);
    }

    @Test
    void testUpdateUserContactInfo_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.updateUserContactInfo(1L, new ContactInfoUpdateRequestDto()));
    }

    @Test
    void testUpdateUserStatus_Found() {
        UserStatusUpdateRequestDto updateDto = new UserStatusUpdateRequestDto();
        updateDto.setStatus(UserStatus.SUSPENDED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userMapper.toDto(mockUser)).thenReturn(mockUserDto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        userService.updateUserStatus(1L, updateDto);

        verify(userRepository).save(userCaptor.capture());
        assertEquals(UserStatus.SUSPENDED, userCaptor.getValue().getStatus());
    }

    @Test
    void testUpdateUserStatus_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.updateUserStatus(1L, new UserStatusUpdateRequestDto()));
    }

    @Test
    void testFindByEmail_Found() {
        when(userRepository.findByContactInfoEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(userMapper.toDto(mockUser)).thenReturn(mockUserDto);

        Optional<UserDto> result = userService.findByEmail("test@example.com");

        assertTrue(result.isPresent());
    }

    @Test
    void testFindByEmail_NotFound() {
        when(userRepository.findByContactInfoEmail("test@example.com")).thenReturn(Optional.empty());
        Optional<UserDto> result = userService.findByEmail("test@example.com");
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchUsers_Success() throws SQLException {
        UserSearchCriteria criteria = UserSearchCriteria.builder().build();
        when(userRepository.findByFilter(any())).thenReturn(List.of(mockUser));

        List<User> results = userService.searchUsers(criteria);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    void testSearchUsers_SQLException() throws SQLException {
        UserSearchCriteria criteria = UserSearchCriteria.builder().build();
        when(userRepository.findByFilter(any())).thenThrow(new SQLException("DB error"));

        assertThrows(ServiceException.class, () -> userService.searchUsers(criteria));
    }

    @Test
    void testLoadUserByUsername_ByEmailFound() {
        when(userRepository.findByContactInfoEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        UserDetails result = userService.loadUserByUsername("test@example.com");
        assertEquals(mockUser, result);
    }

    @Test
    void testLoadUserByUsername_ByIdFound() {
        when(userRepository.findByContactInfoEmail("1")).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        UserDetails result = userService.loadUserByUsername("1");

        assertEquals(mockUser, result);
    }
    
    @Test
    void testLoadUserByUsername_ByEmailWithNumberFormatException() {
        String username = "not_an_id@example.com";
        when(userRepository.findByContactInfoEmail(username)).thenReturn(Optional.empty());
        
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(username));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByContactInfoEmail("1")).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("1"));
    }
}