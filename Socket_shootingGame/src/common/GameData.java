package common;



import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;

public class GameData implements Serializable {
	
	public PlayerData p1 = new PlayerData();
	public PlayerData p2 = new PlayerData();
	public ArrayList<Integer> events = new  ArrayList<>();
	public ArrayList<Point> hpItems = new ArrayList<>();
	
	public ArrayList<Point> explostions = new ArrayList<>();
	public ArrayList<EnemyData> enemiesData  = new ArrayList<>();
	
	public int score;
	public int bossScore;
	
	public class PlayerData implements Serializable{
		//Point player = new Point();
		public ArrayList<Point> playerBullets = new ArrayList<>();
		public int playerX;
		public int playerY;
		public int hp;
		public boolean isInvicible = false; // 무적일때 true
		public boolean isVisible = true; 
	}
	public class EnemyData implements Serializable{
		public int enemyX;
		public int enemyY;
		public int hp;
		public int type; //0: Lv1 / 1: Lv2 / 2: Lv3
		public boolean isCheckedBar; // 부딪혔을때 
		public boolean isLife;
		public int bulletType = 0; //0: 일반, 1: 보스 로켓
		public ArrayList<Point> enemyBullets = new ArrayList<>();
		
		public boolean isHit;
		public EnemyData(int x, int y , int type, int hp, boolean isCheckedBar, boolean isLife){
			this.enemyX = x;
			this.enemyY = y;
			this.hp = hp;
			this.type = type;
			this.isCheckedBar = isCheckedBar;
			this.isLife = isLife;
			//this.bulletType = bulletType;
		}
		
		
	}
	
}