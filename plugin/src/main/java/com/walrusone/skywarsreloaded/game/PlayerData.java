package com.walrusone.skywarsreloaded.game;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import com.walrusone.skywarsreloaded.SkyWarsReloaded;
import com.walrusone.skywarsreloaded.managers.PlayerStat;
import com.walrusone.skywarsreloaded.utilities.Tagged;
import com.walrusone.skywarsreloaded.utilities.Util;

public class PlayerData {
	    private static ArrayList<PlayerData> playerData;
	    private UUID uuid;
	    private Scoreboard sb;
		private Tagged taggedBy;
		private Inventory inv;
		private double health;
		private int food;
		private float sat;
		private boolean beingRestored;
	    
	    public PlayerData(final Player p) {
	    	if (SkyWarsReloaded.getCfg().debugEnabled()) {
	        	Util.get().logToFile(ChatColor.RED + "[skywars] " + ChatColor.YELLOW + "Creating " + p.getName() + "'s Datafile");
	    	}
	    	this.beingRestored = false;
	        this.uuid = p.getUniqueId();
	        this.sb = p.getScoreboard();
	        this.health = p.getHealth();
	        this.food = p.getFoodLevel();
	        this.sat = p.getSaturation();
	        inv = Bukkit.createInventory(null, InventoryType.PLAYER, p.getName());
	        inv.setContents(p.getInventory().getContents());
	    	if (SkyWarsReloaded.getCfg().debugEnabled()) {
	    		Util.get().logToFile(ChatColor.RED + "[skywars] " + ChatColor.YELLOW + p.getName() + "'s Datafile has been created");
	    	}
	    }
	        
	    public static ArrayList<PlayerData> getPlayerData() {
	        return PlayerData.playerData;
	    }
	    
	    public static PlayerData getPlayerData(final UUID uuid) {
	        for (final PlayerData pData : getPlayerData()) {
	            if (pData.getUuid().toString().equals(uuid.toString())) {
	                return pData;
	            }
	        }
	        return null;
	    }
	    
		public void restore() {
			if (!beingRestored) {
				beingRestored = true;
		        final Player player = this.getPlayer();
		        if (player == null) {
		            return;
		        }
		        
				if (SkyWarsReloaded.getCfg().debugEnabled()) {
		        	Util.get().logToFile(ChatColor.RED + "[skywars] " + ChatColor.YELLOW + "Restoring " + player.getName());
		    	}
		    	PlayerStat pStats = PlayerStat.getPlayerStats(player);
		        player.closeInventory();
		        player.setGameMode(GameMode.SURVIVAL);
		        if (SkyWarsReloaded.getCfg().displayPlayerExeperience()) {
		        	if (pStats != null) { Util.get().setPlayerExperience(player, pStats.getXp()); }
		        }
		        Util.get().clear(player);
		        player.getInventory().clear();
		        player.getInventory().setContents(inv.getContents());
		        SkyWarsReloaded.getNMS().setMaxHealth(player, 20);
		        if (health <= 0 || health > 20) {
					player.setHealth(20);
				} else {
					player.setHealth(health);
				}
		        player.setFoodLevel(food);
		        player.setSaturation(sat);
		        player.resetPlayerTime();
		        player.resetPlayerWeather();
		        player.setAllowFlight(false);
		        player.setFlying(false);
		        
		        player.setFireTicks(0);
		        player.setScoreboard(sb);
		        if (SkyWarsReloaded.getCfg().lobbyBoardEnabled() && !SkyWarsReloaded.getCfg().bungeeMode()) {
			        PlayerStat.updateScoreboard(player);
		        }
		        
		        final Location respawn = SkyWarsReloaded.getCfg().getSpawn();
		        player.teleport(respawn, TeleportCause.END_PORTAL);	
		        
		    	if (SkyWarsReloaded.getCfg().debugEnabled()) {
		        	Util.get().logToFile(ChatColor.RED + "[skywars] " + ChatColor.YELLOW + "Finished restoring " + player.getName() + ". Teleporting to Spawn");
		    	}
				if (SkyWarsReloaded.getCfg().bungeeMode()) {
					new BukkitRunnable() {
						@Override
						public void run() {
							String uuid = player.getUniqueId().toString();
							SkyWarsReloaded.get().sendBungeeMsg(player, "Connect", SkyWarsReloaded.getCfg().getBungeeLobby());
							PlayerStat remove = PlayerStat.getPlayerStats(uuid);
							PlayerStat.getPlayers().remove(remove);
						}
					}.runTaskLater(SkyWarsReloaded.get(), 5);
				}
			}
	    }
	    
	    public Player getPlayer() {
	        return SkyWarsReloaded.get().getServer().getPlayer(this.uuid);
	    }
	    
	    public UUID getUuid() {
	        return this.uuid;
	    }
	        
	    static {
	        PlayerData.playerData = new ArrayList<>();
	    }

	    public void setTaggedBy(Player player) {
			taggedBy = new Tagged(player, System.currentTimeMillis());
		}
		
		public Tagged getTaggedBy() {
			return taggedBy;
		}
	}
