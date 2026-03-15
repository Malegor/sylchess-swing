package com.sylvain.chess.ui;

import com.sylvain.chess.board.ChessBoard;
import com.sylvain.chess.board.Square;
import com.sylvain.chess.moves.Move;
import com.sylvain.chess.pieces.PieceOnBoard;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;

@Log4j2
public class ChessBoardPanel extends JPanel {

  private final SquareButton[][] squares;

  private final BoardFrame frame;

  @Getter
  private SquareButton selectedOrigin;
  @Getter
  private SquareButton selectedDestination;

  private SquareButton lastMoveOrigin;
  private SquareButton lastMoveDestination;

  public ChessBoardPanel(final BoardFrame frame) {
    super();
    this.frame = frame;
    this.squares = new SquareButton[8][8];
    this.setLayout(new GridLayout(ChessBoard.BOARD_ROWS, ChessBoard.BOARD_COLS));
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        final SquareButton square = new SquareButton(col, row, this);
        this.squares[row][col] = square;
        // Optional: Store location data in the button for later reference
        // square.putClientProperty("location", new Point(row, col));
        this.add(square);
      }
    }
  }

  public void updatePiecesOnBoard(final ChessBoard board) {
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        final SquareButton squareButton = this.squares[row][col];
        final PieceOnBoard piece = board == null ? null : board.getPieceAt(getSquareFromSquareButton(squareButton));
        squareButton.setIcon(piece == null ? null : piece.getIcon(piece.getColor()));
      }
    }
  }

  private Square getSquareFromSquareButton(final SquareButton squareButton) {
    return new Square(squareButton.getCol() + 1, ChessBoard.BOARD_ROWS - squareButton.getRow());
  }

  private SquareButton getSquareButtonFromSquare(final Square square) {
    return this.squares[ChessBoard.BOARD_ROWS - square.row()][square.column() - 1];
  }

  public void resetAllPaintedSquares(final Set<Color> colorsToRemove) {
    // TODO: priority of prepared move over only selected square: reset should restore prepared move.
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        final SquareButton square = this.squares[row][col];
        if (colorsToRemove.isEmpty() || colorsToRemove.contains(square.getBackground())) {
          square.resetDefaultBackground();
        }
      }
    }
  }

  public void prepareMove(final SquareButton selectedSquareButton) {
    if (selectedSquareButton == null) {
      this.selectedOrigin = null;
      this.selectedDestination = null;
      this.resetAllPaintedSquares(Set.of(SquareButton.MOVE_ORIGIN_COLOR, SquareButton.MOVE_DESTINATION_COLOR, SquareButton.SELECTED_PIECE_COLOR));
    }
    else if (this.selectedOrigin == null) { // TODO: a piece must be in this square
      this.selectedOrigin = selectedSquareButton;
      selectedSquareButton.setBackground(SquareButton.MOVE_ORIGIN_COLOR);
    }
    else if (this.selectedDestination == null) {
      this.selectedDestination = selectedSquareButton;
      selectedSquareButton.setBackground(SquareButton.MOVE_DESTINATION_COLOR);
    }
    else { // TODO: premoves (>1)
      this.selectedOrigin = null;
      this.selectedDestination = null;
      this.resetAllPaintedSquares(Set.of(SquareButton.MOVE_ORIGIN_COLOR, SquareButton.MOVE_DESTINATION_COLOR, SquareButton.SELECTED_PIECE_COLOR));
    }
  }

  public void setLastMove(final Move move) {
    final List<PieceOnBoard> descriptivePieces = move.getDescriptivePieces();
    this.lastMoveOrigin = this.getSquareButtonFromSquare(descriptivePieces.get(0).getSquare());
    this.lastMoveDestination = this.getSquareButtonFromSquare(descriptivePieces.get(1).getSquare());
  }
}
