package com.expriv.web.dto;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import com.expriv.constraint.FieldMatch;

@FieldMatch.List({
        @FieldMatch(first = "password", second = "confirmPassword", message = "The password fields must match"),
        @FieldMatch(first = "email", second = "confirmEmail", message = "The email fields must match")
})
public class UserRegistrationDto {

    @NotEmpty
    private String mturkId;

    @NotEmpty
    private String gender;

    @NotEmpty
    private String age;

    @NotEmpty
    private String education;

    @NotEmpty
    private String socialmediaFrequency;

    @NotEmpty
    private String sharingFrequency;

    @NotEmpty
    private String password;

    @NotEmpty
    private String confirmPassword;

    @Email
    @NotEmpty
    private String email;

    @Email
    @NotEmpty
    private String confirmEmail;

    @AssertTrue
    private Boolean terms;


    public String getMturkId() {
        return mturkId;
    }

    public void setMturkId(String mturkId) {
        this.mturkId = mturkId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getConfirmEmail() {
        return confirmEmail;
    }

    public void setConfirmEmail(String confirmEmail) {
        this.confirmEmail = confirmEmail;
    }

    public Boolean getTerms() {
        return terms;
    }

    public void setTerms(Boolean terms) {
        this.terms = terms;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getSocialmediaFrequency() {
        return socialmediaFrequency;
    }

    public void setSocialmediaFrequency(String socialmediaFrequency) {
        this.socialmediaFrequency = socialmediaFrequency;
    }

    public String getSharingFrequency() {
        return sharingFrequency;
    }

    public void setSharingFrequency(String sharingFrequency) {
        this.sharingFrequency = sharingFrequency;
    }
}
