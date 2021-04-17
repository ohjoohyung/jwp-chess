package chess.service;

import chess.dto.ChessGameDTO;
import chess.dto.ResultDTO;
import chess.dto.RoomDTO;
import chess.database.dao.SpringRoomDAO;
import chess.domain.board.ChessBoard;
import chess.domain.board.Position;
import chess.domain.feature.Color;
import chess.domain.feature.Type;
import chess.domain.game.ChessGame;
import chess.domain.gamestate.Ready;
import chess.domain.gamestate.Running;
import chess.domain.piece.Piece;
import chess.dto.SaveRoomDTO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SpringChessService {
    public static final Gson gson = new Gson();
    private final SpringRoomDAO roomDAO;

    public SpringChessService(SpringRoomDAO roomDAO) {
        this.roomDAO = roomDAO;
    }

    public Optional<Integer> createRoom(String name) {
        try {
            ChessGame chessGame = initializeChessBoard();
            chessGame.start(Collections.singletonList("start"));
            roomDAO.addRoom(new SaveRoomDTO(name, chessGame));
            return Optional.of(roomDAO.getLastInsertId());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ChessGameDTO> movePiece(String param) {
        try {
            JsonObject paramJson = gson.fromJson(param, JsonObject.class);
            int roomNo = paramJson.get("roomNo").getAsInt();
            RoomDTO roomDTO = roomDAO.findRoomByRoomNo(roomNo);
            List<String> input = Arrays.asList(paramJson.get("command").getAsString().split(" "));
            ChessGame chessGame = setChessGame(roomDTO);
            chessGame.play(input);
            roomDAO.updateRoom(new RoomDTO(roomNo, roomDTO.getRoomName(), chessGame));
            return Optional.of(new ChessGameDTO(roomNo, roomDTO.getRoomName(), chessGame));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Optional.empty();
        }
    }

    private ChessGame initializeChessBoard() {
        ChessBoard chessBoard = new ChessBoard();
        return new ChessGame(chessBoard, Color.WHITE, new Ready());
    }

    public Optional<ChessGameDTO> loadRoom(int roomNo) {
        try {
            RoomDTO roomDTO = roomDAO.findRoomByRoomNo(roomNo);
            ChessGame chessGame = setChessGame(roomDTO);
            return Optional.of(new ChessGameDTO(roomNo, roomDTO.getRoomName(), chessGame));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Optional.empty();
        }
    }

    private ChessGame setChessGame(RoomDTO roomDTO) {
        ChessBoard chessBoard = new ChessBoard();
        Color turn = Color.convert(roomDTO.getTurn());
        String[] splitBoard = roomDTO.getBoard().split(",");
        for (String board : splitBoard) {
            String[] pieceInfo = board.split(" ");
            Piece piece = getPiece(pieceInfo);
            chessBoard.replace(Position.of(pieceInfo[0]), piece);
        }
        return new ChessGame(chessBoard, turn, new Running());
    }

    private Piece getPiece(String[] pieceInfo) {
        String type = pieceInfo[1];
        String color = pieceInfo[2];

        Type pieceType = Type.convert(type);
        return pieceType.createPiece(Position.of(pieceInfo[0]), Color.convert(color));
    }

    public List<RoomDTO> getAllSavedRooms() {
        try {
            return roomDAO.getAllRoom();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public boolean isGameEnd(String param){
        try {
            JsonObject paramJson = gson.fromJson(param, JsonObject.class);
            int roomNo = paramJson.get("roomNo").getAsInt();
            RoomDTO roomDTO = roomDAO.findRoomByRoomNo(roomNo);
            ChessGame chessGame = setChessGame(roomDTO);
            return !chessGame.isOngoing();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public void deleteGame(String param) {
        try {
            JsonObject paramJson = gson.fromJson(param, JsonObject.class);
            int roomNo = paramJson.get("roomNo").getAsInt();
            roomDAO.deleteRoomByRoomNo(roomNo);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Optional<ResultDTO> getResult(String param) {
        try {
            JsonObject paramJson = gson.fromJson(param, JsonObject.class);
            int roomNo = paramJson.get("roomNo").getAsInt();
            RoomDTO roomDTO = roomDAO.findRoomByRoomNo(roomNo);
            ChessGame chessGame = setChessGame(roomDTO);
            return Optional.of(new ResultDTO(chessGame.result()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Optional.empty();
        }
    }
}
