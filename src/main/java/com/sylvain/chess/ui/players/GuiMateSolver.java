package com.sylvain.chess.ui.players;

import com.sylvain.chess.PlayerColor;
import com.sylvain.chess.board.ChessBoard;
import com.sylvain.chess.moves.Move;
import com.sylvain.chess.play.players.MateSolver;
import com.sylvain.chess.ui.BoardFrame;

import java.util.List;

public class GuiMateSolver extends MateSolver {
  private final BoardFrame frame;

  public GuiMateSolver(final PlayerColor color, final ChessBoard board, final int maxNumberOfMoves, final BoardFrame frame) {
    super(color, board, maxNumberOfMoves);
    this.frame = frame;
  }

  @Override
  public void publishMove(Move move) {
    super.publishMove(move);
    this.frame.applyMove(move);
  }

  @Override
  protected Move selectMove(final List<Move> validMoves) {
    // TODO: improve, this line is to avoid displaying the board for the previous move, when the current calculation already started
    // Another (better) way to solve the problem would be to run the calculations on a copy of the board and not the original one.
    try {
      Thread.sleep(BoardFrame.DELAY_TO_REPAINT_BOARD);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return super.selectMove(validMoves);
  }
}