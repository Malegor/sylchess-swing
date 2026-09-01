package com.sylvain.chess.ui.runner;

import com.sylvain.chess.ui.BoardFrame;

import javax.swing.*;

public class ChessBoardRunner {

  public static void main(String[] args) {
    // Ensure GUI creation happens on the Event Dispatch Thread (EDT)
    SwingUtilities.invokeLater(BoardFrame::new);
  }
}