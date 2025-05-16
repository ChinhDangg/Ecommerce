package dev.ecommerce.product.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    @GetMapping()
    public String getAdminDashboardPage() {
        return "/admin-dashboard/admin-dashboard.html";
    }

    @GetMapping("/addNewProduct")
    public String getAddNewProductContent() {
        return "/product-add-new-product/add-new-product.html";
    }

}
