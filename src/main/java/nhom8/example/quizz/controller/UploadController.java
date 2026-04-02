package nhom8.example.quizz.controller;

import nhom8.example.quizz.response.ApiResponse;
import nhom8.example.quizz.exception.ApiException;
import nhom8.example.quizz.security.AuthContextService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final AuthContextService authContextService;

    public UploadController(AuthContextService authContextService) {
        this.authContextService = authContextService;
    }

    @PostMapping(value = "/exams/{exam_id}/upload-file", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadExamFile(
            @PathVariable("exam_id") Integer examId,
            @RequestParam("file") MultipartFile file
    ) {
        authContextService.requireAdmin();
        String url = saveFile(file, "uploads/exams/" + examId);

        Map<String, String> responseData = new HashMap<>();
        responseData.put("file_url", url);

        return ResponseEntity.ok(ApiResponse.success("Upload thành công", responseData));
    }

    @PostMapping(value = "/questions/{question_id}/upload-image", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadQuestionImage(
            @PathVariable("question_id") Integer questionId,
            @RequestParam("image") MultipartFile image
    ) {
        authContextService.requireAdmin();
        String url = saveFile(image, "uploads/questions/" + questionId);

        Map<String, String> responseData = new HashMap<>();
        responseData.put("image_url", url);

        return ResponseEntity.ok(ApiResponse.success("Upload thành công", responseData));
    }

    private String saveFile(MultipartFile mf, String folder) {
        if (mf == null || mf.isEmpty()) {
            throw new ApiException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR",
                    "File upload không hợp lệ",
                    null
            );
        }

        try {
            Path dir = Paths.get(folder);
            Files.createDirectories(dir);
            String original = mf.getOriginalFilename() != null ? mf.getOriginalFilename() : "file";
            String ext = "";
            int idx = original.lastIndexOf('.');
            if (idx >= 0) ext = original.substring(idx);
            String filename = UUID.randomUUID() + ext;
            Path target = dir.resolve(filename);
            Files.copy(mf.getInputStream(), target);
            return "/" + folder.replace("\\", "/") + "/" + filename;
        } catch (IOException e) {
            throw new ApiException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "INTERNAL_ERROR",
                    "Không lưu được file",
                    null
            );
        }
    }
}