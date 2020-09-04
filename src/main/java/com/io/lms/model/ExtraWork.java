package com.io.lms.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class ExtraWork {
    @NotNull
    Date date;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long extraWorkId;
    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonBackReference
    private Employee employee;

}
