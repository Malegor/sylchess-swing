package com.sylvain.chess.ui;

import com.sylvain.chess.board.ChessBoard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class BoardFrame extends JFrame implements ActionListener {
  private static final Color SELECTED_COLOR = Color.BLUE;
  private static final int DEFAULT_SIZE = 600;

  private final JButton[][] squares = new JButton[8][8];
  private final JPanel boardPanel = new JPanel();
  private final JPanel infoPanel = new JPanel();

  public BoardFrame() {
    // 1. Set up the JFrame
    this.setTitle("Sylchess Board");
    this.setSize(2 * DEFAULT_SIZE, DEFAULT_SIZE);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setResizable(true);
    // Set the layout manager for the frame's content pane : GridLayout(1, 2) specifies 1 row and 2 columns
    this.getContentPane().setLayout(new GridLayout(1, 2));

    // 2. Use JPanel with GridLayout
    this.boardPanel.setLayout(new GridLayout(ChessBoard.BOARD_ROWS, ChessBoard.BOARD_COLS));
    this.add(this.boardPanel); // Add the panel to the frame's content pane
    final JButton newGameButton = new JButton();
    newGameButton.setText("New Game");
    newGameButton.addActionListener(e -> System.out.println("Listener: Button was clicked!"));
    this.infoPanel.add(newGameButton);
    this.add(this.infoPanel);

    // 3. Create and add JButtons in nested loops
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        final SquareButton square = new SquareButton(row, col);
        square.setBackground(square.getDefaultColor());
        square.addActionListener(this);
        this.squares[row][col] = square;
        // Optional: Store location data in the button for later reference
        // square.putClientProperty("location", new Point(row, col));
        this.boardPanel.add(square);
      }
    }

    // 4. Example of piece
    this.squares[0][0].setIcon(new javax.swing.ImageIcon(Objects.requireNonNull(getClass().getResource("/pieces_png/Chess_rdt60.png"))));
    this.setVisible(true);
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
