package com.vietnl.tableservice.application.usecases;

import com.vietnl.tableservice.application.dto.TableRequest;
import com.vietnl.tableservice.domain.entities.RestaurantTable;
import com.vietnl.tableservice.domain.enums.TableStatus;
import com.vietnl.tableservice.domain.validator.TableValidator;
import com.vietnl.tableservice.infrastructure.persistence.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;
    private final TableValidator tableValidator;
    private final SimpMessagingTemplate messagingTemplate;

    private void notifyTableChange() {
        messagingTemplate.convertAndSend("/topic/tables", "REFRESH_TABLES");
    }

    @jakarta.annotation.PostConstruct
    public void resetTablesStatus() {
        try {
            List<RestaurantTable> tables = tableRepository.findAll();
            tables.forEach(t -> {
                t.setStatus(TableStatus.AVAILABLE);
                t.setCurrentOrderId(null);
            });
            tableRepository.saveAllAndFlush(tables); // Dùng Flush để đẩy dữ liệu xuống DB ngay lập tức
            System.out.println(">>> [TableService] RESET THÀNH CÔNG. Danh sách bàn hiện tại:");
            tableRepository.findAll().forEach(t -> 
                System.out.println("  - Bàn " + t.getTableNumber() + ": " + t.getStatus())
            );
        } catch (Exception e) {
            System.err.println(">>> [TableService] LỖI RESET: " + e.getMessage());
        }
    }

    public List<RestaurantTable> getAll() {
        return tableRepository.findAll();
    }

    public List<RestaurantTable> getByStatus(TableStatus status) {
        return tableRepository.findByStatus(status);
    }

    public RestaurantTable getById(UUID id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn với ID: " + id));
    }

    public RestaurantTable create(TableRequest request) {
        tableValidator.validateCreate(request);

        RestaurantTable table = RestaurantTable.builder()
                .id(UUID.randomUUID())
                .tableNumber(request.getTableNumber())
                .capacity(request.getCapacity() != null ? request.getCapacity() : 4)
                .location(request.getLocation())
                .areaRefId(request.getAreaRefId())
                .status(TableStatus.AVAILABLE)
                .build();

        RestaurantTable savedTable = tableRepository.save(table);
        notifyTableChange();
        return savedTable;
    }

    public RestaurantTable update(UUID id, TableRequest request) {
        tableValidator.validateUpdate(request);
        RestaurantTable table = getById(id);

        if (request.getCapacity() != null) table.setCapacity(request.getCapacity());
        if (request.getLocation() != null) table.setLocation(request.getLocation());
        if (request.getAreaRefId() != null) table.setAreaRefId(request.getAreaRefId());

        RestaurantTable savedTable = tableRepository.save(table);
        notifyTableChange();
        return savedTable;
    }

    public RestaurantTable updateStatus(UUID id, TableStatus status) {
        RestaurantTable table = getById(id);
        tableValidator.validateStatusChange(table, status);

        table.setStatus(status);

        // Tự động gỡ bỏ Order ID nếu bàn chuyển về trạng thái trống hoặc đang dọn
        if (status == TableStatus.AVAILABLE || status == TableStatus.CLEANING) {
            table.setCurrentOrderId(null);
        }

        return tableRepository.save(table);
    }

    public RestaurantTable assignOrder(UUID id, String orderId) {
        RestaurantTable table = getById(id);
        tableValidator.validateAssign(table, orderId);

        table.setCurrentOrderId(UUID.fromString(orderId));
        table.setStatus(TableStatus.OCCUPIED);

        RestaurantTable savedTable = tableRepository.save(table);
        notifyTableChange();
        return savedTable;
    }

    public void delete(UUID id) {
        RestaurantTable table = getById(id);
        tableValidator.validateDelete(table);
        tableRepository.delete(table);
        notifyTableChange();
    }
}