package middleware.engine;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import backend.chess.ChessBoard;
import backend.chess.ChessPiece;
import backend.chess.Color;
import backend.player.PlayerTimer;
import backend.recording.BasicMoveCommand;
import backend.recording.Command;
import backend.websocket_client.SimpleClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import ui.TimerView;
import backend.rules.RuleBook;

/**
 * The bridge between back-end and front-end of the chess game.
 * @author Jiajun Chen
 *
 */
public class GameModel {

	private final String SERVER_URI = "ws://localhost:8886/websocket/";

	private static final GameModel INSTANCE = new GameModel();
	private final List<UIObserver> aObserverList = new ArrayList<>();
	private ChessBoard aChessBoard = ChessBoard.getInstance();
	private Stack<Command> aMoveStack = new Stack<>();
	private Stack<Command> aDiscardedMoveStack = new Stack<>();
	private TimerView aTimerView = null;
	private PlayerTimer aWhiteTimer = new PlayerTimer();
	private PlayerTimer aBlackTimer = new PlayerTimer();
	private SimpleClient client;
	private int aMoveCount = 0;

	private GameModel() {
		try {
			client = new SimpleClient(new URI(SERVER_URI));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public static GameModel getInstance(){
		return INSTANCE;
	}

	/**
	 * Add chessPiece to the board.
	 * @param pChess
	 * @param pX
	 * @param pY
	 */
	public void addChessPiece(ChessPiece pChess, int pX, int pY){
		aChessBoard.addPiece(pChess, pX, pY);
	}

	/**
	 * Return the ChessPiece for the specific Index
	 * @param pX
	 * @param pY
	 * @return
	 */
	public ChessPiece getChessPiece(int pX, int pY){
		return aChessBoard.getPiece(pX, pY);
	}

	/**
	 * Remove ChessPiece on the board.
	 * @param pX
	 * @param pY
	 */
	public void removeChessPiece(int pX, int pY){
		aChessBoard.removePiece(pX, pY);
	}

	/**
	 * Execute the move.
	 * @param pCommand
	 */
	private void executeMove(Command pCommand){

//		Command command = getCommandFromServer(pCommand);
		aMoveStack.push(pCommand);
		pCommand.execute();
		aMoveCount++;
		if(pCommand.getColor() == Color.WHITE){
			aTimerView.triggerTimer(Color.BLACK);
		}else{
			aTimerView.triggerTimer(Color.WHITE);
		}
		notifyObserver();
	}

	public void sendCommandToServer(Command pCommand) {
		Gson gson = new Gson();
		String jsonString = gson.toJson(pCommand);
		if(client.isConnected()){
			client.send(jsonString);
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}else{
			executeMove(pCommand);
		}
	}

	public void undoMove(){
		aDiscardedMoveStack.push(aMoveStack.pop());
		aDiscardedMoveStack.peek().undo();
		aMoveCount--;
		notifyObserver();
	}

	public String getLastMove(){
		if(aMoveStack.isEmpty()) return null;
		return aMoveStack.peek().toString();
	}

	public Color getLastTurn(){
		if(aMoveStack.isEmpty()) return Color.BLACK;
		return aMoveStack.peek().getColor();
	}

	public int getMoveCount(){
		return aMoveCount;
	}

	/**
	 * Reset the whole game.
	 */
	public void reset(){
		aChessBoard.reset();
		aMoveStack.clear();
		aDiscardedMoveStack.clear();
		aMoveCount = 0;
		client.close();
		client.connect();
		notifyObserver();
	}

	public void saveGame(String pPath) {
		aChessBoard.saveGame(pPath);
	}

	/**
	 * TODO: add load game function
	 */
	public void loadGame(String pPath) {
		aChessBoard.loadGame(pPath);
		notifyObserver();
	}

	/**
	 * Add observer
	 * @param pObserver
	 */
	public void addObserver(UIObserver pObserver){
		aObserverList.add(pObserver);
	}

	public void setTimerView(TimerView pView){
		aTimerView = pView;
	}

	/**
	 * Tell the observer that the back-end data has changed.
	 * Notify the UI to update itself.
	 */
	public void updateUI(){
		notifyObserver();
	}

	public void timerTick(Color pColor){
		if(pColor == Color.WHITE){
			aWhiteTimer.tick();
		}else{
			aBlackTimer.tick();
		}
	}

	public StringProperty getTimerStringProperty(Color pColor){
		if(pColor == Color.WHITE){
			return aWhiteTimer.getStringProperty();
		}else{
			return aBlackTimer.getStringProperty();
		}
	}

	/**
	 * Print the board in text View. Use it only for debugging purpose
	 */
	@Deprecated
	public void printBackEnd(){
		for(int i = 0; i < 8; i++){
			for(int j = 0; j < 8; j++){
				ChessPiece piece = aChessBoard.getPiece(i,j);
				if(piece == null){
					System.out.print("NULL" +" | ");
				}else{
					System.out.print(piece.toString() +" | ");
				}
			}
			System.out.println("");
		}
	}

	private void notifyObserver(){
		for(UIObserver observer: aObserverList){
			observer.updateView();
		}
	}

	public void receiveMessage(String msg) {
		Gson gson = new Gson();
		BasicMoveCommand command = gson.fromJson(msg, BasicMoveCommand.class);
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				executeMove(command);
			}
		});
	}
}