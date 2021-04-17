package chess.controller;

import chess.database.dao.RoomDAO;
import chess.domain.board.ChessBoard;
import chess.domain.board.Position;
import chess.domain.feature.Color;
import chess.domain.feature.Type;
import chess.domain.game.ChessGame;
import chess.domain.gamestate.Ready;
import chess.domain.gamestate.Running;
import chess.domain.piece.Piece;
import chess.dto.SparkRoomDTO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class SparkChessController {
    public static final Gson gson = new Gson();

    private final RoomDAO roomDAO = new RoomDAO();
    private ChessGame chessGame;

    public Response createRoom(String roomId) {
        try {
            roomDAO.validateRoomExistence(roomId);
            initializeChessBoard();
            Response response = new Response(chessGame, StatusCode.SUCCESSFUL);
            response.add("name", roomId);
            return response;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            Response response = new Response(StatusCode.CONFLICT);
            response.add("alert", roomId + "는 이미 존재하는 방입니다.");
            return response;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new Response(StatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    public Response movePiece(List<String> input) {
        try {
            chessGame.play(input);
            return new Response(chessGame, StatusCode.SUCCESSFUL);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new Response(StatusCode.BAD_REQUEST);
        }
    }

    private void initializeChessBoard() {
        ChessBoard chessBoard = new ChessBoard();
        chessGame = new ChessGame(chessBoard, Color.WHITE, new Ready());
        chessGame.start(Collections.singletonList("start"));
    }

    public Response saveRoom(String request) {
        try {
            SparkRoomDTO sparkRoomDTO = createRoomToSave(request);
            roomDAO.addRoom(sparkRoomDTO);
            return new Response(StatusCode.SUCCESSFUL);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new Response(StatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    private SparkRoomDTO createRoomToSave(String request) {
        JsonObject roomJson = gson.fromJson(request, JsonObject.class);
        String name = roomJson.get("name").getAsString();
        String turn = roomJson.get("turn").getAsString();
        JsonObject state = roomJson.get("state").getAsJsonObject();
        return new SparkRoomDTO(name, turn, state);
    }

    public Response loadRoom(String request) {
        try {
            JsonObject roomJson = gson.fromJson(request, JsonObject.class);
            String name = roomJson.get("name").getAsString();
            SparkRoomDTO sparkRoomDTO = roomDAO.findByRoomId(name);
            setChessGame(sparkRoomDTO);
            Response response = new Response(chessGame, StatusCode.SUCCESSFUL);
            response.add("name", name);
            return response;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new Response(StatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void setChessGame(SparkRoomDTO sparkRoomDTO) {
        ChessBoard chessBoard = new ChessBoard();
        Color turn = Color.convert(sparkRoomDTO.getTurn());
        JsonObject stateJson = sparkRoomDTO.getState();
        for (String position : stateJson.keySet()) {
            Piece piece = getPiece(stateJson, position);
            chessBoard.replace(Position.of(position), piece);
        }
        chessGame = new ChessGame(chessBoard, turn, new Running());
    }

    private Piece getPiece(JsonObject stateJson, String position) {
        JsonObject pieceJson = gson.fromJson(stateJson.get(position), JsonObject.class);
        String type = pieceJson.get("type").getAsString();
        String color = pieceJson.get("color").getAsString();

        Type pieceType = Type.convert(type);
        return pieceType.createPiece(Position.of(position), Color.convert(color));
    }

    public Response getAllSavedRooms() {
        try {
            Response response = new Response(StatusCode.SUCCESSFUL);
            response.add("rooms", roomDAO.getAllRoom());
            return response;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new Response(StatusCode.BAD_GATEWAY);
        }
    }

    public Response resetGameAsReadyState() {
        try {
            initializeChessBoard();
            return new Response(StatusCode.SUCCESSFUL);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new Response(StatusCode.INTERNAL_SERVER_ERROR);
        }
    }
}
