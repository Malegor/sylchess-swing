package com.sylvain.chess.ui;

import lombok.Getter;

import javax.swing.JButton;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

@Getter
public class SquareButton extends JButton implements ActionListener {
  private static final Color LIGHT_SQUARE_COLOR = Color.LIGHT_GRAY;
  private static final Color DARK_SQUARE_COLOR = Color.DARK_GRAY;
  private static final Color SELECTED_COLOR = Color.BLUE;
  @Serial
  private static final long serialVersionUID = 1L;

  private final int col;
  private final int row;

  public SquareButton(final int col, final int row) {
    super();
    this.col = col;
    this.row = row;
    this.setBackground(this.getDefaultColor());
    this.addActionListener(this);
  }

  private Color getDefaultColor() {
    return (this.row + this.col) % 2 == 0 ? LIGHT_SQUARE_COLOR : DARK_SQUARE_COLOR;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // Example action: change the color of the clicked button
    this.setBackground(this.getBackground().equals(SELECTED_COLOR) ? this.getDefaultColor() : SELECTED_COLOR);
  }
}
