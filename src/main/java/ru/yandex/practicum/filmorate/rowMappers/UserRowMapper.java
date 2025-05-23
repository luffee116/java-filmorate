package ru.yandex.practicum.filmorate.rowMappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.UserDto;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserRowMapper implements RowMapper<UserDto> {

    @Override
    public UserDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return UserDto.builder()
                .id(rs.getInt("id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday") != null ? rs.getDate("birthday").toLocalDate() : null)
                .build();
    }
}
