package client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;
import common.GameData;

//수신 스레드 
public class WriteThreadClient extends Thread {
	DataOutputStream dos; // 입력 스트림
	GameData gameData = new GameData(); // 보낼 데이터 만들기

	public WriteThreadClient(DataOutputStream dos) {
		this.dos = dos;
	}

	@Override
	public void run() {
		try {
			while (true) {
				// 읽기 전에 다 null 처리
				gameData.events.clear();
				// 게임 데이터로 옮기기
				// player2 위치만
				gameData.p2.playerX = MainPanelClient.playerObj2.player.x;
				gameData.p2.playerY = MainPanelClient.playerObj2.player.y;
				// 클라이언트에서 총쏘는 이벤트 리스트
				for (int e : MainPanelClient.events) {
					gameData.events.add(e);
				}
				// GameData 전송(to server).
				// player2 위치
				dos.writeInt(gameData.p2.playerX);
				dos.writeInt(gameData.p2.playerY);
				// 이벤트들
				int enventsSize = gameData.events.size();
				dos.writeInt(enventsSize);
				for (int e : gameData.events) {
					dos.writeInt(e);
				}
				MainPanelClient.events.clear();

				dos.flush(); // 입력 스트림 비우기
				Thread.sleep(50); // 스레드 재우기
			}
		} catch (SocketException e) { // 서버중단
			System.out.println("서버가 종료되었음.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally { // 마지막 스트림닫기
			try {
				dos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
