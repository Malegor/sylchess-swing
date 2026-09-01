
# sylchess-swing

This project is a Java-based chess program that allows users to play starting from a given position, or from the classical starting positions. The program can also solve puzzles up to 5 or 6 moves.


## Authors

- [@Malegor](https://github.com/Malegor)


## 🔗 Links
- [![github](https://img.shields.io/badge/github-repo-blue?logo=github)](https://github.com/Malegor/sylchess)
- [![jira](https://img.shields.io/badge/jira-0A66C2?style=for-the-badge&logo=jira&logoColor=white)](https://sylv1fournier.atlassian.net/jira/software/projects/SYLCHESS/boards/1)


## Features

- Chess game rules (including pawn promotion, en-passant, castling, etc.)
- Interactive game on console (human vs. human or possibility to call a solver to solve a puzzle, or to defend as best as possible)
- Solver based on an alpha-beta minimax algorithm
- Interactive game on desktop-based interface
- Load a given position from [FEN](https://www.chess.com/terms/fen-chess) description

## Run Locally

On the core project (sylchess):
```bash
  mvn clean install
```

Then on this project, it is just needed to call `java -jar sylchess-swing` and the UI opens.

### Right-hand panel

There you can run a new game (New Game button), using either a classical starting position, a chess 960 position, or a given position from a FEN description (fill the text field on the right). The players can be chosen between a human (interactive) player, a dummy player, and a puzzle solver (for the present it is mainly useful for puzzles and not for complete games).

In the next section, in case there is at least one interactive player, you can inform the next move by filling the text field and clicking "Submit move". The text the move you inform is based on PGN-kind moves, such as Kxd3, e8=Q etc. In case the move you enter is not an allowed move, you get a warning and a few more chances to enter a valid move. After 5 unsuccesful attempts, the game is considered resigned and the other player wins the game.

The lowest section gathers some information on the game. On the left-hand side, the warning messages are displayed, when necessary. In the middle, a table shows every move played in the current game until the last move. On the right-hand side, you get the game result as soon as it is over.

### Board panel

The board panel shows the game position after each move was played. As soon as a piece moved, the panel show its squares with distinct colors.

Right-clicking on a square will color the square, and double-clicking reset all the squares' default color.

You can also use the board to indicate your next move, by left-clicking on the piece you want to move and then on the destination square. In the case of castling however, you should click on the king and the rook to indicate that move. Note that even if the other player is still "thinking" (such as in the case of a puzzle solver), you can still pre-move by clicking on the squares, especially if the other player's next move is predictable.

## Acknowledgements

 - [readme.so](https://readme.so/editor)
 - [Badges](https://shields.io/badges)

