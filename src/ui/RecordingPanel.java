package ui;

import backend.chess.Color;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import middleware.engine.GameModel;
import middleware.engine.UIObserver;

public class RecordingPanel extends HBox implements UIObserver{
	
	private VBox aWhiteMove;
	private VBox aBlackMove;
	private int aMoveCount = 0;
	
	public RecordingPanel(){
		aWhiteMove = new VBox();
		aBlackMove = new VBox();
		aWhiteMove.setPrefWidth(100);
		aBlackMove.setPrefWidth(100);
		this.getChildren().add(aWhiteMove);
		this.getChildren().add(aBlackMove);
		this.setWidth(220);
	}
	
	@Override
	public void updateView() {
		if(GameModel.getInstance().getLastMove() == null){
			aWhiteMove.getChildren().clear();
			aBlackMove.getChildren().clear();
		} 
		if(aMoveCount + 1 == GameModel.getInstance().getMoveCount()){
			if (GameModel.getInstance().getLastTurn() == Color.WHITE){
				Label moveLabel = new Label(GameModel.getInstance().getLastMove());
				moveLabel.setPrefWidth(80);
				moveLabel.setPadding(new Insets(5));
				moveLabel.setStyle("-fx-border-color: White; -fx-font-size: 12pt");
				aWhiteMove.getChildren().add(moveLabel);
			}else{
				Label moveLabel = new Label(GameModel.getInstance().getLastMove());
				moveLabel.setPrefWidth(80);
				moveLabel.setPadding(new Insets(5));
				moveLabel.setStyle("-fx-border-color: Black; -fx-font-size: 12pt");
				aBlackMove.getChildren().add(moveLabel);
			}
			aMoveCount++;
		}else{
			if(aWhiteMove.getChildren().size() == aBlackMove.getChildren().size()){
				aBlackMove.getChildren().remove(aBlackMove.getChildren().size() - 1);
			}else{
				aWhiteMove.getChildren().remove(aWhiteMove.getChildren().size() - 1);
			}
			aMoveCount--;
		}
		
	}

}
