package mocacong.server.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseTime {

    @CreatedDate
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @LastModifiedDate
    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;

    @PrePersist
    public void before() {
        LocalDateTime now = LocalDateTime.now();
        this.createdTime = now;
        this.modifiedTime = now;
    }

    @PreUpdate
    public void always() {
        this.modifiedTime = LocalDateTime.now();
    }
}
