package server;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.Timer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

class GameStartPanelServer extends JPanel {
	static MainPanelServer mainPanel;
	JLabel title;
	JButton startBtn;
	JButton exitBtn;
	Timer bgRepaintTimer;

	GameStartPanelServer() {
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
	// 모든 패널의 paintComponent는 이렇게 통일됩니다.
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Background.draw(g, this); // 이 한 줄로 배경 그리기는 끝납니다.
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
	private ServerSocket serverSocket; // 서버 소켓
	private Socket clientSocket;
	private DataOutputStream dos;
	private DataInputStream dis;
	// 리스너가 제어할 패널 정보를 담을 변수
	private JPanel currentPanel;
	private ReadThreadServer readThread;
	private WriteThreadServer writeThread;

	public StartListener(JPanel current) {
		this.currentPanel = current; // 어떤 패널에서 작동중인지 받음.
	}

	@Override // 버튼 클릭
	public void actionPerformed(ActionEvent e) {
		// 클릭 전 한번 비우기
		try {
			if (clientSocket != null && !clientSocket.isClosed())
				clientSocket.close();
			if (serverSocket != null && !serverSocket.isClosed())
				serverSocket.close();
			if (readThread != null)
				readThread.interrupt();
			if (writeThread != null)
				writeThread.interrupt();
		} catch (IOException ex) {
			System.out.println("기존 소켓 닫기 실패: " + ex.getMessage());
		}
		
		GameStartPanelServer p = (GameStartPanelServer) currentPanel;
		p.startBtn.setVisible(false);
		p.exitBtn.setVisible(false);
		p.title.setText("상대방 접속 대기 중...");

		new Thread(() -> {
			try {
				Thread.sleep(100);
				// 8000번 포트 열기 
				serverSocket = new ServerSocket(8000);
				clientSocket = serverSocket.accept(); // 여기서 멈춰서 기다림
				// 데이터를 내보낼 입과 데이터를 받을 귀 생성
				dos = new DataOutputStream(clientSocket.getOutputStream());
				dos.flush();
				dis = new DataInputStream(clientSocket.getInputStream());

				// 화면 전환 로직
				SwingUtilities.invokeLater(() -> {
					java.awt.Container parent = currentPanel.getParent();
					parent.remove(currentPanel); // GameStartPanel 제거

					MainPanelServer mainPanel = new MainPanelServer();
					GameStartPanelServer.mainPanel = mainPanel;
					mainPanel.setBounds(0, 0, 1000, 600);

					parent.add(mainPanel);  // 게임 패널 추가
					parent.revalidate(); // 레이아웃 재구성
					parent.repaint();
					mainPanel.requestFocusInWindow(); // 포커스 
					// 읽기 전용 스레드 실행
					readThread = new ReadThreadServer(dis);
					readThread.start();
					// 쓰기 전용 스레드 실행
					writeThread = new WriteThreadServer(dos);
					writeThread.start();
				});

			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}).start();

	}

}
