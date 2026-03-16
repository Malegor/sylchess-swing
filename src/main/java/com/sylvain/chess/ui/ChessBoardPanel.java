package com.sylvain.chess.ui;

import com.sylvain.chess.board.ChessBoard;
import com.sylvain.chess.board.Square;
import com.sylvain.chess.moves.Move;
import com.sylvain.chess.pieces.PieceOnBoard;
import com.sylvain.chess.ui.players.GuiInteractivePlayer;
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

  public void updatePiecesAfterLastMove(final ChessBoard board) {
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        final SquareButton squareButton = this.squares[row][col];
        final PieceOnBoard piece = board == null ? null : board.getPieceAt(getSquareFromSquareButton(squareButton));
        squareButton.setIcon(piece == null ? null : piece.getIcon(piece.getColor()));
      }
    }
    this.resetAllPaintedSquares(Set.of(SquareButton.LAST_MOVE_ORIGIN_COLOR, SquareButton.LAST_MOVE_DESTINATION_COLOR));
  }

  private void resetSelectedColors() {
    if (this.selectedOrigin != null)
      this.selectedOrigin.setBackground(SquareButton.MOVE_ORIGIN_COLOR);
    if (this.selectedDestination != null)
      this.selectedDestination.setBackground(SquareButton.MOVE_DESTINATION_COLOR);
  }

  private void resetLastMoveColors() {
    if (this.lastMoveOrigin != null)
      this.lastMoveOrigin.setBackground(SquareButton.LAST_MOVE_ORIGIN_COLOR);
    if (this.lastMoveDestination != null)
      this.lastMoveDestination.setBackground(SquareButton.LAST_MOVE_DESTINATION_COLOR);
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
    this.resetLastMoveColors();
    this.resetSelectedColors();
  }

  public void prepareMove(final SquareButton selectedSquareButton) {
    final GuiInteractivePlayer playerToMove = this.frame.getNextInteractivePlayerToMove();
    if (playerToMove == null)
      return;
    if (selectedSquareButton == null) {
      this.resetSelectedMove();
      this.frame.waitForNextMove();
    }
    else if (this.selectedOrigin == null || this.selectedDestination != null || selectedSquareButton.equals(this.selectedOrigin)) {
      // OBS: a piece must be in this square.
      this.selectedDestination = null;
      final PieceOnBoard piece = this.frame.getCurrentBoard().getPieceAt(getSquareFromSquareButton(selectedSquareButton));
      if (piece != null && piece.getColor().equals(playerToMove.getColor())) {
        this.selectedOrigin = selectedSquareButton;
      }
    }
    else {
      // TODO: check controlled squares ignoring other pieces
      this.selectedDestination = selectedSquareButton;
      playerToMove.setNextMoveSquares(this.getSquareFromSquareButton(this.selectedOrigin), this.getSquareFromSquareButton(this.selectedDestination));
      this.frame.publishNextMove();
    }
    this.resetAllPaintedSquares(Set.of(SquareButton.MOVE_ORIGIN_COLOR, SquareButton.MOVE_DESTINATION_COLOR, SquareButton.SELECTED_PIECE_COLOR));
  }

  public void setLastMove(final Move move) {
    final List<PieceOnBoard> descriptivePieces = move.getDescriptivePieces();
    this.lastMoveOrigin = this.getSquareButtonFromSquare(descriptivePieces.get(0).getSquare());
    this.lastMoveDestination = this.getSquareButtonFromSquare(descriptivePieces.get(1).getSquare());
  }

  public void resetLastMove() {
    this.lastMoveOrigin = null;
    this.lastMoveDestination = null;
  }

  public void resetSelectedMove() {
    if (this.selectedOrigin != null)
      this.selectedOrigin.resetDefaultBackground();
    this.selectedOrigin = null;
    if (this.selectedDestination != null)
      this.selectedDestination.resetDefaultBackground();
    this.selectedDestination = null;
  }
}
