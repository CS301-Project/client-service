package com.bank.crm.clientservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bank.crm.clientservice.models.ClientProfile;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClientProfileRepository extends JpaRepository<ClientProfile, UUID> {
    boolean existsByEmailAddressAndClientIdNot(String email, UUID clientId);
    boolean existsByPhoneNumberAndClientIdNot(String phone, UUID clientId);
} 
