package com.finance.likelist.controller;

import com.finance.likelist.common.ApiResponse;
import com.finance.likelist.model.Product;
import com.finance.likelist.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<List<Product>> getAllProducts() {
        return ApiResponse.success(productService.getAllProducts());
    }

    @PostMapping
    public ApiResponse<Void> createProduct(@RequestBody Product product) {
        productService.createProduct(product);
        return ApiResponse.success("操作成功", null);
    }
}
