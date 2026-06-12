package client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

class GameStartPanelClient extends JPanel {
	static MainPanelClient mainPanel;
	private JLabel title;
	private JButton startBtn;
	private JButton exitBtn;
	private Timer bgRepaintTimer;

	GameStartPanelClient() {
		this.setLayout(null); // 레이아웃 없애기
		this.setOpaque(false); // 배경 투명 설정
		// 배경그리기
		bgRepaintTimer = new Timer(50, e -> {
			repaint();
		});
		bgRepaintTimer.start();
		// 제목 라벨
		title = new JLabel("Shooting Game");
		title.setFont(new Font("Arial", Font.TYPE1_FONT, 40));
		title.setForeground(Color.WHITE);
		title.setBounds(360, 100, 600, 80);
		this.add(title);
		// 게임 시작하기 버튼
		startBtn = new JButton("게임 시작");
		startBtn.setBounds(400, 350, 200, 50);
		startBtn.setFocusPainted(false); // 포커스 돌리기
		startBtn.setOpaque(true);
		startBtn.setBorderPainted(false);
		startBtn.addActionListener(new StartListener(this));
		this.add(startBtn);
		// 게임 나가기 버튼
		exitBtn = new JButton("나가기");
		exitBtn.setBounds(400, 420, 200, 50);
		exitBtn.setFocusPainted(false); // 포커스 돌리기
		exitBtn.setOpaque(true);
		exitBtn.setBorderPainted(false);
		exitBtn.addActionListener(new ExitListener());
		this.add(exitBtn);
	}
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Background.draw(g, this); // 배경 그리기
	}
}

//게임 종료 버튼 리스너 
class ExitListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		// 경고창 띄우기
		int answer = JOptionPane.showConfirmDialog(null, "정말 종료하겠습니까?", "확인 창", JOptionPane.YES_OPTION);
		if (answer == JOptionPane.YES_OPTION) { // YES_NO_OPTION이 아니라 YES_OPTION입니다!
		    System.out.println("종료합니다.");
		    System.exit(0);
		}
	}
}

//게임 시작 버튼 리스너
class StartListener implements ActionListener {
	private Socket clientSocket; //클라이언트 소켓 
	private DataOutputStream dos; //출력 스트림  
	private DataInputStream dis; //입력 스트림 
	// 리스너가 제어할 패널 정보를 담을 변수
	private JPanel currentPanel; //현재 패널 받아오는 변수 
	protected ReadThreadClient readThread; 
	protected WriteThreadClient writeThread;

	public StartListener(JPanel current) {
		this.currentPanel = current; // 어떤 패널에서 작동중인지 받음
	}

	@Override 
	public void actionPerformed(ActionEvent e) {
		// 클릭 전 한번 비우기
		try {
			if (clientSocket != null && !clientSocket.isClosed())
				clientSocket.close();
			if (readThread != null)
				readThread.interrupt();
			if (writeThread != null)
				writeThread.interrupt();
		} catch (IOException ex) {
			System.out.println("기존 소켓 닫기 실패: " + ex.getMessage());
		}
		
		try {
			System.out.println("--새로운 클라이언트 입장---");
			// 내 컴퓨터의 8000번 포트로 접속을 시도합니다.
			clientSocket = new Socket("localhost", 8000);
			// 데이터를 내보낼 입과 데이터를 받을 귀 생성
			dos = new DataOutputStream(clientSocket.getOutputStream());
			dos.flush();
			dis = new DataInputStream(clientSocket.getInputStream());

			// 화면 전환 로직
			java.awt.Container parent = currentPanel.getParent();; // 현재 패널의 부모 가져오기
			System.out.println(parent);
			parent.remove(currentPanel); // GameStartPanel 제거
			
			MainPanelClient mainPanel = new MainPanelClient(); 
			GameStartPanelClient.mainPanel = mainPanel; //메인 패널 생성 후 삽입 
			mainPanel.setBounds(0, 0, 1000, 600);
			
			parent.add(mainPanel); // 게임 패널 추가
			parent.revalidate(); // 레이아웃 재구성
			parent.repaint();
			mainPanel.requestFocusInWindow(); // 포커스 
			// 읽기 전용 스레드 실행
			readThread = new ReadThreadClient(dis);
			readThread.start();
			// 쓰기 전용 스레드 실행
			writeThread = new WriteThreadClient(dos);
			writeThread.start();
		} catch (ConnectException ex) { // 서버가 꺼져 있을 떄 발생하는 예외
			// 팝업 생성
			JOptionPane.showMessageDialog(null, "서버가 아직 생성되지 않았습니다.");
			System.out.println("에러: 서버를 찾을 수 없습니다.");
		} catch (IOException e1) { // 클라이언트가 강제로 연결을 끊었을 때
			e1.printStackTrace();
		}
	}
}

