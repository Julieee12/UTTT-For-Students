package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;

import java.util.ArrayList;
import java.util.List;

public class TestBot implements IBot{

    //first number is column, 2nd is row
    @Override
    public IMove doMove(IGameState state) {

        //can you win on the current miniboard?
        //can you win the whole game?

        //if not, can your opponent win?
        String player = "1";
        if(state.getMoveNumber()%2==0){
            player = "0";
        }

        List<IMove> winningMoves = getAllWinningMoves(state, player);
        List<IMove> losingmoves = getAllWinningMoves(state, player == "1" ? "0" : "1");
        //moves that the opponent could do that would cause us to lose


        if(!winningMoves.isEmpty()){
            for(IMove move : winningMoves){
                //check if the board that this will send you to has an opporitunity for the opponent to win
                if(getWinningPositionsThisBoard(state.getField().getBoard(), move.getX()%3, move.getY()%3, player).isEmpty()){
                    return move;
                }
            }
            return winningMoves.get(0);
        } else if (!losingmoves.isEmpty()) {
            return losingmoves.get(0);
        }
        return state.getField().getAvailableMoves().get(0);
    }

    public List<IMove> getWinningPositionsThisBoard(String board[][], int MacroX, int MacroY, String player){

        List<IMove> winningPositions = new ArrayList<>();

        //converting the MacroBoard positions to the (top left of the) regular board in that position
        int startX = MacroX*3;
        int startY = MacroY*3;
        //can you win vertically on this board?
        for(int i = startY; i<=startY + 2; i++){
            if(board[startX][i].equals(player) && board[startX+1][i].equals(player)
                    && board[startX+2][i].equals(IField.AVAILABLE_FIELD)){ //if first 2 spaces are taken by bot, and last is empty
                winningPositions.add(new Move(startX+2, i));
            }
            if(board[startX][i].equals(player) && board[startX+1][i].equals(IField.AVAILABLE_FIELD)
                    && board[startX+2][i].equals(player)){ //if center space on this column is empty, and other 2 are taken by player
                winningPositions.add(new Move(startX+1, i));
            }
            if(board[startX][i].equals(IField.AVAILABLE_FIELD) && board[startX+1][i].equals(player)
                    && board[startX+2][i].equals(player)){ //if last 2 spaces are taken by bot, and first
                winningPositions.add(new Move(startX, i));
            }

        }

        //can you win horizontally on this board?
        for(int i = startX; i<=startX + 2; i++){
            if(board[i][startY].equals(player) && board[i][startY+1].equals(player)
                    && board[i][startY+2].equals(IField.AVAILABLE_FIELD)){ //if first 2 spaces are taken by bot, and last is empty
                winningPositions.add(new Move(i, startY+2));
            }
            if(board[i][startY].equals(player) && board[i][startY+1].equals(IField.AVAILABLE_FIELD)
                    && board[i][startY+2].equals(player)){ //if center space on this row is empty, and other 2 are taken by player
                winningPositions.add(new Move(i, startY+1));
            }
            if(board[i][startY].equals(IField.AVAILABLE_FIELD) && board[i][startY+1].equals(player)
                    && board[i][startY+2].equals(player)){ //if last 2 spaces are taken by bot, and first
                winningPositions.add(new Move(i, startY));
            }


        }

        //can you win diagonally? (left to right
        if(board[startX][startY].equals(player) && board[startX+1][startY+1].equals(player)
                && board[startX+2][startY+2].equals(IField.AVAILABLE_FIELD)){ //if first 2 spaces are taken by bot, and last is empty
            winningPositions.add(new Move(startY+2, startY+2));
        }
        if(board[startX][startY].equals(player) && board[startX+1][startY+1].equals(IField.AVAILABLE_FIELD)
                && board[startX+2][startY+2].equals(player)){ //if center space on this diagonal is empty, and other 2 are taken by player
            winningPositions.add(new Move(startX+1, startY+1));
        }
        if(board[startX][startY].equals(IField.AVAILABLE_FIELD) && board[startX+1][startY+1].equals(player)
                && board[startX+2][startY+2].equals(player)){ //if last 2 spaces are taken by bot, and first
            winningPositions.add(new Move(startX, startY));
        }

        //can you win diagonally (right to left?)
        if(board[startX+2][startY].equals(player) && board[startX+1][startY+1].equals(player)
                && board[startX][startY+2].equals(IField.AVAILABLE_FIELD)){ //if first 2 spaces are taken by bot, and last is empty
            winningPositions.add(new Move(startY, startY+2));
        }
        if(board[startX+2][startY].equals(player) && board[startX+1][startY+1].equals(IField.AVAILABLE_FIELD)
                && board[startX][startY+2].equals(player)){ //if center space on this diagonal is empty, and other 2 are taken by player
            winningPositions.add(new Move(startX+1, startY+1));
        }
        if(board[startX+2][startY].equals(IField.AVAILABLE_FIELD) && board[startX+1][startY+1].equals(player)
                && board[startX][startY+2].equals(player)){ //if last 2 spaces are taken by bot, and first
            winningPositions.add(new Move(startX+2, startY));
        }
        return winningPositions;

    }

    public List<IMove> getAllWinningMoves(IGameState state, String player){
        String[][] board = state.getField().getBoard();

        List<IMove> winningMoves = new ArrayList<IMove>();
        for(int i = 0; i < 2; i++){
            for(int j = 0; j < 2; j++){
                winningMoves.addAll(getWinningPositionsThisBoard(board, i, j, player));
            }
        }

        //remove move if it's not in an active microboard
        for(IMove move: winningMoves){
            if (!state.getField().isInActiveMicroboard(move.getX(), move.getY())){
                winningMoves.remove(move);
            }
        }

        return winningMoves;
    }


    @Override
    public String getBotName() {
        return "Solaris";
    }
}
