package com.medicinelocator.notificationservice.application.query.handler;

import com.medicinelocator.notificationservice.application.dto.response.ActivityResponse;
import com.medicinelocator.notificationservice.application.mapper.NotificationMapper;
import com.medicinelocator.notificationservice.application.query.GetActivityQuery;
import com.medicinelocator.notificationservice.application.service.NotificationService;
import com.medicinelocator.notificationservice.domain.enums.NotificationType;
import com.medicinelocator.notificationservice.domain.enums.RecipientType;
import com.medicinelocator.notificationservice.domain.model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetActivityHandler")
class GetActivityHandlerTest {

    @Mock private NotificationService notificationService;

    private GetActivityHandler handler;

    private final UUID pharmacyId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        handler = new GetActivityHandler(notificationService, new NotificationMapper());
    }

    @Test
    @DisplayName("maps stored dashboard notifications to activity entries")
    void handle_mapsNotifications() {
        LocalDateTime at = LocalDateTime.now();
        Notification sale = new Notification(UUID.randomUUID(), pharmacyId, RecipientType.PHARMACY,
                NotificationType.SALE_COMPLETED, "Sale completed", "Sale of 100 recorded", "/pharmacy/sales",
                false, at, null);
        when(notificationService.findRecentActivity(eq(pharmacyId), any(), eq(5)))
                .thenReturn(List.of(sale));

        List<ActivityResponse> activity = handler.handle(new GetActivityQuery(pharmacyId, 5));

        assertEquals(1, activity.size());
        ActivityResponse entry = activity.get(0);
        assertEquals("SALE_COMPLETED", entry.type());
        assertEquals("Sale completed", entry.title());
        assertEquals("Sale of 100 recorded", entry.detail());
        assertNull(entry.amount());
        assertEquals(at, entry.at());
    }
}
