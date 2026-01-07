import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Pair<T, V> {
  T first;
  V second;

  Pair(T first, V second) {
    this.first = first;
    this.second = second;
  }
}

class Position {
  char playerSign;  //'X' or 'O'  (meaning: player to move in this position)
  char[][] board;
  Integer evaluation;
  Pair<Integer, Integer> turnCoordinates;

  Position(char playerSign, char[][] board, Integer evaluation,
      Pair<Integer, Integer> turnCoordinates, int depth) {
    this.playerSign = playerSign;
    this.board = board;
    this.evaluation = Objects.requireNonNullElseGet(evaluation, () ->
        checkWinner(board, playerSign, depth));
    this.turnCoordinates = turnCoordinates;
  }

  int checkWinner(char[][] board, char playerSign, int depth) {
    char opponentSign = (playerSign == 'X') ? 'O' : 'X';

    int n = board.length;

    // rows
    for (char[] chars : board) {
      boolean rowPlayer = true;
      boolean rowOpponent = true;
      for (int j = 0; j < n; j++) {
        if (chars[j] != playerSign) rowPlayer = false;
        if (chars[j] != opponentSign) rowOpponent = false;
      }
      if (rowPlayer) return 10 - depth;
      if (rowOpponent) return depth - 10;
    }

    // cols
    for (int j = 0; j < n; j++) {
      boolean colPlayer = true;
      boolean colOpponent = true;
      for (int i = 0; i < n; i++) {
        if (board[i][j] != playerSign) colPlayer = false;
        if (board[i][j] != opponentSign) colOpponent = false;
      }
      if (colPlayer) return 10 - depth;
      if (colOpponent) return depth - 10;
    }

    // main diag
    boolean diagPlayer = true;
    boolean diagOpponent = true;
    for (int i = 0; i < n; i++) {
      if (board[i][i] != playerSign) diagPlayer = false;
      if (board[i][i] != opponentSign) diagOpponent = false;
    }
    if (diagPlayer) return 10 - depth;
    if (diagOpponent) return depth - 10;

    // anti diag
    diagPlayer = true;
    diagOpponent = true;
    for (int i = 0; i < n; i++) {
      if (board[i][n - 1 - i] != playerSign) diagPlayer = false;
      if (board[i][n - 1 - i] != opponentSign) diagOpponent = false;
    }
    if (diagPlayer) return 10 - depth;
    if (diagOpponent) return depth - 10;

    return 0;
  }

  char[][] getNewBoard(char[][] board) {
    char[][] newBoard = new char[board.length][board[0].length];
    for (int i = 0; i < board.length; i++) {
      newBoard[i] = board[i].clone();
    }
    return newBoard;
  }

  List<Pair<Integer, Integer>> getFreePositions() {
    List<Pair<Integer, Integer>> freePositions = new ArrayList<>();
    for (int rowIndex = 0; rowIndex < board.length; rowIndex++) {
      for (int colIndex = 0; colIndex < board[0].length; colIndex++) {
        if (board[rowIndex][colIndex] == '_') {
          freePositions.add(new Pair<>(rowIndex, colIndex));
        }
      }
    }
    return freePositions;
  }

  // CHANGED: explicit signToPlay, no reliance on mutating this.playerSign
  List<Position> getSuccessors(int depth, char signToPlay, char maxPlayerSign) {
    List<Position> successors = new ArrayList<>();
    List<Pair<Integer, Integer>> freePositions = getFreePositions();

    char nextToMove = (signToPlay == 'X') ? 'O' : 'X';

    for (Pair<Integer, Integer> move : freePositions) {
      char[][] newBoard = getNewBoard(board);
      newBoard[move.first][move.second] = signToPlay;

      Integer eval = checkWinner(newBoard, maxPlayerSign, depth);

      successors.add(new Position(nextToMove, newBoard, eval, move, depth));
    }
    return successors;
  }
}

public class Main {
  static char maxPlayerSign;
  static char minPlayerSign;

  static int maxValue(Position position, int depth, int alpha, int beta) {
    int terminalScore = position.checkWinner(position.board, maxPlayerSign, depth);
    if (terminalScore != 0) return terminalScore;
    if (isGameOver(position)) return 0;

    int maxEval = Integer.MIN_VALUE;
    List<Position> successors = position.getSuccessors(depth + 1, maxPlayerSign, maxPlayerSign);

    for (Position child : successors) {
      int eval = minValue(child, depth + 1, alpha, beta);
      maxEval = Math.max(maxEval, eval);
      alpha = Math.max(alpha, maxEval);
      if (alpha >= beta) break;
    }
    return maxEval;
  }

  static int minValue(Position position, int depth, int alpha, int beta) {
    int terminalScore = position.checkWinner(position.board, maxPlayerSign, depth);
    if (terminalScore != 0) return terminalScore;
    if (isGameOver(position)) return 0;

    int minEval = Integer.MAX_VALUE;
    List<Position> successors = position.getSuccessors(depth + 1, minPlayerSign, maxPlayerSign);

    for (Position child : successors) {
      int eval = maxValue(child, depth + 1, alpha, beta);
      minEval = Math.min(minEval, eval);
      beta = Math.min(beta, minEval);
      if (alpha >= beta) break;
    }
    return minEval;
  }

  static boolean isLexicographicallySmaller(Pair<Integer, Integer> a, Pair<Integer, Integer> b) {
    if (b == null) return true;
    if (!a.first.equals(b.first)) return a.first < b.first;
    return a.second < b.second;
  }

  static Pair<Integer, Integer> minimax(Position position, int depth, char playerSign) {
    List<Pair<Integer, Integer>> freePositions = position.getFreePositions();

    int bestScore = Integer.MIN_VALUE;
    int alpha = Integer.MIN_VALUE;
    int beta = Integer.MAX_VALUE;
    Pair<Integer, Integer> bestMove = null;

    for (Pair<Integer, Integer> move : freePositions) {
      char[][] newBoard = position.getNewBoard(position.board);
      newBoard[move.first][move.second] = playerSign;

      // After MAX plays, it becomes MIN's turn
      Position child = new Position(minPlayerSign, newBoard, null, move, depth);

      int score = minValue(child, depth + 1, alpha, beta);

      if (score > bestScore || (score == bestScore && isLexicographicallySmaller(move, bestMove))) {
        bestScore = score;
        bestMove = move;
      }

      alpha = Math.max(alpha, bestScore);
    }

    if (bestMove == null) return null;

    // 1-based output
    bestMove.first += 1;
    bestMove.second += 1;
    return bestMove;
  }

  static boolean isGameOver(Position position) {
    int length = position.board.length;
    for (int i = 0; i < length; i++) {
      for (int j = 0; j < length; j++) {
        if (position.board[i][j] == '_') return false;
      }
    }
    return true;
  }

  public static Position readInput() throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    String mode = br.readLine().trim();
    if (!mode.equals("JUDGE"))
      throw new IllegalArgumentException("Expected: JUDGE");

    String turnLine = br.readLine().trim();
    if (!turnLine.startsWith("TURN "))
      throw new IllegalArgumentException("Expected: TURN X or TURN O");

    char player = turnLine.charAt(5);
    if (player == 'X') {
      maxPlayerSign = 'X';
      minPlayerSign = 'O';
    } else {
      maxPlayerSign = 'O';
      minPlayerSign = 'X';
    }

    char[][] board = new char[3][3];

    for (int row = 0; row < 3; row++) {
      br.readLine(); // separator
      String line = br.readLine(); // content
      board[row][0] = line.charAt(2);
      board[row][1] = line.charAt(6);
      board[row][2] = line.charAt(10);
    }

    br.readLine(); // last separator

    // player to move is maxPlayerSign (TURN X means X plays now)
    return new Position(maxPlayerSign, board, null, null, 0);
  }

  public static void main(String[] args) throws IOException {
    Position initialPosition = readInput();

    int terminal = initialPosition.checkWinner(initialPosition.board, maxPlayerSign, 0);
    if (terminal != 0 || isGameOver(initialPosition)) {
      System.out.println(-1);
      return;
    }

    Pair<Integer, Integer> answer = minimax(initialPosition, 0, maxPlayerSign); //changed depth to 1
    if (answer == null) {
      System.out.println(-1);
      return;
    }
    System.out.println(answer.first + " " + answer.second);
  }
}