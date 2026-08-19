package com.infosys.smartwater.repository;

import com.infosys.smartwater.entity.TariffTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TariffTierRepository extends JpaRepository<TariffTier, UUID> {
    List<TariffTier> findByApartmentIdOrderByMinKlAsc(UUID apartmentId);

    @Modifying
    @Query("DELETE FROM TariffTier t WHERE t.apartment.id = :apartmentId")
    void deleteByApartmentId(@Param("apartmentId") UUID apartmentId);
}
