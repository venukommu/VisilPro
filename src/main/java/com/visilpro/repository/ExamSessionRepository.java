package com.visilpro.repository;

import com.visilpro.model.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {
    ExamSession findByStudentIdAndStatus(String studentId, String status);
}
