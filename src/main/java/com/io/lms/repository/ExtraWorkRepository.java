package com.io.lms.repository;

import com.io.lms.model.ExtraWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExtraWorkRepository extends JpaRepository<ExtraWork,Long> {
    List<ExtraWork> findAllByEmployeeId(Long empId);
}
