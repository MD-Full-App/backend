package com.rustam.modern_dentistry.mapper.laboratory;

import com.rustam.modern_dentistry.dao.entity.laboratory.DentalOrder;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class DentalOrderMapperTest {

    @Test
    void toResponse_shouldHandleMissingRelationsAndCollections() {
        DentalOrderMapper mapper = new DentalOrderMapper(null, null, null);
        DentalOrder order = DentalOrder.builder()
                .id(1L)
                .description("Test order")
                .toothDetails(Collections.emptyList())
                .teethList(Collections.emptyList())
                .imagePaths(Collections.emptyList())
                .build();

        var response = mapper.toResponse(order);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test order", response.getDescription());
        assertEquals("", response.getDoctor());
        assertEquals("", response.getPatient());
        assertEquals("", response.getTechnician());
        assertNotNull(response.getToothDetails());
        assertNotNull(response.getTeethList());
        assertNotNull(response.getUrls());
    }
}
