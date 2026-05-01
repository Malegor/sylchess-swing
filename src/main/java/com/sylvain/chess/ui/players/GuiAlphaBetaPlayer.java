package com.sylvain.chess.ui.players;

import com.sylvain.chess.PlayerColor;
import com.sylvain.chess.board.ChessBoard;
import com.sylvain.chess.moves.Move;
import com.sylvain.chess.play.players.AlphaBetaPlayer;
import com.sylvain.chess.ui.BoardFrame;

import java.util.List;

public class GuiAlphaBetaPlayer extends AlphaBetaPlayer {
  private final BoardFrame frame;

  public GuiAlphaBetaPlayer(final PlayerColor color, final ChessBoard board, final int maxNumberOfSemiMoves, final BoardFrame frame) {
    super(color, board, maxNumberOfSemiMoves);
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
      Thread.sleep(BoardFrame.DELAY_TO_REPAINT_BOARD_MS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return super.selectMove(validMoves);
  }
}