package com.finance.likelist.repository;

import com.finance.likelist.model.User;
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
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setUserId(rs.getString("UserId"));
            user.setUserName(rs.getString("UserName"));
            user.setEmail(rs.getString("Email"));
            user.setAccount(rs.getString("Account"));
            user.setIsDeleted(rs.getBoolean("IsDeleted"));
            if (rs.getTimestamp("CreatedAt") != null)
                user.setCreatedAt(rs.getTimestamp("CreatedAt"));
            if (rs.getTimestamp("UpdatedAt") != null)
                user.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
            return user;
        }
    };

    public void insertUser(User user) {
        jdbcTemplate.update("EXEC SP_InsertUser ?, ?, ?, ?",
                user.getUserId(), user.getUserName(), user.getEmail(), user.getAccount());
    }

    public boolean checkUserExists(String userId, String account) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("SP_CheckUserExists")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("UserId", Types.VARCHAR),
                        new SqlParameter("Account", Types.VARCHAR),
                        new SqlOutParameter("Exists", Types.BIT));
        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("UserId", userId)
                .addValue("Account", account);
        Map<String, Object> out = jdbcCall.execute(in);
        Object existsObj = out.get("Exists");
        if (existsObj instanceof Boolean) {
            return (Boolean) existsObj;
        } else if (existsObj instanceof Number) {
            return ((Number) existsObj).intValue() == 1;
        } else if (existsObj != null) {
            return "true".equalsIgnoreCase(existsObj.toString()) || "1".equals(existsObj.toString());
        }
        return false;
    }

    public List<User> getActiveUsers() {
        return jdbcTemplate.query("EXEC SP_GetActiveUsers", userRowMapper);
    }

    public List<User> getDeletedUsers() {
        return jdbcTemplate.query("EXEC SP_GetDeletedUsers", userRowMapper);
    }

    public User getUserById(String userId) {
        List<User> users = jdbcTemplate.query("EXEC SP_GetUserById ?", userRowMapper, userId);
        return users.isEmpty() ? null : users.get(0);
    }

    public void updateUser(User user) {
        jdbcTemplate.update("EXEC SP_UpdateUser ?, ?, ?, ?",
                user.getUserId(), user.getUserName(), user.getEmail(), user.getAccount());
    }

    public void deleteUserSoft(String userId) {
        jdbcTemplate.update("EXEC SP_DeleteUserSoft ?", userId);
    }

    public void restoreUser(String userId) {
        jdbcTemplate.update("EXEC SP_RestoreUser ?", userId);
    }

    public void deleteUserHard(String userId) {
        jdbcTemplate.update("EXEC SP_DeleteUserHard ?", userId);
    }

    public int checkUserHasLikeLists(String userId) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("SP_CheckUserHasLikeLists")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("UserId", Types.VARCHAR),
                        new SqlOutParameter("Count", Types.INTEGER));
        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("UserId", userId);
        Map<String, Object> out = jdbcCall.execute(in);
        return (Integer) out.get("Count");
    }
}
