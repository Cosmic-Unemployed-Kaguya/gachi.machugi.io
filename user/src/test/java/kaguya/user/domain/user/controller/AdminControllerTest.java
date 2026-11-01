package kaguya.user.domain.user.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)  // spring security 필터 무시
class AdminControllerTest {


}