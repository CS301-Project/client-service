package com.bank.crm.client_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bank.crm.client_service.models.ClientProfile;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClientProfileRepository extends JpaRepository<ClientProfile, UUID> {
    List<ClientProfile> findByClientId(UUID clientId);

} 
