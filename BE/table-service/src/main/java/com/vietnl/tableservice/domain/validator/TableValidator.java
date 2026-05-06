package com.vietnl.tableservice.domain.validator;

import com.vietnl.tableservice.application.dto.TableRequest;
import com.vietnl.tableservice.domain.entities.RestaurantTable;
import com.vietnl.tableservice.domain.enums.TableStatus;
import com.vietnl.tableservice.infrastructure.persistence.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class TableValidator {

    private final TableRepository tableRepository;

    public void validateCreate(TableRequest request) {
        if (request.getTableNumber() == null || request.getTableNumber() < 1) {
            throw new RuntimeException("Số bàn phải lớn hơn hoặc bằng 1");
        }
        if (tableRepository.existsByTableNumber(request.getTableNumber())) {
            throw new RuntimeException("Số bàn " + request.getTableNumber() + " đã tồn tại trên hệ thống");
        }
        if (request.getCapacity() != null && request.getCapacity() < 1) {
            throw new RuntimeException("Sức chứa phải ít nhất là 1 người");
        }
    }

    public void validateUpdate(TableRequest request) {
        if (request.getCapacity() != null && request.getCapacity() < 1) {
            throw new RuntimeException("Sức chứa không hợp lệ");
        }
        if (request.getLocation() != null && request.getLocation().length() > 255) {
            throw new RuntimeException("Mô tả vị trí quá dài (tối đa 255 ký tự)");
        }
    }

    public void validateAssign(RestaurantTable table, String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new RuntimeException("Mã đơn hàng (orderId) không được để trống");
        }
        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new RuntimeException("Bàn " + table.getTableNumber() + " hiện đang không ở trạng thái TRỐNG (Status: " + table.getStatus() + ")");
        }
    }

    public void validateStatusChange(RestaurantTable table, TableStatus newStatus) {
        if (newStatus == null) throw new RuntimeException("Trạng thái mới không hợp lệ");
        
        // Ví dụ: Không cho phép chuyển từ OCCUPIED (Đang có khách) về AVAILABLE trực tiếp mà không qua CLEANING (tùy nghiệp vụ)
        // Ở đây tôi giữ đơn giản: Chỉ kiểm tra rỗng
    }

    public void validateDelete(RestaurantTable table) {
        if (table.getStatus() == TableStatus.OCCUPIED) {
            throw new RuntimeException("Không thể xóa bàn đang có khách (OCCUPIED). Vui lòng thanh toán hoặc giải phóng bàn trước.");
        }
    }
}