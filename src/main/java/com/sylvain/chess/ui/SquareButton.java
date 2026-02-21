package com.sylvain.chess.ui;

import lombok.Getter;

import javax.swing.JButton;
import java.awt.*;
import java.io.Serial;

@Getter
public class SquareButton extends JButton {
  private static final Color LIGHT_SQUARE_COLOR = Color.LIGHT_GRAY;
  private static final Color DARK_SQUARE_COLOR = Color.DARK_GRAY;
  @Serial
  private static final long serialVersionUID = 1L;

  private final int col;
  private final int row;

  public SquareButton(final int col, final int row) {
    super();
    this.col = col;
    this.row = row;
  }

  public Color getDefaultColor() {
    return (this.row + this.col) % 2 == 0 ? LIGHT_SQUARE_COLOR : DARK_SQUARE_COLOR;
  }
}
