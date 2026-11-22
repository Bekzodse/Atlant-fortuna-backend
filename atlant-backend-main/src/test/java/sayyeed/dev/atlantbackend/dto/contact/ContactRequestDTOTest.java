package sayyeed.dev.atlantbackend.dto.contact;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContactRequestDTOTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsBlankName() {
        ContactRequestDTO dto = new ContactRequestDTO();
        dto.setName("");
        dto.setPhone("+998901234567");
        dto.setMessage("Hello, I have a question");

        Set<ConstraintViolation<ContactRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void rejectsInvalidPhone() {
        ContactRequestDTO dto = new ContactRequestDTO();
        dto.setName("Aziz");
        dto.setPhone("not-a-phone-number!!");
        dto.setMessage("Hello, I have a question");

        Set<ConstraintViolation<ContactRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void acceptsValidPayload() {
        ContactRequestDTO dto = new ContactRequestDTO();
        dto.setName("Aziz");
        dto.setPhone("+998901234567");
        dto.setMessage("Hello, I have a question");

        Set<ConstraintViolation<ContactRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }
}
