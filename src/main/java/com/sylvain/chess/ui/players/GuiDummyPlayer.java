package com.sylvain.chess.ui.players;

import com.sylvain.chess.PlayerColor;
import com.sylvain.chess.board.ChessBoard;
import com.sylvain.chess.moves.Move;
import com.sylvain.chess.play.players.DummyPlayer;
import com.sylvain.chess.ui.BoardFrame;

public class GuiDummyPlayer extends DummyPlayer {
  private final BoardFrame frame;

  public GuiDummyPlayer(final PlayerColor color, final ChessBoard board, final BoardFrame frame) {
    super(color, board);
    this.frame = frame;
  }

  @Override
  public void publishMove(Move move) {
    super.publishMove(move);
    this.frame.applyMove(move);
  }
}
