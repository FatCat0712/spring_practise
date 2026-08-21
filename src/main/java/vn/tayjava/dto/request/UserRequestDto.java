package vn.tayjava.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;
import vn.tayjava.util.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;


@Getter
public class UserRequestDto implements Serializable {
    @NotBlank(message = "firstName must not be blank")
    private String firstName;

    @NotNull(message = "lastName must not be null")
    private String lastName;

    @Email(message = "email must be a valid email address")
    private String email;

//    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "phone must be a valid phone number")
    @PhoneNumber(message = "phone invalid format")
    private String phone;

    @NotNull(message = "dateOfBirth must not be null")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "MM/dd/yyyy")
    private Date dateOfBirth;

    @GenderSubset(anyOf = {Gender.MALE, Gender.FEMALE, Gender.OTHER})
    private Gender gender;

    @NotNull(message = "username must not be null")
    private String username;

    @NotNull(message = "password must be not null")
    private String password;

    @NotNull(message = "type must not be null")
    @EnumValue(name = "type", enumClass = UserType.class)
    private String type;

    //    @Pattern(regexp = "^ACTIVE|INACTIVE|NONE$", message = "status must be one of the following values: ACTIVE, INACTIVE, NONE")
    @EnumPattern(name = "status", regexp = "^ACTIVE|INACTIVE|NONE$")
    private UserStatus status;

    @NotEmpty(message = "addresses can not be empty")
    private Set<AddressDTO> addresses;

    public UserRequestDto(String firstName, String lastName, String phone, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
    }

}
