package dev.ecommercefrontend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/product")
public class ProductController {

    @GetMapping("/card/{id}")
    public String getProductCard(@PathVariable Long id, Model model) {
        model.addAttribute("productId", id);
        return "/product-card-draft-template";
    }

    @GetMapping("/search")
    public String getProductSearch(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("name", name);
        model.addAttribute("page", page);
        return "/product-search-draft";
    }
    
}
