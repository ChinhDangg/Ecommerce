package dev.ecommercefrontend.controller;

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
        model.addAttribute("media_url", "http://localhost:8080/images");
        model.addAttribute("card_url", "http://localhost:8080/api/productWrapper/card");
        model.addAttribute("cardPage_url", "http://localhost:8081/product/card");
        model.addAttribute("search_url", "http://localhost:8080/api/product/search");
        return "/product-page/product-card";
    }

    @GetMapping("/search")
    public String getProductSearch(
            @RequestParam Map<String, String> allParams,
            Model model) {

        String searchString = allParams.remove("q");
        String categoryIdStr = allParams.remove("cateId");

        String pageStr = allParams.remove("page");
        String featureStr = allParams.remove("feature");
        String sortStr = allParams.remove("sort");

        String specialFilterParam = allParams.remove("s-filters");
        String filterParam = allParams.remove("filters"); // e.g., GPU:4090|4080,RAM:32GB|64GB

        model.addAttribute("search_string", searchString);
        model.addAttribute("category_id", categoryIdStr);

        model.addAttribute("page", pageStr);
        model.addAttribute("feature", featureStr);
        model.addAttribute("sort", sortStr);
        model.addAttribute("special_filter", specialFilterParam);
        model.addAttribute("filter", filterParam);
        model.addAttribute("media_url", "http://localhost:8080/images");
        model.addAttribute("search_url", "http://localhost:8080/api/product/search");
        model.addAttribute("searchPage_url", "http://localhost:8081/product/search");
        model.addAttribute("cardPage_url", "http://localhost:8081/product/card/");
        return "/product-page/product-search";
    }

}
