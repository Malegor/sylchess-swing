package com.sylvain.chess.ui;

import com.sylvain.chess.PlayerColor;
import com.sylvain.chess.board.ChessBoard;
import com.sylvain.chess.board.Square;
import com.sylvain.chess.io.fen.FenLoader;
import com.sylvain.chess.moves.Move;
import com.sylvain.chess.pieces.PieceOnBoard;
import com.sylvain.chess.play.Gameplay;
import com.sylvain.chess.play.players.Player;
import com.sylvain.chess.ui.players.GuiDummyPlayer;
import com.sylvain.chess.ui.players.GuiInteractivePlayer;
import com.sylvain.chess.ui.players.GuiMateSolver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Log4j2
public class BoardFrame extends JFrame {
  private static final int DEFAULT_SIZE = 600;
  private static final String FEN_MODE = "Load FEN description:";
  private static final String HUMAN_PLAYER = "Human player";
  private static final String DUMMY_PLAYER = "Dummy";
  private static final String PUZZLE_SOLVER = "Puzzle solver";
  public static final int DELAY_TO_REPAINT_BOARD = 30;

  private final SquareButton[][] squares;
  private final JTextField moveField;
  @Getter
  private final JLabel warningsLabel;
  @Getter
  private final DefaultTableModel movesTableModel;
  private final JLabel resultLabel;
  private final JComboBox<String> selectNewGameMode;
  private final JTextField fenDescription;
  private final JComboBox<String> whitePlayerChoice;
  private final JComboBox<String> blackPlayerChoice;

  private Gameplay game;
  private List<Player> players;
  private Player playersTurn;
  private int moveNumber;
  @Getter @Setter
  private CountDownLatch moveLatch;

  public BoardFrame() {
    this.setTitle("Sylchess Board");
    this.setSize(2 * DEFAULT_SIZE, DEFAULT_SIZE);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setResizable(true);
    this.getContentPane().setLayout(new GridLayout(1, 2));
    this.warningsLabel = new JLabel();
    this.movesTableModel = new DefaultTableModel(new String[]{"Move #", "White", "Black"}, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    this.resultLabel = new JLabel();
    this.moveField = new JTextField(5);
    this.selectNewGameMode = new JComboBox<>(new String[]{"Classical game", "Chess 960 (TODO)", FEN_MODE});
    this.fenDescription = new JTextField(25);
    this.whitePlayerChoice = new JComboBox<>(new String[]{HUMAN_PLAYER, DUMMY_PLAYER, PUZZLE_SOLVER});
    this.blackPlayerChoice = new JComboBox<>(new String[]{HUMAN_PLAYER, DUMMY_PLAYER, PUZZLE_SOLVER});

    this.players = new ArrayList<>(2);
    this.moveLatch = new CountDownLatch(1);
    this.squares = new SquareButton[8][8];
    this.add(this.getBoardPanel());
    this.add(this.getInteractivePanel());
    this.setVisible(true);
  }

  public void clearMovesTable() {
    this.movesTableModel.setRowCount(0);
  }

  private JPanel getInteractivePanel() {
    final JPanel interactivePanel = new JPanel(new BorderLayout());
    final JPanel newGamePanel = this.getNewGamePanel();
    final JPanel submitMovePanel = this.getSubmitMovePanel();
    submitMovePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    final JPanel infoPanel = this.getInfoPanel();
    interactivePanel.add(newGamePanel, BorderLayout.NORTH);
    interactivePanel.add(submitMovePanel, BorderLayout.CENTER);
    interactivePanel.add(infoPanel, BorderLayout.SOUTH);
    return interactivePanel;
  }

  private JPanel getBoardPanel() {
    final JPanel boardPanel = new JPanel();
    boardPanel.setLayout(new GridLayout(ChessBoard.BOARD_ROWS, ChessBoard.BOARD_COLS));
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        final SquareButton square = new SquareButton(row, col);
        this.squares[row][col] = square;
        // Optional: Store location data in the button for later reference
        // square.putClientProperty("location", new Point(row, col));
        boardPanel.add(square);
      }
    }
    return boardPanel;
  }

  private JPanel getInfoPanel() {
    final JPanel infoPanel = new JPanel(new GridBagLayout());
    infoPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    this.warningsLabel.setForeground(Color.RED);
    final Font currentFontWarning = this.warningsLabel.getFont();
    this.warningsLabel.setFont(currentFontWarning.deriveFont(currentFontWarning.getSize() * 1.5f));
    this.warningsLabel.setHorizontalAlignment(SwingConstants.CENTER);
    this.warningsLabel.setVerticalAlignment(SwingConstants.NORTH);
    final JTable movesTable = new JTable(this.movesTableModel);
    final JScrollPane scrollPane = new JScrollPane(movesTable);
    this.resultLabel.setForeground(Color.GREEN);
    final Font currentFontResult = this.resultLabel.getFont();
    this.resultLabel.setFont(currentFontResult.deriveFont(currentFontResult.getSize() * 6f));
    this.resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
    this.resultLabel.setVerticalAlignment(SwingConstants.NORTH);
    infoPanel.add(this.warningsLabel);
    infoPanel.add(scrollPane);
    infoPanel.add(this.resultLabel);
    return infoPanel;
  }

  private JPanel getSubmitMovePanel() {
    final JPanel submitMovePanel = new JPanel(new FlowLayout());
    final JButton submitButton = new JButton("Submit move");
    submitButton.addActionListener(e -> {
      this.updatePiecesOnBoard(); // This is to permit an update of the board in the case it has not been properly updated (ex: DELAY should be revised).
      final String move = this.moveField.getText();
      for (final Player player : this.players) {
        if (player instanceof GuiInteractivePlayer guiInteractivePlayer) {
          // OBS: in case of two GUI players, both of them will have this move set, even if only one of them will actually play it.
          // This could be improved by keeping track of which player has the next move.
          guiInteractivePlayer.setMove(move);
        }
      }
      this.moveLatch.countDown();
      this.moveField.setText("");
    });
    submitMovePanel.add(this.moveField);
    submitMovePanel.add(submitButton);
    return submitMovePanel;
  }

  private JPanel getNewGamePanel() {
    final JPanel newGamePanel = new JPanel(new FlowLayout());
    newGamePanel.add(this.selectNewGameMode);
    newGamePanel.add(this.fenDescription);
    final JLabel whiteLabel = new JLabel("White: ");
    final JLabel blackLabel = new JLabel("Black: ");
    newGamePanel.add(whiteLabel);
    newGamePanel.add(this.whitePlayerChoice);
    newGamePanel.add(blackLabel);
    newGamePanel.add(this.blackPlayerChoice);
    newGamePanel.add(this.getNewGameButton());
    return newGamePanel;
  }

  private JButton getNewGameButton() {
    final JButton newGameButton = new JButton();
    newGameButton.setText("New Game");
    newGameButton.addActionListener(
      e -> {
        log.info("New Game");
        this.game = this.getGame();
        this.game.getBoard().printBoard();
        this.players = this.getSelectedPlayers(this.game.getBoard());
        for (final Player player : this.players) {
          if (player.getColor().equals(this.game.getFirstPlayingColor())) {
            this.playersTurn = player;
            break;
          }
        }
        this.updatePiecesOnBoard();
        this.resultLabel.setText(" ");
        BoardFrame.this.clearMovesTable();
        this.warningsLabel.setText(" ");
        this.moveLatch = new CountDownLatch(1);
        this.moveNumber = game.getMoveNumber();
        this.movesTableModel.setColumnIdentifiers(new Object[]{this.movesTableModel.getColumnName(0), this.players.getFirst(), this.players.getLast()});
        new SwingWorker<Void, Void>() {
          @Override
          protected Void doInBackground() {
            game.playGame(players);
            resultLabel.setText(game.getEndGame().getPgn());
            return null;
          }
          @Override
          protected void done() {
            log.info("Game over: {}", resultLabel.getText());
          }
        }.execute();
      });
    return newGameButton;
  }

  private Gameplay getGame() {
    try {
      return FEN_MODE.equals(this.selectNewGameMode.getSelectedItem()) ? FenLoader.loadPosition(this.fenDescription.getText()) : new Gameplay(ChessBoard.defaultBoard());
    }
    catch (final IllegalArgumentException ex) {
      this.warningsLabel.setText(ex.getMessage());
      throw ex;
    }
  }

  private List<Player> getSelectedPlayers(final ChessBoard board) {
    this.getSelectedPlayer(board, PlayerColor.WHITE);
    return List.of(this.getSelectedPlayer(board, PlayerColor.WHITE), this.getSelectedPlayer(board, PlayerColor.BLACK));
  }

  private Player getSelectedPlayer(final ChessBoard board, final PlayerColor color) {
    final JComboBox<String> combo = color.equals(PlayerColor.WHITE) ? this.whitePlayerChoice : this.blackPlayerChoice;
    // TODO: parametrize player name and maxNumber of moves for solver
    return PUZZLE_SOLVER.equals(combo.getSelectedItem()) ?
            new GuiMateSolver(color, board, 5, this) :
            DUMMY_PLAYER.equals(combo.getSelectedItem()) ?
                    new GuiDummyPlayer(color, board, this) :
                    new GuiInteractivePlayer(color, "Human", board, BoardFrame.this);
  }

  private void updatePiecesOnBoard() {
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        final SquareButton square = this.squares[row][col];
        final PieceOnBoard piece = this.game == null ? null : this.game.getBoard().getPieceAt(new Square(col + 1, ChessBoard.BOARD_ROWS - row));
        square.setIcon(piece == null ? null : piece.getIcon(piece.getColor()));
      }
    }
  }

  public static void main(String[] args) {
    // Ensure GUI creation happens on the Event Dispatch Thread (EDT)
    SwingUtilities.invokeLater(BoardFrame::new);
  }

  public void applyMove(final Move move) {
    final String moveStr = move.toPgn();
    if (this.playersTurn.getColor().equals(PlayerColor.WHITE)) {
      this.movesTableModel.addRow(new Object[]{moveNumber, moveStr, ""});
    }
    else if (this.movesTableModel.getRowCount() == 0) {
      this.movesTableModel.addRow(new Object[]{moveNumber, "...", moveStr});
      moveNumber++;
    }
    else {
      this.movesTableModel.setValueAt(moveStr, this.movesTableModel.getRowCount() - 1, this.movesTableModel.getColumnCount() - 1);
      moveNumber++;
    }
    this.playersTurn = this.players.getFirst().equals(this.playersTurn) ? this.players.getLast() : this.players.getFirst();
    final ActionListener taskPerformer = evt -> {
      // This code block is executed after the specified delay on the EDT
      updatePiecesOnBoard();
      // Optional: call repaint() and validate() on your components if needed
      // myPanel.validate();
      // myPanel.repaint();
    };
    final Timer timer = new Timer(DELAY_TO_REPAINT_BOARD, taskPerformer);
    timer.setRepeats(false);
    timer.start();
  }
}
