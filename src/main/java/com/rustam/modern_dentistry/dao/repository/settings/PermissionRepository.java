package com.rustam.modern_dentistry.dao.repository.settings;

import com.rustam.modern_dentistry.dao.entity.settings.permission.Permission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission,Long>, JpaSpecificationExecutor<Permission> {
    Optional<Permission> findByPermissionName(String permissionName);

    @Query("""
    SELECT DISTINCT p FROM Permission p
    LEFT JOIN FETCH p.modulePermissions mp
    LEFT JOIN FETCH mp.actions
    WHERE p.id = :id
""")
    Optional<Permission> findWithModulePermissions(Long id);

    @Query("""
    SELECT DISTINCT p FROM Permission p
    LEFT JOIN FETCH p.modulePermissions mp
    LEFT JOIN FETCH mp.actions
""")
    List<Permission> findAllWithModulePermissions();
}
