package com.finance.likelist.service;

import com.finance.likelist.dto.LikeListDto;
import com.finance.likelist.model.LikeList;
import com.finance.likelist.model.Product;
import com.finance.likelist.model.User;
import com.finance.likelist.repository.LikeListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LikeListService {

    private final LikeListRepository likeListRepository;
    private final UserService userService;
    private final ProductService productService;

    @Autowired
    public LikeListService(LikeListRepository likeListRepository, UserService userService, ProductService productService) {
        this.likeListRepository = likeListRepository;
        this.userService = userService;
        this.productService = productService;
    }

    public List<LikeList> getActiveLikeLists() {
        return likeListRepository.getActiveLikeLists();
    }

    public List<LikeList> getDeletedLikeLists() {
        return likeListRepository.getDeletedLikeLists();
    }

    @Transactional
    public Integer createLikeList(LikeListDto dto) {
        User user = userService.getUserById(dto.getUserId());
        if (user == null || Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new IllegalArgumentException("找不到使用者或該使用者已被刪除。");
        }
        if (!user.getAccount().equals(dto.getAccount())) {
            throw new IllegalArgumentException("扣款帳號與該使用者註冊的帳號不符。");
        }

        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setPrice(dto.getPrice());
        product.setFeeRate(dto.getFeeRate());
        Integer productNo = productService.createProduct(product);

        BigDecimal price = dto.getPrice();
        BigDecimal quantity = new BigDecimal(dto.getPurchaseQuantity());
        BigDecimal feeRate = dto.getFeeRate();
        BigDecimal totalFee = price.multiply(quantity).multiply(feeRate);
        BigDecimal totalAmount = price.multiply(quantity).add(totalFee).setScale(0, java.math.RoundingMode.DOWN);

        LikeList likeList = new LikeList();
        likeList.setUserId(dto.getUserId());
        likeList.setProductNo(productNo);
        likeList.setPurchaseQuantity(dto.getPurchaseQuantity());
        likeList.setAccount(dto.getAccount());
        likeList.setTotalFee(totalFee);
        likeList.setTotalAmount(totalAmount);

        return likeListRepository.insertLikeList(likeList);
    }

    @Transactional
    public void updateLikeList(Integer sn, LikeListDto dto) {
        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setPrice(dto.getPrice());
        product.setFeeRate(dto.getFeeRate());
        Integer productNo = productService.createProduct(product);

        BigDecimal price = dto.getPrice();
        BigDecimal quantity = new BigDecimal(dto.getPurchaseQuantity());
        BigDecimal feeRate = dto.getFeeRate();
        BigDecimal totalFee = price.multiply(quantity).multiply(feeRate);
        BigDecimal totalAmount = price.multiply(quantity).add(totalFee).setScale(0, java.math.RoundingMode.DOWN);

        LikeList likeList = new LikeList();
        likeList.setSn(sn);
        likeList.setUserId(dto.getUserId());
        likeList.setAccount(dto.getAccount());
        likeList.setProductNo(productNo);
        likeList.setPurchaseQuantity(dto.getPurchaseQuantity());
        likeList.setTotalFee(totalFee);
        likeList.setTotalAmount(totalAmount);

        likeListRepository.updateLikeList(likeList);
    }

    @Transactional
    public void deleteLikeListSoft(Integer sn) {
        likeListRepository.deleteLikeListSoft(sn);
    }

    @Transactional
    public void restoreLikeList(Integer sn) {
        likeListRepository.restoreLikeList(sn);
    }

    @Transactional
    public void deleteLikeListHard(Integer sn) {
        likeListRepository.deleteLikeListHard(sn);
    }
}
