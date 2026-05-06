package com.vietnl.tableservice.application.usecases;

import com.vietnl.tableservice.application.dto.TableRequest;
import com.vietnl.tableservice.domain.entities.RestaurantTable;
import com.vietnl.tableservice.domain.enums.TableStatus;
import com.vietnl.tableservice.domain.validator.TableValidator;
import com.vietnl.tableservice.infrastructure.persistence.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;
    private final TableValidator tableValidator;

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

        return tableRepository.save(table);
    }

    public RestaurantTable update(UUID id, TableRequest request) {
        tableValidator.validateUpdate(request);
        RestaurantTable table = getById(id);

        if (request.getCapacity() != null) table.setCapacity(request.getCapacity());
        if (request.getLocation() != null) table.setLocation(request.getLocation());
        if (request.getAreaRefId() != null) table.setAreaRefId(request.getAreaRefId());

        return tableRepository.save(table);
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

        return tableRepository.save(table);
    }

    public void delete(UUID id) {
        RestaurantTable table = getById(id);
        tableValidator.validateDelete(table);
        tableRepository.delete(table);
    }
}