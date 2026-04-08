package com.finance.likelist.repository;

import com.finance.likelist.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.sql.Types;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Product> productRowMapper = new RowMapper<Product>() {
        @Override
        public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
            Product product = new Product();
            product.setNo(rs.getInt("No"));
            product.setProductName(rs.getString("ProductName"));
            product.setPrice(rs.getBigDecimal("Price"));
            product.setFeeRate(rs.getBigDecimal("FeeRate"));
            if (rs.getTimestamp("CreatedAt") != null) product.setCreatedAt(rs.getTimestamp("CreatedAt"));
            return product;
        }
    };

    public Integer insertProduct(Product product) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("SP_InsertProduct")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("ProductName", Types.NVARCHAR),
                        new SqlParameter("Price", Types.DECIMAL),
                        new SqlParameter("FeeRate", Types.DECIMAL),
                        new SqlOutParameter("No", Types.INTEGER)
                );
        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("ProductName", product.getProductName())
                .addValue("Price", product.getPrice())
                .addValue("FeeRate", product.getFeeRate());
        Map<String, Object> out = jdbcCall.execute(in);
        return (Integer) out.get("No");
    }

    public List<Product> getAllProducts() {
        return jdbcTemplate.query("EXEC SP_GetAllProducts", productRowMapper);
    }

    public Product getProductByNo(Integer no) {
        List<Product> products = jdbcTemplate.query("EXEC SP_GetProductByNo ?", productRowMapper, no);
        return products.isEmpty() ? null : products.get(0);
    }
}
