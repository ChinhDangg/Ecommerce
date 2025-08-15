package dev.ecommercefrontend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @Value("${url.front-end}")
    private String feUrl;

    @Value("${url.back-end}")
    private String beUrl;

    @ModelAttribute("feUrl")
    public String feUrl() { return feUrl; }

    @ModelAttribute("beUrl")
    public String beUrl() { return beUrl; }

    @ModelAttribute
    public void mediaUrl(Model model) {
        model.addAttribute("media_url", beUrl+"/images");
        model.addAttribute("card_url", beUrl+"/api/productWrapper/card");
        model.addAttribute("search_url", beUrl+"/api/product/search");
        model.addAttribute("cardPage_url", feUrl+"/product/card");
        model.addAttribute("searchPage_url", feUrl+"/product/search");

        model.addAttribute("adminPage_url", feUrl+"/admin/dashboard");
        model.addAttribute("adminPage_path", "/admin/dashboard");
        model.addAttribute("addProduct_path", "/admin/dashboard/addNewProduct");
        model.addAttribute("updateProduct_path", "/admin/dashboard/updateProduct");

        model.addAttribute("updateProductPage_url", feUrl+"/admin/dashboard?query=updateProduct");
        model.addAttribute("productWrapper_url", beUrl+"/api/productWrapper");
        model.addAttribute("categoryTop_url", beUrl+"/api/category/top");
        model.addAttribute("categorySubcategory_url", beUrl+"/api/category/subcategory");

        model.addAttribute("updateProductPage_url", feUrl+"/admin/dashboard?query=updateProduct");
        model.addAttribute("productLine_url", beUrl+"/api/productLine");
        model.addAttribute("productWrapper_url", beUrl+"/api/productWrapper");
        model.addAttribute("product_url", beUrl+"/api/product");
        model.addAttribute("productSearch_url", beUrl+"/api/product/search");
        model.addAttribute("category_url", beUrl+"/api/category");
        model.addAttribute("categoryParent_url", beUrl+"/api/category/parent");
    }
}
