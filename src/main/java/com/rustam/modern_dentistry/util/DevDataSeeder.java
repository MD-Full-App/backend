package com.rustam.modern_dentistry.util;

import com.rustam.modern_dentistry.dao.entity.enums.status.Status;
import com.rustam.modern_dentistry.dao.entity.settings.operations.OpType;
import com.rustam.modern_dentistry.dao.entity.settings.operations.OpTypeItem;
import com.rustam.modern_dentistry.dao.entity.settings.permission.Permission;
import com.rustam.modern_dentistry.dao.repository.settings.PermissionRepository;
import com.rustam.modern_dentistry.dao.repository.settings.operations.OperationTypeItemRepository;
import com.rustam.modern_dentistry.dao.repository.settings.operations.OperationTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final OperationTypeRepository operationTypeRepository;
    private final OperationTypeItemRepository operationTypeItemRepository;

    @Override
    public void run(String... args) {
        seedPermissions();
        seedOperationTypesAndItems();
    }

    private void seedPermissions() {
        permissionRepository.findByPermissionName("DOCTOR")
                .orElseGet(() -> permissionRepository.save(
                        Permission.builder().permissionName("DOCTOR").status(Status.ACTIVE).build()));

        permissionRepository.findByPermissionName("Hesabat:READ")
                .orElseGet(() -> permissionRepository.save(
                        Permission.builder().permissionName("Hesabat:READ").status(Status.ACTIVE).build()));
    }

    private void seedOperationTypesAndItems() {
        if (operationTypeRepository.count() == 0) {
            log.info("Seeding Operation Types and Items...");

            OpType toothCategory = OpType.builder()
                    .categoryName("Diş")
                    .categoryCode("DIS")
                    .colorSelection(true)
                    .implantSelection(true)
                    .status(Status.ACTIVE)
                    .build();

            toothCategory = operationTypeRepository.save(toothCategory);

            String[] operations = {
                "Diş çəkimi",
                "Diş təmizlənməsi",
                "Diş müalicəsi",
                "Diş protezləşdirilməsi",
                "Diş implantı",
                "Diş bərpası",
                "Plomb",
                "Karies",
                "Körpü Diş",
                "Kanal Müalicəsi",
                "Digər"
            };

            List<OpTypeItem> items = new ArrayList<>();
            for (int i = 0; i < operations.length; i++) {
                items.add(OpTypeItem.builder()
                        .operationName(operations[i])
                        .operationCode("DIS_" + (i + 1))
                        .status(Status.ACTIVE)
                        .amount(BigDecimal.valueOf(80.00 + (i * 45)))
                        .opType(toothCategory)
                        .build());
            }
            operationTypeItemRepository.saveAll(items);
            log.info("Operation Types and Items seeded successfully!");
        }
    }
}
