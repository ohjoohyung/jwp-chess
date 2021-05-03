
package chess.controller.web;

import chess.service.SpringChessService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ChessRestControllerTest {

    private final MockMvc mockMvc;

    private final SpringChessService chessService;

    public ChessRestControllerTest(MockMvc mockMvc, SpringChessService chessService) {
        this.mockMvc = mockMvc;
        this.chessService = chessService;
    }

    @Test
    void startGame() throws Exception {
        int id = chessService.createRoom("test");

        mockMvc.perform(get("/api/rooms/" + id + "/start")
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void restartGame() throws Exception {
        int id = chessService.createRoom("test");

        mockMvc.perform(put("/api/rooms/" + id + "/restart")
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void movePiece() throws Exception {
        int id = chessService.createRoom("test");

        mockMvc.perform(put("/api/rooms/" + id + "/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"command\":\"move a2 a4\"}")
        )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void deleteRoom() throws Exception {
        int id = chessService.createRoom("test");

        mockMvc.perform(delete("/api/rooms/" + id)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk());
    }
}