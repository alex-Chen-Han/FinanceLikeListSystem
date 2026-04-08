package com.finance.likelist.controller;

import com.finance.likelist.common.ApiResponse;
import com.finance.likelist.dto.LikeListDto;
import com.finance.likelist.model.LikeList;
import com.finance.likelist.service.LikeListService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preferences")
@CrossOrigin(origins = "*")
public class LikeListController {

    private final LikeListService likeListService;

    @Autowired
    public LikeListController(LikeListService likeListService) {
        this.likeListService = likeListService;
    }

    @GetMapping("/active")
    public ApiResponse<List<LikeList>> getActiveLikeLists() {
        return ApiResponse.success(likeListService.getActiveLikeLists());
    }

    @GetMapping("/deleted")
    public ApiResponse<List<LikeList>> getDeletedLikeLists() {
        return ApiResponse.success(likeListService.getDeletedLikeLists());
    }

    @PostMapping
    public ApiResponse<Integer> createLikeList(@Valid @RequestBody LikeListDto dto) {
        try {
            Integer sn = likeListService.createLikeList(dto);
            return ApiResponse.success("喜好清單建立成功", sn);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{sn}")
    public ApiResponse<Void> updateLikeList(@PathVariable Integer sn, @Valid @RequestBody LikeListDto dto) {
        try {
            likeListService.updateLikeList(sn, dto);
            return ApiResponse.success("喜好清單更新成功", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{sn}")
    public ApiResponse<Void> deleteLikeListSoft(@PathVariable Integer sn) {
        likeListService.deleteLikeListSoft(sn);
        return ApiResponse.success("喜好清單刪除成功", null);
    }

    @PatchMapping("/{sn}/restore")
    public ApiResponse<Void> restoreLikeList(@PathVariable Integer sn) {
        likeListService.restoreLikeList(sn);
        return ApiResponse.success("喜好清單復原成功", null);
    }

    @DeleteMapping("/{sn}/hard")
    public ApiResponse<Void> deleteLikeListHard(@PathVariable Integer sn) {
        likeListService.deleteLikeListHard(sn);
        return ApiResponse.success("喜好清單永久刪除成功", null);
    }
}
