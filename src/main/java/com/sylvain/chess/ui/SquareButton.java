package com.sylvain.chess.ui;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.JButton;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serial;
import java.util.Set;

@Log4j2
@Getter
public class SquareButton extends JButton implements MouseListener {
  // Colors still to use: PINK, BLACK, GRAY, WHITE
  private static final Color LIGHT_SQUARE_COLOR = Color.LIGHT_GRAY;
  private static final Color DARK_SQUARE_COLOR = Color.DARK_GRAY;
  public static final Color PAINT_COLOR = Color.GREEN;
  public static final Color SELECTED_PIECE_COLOR = Color.MAGENTA;
  public static final Color MOVE_ORIGIN_COLOR = Color.CYAN;
  public static final Color MOVE_DESTINATION_COLOR = Color.BLUE;
  public static final Color LAST_MOVE_ORIGIN_COLOR = Color.ORANGE;
  public static final Color LAST_MOVE_DESTINATION_COLOR = Color.YELLOW;
  public static final Color INVALID_MOVE_ATTEMPT_COLOR = Color.RED;
  @Serial
  private static final long serialVersionUID = 1L;

  private final int col;
  private final int row;
  private final ChessBoardPanel boardPanel;

  public SquareButton(final int col, final int row, ChessBoardPanel chessBoardPanel) {
    super();
    this.col = col;
    this.row = row;
    this.boardPanel = chessBoardPanel;
    this.setBackground(this.getDefaultColor());
    this.addMouseListener(this);
  }

  public Color getDefaultColor() {
    return (this.row + this.col) % 2 == 0 ? LIGHT_SQUARE_COLOR : DARK_SQUARE_COLOR;
  }

  public void resetDefaultBackground() {
    this.setBackground(this.getDefaultColor());
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (e.getButton() == MouseEvent.BUTTON1) { // Left click
      this.setBackground(this.getBackground().equals(MOVE_ORIGIN_COLOR) ? this.getDefaultColor() : MOVE_ORIGIN_COLOR);
      this.boardPanel.prepareMove(e.getClickCount() == 2 ? null : this);
      // TODO: move
    } else if (e.getButton() == MouseEvent.BUTTON3) { // Right click
      if (e.getClickCount() == 2) {
        this.boardPanel.resetAllPaintedSquares(Set.of(PAINT_COLOR));
      }
      else {
        this.setBackground(this.getBackground().equals(PAINT_COLOR) ? this.getDefaultColor() : PAINT_COLOR);
      }
    } else if (e.getButton() == MouseEvent.BUTTON2) { // Middle click
      log.info("Middle click detected! For now no action.");
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
