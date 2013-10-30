package ca.kanoa.slashlock;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class EventListener implements Listener {

	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onInventoryOpen(InventoryOpenEvent event) {
		Block[] blocks = SharedUtils.getChests(event.getInventory().getHolder());
		if (blocks == null || event.getPlayer() == null || 
				event.getPlayer().hasPermission("slashlock.bypass")) {
			return;
		}
		String[] users = SharedUtils.getUsers(SharedUtils.getSigns(blocks));
		if (users.length == 0) {
			return;
		}
		for (String user : users) {
			if (user.equalsIgnoreCase(event.getPlayer().getName())) {
				return;
			}
		}

		//If the player does NOT have permission to open this chest
		((Player) event.getPlayer()).sendMessage(ChatColor.RED + 
				"You don't have permission to open this chest!");
		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock().getType() == Material.WALL_SIGN) {
			Sign sign = (Sign) event.getBlock().getState();
			if (sign.getLine(1).equalsIgnoreCase(ChatColor.DARK_RED + "Locked")
					&& !sign.getLine(2).equalsIgnoreCase(event.getPlayer()
							.getName()) && !event.getPlayer()
							.hasPermission("slashlock.unlock")) {
				event.getPlayer().sendMessage(ChatColor.RED + 
						"You don't have permission to unlock a chest!");
				event.setCancelled(true);
			} else {
				return;
			}
		} else if (event.getBlock().getType() == Material.CHEST || 
				event.getBlock().getType() == Material.TRAPPED_CHEST) {
			String[] users = SharedUtils.getUsers(SharedUtils.getSigns(
					SharedUtils.getChests(SharedUtils.getHolder(event
					.getBlock()))));
			if (users.length == 0 || Arrays.asList(users).contains(
					event.getPlayer().getName()) || 
					event.getPlayer().hasPermission("slashlock.unlock")) {
				return;
			} else {
				event.getPlayer().sendMessage(ChatColor.RED + 
						"You don't have permission to unlock a chest!");
				event.setCancelled(true);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onSignPlace(SignChangeEvent event) {
		if (!event.getLine(1).equalsIgnoreCase("locked")) {
			return;
		}
		if (event.getLine(1).equalsIgnoreCase("locked")) {
			if ((event.getLine(2).equalsIgnoreCase(event.getPlayer().getName()) ||
					event.getLine(2).equalsIgnoreCase("") ||
					event.getPlayer().hasPermission("slashlock.lockother")) &&
					event.getPlayer().hasPermission("slashlock.lock")) {
				//Check to see if the chest is already locked
				Block chestBlock = null;
				switch (event.getBlock().getData()) {
				case 2: 
					chestBlock = event.getBlock().getRelative(BlockFace.NORTH
							.getOppositeFace());
					break;
				case 5: 
					chestBlock = event.getBlock().getRelative(BlockFace.EAST
							.getOppositeFace());
					break;
				case 3: 
					chestBlock = event.getBlock().getRelative(BlockFace.SOUTH
							.getOppositeFace());
					break;
				case 4: 
					chestBlock = event.getBlock().getRelative(BlockFace.WEST
							.getOppositeFace());
					break;
				}
				if (chestBlock.getType() != Material.CHEST && chestBlock
						.getType() != Material.TRAPPED_CHEST) {
					return;
				}
				String[] users = SharedUtils.getUsers(SharedUtils.getSigns(SharedUtils.getChests(
						SharedUtils.getHolder(chestBlock))));
				if (users.length > 0 && !Arrays.asList(users).contains(event
						.getPlayer().getName())) {
					event.getPlayer().sendMessage(ChatColor.RED + "That chest is already locked!");
					event.setCancelled(true);
					event.getBlock().breakNaturally();
					return;
				}

				event.setLine(0, "");
				event.setLine(1, ChatColor.DARK_RED + "Locked");
				if (event.getPlayer().getName().equalsIgnoreCase(event
						.getLine(2)) ||
						event.getLine(2).equalsIgnoreCase("")) {
					event.setLine(2, event.getPlayer().getName());
				}
				event.setLine(3, "");
			} else {
				event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission!");
				event.setCancelled(true);
			}
		}
	}

}
