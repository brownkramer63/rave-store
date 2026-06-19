package com.cydeo.entity.common;

import com.cydeo.security.SparkleUserMapperToSecurity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@Component
public class BaseEntityListener extends AuditingEntityListener {

    @PrePersist
    public void onPrePersist(BaseEntity baseEntity) {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        baseEntity.insertDateTime = LocalDateTime.now();
        baseEntity.lastUpdateDateTime = LocalDateTime.now();

        if (authentication != null && !authentication.getName().equals("anonymousUser")) {
            try {
                SparkleUserMapperToSecurity principal = (SparkleUserMapperToSecurity) authentication.getPrincipal();
                baseEntity.insertUserId = principal.getId();
                baseEntity.lastUpdateUserId = principal.getId();
            } catch (ClassCastException classCastException) {
                // todo these code are expectiong running inly test purpose
                baseEntity.insertUserId = -1L;
                baseEntity.lastUpdateUserId = -1L;
            }

        } else {
            baseEntity.insertUserId = -1L;
            baseEntity.lastUpdateUserId = -1L;
        }
    }

    @PreUpdate
    public void onPreUpdate(BaseEntity baseEntity) {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        baseEntity.lastUpdateDateTime = LocalDateTime.now();

        if (authentication != null && !authentication.getName().equals("anonymousUser")) {
            SparkleUserMapperToSecurity principal = (SparkleUserMapperToSecurity) authentication.getPrincipal();
            baseEntity.lastUpdateUserId = principal.getId();
        }
    }
}
