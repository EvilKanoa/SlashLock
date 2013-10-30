package ca.kanoa.slashlock;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandExecutor implements org.bukkit.command.CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		//Make sure the command and sender are valid
		if (args.length > 0) {
			return false;
		} else if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Player only command!");
			return true;
		}
		Player player = (Player) sender;

		//Find and check the block the user wants locked
		@SuppressWarnings("deprecation")
		Block chest = player.getTargetBlock(null, 15);
		if (chest.getType() != Material.CHEST && chest.getType() != Material
				.TRAPPED_CHEST) {
			player.sendMessage(ChatColor.RED + "No chest in sight or chest too far away!");
			return true;
		}
		
		//Check to see if the chest is already locked
		String[] users = SharedUtils.getUsers(SharedUtils.getSigns(SharedUtils.getChests(
				SharedUtils.getHolder(chest))));
		if (users.length > 0 && !Arrays.asList(users).contains(player.getName())) {
			player.sendMessage(ChatColor.RED + "That chest is already locked!");
			return true;
		}

		//Find a side to place the Lock Sign on
		BlockFace direction = getDirectionTo(player.getLocation(), 
				chest.getLocation()).getOppositeFace();
		Block signBlock = null;
		if (chest.getRelative(direction).getType() != Material.AIR) {
			if (chest.getRelative(BlockFace.NORTH).getType() == 
					Material.AIR) {
				direction = BlockFace.NORTH;
			} else if (chest.getRelative(BlockFace.EAST).getType() == 
					Material.AIR) {
				direction = BlockFace.EAST;
			} else if (chest.getRelative(BlockFace.SOUTH).getType() == 
					Material.AIR) {
				direction = BlockFace.SOUTH;
			} else if (chest.getRelative(BlockFace.WEST).getType() == 
					Material.AIR) {
				direction = BlockFace.WEST;
			} else {
				player.sendMessage(ChatColor.RED + 
						"There's no empty side around the chest to place a Lock Sign!");
				return true;
			}
		}
		signBlock = chest.getRelative(direction);

		//Place the lock sign
		signBlock.setType(Material.WALL_SIGN);
		org.bukkit.material.Sign signData = new org.bukkit.material.Sign(Material.WALL_SIGN);
		signData.setFacingDirection(direction);
		Sign sign = (Sign) signBlock.getState();
		sign.setData(signData);
		sign.setLine(1, ChatColor.DARK_RED + "Locked");
		sign.setLine(2, player.getName());
		sign.update(true);

		//Alert the user that the chest is now locked
		player.sendMessage(ChatColor.GREEN + "That chest is now locked!");

		return true;
	}

	public BlockFace getDirectionTo(Location from, Location to) {
		int x1 = from.getBlockX(), z1 = from.getBlockZ();
		int x2 = to.getBlockX(), z2 = to.getBlockZ();

		int dx = x1 - x2, dz = z1 - z2;
		int adx = Math.abs(dx), adz = Math.abs(dz);

		if (adx > adz) {
			if (dx > 0) {
				return BlockFace.WEST;
			} else {
				return BlockFace.EAST;
			}
		} else {
			if (dz > 0) {
				return BlockFace.NORTH;
			} else {
				return BlockFace.SOUTH;
			}
		}
	}

}
