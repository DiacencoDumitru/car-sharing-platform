package com.dynamiccarsharing.carsharing.specification;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import org.springframework.data.jpa.domain.Specification;

public class ContactInfoSpecification {

    private ContactInfoSpecification() {
    }

    public static Specification<ContactInfo> firstNameContains(String firstName) {
        return (root, query, cb) -> firstName != null ? cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%") : null;
    }

    public static Specification<ContactInfo> lastNameContains(String lastName) {
        return (root, query, cb) -> lastName != null ? cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%") : null;
    }

    public static Specification<ContactInfo> hasEmail(String email) {
        return (root, query, cb) -> email != null ? cb.equal(root.get("email"), email) : null;
    }

    public static Specification<ContactInfo> withCriteria(String firstName, String lastName, String email) {
        return Specification
                .where(firstNameContains(firstName))
                .and(lastNameContains(lastName))
                .and(hasEmail(email));
    }
}