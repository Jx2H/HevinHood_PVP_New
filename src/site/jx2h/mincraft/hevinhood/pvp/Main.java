package site.jx2h.mincraft.hevinhood.pvp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	ConsoleCommandSender consol = Bukkit.getConsoleSender();
	public static Main _main;
	public static TeamVS _game;
	public static String profix = ChatColor.AQUA + "[Hevin Hood's PVP] ";
	
	@Override
	public void onEnable() {
		consol.sendMessage(profix + "ON");
		this.saveDefaultConfig();
		_main = this;
		_game = new TeamVS();
		// 명령어 초기화
		ChatCommand cmds = new ChatCommand();
		getCommand("testa").setExecutor(cmds.new testa());
		getCommand("gamestart").setExecutor(cmds.new gamestart());
		getCommand("gamecheck").setExecutor(cmds.new gamecheck());
		getCommand("gamestop").setExecutor(cmds.new gamestop());
		getCommand("bunker").setExecutor(cmds.new bunker());
		getCommand("house").setExecutor(cmds.new house());
		PluginCommand gamemenu = getCommand("gamemenu");
		gamemenu.setExecutor(cmds.new gamemenu());
		gamemenu.setTabCompleter(cmds.new gamemenu());
		getCommand("qc").setExecutor(cmds.new quickCommand());
	}
	
	@Override
	public void onDisable() {
		consol.sendMessage(profix + "OFF");
	}
}