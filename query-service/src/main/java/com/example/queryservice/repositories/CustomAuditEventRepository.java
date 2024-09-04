package com.example.queryservice.repositories;

import org.activiti.cloud.services.audit.jpa.events.AuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomAuditEventRepository extends JpaRepository<AuditEventEntity, Long> {

    List<AuditEventEntity> findAllByProcessInstanceId(String processInstanceId);
}
