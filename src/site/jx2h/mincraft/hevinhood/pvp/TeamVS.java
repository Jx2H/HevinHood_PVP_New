package site.jx2h.mincraft.hevinhood.pvp;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.minecraft.server.v1_12_R1.EntityShulker;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_12_R1.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_12_R1.PlayerConnection;

public class TeamVS implements Listener {
	// 0 = 대기
	// 1 = 게임 준비
	// 2 = 게임 승인
	// 3 = 팀장 투표 시작
	// 4 = 팀장 투표 끝
	// 5 = 경기 준비
	// 6 = 경기 중
	// 7 = 보급상자 드랍됨
	public static int gamestatus = 0;
	public static int gamespawnc = 0;
	public static int gamemode = 0;
	
	public static ArrayList<HashMap<String, Object>> teams = new ArrayList<>();
	
	public ArrayList<Object> votelist = new ArrayList<>();
	public ArrayList<HashMap<String, Object>> vaccinesave = new ArrayList<>();
	public ArrayList<HashMap<String, Object>> bedsave = new ArrayList<>();
	public ArrayList<HashMap<String, Object>> playerdatas = new ArrayList<>();
	// NEW
	public List<UUID> players = new ArrayList<>();
	
	public static String profix = String.format("%s%s[%s안내%s]%s ", ChatColor.BOLD, ChatColor.AQUA, ChatColor.GOLD, ChatColor.AQUA, ChatColor.WHITE);
	public static Shop _shop;
	
	public TeamVS() {
		_shop = new Shop();
		Main._main.getServer().getPluginManager().registerEvents(this, Main._main);
	}
	
	public void GameStart() {
		sendBroadcast("곧, 게임을 시작합니다!");
		gamestatus = 1;
		for (Player a : Bukkit.getOnlinePlayers()) players.add(a.getUniqueId());
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main._main, () -> {
			sendBroadcast("관리자에게 게임 진행을 위해 승인을 요청 중 입니다..");
			Bukkit.getOnlinePlayers().forEach(p -> {
				if (p.isOp()) {
					p.sendMessage(profix + "" + ChatColor.RED + "게임을 진행하는데 승인이 필요합니다. 아래 명령어를 사용해주세요.\n " + ChatColor.GOLD + "/gamecheck start " + players.size());
				}
			});
		}, 3 * 20L);
		GameStartCheckTask gt = new GameStartCheckTask();
		gt.runTaskTimer(Main._main, 0, 1 * 20L);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main._main, () -> {
			gt.stoped = 1;
			if (gamestatus < 2) {
				sendBroadcast("승인이 되지 않아 게임이 취소되었습니다.");
				gamestatus = 0;
			}
		}, 30 * 20L);
	}
	
	private class GameStartCheckTask extends BukkitRunnable {
		public int stoped = 0;
	    @Override
	    public void run() {
	    	if (stoped != 0) this.cancel();
	    	if (gamestatus == 2) {
	    		sendBroadcast("관리자가 본 게임을 승인하였습니다.");
	    		Bukkit.getScheduler().scheduleSyncDelayedTask(Main._main, () -> {
	    			TeamVote();
	    		}, 20L);
	    		this.cancel();
	    	}
	    }

	}
	
	public void TeamVote() {
		int SET_tests = Main._main.getConfig().getInt("startoptions.teams");
		if (SET_tests == players.size()) {
			sendBroadcast("최소 팀 수와 플레이어 수가 동일하기에 투표는 진행되지 않았습니다.");
			gamestatus = 4;
			VoteTeamSet();
			return;
		}
		sendBroadcast("팀장이 되고 싶은 사람은 아래 명령어를 사용해 후보에 오르실 수 있습니다. 그렇지 않은 사람은 사용하지 마십시오.\n" + ChatColor.GREEN + " 제한시간: 120초\n " + ChatColor.GOLD + "/qc 팀장");
		gamestatus = 3; // 시스템 상태 변경
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main._main, () -> {
			if (gamestatus >= 4) return;
			sendBroadcast("팀장 후보 투표가 약 60초 남았습니다.\n투표 수: " + votelist.size());
		}, 60 * 20L);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main._main, () -> {
			gamestatus = 4;
			sendBroadcast(ChatColor.GREEN + "팀장 후보 투표가 종료되었습니다.");
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main._main, () -> {
				VoteTeamSet();
			}, 10 * 20L);
		}, 120 * 20L);
	}
	
	private void VoteTeamSet() {
		int SET_tests = Main._main.getConfig().getInt("startoptions.teams");
		int i = 1;
		List<Player> olds = ToPlayers();
		int ci = SET_tests;
		for (Object u : votelist) {
			ci--;
			Player ply = Bukkit.getPlayer((UUID) u);
			if (ply == null) continue;
			olds.remove(ply);
			HashMap<String, Object> info = new HashMap<>();
			info.put("id", i++);
			info.put("leader", (UUID) u);
			List<UUID> plys = new ArrayList<>();
			plys.add((UUID) u);
			info.put("players", plys);
			teams.add(info);
		}
		if (teams.size() < SET_tests) {
			if (Bukkit.getOnlinePlayers().size() < SET_tests) {
				sendBroadcast(ChatColor.RED + "투표 최소 결과값이 팀 수보다 적어 게임이 취소되었습니다.");
				return;
			}
			for (int _i = 0; _i < ci; _i++) {
				int set = (int) (Math.random() * olds.size());
				HashMap<String, Object> info = new HashMap<>();
				info.put("id", i++);
				Player selectply = olds.get(set);
				List<UUID> plys = new ArrayList<UUID>();
				plys.add(selectply.getUniqueId());
				olds.remove(selectply);
				info.put("leader", selectply.getUniqueId());
				info.put("players", plys);
				teams.add(info);
				votelist.add(selectply.getUniqueId());
			}
		}
		else if (votelist.size() > SET_tests) {
			int si = votelist.size() - SET_tests;
			for (int _i = 0; _i < si; _i++) {
				int set = (int) Math.random() * teams.size();
				UUID seid = (UUID) votelist.get(set);
				HashMap<String, Object> t = getTeam(seid);
				teams.remove(t);
				votelist.remove(seid);
			}
			if (teams.size() == SET_tests) {
				int ti = SET_tests;
				// id 재 갱신
				for (HashMap<String, Object> t : teams) {
					t.put("id", ti--);
				}
			}
			else {
				gamestatus = 0;
				sendBroadcast(ChatColor.RED + "팀장을 선정하는데 예상한 데이터와 일치되지 않아 취소되었습니다. (내부 오류)");
				return;
			}
		}
		String bmsg = "";
		for (Object u : votelist) {
			Player ply = Bukkit.getPlayer((UUID) u);
			bmsg += "\n - ";
			bmsg += (ply != null) ? ply.getName() : "Unknown";
		}
		sendBroadcast(ChatColor.GREEN + "투표 결과, 다음과 같은 플레이어는 팀장으로 입명되었습니다." + ChatColor.WHITE + bmsg);
		votelist = new ArrayList<>();
		GameReady();
	}
	
	// 경기 시작전 초기화
	public void GameReady() {
		ConsoleCommandSender console = Bukkit.getConsoleSender();
		for (HashMap<String, Object> v : teams) {
			int teamNum = (int) v.get("id");
			Bukkit.dispatchCommand(console, "lp user " + Bukkit.getPlayer((UUID) v.get("leader")).getName() + " parent set team" + teamNum);
		}
		sendBroadcast("이제, 게임을 시작합니다!\n시작은 팀장이 되신 분들만 이동됩니다.");
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main._main, () -> {
			new BukkitRunnable() {
				int count = 3;
				@Override
				public void run() {
					if (count < 1) {
						GameRun();
						this.cancel();
						return;
					}
					sendBroadcast(ChatColor.GREEN + "텔레포트 " + count-- + " 초전");
					Bukkit.getOnlinePlayers().forEach(p -> {
						p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
					});
				}
			}.runTaskTimer(Main._main, 5 * 20L, 20L);
		}, 1 * 20L);
	}
	
	public void GameRun() {
		gamestatus = 5; // 게임 준비
		// 플레이어 데이터 초기화
		for (Player p : ToPlayers()) {
			HashMap<String, Object> data = new HashMap<>();
			data.put("ply", p.getUniqueId().toString());
			data.put("taint", Main._main.getConfig().getInt("startoptions.taint"));
			if (hasLeader(p.getUniqueId())) {
				data.put("useradds", 0);
			}
			playerdatas.add(data);
		}
		
		List<String> spawn_list = Main._main.getConfig().getStringList("startoptions.spawn-list");
		int ti = spawn_list.size() - 1;
		for (HashMap<String, Object> ts : teams) {
			if (ti < 0) break; // 이 코드는 스폰리스트가 팀수보다 많을때 일어남.
			String v = spawn_list.get(ti--);
			String[] xyz = v.split(",+");
			double x = Integer.parseInt(xyz[0]) + .5;
			double y = Integer.parseInt(xyz[1]);
			double z = Integer.parseInt(xyz[2]) + .5;
			Location spawnLacation = new Location(Bukkit.getWorld("world"), x, y, z);
			ts.put("spawn", spawnLacation);
			Player leader = Bukkit.getPlayer((UUID) ts.get("leader"));
			leader.teleport(spawnLacation);
			leader.setLevel(0);
			leader.setGameMode(GameMode.SURVIVAL);
			leader.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "게임이 시작되었습니다. 최대한 빠른 시간내 아래 조건을 맞추어 팀원을 데려오세요.\n "
				+ ChatColor.RESET + "" + ChatColor.GOLD
				+ "첫번째 조건: " 
				+ ChatColor.AQUA
				+ "Level 7 + 금괴 1개\n "
				+ ChatColor.GOLD 
				+ "두번째 조건: " 
				+ ChatColor.AQUA
				+ "Level 15 + 철블록 3개\n "
				+ ChatColor.GOLD 
				+ "세번째 조건: " 
				+ ChatColor.AQUA
				+ "Level 18 + 돌 10세트\n " 
				+ ChatColor.GRAY + "" + ChatColor.ITALIC
				+ "('조약돌' --화로--> '돌')\n ('섬세한 손길' 곡괭이 사용시 '돌' 드랍)\n\n"
				+ ChatColor.RESET + "" + ChatColor.YELLOW
				+ "팀원 추가 티켓 구매 명령어:\n"
				+ ChatColor.LIGHT_PURPLE
				+ "/qc 팀원뽑기\n"
			);
			leader.getEnderChest().clear();
			Inventory leaderInv = leader.getInventory();
			leaderInv.clear();
			HashMap<String, Integer> beds = new HashMap<>();
			beds.put("1", 14);
			beds.put("2", 11);
			beds.put("3", 4);
			leaderInv.setItem(7, Items.SpawnSet((int) beds.get(Integer.toString((int) ts.get("id"))), 1));
			leaderInv.setItem(8, Items.Vaccine(Main._main.getConfig().getInt("items.101.set")));
		}
		// 경계선 지정
		World world = Bukkit.getWorld("world");
		WorldBorder wb = world.getWorldBorder();
		int wb_x = Main._main.getConfig().getInt("startoptions.center-x");
		int wb_z = Main._main.getConfig().getInt("startoptions.center-z");
		wb.setCenter((double) wb_x + .5, (double) wb_z + .5);
		wb.setSize(2000);
		wb.setWarningDistance(5);
		wb.setWarningTime(5);
		wb.setDamageAmount(0.1);
		wb.setDamageBuffer(10);
		// 월드 상태 초기화
		world.setDifficulty(Difficulty.NORMAL);
		world.setTime(0);
		world.setStorm(false);
		world.setThundering(false);
		ToPlayers().forEach(p -> {
			p.setCompassTarget(world.getBlockAt(wb_x, 100, wb_z).getLocation());
		});
		gamestatus = 6; // 게임 중 상태 변경
	}
	
	public void GameStop() {
		teams = new ArrayList<>();
		vaccinesave = new ArrayList<>();
		playerdatas = new ArrayList<>();
		players = new ArrayList<>();
		WorldBorder wb = Bukkit.getWorld("world").getWorldBorder();
		wb.setCenter(Bukkit.getWorld("world").getSpawnLocation());
		wb.setSize(60000000);
		ConsoleCommandSender console = Bukkit.getConsoleSender();
		sendBroadcast(ChatColor.GREEN + "플러그인 개발자, Jx2H.\nhttps://github.com/Jx2H");
		for (Player ply : Bukkit.getOnlinePlayers()) {
			ply.getInventory().clear();
			ply.teleport(Bukkit.getWorld("spawn").getSpawnLocation());
			ply.setLevel(0);
			if (ply.isOp()) {
				Bukkit.dispatchCommand(console, "lp user " + ply.getName() + " parent set admin");
				ply.setGameMode(GameMode.CREATIVE);
			}
			else {
				Bukkit.dispatchCommand(console, "lp user " + ply.getName() + " parent set default");
				ply.setGameMode(GameMode.SURVIVAL);
				ply.getEnderChest().clear();
			}
		}
		gamestatus = 0;
	}
	
	public List<Player> ToPlayers() {
		if (players.size() == 0) return null;
		List<Player> olds = new ArrayList<>();
		for (UUID v : players) {
			Player p = Bukkit.getPlayer(v);
			if (p == null) continue;
			olds.add(p);
		}
		return olds;
	}
	
	public static boolean hasLeader(UUID uuid) {
		int ti = 0;
		for (HashMap<String, Object> t : teams) {	
			if (uuid.equals((UUID) t.get("leader"))) ti++;
		}
		if (ti == 0) return false;
		else return true;
	}
	
	public static HashMap<String, Object> getTeam(UUID uuid) {
		for (HashMap<String, Object> t : teams) {
			for (Object team : (ArrayList<?>) t.get("players")) {
				if (uuid.equals(team)) {
					return t;
				}
			}
		}
		return null;
	}
	
	public static boolean setTeam(UUID uuid, String key, Object value) {
		for (HashMap<String, Object> t : teams) {
			for (Object ply : (ArrayList<?>) t.get("players")) {
				if (uuid.equals((UUID) ply)) {
					t.put(key, value);
					return true;
				}
			}
		}
		return false;
	}
	
	public static String changeTeam(UUID fromply, UUID toply) {
		HashMap<String, Object> newteam = getTeam(toply); // 킬한 놈 데이터 가져옴
		HashMap<String, Object> team = getTeam(fromply); // 당한 놈 데이터 가져옴
		List<UUID> newplys = new ArrayList<>();
		List<UUID> plys = new ArrayList<>();
		
		for (Object v : (ArrayList<?>) newteam.get("players")) {
			newplys.add((UUID) v);
		}
		
		if (newplys.contains(fromply)) return "[처리되지 않은 데이터] 이미 상대팀에 들어가 있습니다.";
		
		for (Object v : (ArrayList<?>) team.get("players")) {
			plys.add((UUID) v);
		}
		
		plys.remove(fromply);
		setTeam(fromply, "players", plys);

		newplys.add(fromply);
		setTeam(toply, "players", newplys);
		
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + Bukkit.getPlayer(fromply).getName() + " parent set team" + ((int) newteam.get("id")));
		
		if (hasLeader(fromply)) {
			if (plys.size() == 0) {
				teams.remove(team);
				HashMap<String, String> clist = getTeamColors();
				sendBroadcast(clist.get(Integer.toString((int) team.get("id"))) + "팀이 멸망했습니다.");
				return null;
			}
			else setTeam(fromply, "leader", (UUID) plys.get(0));
		}
		return null;
	}
	
	public static int delTeam(UUID ply) {
		HashMap<String, Object> t = getTeam(ply);
		if (t == null) return 0;
		List<UUID> plys = new ArrayList<>();
		for (Object v : (ArrayList<?>) t.get("players")) plys.add((UUID) v);
		plys.remove(ply);
		if (plys.size() == 0) {
			teams.remove(t);
			return (int) t.get("id");
		}
		if (hasLeader(ply)) {
			setTeam(ply, "leader", (UUID) plys.get(0));
		}
		setTeam(ply, "players", plys);
		return 0;
	}
	
	public static HashMap<String, Object> getData(String uuid) {
		for (HashMap<String, Object> ps : Main._game.playerdatas) {
			if (uuid.equals((String) ps.get("ply"))) return ps;
		}
		return null;
	}
	
	public static boolean setData(String uuid, String key, Object value) {
		for (HashMap<String, Object> ps : Main._game.playerdatas) {
			if (uuid.equals((String) ps.get("ply"))) {
				ps.put(key, value);
				return true;
			}
		}
		return false;
	}
	
	public static void delData(String uuid) {
		for (HashMap<String, Object> ps : Main._game.playerdatas) {
			if (uuid.equals((String) ps.get("ply"))) {
				Main._game.playerdatas.remove(ps);
			}
		}
	}
	
	public static HashMap<String, String> getTeamColors() {
		HashMap<String, String> cllist = new HashMap<>();
		cllist.put("1", ChatColor.RED + "빨강");
		cllist.put("2", ChatColor.BLUE + "파랑");
		cllist.put("3", ChatColor.YELLOW + "노랑");
		return cllist;
	}
	
	public static long hasCmd(UUID ply) {
		HashMap<String, Object> d = getData(ply.toString());
		if (d == null || d.get("ncmd") == null) return 0;
		Long time = (Long) d.get("ncmd");
		if (time == null) return 0;
		long seti = System.currentTimeMillis() / 1000;
		long sett = Main._main.getConfig().getLong("startoptions.notcmd");
		if ((time.longValue() + sett) > seti) return (time.longValue() + sett) - seti;
		return 0;
	}
	
	public static void SpawnAllChange() {
		if (gamespawnc == 1) return;
		gamespawnc = 1;
		String[] newspawn = Main._main.getConfig().getString("startoptions.endspawn").split(",+");
		double x = Integer.parseInt(newspawn[0]) + .5;
		double y = Integer.parseInt(newspawn[1]);
		double z = Integer.parseInt(newspawn[2]) + .5;
		for (HashMap<String, Object> t : teams) {
			t.put("spawn", new Location(Bukkit.getWorld("world"), x, y, z));
		}
		sendBroadcast("맵 활동 범위가 좁아짐에 따라 스폰 지점이 통합되었습니다.");
	}
	
	public static void sendAdminBroadcast(String m) {
		Bukkit.broadcast(ChatColor.RED + "[시스템] " + ChatColor.WHITE + "" + m, Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
	}
	
	public static void sendBroadcast(String m) {
		Bukkit.broadcastMessage(profix + "" + m);
	}
	
	public void CheckRecipe_AddUser(Player ply) {
		if (!hasLeader(ply.getUniqueId())) {
			ply.sendMessage(profix + "" + ChatColor.RED + "팀장만 사용할 수 있습니다.");
			return;
		}
		HashMap<String, Object> selfdata = getData(ply.getUniqueId().toString());
		if (selfdata == null) {
			ply.sendMessage(profix + "" + ChatColor.RED + "당신의 데이터를 찾을 수 없습니다. 관리자에게 문의하십시오.");
			sendAdminBroadcast(ply.getName() + "님의 데이터 찾을 수 없음. (CheckRecipe_AddUser)");
			return;
		}
		String msg = "번째 팀원을 불러오기에는 재료가 부족합니다.";
		String successMsg = ChatColor.GREEN + "성공적으로 획득하셨습니다. (팀원 뽑기권)";
		if (ply.getInventory().getItemInMainHand().getType() != Material.AIR) {
			ply.sendMessage(profix + "" + ChatColor.RED + "빈 손인 상태에서만 티켓을 받으실 수 있습니다.");
			return;
		}
		int as = (int) selfdata.get("useradds");
		switch (as) {
			case 0: {
				if (Recipe.IF1(ply)) {
					setData(ply.getUniqueId().toString(), "useradds", 1);
					ply.getInventory().setItemInMainHand(Items.Ticket(1));
					ply.sendMessage(successMsg);
				}
				else ply.sendMessage(profix + "" + ChatColor.RED + "첫" + msg);
				break;
			}
			case 1: {
				if (Recipe.IF2(ply)) {
					setData(ply.getUniqueId().toString(), "useradds", 2);
					ply.getInventory().setItemInMainHand(Items.Ticket(1));
					ply.sendMessage(successMsg);
				}
				else ply.sendMessage(profix + "" + ChatColor.RED + "두" + msg);
				break;
			}
			case 2: {
				if (Recipe.IF3(ply)) {
					setData(ply.getUniqueId().toString(), "useradds", 3);
					ply.getInventory().setItemInMainHand(Items.Ticket(1));
					ply.sendMessage(successMsg);
				}
				else ply.sendMessage(profix + "" + ChatColor.RED + "세" + msg);
				break;
			}
			case 3: {
				ply.sendMessage(profix + "" + ChatColor.RED + "당신은 이미 팀 최대 인원 수만큼 팀원을 뽑으셨습니다.");
				break;
			}
		}
	}
	
	public void UserAdd(Player ply, Player selply) {
		// 팀 데이터에 추가
		HashMap<String, Object> team = getTeam(ply.getUniqueId());
		if (team == null) return; // TODO: ?
		List<UUID> plys = new ArrayList<>();
		for (Object v : (ArrayList<?>) team.get("players")) {
			plys.add((UUID) v);
		}
		plys.add(selply.getUniqueId());
		setTeam(ply.getUniqueId(), "players", plys);
		
		// 그룹이동
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + selply.getName() + " parent set team" + ((int) team.get("id")));
		// if 중도 참가한 플레이어 데이터 초기화
		if (getData(selply.getUniqueId().toString()) == null) {
			HashMap<String, Object> data = new HashMap<>();
			data.put("ply", selply.getUniqueId().toString());
			data.put("taint", Main._main.getConfig().getInt("startoptions.taint"));
			playerdatas.add(data);
			players.add(selply.getUniqueId());
			int wb_x = Main._main.getConfig().getInt("startoptions.center-x");
			int wb_z = Main._main.getConfig().getInt("startoptions.center-z");
			selply.setCompassTarget(new Location(Bukkit.getWorld("world"), wb_x, 100, wb_z));
		} else {
			// 사망 유저 고려
			setData(selply.getUniqueId().toString(), "taint", Main._main.getConfig().getInt("startoptions.taint"));
		}
		// 상태 초기화
		selply.getInventory().clear();
		selply.getEnderChest().clear();
		selply.teleport(ply.getLocation());
		selply.setLevel(0);
		selply.setGameMode(GameMode.SURVIVAL);
		// 백신 지급
		selply.getInventory().setItem(8, Items.Vaccine(Main._main.getConfig().getInt("items.101.set")));
		ply.sendMessage(profix + selply.getName() + "님은 이제 당신의 팀원입니다.");
		HashMap<String, String> cllist = getTeamColors();
		selply.sendMessage(ChatColor.WHITE + "당신은 이제 " + ((String) cllist.get(Integer.toString((int) team.get("id")))) + ChatColor.WHITE + "팀 입니다.");
		sendAdminBroadcast(ChatColor.RED + "팀원추가/" + ((String) cllist.get(Integer.toString((int) team.get("id")))) + "팀이 " + selply.getName() + "님을 데려갔습니다.");
	}
	
	public void changeTeamCheck(Player player, Player killer) {
		HashMap<String, Object> data = getData(player.getUniqueId().toString());
		// 승리
		if (teams.size() <= 2 && ((ArrayList<?>) getTeam(player.getUniqueId()).get("players")).size() == 1) {
			delTeam(player.getUniqueId());
			HashMap<String, String> clist = getTeamColors();
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_TWINKLE, 1, 1);
				p.sendTitle("게임이 종료되었습니다", "최종 우승팀은 " + clist.get(Integer.toString((int) teams.get(0).get("id"))) + "팀 입니다", 10, 5 * 20, 20);
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main._main, () -> {
				GameStop();
			}, 10 * 20L);
			return;
		}
		
		int taint = (int) data.get("taint");
		if (taint-- < 1 && gamemode != 1) {
			int dti = delTeam(player.getUniqueId());
			if (dti > 0) {
				HashMap<String, String> clist = getTeamColors();
				sendBroadcast(clist.get(Integer.toString(dti)) + "팀이 멸망했습니다.");
			}
			sendAdminBroadcast(ChatColor.RED + "사망/" + player.getName());
			new BukkitRunnable() {
				@Override
				public void run() {
					if (player.getHealth() > 0) {
						this.cancel();
						player.getInventory().clear();
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent set default");
						player.sendTitle(ChatColor.RED + "사망하였습니다", null,
								10, 3 * 20, 20);
						player.setGameMode(GameMode.SPECTATOR);
						Bukkit.getScheduler().scheduleSyncDelayedTask(Main._main, () -> {
							player.teleport(killer.getLocation());
						}, 20L);
					}
				}
			}.runTaskTimer(Main._main, 0, 20L);
			return;
		}
		if (gamemode == 1) taint++;
		setData(player.getUniqueId().toString(), "taint", taint);
		String reason = changeTeam(player.getUniqueId(), killer.getUniqueId());
		if (reason != null) {
			player.sendMessage(profix + "" + ChatColor.RED + reason + "\n관리자에게 문의하십시오.");
			sendAdminBroadcast(player.getName() + "/" + reason);
			return;
		}
		sendAdminBroadcast("감염/" + player.getName() + "님이 " + Integer.toString((int) getTeam(killer.getUniqueId()).get("id")) + "팀으로 이동되었습니다.");
		new BukkitRunnable() {
			@Override
			public void run() {
				if (player.getHealth() > 0) {
					HashMap<String, Object> kteam = getTeam(killer.getUniqueId());
					if (kteam == null) {
						this.cancel();
						return;
					}
					HashMap<String, String> cllist = getTeamColors();
					player.sendTitle(ChatColor.RED + "감염되었습니다", ChatColor.WHITE + "당신은 이제 " + cllist.get(Integer.toString((int) kteam.get("id"))) + ChatColor.WHITE + "팀으로 이동되었습니다", 10, 3 * 20, 20);
					this.cancel();
				}
			}
		}.runTaskTimer(Main._main, 0, 20L);
	}
	
	public static class Recipe {
		private static boolean save(Player ply, int level, int _count, Material mate) {
			int igi = 0;
			int count = _count;
			if (ply.getLevel() < level) return false;
			for (ItemStack item : ply.getInventory().getContents()) {
				if (item != null && item.getType() == mate) {
					igi += item.getAmount();
				}
			}
			if (igi == 0 || igi < count) return false;
			for (ItemStack item : ply.getInventory().getContents()) {
				if (item != null && item.getType() == mate) {
					if (item.getAmount() >= count) {
						item.setAmount(item.getAmount() - count);
						return true;
					}
					count = count - item.getAmount();
					item.setAmount(0);
					if (count == 0) return true;
				}
			}
			return false;
		}
		
		public static boolean IF1(Player ply) {
			return save(ply, 7, 1, Material.GOLD_INGOT);
		}
		
		public static boolean IF2(Player ply) {
			return save(ply, 15, 3, Material.IRON_BLOCK);
		}
		
		public static boolean IF3(Player ply) {
			return save(ply, 18, 640, Material.STONE);
		}
	}
	
	public static class Items {
		public static int getId(ItemStack item) {
			if (item.hasItemMeta()) {
				net.minecraft.server.v1_12_R1.ItemStack ns = CraftItemStack.asNMSCopy(item);
				NBTTagCompound tags = ns.getTag();
				if (!tags.hasKey("hvp_item")) return 0;
				return tags.getInt("hvp_item");
			}
			return 0;
		}
		
		private static ItemStack setId(ItemStack item, int id) {
			net.minecraft.server.v1_12_R1.ItemStack ns = CraftItemStack.asNMSCopy(item);
			NBTTagCompound tags = ns.getTag();
			if (tags == null) tags = new NBTTagCompound();
			tags.setInt("hvp_item", id);
			ns.setTag(tags);
			return CraftItemStack.asBukkitCopy(ns);
		}
		
		private static ItemStack CreateItem(int id, int amount, Material base) {
			ItemStack a = new ItemStack(base, amount);
			a = Items.setId(a, id);
			return a;
		}
		
		public static ItemStack Vaccine(int amount) {
			// INFO
			int NUM = 101;
			Material base = Material.DRAGONS_BREATH;
			String NAME = ChatColor.BOLD + "" + ChatColor.AQUA + "백신";
			List<String> LORE = Arrays.asList(
				"감염을 " 
				+ ChatColor.YELLOW + ChatColor.ITALIC 
				+ "1회 " 
				+ ChatColor.RESET 
				+ "예방한다"
				,
				ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH  
				+ "구하기 힘들다는 백신이다"
			);
			// INFO END
			ItemStack i = Items.CreateItem(NUM, amount, base);
			ItemMeta m = i.getItemMeta();
			m.setDisplayName(NAME);
			m.setLore(LORE);
			m.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 7, true);
			i.setItemMeta(m);
			return i;
		}
		
		public static ItemStack Ticket(int amount) {
			// INFO
			int NUM = 102;
			Material base = Material.PAPER;
			String NAME = ChatColor.YELLOW + "팀원 뽑기권";
			List<String> LORE = Arrays.asList(
				ChatColor.MAGIC 
				+ "누굴"
				+ ChatColor.RESET + "" + ChatColor.AQUA
				+ " 뽑을지 신중하게 정하시고 선택하세요."
				,
				ChatColor.GRAY + "" + ChatColor.ITALIC 
				+ "사용시 당신의 위치로 텔레포트됩니다."
			);
			// INFO END
			ItemStack i = Items.CreateItem(NUM, amount, base);
			ItemMeta m = i.getItemMeta();
			m.setDisplayName(NAME);
			m.setLore(LORE);
			m.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
			i.setItemMeta(m);
			return i;
		}
		
		public static ItemStack SpawnSet(int color, int amount) {
			// INFO
			int NUM = 103;
			Material base = Material.BED;
			String NAME = ChatColor.DARK_PURPLE + "초소 지정권";
			List<String> LORE = Arrays.asList(
				ChatColor.WHITE 
				+ "어디든 이동이 가능한 초소를 지정해준다."
				,
				ChatColor.GOLD + "" + ChatColor.UNDERLINE
				+ "1회"
				+ ChatColor.RESET + "" + ChatColor.WHITE
				+ " 밖에 못 사용하니 활동 범위가 좁아질 때"
				,
				ChatColor.WHITE
				+ "사용하는 것이 좋겠다."
				,
				ChatColor.GRAY + "" + ChatColor.ITALIC 
				+ "적 근처로 이동하면 아무래도 싸움이 잦아들겠지.."
			);
			// INFO END
			ItemStack i = Items.CreateItem(NUM, amount, base);
			ItemMeta m = i.getItemMeta();
			i.setDurability((short) color);
			m.setDisplayName(NAME);
			m.setLore(LORE);
			m.addEnchant(Enchantment.LOOT_BONUS_MOBS, 4, true);
			i.setItemMeta(m);
			return i;
		}
		
		public static ItemStack Money(int amount) {
			// INFO
			int NUM = 300;
			Material base = Material.PAPER;
			String NAME = ChatColor.AQUA + "수표";
			List<String> LORE = Arrays.asList(
				ChatColor.WHITE 
				+ "(상급) 게임에서 쓰이는 화폐이다."
				,
				ChatColor.WHITE
				+ "경기 내 "
				+ ChatColor.GOLD + "" + ChatColor.ITALIC
				+ "공용 상점"
				+ ChatColor.RESET + "" + ChatColor.WHITE
				+ "에서만 소비할 수 있다."
				,
				ChatColor.GRAY + "" + ChatColor.ITALIC 
				+ "상대 팀과 교류할 때 쓰이기도 한다."
			);
			// INFO END
			ItemStack i = Items.CreateItem(NUM, amount, base);
			ItemMeta m = i.getItemMeta();
			m.setDisplayName(NAME);
			m.setLore(LORE);
			m.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 5, true);
			i.setItemMeta(m);
			return i;
		}
		
		public static ItemStack GOLD_Money(int amount) {
			// INFO
			int NUM = 301;
			Material base = Material.GOLD_NUGGET;
			String NAME = ChatColor.AQUA + "골드";
			List<String> LORE = Arrays.asList(
				ChatColor.WHITE 
				+ "(중급) 게임에서 쓰이는 화폐이다."
				,
				ChatColor.WHITE
				+ "경기 내 "
				+ ChatColor.GOLD + "" + ChatColor.ITALIC
				+ "공용 상점"
				+ ChatColor.RESET + "" + ChatColor.WHITE
				+ "에서만 소비할 수 있다."
				,
				ChatColor.GRAY + "" + ChatColor.ITALIC 
				+ "상대 팀과 교류할 때 쓰이기도 한다."
			);
			// INFO END
			ItemStack i = Items.CreateItem(NUM, amount, base);
			ItemMeta m = i.getItemMeta();
			m.setDisplayName(NAME);
			m.setLore(LORE);
			m.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 5, true);
			i.setItemMeta(m);
			return i;
		}
		
		public static ItemStack IRON_Money(int amount) {
			// INFO
			int NUM = 302;
			Material base = Material.IRON_NUGGET;
			String NAME = ChatColor.AQUA + "실버";
			List<String> LORE = Arrays.asList(
				ChatColor.WHITE 
				+ "(하급) 게임에서 쓰이는 화폐이다."
				,
				ChatColor.WHITE
				+ "경기 내 "
				+ ChatColor.GOLD + "" + ChatColor.ITALIC
				+ "공용 상점"
				+ ChatColor.RESET + "" + ChatColor.WHITE
				+ "에서만 소비할 수 있다."
				,
				ChatColor.GRAY + "" + ChatColor.ITALIC 
				+ "상대 팀과 교류할 때 쓰이기도 한다."
			);
			// INFO END
			ItemStack i = Items.CreateItem(NUM, amount, base);
			ItemMeta m = i.getItemMeta();
			m.setDisplayName(NAME);
			m.setLore(LORE);
			m.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 5, true);
			i.setItemMeta(m);
			return i;
		}
		
		public static ItemStack Test(int amount) {
			ItemStack i = Items.CreateItem(999, 1, Material.FISHING_ROD);
			ItemMeta m = i.getItemMeta();
			m.addEnchant(Enchantment.LUCK, 255, true);
			m.addEnchant(Enchantment.LURE, 3, true);
			i.setItemMeta(m);
			return i;
		}
	}
	
	public class UserAddGui implements Listener {
		private Inventory inv;
		public int clicked = 0;
		public Player checked;

	    public UserAddGui() {
	        inv = Bukkit.createInventory(null, 9 * 2, ChatColor.BLACK + "원하시는 팀원을 선택해주세요");
	        Main._main.getServer().getPluginManager().registerEvents(this, Main._main);
	        initializeItems();
	    }

	    public void initializeItems() {
	    	for (ItemStack i : createHeadItem()) {
	    		inv.addItem(i);
	    	}
	    	
	    }

	    protected List<ItemStack> createHeadItem() {
	    	List<ItemStack> heads = new ArrayList<>();
	    	for (Player u : Bukkit.getOnlinePlayers()) {
	    		if (u.isOp()) continue;
	    		int i = 0;
	    		for (HashMap<String, Object> t : teams) {
	    			for (Object p : (ArrayList<?>) t.get("players")) if (u.getUniqueId().equals(p)) i++;
	    		}
	    		if (i == 0) {
	    			ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
	    			SkullMeta headMeta = (SkullMeta) head.getItemMeta();
	    			headMeta.setOwningPlayer(Bukkit.getOfflinePlayer(u.getUniqueId()));
	    			headMeta.setDisplayName(ChatColor.WHITE + u.getName());
	    			head.setItemMeta(headMeta);
	    			heads.add(head);
	    		}
	    	}
	    	return heads;
	    }

	    public void openInventory(final HumanEntity ent) {
	        ent.openInventory(inv);
	    }
	    
	    @EventHandler
	    public void onInventoryClick(InventoryClickEvent e) {
	    	if (!e.getInventory().getName().equals(inv.getName())) return;
	    	if (e.getInventory().getHolder() != inv.getHolder()) return;
	    	if (clicked > 0) return;
	    	e.setCancelled(true);
	    	Player ply = (Player) e.getWhoClicked();
	    	ItemStack item = e.getCurrentItem();
	    	if (item == null || !item.hasItemMeta()) return;
	    	Player sel = Bukkit.getPlayer(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
	    	checked = sel;
	    	clicked++;
	    	Bukkit.getScheduler().scheduleSyncDelayedTask(Main._main, () -> {
	    		ply.closeInventory();
	    	});
	    }
	    
	    @EventHandler
	    public void onInventoryDrag(InventoryDragEvent e) {
	    	if (e.getInventory().getHolder() != inv.getHolder()) return;
	    	if (clicked > 0) return;
	    	e.setCancelled(true);
	    }
	    
	    @EventHandler
	    public void onInventoryClose(InventoryCloseEvent e) {
	    	if (e.getInventory().getHolder() != inv.getHolder()) return;
	    	clicked++;
	    }
	}
	
	public class SupplyBox implements Listener {
		private final Location xyz;
		private Block box;
		private EntityShulker shulker;
		private int stayed = 0;
		private BossBar bb = Bukkit.createBossBar(ChatColor.BOLD + "보급상자가 떨어졌습니다", BarColor.RED, BarStyle.SOLID);
		
		public SupplyBox(Location xyz) {
			xyz.setY(xyz.getY() + 1);
			this.xyz = xyz;
			Main._main.getServer().getPluginManager().registerEvents(this, Main._main);
		}
		
		public void Drop() {
			Block target = xyz.getBlock();
			target.setType(Material.CHEST);
			Chest inbox = (Chest) target.getState();
			inbox.setCustomName("보급상자");
			inbox.update();
			initialize(inbox.getInventory());
			this.box = target;
			gamestatus = 7;
			// 안내
			Bukkit.getOnlinePlayers().forEach(p -> {
				bb.addPlayer(p);
				p.setCompassTarget(xyz); // 나침반 방향 지정
				sendGlowingBlock(p, xyz);
			});
			bb.setTitle(ChatColor.BOLD + "보급상자가 떨어졌습니다");
			bb.setVisible(true);
			
			// 채팅
			sendBroadcast("보급상자가 떨어졌습니다.\n"
				+ ChatColor.BLUE
				+ " - 위치: "
				+ ChatColor.GOLD
				+ String.format("%d %d %d\n", (int) xyz.getX(), (int) xyz.getY(), (int) xyz.getZ())
				+ ChatColor.GRAY + ChatColor.ITALIC
				+ "보급상자를 누군가 픽업시 상단 바가 사라집니다!");
			
			// 빈 슬릇 확인 타이머
			new BukkitRunnable() {
				@Override
				public void run() {
					if (xyz.getBlock().getType() == Material.AIR) {
						this.cancel();
						return;
					}
					Chest inbox = (Chest) xyz.getBlock().getState();
					int i = 0;
					for (ItemStack is : inbox.getInventory().getContents()) {
						if (is != null) i++;
					}
					if (i == 0) {
						stay();
						this.cancel();
					}
				}
			}.runTaskTimer(Main._main, 0, 100L);
		}
		
		private void initialize(Inventory inv) {
			inv.addItem(Items.Vaccine(1));
			if (Math.random() > 0.5) inv.addItem(new ItemStack(Material.ORANGE_SHULKER_BOX));
			if (Math.random() > 0.5) inv.addItem(new ItemStack(Material.TOTEM));
			int i1 = (int) (Math.random() * 3);
			if (i1 == 0) {
				inv.addItem(new ItemStack(Material.IRON_INGOT, (int) (Math.random() * 20) + 1));
			}
			else if (i1 == 1) {
				inv.addItem(new ItemStack(Material.CAKE));
			}
			else if (i1 >= 2) {
				inv.addItem(new ItemStack(Material.ENDER_PEARL, (int) (Math.random() * 10) + 1));
			}
			String profix = ChatColor.GOLD + "[보급품] " + ChatColor.WHITE;
			
			Object[][] sets = {
				{
					Material.POTION,
					PotionEffectType.INVISIBILITY,
					3 * 60,
					0,
					"은신 물약"
				},
				{
					Material.POTION,
					PotionEffectType.HEAL,
					0,
					1,
					"회복약"
				},
				{
					Material.POTION,
					PotionEffectType.HEAL,
					0,
					4,
					"고급 회복약"
				},
				{
					Material.POTION,
					PotionEffectType.REGENERATION,
					15,
					1,
					"소독약"
				},
				{
					Material.POTION,
					PotionEffectType.REGENERATION,
					15,
					2,
					"고급 소독약"
				},
				{
					Material.SPLASH_POTION,
					PotionEffectType.HARM,
					0,
					0,
					"E-1 수류탄"
				},
				{
					Material.SPLASH_POTION,
					PotionEffectType.HARM,
					0,
					1,
					"E-2 수류탄"
				},
				{
					Material.LINGERING_POTION,
					PotionEffectType.POISON,
					5,
					0,
					"COVID-19 연구 샘플",
					new ArrayList<String>(Arrays.asList("지속시간: 5초"))
				},
				{
					Material.LINGERING_POTION,
					PotionEffectType.POISON,
					10,
					1,
					"B.1.1.7(변이바이러스) 연구 샘플",
					new ArrayList<String>(Arrays.asList("지속시간: 10초"))
				}
			};
			
			List<ItemStack> potions = new ArrayList<>();
			potions.add(new ItemStack(Material.GOLDEN_APPLE));
			
			for (Object[] o : sets) {
				ItemStack p1 = new ItemStack((Material) o[0]);
				PotionMeta p1m = (PotionMeta) p1.getItemMeta();
				p1m.addCustomEffect(
					new PotionEffect((PotionEffectType) o[1], ((int) o[2]) * 20, ((int) o[3]))
					, false);
				p1m.setDisplayName(profix + (String) o[4]);
				if (o.length == 6) {
					List<String> lore = new ArrayList<>();
					for (Object v : (ArrayList<?>) o[5]) lore.add((String) v);
					p1m.setLore(lore);
				}
				p1.setItemMeta(p1m);
				potions.add(p1);
			}
			
			for (int i = 0; i < 2; i++) {
				int random = (int) (Math.random() * (sets.length));
				inv.addItem(potions.get(random));
				potions.remove(random);
			}
			
			// 방어구
			Object[][] armors = {
				{
					Material.IRON_HELMET,
					"투구",
					Enchantment.PROTECTION_ENVIRONMENTAL
				},
				{
					Material.IRON_CHESTPLATE,
					"갑옷",
					Enchantment.PROTECTION_ENVIRONMENTAL,
					Enchantment.PROTECTION_PROJECTILE
				},
				{
					Material.IRON_LEGGINGS,
					"다리",
					Enchantment.PROTECTION_ENVIRONMENTAL
				},
				{
					Material.IRON_BOOTS,
					"부츠",
					Enchantment.PROTECTION_FALL
				}
			};
			
			List<ItemStack> armor = new ArrayList<>();
			for (Object[] set : armors) {
				ItemStack p1 = new ItemStack((Material) set[0]);
				ItemMeta p1m = p1.getItemMeta();
				p1m.setDisplayName(profix + (String) set[1]);
				p1m.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
				for (int i = 2; i < set.length; i++) {
					int rm = (int) (Math.random() * (3 + 1)) + 1;
					p1m.addEnchant((Enchantment) set[i], rm, true);
				}
				p1.setItemMeta(p1m);
				armor.add(p1);
			}
			
			for (int i = 0; i < 2; i++) {
				int rm = (int) (Math.random() * armor.size());
				inv.addItem(armor.get(rm));
				armor.remove(rm);
			}
		}
		
		public void stay() {
			if (stayed++ > 0) return;
			gamestatus = 6;
			bb.setVisible(false);
			sendBroadcast(profix
				+ "누군가 보급상자를 열었습니다.\n"
				+ "다음 보급상자가 있을 수 있으니 다시 도전해보세요!");
			int wb_x = Main._main.getConfig().getInt("startoptions.center-x");
			int wb_z = Main._main.getConfig().getInt("startoptions.center-z");
			Bukkit.getOnlinePlayers().forEach(p -> {
				p.setCompassTarget(Bukkit.getWorld("world").getBlockAt(wb_x, 100, wb_z).getLocation());
	            PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(shulker.getId());
	            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(destroyPacket);
			});
			box.setType(Material.AIR);
		}
		
		public void sendGlowingBlock(Player p, Location loc) {
	        Bukkit.getScheduler().scheduleSyncDelayedTask(Main._main, () -> {
	            PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
	            EntityShulker shulker = new EntityShulker(((CraftWorld) loc.getWorld()).getHandle());
	            shulker.setLocation(loc.getX() + .5, loc.getY() - 1, loc.getZ() + .5, 0, 0);
	            shulker.setFlag(6, true); //Glow
	            shulker.setFlag(5, true); //Invisibility
	            this.shulker = shulker;
	            PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving(shulker);
	            connection.sendPacket(spawnPacket);
//	            Bukkit.getScheduler().scheduleSyncDelayedTask(Main._main, () -> {
//	                PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(shulker.getId());
//	                connection.sendPacket(destroyPacket);
//	            }, lifetime * 20L);
	        }, 20L);
	    }
		
		@EventHandler
		public void onBlockCracked(PlayerInteractEvent e) {
			if (e.getAction() == Action.LEFT_CLICK_BLOCK
				&& e.getHand() == EquipmentSlot.HAND
				&& e.getClickedBlock().getType() == Material.CHEST) {
				Chest inbox = (Chest) e.getClickedBlock().getState();
				if (!(box.getState() instanceof Chest)) return; 
				Chest setbox = (Chest) box.getState();
				if (setbox == null) return;
				if (inbox.equals(setbox)) {
					 e.setCancelled(true);
				}
			}
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK
					&& e.getHand() == EquipmentSlot.HAND
					&& e.getClickedBlock().getType() == Material.CHEST) {
				Chest inbox = (Chest) e.getClickedBlock().getState();
				if (!(box.getState() instanceof Chest)) return;
				Chest setbox = (Chest) box.getState();
				if (setbox == null) return;
				if (inbox.equals(setbox)) {
					setData(e.getPlayer().getUniqueId().toString(), "ncmd", (long) System.currentTimeMillis() / 1000);
				}
			}
		}
		
		@EventHandler
		public void onBoxChecked(InventoryCloseEvent e) {
			if (!(box.getState() instanceof Chest)) return; 
			Chest setbox = (Chest) box.getState();
			if (setbox == null) return;
			if (e.getInventory().equals(setbox.getInventory())) {
				int i = 0;
				for (ItemStack is : e.getInventory().getHolder().getInventory().getContents()) {
					if (is != null) i++;
				}
				if (i == 0) {
					stay();
				}
			}
		}
		
		@EventHandler
		public void onEventReTry(PlayerJoinEvent e) {
			if (gamestatus > 6) {
				Player p = e.getPlayer();
				bb.addPlayer(p);
				p.setCompassTarget(xyz); // 나침반 방향 지정
				sendGlowingBlock(p, xyz);
				p.sendMessage(profix + "(재접속) 보급상자가 떨어졌습니다.\n"
						+ ChatColor.BLUE
						+ " - 위치: "
						+ ChatColor.GOLD
						+ String.format("%d %d %d\n", (int) xyz.getX(), (int) xyz.getY(), (int) xyz.getZ())
						+ ChatColor.GRAY + ChatColor.ITALIC
						+ "보급상자를 누군가 픽업시 상단 바가 사라집니다!");
			}
		}
	}
	
	public static class BackUp {
		private static String path = Main._main.getDataFolder() + "\\backup";
		
		public static void Save() {
			int savetime = (int) (System.currentTimeMillis() / 1000);
			File folder = new File(path);
			if (!folder.exists()) folder.mkdir();
			File f = new File(path, Integer.toString(savetime) + ".json");
			JsonObject ob = new JsonObject();
			ob.addProperty("id", Integer.toString(savetime));
			ob.addProperty("time", new Date(System.currentTimeMillis()).toString());
			ob.addProperty("gamespawnc", (int) gamespawnc);
			JsonArray jts = new JsonArray();
			for (HashMap<String, Object> t : teams) {
				JsonObject tob = new JsonObject();
				tob.addProperty("leader", ((UUID) t.get("leader")).toString());
				tob.addProperty("id", (int) t.get("id"));
				Location spawn = (Location) t.get("spawn");
				tob.addProperty("spawn", String.format("%d,%d,%d", (int) spawn.getX(), (int) spawn.getY(), (int) spawn.getZ()));
				if (t.get("bunker") != null) {
					Location bunker = (Location) t.get("bunker");
					tob.addProperty("bunker", String.format("%d,%d,%d", (int) bunker.getX(), (int) bunker.getY(), (int) bunker.getZ()));
				}
				JsonArray tplys = new JsonArray();
				for (Object p : (ArrayList<?>) t.get("players")) {
					tplys.add(((UUID) p).toString());
				}
				tob.add("players", tplys);
				jts.add(tob);
			}
			ob.add("teams", jts);
			JsonArray jps = new JsonArray();
			for (HashMap<String, Object> ps : Main._game.playerdatas) {
				jps.add(new Gson().toJsonTree(ps).getAsJsonObject());
			}
			ob.add("playerdatas", jps);
			JsonArray jplys = new JsonArray();
			for (UUID u : Main._game.players) {
				jplys.add(u.toString());
			}
			ob.add("players", jplys);
			try {
				FileWriter fw = new FileWriter(f);
				fw.write(ob.toString());
				fw.close();
				sendAdminBroadcast("성공적으로 백업 파일을 생성하였습니다. 생성ID: " + Integer.toString(savetime));
			} catch (IOException e) {
				e.printStackTrace();
				sendAdminBroadcast("백업을 하는 중 오류가 발생되어 취소되었습니다.");
			}
		}
		
		public static void Load(int num) {
			File folder = new File(path);
			if (!folder.exists()) folder.mkdir();
			File f = new File(path, Integer.toString(num) + ".json");
			if (!f.exists()) {
				sendAdminBroadcast(Integer.toString(num) + " 으로 저장된 백업 데이터를 찾지 못하였습니다.");
				return;
			}
			List<String> fr = new ArrayList<>();
			try {
				fr = Files.readAllLines(Paths.get(path, Integer.toString(num) + ".json"), StandardCharsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String data = "";
			for (String d : fr) data += d;
			Type type = new TypeToken<Map<String, Object>>(){}.getType();
			Map<String, Object> json = new Gson().fromJson(data, type);
			gamestatus = 6;
			gamespawnc = ((Double) json.get("gamespawnc")).intValue();
			ArrayList<HashMap<String, Object>> newteam = new ArrayList<>();
			for (Object t : (ArrayList<?>) json.get("teams")) {
				Map<?, ?> r = (Map<?, ?>) t;
				HashMap<String, Object> a = new HashMap<>();
				a.put("leader", UUID.fromString((String) r.get("leader")));
				a.put("id", ((Double) r.get("id")).intValue());
				String[] spawn_xyz = ((String) r.get("spawn")).split(",+");
				a.put("spawn", Bukkit.getWorld("world").getBlockAt(Integer.parseInt(spawn_xyz[0]), Integer.parseInt(spawn_xyz[1]), Integer.parseInt(spawn_xyz[2])).getLocation());
				if (r.get("bunker") != null) {
					String[] bunker_xyz = ((String) r.get("bunker")).split(",+");
					a.put("bunker", Bukkit.getWorld("world").getBlockAt(Integer.parseInt(bunker_xyz[0]), Integer.parseInt(bunker_xyz[1]), Integer.parseInt(bunker_xyz[2])).getLocation());
				}
				List<UUID> ps = new ArrayList<>();
				for (Object c : (ArrayList<?>) r.get("players")) {
					ps.add(UUID.fromString((String) c));
				}
				a.put("players", ps);
				newteam.add(a);
			}
			teams = newteam; // 덮어쓰기
			ArrayList<HashMap<String, Object>> newplydatas = new ArrayList<>();
			for (Object a : (ArrayList<?>) json.get("playerdatas")) {
				Map<?, ?> b = (Map<?, ?>) a;
				HashMap<String, Object> c = new HashMap<>();
				for (Entry<?, ?> d : b.entrySet()) {
					if (((String) d.getKey()).equals("ncmd") || ((String) d.getKey()).equals("cspawn")) {
						c.put((String) d.getKey(), ((Double) d.getValue()).longValue());
						continue;
					}
					if (d.getValue() instanceof Double) {
						c.put((String) d.getKey(), ((Double) d.getValue()).intValue());
					}
					else c.put((String) d.getKey(), d.getValue());
				}
				newplydatas.add(c);
			}
			Main._game.playerdatas = newplydatas;
			List<UUID> newplys = new ArrayList<>();
			for (Object a : (ArrayList<?>) json.get("players")) {
				newplys.add(UUID.fromString((String) a));
			}
			Main._game.players = newplys;
			sendAdminBroadcast("롤백하였습니다.");
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.getPlayer().sendMessage(ChatColor.BLUE + "[" + ChatColor.AQUA + "Hevin Hood's PVP" + ChatColor.BLUE + "]\n"
							+ ChatColor.YELLOW + "환영합니다.");
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main._main, () -> {
			e.getPlayer().sendTitle(ChatColor.GREEN + Main.profix, ChatColor.GOLD + "환영합니다", 10, 3 * 20, 20);
		}, 3 * 20L);
	}

	@EventHandler
	public void onDeathSaveLevel(PlayerDeathEvent e) {
		if (gamestatus < 6) return; 
		e.setKeepLevel(true); // PlayerDeathEvent 이벤트중 딱 한번만 사용, 경험치 보호
	}
	
	@EventHandler
	public void onAttack(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		if (e.getCause() == DamageCause.ENTITY_ATTACK) {
			HashMap<String, Object> team = getTeam(((Player) e.getEntity()).getUniqueId());
			if (team != null) {
				if (gamespawnc == 1) {
					HashMap<String, Object> d = getData(e.getEntity().getUniqueId().toString());
					if (d.get("cspawn") != null) {
						long time = System.currentTimeMillis() / 1000;
						long cooltime = Main._main.getConfig().getLong("startoptions.endspawn-gt");
						if (((long) d.get("cspawn") + cooltime) > time) {
							e.setCancelled(true);
						}
					}
				}
				return;
			}
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onVaccineDeath(PlayerDeathEvent e) {
		if (gamestatus < 6) return; 
		Player player = e.getEntity();
		Player killer = player.getKiller();
		int vaccines = 0;
		for (ItemStack item: e.getDrops()) {
			if (Items.getId(item) == 101) {
				vaccines += item.getAmount();
				item.setType(Material.AIR);
			}
		}
		if (killer != null) {
			HashMap<String, Object> _team = getTeam(killer.getUniqueId());
			if (_team != null) {
				int isteam = 0;
				for (HashMap<String, Object> v : teams) {
					for (Object u : (ArrayList<?>) v.get("players")) {
						if (player.getUniqueId().equals(((UUID) u))) {
							if (((ArrayList<?>) v.get("players")).contains((UUID) killer.getUniqueId())) isteam++;
						}
					}
					
				}
				if (vaccines == 0) {
					if (isteam == 0) {
						changeTeamCheck(player, killer);
						return;
					}
					player.sendMessage(profix + "" + ChatColor.RED + "위험합니다! 백신을 지니고 계세요.");
					return;
				}
				else {
					if (isteam == 0) {
						if (--vaccines == 0) {
							player.sendMessage(profix + "" + ChatColor.RED + "이제 백신이 없습니다. 항상 죽을 때 조심하세요!");
							return;
						}
						else if (vaccines == 1) {
							player.sendMessage(profix + "" + ChatColor.RED + "백신이 이제 한개 남았습니다. 팀원에게 백신을 받거나 조심히 활동 하세요.");
						}
						player.sendMessage(profix + "" + ChatColor.RED + "백신을 사용하여 감염을 물리쳤습니다.");
					}
				}
			}
		}
		HashMap<String, Object> data = new HashMap<>();
		data.put("pid", player.getUniqueId());
		data.put("amount", vaccines);
		vaccinesave.add(data);
	}
	
	@EventHandler
	public void onRespawnVaccineGive(PlayerRespawnEvent e) {
		if (gamestatus < 6) return; 
		Player ply = e.getPlayer();
		for (HashMap<String, Object> v : vaccinesave) {
			if (v.get("pid").equals(ply.getUniqueId())) {
				ply.getInventory().setItem(8, Items.Vaccine((int) v.get("amount")));
				vaccinesave.remove(v);
				break;
			}
		}
	}
	
	@EventHandler
	public void onTicketClick(PlayerInteractEvent e) {
		Player ply = e.getPlayer();
		if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getHand() == EquipmentSlot.HAND) {
			ItemStack it = e.getItem();
			if (it == null) return;
			if (Items.getId(it) == 102) {
				UserAddGui gui = new UserAddGui();
				gui.openInventory(ply);
				new BukkitRunnable() {
					@Override
					public void run() {
						if (gui.clicked > 0) {
							if (gui.checked == null) {
								ply.sendMessage(ChatColor.RED + "사용하지 않으셨습니다. 다시 시도해주세요.");
								this.cancel();
								return;
							}
							UserAdd(ply, gui.checked);
							it.setAmount(it.getAmount() - 1);
							this.cancel();
						}
					}
				}.runTaskTimer(Main._main, 0, 20L);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onStartSpawn(PlayerRespawnEvent e) {
		if (gamestatus < 6) return;
		Player ply = e.getPlayer();
		HashMap<String, Object> teamdata = getTeam(ply.getUniqueId());
		if (teamdata == null) return;
		Location spawnL = (Location) teamdata.get("spawn");
		if (spawnL == null) return;
		setData(e.getPlayer().getUniqueId().toString(), "ncmd", (long) System.currentTimeMillis() / 1000 - (Main._main.getConfig().getLong("startoptions.notcmd")));
		e.setRespawnLocation(spawnL);
		if (gamespawnc == 1) {
			setData(e.getPlayer().getUniqueId().toString(), "cspawn", (long) System.currentTimeMillis() / 1000);
			ply.sendMessage(ChatColor.RED + "[주의] " + ChatColor.WHITE + "스폰 지점이 통합된 상태입니다. 최단 시간내 팀원에게 이동하십시오.\n" + ChatColor.GOLD + "/tpa <팀원 이름> 혹은 /qc 초소");
		}
	}

	@EventHandler
	public void onBedNotDrop(PlayerDropItemEvent e) {
		if (gamestatus < 6) return;
		ItemStack item = e.getItemDrop().getItemStack();
		if (Items.getId(item) == 103) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBedNotDel(PlayerDeathEvent e) {
		if (gamestatus < 6) return;
		Player ply = e.getEntity();
		// if (!hasLeader(ply.getUniqueId())) return;
		for (ItemStack di : e.getDrops()) {
			if (Items.getId(di) == 103) {
				di.setType(Material.AIR);
				HashMap<String, Object> data = new HashMap<>();
				data.put("pid", ply.getUniqueId());
				bedsave.add(data);
			}
		}
	}
	
	@EventHandler
	public void onBedRespawn(PlayerRespawnEvent e) {
		if (gamestatus < 6) return;
		Player ply = e.getPlayer();
		// if (!hasLeader(ply.getUniqueId())) return;
		for (HashMap<String, Object> v : bedsave) {
			if (((UUID) v.get("pid")).equals(ply.getUniqueId())) {
				HashMap<String, Integer> beds = new HashMap<>();
				beds.put("1", 14);
				beds.put("2", 11);
				beds.put("3", 4);
				if (getTeam(ply.getUniqueId()) != null) ply.getInventory().setItem(7, Items.SpawnSet((int) beds.get(Integer.toString((int) getTeam(ply.getUniqueId()).get("id"))), 1));
				bedsave.remove(v);
				break;
			}
		}
	}
	
	@EventHandler
	public void onBedUse(PlayerInteractEvent e) {
		if (gamestatus < 6) return;
		Player ply = e.getPlayer();
		if (!hasLeader(ply.getUniqueId())) return;
		if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getHand() == EquipmentSlot.HAND) {
			ItemStack item = e.getItem();
			if (item == null || !item.hasItemMeta()) return;
			if (Items.getId(item) == 103) {
				HashMap<String, Object> team = getTeam(ply.getUniqueId());
				Location bunker = (Location) team.get("bunker");
				if (bunker == null) {
					item.setAmount(0);
					Location selLocation = ply.getLocation();
					setTeam(ply.getUniqueId(), "bunker", selLocation);
					ply.sendMessage(ChatColor.GREEN + "성공적으로 자신의 위치를 초소로 지정하였습니다.");
				}
				else ply.sendMessage(ChatColor.RED + "이미 초소를 지정하셨습니다. 애기치않은 문제로 아이템이 삭제되지 않았음으로 관리자에게 문의하십시오.");
			}
		}
	}
	
	@EventHandler
	public void onBedMove(InventoryClickEvent e) {
		if (gamestatus < 6) return;
		if (!hasLeader(e.getWhoClicked().getUniqueId())) return;
		if (!(e.getInventory().getHolder() instanceof Chest)) return;
		if (Items.getId(e.getCurrentItem()) == 103) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onTicketMove(InventoryClickEvent e) {
		if (gamestatus < 6) return;
		//if (!hasLeader(e.getWhoClicked().getUniqueId())) return;
		if (!(e.getInventory().getHolder() instanceof Chest)) return;
		if (Items.getId(e.getCurrentItem()) == 102) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onNotCmd(EntityDamageEvent e) {
		if (gamestatus < 6) return;
		if (!(e.getEntity() instanceof Player)) return;
		if (e.getDamage() == 0) return;
		Player ply = (Player) e.getEntity();
		long time = System.currentTimeMillis() / 1000;
		setData(ply.getUniqueId().toString(), "ncmd", time);
	}
}