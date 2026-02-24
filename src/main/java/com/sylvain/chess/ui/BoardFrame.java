package com.sylvain.chess.ui;

import com.sylvain.chess.PlayerColor;
import com.sylvain.chess.board.ChessBoard;
import com.sylvain.chess.board.Square;
import com.sylvain.chess.pieces.PieceOnBoard;
import com.sylvain.chess.play.GameStatus;
import com.sylvain.chess.play.Gameplay;
import com.sylvain.chess.play.players.interactive.GuiPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class BoardFrame extends JFrame implements ActionListener {
  private static final Color SELECTED_COLOR = Color.BLUE;
  private static final int DEFAULT_SIZE = 600;

  private final SquareButton[][] squares = new SquareButton[8][8];
  private Gameplay game;
  private final JTextField moveField;
  private final JButton submitButton;

  public BoardFrame() {
    // 1. Set up the JFrame
    this.setTitle("Sylchess Board");
    this.setSize(2 * DEFAULT_SIZE, DEFAULT_SIZE);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setResizable(true);
    // Set the layout manager for the frame's content pane : GridLayout(1, 2) specifies 1 row and 2 columns
    this.getContentPane().setLayout(new GridLayout(1, 2));

    // 2. Use JPanel with GridLayout
    final JPanel boardPanel = new JPanel();
    boardPanel.setLayout(new GridLayout(ChessBoard.BOARD_ROWS, ChessBoard.BOARD_COLS));
    this.add(boardPanel); // Add the panel to the frame's content pane
    final JPanel interactivePanel = new JPanel();
    interactivePanel.setLayout(new BorderLayout());
    final JPanel genericPanel = new JPanel();
    genericPanel.add(this.getNewGameButton());
    final JPanel movePanel = new JPanel(new FlowLayout());
    this.moveField = new JTextField(5);
    this.submitButton = new JButton("Submit move");
    this.submitButton.addActionListener(e -> this.updateBoard());
    movePanel.add(this.moveField);
    movePanel.add(this.submitButton);
    final JPanel infoPanel = new JPanel();
    interactivePanel.add(genericPanel, BorderLayout.NORTH);
    interactivePanel.add(movePanel, BorderLayout.CENTER);
    interactivePanel.add(infoPanel, BorderLayout.SOUTH);
    this.add(interactivePanel);

    // 3. Create and add JButtons in nested loops
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        final SquareButton square = new SquareButton(row, col);
        square.setBackground(square.getDefaultColor());
        square.addActionListener(this);
        this.squares[row][col] = square;
        // Optional: Store location data in the button for later reference
        // square.putClientProperty("location", new Point(row, col));
        boardPanel.add(square);
      }
    }
    this.setVisible(true);
  }

  private JButton getNewGameButton() {
    final JButton newGameButton = new JButton();
    newGameButton.setText("New Game");
    newGameButton.addActionListener(
      e -> {
        final ChessBoard board = ChessBoard.defaultBoard();
        this.game = new Gameplay(board); // TODO: generalize
        this.updateBoard();
        new SwingWorker<Void, Void>() {
          @Override
          protected Void doInBackground() {
            final GameStatus result = game.playGame(List.of(new GuiPlayer(PlayerColor.WHITE, "white", board, moveField, submitButton),
                    new GuiPlayer(PlayerColor.BLACK, "black", board, moveField, submitButton)));
            return null;
          }
          @Override
          protected void done() {
            System.out.println("Done!");
          }
        }.execute();
      });
    return newGameButton;
  }

  private void updateBoard() {
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        final SquareButton square = this.squares[row][col];
        square.setBackground(square.getDefaultColor());
        final PieceOnBoard piece = this.game.getBoard().getPieceAt(new Square(col + 1, ChessBoard.BOARD_ROWS - row));
        square.setIcon(piece == null ? null : piece.getIcon(piece.getColor()));
      }
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final SquareButton clickedButton = (SquareButton) e.getSource();
    // Example action: change the color of the clicked button
    clickedButton.setBackground(clickedButton.getBackground().equals(SELECTED_COLOR) ? clickedButton.getDefaultColor() : SELECTED_COLOR);
    this.updateBoard();
  }

  public static void main(String[] args) {
    // Ensure GUI creation happens on the Event Dispatch Thread (EDT)
    SwingUtilities.invokeLater(BoardFrame::new);
  }
}
