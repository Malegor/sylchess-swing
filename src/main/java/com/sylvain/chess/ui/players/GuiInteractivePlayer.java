package com.sylvain.chess.ui.players;

import com.sylvain.chess.PlayerColor;
import com.sylvain.chess.board.ChessBoard;
import com.sylvain.chess.board.Square;
import com.sylvain.chess.moves.Move;
import com.sylvain.chess.pieces.King;
import com.sylvain.chess.pieces.PieceOnBoard;
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
    final String to = " -> ";
    if (pieceAtOrigin == null || !pieceAtOrigin.getColor().equals(this.getColor()))
      return this.selectedOrigin + to + this.selectedDestination; // OBS: Raise a warning of invalid move.
    final PieceOnBoard pieceAtDestination = board.getPieceAt(this.selectedDestination);
    if (pieceAtDestination != null && pieceAtDestination.getColor().equals(this.getColor())) {
      // Castling
      if (pieceAtDestination.equals(pieceAtOrigin)) {
        return this.selectedOrigin + to + this.selectedDestination; // OBS: Raise a warning of invalid move.
      }
      final King king = (King) Stream.of(pieceAtOrigin, pieceAtDestination).filter(p -> p instanceof King).findFirst().orElse(null);
      final Rook rook = (Rook) Stream.of(pieceAtOrigin, pieceAtDestination).filter(p -> p instanceof Rook).findFirst().orElse(null);
      if (king == null || rook == null || king.getSquare().row() != ChessBoard.getFirstRow(this.getColor()) || rook.getSquare().row() != ChessBoard.getFirstRow(this.getColor()))
        return this.selectedOrigin + to + this.selectedDestination; // OBS: Raise a warning of invalid move.
      return ChessBoard.areValidSquaresForCastle(king, rook, true) ? Move.KING_SIDE_CASTLE_PGN : Move.QUEEN_SIDE_CASTLE_PGN;
    }
    // TODO: promo
    return new Move(Map.of(pieceAtOrigin, pieceAtOrigin.move(this.selectedDestination.column() - this.selectedOrigin.column(), this.selectedDestination.row() - this.selectedOrigin.row())), this.frame.getCurrentBoard()).toPgn();
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
