package com.dynamiccarsharing.contracts.dto;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private UserRole role;
    private UserStatus status;
    private ContactInfoDto contactInfo;
    private String instanceId;
    /** Public shareable code for this user; assigned at registration. */
    private String referralCode;
    /** User id of the referrer, if this account was created with a valid referral code. */
    private Long referredByUserId;
}