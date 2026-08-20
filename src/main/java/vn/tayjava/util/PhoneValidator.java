package vn.tayjava.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<PhoneNumber, String> {

    @Override
    public void initialize(PhoneNumber phoneNumberNo) {

    }

    @Override
    public boolean isValid(String phoneNo, ConstraintValidatorContext context) {
        if (phoneNo == null) return false;

        // Validate phone number format
        if (phoneNo.matches("\\d{10}")) {
            return true;
        } else if (phoneNo.matches("\\d{3}-\\d{3}-\\d{4}")) {
            return true;
        } else if (phoneNo.matches("\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}")) {
            return true;
        } else return phoneNo.matches("\\(\\d{3}\\)-\\d{3}-\\d{4}");
    }

}
