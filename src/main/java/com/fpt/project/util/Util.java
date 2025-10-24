package com.fpt.project.util;

import com.fpt.project.exception.ApiException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public class Util {
    public static String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads";
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

    public static String uploadImage(MultipartFile file) throws IOException {
        java.nio.file.Files.createDirectories(Paths.get(UPLOAD_DIR));
        String extension = "";
        String fileName = file.getOriginalFilename();
        if (fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf("."));
        }
        String newName = UUID.randomUUID().toString() + extension;
        Path filePath = Paths.get(UPLOAD_DIR, newName);
        // Lưu file vào thư mục "uploads"
        file.transferTo(filePath.toFile());  // Chuyển file vào thư mục
        return "/uploads/" + newName;
    }
}
