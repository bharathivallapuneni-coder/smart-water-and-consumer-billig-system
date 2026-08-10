package com.infosys.smartwater.repository;

import com.infosys.smartwater.entity.TariffTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TariffTierRepository extends JpaRepository<TariffTier, UUID> {
    List<TariffTier> findByApartmentIdOrderByMinKlAsc(UUID apartmentId);
    void deleteByApartmentId(UUID apartmentId);
}
