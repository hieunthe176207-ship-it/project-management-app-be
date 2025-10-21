package com.fpt.project.util;

import com.fpt.project.exception.ApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Util {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * Chuyển String -> LocalDate theo định dạng yyyy/MM/dd
     * Nếu sai format sẽ ném IllegalArgumentException
     */
    public static LocalDate parseToLocalDate(String dateString) throws ApiException {
        try {
            return LocalDate.parse(dateString, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ApiException(
                    400,
                    "Ngày tháng không hợp lệ, vui lòng sử dụng định dạng yyyy/MM/dd"
            );
        }
    }
}
