package vn.tayjava.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import vn.tayjava.util.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class UserRequestDto implements Serializable {
    @NotBlank(message = "firstName must not be blank")
    private String firstName;

    @NotNull(message = "lastName must not be null")
    private String lastName;

//    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "phone must be a valid phone number")
    @PhoneNumber
    private String phone;

    @Email(message = "email must be a valid email address")
    private String email;

    @NotNull(message = "dateOfBirth must not be null")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "MM/dd/yyyy")
    private Date dateOfBirth;

    @NotEmpty(message = "addresses can not be empty")
    private Set<Address> addresses;

//    @Pattern(regexp = "^ACTIVE|INACTIVE|NONE$", message = "status must be one of the following values: ACTIVE, INACTIVE, NONE")
    @EnumPattern(name = "status", regexp = "^ACTIVE|INACTIVE|NONE$")
    private UserStatus status;

    @GenderSubset(anyOf = {Gender.MALE, Gender.FEMALE, Gender.OTHER})
    private Gender gender;

    @NotNull(message = "type must not be null")
    @EnumValue(name = "type", enumClass = UserType.class)
    private String type;

    public UserRequestDto(String firstName, String lastName, String phone, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
    }

    public static class Address {
        private String apartmentNumber;
        private String floor;
        private String building;
        private String streetNumber;
        private String street;
        private String city;
        private String country;
        private Integer addressType;

        public String getApartmentNumber() {
            return apartmentNumber;
        }

        public String getFloor() {
            return floor;
        }

        public String getBuilding() {
            return building;
        }

        public String getStreetNumber() {
            return streetNumber;
        }

        public String getStreet() {
            return street;
        }

        public String getCity() {
            return city;
        }

        public String getCountry() {
            return country;
        }

        public Integer getAddressType() {
            return addressType;
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public Set<Address> getAddresses() {
        return addresses;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Gender getGender() {
        return gender;
    }

    public String getType() {
        return type;
    }
}
