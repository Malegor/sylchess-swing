package com.sylvain.chess.ui;

import com.sylvain.chess.PlayerColor;
import com.sylvain.chess.board.ChessBoard;
import com.sylvain.chess.play.players.interactive.InteractivePlayer;
import lombok.Setter;
import java.util.concurrent.CountDownLatch;

public class GuiPlayer extends InteractivePlayer {
  private final BoardFrame frame;
  @Setter
  private String move;

  public GuiPlayer(final PlayerColor color, final String name, final ChessBoard board, final BoardFrame frame) {
    super(color, name, board);
    this.frame = frame;
  }

  @Override
  protected String getNextMove() {
    try {
      this.frame.getLatch().await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    this.frame.setLatch(new CountDownLatch(1));
    return this.move;
  }
}
