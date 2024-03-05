package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameManager;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;

import java.util.List;

public class GeminiBot implements IBot {

    private static final String BOTNAME = "Gemini";

    @Override
    public IMove doMove(IGameState state) {
        int currentPlayer = getCurrentPlayer(state);

        // Get available moves
        List<IMove> availableMoves = state.getField().getAvailableMoves();

        // Check if there are any available winning moves
        IMove winningMove = getWinningMove(state, availableMoves, currentPlayer);
        if (winningMove != null) {
            return winningMove;
        }

        // Check if there are any available blocking moves
        IMove blockingMove = getBlockingMove(state, availableMoves, currentPlayer);
        if (blockingMove != null) {
            return blockingMove;
        }

        // Use lookahead to simulate possible future moves and select the best one
        IMove bestMove = lookahead(state, availableMoves, currentPlayer);

        // If no winning, blocking, or lookahead move is available, return the first available move
        return bestMove != null ? bestMove : availableMoves.get(0);
    }

    private IMove lookahead(IGameState state, List<IMove> moves, int currentPlayer) {
        // Simulate each move and evaluate its outcome
        int maxScore = Integer.MIN_VALUE;
        IMove bestMove = null;

        for (IMove move : moves) {
            // Simulate placing the current player's symbol in the move position
            state.getField().getBoard()[move.getX()][move.getY()] = String.valueOf(currentPlayer);

            // Evaluate the macroboard (global state)
            int score = evaluateBoard(state);

            // Undo the simulation
            state.getField().getBoard()[move.getX()][move.getY()] = IField.EMPTY_FIELD;

            // Update the best move if the current move has a higher score
            if (score > maxScore) {
                maxScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private int evaluateBoard(IGameState state) {
        // Access the macroboard
        String[][] macroBoard = state.getField().getMacroboard();

        // Count the number of global wins
        int globalWins = countGlobalWins(macroBoard);

        // Analyze the overall game state and assign scores accordingly
        // You can add more criteria based on your game rules

        // Return a higher score for more desirable macroboard states
        return globalWins;
    }

    private int countGlobalWins(String[][] macroBoard) {
        int globalWins = 0;

        // Check rows
        for (int i = 0; i < 3; i++) {
            if (isWin(macroBoard[i][0], macroBoard[i][1], macroBoard[i][2])) {
                globalWins++;
            }
        }

        // Check columns
        for (int i = 0; i < 3; i++) {
            if (isWin(macroBoard[0][i], macroBoard[1][i], macroBoard[2][i])) {
                globalWins++;
            }
        }

        // Check diagonals
        if (isWin(macroBoard[0][0], macroBoard[1][1], macroBoard[2][2])) {
            globalWins++;
        }
        if (isWin(macroBoard[0][2], macroBoard[1][1], macroBoard[2][0])) {
            globalWins++;
        }

        return globalWins;
    }

    private boolean isWin(String cell1, String cell2, String cell3) {
        // Check if the cells are not empty and belong to the same player
        return !cell1.equals(IField.EMPTY_FIELD) && cell1.equals(cell2) && cell2.equals(cell3);
    }



    private IMove getWinningMove(IGameState state, List<IMove> moves, int currentPlayer) {
        for (IMove move : moves) {
            if (isWinningMove(state, move, currentPlayer)) {
                return move;
            }
        }
        return null;
    }

    private IMove getBlockingMove(IGameState state, List<IMove> moves, int currentPlayer) {
        int opponentPlayer = (currentPlayer + 1) % 2;

        // Check for winning moves on the global board
        for (IMove move : moves) {
            IField field = state.getField();
            String[][] macroBoard = field.getMacroboard();
            int macroX = move.getX() / 3;
            int macroY = move.getY() / 3;

            if (macroBoard[macroX][macroY].equals(IField.EMPTY_FIELD) ||
                    macroBoard[macroX][macroY].equals(IField.AVAILABLE_FIELD)) {

                // Simulate placing the opponent's symbol in the move position
                macroBoard[macroX][macroY] = String.valueOf(opponentPlayer);

                // Check if the move results in a win on the global board
                if (GameManager.isWin(macroBoard, new Move(macroX, macroY), String.valueOf(opponentPlayer))) {
                    // Undo the simulation and return the blocking move
                    macroBoard[macroX][macroY] = IField.EMPTY_FIELD;
                    return move;
                }

                // Undo the simulation for the next iteration
                macroBoard[macroX][macroY] = IField.EMPTY_FIELD;
            }
        }

        // No blocking move found
        return null;
    }

    private boolean isWinningMove(IGameState state, IMove move, int currentPlayer) {
        IField field = state.getField();
        String[][] board = field.getBoard();

        // Simulate placing the current player's symbol in the move position
        board[move.getX()][move.getY()] = String.valueOf(currentPlayer);

        // Check if the move results in a win
        return GameManager.isWin(board, move, String.valueOf(currentPlayer));
    }

    private int getCurrentPlayer(IGameState state) {
        if (state instanceof GameManager) {
            return ((GameManager) state).getCurrentPlayer();
        } else {
            // Handle the case where getCurrentPlayer() is not available
            // or use some default behavior
            return 0; // Default to player 0
        }
    }

    @Override
    public String getBotName() {
        return BOTNAME;
    }
}
