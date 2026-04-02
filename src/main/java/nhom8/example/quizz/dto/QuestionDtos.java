package nhom8.example.quizz.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public class QuestionDtos {

    public record QuestionDto(
            Integer QuestionId,
            String questionText,
            @JsonProperty("option_a") String optionA,
            @JsonProperty("option_b") String optionB,
            @JsonProperty("option_c") String optionC,
            @JsonProperty("option_d") String optionD
    ){}


}
