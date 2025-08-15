package dev.ecommercefrontend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @Value("${url.gateway}")
    private String gatewayUrl;

    @ModelAttribute("gatewayUrl")
    public String gatewayUrl() { return gatewayUrl; }

    @ModelAttribute
    public void mediaUrl(Model model) {
        model.addAttribute("media_url", gatewayUrl+"/images");
        model.addAttribute("card_url", gatewayUrl+"/api/productWrapper/card");
        model.addAttribute("search_url", gatewayUrl+"/api/product/search");
        model.addAttribute("cardPage_url", gatewayUrl+"/product/card");
        model.addAttribute("searchPage_url", gatewayUrl+"/product/search");

        model.addAttribute("adminPage_url", gatewayUrl+"/admin/dashboard");
        model.addAttribute("adminPage_path", "/admin/dashboard");
        model.addAttribute("addProduct_path", "/admin/dashboard/addNewProduct");
        model.addAttribute("updateProduct_path", "/admin/dashboard/updateProduct");

        model.addAttribute("updateProductPage_url", gatewayUrl+"/admin/dashboard?query=updateProduct");
        model.addAttribute("productWrapper_url", gatewayUrl+"/api/productWrapper");
        model.addAttribute("categoryTop_url", gatewayUrl+"/api/category/top");
        model.addAttribute("categorySubcategory_url", gatewayUrl+"/api/category/subcategory");

        model.addAttribute("updateProductPage_url", gatewayUrl+"/admin/dashboard?query=updateProduct");
        model.addAttribute("productLine_url", gatewayUrl+"/api/productLine");
        model.addAttribute("productWrapper_url", gatewayUrl+"/api/productWrapper");
        model.addAttribute("product_url", gatewayUrl+"/api/product");
        model.addAttribute("productSearch_url", gatewayUrl+"/api/product/search");
        model.addAttribute("category_url", gatewayUrl+"/api/category");
        model.addAttribute("categoryParent_url", gatewayUrl+"/api/category/parent");
    }
}
