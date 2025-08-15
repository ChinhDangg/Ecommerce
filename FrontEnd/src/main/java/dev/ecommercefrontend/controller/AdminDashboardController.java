package dev.ecommercefrontend.controller;

import dev.ecommercefrontend.service.BackendCall;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    @Value("${url.gateway}")
    private String gatewayUrl;

    private final BackendCall backendCall;

    public AdminDashboardController(BackendCall backendCall) {
        this.backendCall = backendCall;
    }

    @GetMapping()
    public String getAdminDashboardPage(HttpServletRequest request, Model model) {
        if (!backendCall.checkHasAdminRole(request)) {
            return "redirect:" +gatewayUrl+ "/login";
        }
        return "/admin-dashboard/admin-dashboard";
    }

    @GetMapping("/addNewProduct")
    public String getAddNewProductTemplate(HttpServletRequest request, Model model) {
        if (!backendCall.checkHasAdminRole(request)) {
            return "redirect:" +gatewayUrl+ "/login";
        }
        return "/admin-dashboard/add-new-product :: add-new-product-template";
    }

    @GetMapping("/updateProduct")
    public String getUpdateProductContent(HttpServletRequest request, Model model) {
        if (!backendCall.checkHasAdminRole(request)) {
            return "redirect:" +gatewayUrl+ "/login";
        }
        return "/admin-dashboard/update-product :: update-product-template";
    }

}
