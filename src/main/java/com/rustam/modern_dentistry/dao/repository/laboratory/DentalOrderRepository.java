package com.rustam.modern_dentistry.dao.repository.laboratory;

import com.rustam.modern_dentistry.dao.entity.laboratory.DentalOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DentalOrderRepository extends JpaRepository<DentalOrder, Long>, JpaSpecificationExecutor<DentalOrder> {
    // findById üçün
    @Query("""
                SELECT DISTINCT do FROM DentalOrder do
                LEFT JOIN FETCH do.imagePaths
                LEFT JOIN FETCH do.baseUser
                LEFT JOIN FETCH do.technician
                LEFT JOIN FETCH do.patient
                WHERE do.id = :id
            """)
    Optional<DentalOrder> findByIdWithBasicRelations(@Param("id") Long id);

    @Query("""
                SELECT DISTINCT do FROM DentalOrder do
                LEFT JOIN FETCH do.toothDetails
                WHERE do = :order
            """)
    DentalOrder fetchToothDetails(@Param("order") DentalOrder order);

    @Query("""
                SELECT DISTINCT do FROM DentalOrder do
                LEFT JOIN FETCH do.teethList
                WHERE do = :order
            """)
    DentalOrder fetchTeethList(@Param("order") DentalOrder order);

    // findALl üçün
    // İlk sorğu - imagePaths və əsas əlaqələr
    @Query("""
                SELECT do FROM DentalOrder do
                LEFT JOIN FETCH do.baseUser
                LEFT JOIN FETCH do.technician
                LEFT JOIN FETCH do.patient
            """)
    List<DentalOrder> findAllWithBasicRelations();

    // İlk sorğu - imagePaths və əsas əlaqələr
    @Query("""
                SELECT DISTINCT do FROM DentalOrder do
                LEFT JOIN FETCH do.imagePaths
                LEFT JOIN FETCH do.baseUser
                LEFT JOIN FETCH do.technician
                LEFT JOIN FETCH do.patient
                WHERE do.technician.id = :technicianId
            """)
    List<DentalOrder> findAllWithTechnicianId(@Param("technicianId") UUID technicianId);

    // İkinci sorğu - toothDetails ayrıca yüklənir
    @Query("""
                SELECT DISTINCT do FROM DentalOrder do
                LEFT JOIN FETCH do.toothDetails
                WHERE do IN :orders
            """)
    List<DentalOrder> fetchToothDetails(@Param("orders") List<DentalOrder> orders);

    // Üçüncü sorğu - teethList ayrıca yüklənir
    @Query("""
                SELECT DISTINCT do FROM DentalOrder do
                LEFT JOIN FETCH do.teethList
                WHERE do IN :orders
            """)
    List<DentalOrder> fetchTeethList(@Param("orders") List<DentalOrder> orders);
}

