package com.sylvain.chess.ui;

import com.sylvain.chess.PlayerColor;
import com.sylvain.chess.board.ChessBoard;
import com.sylvain.chess.io.fen.FenLoader;
import com.sylvain.chess.io.fen.FenSaver;
import com.sylvain.chess.io.pgn.PgnSaver;
import com.sylvain.chess.moves.Move;
import com.sylvain.chess.play.Gameplay;
import com.sylvain.chess.play.players.Player;
import com.sylvain.chess.ui.players.GuiDummyPlayer;
import com.sylvain.chess.ui.players.GuiInteractivePlayer;
import com.sylvain.chess.ui.players.GuiAlphaBetaPlayer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

@Log4j2
public class BoardFrame extends JFrame {
  private static final int DEFAULT_SIZE = 600;
  private static final String STANDARD_GAME = "Standard game";
  private static final String CHESS_960 = "Chess 960";
  private static final String FEN_MODE = "Load FEN description:";
  private static final String HUMAN_PLAYER = "Human";
  private static final String DUMMY_PLAYER = "Dummy";
  private static final String ALPHA_BETA_PLAYER = "Alpha-beta";
  public static final int DELAY_TO_REPAINT_BOARD_MS = 30;
  public static final int DEFAULT_SEMI_MOVES = 9;

  @Getter
  private final JTextField moveField;
  @Getter
  private final JLabel warningsLabel;
  @Getter
  private final DefaultTableModel movesTableModel;
  private final JLabel resultLabel;
  private final JComboBox<String> selectNewGameMode;
  private final JTextField newGameTextField;
  private final JComboBox<String> whitePlayerChoice;
  private final JComboBox<String> blackPlayerChoice;
  private final ChessBoardPanel boardPanel;

  private Gameplay game;
  private List<Player> players;
  private Player playersTurn;
  private int moveNumber;
  @Getter
  private CountDownLatch waitingForNextMove;
  @Getter
  private ChessBoard currentBoard;
  private long timeInMs;

  public BoardFrame() {
    this.setTitle("Sylchess Board");
    this.setSize(2 * DEFAULT_SIZE, DEFAULT_SIZE);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setResizable(true);
    this.getContentPane().setLayout(new GridLayout(1, 2));
    this.warningsLabel = new JLabel();
    this.movesTableModel = new DefaultTableModel(new String[]{"Move #", "White", "Black", "White time", "Black time"}, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    this.resultLabel = new JLabel();
    this.moveField = new JTextField(5);

    this.selectNewGameMode = new JComboBox<>(new String[]{STANDARD_GAME, CHESS_960, FEN_MODE});
    this.newGameTextField = new JTextField(25);
    this.whitePlayerChoice = new JComboBox<>(new String[]{HUMAN_PLAYER, DUMMY_PLAYER, ALPHA_BETA_PLAYER});
    this.blackPlayerChoice = new JComboBox<>(new String[]{HUMAN_PLAYER, DUMMY_PLAYER, ALPHA_BETA_PLAYER});
    this.boardPanel = new ChessBoardPanel(this);

    this.players = new ArrayList<>(2);
    this.waitForNextMove();
    this.add(this.boardPanel);
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
    final JPanel panel = new JPanel(new FlowLayout());
    final JButton submitButton = new JButton("Submit move");
    submitButton.addActionListener(e -> {
      this.updatePiecesAfterMove();
      final String move = this.moveField.getText();
      if (move.isEmpty())
        return;
      final GuiInteractivePlayer nextPlayer = this.getNextInteractivePlayerToMove();
      if (nextPlayer != null)
        nextPlayer.setNextMove(move);
      this.publishNextMove();
      this.moveField.setText("");
    });
    final JButton exportFenButton = this.getExportPositionButton(panel);
    final JButton flipBoardButton = new JButton("Flip board");
    flipBoardButton.addActionListener(e -> {
      if (this.game == null)
        return;
      this.boardPanel.flipBoard();
      this.updatePiecesAfterMove();
    });
    panel.add(this.moveField);
    panel.add(submitButton);
    panel.add(exportFenButton);
    panel.add(flipBoardButton);
    return panel;
  }

  private JButton getExportPositionButton(final JPanel panel) {
    final JButton exportFenButton = new JButton("Export game");
    exportFenButton.addActionListener(e -> {
      if (this.game == null)
        return;
      final JTextArea fenText = new JTextArea(1, 40);
      fenText.setText(FenSaver.getPositionString(this.game));
      fenText.setEditable(false);
      fenText.setLineWrap(true);
      fenText.setWrapStyleWord(true);
      final JScrollPane fenPane = new JScrollPane(fenText);

      final JTextArea pgnText = new JTextArea(20, 40);
      pgnText.setText(PgnSaver.saveGame(this.game));
      pgnText.setEditable(false);
      pgnText.setLineWrap(true);
      pgnText.setWrapStyleWord(true);
      final JScrollPane pgnPane = new JScrollPane(pgnText);

      final JTabbedPane tabbedPane = new JTabbedPane();
      tabbedPane.addTab("FEN", fenPane);
      tabbedPane.addTab("PGN", pgnPane);

      // Set a preferred size for the scroll pane to control the dialog size if needed,
      // otherwise default size works well with JTextArea hints

      // Display the scrollable text area in a JOptionPane message dialog
      // null as the parent component centers the dialog on the screen
      JOptionPane.showMessageDialog(
              panel,
              tabbedPane,
              "Game description",
              JOptionPane.INFORMATION_MESSAGE
      );
    });
    return exportFenButton;
  }

  public GuiInteractivePlayer getNextInteractivePlayerToMove() {
    final List<GuiInteractivePlayer> guiInteractivePlayers = this.players.stream().filter(GuiInteractivePlayer.class::isInstance).map(GuiInteractivePlayer.class::cast).toList();
    return guiInteractivePlayers.size() > 1 ? (GuiInteractivePlayer) this.playersTurn : guiInteractivePlayers.isEmpty() ? null : guiInteractivePlayers.getFirst();
  }

  private JPanel getNewGamePanel() {
    final JPanel newGamePanel = new JPanel(new FlowLayout());
    newGamePanel.add(this.selectNewGameMode);
    newGamePanel.add(this.newGameTextField);
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
        if (this.game != null)
          this.game.abort();
        this.game = this.getNewGame();
        this.currentBoard = this.game.getBoard().copy();
        this.currentBoard.printBoard();
        this.players = this.getSelectedPlayers(this.game);
        for (final Player player : this.players) {
          if (player.getColor().equals(this.game.getHistory().getFirstPlayingColor())) {
            this.playersTurn = player;
            break;
          }
        }
        this.boardPanel.resetLastMove();
        this.boardPanel.resetSelectedMove();
        this.updatePiecesAfterMove();
        this.boardPanel.resetAllPaintedSquares(Set.of());
        this.resultLabel.setText(" ");
        BoardFrame.this.clearMovesTable();
        this.warningsLabel.setText(" ");
        this.waitForNextMove();
        this.moveNumber = game.getInfo().getMoveNumber();
        this.movesTableModel.setColumnIdentifiers(new Object[]{this.movesTableModel.getColumnName(0), this.players.getFirst(), this.players.getLast(),
                this.movesTableModel.getColumnName(3), this.movesTableModel.getColumnName(4)});
        this.timeInMs = System.currentTimeMillis();
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

  private Gameplay getNewGame() {
    log.info("New Game");
    try {
      return FEN_MODE.equals(this.selectNewGameMode.getSelectedItem()) ? FenLoader.loadPosition(this.newGameTextField.getText())
              : CHESS_960.equals(this.selectNewGameMode.getSelectedItem()) ?
                (this.is960ByIndex() ? new Gameplay(ChessBoard.board960ByIndex(this.get960Index()))
                : new Gameplay(ChessBoard.board960BySeed(this.get960Seed())))
                  : new Gameplay(ChessBoard.defaultBoard());
    }
    catch (final IllegalArgumentException ex) {
      this.warningsLabel.setText(ex.getMessage());
      throw ex;
    }
  }

  private boolean is960ByIndex() {
    return this.newGameTextField.getText().trim().startsWith("n");
  }

  private Long get960Seed() {
    final String text = this.newGameTextField.getText().trim();
    try {
      return Long.parseLong(text);
    } catch (NumberFormatException e) {
      if (!text.isEmpty())
        log.warn("Invalid format: \"{}\"", text);
    }
    return null;
  }

  private int get960Index() {
    final String text = this.newGameTextField.getText().trim().substring(1);
    try {
      return Integer.parseInt(text);
    } catch (NumberFormatException e) {
      if (!text.isEmpty())
        log.warn("Invalid format: \"{}\"", text);
    }
    return -1;
  }

  private List<Player> getSelectedPlayers(final Gameplay game) {
    return List.of(this.getSelectedPlayer(game, PlayerColor.WHITE), this.getSelectedPlayer(game, PlayerColor.BLACK));
  }

  private Player getSelectedPlayer(final Gameplay game, final PlayerColor color) {
    final JComboBox<String> combo = color.equals(PlayerColor.WHITE) ? this.whitePlayerChoice : this.blackPlayerChoice;
    // TODO: parametrize player name and maxNumber of moves for solver
    return ALPHA_BETA_PLAYER.equals(combo.getSelectedItem()) ?
            new GuiAlphaBetaPlayer(color, game, this.getMaxNumberOfSemiMoves(), this) :
            DUMMY_PLAYER.equals(combo.getSelectedItem()) ?
                    new GuiDummyPlayer(color, game.getBoard(), this) :
                    new GuiInteractivePlayer(color, HUMAN_PLAYER, game.getBoard(), BoardFrame.this);
  }

  private int getMaxNumberOfSemiMoves() {
    final String text = this.moveField.getText();
    try {
      if (!text.isEmpty())
        log.info("Reading depth from text field: {}", text);
      return text.isEmpty() ? DEFAULT_SEMI_MOVES : Integer.parseInt(text);
    } catch (NumberFormatException e) {
      if (!text.isEmpty())
        log.warn("Invalid format: \"{}\"", text);
    }
    return 9;
  }

  private void updatePiecesAfterMove() {
    this.boardPanel.updatePiecesAfterLastMove(this.currentBoard);
  }

  public void applyMove(final Move move) {
    final String moveStr = move.toCompleteSan();
    final long timeDiff = System.currentTimeMillis() - this.timeInMs;
    if (this.playersTurn.getColor().equals(PlayerColor.WHITE)) {
      this.movesTableModel.addRow(new Object[]{this.moveNumber, moveStr, "", timeDiff, ""});
    }
    else if (this.movesTableModel.getRowCount() == 0) {
      this.movesTableModel.addRow(new Object[]{this.moveNumber, Move.NO_WHITE_MOVE_STR, moveStr, "", timeDiff});
      this.moveNumber++;
    }
    else {
      // TODO constants for columns
      this.movesTableModel.setValueAt(moveStr, this.movesTableModel.getRowCount() - 1, 2);
      this.movesTableModel.setValueAt(timeDiff, this.movesTableModel.getRowCount() - 1, 4);
      this.moveNumber++;
    }
    this.playersTurn = this.players.getFirst().equals(this.playersTurn) ? this.players.getLast() : this.players.getFirst();
    this.boardPanel.resetSelectedMove();
    this.boardPanel.setLastMove(move);
    final ActionListener taskPerformer = evt -> {
      // This code block is executed after the specified delay on the EDT
      this.currentBoard = this.game.getBoard().copy();
      this.updatePiecesAfterMove();
      // Optional: call repaint() and validate() on your components if needed
      // myPanel.validate();
      // myPanel.repaint();
    };
    final Timer timer = new Timer(DELAY_TO_REPAINT_BOARD_MS, taskPerformer);
    timer.setRepeats(false);
    timer.start();
    this.timeInMs = System.currentTimeMillis();
  }

  public void waitForNextMove() {
    this.waitingForNextMove = new CountDownLatch(1);
  }

  public void publishNextMove() {
    this.waitingForNextMove.countDown();
  }

  public ChessBoard getInternalBoard() {
    return this.game.getBoard();
  }
}
