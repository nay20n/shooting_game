package server;

import javax.swing.JFrame;

class MyFrameServer extends JFrame {
	static final int SCREEN_W = 1000;
	static final int SCREEN_H = 600;
	private GameStartPanelServer gameStartPanel; // 게임 스타트 화면 패널

	private MyFrameServer() {
		init();
		gameStartPanel = new GameStartPanelServer();
		gameStartPanel.setBounds(0, 0, SCREEN_W, SCREEN_H);
		this.add(gameStartPanel);
		this.setVisible(true);
	}

	// 패널 초기화 함수
	private void init() {
		this.setSize(SCREEN_W, SCREEN_H); // 크기
		this.setLocation(250, 100); // 위치
		this.setResizable(false); // 사용자가 크기를 못 바꾸게
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 닫기를 눌럿을때 프로그램 정지
		this.setTitle("Server JFream"); // 제목
		this.setLayout(null); // 배치관리자 레이아웃 없애기
	}

	public static void main(String[] args) {
		new MyFrameServer();
		new Background();

	}
}
