package com.sylvain.chess.ui.players;

import com.sylvain.chess.PlayerColor;
import com.sylvain.chess.board.ChessBoard;
import com.sylvain.chess.board.Square;
import com.sylvain.chess.moves.Move;
import com.sylvain.chess.pieces.Bishop;
import com.sylvain.chess.pieces.King;
import com.sylvain.chess.pieces.Knight;
import com.sylvain.chess.pieces.Pawn;
import com.sylvain.chess.pieces.PieceOnBoard;
import com.sylvain.chess.pieces.Queen;
import com.sylvain.chess.pieces.Rook;
import com.sylvain.chess.play.players.interactive.InteractivePlayer;
import com.sylvain.chess.ui.BoardFrame;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class GuiInteractivePlayer extends InteractivePlayer {
  private final BoardFrame frame;
  @Setter
  private String move;
  private Square selectedOrigin;
  private Square selectedDestination;

  public GuiInteractivePlayer(final PlayerColor color, final String name, final ChessBoard board, final BoardFrame frame) {
    super(color, name, board);
    this.frame = frame;
  }

  @Override
  protected String getNextMove() {
    try {
      this.frame.getWaitingForNextMove().await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    this.frame.waitForNextMove();
    this.frame.getWarningsLabel().setText(" ");
    if (this.selectedOrigin == null || this.selectedDestination == null)
      return this.move;
    final ChessBoard board = this.frame.getCurrentBoard();
    final PieceOnBoard pieceAtOrigin = board.getPieceAt(this.selectedOrigin);
    if (pieceAtOrigin == null || !pieceAtOrigin.getColor().equals(this.getColor()))
      return this.getBadMoveStr();
    final PieceOnBoard originalPieceAtDestination = board.getPieceAt(this.selectedDestination);
    final int columnDiff = this.selectedDestination.column() - this.selectedOrigin.column();
    final int rowDiff = this.selectedDestination.row() - this.selectedOrigin.row();
    PieceOnBoard movedPiece = pieceAtOrigin.move(columnDiff, rowDiff);
    if (originalPieceAtDestination != null && originalPieceAtDestination.getColor().equals(this.getColor())) {
      // Castling
      if (originalPieceAtDestination.equals(pieceAtOrigin)) {
        return this.getBadMoveStr();
      }
      final King king = (King) Stream.of(pieceAtOrigin, originalPieceAtDestination).filter(p -> p instanceof King).findFirst().orElse(null);
      final Rook rook = (Rook) Stream.of(pieceAtOrigin, originalPieceAtDestination).filter(p -> p instanceof Rook).findFirst().orElse(null);
      if (king == null || rook == null || king.getSquare().row() != ChessBoard.getFirstRow(this.getColor()) || rook.getSquare().row() != ChessBoard.getFirstRow(this.getColor()))
        return this.getBadMoveStr();
      return ChessBoard.areValidSquaresForCastle(king, rook, true) ? Move.KING_SIDE_CASTLE_PGN : Move.QUEEN_SIDE_CASTLE_PGN;
    }
    if (pieceAtOrigin.getName().equals(Pawn.NAME_LC) && ChessBoard.getPromotionRow(this.getColor()) == this.selectedDestination.row()) {
      // Promotion: for now, read the promotion piece from the move text field (in the future, there could be a popup to select the piece).
      final Pawn pawn = (Pawn) pieceAtOrigin;
      final String pieceStr = this.frame.getMoveField().getText();
      final char pieceChar = pieceStr.isEmpty() ? Queen.NAME_LC : Character.toLowerCase(pieceStr.toCharArray()[0]);
      movedPiece = switch (pieceChar) {
        case Bishop.NAME_LC -> pawn.toBishop(this.selectedDestination);
        case Rook.NAME_LC -> pawn.toRook(this.selectedDestination);
        case Knight.NAME_LC -> pawn.toKnight(this.selectedDestination);
        default -> pawn.toQueen(this.selectedDestination);
      };
    }
    final Move moveToPlay = new Move(Map.of(pieceAtOrigin, movedPiece), this.frame.getCurrentBoard());
    return moveToPlay.isValidMove() ? moveToPlay.toPgn() : this.getBadMoveStr();
  }

  private String getBadMoveStr() {
    return this.selectedOrigin + " -> " + this.selectedDestination;
  }

  @Override
  protected void handleInvalidMove(List<Move> validMoves, String moveStr) {
    super.handleInvalidMove(validMoves, moveStr);
    this.frame.getWarningsLabel().setText("Invalid move: \"" + moveStr + "\"");
    this.move = null;
    this.selectedOrigin = null;
    this.selectedDestination = null;
  }

  @Override
  protected void handleValidMove(final Move move) {
    super.handleValidMove(move);
    this.frame.applyMove(move);
  }

  public void setNextMoveSquares(final Square squareOrigin, final Square squareDestination) {
    this.selectedOrigin = squareOrigin;
    this.selectedDestination = squareDestination;
  }
}
