package com.exaguard.repository;

import com.exaguard.model.MalpracticeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MalpracticeEventRepository extends JpaRepository<MalpracticeEvent, Long> {
    List<MalpracticeEvent> findBySessionId(Long sessionId);
}
