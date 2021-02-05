package site.jx2h.mincraft.hevinhood.pvp;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import site.jx2h.mincraft.hevinhood.pvp.Shop.VillagerShop;

public class ChatCommand implements Listener {
	public class testa implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("비정상 루트");
				return false;
			}
			Player ply = (Player) sender;
			ply.sendMessage(ply.getInventory().getItemInMainHand().getType().toString());
			return true;
		}
	}
	
	public class gamestart implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if (((Player) sender).isOp()) {
				if (Bukkit.getOnlinePlayers().size() < Main._main.getConfig().getInt("startoptions.teams")) {
					sender.sendMessage(TeamVS.profix + "" + ChatColor.RED + "플레이어 수가 팀 수 보다 적어 시작할 수 없습니다.");
					return false;
				}
				if (TeamVS.gamestatus > 0) {
					sender.sendMessage("이미 게임이 진행되고 있습니다.");
					return true;
				}
				Main._game.GameStart();
				return true;
			}
			else {
				sender.sendMessage("권한이 없습니다.");
			}
			return false;
		}
	}
	
	public class gamestop implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if (((Player) sender).isOp()) {
				if (TeamVS.gamestatus < 6) {
					sender.sendMessage(TeamVS.profix + "" + ChatColor.RED + "게임을 정지할 수 없습니다.");
					return false;
				}
				Main._game.GameStop();
				return true;
			}
			else {
				sender.sendMessage("권한이 없습니다.");
			}
			return false;
		}
	}
	
	public class bunker implements CommandExecutor {		
		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			Player ply = (Player) sender;
			if (TeamVS.gamestatus < 6) return false;
			if (ply.isOp() && args.length == 1) {
				Player selply = Bukkit.getPlayer(ChatColor.stripColor(args[0]));
				if (selply == null) {
					ply.sendMessage("플레이어를 찾을 수 없습니다.");
					return true;
				}
				HashMap<String, Object> team = TeamVS.getTeam(selply.getUniqueId());
				if (team == null) {
					ply.sendMessage(ChatColor.GOLD + "아직 어느 팀에 속해 있지 않습니다.");
					return true;
				}
				Location bunkerXYZ = (Location) team.get("bunker");
				if (bunkerXYZ == null) {
					ply.sendMessage(ChatColor.GOLD + "팀장이 아직 초소를 지정하지 않았습니다.");
					return true;
				}
				ply.teleport(bunkerXYZ);
				ply.sendMessage(ChatColor.WHITE + "" + ChatColor.ITALIC + "" + ChatColor.BOLD + "초소로 이동하였습니다.");
				return true;
			}
			else {
				long cooltime;
				if ((cooltime = TeamVS.hasCmd(ply.getUniqueId())) != 0) {
					ply.sendMessage(ChatColor.RED + "피해를 입지 않은 상태를 " + Long.toString(cooltime) + " 초 유지해야 사용가능 합니다.");
					return true;
				}
				HashMap<String, Object> team = TeamVS.getTeam(ply.getUniqueId());
				if (team == null) {
					ply.sendMessage(ChatColor.GOLD + "아직 당신은 어느 팀에 속해 있지 않습니다.");
					return true;
				}
				Location bunkerXYZ = (Location) team.get("bunker");
				if (bunkerXYZ == null) {
					ply.sendMessage(ChatColor.GOLD + "팀장이 아직 초소를 지정하지 않았습니다.");
					return true;
				}
				ply.teleport(bunkerXYZ);
				ply.sendMessage(ChatColor.WHITE + "" + ChatColor.ITALIC + "" + ChatColor.BOLD + "초소로 이동하였습니다.");
				return true;
			}
		}
	}
	
	public class house implements CommandExecutor {		
		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			Player ply = (Player) sender;
			if (TeamVS.gamestatus < 6) return false;
			if (ply.isOp() && args.length == 1) {
				Player selply = Bukkit.getPlayer(ChatColor.stripColor(args[0]));
				if (selply == null) {
					ply.sendMessage("플레이어를 찾을 수 없습니다.");
					return true;
				}
				HashMap<String, Object> team = TeamVS.getTeam(selply.getUniqueId());
				if (team == null) {
					ply.sendMessage(ChatColor.GOLD + "아직 어느 팀에 속해 있지 않습니다.");
					return true;
				}
				Location bunkerXYZ = (Location) team.get("spawn");
				ply.teleport(bunkerXYZ);
				ply.sendMessage(ChatColor.WHITE + "" + ChatColor.ITALIC + "" + ChatColor.BOLD + "팀 스폰지점으로 이동하였습니다.");
				return true;
			}
			else {
				long cooltime;
				if ((cooltime = TeamVS.hasCmd(ply.getUniqueId())) != 0) {
					ply.sendMessage(ChatColor.RED + "피해를 입지 않은 상태를 " + Long.toString(cooltime) + " 초 유지해야 사용가능 합니다.");
					return true;
				}
				HashMap<String, Object> team = TeamVS.getTeam(ply.getUniqueId());
				if (team == null) {
					ply.sendMessage(ChatColor.GOLD + "아직 당신은 어느 팀에 속해 있지 않습니다.");
					return true;
				}
				Location bunkerXYZ = (Location) team.get("spawn");
				ply.teleport(bunkerXYZ);
				ply.sendMessage(ChatColor.WHITE + "" + ChatColor.ITALIC + "" + ChatColor.BOLD + "팀 스폰지점으로 이동하였습니다.");
				return true;
			}
		}
	}
	
	public class gamecheck implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			Player ply = (Player) sender;
			if (((Player) sender).isOp()) {
				if (args.length == 2 && args[0].equals("start") && args[1].equals(Integer.toString(Bukkit.getOnlinePlayers().size()))) {
					if (TeamVS.gamestatus == 1) TeamVS.gamestatus = 2;
					else if (TeamVS.gamestatus == 0) sender.sendMessage("게임이 진행되지 않고 있습니다.");
					else sender.sendMessage("이미 게임이 진행중입니다.");
					return true;
				}
			}
			if (args.length >= 1 && args[0].equals("teamleader")) {
				if (TeamVS.gamestatus == 3) {
					if (args.length == 2 && args[1].equals("canecl")) {
						int i = 0;
						for (Object u : Main._game.votelist) {
							if (ply == Bukkit.getPlayer((UUID) u)) {
								i++;
								Main._game.votelist.remove(u);
								break;
							}
						}
						if (i == 0) sender.sendMessage(TeamVS.profix + "" + ChatColor.RED + "당신은 등록하지 않았습니다.");
						else sender.sendMessage(TeamVS.profix + "" + ChatColor.GREEN + "성공적으로 제외되었습니다.");
						return true;
					}
					int i = 0;
					for (Object u : Main._game.votelist) {
						if (ply == Bukkit.getPlayer((UUID) u)) {
							i++;
							break;
						}
					}
					if (i > 0) {
						sender.sendMessage(TeamVS.profix + "" + ChatColor.RED + "이미 신청하셨습니다. 취소하실려면 아래 명령어를 사용해주세요.\n " + ChatColor.GOLD + "/gamecheck teamleader canecl");
						return true;
					}
					Main._game.votelist.add(ply.getUniqueId());
					sender.sendMessage(TeamVS.profix + "" + ChatColor.WHITE + "정상적으로 등록되었습니다. 투표가 끝날때까지 기달려주세요.");
				}
				else {
					sender.sendMessage(TeamVS.profix + "" + ChatColor.RED + "진행되고 있지 않습니다.");
				}
				return true;
			}
			if (args.length == 1 && args[0].equals("adduser")) {
				if (TeamVS.gamestatus < 6) {
					sender.sendMessage(TeamVS.profix + "" + ChatColor.RED + "게임이 진행중이 아닙니다.");
					return true;
				}
				Main._game.CheckRecipe_AddUser(ply);
				return true;
			}
//			if (args.length == 1 && args[0].equals("adduser")) {
//				return true;
//			}
			sender.sendMessage("알 수 없는 명령어.");
			return false;
		}
	}
	
	public class gamemenu implements TabExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command _cmd, String label, String[] args) {
			Player ply = (Player) sender;
			if (!ply.isOp()) {
				ply.sendMessage(ChatColor.RED + "당신은 게임에 영향을 주는 이 명령어를 사용할 수 없습니다.");
				return true;
			}
//			if (TeamVS.gamestatus < 6) {
//				ply.sendMessage(ChatColor.RED + "게임이 시작되지 않아 데이터에 접근할 수 없습니다.");
//				return true;
//			}
			if (args.length < 1) {
				ply.sendMessage(ChatColor.RED + "/gamemenu <command> [option...]");
				return false;
			}
			String cmd = args[0];
			switch (cmd) {
				case "bunker_change": {
					if (TeamVS.gamestatus < 6) break;
					if (args.length < 2) {
						ply.sendMessage(ChatColor.RED + "팀 ID 누락.");
						return true;
					}
					HashMap<String, Object> team = null;
					try {
						for (HashMap<String, Object> t : TeamVS.teams) {
							if (Integer.parseInt(args[1]) == (int) t.get("id")) {
								team = t;
							}
						}
					}
					catch (Exception e) {
						ply.sendMessage(ChatColor.RED + "[오류] 팀 색인에 실패했습니다.");
						return true;
					}
					if (team == null) {
						ply.sendMessage(ChatColor.RED + "팀을 찾을 수 없습니다.");
						return true;
					}
					TeamVS.setTeam((UUID) team.get("leader"), "bunker", ply.getLocation());
					ply.sendMessage(ChatColor.GREEN + "자신의 위치로 해당 팀의 초소를 지정 및 변경하였습니다.");
					break;
				}
				case "supplybox": {
					if (TeamVS.gamestatus < 6) break;
					TeamVS.SupplyBox box = Main._game.new SupplyBox(ply.getTargetBlock(null, 100).getLocation());
					box.Drop();
					ply.sendMessage(ChatColor.GREEN + "바라보는 방향에 보급상자를 생성했습니다.");
					break;
				}
				case "backup": {
					if (args.length > 1) {
						if (args[1].equals("save")) {
							TeamVS.BackUp.Save();
						}
						else if (args[1].equals("load") && args.length > 2) {
							try {
								TeamVS.BackUp.Load(Integer.parseInt(args[2]));
							}
							catch (Exception e) {
								e.printStackTrace();
								ply.sendMessage(ChatColor.RED + "[오류] 숫자로 변환에 실패했습니다.");
							}
						}
						else {
							ply.sendMessage(ChatColor.RED + "알 수 없음.");
						}
					}
					else {
						ply.sendMessage(ChatColor.RED + "save|load 옵션이 필요합니다.");
					}
					break;
				}
				case "item": {
					if (TeamVS.gamestatus < 6) break;
					if (args.length >= 2) {
						int num;
						int amount;
						try {
							num = Integer.parseInt(args[1]);
							amount = Integer.parseInt((args.length > 2) ? args[2] : "1");
						}
						catch (Exception e) {
							ply.sendMessage(ChatColor.RED + "[오류] 숫자로 변환에 실패했습니다.");
							break;
						}
						Inventory pi = ply.getInventory();
						if (num == 101) pi.addItem(TeamVS.Items.Vaccine(amount));
						if (num == 102) pi.addItem(TeamVS.Items.Ticket(amount));
						if (num == 103 && args.length > 3) {
							int id;
							try {
								id = Integer.parseInt(args[3]);
							} catch (Exception e) {
								ply.sendMessage(ChatColor.RED + "[오류] 숫자로 변환에 실패했습니다. args[3]");
								break;
							}
							HashMap<String, Integer> beds = new HashMap<>();
							beds.put("1", 14);
							beds.put("2", 11);
							beds.put("3", 4);
							pi.addItem(TeamVS.Items.SpawnSet(beds.get(Integer.toString(id)), 1));
						}
						if (num == 999) pi.addItem(TeamVS.Items.Test(1));
						ply.sendMessage(ChatColor.GREEN + "정상적으로 아이템을 지급하였습니다.");
					}
					else ply.sendMessage(ChatColor.RED + "[커스텀 아이템코드] 옵션이 필요합니다.");
					break;
				}
				case "team_change": {
					if (TeamVS.gamestatus < 6) break;
					if (args.length > 2) {
						Player se1 = Bukkit.getPlayer(ChatColor.stripColor(args[1]));
						Player se2 = Bukkit.getPlayer(ChatColor.stripColor(args[2]));
						if (se1 == null || se2 == null) {
							ply.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다.");
							break;
						}
						TeamVS.changeTeam(se1.getUniqueId(), se2.getUniqueId());
						ply.sendMessage(ChatColor.GREEN + "데이터상에서 팀을 이동시켰습니다. 직접 house 입력 필요.");
					}
					else {
						ply.sendMessage(ChatColor.RED + "<이동될 플레이어 이름> <이동되는 팀의 아무 플레이어 이름> 이 옵션으로 필요합니다.");
					}
					break;
				}
				case "player": {
					if (TeamVS.gamestatus < 6) break;
					if (args.length == 2) {
						Player se = Bukkit.getPlayer(ChatColor.stripColor(args[1]));
						if (se == null) {
							ply.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다.");
							break;
						}
						ply.sendMessage("[플레이어 데이터 조회] " + se.getName() + "님의 현재 데이터입니다.");
						for (Entry<String, Object> v : TeamVS.getData(se.getUniqueId().toString()).entrySet()) {
							Object value = v.getValue();
							if (v.getValue() instanceof Integer) {
								value = Integer.toString((int) value);
							}
							if (v.getValue() instanceof Long) {
								value = Long.toString(((Long) value));
							}
							ply.sendMessage(String.format("%s: %s", v.getKey(), (String) value));
						}
						ply.sendMessage("[플레이어 데이터 조회] 출력 끝.");
					}
//					else if (args.length == 4) {
//						
//					}
					else ply.sendMessage(ChatColor.RED + "player <플레이어 이름> 옵션이 필요합니다.");
					break;
				}
				case "worldborder": {
					if (TeamVS.gamestatus < 6) break;
					if (args.length > 2) {
						int size;
						int time;
						try {
							size = Integer.parseInt(args[1]);
							time = Integer.parseInt(args[2]);
						} catch (Exception e) {
							ply.sendMessage(ChatColor.RED + "[오류] 숫자로 변환 실패했습니다.");
							break;
						}
						if (size < 2000) {
							TeamVS.SpawnAllChange();
						}
						Bukkit.getWorld("world").getWorldBorder().setSize(size, time);
						Bukkit.broadcastMessage(TeamVS.profix + "맵 활동 범위가 " + args[1] + "으로 변경되었습니다. 경계선 밖에 있는 경우 사망하오니 맵 가운데로 신속히 이동바랍니다.");
					}
					break;
				}
				case "status": {
					if (args.length > 1) {
						int i;
						try {
							i = Integer.parseInt(args[1]);
						} catch (Exception e) {
							ply.sendMessage(ChatColor.RED + "[오류] 숫자 변환에 실패했습니다.");
							break;
						}
						TeamVS.gamestatus = i;
						ply.sendMessage(ChatColor.GREEN + "정상적으로 변경되었습니다.");
					} else ply.sendMessage(ChatColor.RED + "<status> 옵션이 필요합니다.");
					break;
				}
				case "gamemode": {
					if (args.length > 1) {
						int i;
						try {
							i = Integer.parseInt(args[1]);
						} catch (Exception e) {
							ply.sendMessage(ChatColor.RED + "[오류] 숫자 변환에 실패했습니다.");
							break;
						}
						TeamVS.gamemode = i;
						ply.sendMessage(ChatColor.GREEN + "정상적으로 변경되었습니다.");
					} else ply.sendMessage(ChatColor.RED + "<[int] mod> 옵션이 필요합니다.");
					break;
				}
				case "shop": {
					if (args.length > 1) {
						try {
							Class<VillagerShop> b = Shop.VillagerShop.class;
							Method a = b.getMethod(args[1], Location.class);
							a.invoke(b, ply.getTargetBlock(null, 100).getLocation());
							ply.sendMessage(ChatColor.GREEN + "정상적으로 배치했습니다.");
						} catch (Exception e) {
							e.printStackTrace();
							ply.sendMessage(ChatColor.RED + "찾을 수 없습니다.");
						}
					} else ply.sendMessage(ChatColor.RED + "<[String] id> 옵션이 필요합니다.");
					break;
				}
				case "adduser": {
					if (TeamVS.gamestatus < 6) break;
					if (args.length > 2) {
						Player sel = Bukkit.getPlayer(ChatColor.stripColor(args[1]));
						Player to = Bukkit.getPlayer(ChatColor.stripColor(args[2]));
						if (sel == null || to == null) {
							ply.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다.");
							break;
						}
						TeamVS.delTeam(sel.getUniqueId());
						Main._game.UserAdd(to, sel);
						ply.sendMessage(ChatColor.GREEN + "플레이어를 성공적으로 상대 팀에게 추가시켰습니다.");
					}
					break;
				}
				case "say": {
					if (args.length > 1) {
						String[] a = args.clone();
						a = Arrays.copyOfRange(a, 1, a.length);
						TeamVS.sendBroadcast(String.join(" ", a));
						Bukkit.getOnlinePlayers().forEach(p -> {
							p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
						});
					}
					break;
				}
				case "deluser": { 
					if (TeamVS.gamestatus < 6) break;
					if (args.length > 1) {
						OfflinePlayer sel;
						try {
							sel = Bukkit.getOfflinePlayer(UUID.fromString(ChatColor.stripColor(args[1])));
						}
						catch (Exception e) {
							ply.sendMessage(ChatColor.RED + "UUID를 인식할 수 없습니다. (내부 오류)");
							break;
						}
						if (sel == null) {
							ply.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다.");
							break;
						}
						TeamVS.delTeam(sel.getUniqueId());
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + sel.getName() + " parent set default");
						ply.sendMessage(ChatColor.GREEN + "성공적으로 플레이어를 팀에서 추방시켰습니다.");
					}
					break;
				}
				case "team": {
					if (TeamVS.gamestatus < 6) break;
					if (args.length > 1) {
						int tid;
						try {
							tid = Integer.parseInt(args[1]);
						}
						catch (Exception e) {
							ply.sendMessage(ChatColor.RED + "숫자 변환 실패");
							break;
						}
						for (HashMap<String, Object> a : TeamVS.teams) {
							if ((int) a.get("id") == tid) {
								ply.sendMessage(ChatColor.WHITE + "팀 정보 출력.");
								for (Entry<String, Object> b : a.entrySet()) {
									ply.sendMessage(ChatColor.WHITE + b.getKey() + ": " + b.getValue().toString());
								}
								for (Object b : (ArrayList<?>) a.get("players")) {
									ply.sendMessage(ChatColor.WHITE + "플레이어 찾음: " + ((Bukkit.getPlayer((UUID) b) != null) ? Bukkit.getPlayer((UUID) b).getName() : ((UUID) b).toString()));
								}
								ply.sendMessage(ChatColor.WHITE + "팀 정보 출력끝.");
								return true;
							}
						}
						ply.sendMessage(ChatColor.RED + "팀을 찾을 수 없습니다.");
					}
					break;
				}
				default: {
					ply.sendMessage(ChatColor.RED + "알 수 없는 명령어.");
					return false;
				}	
			}
			return true;
		}

		@Override
		public List<String> onTabComplete(CommandSender sender, Command com, String label, String[] args) {
			if (args.length == 1) {
				List<String> a = new ArrayList<>();
				a.add("bunker_change");
				a.add("worldborder");
				a.add("team_change");
				a.add("item");
				a.add("supplybox");
				a.add("backup");
				a.add("player");
				a.add("status");
				a.add("gamemode");
				a.add("shop");
				a.add("adduser");
				a.add("say");
				a.add("deluser");
				a.add("team");
				return a;
			}
			return null;
		}
	}
	
	public class quickCommand implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
			Player ply = (Player) sender;
			if (args.length > 0) {
				switch (args[0]) {
				case "팀장":
					ply.performCommand("gamecheck teamleader");
					break;
				case "기지":
					ply.performCommand("house");
					break;
				case "초소":
					ply.performCommand("bunker");
					break;
				case "팀원뽑기":
					ply.performCommand("gamecheck adduser");
					break;
				default:
					ply.sendMessage(ChatColor.RED + "알 수 없는 단축 명령어.");
					break;
				}
			}
			return false;
		}
		
	}
	
//	public static void sendGlowingBlock(Player p, Location loc, long lifetime){
//        Bukkit.getScheduler().scheduleSyncDelayedTask(Main._main, () -> {
//            PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
//            EntityShulker shulker = new EntityShulker(((CraftWorld) loc.getWorld()).getHandle());
//            shulker.setLocation(loc.getX() + .5, loc.getY() - 1, loc.getZ() + .5, 0, 0);
//            shulker.setFlag(6, true); //Glow
//            shulker.setFlag(5, true); //Invisibility
//            PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving(shulker);
//            connection.sendPacket(spawnPacket);
//            Bukkit.getScheduler().scheduleSyncDelayedTask(Main._main, () -> {
//                PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(shulker.getId());
//                connection.sendPacket(destroyPacket);
//            }, lifetime + 200L);
//        }, (long) ((Math.random() + 1) * 10));
//    }
}