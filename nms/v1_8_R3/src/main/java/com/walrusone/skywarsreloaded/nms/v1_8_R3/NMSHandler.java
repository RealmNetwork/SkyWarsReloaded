package com.walrusone.skywarsreloaded.nms.v1_8_R3;


import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.server.v1_8_R3.EntityFallingBlock;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.TileEntityEnderChest;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.EntityEnderDragon;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.EntityCreature;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFallingSand;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.walrusone.skywarsreloaded.api.NMS;

public class NMSHandler implements NMS {
	
	public void respawnPlayer(Player player) {
		((CraftServer)Bukkit.getServer()).getHandle().moveToWorld(((CraftPlayer)player).getHandle(), 0, false);
	}
	
	public void updateChunks(World world, List<Chunk> chunks) {
		for (Chunk currentChunk: chunks) {
			net.minecraft.server.v1_8_R3.World mcWorld = ((CraftChunk) currentChunk).getHandle().world;
	        for (EntityHuman eh : (List<EntityHuman>) mcWorld.players) {
	        	EntityPlayer ep = (EntityPlayer) eh;
	            ep.chunkCoordIntPairQueue.add(new ChunkCoordIntPair(currentChunk.getX(), currentChunk.getZ()));
	        }
		}
	}
	
	public void sendParticles(World world, String type, float x, float y, float z, float offsetX, float offsetY, float offsetZ, float data, int amount) {
		EnumParticle particle = EnumParticle.valueOf(type);
		PacketPlayOutWorldParticles particles = new PacketPlayOutWorldParticles(particle, true, x, y, z, offsetX, offsetY, offsetZ, data, amount, 1);
		for (Player player: world.getPlayers()) {
			CraftPlayer start = (CraftPlayer) player; //Replace player with your player.
			EntityPlayer target = start.getHandle();
			PlayerConnection connect = target.playerConnection;
			connect.sendPacket(particles);
		}
	}
	
	public FireworkEffect getFireworkEffect(Color one, Color two, Color three, Color four, Color five, Type type) {
		return FireworkEffect.builder().flicker(false).withColor(one, two, three, four).withFade(five).with(type).trail(true).build();
	}
	
	public void sendTitle(Player player, int fadein, int stay, int fadeout, String title, String subtitle) {
		PlayerConnection pConn = ((CraftPlayer) player).getHandle().playerConnection;
		PacketPlayOutTitle pTitleInfo = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, (IChatBaseComponent) null, (int) fadein, (int) stay, (int) fadeout);
		pConn.sendPacket(pTitleInfo);
		if (subtitle != null) {
			subtitle = subtitle.replaceAll("%player%", player.getDisplayName());
			subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
			IChatBaseComponent iComp = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + subtitle + "\"}");
			PacketPlayOutTitle pSubtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, iComp);
			pConn.sendPacket(pSubtitle);
		}
		if (title != null) {
			title = title.replaceAll("%player%", player.getDisplayName());
			title = ChatColor.translateAlternateColorCodes('&', title);
			IChatBaseComponent iComp = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + title + "\"}");
			PacketPlayOutTitle pTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, iComp);
			pConn.sendPacket(pTitle);
		}
	}
	
	public boolean isOnePointEight() {
		return true;
	} 
	
	public void playGameSound(Location loc, String sound, float volume, float pitch, boolean customSound) {
		if (customSound) {
		} else {
			loc.getWorld().playSound(loc, Sound.valueOf(sound), volume, pitch);
		}
	}

	public void sendActionBar(Player p, String msg) {
        String s = ChatColor.translateAlternateColorCodes('&', msg);
        IChatBaseComponent icbc = ChatSerializer.a("{\"text\": \"" + s + "\"}");
        PacketPlayOutChat bar = new PacketPlayOutChat(icbc, (byte)2);
        ((CraftPlayer)p).getHandle().playerConnection.sendPacket(bar);		
	}

	public String getItemName(ItemStack item) {
		if (item != null) {
			return CraftItemStack.asNMSCopy(item).getName();
		}
		return "";
	}
	
	public ItemStack getMainHandItem(Player player) {
        return player.getInventory().getItemInHand();
	}
	
	public ItemStack getOffHandItem(Player player) {
		return null;
	}
	
	public ItemStack getItemStack(Material material, List<String> lore, String message) {
    	ItemStack addItem = new ItemStack(material, 1);
        ItemMeta addItemMeta = addItem.getItemMeta();
        addItemMeta.setDisplayName(message);
        addItemMeta.setLore(lore);
        addItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        addItemMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        addItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        addItemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        addItemMeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        addItemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        addItem.setItemMeta(addItemMeta);
        return addItem;
	}
	
	@Override
	public ItemStack getItemStack(ItemStack item, List<String> lore, String message) {
		ItemStack addItem = item.clone();
        ItemMeta addItemMeta = addItem.getItemMeta();
        addItemMeta.setDisplayName(message);
        addItemMeta.setLore(lore);
        addItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        addItemMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        addItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        addItemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        addItemMeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        addItemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        addItem.setItemMeta(addItemMeta);
        return addItem;
	}

	@Override
	public boolean isValueParticle(String string) {
		return true;
	}
	
	@Override 
	public void updateSkull(Skull skull, UUID uuid) {
		skull.setOwner(Bukkit.getOfflinePlayer(uuid).getName());
	}
	
	public void setMaxHealth(Player player, int health) {
		player.setMaxHealth(health);
	}
	
	@Override
	public void spawnDragon(World world, Location loc) {
		WorldServer w = ((CraftWorld) world).getHandle();
		EntityEnderDragon dragon = new EntityEnderDragon(w);
		dragon.setLocation(loc.getX(), loc.getY(), loc.getZ(), w.random.nextFloat() * 360.0F, 0.0F);
		w.addEntity(dragon);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public Entity spawnFallingBlock(Location loc, Material mat, boolean damage) {
		FallingBlock block = loc.getWorld().spawnFallingBlock(loc, mat, (byte) 0);
		block.setDropItem(false);
		EntityFallingBlock fb = ((CraftFallingSand) block).getHandle();
		fb.a(damage);
		return block;
	}
	
	@Override
	public void playEnderChestAction(Block block, boolean open) {
        Location location = block.getLocation();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
        TileEntityEnderChest ec = (TileEntityEnderChest) world.getTileEntity(position);
        world.playBlockAction(position, ec.w(), 1, open ? 1 : 0);
    }
	
	@Override
	public void setEntityTarget(Entity ent, Player player) {
		EntityCreature entity = (EntityCreature) ((CraftEntity) ent).getHandle();
		entity.setGoalTarget(((EntityLiving) ((CraftPlayer) player).getHandle()), null, false);
	}
	
	@Override
	public void updateSkull(SkullMeta meta1, Player player) {
		meta1.setOwner(player.getName());
	}

	@Override
	public ChunkGenerator getChunkGenerator() {
		return new ChunkGenerator() {
			@Override
			public List<BlockPopulator> getDefaultPopulators(World world) {
				return Arrays.asList(new BlockPopulator[0]);
			}

			@Override
			public boolean canSpawn(World world, int x, int z) {
				return true;
			}

			@Override
			public byte[] generate(World world, Random random, int x, int z) {
				return new byte[32768];
			}

			@Override
			public Location getFixedSpawnLocation(World world, Random random) {
				return new Location(world, 0.0D, 64.0D, 0.0D);
			}
		};
	}
}
