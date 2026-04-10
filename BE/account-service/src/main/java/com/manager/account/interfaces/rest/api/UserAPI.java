package com.manager.account.interfaces.rest.api;

import com.manager.common.interfaces.rest.dto.BaseResponseDTO;
import com.manager.account.interfaces.rest.dto.UserDTOs;
import com.manager.account.interfaces.rest.dto.UserDTOs.UserListItemDTO;
import com.manager.account.domain.models.entities.Users;
import com.manager.account.infrastructure.persistence.jpa.UserRepository;
import com.manager.account.application.services.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserAPI {

    private final UserService userService;
    private final UserRepository userRepository;

    // POST /users/register (body JSON)
    @PostMapping("/register")
    public BaseResponseDTO register(@Valid @RequestBody UserDTOs.RegisterRequestDTO body) {
        return userService.register(body);
    }

    @GetMapping("/getInfo")
    public BaseResponseDTO getInfo(HttpServletRequest request) {
        BaseResponseDTO res = new BaseResponseDTO();
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) {
            res.setCode("ERROR");
            res.setMessage("Missing or invalid token");
            return res;
        }

        String uid = claims.get("uid", String.class);
        String role = claims.get("role", String.class);
        String server = claims.get("server", String.class);

        Users u = null;
        if (uid != null) {
            u = userRepository.findByUsername(uid)
                    .orElseGet(() -> userRepository.findByEmployeeId(uid).orElse(null));
        }

        Map<String, Object> data = Map.of(
                "uid", uid != null ? uid : "unknown",
                "role", role != null ? role : "USER",
                "server", server != null ? server : "HCM",
                "fullName", u != null && u.getFullName() != null ? u.getFullName() : "",
                "email", u != null && u.getEmail() != null ? u.getEmail() : "",
                "phoneNumber", u != null && u.getPhoneNumber() != null ? u.getPhoneNumber() : "",
                "birthday", u != null && u.getBirthday() != null ? u.getBirthday().toString() : "",
                "gender", u != null && u.getGender() != null ? u.getGender() : "");

        res.setCode("OK");
        res.setMessage("OK");
        res.setData(data);
        return res;
    }

    @GetMapping
    public BaseResponseDTO listUsers(@RequestParam String server) {
        List<Users> list = userRepository.findByServer(server);
        List<UserListItemDTO> data = list.stream()
                .map(u -> new UserListItemDTO(
                        u.getEmployeeId(),
                        safe(u.getFullName()),
                        safe(u.getRole()),
                        safe(u.getPhoneNumber()),
                        u.getBirthday() != null ? u.getBirthday().toString() : "",
                        "", // createdAt
                        null, // createdBy
                        safe(u.getEmail()),
                        safe(u.getServer()),
                        safe(u.getGender())))
                .collect(Collectors.toList());

        BaseResponseDTO res = new BaseResponseDTO();
        res.setCode("OK");
        res.setMessage("OK");
        res.setData(data);
        return res;
    }

    @GetMapping("/count-by-server")
    public BaseResponseDTO countByServer(@RequestParam String server) {
        long count = userRepository.findByServer(server).size();
        BaseResponseDTO res = new BaseResponseDTO();
        res.setCode("OK");
        res.setMessage("OK");
        res.setData(count);
        return res;
    }

    @PutMapping("/{server}/{employeeId}")
    public BaseResponseDTO updateEmployee(
            @PathVariable String server,
            @PathVariable String employeeId,
            @RequestBody UserDTOs.UpdateStaffRequestDTO body) {
        return userService.updateEmployee(server, employeeId, body);
    }

    @DeleteMapping("/{server}/{employeeId}")
    public BaseResponseDTO deleteEmployee(
            @PathVariable String server,
            @PathVariable String employeeId) {
        return userService.deleteEmployee(server, employeeId);
    }

    private static String safe(String v) {
        return v == null ? "" : v;
    }
}
