package com.sylvain.chess.ui;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.JButton;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serial;

@Log4j2
@Getter
public class SquareButton extends JButton implements MouseListener {
  private static final Color LIGHT_SQUARE_COLOR = Color.LIGHT_GRAY;
  private static final Color DARK_SQUARE_COLOR = Color.DARK_GRAY;
  private static final Color SELECTED_COLOR = Color.BLUE;
  private static final Color SELECTED_MOVE_COLOR = Color.RED;
  @Serial
  private static final long serialVersionUID = 1L;

  private final int col;
  private final int row;
  private final BoardFrame frame;

  public SquareButton(final int col, final int row, final BoardFrame frame) {
    super();
    this.col = col;
    this.row = row;
    this.frame = frame;
    this.setBackground(this.getDefaultColor());
    this.addMouseListener(this);
  }

  private Color getDefaultColor() {
    return (this.row + this.col) % 2 == 0 ? LIGHT_SQUARE_COLOR : DARK_SQUARE_COLOR;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (e.getButton() == MouseEvent.BUTTON1) { // Left click

      // TODO: move
    } else if (e.getButton() == MouseEvent.BUTTON3) { // Right click
      this.setBackground(this.getBackground().equals(SELECTED_COLOR) ? this.getDefaultColor() : SELECTED_COLOR);
    } else if (e.getButton() == MouseEvent.BUTTON2) { // Middle click
      log.info("Middle click detected!");
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }
}
