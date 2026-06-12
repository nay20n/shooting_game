package server;


import java.awt.Point;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;

import javax.swing.SwingUtilities;

import common.GameData;
import common.GameData.EnemyData;

//수신 스레드 
public class WriteThreadServer extends Thread{
	DataOutputStream dos; // 출력스트림 
	GameData gameData;
	public WriteThreadServer(DataOutputStream dos) {
		this.dos = dos;
		System.out.println("쓰는 서버  스레드 실행돼습니다.");
	}
	@Override
	public void run() {
		try {
			gameData = new GameData();
			while(true) {
				//1. 읽기 전에 다 null 처리 
				gameData.enemiesData.clear();
				gameData.hpItems.clear();
				gameData.p1.playerBullets.clear();
				gameData.p2.playerBullets.clear();
				gameData.explostions.clear();
				
				//2. 게임 데이터로 옮기기 
				gameData.score = MainPanelServer.st.score; //스코어 
				gameData.bossScore = MainPanelServer.st.bossCnt; //보스스코어 
				//P1
				gameData.p1.playerX = MainPanelServer.playerObj1.player.x; //플레이어1 위치 
				gameData.p1.playerY = MainPanelServer.playerObj1.player.y;
				gameData.p1.hp = MainPanelServer.playerObj1.hp;///플레이어2 위치 
				gameData.p1.isInvicible = MainPanelServer.playerObj1.isInvicible; //플레이어1 무적 
				gameData.p1.isVisible = MainPanelServer.playerObj1.isVisible; //플레이어1 보이게 할건지 
				//playerObj1의 bullets
				for(Bullet b : MainPanelServer.playerObj1.bullets) {
					gameData.p1.playerBullets.add(b.bullet);
				}
				
				//P2
				gameData.p2.isInvicible = MainPanelServer.playerObj2.isInvicible;
				gameData.p2.isVisible = MainPanelServer.playerObj2.isVisible;
				gameData.p2.hp = MainPanelServer.playerObj2.hp;
				//playerObj2의 bullets
				for(Bullet b : MainPanelServer.playerObj2.bullets) {
					gameData.p2.playerBullets.add(b.bullet);
				}
		
				//체력 증강 아이템 배열 
				for(Point p : MainPanelServer.hpItems) {
					gameData.hpItems.add(p);
				}
				
				//ㅇ[ㅔ너미; 
				for(Enemy em : MainPanelServer.enemies) {
					int type = -1;;
					if(em instanceof Lv1Enemy) type = 0;
					else if(em instanceof Lv2Enemy) type = 1;
					else if(em instanceof Lv3Enemy) type = 2;
					else if(em instanceof Boss) {
						type = 3;
					}
					//에너미 위치, 타입, 체력, chedckedBar, isLife을 가지고 에너미 데이터 객체 새엉 
					EnemyData ed = gameData.new EnemyData(em.enemy.x, em.enemy.y, type, em.hp, em.isCheckedBar, em.isLife);
					//
					if (em instanceof Boss) ed.bulletType = 1; 
				    else ed.bulletType = 0;
					
					for(Bullet b : em.bullets) {
						ed.enemyBullets.add(new Point(b.bullet.x, b.bullet.y));
					}
					gameData.enemiesData.add(ed);
				}
				//폭발 사진 위치 가져오기 
				for(Explo e : MainPanelServer.explostions) {
					if(e.idx==0)
						gameData.explostions.add(e.explo);
				}

				// 3. GameData 전송(to client).
				dos.writeInt(gameData.score);
				dos.writeInt(gameData.bossScore);
				
				//player1의 위치 (x,y)값과 hp값 
				dos.writeInt(gameData.p1.playerX);
				dos.writeInt(gameData.p1.playerY);
				dos.writeInt(gameData.p1.hp);
				dos.writeBoolean(gameData.p1.isInvicible);
				dos.writeBoolean(gameData.p1.isVisible);
				//player bullets의 리스트 크기와 위치 (x,y) 값 하나씩 보내기 
				int player1BulletsSize = gameData.p1.playerBullets.size();
				dos.writeInt(player1BulletsSize);
				for(Point p : gameData.p1.playerBullets) {
					dos.writeInt(p.x);
					dos.writeInt(p.y);
				}
				
				dos.writeInt(gameData.p2.hp);
				dos.writeBoolean(gameData.p2.isInvicible);
				dos.writeBoolean(gameData.p2.isVisible);
				int player2BulletsSize = gameData.p2.playerBullets.size();
				dos.writeInt(player2BulletsSize);
				for(Point p : gameData.p2.playerBullets) {
					dos.writeInt(p.x);
					dos.writeInt(p.y);
				}
				
				//hp item의 리스트 크기와 위치 (x,y) 값 하나씩 보내기  
				int hpItemsSize = gameData.hpItems.size();
				//Thread.sleep(5000);
				dos.writeInt(hpItemsSize);
				for(Point p : gameData.hpItems) {
					dos.writeInt(p.x);
					dos.writeInt(p.y);
				}
				
				//Enemy의 리스트 크기와 위치 
				int enemiesSize = gameData.enemiesData.size();
				dos.writeInt(enemiesSize);
				for(EnemyData ed : gameData.enemiesData) {
					dos.writeInt(ed.type);
					dos.writeBoolean(ed.isLife);
					dos.writeInt(ed.enemyX);
					dos.writeInt(ed.enemyY);
					dos.writeInt(ed.hp);
					dos.writeBoolean(ed.isCheckedBar);
					dos.writeInt(ed.bulletType);
					int enemyBulletsSize = ed.enemyBullets.size();
					dos.writeInt(enemyBulletsSize);
					for(Point p : ed.enemyBullets) {
						dos.writeInt(p.x);
						dos.writeInt(p.y);	
					}
				}
				//폭발 위치 
				int explosionsSize = gameData.explostions.size();
				dos.writeInt(explosionsSize);
				for(Point p : gameData.explostions) {
					dos.writeInt(p.x);
					dos.writeInt(p.y);
				}
				
				dos.flush();
				Thread.sleep(50);
			}
		} catch (SocketException e) {
	        System.out.println("클라이언트가 나갔습니다. 통신을 종료합니다.");
	       
	    } catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				dos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}


