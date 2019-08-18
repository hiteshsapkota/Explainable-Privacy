package com.expriv.model;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String mturkId;
    private String email;
    private String password;
    private String gender;
    private String age;
    private String education;
    private String socialmediaFrequency;
    private String sharingFrequency;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))
    private Collection<Role> roles;

    public User() {
    }

    public User(String mturkId, String email, String password, String gender, String age, String education, String socialmediaFrequency, String sharingFrequency) {
        this.mturkId=mturkId;
        this.email = email;
        this.password = password;
        this.gender=gender;
        this.age = age;
        this.education = education;
        this.socialmediaFrequency = socialmediaFrequency;
        this.sharingFrequency = sharingFrequency;
    }

    public User(String mturkId, String email, String password, String gender, String age, String education, String socialmediaFrequency, String sharingFrequency, Collection<Role> roles) {
        this.mturkId = mturkId;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.gender=gender;
        this.age = age;
        this.education = education;
        this.socialmediaFrequency = socialmediaFrequency;
        this.sharingFrequency = sharingFrequency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

  public String getMturkId() {
    return mturkId;
  }

  public void setMturkId(String mturkId) {
    this.mturkId = mturkId;
  }

  public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

  public Collection<Role> getRoles() {
        return roles;
    }

    public void setRoles(Collection<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", mturkId='" + mturkId + '\'' +
                ", email='" + email + '\'' +
                ", password='" + "*********" + '\'' +
                ", gender='" + gender + '\'' +
                ", age='" + age + '\'' +
                ", education='" + education + '\'' +
                ", socialmediaFrequency='" + socialmediaFrequency + '\'' +
                ", sharingFrequency='" + sharingFrequency + '\'' +
                ", roles=" + roles +
                '}';
    }
}
