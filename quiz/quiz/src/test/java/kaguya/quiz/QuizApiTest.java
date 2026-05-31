package kaguya.quiz;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import kaguya.domain.QuizApplication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(classes = QuizApplication.class)
class QuizApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("퀴즈 생성 테스트")
    void createQuiz() throws Exception {
        String body = """
                {
                  "title": "라면 맞추기",
                  "description": "사진 보고 라면 맞추기 퀴즈",
                  "thumbnail": "https://example.com/ramen.jpg",
                  "category": "음식",
                  "creatorId": 1
                }
                """;

        mockMvc.perform(post("/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("라면 맞추기"))
                .andExpect(jsonPath("$.category").value("음식"));
    }

    @Test
    @DisplayName("문제 생성 테스트")
    void createQuestion() throws Exception {
        String body = """
                {
                  "problemText": "이 라면 이름은?",
                  "problemUrl": "https://example.com/ramen.jpg",
                  "type": "IMAGE",
                  "sortOrder": 1
                }
                """;

        mockMvc.perform(post("/quizzes/1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.problemText").value("이 라면 이름은?"))
                .andExpect(jsonPath("$.type").value("IMAGE"));
    }

    @Test
    @DisplayName("정답 생성 테스트")
    void createAnswer() throws Exception {
        String body = """
                {
                  "answer": "신라면"
                }
                """;

        mockMvc.perform(post("/questions/1/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.answer").value("신라면"));
    }
}