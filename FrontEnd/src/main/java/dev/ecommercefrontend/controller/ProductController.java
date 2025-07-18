package dev.ecommercefrontend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

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
            @RequestParam Map<String, String> allParams,
            Model model) {

        String searchString = allParams.remove("q");
        String pageStr = allParams.remove("page");
        int page = Integer.parseInt(pageStr != null ? pageStr : "0");
        String featureStr = allParams.remove("feature");
        boolean getFeatures = Boolean.parseBoolean(featureStr != null ? featureStr : "false");

        String filterParam = allParams.remove("filters"); // e.g., GPU:4090|4080,RAM:32GB|64GB

        model.addAttribute("search_string", searchString);
        model.addAttribute("page", page);
        model.addAttribute("feature", getFeatures);
        model.addAttribute("filters", filterParam);
        return "/product-search-draft";
    }
    
}
