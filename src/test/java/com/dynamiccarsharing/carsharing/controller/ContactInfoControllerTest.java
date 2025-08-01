package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.TestApplication;
import com.dynamiccarsharing.carsharing.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.ContactInfoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class ContactInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContactInfoRepository contactInfoRepository;

    @Test
    @WithMockUser
    void createContactInfo_withValidData_shouldReturnCreated() throws Exception {
        ContactInfoCreateRequestDto createDto = new ContactInfoCreateRequestDto();
        createDto.setFirstName("John");
        createDto.setLastName("Doe");
        createDto.setEmail("john.doe@example.com");
        createDto.setPhoneNumber("123-456-7890");

        ContactInfo savedEntity = ContactInfo.builder()
                .id(1L).firstName("John").lastName("Doe")
                .email("john.doe@example.com").phoneNumber("123-456-7890")
                .build();

        when(contactInfoRepository.save(any(ContactInfo.class))).thenReturn(savedEntity);

        mockMvc.perform(post("/api/v1/contact-infos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser
    void getContactInfoById_whenExists_shouldReturnOk() throws Exception {
        ContactInfo entity = ContactInfo.builder().id(1L).email("jane.doe@example.com").build();
        when(contactInfoRepository.findById(1L)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/api/v1/contact-infos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"));
    }

    @Test
    @WithMockUser
    void getContactInfoById_whenNotExists_shouldReturnNotFound() throws Exception {
        when(contactInfoRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/contact-infos/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getAllContactInfos_shouldReturnList() throws Exception {
        ContactInfo entity1 = ContactInfo.builder().id(1L).build();
        ContactInfo entity2 = ContactInfo.builder().id(2L).build();
        when(contactInfoRepository.findAll()).thenReturn(List.of(entity1, entity2));

        mockMvc.perform(get("/api/v1/contact-infos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void updateContactInfo_whenExists_shouldReturnOk() throws Exception {
        ContactInfo existingEntity = ContactInfo.builder().id(1L).email("old@example.com").build();

        ContactInfoUpdateRequestDto updateDto = new ContactInfoUpdateRequestDto();
        updateDto.setFirstName("Jane");
        updateDto.setLastName("Doe");
        updateDto.setEmail("new@example.com");
        updateDto.setPhoneNumber("987-654-3210");

        ContactInfo updatedEntity = ContactInfo.builder()
                .id(1L).firstName("Jane").lastName("Doe")
                .email("new@example.com").phoneNumber("987-654-3210")
                .build();

        when(contactInfoRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(contactInfoRepository.save(any(ContactInfo.class))).thenReturn(updatedEntity);

        mockMvc.perform(put("/api/v1/contact-infos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    @WithMockUser
    void deleteContactInfo_whenExists_shouldReturnNoContent() throws Exception {
        when(contactInfoRepository.findById(1L)).thenReturn(Optional.of(ContactInfo.builder().id(1L).build()));
        doNothing().when(contactInfoRepository).deleteById(1L);

        mockMvc.perform(delete("/api/v1/contact-infos/{id}", 1L).with(csrf()))
                .andExpect(status().isNoContent());

        verify(contactInfoRepository, times(1)).deleteById(1L);
    }
}