package com.sylvain.chess.ui;

import com.sylvain.chess.PlayerColor;
import com.sylvain.chess.board.ChessBoard;
import com.sylvain.chess.board.Square;
import com.sylvain.chess.pieces.PieceOnBoard;
import com.sylvain.chess.play.GameStatus;
import com.sylvain.chess.play.Gameplay;
import com.sylvain.chess.play.players.Player;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class BoardFrame extends JFrame {
  private static final Color SELECTED_COLOR = Color.BLUE;
  private static final int DEFAULT_SIZE = 600;

  private final SquareButton[][] squares;
  private final JTextField moveField;
  @Getter
  private final JLabel warningsLabel;
  private Gameplay game;
  private List<Player> players;
  @Getter @Setter
  private CountDownLatch moveLatch;

  public BoardFrame() {
    // 1. Set up the JFrame
    this.setTitle("Sylchess Board");
    this.setSize(2 * DEFAULT_SIZE, DEFAULT_SIZE);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setResizable(false);
    this.getContentPane().setLayout(new GridLayout(1, 2));
    this.warningsLabel = new JLabel();
    this.moveField = new JTextField(5);
    this.players = new ArrayList<>(2);
    this.moveLatch = new CountDownLatch(1); // OBS: unnecessary?
    this.squares = new SquareButton[8][8];
    // 2. Use JPanel with GridLayout
    this.add(this.getBoardPanel());
    this.add(this.getInteractivePanel());
    this.setVisible(true);
  }

  private JPanel getInteractivePanel() {
    final JPanel interactivePanel = new JPanel();
    interactivePanel.setLayout(new BorderLayout());
    final JPanel genericPanel = this.getGenericPanel();
    final JPanel movePanel = this.getMovePanel();
    final JPanel infoPanel = this.getInfoPanel();
    interactivePanel.add(genericPanel, BorderLayout.NORTH);
    interactivePanel.add(movePanel, BorderLayout.CENTER);
    interactivePanel.add(infoPanel, BorderLayout.SOUTH);
    return interactivePanel;
  }

  private JPanel getBoardPanel() {
    final JPanel boardPanel = new JPanel();
    boardPanel.setLayout(new GridLayout(ChessBoard.BOARD_ROWS, ChessBoard.BOARD_COLS));
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        final SquareButton square = new SquareButton(row, col);
        square.setBackground(square.getDefaultColor());
        square.addActionListener(e -> {
          this.updatePiecesOnBoard();
          final SquareButton clickedButton = (SquareButton) e.getSource();
          // Example action: change the color of the clicked button
          clickedButton.setBackground(clickedButton.getBackground().equals(SELECTED_COLOR) ? clickedButton.getDefaultColor() : SELECTED_COLOR);
        });
        this.squares[row][col] = square;
        // Optional: Store location data in the button for later reference
        // square.putClientProperty("location", new Point(row, col));
        boardPanel.add(square);
      }
    }
    return boardPanel;
  }

  private JPanel getInfoPanel() {
    final JPanel infoPanel = new JPanel();
    infoPanel.setLayout(new FlowLayout());
    this.warningsLabel.setForeground(Color.RED);
    infoPanel.add(this.warningsLabel, BorderLayout.NORTH);
    return infoPanel;
  }

  private JPanel getMovePanel() {
    final JPanel movePanel = new JPanel(new FlowLayout());
    final JButton submitButton = new JButton("Submit move");
    submitButton.addActionListener(e -> {
      final String move = this.moveField.getText();
      for (final Player player : this.players) {
        if (player instanceof GuiPlayer guiPlayer) {
          // OBS: in case of two GUI players, both of them will have this move set, even if only one of them will actually play it.
          // This could be improved by keeping track of which player has the next move.
          guiPlayer.setMove(move);
        }
      }
      this.moveLatch.countDown();
      this.moveField.setText("");
      try {
        Thread.sleep(20);
      } catch (InterruptedException ex) {
        throw new RuntimeException(ex);
      }
      this.updatePiecesOnBoard();
    });
    movePanel.add(this.moveField);
    movePanel.add(submitButton);
    return movePanel;
  }

  private JPanel getGenericPanel() {
    final JPanel genericPanel = new JPanel();
    genericPanel.add(this.getNewGameButton());
    return genericPanel;
  }

  private JButton getNewGameButton() {
    final JButton newGameButton = new JButton();
    newGameButton.setText("New Game");
    newGameButton.addActionListener(
      e -> {
        final ChessBoard board = ChessBoard.defaultBoard();
        this.game = new Gameplay(board); // TODO: generalize
        this.updatePiecesOnBoard();
        new SwingWorker<Void, Void>() {
          @Override
          protected Void doInBackground() {
            players = List.of(new GuiPlayer(PlayerColor.WHITE, "white", board, BoardFrame.this),
                    new GuiPlayer(PlayerColor.BLACK, "black", board, BoardFrame.this));
            moveLatch = new CountDownLatch(1);
            final GameStatus result = game.playGame(players);
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

  private void updatePiecesOnBoard() {
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        final SquareButton square = this.squares[row][col];
        //square.setBackground(square.getDefaultColor());
        final PieceOnBoard piece = this.game == null ? null : this.game.getBoard().getPieceAt(new Square(col + 1, ChessBoard.BOARD_ROWS - row));
        square.setIcon(piece == null ? null : piece.getIcon(piece.getColor()));
      }
    }
  }

  public static void main(String[] args) {
    // Ensure GUI creation happens on the Event Dispatch Thread (EDT)
    SwingUtilities.invokeLater(BoardFrame::new);
  }
}
