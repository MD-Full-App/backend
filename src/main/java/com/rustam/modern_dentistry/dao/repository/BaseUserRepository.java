package com.rustam.modern_dentistry.dao.repository;

import com.rustam.modern_dentistry.dao.entity.users.BaseUser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface BaseUserRepository extends JpaRepository<BaseUser, UUID>, JpaSpecificationExecutor<BaseUser> {
    Optional<BaseUser> findByUsername(String username);

    boolean existsBaseUserByEmail(String mail);

//    @Query(value = """
//    SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
//    FROM base_users u
//    LEFT JOIN base_users d ON d.base_user_id = u.id
//    LEFT JOIN authorities a ON u.id = a.base_user_id
//    LEFT JOIN permission p ON a.permission_id = p.id
//    WHERE (u.username = :username OR u.email = :email OR u.fin_code = :finCode)
//      AND (
//          p.permission_name <> 'DOCTOR'
//          OR (:colorCode IS NULL OR d.color_code = :colorCode)
//      )
//    """, nativeQuery = true)
//    boolean existsUserFully(String username, String email, String finCode, String colorCode);



    @Query("""
                SELECT DISTINCT u FROM BaseUser u
                LEFT JOIN FETCH u.permissions p
                LEFT JOIN FETCH p.modulePermissions mp
                LEFT JOIN FETCH mp.actions a
                WHERE u.id = :username
            """)
    Optional<BaseUser> findUserWithAllPermissions(UUID username);

    @Query("SELECT u FROM BaseUser u LEFT JOIN FETCH u.permissions WHERE u.id = :id")
    Optional<BaseUser> findByIdWithPermissions(UUID id);

    @Query("""
    SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END
    FROM BaseUser u
    WHERE u.username = :username OR u.email = :email OR u.finCode = :finCode
    """)
    boolean existsByUsernameOrEmailOrFinCode(String username, String email, String finCode);

    boolean existsByFinCode(String finCode);

    boolean existsByFinCodeIgnoreCase(String finCode);

    boolean existsByFinCodeAndIdNot(String finCode, UUID id);

    boolean existsByFinCodeIgnoreCaseAndIdNot(String finCode, UUID id);

    boolean existsByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameAndIdNot(String username, UUID id);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, UUID id);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    @EntityGraph(attributePaths = "permissions")
    Optional<BaseUser> findByUsernameOrEmailOrFinCode(String username, String email, String finCode);

    List<BaseUser> findAllByPermissionsPermissionNameIn(Set<String> visiblePermissions);

    @Query("""
    SELECT DISTINCT b FROM BaseUser b 
    LEFT JOIN b.permissions p 
    WHERE p.permissionName = 'DOCTOR'
""")
    List<BaseUser> findAllWithPermissionDoctor();

//    @Query("""
//                SELECT u FROM BaseUser u
//                LEFT JOIN FETCH u.permissions p
//                WHERE :permission IS NULL OR :permission = p.permissionName
//            """)
//    List<BaseUser> findAllByPermissionWithJoin(String permission);
}
