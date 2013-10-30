package ca.kanoa.slashlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.InventoryHolder;

public class SharedUtils {
	
	public static InventoryHolder getHolder(Block block) {
		if (block.getState() instanceof DoubleChest) {
			return ((DoubleChest) block.getState()).getInventory()
					.getHolder();
		} else if (block.getState() instanceof Chest) {
			return ((Chest) block.getState()).getInventory().getHolder();
		} else {
			return null;
		}
	}
	
	public static Block getBlock(InventoryHolder holder) {
		if (holder instanceof DoubleChest) {
			return ((Chest) ((DoubleChest) holder).getLeftSide()).getBlock();
		} else if (holder instanceof Chest) {
			return ((Chest) holder).getBlock();
		} else {
			return null;
		}
	}
	
	public static Block[] getChests(InventoryHolder holder) {
		Block[] blocks;
		if (holder instanceof Chest) {
			blocks = new Block[]{
					((Chest) holder).getBlock()
					};
		} else if (holder instanceof DoubleChest) {
			blocks = new Block[]{
					((Chest)((DoubleChest) holder).getLeftSide()).getBlock(),
					((Chest)((DoubleChest) holder).getRightSide()).getBlock()
				};
		} else {
			return null;
		}
		return blocks;
	}
	
	public static Sign[] getSigns(Block[] blocks) {
		List<Sign> signs = new ArrayList<Sign>();
		for (Block b : blocks) {
			if (b.getRelative(BlockFace.NORTH).getType() == 
					Material.WALL_SIGN) {
				signs.add((Sign) b.getRelative(BlockFace.NORTH).getState());
			}
			if (b.getRelative(BlockFace.EAST).getType() == 
					Material.WALL_SIGN) {
				signs.add((Sign) b.getRelative(BlockFace.EAST).getState());
			}
			if (b.getRelative(BlockFace.SOUTH).getType() == 
					Material.WALL_SIGN) {
				signs.add((Sign) b.getRelative(BlockFace.SOUTH).getState());
			}
			if (b.getRelative(BlockFace.WEST).getType() == 
					Material.WALL_SIGN) {
				signs.add((Sign) b.getRelative(BlockFace.WEST).getState());
			}
		}
		return signs.toArray(new Sign[0]);
	}
	
	public static String[] getUsers(Sign[] signs) {
		Set<String> users = new HashSet<String>();
		for (Sign sign : signs) {
			if (sign.getLine(1).equalsIgnoreCase(ChatColor.DARK_RED + "Locked")) {
				users.add(sign.getLine(2) + sign.getLine(3));
			}
		}
		return users.toArray(new String[0]);
	}
	
	public static boolean isLockSign(Block sign) {
		if (sign.getType() == Material.WALL_SIGN) {
			return isLockSign((Sign) sign.getState());
		} else {
			return false;
		}
	}
	
	public static boolean isLockSign(Sign sign) {
		return sign.getLine(1).equalsIgnoreCase(ChatColor.DARK_RED + "Locked");
	}
	
	@SuppressWarnings("deprecation")
	public static boolean hasPermission(String player, Block block) {
		//Find the chest depending on weather `block` is a sign vs chest
		InventoryHolder chest;
		if (block.getType() == Material.CHEST || block.getType() == 
				Material.TRAPPED_CHEST) {
			chest = getHolder(block);
		} else if (block.getType() == Material.WALL_SIGN) {
			Block chestBlock = null;
			switch (block.getData()) {
			case 2: 
				chestBlock = block.getRelative(BlockFace.NORTH
						.getOppositeFace());
				break;
			case 5: 
				chestBlock = block.getRelative(BlockFace.EAST
						.getOppositeFace());
				break;
			case 3: 
				chestBlock = block.getRelative(BlockFace.SOUTH
						.getOppositeFace());
				break;
			case 4: 
				chestBlock = block.getRelative(BlockFace.WEST
						.getOppositeFace());
				break;
			}
			if (chestBlock.getType() != Material.CHEST && chestBlock
					.getType() != Material.TRAPPED_CHEST) {
				return false;
			}
			chest = getHolder(chestBlock);
		} else {
			return false;
		}
		
		String[] owners = getUsers(getSigns(getChests(chest)));
		
		return owners.length == 0 || Arrays.asList(owners).contains(player);
	}
}
