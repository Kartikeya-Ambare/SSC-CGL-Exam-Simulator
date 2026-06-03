package com.ssccgl.exam.repository;

import com.ssccgl.exam.entity.LoginAudit;
import com.ssccgl.exam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {
    List<LoginAudit> findByUserOrderByLoginTimeDesc(User user);
    List<LoginAudit> findTop10ByUserOrderByLoginTimeDesc(User user);
    List<LoginAudit> findTop10ByUser(User user);
}
