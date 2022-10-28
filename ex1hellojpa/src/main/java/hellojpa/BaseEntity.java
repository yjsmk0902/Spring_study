package hellojpa;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.time.LocalDateTime;
import java.util.Date;

@MappedSuperclass   //-> 매핑 정보만 상속
public abstract class BaseEntity {
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastModifiedDate;

    //[@Temporal Option]
    //LocalDate, LocalDateTime을 사용할 때는 생략 가능
    //value =>  TemporalType.Date       : 날짜, DB date 타입과 매핑 (2013-10-11)
    //          TemporalType.TIME       : 시간, DB time 타입과 매핑 (11:11:11)
    //          TemporalType.TIMESTAMP  : 날짜와 시간, DB timestamp 타입과 매핑 (2013-10-11 11:11:11)

    private String createdBy;
    private String lastModifiedBy;

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
}
