package dev.ecommercefrontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String getAddNewProductTemplate(Model model) {
        model.addAttribute("categoryTop_url", "http://localhost:8080/api/category/top");
        model.addAttribute("categorySubcategory_url", "http://localhost:8080/api/category/subcategory");
        return "/admin-dashboard/add-new-product :: add-new-product-template";
    }

    @GetMapping("/updateProduct")
    public String getUpdateProductContent(Model model) {
        model.addAttribute("media_url", "http://localhost:8080");
        model.addAttribute("productLine_url", "http://localhost:8080/api/productLine");
        model.addAttribute("productWrapper_url", "http://localhost:8080/api/productWrapper");
        model.addAttribute("product_url", "http://localhost:8080/api/product");
        model.addAttribute("productSearch_url", "http://localhost:8080/api/product/search");
        model.addAttribute("category_url", "http://localhost:8080/api/category");
        model.addAttribute("categoryTop_url", "http://localhost:8080/api/category/top");
        model.addAttribute("categorySubcategory_url", "http://localhost:8080/api/category/subcategory");
        model.addAttribute("categoryParent_url", "http://localhost:8080/api/category/parent");
        return "/admin-dashboard/update-product :: update-product-template";
    }

}
