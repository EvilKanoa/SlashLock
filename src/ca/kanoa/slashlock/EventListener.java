package ca.kanoa.slashlock;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {

	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onInventoryMoveItem (InventoryMoveItemEvent event) {
		Sign[] signs = SharedUtils.getSigns(SharedUtils.getChests(event
				.getSource().getHolder()));
		for (Sign s : signs) {
			if (SharedUtils.isLockSign(s)) {
				event.setCancelled(true);
				break;
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onInventoryOpen(InventoryOpenEvent event) {
		Block[] blocks = SharedUtils.getChests(event.getInventory().getHolder());
		if (blocks == null || event.getPlayer() == null || 
				event.getPlayer().hasPermission("slashlock.bypass")) {
			return;
		}

		if (SharedUtils.hasPermission(event.getPlayer().getName(), 
				SharedUtils.getBlock(event.getInventory().getHolder()))) {
			return;
		}

		//If the player does NOT have permission to open this chest
		((Player) event.getPlayer()).sendMessage(ChatColor.RED + 
				"You don't have permission to open this chest!");
		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		//Check if it's a sign or chest that is being broken, otherwise ignore it
		if (event.getBlock().getType() == Material.WALL_SIGN) {
			Sign sign = (Sign) event.getBlock().getState();
			//Check to see if the player has permission/is an owner of the sign.
			if (sign.getLine(1).equalsIgnoreCase(ChatColor.DARK_RED + "Locked")
					&& !SharedUtils.hasPermission(event.getPlayer().getName(), 
							event.getBlock()) && !event.getPlayer()
							.hasPermission("slashlock.unlock")) {
				event.getPlayer().sendMessage(ChatColor.RED + 
						"You don't have permission to unlock this chest!");
				event.setCancelled(true);
			} else {
				//If the player does have permission and it's a lock sign, 
				// cancel the drop 
				// (by canceling the event but deleting the block)
				if (sign.getLine(1).equalsIgnoreCase(ChatColor.DARK_RED + "Locked")) {
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
				}
				return;
			}
		} else if (event.getBlock().getType() == Material.CHEST || 
				event.getBlock().getType() == Material.TRAPPED_CHEST) {
			if (SharedUtils.hasPermission(event.getPlayer().getName(), 
					event.getBlock()) || event.getPlayer()
					.hasPermission("slashlock.unlock")) {
				//Remove any old lock signs so infinite sign glitch can't be 
				// exploited
				for (Sign s : SharedUtils.getSigns(SharedUtils.getChests(
						SharedUtils.getHolder(event.getBlock())))) {
					if (SharedUtils.isLockSign(s)) {
						s.getBlock().setType(Material.AIR);
					}
				}
				return;
			} else {
				event.getPlayer().sendMessage(ChatColor.RED + 
						"You don't have permission to unlock this chest!");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onSignPlace(SignChangeEvent event) {
		if (!event.getLine(1).equalsIgnoreCase("locked")) {
			return;
		}
		if (event.getLine(1).equalsIgnoreCase("locked")) {
			if (((event.getLine(2) + event.getLine(3)).equalsIgnoreCase(
					event.getPlayer().getName()) ||
					event.getLine(2).equalsIgnoreCase("") ||
					event.getPlayer().hasPermission("slashlock.lockother") ||
					SharedUtils.hasOwnership(event.getPlayer().getName(), 
							event.getBlock())) &&
					event.getPlayer().hasPermission("slashlock.lock")) {
				//Check to see if the chest is already locked
				if (!SharedUtils.hasPermission(event.getPlayer().getName(), 
						event.getBlock())) {
					event.getPlayer().sendMessage(ChatColor.RED + "That chest is already locked!");
					event.setCancelled(true);
					event.getBlock().breakNaturally();
					return;
				}

				//Turn the sign into a proper Lock Sign
				event.setLine(0, "");
				event.setLine(1, ChatColor.DARK_RED + "Locked");
				if (event.getPlayer().getName().equalsIgnoreCase(
						event.getLine(2) + event.getLine(3)) ||
						event.getLine(2).equalsIgnoreCase("")) {
					String name = event.getPlayer().getName();
					if (name.length() <= 15) {
						event.setLine(2, name);
						event.setLine(3, "");
					} else {
						event.setLine(2, name.substring(0, 15));
						event.setLine(3, name.substring(15));
					}
				}
				
				//Give the player a sign item back 'cause Lock Signs are free :D
				event.getPlayer().getInventory().addItem(
						new ItemStack(Material.SIGN));
			} else {
				event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission!");
				event.setCancelled(true);
			}
		}
	}

}
