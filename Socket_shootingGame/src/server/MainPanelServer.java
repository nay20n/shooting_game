package server;

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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

//게임 화면 
public class MainPanelServer extends JPanel {
	final static int SCREEN_W = MyFrameServer.SCREEN_W;
	final static int SCREEN_H = MyFrameServer.SCREEN_H;

	// BufferedImage은 먼지 알아보기
	// 객체를 만들고 있기 떄문에 허용되지 않는다.
	static Image playerImg1 = new ImageIcon("imgs/player1.png").getImage(); // 플레이어 이미지
	static Image playerImg2 = new ImageIcon("imgs/player2.png").getImage(); // 플레이어 이미지
	static Image missileImg = new ImageIcon("imgs/Missile1.png").getImage(); // 미사일 이미지
	static Image Lv1EnemyImg = new ImageIcon("imgs/enemy1.png").getImage();
	private Image Lv2EnemyImg = new ImageIcon("imgs/enemy2.png").getImage();
	private Image Lv3EnemyImg = new ImageIcon("imgs/enemy3.png").getImage();
	static Image bossImg = new ImageIcon("imgs/enemy_boss.png").getImage();
	static Image missileEnemyImg = new ImageIcon("imgs/Missile2.png").getImage();
	private Image heartImg = new ImageIcon("imgs/heart.png").getImage();
	static Image rocketImg = new ImageIcon("imgs/rocket.png").getImage();
	private Image itemImg = new ImageIcon("imgs/heartshield.png").getImage();

	private static final int PLAYER_SPEED = 13;
	static Player playerObj1 = new Player();
	static Player playerObj2 = new Player();
	static ScoreText st = new ScoreText();

	// 폭발 이미지와 폭발들 모여잇는 리스트
	static BufferedImage exploImg; // 폭발 모여 있는 이미지
	static ArrayList<BufferedImage> exploImgs = new ArrayList<BufferedImage>(); // 나눈 이미지들
	static ArrayList<Explo> explostions = new ArrayList<Explo>(); // 폭발 리스트

	// 에너미 출연하는 타이머와 에너미가 모여있는 리스트
	static ArrayList<Enemy> enemies = new ArrayList<Enemy>(); // 적 리스트
	static int[] delay = new int[] { 8000, 13000, 23000 };
	static Timer[] enemiesT = new Timer[3];

	static ArrayList<Point> hpItems = new ArrayList<Point>(); // 하트 아이템 리스트

	static boolean isGameOver = false;
	private GameOverPanelServer gameOverPanel; // 게임 오버 패널

	public MainPanelServer() {
		this.setLayout(null);
		this.setOpaque(false);

		new Timer(30, e -> repaint()).start(); // 0.03s 씩 그리기

		masterApperEmeny(); // 에너미 나타내기
		extractExplo(); // 폭탄 이미지 추출하기
		playerObj1.hpItemLocatedTimer(); // 렌덤하게 나오는 아이템 생성

		// 게임 오버 페널 띄우기
		gameOverPanel = new GameOverPanelServer();
		gameOverPanel.setBounds(0, 0, SCREEN_W, SCREEN_H);
		this.add(gameOverPanel);

		// 키입력 설정
		this.addKeyListener(new MyKeyListener());
		this.setFocusable(true); // 패널이 포커스를 받을 수 있게 함
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g); // JPanel의 paintComponent()
		Background.draw(g, this); // 이 한 줄로 배경 그리기는 끝납니다.

		// 점수 그리기
		g.setFont(new Font("Arial", Font.TYPE1_FONT, 30));
		g.setColor(Color.WHITE);
		if (!(playerObj1.hp <= 0 && playerObj2.hp <= 0)) // 죽엇을때는 점수 안뜨게 하기
			g.drawString(st.getScore(), 800, 60);

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
		for (Bullet b : playerObj2.bullets)
			g.drawImage(missileImg, b.bullet.x, b.bullet.y, this);

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
				if (em instanceof Boss)
					g.drawImage(rocketImg, bem.bullet.x, bem.bullet.y, 55, 21, this);
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

	// 적 레벨 세개의 타이머를 돌리는 함수
	static void masterApperEmeny() {
		for (int i = 0; i < delay.length; i++) {
			appearEmeny(i);
		}
	}

	// 에너미 나타나는 타이머 시작함수
	static void appearEmeny(int index) {
		// System.out.println("Lv" + (index+1) + " : " + (delay[index]/1000));
		if (enemiesT[index] != null)
			enemiesT[index].stop(); // 타이머가 작동중이라면 멈춰라
		// 타이머 시작
		enemiesT[index] = new Timer(delay[index], e -> {
			if (isGameOver)
				enemiesT[index].stop(); // 게임오버 시 타이머 멈추기
			Enemy newEnemy; // 새로운 적 생성
			if (index == 0)
				newEnemy = new Lv1Enemy();
			else if (index == 1)
				newEnemy = new Lv2Enemy();
			else
				newEnemy = new Lv3Enemy();
			enemies.add(newEnemy);
			moveEmenyT(newEnemy);
		});
		enemiesT[index].start();
	}

	// 에너미 움직이는 타이머 시작함수
	static void moveEmenyT(Enemy newEnemy) {
		Timer moveETimer = new Timer(50, e -> { // 0.5초 마다 움직임
			newEnemy.moveEnemy();
			// 적이 죽엇을 떄
			if (newEnemy.hp <= 0) {
				newEnemy.isLife = false; // 일단 죽이기
				Timer t = (Timer) e.getSource();
				t.stop(); // 타이머 멈춤
				st.updateScore(newEnemy, playerObj1); // 점수 상승
				// 폭발 객체 생성 후 배열에 넣기
				Explo newExplo = new Explo(MainPanelServer.exploImg, newEnemy.enemy.x - 50, newEnemy.enemy.y - 45,
						newEnemy.W, newEnemy.H);
				explostions.add(newExplo);
				newExplo.exploTimer();

				// 그리고 총알이 화면에 안남아 있다면 객체 삭제
				if (!newEnemy.isRemainBullet()) {// 총알이 화면에 안남아있다면
					enemies.remove(newEnemy); // 리스트에서 삭제
				}
			}
			// enemy가 화면 밖으로 나가면 삭제
			if (newEnemy.enemy.x < 0 || newEnemy.enemy.y < 0 || newEnemy.enemy.y > SCREEN_H - 30) {
				Timer t = (Timer) e.getSource();
				t.stop();
				enemies.remove(newEnemy); // 리스트에서 삭제
				// System.out.println("적 제거");
			}
			isCrashedEnemy(newEnemy, playerObj1); // 에너미와 플레이어와의 충돌체크
			isCrashedEnemy(newEnemy, playerObj2);

		});
		moveETimer.start();
	}

	// 에너미와 플레이어와의 충돌체크
	static boolean isCrashedEnemy(Enemy e, Player p) {
		if (p.hp <= 0)
			return false;
		// enemy와 player가 부딪혔을 경우 둘 다 heart--;
		if (p.isVisible && e.isLife && isCrashed(p.player, new Point(p.player.x + p.W, p.player.y + p.H), e.enemy,
				new Point(e.enemy.x + e.W, e.enemy.y + e.H))) {
			if (!p.isInvicible) { // 플레이어가 무적이 아닐 때
				p.modifyHP(-1); // 피감소
				if (p.hp > 0) {
					p.invincibility(); // 무적상태
				}
				e.modifyHP(-1); // 적의 체력 하나 삭제
				e.drawHpBar(); // hp bar 그리기
				p.player.x -= 4;
				System.out.println("player.hp = " + p.hp);
				return true;
			}
		}
		return false;
	}

	// 키 입력 함수
	private class MyKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
			// 화면에 넘어가면 작동하지 않도록 설정 완

			switch (keyCode) {
			case KeyEvent.VK_W: // 위
				if (playerObj1.player.y > 0)
					playerObj1.player.y -= PLAYER_SPEED;
				break;
			case KeyEvent.VK_S: // 아래
				if (playerObj1.player.y < SCREEN_H - playerObj1.H - 35)
					playerObj1.player.y += PLAYER_SPEED;
				break;
			case KeyEvent.VK_D: // 오른쪽
				if (playerObj1.player.x < SCREEN_W - playerObj1.W)
					playerObj1.player.x += PLAYER_SPEED;
				break;
			case KeyEvent.VK_A: // 왼쪽
				if (playerObj1.player.x > 0)
					playerObj1.player.x -= PLAYER_SPEED;
				break;
			case KeyEvent.VK_SPACE: // 미사일발사
				if (playerObj1.hp > 0) { // 죽었을 경우 미사일 못쏘게 하기
					playerObj1.appearBullet();
					break;
				}
			}
		}
	}

	// 폭발이미지 배열 추출 함수
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

	// 충돌 판별 함수
	// Object1 : (p1 ~ p2) <---> Ojbect2 : (p3 ~ p4) 충돌했으면 true를 리턴.
	static boolean isCrashed(Point p1, Point p2, Point p3, Point p4) {
		// p2.x 가 p3~p4 사이에 있다면
		if (p2.x >= p3.x && p2.x <= p4.x) {
			// p1.y가 p3.y ~ p4.y 사이에 있다면 crashed.
			if (p1.y >= p3.y && p1.y <= p4.y)
				return true;
			// or
			// p2.y가 p3.y ~ p4.y 사이에 있다면 crashed.
			if (p2.y >= p3.y && p2.y <= p4.y)
				return true;
		}
		// p1.x 가 p3~p4 사이에있다면
		if (p1.x >= p3.x && p1.x <= p4.x) {
			// p1.y가 p3.y ~ p4.y 사이에 있다면 crashed.
			if (p1.y >= p3.y && p1.y <= p4.y)
				return true;
			// or
			// p2.y가 p3.y ~ p4.y 사이에 있다면 crashed.
			if (p2.y >= p3.y && p2.y <= p4.y)
				return true;
		}
		return false;
	}
}

//스코어 조절 클래스 
class ScoreText {
	protected int score = 0;
	protected int bossScore = 0;
	protected int bossCnt = 1;

	// 점수 문자열 리턴 함수
	protected String getScore() {
		String s = "score : " + score;
		return s;
	}

	// 점수 올라가는 함수
	protected void updateScore(Enemy em, Player playerObj) {
		if (em instanceof Lv1Enemy) {
			score += 10;
			bossScore += 10;
		} else if (em instanceof Lv2Enemy) {
			score += 15;
			bossScore += 15;
		} else if (em instanceof Lv3Enemy) {
			score += 20;
			bossScore += 20;
		} else { // boss
			score += 40;
			bossScore += 40;
		}
		// 올라가다가 점수가 100 300 700 1000 ... 일 때 보스 출연
		if (bossScore >= 100 * bossCnt) {
			bossScore = 0;

			Enemy newEnemy = new Boss(this); // 보스 출
			MainPanelServer.enemies.add(newEnemy);
			MainPanelServer.moveEmenyT(newEnemy);
			// MyFrameServer.GameStartPanelServer.repaint();

			bossCnt++;
			em.speedUpEnemies();// 적 나오는 빈도수도 증가
			em.hpItemLocatedTimer();
		}
	}
}

//체력 관리 클래스 
class HpBar {
	protected int hp, maxHp;
	protected static final int USER_MAX_HP = 5;
	protected boolean isCheckedBar = false; // 체력바가 안보이면 false, 보이면 true
	private Timer hpBarT; // 적의 hp바 2초 보이게 하는 타이머
	private Timer itemT; // 주기적을 아이템 생성 체크 타이머
	private int itemTime = 20000; // 20s에 한번씩 아이템 생성
	// HP 수정 함수

	protected void modifyHP(int damage) {
		hp += damage;
		this.isCheckedBar = true;
	}

	// 체력 아이템 생성 무작위 위치 타이머함수
	protected void hpItemLocatedTimer() {
		// 이미 돌아가는 타이머가 있으면 스탑
		if (itemT != null)
			itemT.stop();

		itemT = new Timer(itemTime, e -> {
			// System.out.println(itemTime + "s 후 하트가 생성되었습니다.");
			if (MainPanelServer.isGameOver)
				itemT.stop();
			Point p = randomlyLocated();
			MainPanelServer.hpItems.add(p);
			removehpItem(p);

			// playerObj.itemHp();
		});
		itemT.start();
	}

	// 아이템 5초 후 사라지게 하는 타이머 함수
	private void removehpItem(Point p) {
		// 5초 후 아이템 삭제
		Timer removalTimer = new Timer(5000, e -> {
			if (MainPanelServer.hpItems.contains(p)) {
				MainPanelServer.hpItems.remove(p); // 리스크 삭제
			}
			Timer t = (Timer) e.getSource();
			t.stop(); // 사라졌으니까 타이머 정지
		});
		removalTimer.setRepeats(false); // 한 번만 실행
		removalTimer.start();
	}

	// 체력 아이템 랜덤하게 위치 설정하는함수
	private Point randomlyLocated() {
		Point p = new Point();
		p.x = (int) (Math.random() * 900) + 10;
		p.y = (int) (Math.random() * 500) + 10;
		return p;

	}

	// enemy의 hp가 하나씩 없어질때마다 함수 사용, 잠깐 리페이팅후 2초 후 삭제하기
	protected void drawHpBar() {
		// isCheckedBar = true; //부딪히면 True / true일 때 출력
		if (hpBarT != null) {
			hpBarT.restart();
		}
		// 2초 후 다시 false
		hpBarT = new Timer(2000, e -> {
			isCheckedBar = false;
			Timer t = (Timer) e.getSource();
			t.stop();
		});
		hpBarT.start();
	}
}

//플레이어 클래스 
class Player extends HpBar {
	// x: (화면너비/2) - (비행기너비/2), y: 화면높이 - 150
	protected Point player;
	protected boolean isInvicible; // 무적일때 true
	protected boolean isVisible; // 보일때 true
	protected int W = MainPanelServer.playerImg1.getWidth(null); // 32,32
	protected int H = MainPanelServer.playerImg1.getHeight(null);
	protected List<Bullet> bullets = new CopyOnWriteArrayList<Bullet>();

	public Player() {
		player = new Point((MainPanelServer.SCREEN_W / 2) - 32, MainPanelServer.SCREEN_H - 150);
		maxHp = 3;
		hp = maxHp; // 체력 설정
		isVisible = true; // 보이게
		isInvicible = false; // 무적이 아니게
		itemHp(); // HP 먹는거 체크하는 타이머 함수
	}

	// 체력 수정 함수
	@Override
	public void modifyHP(int damage) {
		hp += damage;

		// 체력이 5이상이면 더이상 안커지게 하기
		if (hp > USER_MAX_HP)
			hp = USER_MAX_HP;

		if (hp <= 0) { // 체력 0이면 죽이기
			hp=0;
			isInvicible = true; // 무적
			isVisible = false; // 안보이게
			bullets.clear(); // 총알 다 지우기
			// 폭발 생성
			Explo newExplo = new Explo(MainPanelServer.exploImg, player.x - 50, player.y - 45, W, H);
			MainPanelServer.explostions.add(newExplo);
			newExplo.exploTimer();
		}
	}

	// 아이템먹은 타이머함수
	private void itemHp() {
		Timer t = new Timer(50, e -> {
			for (Point p : MainPanelServer.hpItems) {
				if (isVisible && MainPanelServer.isCrashed(player, new Point(player.x + W, player.y + H), p,
						new Point(p.x + 20, p.y + 20))) {
					modifyHP(1); // hp++
					MainPanelServer.hpItems.remove(p);
					break;
				}
			}
		});
		t.start();
	}

	// 무적 시간 만들기
	public void invincibility() {
		if (this.hp <= 0) return;
		
		isInvicible = true; // 무적이면 true
		startBlink(); // 반짝이는 타이머 시작
		Timer it = new Timer(1500, e -> { // 1.5s
			isInvicible = false;
			Timer t = (Timer) e.getSource();
			t.stop();
		});
		it.start();
	}

	// 무적일 때 깜빡깜빡 함수
	public void startBlink() {
		Timer bt = new Timer(200, e -> { // 0.2s
			if (isInvicible) { // 무적상태일 떄
				isVisible = !isVisible; // 바로 true는 fasle로 false는 true로
				// GameServer.mainPanel.repaint();
			} else { // 무적상태가 아닐때
				isVisible = true; // 보이게 하고
				Timer t = (Timer) e.getSource();
				t.stop(); // 타이머 멈춤
			}
		});
		bt.start();
	}

	// 플레이어 총알 생성 함수
	public void appearBullet() {
		Bullet newBullet = new Bullet();
		newBullet.setPlayerBullet(player.x, player.y);
		bullets.add(newBullet);
		moveBullet(newBullet);
	}

	// 플레이어 총알 움직이는 타이머 함수
	public void moveBullet(Bullet newBullet) {

		Timer bTimer = new Timer(50, e -> { // 0.5s
			newBullet.moveBullet(6); // 6px씩 0.5초 하나하나당 움직이기
			// 미사일이 화면 밖에 나가면 stop, Bullet 리스트 삭제
			if (newBullet.bullet.x > MainPanelServer.SCREEN_W) {
				Timer t = (Timer) e.getSource();
				t.stop();
				bullets.remove(newBullet); // 리스트에서 삭제
			}
			// 충돌(isCrashed) 체크
			for (Enemy em : MainPanelServer.enemies) {
				if (em.isLife && MainPanelServer.isCrashed(newBullet.bullet,
						new Point(newBullet.bullet.x + newBullet.W, newBullet.bullet.y + newBullet.H), em.enemy,
						new Point(em.enemy.x + em.W, em.enemy.y + em.H))) {
					Timer t = (Timer) e.getSource();
					t.stop(); // 플레이어 총알 타이머 멈추기
					bullets.remove(newBullet); // 리스트 삭제
					em.modifyHP(-1); // 체력 하나 삭제
					em.drawHpBar(); // hp bar 그리기
					em.enemy.x += 4;
					// System.out.println("충돌! enemy&bullet\t enemy.heart :" + em.hp);
				}
			}
		});
		bTimer.start();
	}
}

//적 클래스 
abstract class Enemy extends HpBar implements Serializable {
	protected Point enemy = new Point();
	List<Bullet> bullets = new CopyOnWriteArrayList<Bullet>();

	protected int W = MainPanelServer.Lv1EnemyImg.getWidth(null);;
	protected int H = MainPanelServer.Lv1EnemyImg.getWidth(null);;
	protected int bulletSpeedX;
	protected int bulletSpeedY;
	protected int enemySpeedX, enemySpeedY;
	boolean isLife = true; // 살아있으면 True, 죽어있으면 false
	// 자동적으로 오른쪽에서 나오는 함수
	protected void setEnemyPoint() { 
		enemy.x = 1000;
		enemy.y = (int) (Math.random() * 530 + 30);
	}
	// 보스에서 나올려고 위치를 세팅하는 함수
	protected void setEnemyPoint(int x, int y) { 
		enemy.x = x - 12;
		enemy.y = y + 55;
	}
	// 움직이는 함수
	abstract protected void moveEnemy();

	// 리스트가 비어있지 않으면 총알이 남아있는 것(true), 비어있으면 없는 것(false)
	boolean isRemainBullet() {
		return !bullets.isEmpty();
	}
	// 적 나오는 빈도수 올리는 함수
	void speedUpEnemies() {
		for (int i = 0; i < MainPanelServer.delay.length; i++) {
			MainPanelServer.delay[i] -= 3000;
			if (MainPanelServer.delay[i] < 0) {
				switch (i) {
				case 0:
					MainPanelServer.delay[i] = 1000;
					break;
				case 1:
					MainPanelServer.delay[i] = 1300;
					break;
				case 2:
					MainPanelServer.delay[i] = 1500;
					break;
				}
			}
		}
		MainPanelServer.masterApperEmeny(); // 새로 있던 거 없애고 다시 타이머 조절
	}

	// 적 총알 발사하는 함수
	protected void appearBullet(int bulletAppear) { // bulletAppear 속도 만큼 생성

		Timer apperEBTimer = new Timer(bulletAppear, e -> {
			if (!isLife) { // 만약에 죽었으면(false) 총알 그만 발사
				Timer t = (Timer) e.getSource();
				t.stop();
			} else { // 안죽어 있으면 발사
				Bullet newBullet = new Bullet();
				if (this instanceof Boss)
					newBullet.setBossRocket(enemy.x, enemy.y);
				else
					newBullet.setEnemyBullet(enemy.x, enemy.y);
				bullets.add(newBullet);
				moveBullet(newBullet);
			}
		});
		apperEBTimer.start();
	}

	// 적 총알 움직이는 함수
	protected void moveBullet(Bullet newBullet) {
		Timer bTimer = new Timer(50, e -> { // 0.5s
			if (this instanceof Boss) //총 쏘는게 보스이면 
				newBullet.moveRocket(bulletSpeedX, bulletSpeedY);
			else // 그냥 적들이면 
				newBullet.moveBullet(bulletSpeedX); // 하나하나당 움직이기
			// 총알이 화면 밖으로 나갔을 때 리스트 삭제
			if (newBullet.bullet.x <= -1) {
				Timer t = (Timer) e.getSource();
				t.stop(); // 타이머 멈춤 
				bullets.remove(newBullet); // 리스트에서 삭제
			}

			// 적의 총알과 플레이어1가 부딪히면 플레이어 hp--;, 총알 삭제
			if (isCrashedBullet(newBullet, MainPanelServer.playerObj2)) {
				Timer t = (Timer) e.getSource();
				t.stop(); // 총알 멈춤
				bullets.remove(newBullet); // 총알 리스트에서 삭제
				// 적이 죽고 총알이 없음을 인지하면 객체 삭제
				if (!isLife && bullets.isEmpty())
					MainPanelServer.enemies.remove(this);
			}
			// 적의 총알과 플레이어2가 부딪히면 플레이어 hp--;, 총알 삭제
			if (isCrashedBullet(newBullet, MainPanelServer.playerObj1)) {
				Timer t = (Timer) e.getSource();
				t.stop();
				bullets.remove(newBullet); // 리스트에서 삭제
				// 적이 죽고 총알이 없음을 인지하면 객체 삭제
				if (!isLife && bullets.isEmpty()) 
					MainPanelServer.enemies.remove(this);
			}
		});
		bTimer.start();
	}

	// 적 총알과 플레이어가 부디진지 확인하느 함수
	private boolean isCrashedBullet(Bullet b, Player p) {

		if (p.hp <= 0) // 안 부딪혔으면
			return false;

		// 플레이어가 보이고 적의 총알과 플레이어가 부딪히면 플레이어 hp--;, 총알 삭제
		if (p.isVisible && MainPanelServer.isCrashed(b.bullet, 
				new Point(b.bullet.x + b.W, b.bullet.y + b.H), 
				p.player,
				new Point(p.player.x + p.W, p.player.y + p.H))) {
			if (!p.isInvicible) { // 무적이 아니면 
				p.modifyHP(-1); // 피감소
				p.invincibility(); // 무적상태
				//System.out.println("player.hp = " + p.hp);
				p.player.x -= 4; // 맞을 때 살짝 뒤로 
				return true;
			}
		}
		return false;
	}
}
// Lv1 적 
class Lv1Enemy extends Enemy {
	protected Lv1Enemy() {
		setEnemyPoint();
		bulletSpeedX = -4;
		maxHp = 2;
		hp = maxHp;
		enemySpeedX = 2;
		appearBullet(5000); // 5s마다 생성
	}
	// 움직이는 함수 
	@Override
	protected void moveEnemy() {
		enemy.x -= enemySpeedX;
	}
}
// Lv2 적 
class Lv2Enemy extends Enemy {
	private int direction; // 위 아래 방향
	protected Lv2Enemy() {
		setEnemyPoint();
		bulletSpeedX = -6;
		maxHp = 3;
		hp = maxHp;
		enemySpeedX = 3;
		enemySpeedY = 1;
		direction = (int) (Math.random() * 2); // level2이 어느방향으로 대각선으로 나갈지 랜덤으로 결정
		appearBullet(4000); // 4s 마다 생성
	}

	@Override
	// 대각선으로 생겻다가 없어
	protected void moveEnemy() {
		enemy.x -= enemySpeedX;
		if (direction == 1)
			enemy.y -= enemySpeedY;
		else if (direction == 0)
			enemy.y += enemySpeedY;
	}
}
// Lv3 적 
class Lv3Enemy extends Enemy {
	private double degree; // 각도 조정
	protected Lv3Enemy() {
		setEnemyPoint();
		bulletSpeedX = -6;
		maxHp = 3;
		hp = maxHp;
		enemySpeedX = 4;
		degree = 0; // 생성할때마다 0으로 초기화
		appearBullet(4000); // 4s 마다 생성
	}
	@Override
	// 물결로 지그재그로 지그재그
	protected void moveEnemy() {
		enemy.x -= enemySpeedX;
		degree = (0.1 + degree); // x축 증가
		enemy.y += 3 * Math.sin(0.5 * degree);
	}
}
// Boss 적 
class Boss extends Enemy {
	Boss(ScoreText score) {
		// 크기 재조정 
		W = 10 + MainPanelServer.bossImg.getWidth(null); // 138 x 138
		H = 10 + MainPanelServer.bossImg.getWidth(null);
		enemy.x = 1000; // 출현 위치 
		enemy.y = (MainPanelServer.SCREEN_H / 2) - (H / 2); // 정중앙 소환

		bulletSpeedX = -14;
		bulletSpeedY = 12;
		enemySpeedX = -2; // 타이머가 시작되기 전에 일단 앞으로 가고 있음

		maxHp = (score.bossCnt * 5); // 보스가 나올 때마다 체력이 5씩 추가로 증가
		hp = maxHp;

		startSpawnEnemies(); // 다른 적들을 생성 
		timerBoss(); // 보스가 만들어지자마자 속도바꾸는 타이머 생성.

		appearBullet(5000); // 5s 마다 생성
	}
	// 작은 적들 스폰하는 타이머 
	private void startSpawnEnemies() {
		Timer tS = new Timer(3000, e -> { // 4초 마다 미니언 생성
			if (!isLife) { // 보스가 죽었으면 타이머 정지
				Timer t = (Timer) e.getSource();
				t.stop();
			}
			Enemy newMinion; 
			int idx = (int) (Math.random() * 3); // 3개의 애너미 랜덤 
			if (idx == 0)
				newMinion = new Lv1Enemy();
			else if (idx == 1)
				newMinion = new Lv2Enemy();
			else
				newMinion = new Lv3Enemy();
			newMinion.setEnemyPoint(enemy.x, enemy.y); // 위치는 보스 몸에서 

			MainPanelServer.enemies.add(newMinion); // 리스트에 적 삽입 
			MainPanelServer.moveEmenyT(newMinion); // 생성된 적 움직이기 시작 
		});
		tS.start();
	}
	// 보스 움직이는 속도를 2초마다 랜덤으로 만들어지는 타이머
	private void timerBoss() {
		Timer tb = new Timer(2000, e -> {
			do {
				enemySpeedX = (int) (Math.random() * 5) - 2;
				enemySpeedY = (int) (Math.random() * 5) - 2;
			} while (enemySpeedX == 0 && enemySpeedY == 0); // 스피드가 0이 아닐때까지 스피드 정하기 
			
			if (!isLife) { // 죽어잇다면 타이머 종료 
				Timer t = (Timer) e.getSource();
				t.stop();
			}
		});
		tb.start();
	}
	// 보스 움직이는 함수 
	@Override
	protected void moveEnemy() {
		// 화면 밖에 나갔을 떄 절댓값 바꾸기
		if (enemy.x <= 100)
			enemySpeedX = Math.abs(enemySpeedX);
		else if (enemy.x >= MainPanelServer.SCREEN_W - W)
			enemySpeedX = -Math.abs(enemySpeedX);
		else if (enemy.y <= 100)
			enemySpeedY = Math.abs(enemySpeedY);
		else if (enemy.y >= MainPanelServer.SCREEN_H - H - 30)
			enemySpeedY = -Math.abs(enemySpeedY);
		// 밖으로 안나가는 거 확인 후 움직이기
		enemy.x += enemySpeedX;
		enemy.y += enemySpeedY;
	}
}

//총알 클래스 
class Bullet implements Serializable {
	protected Point bullet = new Point(); //총알 위치 
	private int bulletDirection; // 총알 방향 

	protected int W = MainPanelServer.missileImg.getWidth(null);
	protected int H = 8;

	// 플레이어 총알 발사 위치지정
	public void setPlayerBullet(int x, int y) { // 13,8
		W = MainPanelServer.missileImg.getWidth(null);
		H = 8;
		bullet.x = x + 30;
		bullet.y = y + 13;
	}
	// 적 총알 발사 위치 지정
	public void setEnemyBullet(int x, int y) { // 17,10
		W = MainPanelServer.missileEnemyImg.getWidth(null);
		H = MainPanelServer.missileEnemyImg.getHeight(null);
		bullet.x = x - 12;
		bullet.y = y + 13;
	}
	// 보스 총알 발사 위치 지정 
	public void setBossRocket(int x, int y) { // 49,15
		bulletDirection = (int) (Math.random() * 3);
		W = MainPanelServer.rocketImg.getWidth(null);
		H = 15;
		// System.out.println(W + " " + H);
		bullet.x = x - 12;
		bullet.y = y + 55;
	}
	// 총알 움직이는 함수 
	public void moveBullet(int sx) { // 총알 움직이는함수
		bullet.x += sx;
	}
	// 보스 로켓 움직이는 함수 
	public void moveRocket(int sx, int sy) { // 총알 움직이는함수
		bullet.x += sx;
		// 각도 조절 (3갈래 중 하나)
		if (bulletDirection == 0)
			bullet.y += sy;
		else if (bulletDirection == 1)
			bullet.y += 0;
		else if (bulletDirection == 2)
			bullet.y -= sy;
	}
}
// 배경 움직이는 클래스 
class Background {
	static private Image mainImg = new ImageIcon("imgs/gameBackground.jpg").getImage();
	private static int bg1X = 0;
	private static int bg2X = 1000; // SCREEN_W가 1000이라고 가정
	private static final int BG_SPEED = 5;

	Background() {
		moveBg();
	}

	// 배경 움직이는 함수
	private void moveBg() {
		// setLayout(null);
		Timer bgTimer = new Timer(100, e -> {
			bg1X -= BG_SPEED;
			bg2X -= BG_SPEED;
			// 만약에 창 밖으로 나가면 대기열에 대기해
			if (bg1X <= -MainPanelServer.SCREEN_W) {
				bg1X = MainPanelServer.SCREEN_W;
			}
			if (bg2X <= -MainPanelServer.SCREEN_W) {
				bg2X = MainPanelServer.SCREEN_W;
			}
		});
		bgTimer.start();
	}

	// 실제로 배경을 그려주는 함수 (어느 패널에서든 호출 가능)
	public static void draw(Graphics g, JPanel panel) {
		g.drawImage(mainImg, bg1X, 0, 1000, 600, panel);
		g.drawImage(mainImg, bg2X, 0, 1000, 600, panel);
	}
}

//폭발 클래스 
class Explo {
	Point explo;
	int idx = 0;
	int cellW, cellH;

	// 생성자에서 이미지를 직접 받거나, 이미지를 가진 패널을 받습니다.
	Explo(Image img, int x, int y, int w, int h) {
		// this.targetPanel = panel;
		this.explo = new Point(x, y);

		// 이미지 정보를 안전하게 가져옵니다.
		this.cellW = (img.getWidth(null) / 17) + w - 32;
		this.cellH = img.getHeight(null) + h - 32;

	}

	// 폭발 이미지 바뀌는 함수
	public void exploTimer() {
		Timer exT = new Timer(40, e -> { // 0.4ms

			idx++; // 0.4초 마다 인덱스 바꾸면서 다시 그리기
			if (idx >= 15) { // 마지막까지 그렸으면 멈추기
				((Timer) e.getSource()).stop();
				MainPanelServer.explostions.remove(this);
			}
		});
		exT.start();
	}
}
