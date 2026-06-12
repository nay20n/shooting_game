package server;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;


class GameOverPanelServer extends JPanel{
	private ImageIcon gameOverImg = new ImageIcon("imgs/gameover1.png"); // 게임 오버 사진 가져오기 
	private JLabel gameOverLabel;	//게임오버 텍스트 라벨 
	private JLabel totalScore; 		//토탈 스코어 라벨
	private JButton exitBtn;
	
	GameOverPanelServer(){
		this.setLayout(null); //레이아웃 없애기 
		this.setOpaque(false); //베경 없애기 
		//게임오버 이미지 
		gameOverLabel = new JLabel(gameOverImg);
        gameOverLabel.setBounds(320, -30, 360, 360);
        this.add(gameOverLabel);
        //총 점수 띄어기 
        totalScore = new JLabel( "total Score : " + MainPanelServer.st.score);
        totalScore.setFont(new Font("Arial", Font.TYPE1_FONT, 40));
        totalScore.setForeground(Color.WHITE);
        totalScore.setBounds(360, 240, 600, 80);
        this.add(totalScore);
		//나가는 버튼 
        exitBtn = new JButton("나가기");
        exitBtn.setBounds(400, 420, 200, 50);
        exitBtn.setFocusPainted(false); //포커스 돌리기 
        exitBtn.setOpaque(true);
        exitBtn.setBorderPainted(false);
        exitBtn.addActionListener(new ExitListener());
 		this.add(exitBtn,0);
		
		this.setVisible(false);
	}
	// 마지막으로 스코어 업데이트 해주는 메서드 
	public void updateFinalScore(int score) {
		 totalScore.setText("total Score : " + score);
		
	}
}
