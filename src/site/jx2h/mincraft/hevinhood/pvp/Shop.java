package site.jx2h.mincraft.hevinhood.pvp;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.metadata.FixedMetadataValue;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Shop implements Listener {
	public Shop() {
		Main._main.getServer().getPluginManager().registerEvents(this, Main._main);
	}
	
	public static boolean hasPos(Location ply) {
		String[] pos1 = Main._main.getConfig().getString("shop.pos1").split(",+");
		String[] pos2 = Main._main.getConfig().getString("shop.pos2").split(",+");
		int[] a = new int[2];
		int[] b = new int[2];
		for (int i = 0; i < 2; i++) a[i] = Integer.parseInt(pos1[i]);
		for (int i = 0; i < 2; i++) b[i] = Integer.parseInt(pos2[i]);
		int x_max = (a[0] >= b[0]) ? a[0] : b[0];
		int z_max = (a[1] >= b[1]) ? a[1] : b[1];
		int x_min = (a[0] < b[0]) ? a[0] : b[0];
		int z_min = (a[1] < b[1]) ? a[1] : b[1];
		Location loc = ply;
		if (loc.getX() <= x_max && loc.getX() >= x_min &&
			loc.getZ() <= z_max && loc.getZ() >= z_min) return true;
		return false;
	}
	
	public static class VillagerShop {
		public static String Profix_Sell = ChatColor.YELLOW + "" + ChatColor.BOLD + "[판매] " + ChatColor.RESET + ChatColor.WHITE;
		public static String Profix_Buy = ChatColor.BLUE + "" + ChatColor.BOLD + "[구매] " + ChatColor.RESET + ChatColor.WHITE;
		
		public static Villager Spawn(Location loc) {
			loc.setY(loc.getY() + 1);
			loc.setX(loc.getX() + .5);
			loc.setZ(loc.getZ() + .5);
			return (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
			
		}
		
		public static Villager RecipeReset(Villager v) {
			v.setInvulnerable(true);
			for (int i = 0; i < v.getRecipeCount(); i++) {
				MerchantRecipe r = new MerchantRecipe(new ItemStack(Material.AIR), 0);
				r.addIngredient(new ItemStack(Material.AIR));
				v.setRecipe(0, r);
			}
			return v;
		}
		
		public static void Sell1(Location loc) {
			Villager v = RecipeReset(Spawn(loc));
			v.setCustomName(Profix_Sell + "광물 수집가");
			v.setMetadata("shop_type", new FixedMetadataValue(Main._main, "sell1"));
			Object[][] sets = {
				{
					1,
					Material.DIAMOND,
					1
				},
				{
					1,
					Material.IRON_INGOT,
					48
				},
				{
					1,
					Material.GOLD_INGOT,
					30
				},
				{
					1,
					Material.COAL,
					64,
					Material.COAL,
					36
				}
			};
			List<MerchantRecipe> recipes = new ArrayList<>();
			for (Object[] o : sets) {
				MerchantRecipe r = new MerchantRecipe(TeamVS.Items.Money((int) o[0]), Integer.MAX_VALUE);
				for (int i = 1; i < o.length; i = i + 2) {
					r.addIngredient(new ItemStack((Material) o[i], (int) o[i + 1]));
				}
				recipes.add(r);
			}
			v.setRecipes(recipes);
		}
		
		public static void Sell2(Location loc) {
			Villager v = RecipeReset(Spawn(loc));
			v.setCustomName(Profix_Sell + "잡화 상인");
			v.setMetadata("shop_type", new FixedMetadataValue(Main._main, "sell2"));
			Object[][] sets = {
				{
					TeamVS.Items.GOLD_Money(1),
					Material.APPLE,
					40
				},
				{
					TeamVS.Items.GOLD_Money(1),
					Material.SEEDS,
					64,
					Material.SEEDS,
					64
				},
				{
					TeamVS.Items.GOLD_Money(1),
					Material.STRING,
					30
				},
				{
					TeamVS.Items.GOLD_Money(2),
					Material.RAW_FISH,
					40
				},
				{
					TeamVS.Items.GOLD_Money(4),
					Material.SULPHUR,
					30
				}
			};
			List<MerchantRecipe> recipes = new ArrayList<>();
			for (Object[] o : sets) {
				MerchantRecipe r = new MerchantRecipe((ItemStack) o[0], Integer.MAX_VALUE);
				for (int i = 1; i < o.length; i = i + 2) {
					r.addIngredient(new ItemStack((Material) o[i], (int) o[i + 1]));
				}
				recipes.add(r);
			}
			v.setRecipes(recipes);
		}
		
		public static void Sell3(Location loc) {
			Villager v = RecipeReset(Spawn(loc));
			v.setCustomName(Profix_Sell + "건축가");
			v.setMetadata("shop_type", new FixedMetadataValue(Main._main, "sell3"));
			Object[][] sets = {
				{
					TeamVS.Items.GOLD_Money(1),
					Material.OBSIDIAN,
					15
				}
			};
			List<MerchantRecipe> recipes = new ArrayList<>();
			for (Object[] o : sets) {
				MerchantRecipe r = new MerchantRecipe((ItemStack) o[0], Integer.MAX_VALUE);
				for (int i = 1; i < o.length; i = i + 2) {
					r.addIngredient(new ItemStack((Material) o[i], (int) o[i + 1]));
				}
				recipes.add(r);
			}
			v.setRecipes(recipes);
		}
		
		public static void Buy1(Location loc) {
			Villager v = RecipeReset(Spawn(loc));
			v.setCustomName(Profix_Buy + "금은방");
			v.setMetadata("shop_type", new FixedMetadataValue(Main._main, "buy1"));
			Object[][] sets = {
				{
					Material.DIAMOND,
					1,
					TeamVS.Items.Money(1),
					TeamVS.Items.GOLD_Money(1)
				},
				{
					Material.GOLD_INGOT,
					25,
					TeamVS.Items.Money(1)
				},
				{
					Material.IRON_INGOT,
					32,
					TeamVS.Items.Money(1)
				}
			};
			List<MerchantRecipe> recipes = new ArrayList<>();
			for (Object[] o : sets) {
				MerchantRecipe r = new MerchantRecipe(new ItemStack((Material) o[0], (int) o[1]), Integer.MAX_VALUE);
				for (int i = 2; i < o.length; i++) {
					r.addIngredient((ItemStack) o[i]);
				}
				recipes.add(r);
			}
			v.setRecipes(recipes);
		}
		
		public static void Buy2(Location loc) {
			Villager v = RecipeReset(Spawn(loc));
			v.setCustomName(Profix_Buy + "연금술사");
			v.setMetadata("shop_type", new FixedMetadataValue(Main._main, "buy2"));
			Object[][] sets = {
				{
					Material.BLAZE_ROD,
					2,
					TeamVS.Items.Money(1)
				},
				{
					Material.FERMENTED_SPIDER_EYE,
					1,
					TeamVS.Items.GOLD_Money(7)
				},
				{
					Material.NETHER_STALK,
					2,
					TeamVS.Items.GOLD_Money(1)
				},
				{
					Material.SUGAR,
					2,
					TeamVS.Items.GOLD_Money(1)
				},
				{
					Material.SULPHUR,
					2,
					TeamVS.Items.GOLD_Money(1)
				}
			};
			List<MerchantRecipe> recipes = new ArrayList<>();
			for (Object[] o : sets) {
				MerchantRecipe r = new MerchantRecipe(new ItemStack((Material) o[0], (int) o[1]), Integer.MAX_VALUE);
				for (int i = 2; i < o.length; i++) {
					r.addIngredient((ItemStack) o[i]);
				}
				recipes.add(r);
			}
			v.setRecipes(recipes);
		}
		
		public static void BuyKit1(Location loc) {
			Villager v = RecipeReset(Spawn(loc));
			v.setCustomName(Profix_Buy + "상품 판매원");
			v.setMetadata("shop_type", new FixedMetadataValue(Main._main, "buykit1"));
			Object[][] sets = {
				{
					"농사 스타트 키트",
					new Object[] {
						Material.STONE_HOE,
						1,
						Material.DIRT,
						30,
						Material.SEEDS,
						20,
						Material.PUMPKIN_SEEDS,
						20,
						Material.MELON_SEEDS,
						20,
						Material.WATER_BUCKET,
						1
					},
					TeamVS.Items.Money(1)
				},
				{
					"농사 발전 키트",
					new Object[] {
						Material.IRON_HOE,
						1,
						Material.IRON_SPADE,
						1,
						Material.GRASS,
						1,
						Material.DIRT,
						20,
						Material.SOUL_SAND,
						6,
						Material.NETHER_STALK,
						6,
						Material.SUGAR_CANE,
						3,
						Material.EGG,
						16
					},
					TeamVS.Items.Money(1),
					TeamVS.Items.GOLD_Money(4)
				}
			};
			List<MerchantRecipe> recipes = new ArrayList<>();
			for (Object[] a : sets) {
				ItemStack kit = new ItemStack(Material.ORANGE_SHULKER_BOX);
				BlockStateMeta kitMeta = (BlockStateMeta) kit.getItemMeta();
				ShulkerBox kitbox = (ShulkerBox) kitMeta.getBlockState();
				kitMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "[키트] " + ChatColor.RESET + "" + ChatColor.WHITE + (String) a[0]);
				for (int i = 0; i < ((Object[]) a[1]).length; i = i + 2) {
					Object[] b = (Object[]) a[1];
					kitbox.getInventory().addItem(new ItemStack((Material) b[i], (int) b[i + 1]));
				}
				kitMeta.setBlockState(kitbox);
				kit.setItemMeta(kitMeta);
				MerchantRecipe r = new MerchantRecipe(kit, Integer.MAX_VALUE);
				for (int i = 2; i < a.length; i++) {
					r.addIngredient((ItemStack) a[i]);
				}
				recipes.add(r);
			}
			v.setRecipes(recipes);
		}
	}
	
	@EventHandler // 블록 부술 때
	public void onBlockCracked(PlayerInteractEvent e) {
		Player ply = e.getPlayer();
		if (ply.isOp() || !hasPos(ply.getLocation())) return;
		if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler // 블록 설치할 때
	public void onBlockPlace(BlockPlaceEvent e) {
		Player ply = e.getPlayer();
		if (ply.isOp() || !hasPos(ply.getLocation())) return;
		e.setBuild(false);
		e.setCancelled(true);
	}
	
	@EventHandler // 플레이어가 피해를 받을 때
	public void onPlayerDamaged(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player)) return;
		Player ply = (Player) e.getEntity();
		if (!hasPos(ply.getLocation())) return;
		e.setCancelled(true);
	}
	
	@EventHandler // 보호 지역 알림
	public void onMoveJoin(PlayerMoveEvent e) {
		if (hasPos(e.getPlayer().getLocation())) {
			TextComponent t = new TextComponent("보호 구역");
			t.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
			t.setBold(true);
			e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, t);
		}
	}
	
	@EventHandler // 크리퍼 폭발 방지
	public void onCreeper(EntityExplodeEvent e) {
		if (!(e.getEntity() instanceof Creeper)) return;
		if (!hasPos(e.getEntity().getLocation())) return;
		e.setCancelled(true);
	}
	
	@EventHandler // 거래시 초기화
	public void onTradeEvent(PlayerInteractAtEntityEvent e) {
		if (!(e.getRightClicked() instanceof Villager)) return;
		if (!hasPos(e.getRightClicked().getLocation())) return;
		Villager v = (Villager) e.getRightClicked();
		for (MerchantRecipe re : v.getRecipes()) {
			re.setUses(0);
			re.setExperienceReward(false);
		}
	}
	
	@EventHandler // 거래시 항목 추가 금지
	public void onReplenishTrade(VillagerReplenishTradeEvent e) {
		if (!hasPos(e.getEntity().getLocation())) return;
		e.setCancelled(true);
		e.setBonus(0);
	}
	
	@EventHandler // 거래 제안 추가시
	public void onAcquireTrade(VillagerAcquireTradeEvent e) {
		if (!hasPos(e.getEntity().getLocation())) return;
		e.setCancelled(true);
	}
}