package client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

//게임 화면 
public class MainPanelClient extends JPanel {
	static final int SCREEN_W = 1000;
	static final int SCREEN_H = 600;

	static Image playerImg1 = new ImageIcon("imgs/player1.png").getImage(); // 플레이어 이미지
	private Image playerImg2 = new ImageIcon("imgs/player2.png").getImage(); // 플레이어 이미지
	private Image missileImg = new ImageIcon("imgs/Missile1.png").getImage(); // 미사일 이미지
	static Image Lv1EnemyImg = new ImageIcon("imgs/enemy1.png").getImage();
	private Image Lv2EnemyImg = new ImageIcon("imgs/enemy2.png").getImage();
	private Image Lv3EnemyImg = new ImageIcon("imgs/enemy3.png").getImage();
	static Image bossImg = new ImageIcon("imgs/enemy_boss.png").getImage();
	private Image missileEnemyImg = new ImageIcon("imgs/Missile2.png").getImage();
	private Image heartImg = new ImageIcon("imgs/heart.png").getImage();
	private Image rocketImg = new ImageIcon("imgs/rocket.png").getImage();
	private Image itemImg = new ImageIcon("imgs/heartshield.png").getImage();

	private static final int PLAYER_SPEED = 13; // 플레이어 스피드
	static Player playerObj1 = new Player();
	static Player playerObj2 = new Player();
	static ScoreText st = new ScoreText(); // 점수 클래스

	static BufferedImage exploImg; // 폭발 모여 있는 이미지
	static List<BufferedImage> exploImgs = new CopyOnWriteArrayList<BufferedImage>(); // 나눈 이미지들
	static List<Explo> explostions = new CopyOnWriteArrayList<Explo>(); // 폭발 리스트 
	
	static List<Enemy> enemies = new CopyOnWriteArrayList<Enemy>(); // 적 리스트
	
	static List<Point> hpItems = new CopyOnWriteArrayList<Point>(); // 하트 아이템 리스트
	static List<Integer> events = new CopyOnWriteArrayList<Integer>(); // 클라이언트에서 서버로 보내는 이벤트 모으는 리스트

	private boolean isGameOver = false;
	private GameOverPanelClient gameOverPanel; //게임 오버 패널 

	// 생성자
	protected MainPanelClient() {
		this.setLayout(null);
		this.setOpaque(false);
		
		new Timer(30, e -> repaint()).start(); // 0.03s 씩 그리기 
		
		extractExplo(); // 폭탄 이미지 추출하기
		// 게임 오버 페널 띄우기
		gameOverPanel = new GameOverPanelClient();
		gameOverPanel.setBounds(0, 0, SCREEN_W, SCREEN_H);
		this.add(gameOverPanel);

		// 키입력 설정
		this.addKeyListener(new MyKeyListener());
		this.setFocusable(true); // 패널이 포커스를 받을 수 있게 함
	}

	// 화면 그리기
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g); // JPanel의 paintComponent()
		Background.draw(g, this);

		// 점수 그리기
		g.setFont(new Font("Arial", Font.TYPE1_FONT, 30));
		g.setColor(Color.WHITE);
		if(!(playerObj1.hp<=0 && playerObj2.hp <=0 )) // 죽엇을때는 점수 안뜨게 하기 
			g.drawString(st.getScore() ,800,60);
		
		// 플레이어1 그리기
		if (playerObj1.isVisible)
			g.drawImage(playerImg1, playerObj1.player.x, playerObj1.player.y, this); // 플레이어 그리기
		// 플레이어1 하트 그리기
		for (int i = 0; i < playerObj1.hp; i++)
			g.drawImage(heartImg, (0 + i * 50), 10, 70, 70, this);
		// 플레이어1 미사일 그리기
		for (Bullet b : playerObj1.bullets)
			g.drawImage(missileImg, b.bullet.x, b.bullet.y, this);

		// 플레이어2 그리기
		if (playerObj2.isVisible)
			g.drawImage(playerImg2, playerObj2.player.x, playerObj2.player.y, this); // 플레이어 그리기
		// 플레이어2 하트 그리기
		for (int i = 0; i < playerObj2.hp; i++)
			g.drawImage(heartImg, (0 + i * 50), 90, 70, 70, this);
		// 플레이어2 미사일 그리기
		for (Bullet b : playerObj2.bullets) {
			g.drawImage(missileImg, b.bullet.x, b.bullet.y, this);
		}

		// 생명 아이템 그리기
		for (Point p : hpItems)
			g.drawImage(itemImg, p.x, p.y, 30, 30, this);

		g.setColor(Color.RED);
		for (Enemy em : enemies) {
			if (em.isLife) { // 적이 살아 있을때만 그리기
				if (em instanceof Lv1Enemy) {
					g.drawImage(Lv1EnemyImg, em.enemy.x, em.enemy.y, this);
				} else if (em instanceof Lv2Enemy) {
					g.drawImage(Lv2EnemyImg, em.enemy.x, em.enemy.y, this);
				} else if (em instanceof Lv3Enemy) {
					g.drawImage(Lv3EnemyImg, em.enemy.x, em.enemy.y, this);
				} else if (em instanceof Boss) {
					g.drawImage(bossImg, em.enemy.x, em.enemy.y, this);

				}
				// 적이 살아 있고 체력바가 부딪힐때만 2초 동안 체력바 생성
				if (em.isCheckedBar) {
					int healthWidth = (int) ((double) em.hp / em.maxHp * em.W - 4); // 남은 피 비율 맞추기
					g.fillRect(em.enemy.x + 3, em.enemy.y + em.H, em.W - 4, 3);
					g.clearRect(em.enemy.x + 3, em.enemy.y + em.H, em.W - 4 - healthWidth, 3);
				}
			}
			for (Bullet bem : em.bullets) {
				if (bem.bulletType == 1)
					g.drawImage(rocketImg, bem.bullet.x, bem.bullet.y, 55, 21, this);
				else
					g.drawImage(missileEnemyImg, bem.bullet.x, bem.bullet.y, this);
			}
		}
		// 폭발 이미지 출력
		for (Explo ex : explostions) {
			g.drawImage(exploImgs.get(ex.idx), ex.explo.x, ex.explo.y, ex.cellW, ex.cellH, this);
		}

		// 플레이어 두명이 다 죽었을 떄
		if (playerObj1.hp <= 0 && playerObj2.hp <= 0) {
			if (!isGameOver) { // 처음 한 번만 실행되도록
				isGameOver = true;
				gameOverPanel.setVisible(isGameOver);
				gameOverPanel.updateFinalScore(st.score);
				gameOverPanel.setVisible(true);
				// 다른 컴포넌트보다 위로 오게 설정 (필요시)
				this.setComponentZOrder(gameOverPanel, 0);
				// 화면을 다시 그려서 패널이 보이게 함
				this.revalidate();
			}
		}
	}

	// 키 입력 함수
	private class MyKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();

			switch (keyCode) { // 화면에 넘어가면 작동하지 않도록 설정 완.
			case KeyEvent.VK_W: // 위
				if (playerObj2.player.y > 0)
					playerObj2.player.y -= PLAYER_SPEED;
				break;
			case KeyEvent.VK_S: // 아래
				if (playerObj2.player.y < SCREEN_H - playerObj2.H - 35)
					playerObj2.player.y += PLAYER_SPEED;
				break;
			case KeyEvent.VK_D: // 오른쪽
				if (playerObj2.player.x < SCREEN_W - playerObj2.W)
					playerObj2.player.x += PLAYER_SPEED;
				break;
			case KeyEvent.VK_A: // 왼쪽
				if (playerObj2.player.x > 0)
					playerObj2.player.x -= PLAYER_SPEED;
				break;
			case KeyEvent.VK_SPACE: // 미사일발사
				if(playerObj2.hp > 0) { // 죽었을 경우 미사일 못쏘게 하기 
					events.add(1); // 미사일을 쏘면 이벤트 리스트에 남아서 서버로 보냄
					break;
				}
			}
		}
	}

	// 폭발이미지 배열 추출 함수 (이미지 준비)
	private void extractExplo() {
		try {
			exploImg = ImageIO.read(new File("imgs/explosion.png"));
		} catch (IOException e) {
			System.out.println("폭발 이미지를 불러오는데에 오류가 생겼습니다.");
		}

		int cellW = exploImg.getWidth() / 17;
		int cellH = exploImg.getHeight();

		for (int i = 0; i < 17; i++) {
			exploImgs.add(exploImg.getSubimage(cellW * i, 0, cellW, cellH));
		}
	}
}

// 스코어 클래스 
class ScoreText {
	protected int score;
	protected int bossCnt;

	protected String getScore() {
		String s = "score : " + score;
		return s;
	}
}

//폭발 클래스 
class Explo {
	protected Point explo;
	protected int idx = 0;
	protected int cellW, cellH;

	// 생성자에서 이미지를 직접 받거나, 이미지를 가진 패널을 받습니다.
	protected Explo(Image img, int x, int y, int w, int h) {
		this.explo = new Point(x, y);

		// 이미지 정보를 안전하게 가져옵니다.
		this.cellW = (img.getWidth(null) / 17) + w - 32;
		this.cellH = img.getHeight(null) + h - 32;
	}

	// 폭발 이미지 바뀌는 함수
	protected void exploTimerClient() {
		Timer exT = new Timer(40, e -> { // 0.4ms
			idx++; // 0.4초 마다 인덱스 바꾸면서 다시 그리기
			if (idx >= 15) { // 마지막까지 그렸으면 멈추기
				((Timer) e.getSource()).stop();
				MainPanelClient.explostions.remove(this);
			}
		});
		exT.start();
	}
}
// 배경 움직인느 클래스 
class Background {
	static private Image mainImg = new ImageIcon("imgs/gameBackground.jpg").getImage();
	private static int bg1X = 0;
	private static int bg2X = 1000; // SCREEN_W가 1000이라고 가정
	private static final int BG_SPEED = 5;

	protected Background() {
		moveBg();
	}

	// 배경 움직이는 함수
	private void moveBg() {
		// setLayout(null);
		Timer bgTimer = new Timer(100, e -> {
			bg1X -= BG_SPEED;
			bg2X -= BG_SPEED;
			// 만약에 창 밖으로 나가면 대기열에 대기해
			if (bg1X <= -MainPanelClient.SCREEN_W) {
				bg1X = MainPanelClient.SCREEN_W;
			}
			if (bg2X <= -MainPanelClient.SCREEN_W) {
				bg2X = MainPanelClient.SCREEN_W;
			}
			// repaint();
		});
		bgTimer.start();
	}

	// 실제로 배경을 그려주는 함수 (어느 패널에서든 호출 가능)
	static void draw(Graphics g, JPanel panel) {
		g.drawImage(mainImg, bg1X, 0, 1000, 600, panel);
		g.drawImage(mainImg, bg2X, 0, 1000, 600, panel);
	}
}
//하트바 클래스 
class HpBar {
	protected int hp, maxHp;
	protected boolean isCheckedBar;
}
// 플레이어 클래스 
class Player extends HpBar {
	protected Point player = new Point((MainPanelClient.SCREEN_W / 2) - 32, MainPanelClient.SCREEN_H - 150);
	protected boolean isInvicible; // 무적일때 true
	protected boolean isVisible;
	protected int W = MainPanelClient.playerImg1.getWidth(null); // 32,32
	protected int H = MainPanelClient.playerImg1.getHeight(null);
	protected List<Bullet> bullets = new CopyOnWriteArrayList<Bullet>();

	protected Player() {
		maxHp = 3;
		hp = maxHp;
	}

}
//총알클래스 
class Bullet {
	protected Point bullet = new Point();
	protected int bulletType; // 0: 일반, 1: 보스 로켓
}

//적 클래스 
class Enemy extends HpBar {
	protected Point enemy = new Point(); // 적 위치
	protected List<Bullet> bullets = new CopyOnWriteArrayList<>();// 적이 가지고 있는 총알들
	protected int W = MainPanelClient.Lv1EnemyImg.getWidth(null);;
	protected int H = MainPanelClient.Lv1EnemyImg.getWidth(null);;
	protected boolean isLife; // 살아있으면 True, 죽어있으면 false
}

//Lv 1 적 
class Lv1Enemy extends Enemy {
	protected Lv1Enemy() {
		maxHp = 2;
	}
}

//Lv 2 적 
class Lv2Enemy extends Enemy {
	protected Lv2Enemy() {
		maxHp = 3;
	}
}

//Lv 3 적 
class Lv3Enemy extends Enemy {
	protected Lv3Enemy() {
		maxHp = 3;
	}
}

//Boss 적 
class Boss extends Enemy {
	protected Boss(ScoreText score) {
		maxHp = (score.bossCnt * 5) - 5;
		W = 10 + MainPanelClient.bossImg.getWidth(null); // 138 x 138
		H = 10 + MainPanelClient.bossImg.getWidth(null);
	}
}
