package client;

import java.awt.Point;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

//받는 스레드 
public class ReadThreadClient extends Thread {
	DataInputStream dis; // 입력 스트림

	public ReadThreadClient(DataInputStream dis) {
		this.dis = dis;
	}

	@Override
	public void run() {
		try {
			while (true) {
				// 순서대로 받아야됨.
				// 1. GameData 풀어서 원래 변수에 담기.
				// 리스트들은 받을 때마다, 본래 갖고 있는 것이랑 안되기 때문에, 리셋 시켜야됨.
				// 점수
				MainPanelClient.st.score = dis.readInt();
				MainPanelClient.st.bossCnt = dis.readInt();
				// player1
				MainPanelClient.playerObj1.player.x = dis.readInt();
				MainPanelClient.playerObj1.player.y = dis.readInt();
				MainPanelClient.playerObj1.hp = dis.readInt();
				MainPanelClient.playerObj1.isInvicible = dis.readBoolean();
				MainPanelClient.playerObj1.isVisible = dis.readBoolean();
				// player1 총알들
				int player1BulletsSize = dis.readInt();
				MainPanelClient.playerObj1.bullets.clear();
				for (int i = 0; i < player1BulletsSize; i++) {
					Bullet p1bullet = new Bullet();
					p1bullet.bullet.x = dis.readInt();
					p1bullet.bullet.y = dis.readInt();
					MainPanelClient.playerObj1.bullets.add(p1bullet);
				}

				// player2
				MainPanelClient.playerObj2.hp = dis.readInt();
				MainPanelClient.playerObj2.isInvicible = dis.readBoolean();
				MainPanelClient.playerObj2.isVisible = dis.readBoolean();
				// player2 총알들
				int player2BulletsSize = dis.readInt();
				MainPanelClient.playerObj2.bullets.clear();
				for (int i = 0; i < player2BulletsSize; i++) {
					Bullet p2bullet = new Bullet();
					p2bullet.bullet.x = dis.readInt();
					p2bullet.bullet.y = dis.readInt();
					MainPanelClient.playerObj2.bullets.add(p2bullet);
				}

				// 아이템들
				int hpItemsSize = dis.readInt(); // 사이즈가 계속 변동되므로 사이즈도 계속 저장
				MainPanelClient.hpItems.clear();
				for (int i = 0; i < hpItemsSize; i++) {
					int x = dis.readInt();
					int y = dis.readInt();
					MainPanelClient.hpItems.add(new Point(x, y));
				}
				// 적들
				int enemiesSize = dis.readInt();
				MainPanelClient.enemies.clear();
				for (int i = 0; i < enemiesSize; i++) {
					int type = dis.readInt(); // 타입 건너받기
					// 타입 선정 type=0 :Lv1 / type=1 :Lv2 / type=2 :Lv2 / type=3 :Boss
					Enemy em;
					if (type == 0)
						em = new Lv1Enemy();
					else if (type == 1)
						em = new Lv2Enemy();
					else if (type == 2)
						em = new Lv3Enemy();
					else
						em = new Boss(MainPanelClient.st);
					em.isLife = dis.readBoolean(); // 적 죽었는지 확인하는변수
					em.enemy.x = dis.readInt(); // 적위치
					em.enemy.y = dis.readInt();
					em.hp = dis.readInt();
					em.isCheckedBar = dis.readBoolean(); // 적의 hp바가 노출 중인지
					// 적의 총알들 (이건 안비워도 됨, 왜냐면 적을 지울 때 같이 지워지니까)
					int bulletType = dis.readInt(); // 보스의 로켓인지, 그냥 적들의 총알인지
					int enemyBulletsSize = dis.readInt();
					for (int j = 0; j < enemyBulletsSize; j++) {
						Bullet enemyBullet = new Bullet();
						enemyBullet.bullet.x = dis.readInt();
						enemyBullet.bullet.y = dis.readInt();
						enemyBullet.bulletType = bulletType;
						em.bullets.add(enemyBullet);
					}
					MainPanelClient.enemies.add(em);
				}
				// 폭탄들
				MainPanelClient.explostions.clear();
				int explosionsSize = dis.readInt();
				for (int i = 0; i < explosionsSize; i++) {
					int x = dis.readInt();
					int y = dis.readInt();
					Explo newExplo = new Explo(MainPanelClient.exploImg, x, y, 32, 32);
					MainPanelClient.explostions.add(newExplo);
					newExplo.exploTimerClient(); // 리스트에 넣을 떄마다 폭발 타이머 시작
				}
			}

		} catch (SocketException e) { // 소켓 중단 예외처리
			System.out.println("ReadThreadClient 서버가 종료되었음.");
			// 서버가 중단 했을 경우 연결 끊기 
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(null, "서버와의 연결이 끊겼습니다. 프로그램을 종료합니다.");
				System.exit(0);
			});

		} catch (IOException e) {
			e.printStackTrace();
		} finally {// 마지막으로
			try {
				if (dis != null) // 스트림 닫기
					dis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}