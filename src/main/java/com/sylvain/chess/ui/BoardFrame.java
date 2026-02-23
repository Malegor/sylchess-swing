package com.sylvain.chess.ui;

import com.sylvain.chess.board.ChessBoard;
import com.sylvain.chess.board.Square;
import com.sylvain.chess.pieces.PieceOnBoard;
import com.sylvain.chess.play.Gameplay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

public class BoardFrame extends JFrame implements ActionListener {
  private static final Color SELECTED_COLOR = Color.BLUE;
  private static final int DEFAULT_SIZE = 600;

  private final SquareButton[][] squares = new SquareButton[8][8];
  private Gameplay game;

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
    final JTextField moveField = new JTextField(5);
    final JButton submitButton = new JButton("Submit move");
    final Scanner scanner = new Scanner(moveField.getText());
    submitButton.addActionListener(e -> {
      final String value = scanner.next();
      System.out.println("Move entered: " + value);
    });
    movePanel.add(moveField);
    movePanel.add(submitButton);
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
        this.game = new Gameplay(ChessBoard.defaultBoard()); // TODO: generalize
        this.updateBoard();
        //this.game.playGame();
      });
    return newGameButton;
  }

  private void updateBoard() {
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        final SquareButton square = this.squares[row][col];
        square.setBackground(square.getDefaultColor());
        final PieceOnBoard piece = this.game.getBoard().getPieceAt(new Square(col + 1, ChessBoard.BOARD_ROWS - row));
        if (piece != null)
          square.setIcon(piece.getIcon(piece.getColor()));
      }
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final SquareButton clickedButton = (SquareButton) e.getSource();
    // Example action: change the color of the clicked button
    clickedButton.setBackground(clickedButton.getBackground().equals(SELECTED_COLOR) ? clickedButton.getDefaultColor() : SELECTED_COLOR);
  }

  public static void main(String[] args) {
    // Ensure GUI creation happens on the Event Dispatch Thread (EDT)
    SwingUtilities.invokeLater(BoardFrame::new);
  }
}
