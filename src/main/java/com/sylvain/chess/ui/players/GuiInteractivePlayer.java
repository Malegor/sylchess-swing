package com.sylvain.chess.ui.players;

import com.sylvain.chess.PlayerColor;
import com.sylvain.chess.board.ChessBoard;
import com.sylvain.chess.moves.Move;
import com.sylvain.chess.play.players.interactive.InteractivePlayer;
import com.sylvain.chess.ui.BoardFrame;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class GuiInteractivePlayer extends InteractivePlayer {
  private final BoardFrame frame;
  @Setter
  private String move;

  public GuiInteractivePlayer(final PlayerColor color, final String name, final ChessBoard board, final BoardFrame frame) {
    super(color, name, board);
    this.frame = frame;
  }

  @Override
  protected String getNextMove() {
    try {
      this.frame.getMoveLatch().await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    this.frame.setMoveLatch(new CountDownLatch(1));
    this.frame.getWarningsLabel().setText(" ");
    return this.move;
  }

  @Override
  protected void handleInvalidMove(List<Move> validMoves, String moveStr) {
    super.handleInvalidMove(validMoves, moveStr);
    this.frame.getWarningsLabel().setText("Invalid move: \"" + moveStr + "\"");
  }

  @Override
  protected void handleValidMove(final Move move) {
    super.handleValidMove(move);
    this.frame.applyMove(move);
  }
}
