package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketException;

import javax.swing.SwingUtilities;

//받는 스레드 
public class ReadThreadServer extends Thread{
	DataInputStream dis; //입력 스트림 
	public ReadThreadServer(DataInputStream dis) {
		this.dis = dis;
	}
	//쓰레드 실행 함수 
	@Override
	public void run() {
			try {
				while(true){
					//순서대로 받아야됨.
					// 1. GameData 풀어서 원래 변수에 담기.
					// 플레이어2 위치 
					MainPanelServer.playerObj2.player.x = dis.readInt();
					MainPanelServer.playerObj2.player.y = dis.readInt();

					// 클라이언트에서 발생하는 이벤트 배열 받기 
					int eventsSize = dis.readInt();
					for(int i=0; i<eventsSize; i++) {
						if(dis.readInt() == 1) {
							//이건 안보내고 그냥 받으면 바로 쏘기 
							MainPanelServer.playerObj2.appearBullet();
						}
					}
				}
			} catch(SocketException e) { // 소켓 중단 예외처리  
				System.out.println("클라이언트가 나갔습니다.");
				// 클라이언트가 나갈 시 클라이언트 (플레이어2) 탈락 
				MainPanelServer.playerObj2.isVisible = false;
				MainPanelServer.playerObj2.isInvicible = true;
				MainPanelServer.playerObj2.hp = -1;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if(dis != null)//스트림 닫기 
						dis.close(); 
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
		}
	}


