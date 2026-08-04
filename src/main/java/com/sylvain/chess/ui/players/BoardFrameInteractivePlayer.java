package com.sylvain.chess.ui.players;

import com.sylvain.chess.PlayerColor;
import com.sylvain.chess.board.ChessBoard;
import com.sylvain.chess.moves.Move;
import com.sylvain.chess.play.players.interactive.GuiInteractivePlayer;
import com.sylvain.chess.ui.BoardFrame;

import java.util.List;

public class BoardFrameInteractivePlayer extends GuiInteractivePlayer {
  private final BoardFrame frame;

  public BoardFrameInteractivePlayer(final PlayerColor color, final String name, final ChessBoard board, final BoardFrame frame) {
    super(color, name, board, frame.getWaitingForNextMove());
    this.frame = frame;
  }

  @Override
  protected void handleInvalidMove(List<Move> validMoves, String moveStr) {
    super.handleInvalidMove(validMoves, moveStr);
    this.frame.getWarningsLabel().setText("Invalid move: \"" + moveStr + "\"");
  }

  @Override
  public void publishMove(final Move move) {
    super.publishMove(move);
    this.frame.applyMove(move);
  }

  @Override
  protected void resetWarnings() {
    this.frame.getWarningsLabel().setText(" ");
  }

  @Override
  protected ChessBoard getBoardToUse() {
    return this.frame.getInternalBoard();
  }

  @Override
  protected String getPromotion() {
    return this.frame.getMoveField().getText();
  }

  @Override
  protected void resetWaitForNextMove() {
    super.resetWaitForNextMove();
    this.frame.setWaitingForNextMove(this.getWaitingForNextMove());
  }
}
