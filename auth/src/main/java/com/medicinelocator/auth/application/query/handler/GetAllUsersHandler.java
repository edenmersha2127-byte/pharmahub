package com.medicinelocator.auth.application.query.handler;

import com.medicinelocator.auth.application.dto.response.PagedResponse;
import com.medicinelocator.auth.application.dto.response.UserAdminResponse;
import com.medicinelocator.auth.application.mapper.AdminMapper;
import com.medicinelocator.auth.application.query.GetAllUsersQuery;
import com.medicinelocator.auth.application.service.AdminService;
import com.medicinelocator.auth.application.service.CustomerService;
import com.medicinelocator.auth.application.service.PharmacyService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Component
public class GetAllUsersHandler {

    private final CustomerService customerService;
    private final PharmacyService pharmacyService;
    private final AdminService adminService;
    private final AdminMapper adminMapper;

    public GetAllUsersHandler(CustomerService customerService,
                              PharmacyService pharmacyService,
                              AdminService adminService,
                              AdminMapper adminMapper) {
        this.customerService = customerService;
        this.pharmacyService = pharmacyService;
        this.adminService = adminService;
        this.adminMapper = adminMapper;
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserAdminResponse> handle(GetAllUsersQuery query) {
        List<UserAdminResponse> allUsers = new ArrayList<>();

        customerService.findAll().forEach(c -> allUsers.add(adminMapper.toUserAdminResponse(c)));
        pharmacyService.findAllUnpaged().forEach(p -> allUsers.add(adminMapper.toUserAdminResponse(p)));
        adminService.findAll().forEach(a -> allUsers.add(adminMapper.toUserAdminResponse(a)));

        allUsers.sort(Comparator.comparing(UserAdminResponse::getCreatedAt).reversed());

        int totalElements = allUsers.size();
        int fromIndex = Math.min(query.getPage() * query.getSize(), totalElements);
        int toIndex = Math.min(fromIndex + query.getSize(), totalElements);

        List<UserAdminResponse> pageContent = allUsers.subList(fromIndex, toIndex);

        return new PagedResponse<>(pageContent, query.getPage(), query.getSize(), totalElements);
    }
}