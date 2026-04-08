package com.finance.likelist.service;

import com.finance.likelist.model.Product;
import com.finance.likelist.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.getAllProducts();
    }

    public Product getProductByNo(Integer no) {
        return productRepository.getProductByNo(no);
    }

    public Integer createProduct(Product product) {
        return productRepository.insertProduct(product);
    }
}
