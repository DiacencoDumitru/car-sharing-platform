package com.dynamiccarsharing.user.controller;

import com.dynamiccarsharing.contracts.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.ContactInfoDto;
import com.dynamiccarsharing.contracts.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.user.service.interfaces.ContactInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactInfoController.class)
class ContactInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContactInfoService contactInfoService;

    @Test
    @WithMockUser
    void createContactInfo_withValidData_shouldReturnCreated() throws Exception {
        ContactInfoCreateRequestDto createDto = new ContactInfoCreateRequestDto();
        createDto.setFirstName("Dumitru");
        createDto.setLastName("Diacenco");
        createDto.setPhoneNumber("+3736777388");
        createDto.setEmail("dd.prodev@gmail.com");

        ContactInfoDto savedDto = new ContactInfoDto();
        savedDto.setId(1L);
        savedDto.setEmail("dd.prodev@gmail.com");

        when(contactInfoService.createContactInfo(any(ContactInfoCreateRequestDto.class))).thenReturn(savedDto);

        mockMvc.perform(post("/api/v1/contact-infos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("dd.prodev@gmail.com"));
    }

    @Test
    @WithMockUser
    void getContactInfoById_whenExists_shouldReturnOk() throws Exception {
        ContactInfoDto dto = new ContactInfoDto();
        dto.setId(1L);
        when(contactInfoService.findContactInfoById(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/v1/contact-infos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser
    void getAllContactInfos_shouldReturnList() throws Exception {
        when(contactInfoService.findAllContactInfos()).thenReturn(List.of(new ContactInfoDto(), new ContactInfoDto()));

        mockMvc.perform(get("/api/v1/contact-infos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void updateContactInfo_whenExists_shouldReturnOk() throws Exception {
        ContactInfoUpdateRequestDto updateDto = new ContactInfoUpdateRequestDto();
        updateDto.setFirstName("Dumitru");
        updateDto.setLastName("Diacenco");
        updateDto.setPhoneNumber("+3736777388");
        updateDto.setEmail("new.email@gmail.com");

        ContactInfoDto updatedDto = new ContactInfoDto();
        updatedDto.setId(1L);
        updatedDto.setEmail("new.email@gmail.com");

        when(contactInfoService.updateContactInfo(eq(1L), any(ContactInfoUpdateRequestDto.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/api/v1/contact-infos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new.email@gmail.com"));
    }
}