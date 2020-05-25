package fr.gouv.stopc.robert.crypto.grpc.server.storage.database.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "IDENTITY")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ClientIdentifier {

    @Id
    @ToString.Exclude
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @Column(name = "idA", unique = true, nullable = false)
    private String idA;

    @ToString.Exclude
    @Column(name = "key", updatable = false, nullable = false)
    private String key;

    @ToString.Exclude
    @Column(name = "key_for_tuples", updatable = false, nullable = false)
    private String keyForTuples;

    @CreatedDate
    @Column(name = "date_creation", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    protected Date dateCreation;

    @LastModifiedDate
    @Column(name = "date_maj")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date dateMaj;
}
