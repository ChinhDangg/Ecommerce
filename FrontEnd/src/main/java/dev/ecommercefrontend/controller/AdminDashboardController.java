package dev.ecommercefrontend.controller;

import dev.ecommercefrontend.service.BackendCall;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    private final BackendCall backendCall;

    public AdminDashboardController(BackendCall backendCall) {
        this.backendCall = backendCall;
    }

    @GetMapping()
    public String getAdminDashboardPage(HttpServletRequest request, Model model) {
        if (!backendCall.checkHasAdminRole(request)) {
            return "redirect:/login";
        }

        model.addAttribute("adminPage_url", "http://localhost:8081/admin/dashboard");
        model.addAttribute("adminPage_path", "/admin/dashboard");
        model.addAttribute("addProduct_path", "/admin/dashboard/addNewProduct");
        model.addAttribute("updateProduct_path", "/admin/dashboard/updateProduct");
        return "/admin-dashboard/admin-dashboard";
    }

    @GetMapping("/addNewProduct")
    public String getAddNewProductTemplate(HttpServletRequest request, Model model) {
        if (!backendCall.checkHasAdminRole(request)) {
            return "redirect:/login";
        }

        model.addAttribute("updateProductPage_url", "http://localhost:8081/admin/dashboard?query=updateProduct");
        model.addAttribute("productWrapper_url", "http://localhost:8080/api/productWrapper");
        model.addAttribute("media_url", "http://localhost:8080/images");
        model.addAttribute("categoryTop_url", "http://localhost:8080/api/category/top");
        model.addAttribute("categorySubcategory_url", "http://localhost:8080/api/category/subcategory");
        return "/admin-dashboard/add-new-product :: add-new-product-template";
    }

    @GetMapping("/updateProduct")
    public String getUpdateProductContent(HttpServletRequest request, Model model) {
        if (!backendCall.checkHasAdminRole(request)) {
            return "redirect:/login";
        }

        model.addAttribute("media_url", "http://localhost:8080/images");
        model.addAttribute("updateProductPage_url", "http://localhost:8081/admin/dashboard?query=updateProduct");
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
