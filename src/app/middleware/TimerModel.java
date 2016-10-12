package app.middleware;

import java.util.ArrayList;
import java.util.List;

import backend.timer.PlayerTimer;

public class TimerModel {
	
	private static final TimerModel INSTANCE = new TimerModel();
	private final PlayerTimer aWhiteTimer = new PlayerTimer();
	private final PlayerTimer aBlackTimer = new PlayerTimer();
	private final List<UIObserver> aObserverList = new ArrayList<>();

	private TimerModel(){}
	
	public static TimerModel getInstance(){
		return INSTANCE;
	}
	
	public String getBlackTimerStatus(){
		return aBlackTimer.toString();
	}
	
	public String getWhiteTimerStatus(){
		return aWhiteTimer.toString();
	}
	
	
	
	public void setTime(int pMinues, int pSeconds){
		aWhiteTimer.setTimer(pMinues, pSeconds);
		aBlackTimer.setTimer(pMinues, pSeconds);
	}
	
	public void startTimer(){
		aWhiteTimer.start();
		aBlackTimer.start();
		notifyObserver();
	}
	
	public void pauseTimer(){
		
	}
	
	
	public void addObserver(UIObserver pObserver){
		aObserverList.add(pObserver);
	}
	
	public void updateUI(){
		notifyObserver();
	}
	
	private void notifyObserver(){
		for(UIObserver observer: aObserverList){
			observer.updateView();
		}
	}
	
//	public int getBlackTimerMinutes(){
//		return aBlackTimer.getMinute();
//	}
//	
//	public int getWhiteTimerMinutes(){
//		return aWhiteTimer.getMinute();
//	}
	
	
	
	
}