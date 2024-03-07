package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeminiBot implements IBot{

    private final int totalAllowedTime = 1000; //in milliseconds

    private int player = 1; //which player is this bot?
    String botName = "Gemini";

    @Override
    public IMove doMove(IGameState state) {
        if(state.getMoveNumber()%2==0){
            player = 0;
        }
        List<Node> Nodes = new ArrayList<>();
        List<IMove> available = state.getField().getAvailableMoves();

        for(IMove m : available){
            Nodes.add(new Node(m));
        }


        int index = 0; //index of which node we are looking at
        long currentTime = System.currentTimeMillis();
        Random rand = new Random();
        while(System.currentTimeMillis() < currentTime + totalAllowedTime){

            //create new simulator and do the move we're evaluating
            GameSimulator sim = createSimulator(state);
            sim.updateBoard(Nodes.get(index).getMove());


            while (sim.getGameOver() == GameOverState.Active && System.currentTimeMillis() < currentTime + totalAllowedTime){ //while the game is still going
                List<IMove> avail = sim.getCurrentState().getField().getAvailableMoves();
                //do 2 random moves
                if(!avail.isEmpty()){
                    IMove move = avail.get(rand.nextInt(avail.size()));
                    sim.updateGame(move);
                } else{
                    System.out.println("it's empty!!");
                }

                if(sim.getGameOver() == GameOverState.Active){
                    System.out.println("amount of available moves: " + avail.size());
                    IMove move2 = avail.get(rand.nextInt(avail.size()));
                    sim.updateGame(move2);
                }
                System.out.println("did 2 moves");
            } //end while
            System.out.println("finished a match");
            System.out.println(sim.getTotalMoves());
            if(sim.getGameOver().equals(GameOverState.Win)){ //if the bot won
                Nodes.get(index).addWin();
                Nodes.get(index).setTotalScore(Nodes.get(index).getTotalScore() + 2000 - sim.getTotalMoves());
            } else if(sim.gameOver.equals(GameOverState.Loss)) {
                Nodes.get(index).addWin();
                Nodes.get(index).setTotalScore(Nodes.get(index).getTotalScore() - 2000 + sim.getTotalMoves());
            } else { // if it tied
                Nodes.get(index).addLoss();
                Nodes.get(index).setTotalScore(Nodes.get(index).getTotalScore() + sim.getTotalMoves());
            }
            System.out.println(index);
            index++;
            if(index >= Nodes.size()){
                index = 0;
            }
        }

        Node bestNode = Nodes.get(0);

        for(Node n: Nodes){
            System.out.println("score of " +  Nodes.indexOf(n) + ": " + n.totalScore);
            if(n.totalScore > bestNode.totalScore){
                bestNode = n;
            }
        }

        return bestNode.getMove();
    }

    public int getPlayer(){//retrieves whether this bot is player 1 or 0
        return player;
    }


    @Override
    public String getBotName() {
        return botName;
    }

    private GameSimulator createSimulator(IGameState state) {
        GameSimulator simulator = new GameSimulator(new GameState());
        simulator.setGameOver(GameOverState.Active);
        simulator.setCurrentPlayer(state.getMoveNumber() % 2);
        simulator.getCurrentState().setRoundNumber(state.getRoundNumber());
        simulator.getCurrentState().setMoveNumber(state.getMoveNumber());
        simulator.getCurrentState().getField().setBoard(state.getField().getBoard());
        simulator.getCurrentState().getField().setMacroboard(state.getField().getMacroboard());
        return simulator;
    }


    public class Node{
        private int winCount = 0;
        private int lossCount = 0;
        private int totalCount = 0;
        private int totalScore = 0;
        private IMove Move;
        public Node(IMove Move){
            this.Move = Move;
        }

        public int getWinCount() {
            return winCount;
        }

        public void setWinCount(int winCount) {
            this.winCount = winCount;
        }

        public void addWin(){
            winCount++;
            totalCount++;
        }

        public int getLossCount() {
            return lossCount;
        }

        public void setLossCount(int lossCount) {
            this.lossCount = lossCount;
        }

        public void addLoss(){
            lossCount++;
            totalCount++;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public IMove getMove() {
            return Move;
        }

        public void setMove(IMove move) {
            Move = move;
        }

        public int getTotalScore() {
            return totalScore;
        }

        public void setTotalScore(int totalScore) {
            this.totalScore = totalScore;
        }
    }


    //code from Jeppes SneakyBot (I think it would take too long to do this myself)
    public enum GameOverState {
        Active,
        Win,
        Loss,
        Tie
    }

    class GameSimulator {
        private final IGameState currentState;
        private int currentPlayer = 0; //player0 == 0 && player1 == 1
        private volatile GameOverState gameOver = GameOverState.Active;

        private int totalMoves = 0;

        public void setGameOver(GameOverState state) {
            gameOver = state;
        }

        public GameOverState getGameOver() {
            return gameOver;
        }

        public void setCurrentPlayer(int player) {
            currentPlayer = player;
        }

        public IGameState getCurrentState() {
            return currentState;
        }
        public int getTotalMoves(){
            return totalMoves;
        }

        public GameSimulator(IGameState currentState) {
            this.currentState = currentState;
        }

        public Boolean updateGame(IMove move) {
            if (!verifyMoveLegality(move))
                return false;

            updateBoard(move);
            currentPlayer = (currentPlayer + 1) % 2;
            totalMoves++;
            return true;
        }

        private Boolean verifyMoveLegality(IMove move) {
            IField field = currentState.getField();
            boolean isValid = field.isInActiveMicroboard(move.getX(), move.getY());

            if (isValid && (move.getX() < 0 || 9 <= move.getX())) isValid = false;
            if (isValid && (move.getY() < 0 || 9 <= move.getY())) isValid = false;

            if (isValid && !field.getBoard()[move.getX()][move.getY()].equals(IField.EMPTY_FIELD))
                isValid = false;

            return isValid;
        }

        private void updateBoard(IMove move) {
            String[][] board = currentState.getField().getBoard();
            board[move.getX()][move.getY()] = currentPlayer + "";
            currentState.setMoveNumber(currentState.getMoveNumber() + 1);
            if (currentState.getMoveNumber() % 2 == 0) {
                currentState.setRoundNumber(currentState.getRoundNumber() + 1);
            }
            checkAndUpdateIfWin(move);
            updateMacroboard(move);

        }

        private void checkAndUpdateIfWin(IMove move) {
            String[][] macroBoard = currentState.getField().getMacroboard();
            int macroX = move.getX() / 3;
            int macroY = move.getY() / 3;

            if (macroBoard[macroX][macroY].equals(IField.EMPTY_FIELD) ||
                    macroBoard[macroX][macroY].equals(IField.AVAILABLE_FIELD)) {

                String[][] board = getCurrentState().getField().getBoard();

                if (isWin(board, move, "" + currentPlayer))
                    macroBoard[macroX][macroY] = currentPlayer + "";
                else if (isTie(board, move))
                    macroBoard[macroX][macroY] = "TIE";

                //Check macro win
                if (isWin(macroBoard, new Move(macroX, macroY), "" + currentPlayer))
                    if(currentPlayer == player){
                        gameOver = GameOverState.Win;
                    } else {
                        gameOver = GameOverState.Loss;
                    }
                else if (isTie(macroBoard, new Move(macroX, macroY)))
                    gameOver = GameOverState.Tie;
            }

        }

        private boolean isTie(String[][] board, IMove move) {
            int localX = move.getX() % 3;
            int localY = move.getY() % 3;
            int startX = move.getX() - (localX);
            int startY = move.getY() - (localY);

            for (int i = startX; i < startX + 3; i++) {
                for (int k = startY; k < startY + 3; k++) {
                    if (board[i][k].equals(IField.AVAILABLE_FIELD) ||
                            board[i][k].equals(IField.EMPTY_FIELD))
                        return false;
                }
            }
            return true;
        }


        public boolean isWin(String[][] board, IMove move, String currentPlayer) {
            int localX = move.getX() % 3;
            int localY = move.getY() % 3;
            int startX = move.getX() - (localX);
            int startY = move.getY() - (localY);

            //check col
            for (int i = startY; i < startY + 3; i++) {
                if (!board[move.getX()][i].equals(currentPlayer))
                    break;
                if (i == startY + 3 - 1) return true;
            }

            //check row
            for (int i = startX; i < startX + 3; i++) {
                if (!board[i][move.getY()].equals(currentPlayer))
                    break;
                if (i == startX + 3 - 1) return true;
            }

            //check diagonal
            if (localX == localY) {
                //we're on a diagonal
                int y = startY;
                for (int i = startX; i < startX + 3; i++) {
                    if (!board[i][y++].equals(currentPlayer))
                        break;
                    if (i == startX + 3 - 1) return true;
                }
            }

            //check anti diagonal
            if (localX + localY == 3 - 1) {
                int less = 0;
                for (int i = startX; i < startX + 3; i++) {
                    if (!board[i][(startY + 2) - less++].equals(currentPlayer))
                        break;
                    if (i == startX + 3 - 1) return true;
                }
            }
            return false;
        }

        private void updateMacroboard(IMove move) {
            String[][] macroBoard = currentState.getField().getMacroboard();
            for (int i = 0; i < macroBoard.length; i++)
                for (int k = 0; k < macroBoard[i].length; k++) {
                    if (macroBoard[i][k].equals(IField.AVAILABLE_FIELD))
                        macroBoard[i][k] = IField.EMPTY_FIELD;
                }

            int xTrans = move.getX() % 3;
            int yTrans = move.getY() % 3;

            if (macroBoard[xTrans][yTrans].equals(IField.EMPTY_FIELD))
                macroBoard[xTrans][yTrans] = IField.AVAILABLE_FIELD;
            else {
                // Field is already won, set all fields not won to avail.
                for (int i = 0; i < macroBoard.length; i++)
                    for (int k = 0; k < macroBoard[i].length; k++) {
                        if (macroBoard[i][k].equals(IField.EMPTY_FIELD))
                            macroBoard[i][k] = IField.AVAILABLE_FIELD;
                    }
            }
        }
    }



}


