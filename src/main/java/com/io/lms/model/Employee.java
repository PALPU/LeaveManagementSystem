package com.io.lms.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    @NotBlank
    private String name;
    @Column
    private String email;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Column
    private Date joiningDate;

    @OneToMany(mappedBy = "employee")
    @JsonManagedReference
    private List<Leave> leaves;

    @OneToMany(mappedBy = "employee")
    @JsonManagedReference
    private List<ExtraWork> extraWorks;


}
