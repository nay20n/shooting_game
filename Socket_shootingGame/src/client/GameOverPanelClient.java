package client;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

//게임 오버 클래스 
class GameOverPanelClient extends JPanel {
	private ImageIcon gameOverImg = new ImageIcon("imgs/gameover1.png");
	private JLabel gameOverLabel;
	private JLabel totalScore;
	private JButton exitBtn;

	protected GameOverPanelClient() {
		this.setLayout(null);
		this.setOpaque(false);
		// 게임오버 이미지
		gameOverLabel = new JLabel(gameOverImg);
		gameOverLabel.setBounds(320, -30, 360, 360);
		this.add(gameOverLabel);
		// 총 점수 띄어주기
		totalScore = new JLabel("total Score : " + MainPanelClient.st.score);
		totalScore.setFont(new Font("Arial", Font.TYPE1_FONT, 40));
		totalScore.setForeground(Color.WHITE);
		totalScore.setBounds(360, 240, 600, 80);
		this.add(totalScore);
		// 나가는 버튼
		exitBtn = new JButton("나가기");
		exitBtn.setBounds(400, 420, 200, 50);
		exitBtn.setFocusPainted(false); // 포커스 돌리기
		exitBtn.setOpaque(true); // 배경 투명
		exitBtn.setBorderPainted(false); // 테두리 없애기
		exitBtn.addActionListener(new ExitListener());
		this.add(exitBtn, 0);

		this.setVisible(false);
	}
	// 마지막으로 스코어 업데이트 해주는 메서드 
	public void updateFinalScore(int score) {
		 totalScore.setText("total Score : " + score);

	}
}
