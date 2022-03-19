package de.sebli.serverbackup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import de.sebli.serverbackup.commands.SBCommand;

public class ServerBackup extends JavaPlugin implements Listener {

	private static ServerBackup sb;

	public static ServerBackup getInstance() {
		return sb;
	}

	public static File folder = new File("Backups");

	@Override
	public void onDisable() {
		stopTimer();

		for (BukkitTask task : Bukkit.getScheduler().getPendingTasks()) {
			task.cancel();

			this.getLogger().log(Level.WARNING, "WARNING - ServerBackup: Task [" + task.getTaskId()
					+ "] cancelled due to server shutdown. There might be some unfinished Backups.");
		}

		this.getLogger().log(Level.INFO, "ServerBackup: Plugin disabled.");
//		System.out.println("ServerBackup: Plugin disabled.");
	}

	@Override
	public void onEnable() {
		sb = this;

		loadFiles();

		getCommand("backup").setExecutor(new SBCommand());

		Bukkit.getPluginManager().registerEvents(this, this);

		startTimer();

		this.getLogger().log(Level.INFO, "ServerBackup: Plugin enabled.");
//		System.out.println("ServerBackup: Plugin enabled.");
	}

	private void loadFiles() {
		if (!folder.exists()) {
			folder.mkdir();
		}

		getConfig().options()
				.header("BackupTimer = At what time should a Backup be created? The format is: 'hh-mm' e.g. '12-30'."
						+ "\nDeleteOldBackups = Deletes old backups automatically after a specific time (in days, standard = 7 days)"
						+ "\nDeleteOldBackups - Type '0' at DeleteOldBackups to disable the deletion of old backups."
						+ "\nBackupLimiter = Deletes old backups automatically if number of total backups is greater than this number (e.g. if you enter '5' - the oldest backup will be deleted if there are more than 5 backups, so you will always keep the latest 5 backups)"
						+ "\nBackupLimiter - Type '0' to disable this feature. If you don't type '0' the feature 'DeleteOldBackups' will be disabled and this feature ('BackupLimiter') will be enabled."
						+ "\nKeepUniqueBackups - Type 'true' to disable the deletion of unique backups. The plugin will keep the newest backup of all backed up worlds or folders, no matter how old it is."
						+ "\nCollectiveZipFile - Type 'true' if you want to have all backed up worlds in just one zip file.");
		getConfig().options().copyDefaults(true);

		getConfig().addDefault("AutomaticBackups", true);

		List<String> days = new ArrayList<>();
		days.add("MONDAY");
		days.add("TUESDAY");
		days.add("WEDNESDAY");
		days.add("THURSDAY");
		days.add("FRIDAY");
		days.add("SATURDAY");
		days.add("SUNDAY");

		List<String> times = new ArrayList<>();
		times.add("00-00");

		getConfig().addDefault("BackupTimer.Days", days);
		getConfig().addDefault("BackupTimer.Times", times);

		List<String> worlds = new ArrayList<>();
		worlds.add("world");
		worlds.add("world_nether");
		worlds.add("world_the_end");

		getConfig().addDefault("BackupWorlds", worlds);

		getConfig().addDefault("DeleteOldBackups", 7);
		getConfig().addDefault("BackupLimiter", 0);

		getConfig().addDefault("KeepUniqueBackups", false);

		getConfig().addDefault("CollectiveZipFile", false);

//		getConfig().addDefault("ZipCompression", true);

		if (getConfig().contains("ZipCompression")) {
			getConfig().set("ZipCompression", null);
		}

		getConfig().addDefault("SendLogMessages", false);
		
		getConfig().addDefault("BackupPath", "Backups");

		saveConfig();
	}

	public void startTimer() {
		if (getConfig().getBoolean("AutomaticBackups")) {
			Bukkit.getScheduler().runTaskTimerAsynchronously(this, new BackupTimer(), 20 * 60, 20 * 60);
		}
	}

	public void stopTimer() {
		Bukkit.getScheduler().cancelTasks(this);
	}

}
