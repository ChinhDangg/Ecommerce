package dev.ecommercefrontend.controller;

import dev.ecommercefrontend.service.BackendCall;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
    public String getAdminDashboardPage(HttpServletRequest request) {
        if (!backendCall.checkHasAdminRole(request)) {
            String redirectParam = URLEncoder.encode("/admin/dashboard", StandardCharsets.UTF_8);
            String redirectUrl = gatewayUrl + "/login?r=" + redirectParam;
            return "redirect:" + redirectUrl;
        }
        return "/admin-dashboard/admin-dashboard";
    }

    @GetMapping("/addNewProduct")
    public String getAddNewProductTemplate(HttpServletRequest request) {
        if (!backendCall.checkHasAdminRole(request)) {
            return "redirect:" +gatewayUrl+ "/login";
        }
        return "/admin-dashboard/add-new-product :: add-new-product-template";
    }

    @GetMapping("/updateProduct")
    public String getUpdateProductContent(HttpServletRequest request) {
        if (!backendCall.checkHasAdminRole(request)) {
            return "redirect:" +gatewayUrl+ "/login";
        }
        return "/admin-dashboard/update-product :: update-product-template";
    }

}
