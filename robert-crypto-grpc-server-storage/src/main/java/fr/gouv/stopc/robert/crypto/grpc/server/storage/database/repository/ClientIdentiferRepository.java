package fr.gouv.stopc.robert.crypto.grpc.server.storage.database.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.gouv.stopc.robert.crypto.grpc.server.storage.database.model.ClientIdentifier;


@Repository
public interface ClientIdentiferRepository extends JpaRepository<ClientIdentifier, Long> {

    Optional<ClientIdentifier> findByIdA(String idA);
}
