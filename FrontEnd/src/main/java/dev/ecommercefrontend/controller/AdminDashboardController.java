package dev.ecommercefrontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    @GetMapping()
    public String getAdminDashboardPage() {
        return "/admin-dashboard/admin-dashboard";
    }

    @GetMapping("/addNewProduct")
    public String getAddNewProductTemplate() {
        return "/product-add-new-product/add-new-product :: add-new-product-template";
    }

    @GetMapping("/updateProduct")
    public String getUpdateProductContent() {
        return "/product-update-product/update-product :: update-product-template";
    }

}
