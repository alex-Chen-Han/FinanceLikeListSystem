package com.finance.likelist.repository;

import com.finance.likelist.model.LikeList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.sql.Types;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;

@Repository
public class LikeListRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public LikeListRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<LikeList> likeListRowMapper = new RowMapper<LikeList>() {
        @Override
        public LikeList mapRow(ResultSet rs, int rowNum) throws SQLException {
            LikeList likeList = new LikeList();
            likeList.setSn(rs.getInt("SN"));
            likeList.setUserId(rs.getString("UserId"));
            likeList.setProductNo(rs.getInt("ProductNo"));
            likeList.setPurchaseQuantity(rs.getInt("PurchaseQuantity"));
            likeList.setAccount(rs.getString("Account"));
            likeList.setTotalFee(rs.getBigDecimal("TotalFee"));
            likeList.setTotalAmount(rs.getBigDecimal("TotalAmount"));
            likeList.setIsDeleted(rs.getBoolean("IsDeleted"));
            if (rs.getTimestamp("CreatedAt") != null)
                likeList.setCreatedAt(rs.getTimestamp("CreatedAt"));

            try {
                likeList.setUserName(rs.getString("UserName"));
                likeList.setProductName(rs.getString("ProductName"));
                likeList.setPrice(rs.getBigDecimal("Price"));
                if (rowNum == 0) {
                    System.out.println("成功取得關聯欄位資料");
                }
            } catch (SQLException e) {
                if (rowNum == 0) {
                    System.out.println("未取得關聯欄位資料");
                }
            }
            return likeList;
        }
    };

    public Integer insertLikeList(LikeList likeList) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("SP_InsertLikeList")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("UserId", Types.VARCHAR),
                        new SqlParameter("ProductNo", Types.INTEGER),
                        new SqlParameter("PurchaseQuantity", Types.INTEGER),
                        new SqlParameter("Account", Types.VARCHAR),
                        new SqlParameter("TotalFee", Types.DECIMAL),
                        new SqlParameter("TotalAmount", Types.DECIMAL),
                        new SqlOutParameter("SN", Types.INTEGER));
        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("UserId", likeList.getUserId())
                .addValue("ProductNo", likeList.getProductNo())
                .addValue("PurchaseQuantity", likeList.getPurchaseQuantity())
                .addValue("Account", likeList.getAccount())
                .addValue("TotalFee", likeList.getTotalFee())
                .addValue("TotalAmount", likeList.getTotalAmount());
        Map<String, Object> out = jdbcCall.execute(in);
        return (Integer) out.get("SN");
    }

    public List<LikeList> getActiveLikeLists() {
        return jdbcTemplate.query("EXEC SP_GetActiveLikeLists", likeListRowMapper);
    }

    public List<LikeList> getDeletedLikeLists() {
        return jdbcTemplate.query("EXEC SP_GetDeletedLikeLists", likeListRowMapper);
    }

    public void updateLikeList(LikeList likeList) {
        jdbcTemplate.update("EXEC SP_UpdateLikeList ?, ?, ?, ?, ?, ?, ?",
                likeList.getSn(), likeList.getUserId(), likeList.getAccount(), likeList.getProductNo(),
                likeList.getPurchaseQuantity(), likeList.getTotalFee(), likeList.getTotalAmount());
    }

    public void deleteLikeListSoft(Integer sn) {
        jdbcTemplate.update("EXEC SP_DeleteLikeListSoft ?", sn);
    }

    public void restoreLikeList(Integer sn) {
        jdbcTemplate.update("EXEC SP_RestoreLikeList ?", sn);
    }

    public void deleteLikeListHard(Integer sn) {
        jdbcTemplate.update("EXEC SP_DeleteLikeListHard ?", sn);
    }
}
