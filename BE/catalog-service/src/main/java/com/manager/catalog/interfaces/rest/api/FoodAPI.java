package com.manager.catalog.interfaces.rest.api;

import com.manager.common.interfaces.rest.dto.BaseResponseDTO;
import com.manager.catalog.interfaces.rest.dto.FoodDTOs;
import com.manager.catalog.domain.models.entities.Food;
import com.manager.catalog.domain.models.enums.FoodCategory;
import com.manager.catalog.infrastructure.persistence.jpa.FoodRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/foods")
@RequiredArgsConstructor
public class FoodAPI {

    private final FoodRepository foodRepository;

    private String resolveServer(HttpServletRequest http, Claims claims) {
        if (claims != null) {
            String s = claims.get("server", String.class);
            if (s != null && !s.isBlank())
                return s;
        }
        String header = http.getHeader("X-Server");
        return (header == null || header.isBlank()) ? "local" : header;
    }

    private String resolveEmployeeId(Claims claims) {
        if (claims == null)
            return "anonymous";
        String sub = claims.getSubject();
        if (sub != null && !sub.isBlank())
            return sub;
        String eid = claims.get("employeeId", String.class);
        return (eid == null || eid.isBlank()) ? "anonymous" : eid;
    }

    @PostMapping("/create")
    public BaseResponseDTO createFood(@Valid @RequestBody FoodDTOs.FoodRequest request, HttpServletRequest http) {
        Claims claims = (Claims) http.getAttribute("claims");
        String server = resolveServer(http, claims);
        String createdBy = resolveEmployeeId(claims);

        Food f = new Food();
        f.setId(java.util.UUID.randomUUID().toString());
        f.setFoodName(request.getFoodName());
        f.setImage(request.getImage());
        f.setPrice(request.getPrice());
        f.setUnit(request.getUnit());
        f.setCategory(request.getCategory());
        f.setDescription(request.getDescription());
        f.setCreatedBy(createdBy);
        f.setCreatedAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        f.setServer(server);

        Food saved = foodRepository.save(f);
        return new BaseResponseDTO("OK", "Resource created successfully", saved);
    }

    @PutMapping("/{id}")
    public BaseResponseDTO updateFood(@PathVariable String id,
            @Valid @RequestBody FoodDTOs.UpdateFoodRequest request,
            HttpServletRequest http) {
        Claims claims = (Claims) http.getAttribute("claims");
        String server = resolveServer(http, claims);

        Optional<Food> opt = foodRepository.findByIdAndServer(id, server);
        if (opt.isEmpty()) {
            return new BaseResponseDTO("ERROR", "Food not found");
        }

        Food f = opt.get();
        if (request.getFoodName() != null)
            f.setFoodName(request.getFoodName());
        if (request.getImage() != null)
            f.setImage(request.getImage());
        if (request.getPrice() != null)
            f.setPrice(request.getPrice());
        if (request.getUnit() != null)
            f.setUnit(request.getUnit());
        if (request.getCategory() != null)
            f.setCategory(request.getCategory());
        if (request.getDescription() != null)
            f.setDescription(request.getDescription());

        if (f != null) {
            Food saved = foodRepository.save(f);
            return new BaseResponseDTO("OK", "Food updated successfully", saved);
        }
        return new BaseResponseDTO("ERROR", "Update failed");
    }

    @SuppressWarnings("null")
    @DeleteMapping("/{id}")
    public BaseResponseDTO deleteFood(@PathVariable String id, HttpServletRequest http) {
        Claims claims = (Claims) http.getAttribute("claims");
        String server = resolveServer(http, claims);

        Optional<Food> opt = foodRepository.findByIdAndServer(id, server);
        if (opt.isEmpty()) {
            return new BaseResponseDTO("ERROR", "Food not found");
        }
        if (opt.isPresent()) {
            foodRepository.delete(opt.get());
        }
        return new BaseResponseDTO("OK", "Food deleted successfully");
    }

    @GetMapping(value = { "", "/", "/list" })
    public BaseResponseDTO listFoods(HttpServletRequest http) {
        Claims claims = (Claims) http.getAttribute("claims");
        String server = resolveServer(http, claims);
        return new BaseResponseDTO("OK", "Success", foodRepository.findByServer(server));
    }

    @GetMapping("/{id}")
    public BaseResponseDTO getFoodById(@PathVariable String id, HttpServletRequest http) {
        Claims claims = (Claims) http.getAttribute("claims");
        String server = resolveServer(http, claims);
        return foodRepository.findByIdAndServer(id, server)
                .map(food -> new BaseResponseDTO("OK", "Success", food))
                .orElse(new BaseResponseDTO("ERROR", "Food not found"));
    }

    @GetMapping("/categories")
    public BaseResponseDTO getCategories() {
        return new BaseResponseDTO("OK", "Success", List.of(FoodCategory.values()));
    }
}
