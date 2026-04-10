package com.manager.order.interfaces.rest.api;

import com.manager.common.interfaces.rest.dto.BaseResponseDTO;
import com.manager.order.interfaces.rest.dto.TableDTOs;
import com.manager.order.domain.models.entities.Order;
import com.manager.order.domain.models.entities.OrderItem;
import com.manager.common.domain.models.enums.OrderStatus;
import com.manager.order.domain.models.entities.Table;
import com.manager.order.infrastructure.persistence.jpa.OrderRepository;
import com.manager.order.infrastructure.persistence.jpa.TableRepository;
//import com.manager.account.infrastructure.persistence.jpa.UserRepository;
import io.jsonwebtoken.Claims;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/tables")
@RequiredArgsConstructor
public class TableAPI {

    private final TableRepository tableRepository;
    private final OrderRepository orderRepository;
    // private final UserRepository userRepository;

    @PostMapping("/create")
    public BaseResponseDTO createTable(
            HttpServletRequest request,
            @Valid @RequestBody TableDTOs.TableRequest tableRequest) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) {
            return new BaseResponseDTO("ERROR", "Token invalid or missing");
        }
        String server = claims.get("server", String.class);

        Table table = new Table();
        table.setId(java.util.UUID.randomUUID().toString());
        table.setTableName(tableRequest.getTableName());
        table.setServer(server);
        table.setChairs(0); // Mặc định

        tableRepository.save(table);

        return new BaseResponseDTO("OK", "Resource created successfully");
    }

    @PutMapping("/{id}")
    public BaseResponseDTO updateTable(
            HttpServletRequest request,
            @PathVariable String id,
            @Valid @RequestBody TableDTOs.TableRequest updateRequest) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null)
            return new BaseResponseDTO("ERROR", "Unauthorized");
        String server = claims.get("server", String.class);

        Optional<Table> optionalTable = tableRepository.findByIdAndServer(id, server);
        if (optionalTable.isEmpty()) {
            return new BaseResponseDTO("ERROR", "Table not found");
        }
        Table table = optionalTable.get();
        table.setTableName(updateRequest.getTableName());
        tableRepository.save(table);

        return new BaseResponseDTO("OK", "Table updated successfully");
    }

    @SuppressWarnings("null")
    @DeleteMapping("/{id}")
    public BaseResponseDTO deleteTable(
            HttpServletRequest request,
            @PathVariable String id) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null)
            return new BaseResponseDTO("ERROR", "Unauthorized");
        String server = claims.get("server", String.class);

        Optional<Table> optionalTable = tableRepository.findByIdAndServer(id, server);
        if (optionalTable.isEmpty()) {
            return new BaseResponseDTO("ERROR", "Table not found");
        }
        if (optionalTable.isPresent()) {
            tableRepository.delete(optionalTable.get());
        }
        return new BaseResponseDTO("OK", "Table deleted successfully");
    }

    @GetMapping("/list")
    public BaseResponseDTO getTablesByServer(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null)
            return new BaseResponseDTO("ERROR", "Unauthorized");
        String server = claims.get("server", String.class);
        return new BaseResponseDTO("OK", "Success", tableRepository.findByServer(server));
    }

    @GetMapping("/list-free")
    public BaseResponseDTO getTablesFreeByServer(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null)
            return new BaseResponseDTO("ERROR", "Unauthorized");
        String server = claims.get("server", String.class);
        return new BaseResponseDTO("OK", "Success",
                tableRepository.findByServerAndCurrentOrderIdIsNull(server));
    }

    @GetMapping("/{tableId}")
    public BaseResponseDTO getTableById(
            HttpServletRequest request,
            @PathVariable String tableId) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null)
            return new BaseResponseDTO("ERROR", "Unauthorized");
        String server = claims.get("server", String.class);

        return tableRepository.findByIdAndServer(tableId, server)
                .map(table -> new BaseResponseDTO("OK", "Success", table))
                .orElse(new BaseResponseDTO("ERROR", "Table not found"));
    }

    @PostMapping("/copy-items")
    @Transactional
    public BaseResponseDTO copyItemsBetweenTables(
            HttpServletRequest request,
            @Valid @RequestBody TableDTOs.CopyItemsRequest dto) {

        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null)
            return new BaseResponseDTO("ERROR", "Unauthorized");
        String server = claims.get("server", String.class);
        String employeeId = claims.getSubject();

        Table sourceTable = tableRepository.findByIdAndServer(dto.getSourceTableId(), server).orElse(null);
        if (sourceTable == null || sourceTable.getCurrentOrderId() == null) {
            return new BaseResponseDTO("ERROR", "Source table/order not found");
        }

        Order sourceOrder = orderRepository.findByIdAndServer(sourceTable.getCurrentOrderId(), server).orElse(null);
        if (sourceOrder == null)
            return new BaseResponseDTO("ERROR", "Source order not found");

        Table targetTable = tableRepository.findByIdAndServer(dto.getTargetTableId(), server).orElse(null);
        if (targetTable == null)
            return new BaseResponseDTO("ERROR", "Target table not found");

        Order targetOrder;
        if (targetTable.getCurrentOrderId() == null) {
            targetOrder = new Order();
            targetOrder.setId(java.util.UUID.randomUUID().toString());
            targetOrder.setTableId(targetTable.getId());
            targetOrder.setServer(server);
            targetOrder.setCreatedAt(LocalDateTime.now());
            targetOrder.setCreatedBy(employeeId);
            targetOrder.setStatus(OrderStatus.ORDERING);
        } else {
            targetOrder = orderRepository.findByIdAndServer(targetTable.getCurrentOrderId(), server)
                    .orElseThrow(() -> new IllegalStateException("Order id not found"));
        }

        for (OrderItem src : sourceOrder.getItems()) {
            boolean found = false;
            for (OrderItem target : targetOrder.getItems()) {
                if (target.getFoodId().equals(src.getFoodId())) {
                    target.setQuantity(target.getQuantity() + src.getQuantity());
                    found = true;
                    break;
                }
            }
            if (!found) {
                OrderItem copy = new OrderItem();
                copy.setId(java.util.UUID.randomUUID().toString());
                copy.setOrderItemId(java.util.UUID.randomUUID().toString());
                copy.setFoodId(src.getFoodId());
                copy.setFoodName(src.getFoodName());
                copy.setPrice(src.getPrice());
                copy.setQuantity(src.getQuantity());
                copy.setOrder(targetOrder);
                targetOrder.getItems().add(copy);
            }
        }

        double total = targetOrder.getItems().stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
        targetOrder.setTotalAmount(total);
        orderRepository.save(targetOrder);

        targetTable.setCurrentOrderId(targetOrder.getId());
        tableRepository.save(targetTable);

        return new BaseResponseDTO("OK", "Items copied successfully", Map.of("targetOrderId", targetOrder.getId()));
    }

    @GetMapping("/{tableId}/current-order/creator")
    public BaseResponseDTO getOrderCreator(
            HttpServletRequest request,
            @PathVariable String tableId) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) {
            return new BaseResponseDTO("ERROR", "Unauthorized");
        }
        String server = claims.get("server", String.class);

        Optional<Table> optionalTable = tableRepository.findByIdAndServer(tableId, server);
        if (optionalTable.isEmpty()) {
            return new BaseResponseDTO("ERROR", "Table not found");
        }

        Table table = optionalTable.get();
        if (table.getCurrentOrderId() == null) {
            return new BaseResponseDTO("ERROR", "No active order on this table");
        }

        Optional<Order> optionalOrder = orderRepository.findByIdAndServer(table.getCurrentOrderId(), server);
        if (optionalOrder.isEmpty()) {
            return new BaseResponseDTO("ERROR", "Active order not found");
        }

        Order order = optionalOrder.get();
        String creatorId = order.getCreatedBy();
        return new BaseResponseDTO("OK", "Success", creatorId);
        /*
        return userRepository.findByEmployeeIdAndServer(creatorId, server)
                .or(() -> userRepository.findByUsername(creatorId))
                .map(user -> new BaseResponseDTO("OK", "Success", user.getFullName()))
                .orElse(new BaseResponseDTO("ERROR", "Creator details not found"));
        */
    }
}
