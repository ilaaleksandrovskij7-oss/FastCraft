package com.aid;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FastCraft implements ModInitializer {
	private static final Set<Block> TREE_BLOCKS = new HashSet<>(Arrays.asList(
			Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG,
			Blocks.JUNGLE_LOG, Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG,
			Blocks.MANGROVE_LOG, Blocks.CHERRY_LOG,
			Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_SPRUCE_LOG,
			Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_JUNGLE_LOG,
			Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_DARK_OAK_LOG,
			Blocks.STRIPPED_MANGROVE_LOG, Blocks.STRIPPED_CHERRY_LOG
	));

	private static final Set<Block> ORE_BLOCKS = new HashSet<>(Arrays.asList(
			Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
			Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
			Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
			Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
			Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
			Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
			Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
			Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
			Blocks.NETHER_QUARTZ_ORE, Blocks.NETHER_GOLD_ORE,
			Blocks.ANCIENT_DEBRIS
	));

	@Override
	public void onInitialize() {
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, entity) -> {
			if (!level.isClientSide && player != null) {
				ItemStack tool = player.getMainHandItem();

				if (isCorrectTool(tool, state)) {
					if (TREE_BLOCKS.contains(state.getBlock())) {
						breakTree(level, player, pos, state, tool);
						return false;
					}

					if (ORE_BLOCKS.contains(state.getBlock())) {
						breakVein(level, player, pos, state, tool);
						return false;
					}
				}
			}
			return true;
		});
	}

	private boolean isCorrectTool(ItemStack tool, BlockState state) {
		if (TREE_BLOCKS.contains(state.getBlock())) {
			return tool.getItem() instanceof AxeItem;
		}
		if (ORE_BLOCKS.contains(state.getBlock())) {
			return tool.getItem() instanceof PickaxeItem;
		}
		return false;
	}

	private void breakTree(Level level, Player player, BlockPos startPos, BlockState startState, ItemStack tool) {
		if (level.isClientSide) return;

		Set<BlockPos> toBreak = new HashSet<>();
		Queue<BlockPos> queue = new LinkedList<>();
		Set<BlockPos> visited = new HashSet<>();

		queue.add(startPos);
		visited.add(startPos);

		while (!queue.isEmpty()) {
			BlockPos current = queue.poll();
			BlockState currentState = level.getBlockState(current);

			if (TREE_BLOCKS.contains(currentState.getBlock())) {
				toBreak.add(current);

				for (int dx = -1; dx <= 1; dx++) {
					for (int dy = -1; dy <= 1; dy++) {
						for (int dz = -1; dz <= 1; dz++) {
							if (dx == 0 && dy == 0 && dz == 0) continue;

							BlockPos neighbor = current.offset(dx, dy, dz);
							if (!visited.contains(neighbor) &&
									level.isLoaded(neighbor) &&
									level.getBlockState(neighbor).getBlock() == currentState.getBlock()) {

								visited.add(neighbor);
								queue.add(neighbor);
							}
						}
					}
				}
			}
		}

		for (BlockPos pos : toBreak) {
			if (!pos.equals(startPos)) {
				breakBlock(level, player, pos, tool);
			}
		}
	}

	private void breakVein(Level level, Player player, BlockPos startPos, BlockState startState, ItemStack tool) {
		if (level.isClientSide) return;

		Set<BlockPos> toBreak = new HashSet<>();
		Queue<BlockPos> queue = new LinkedList<>();
		Set<BlockPos> visited = new HashSet<>();

		queue.add(startPos);
		visited.add(startPos);

		Block targetBlock = startState.getBlock();
		int maxBlocks = 32;

		while (!queue.isEmpty() && toBreak.size() < maxBlocks) {
			BlockPos current = queue.poll();
			BlockState currentState = level.getBlockState(current);

			if (currentState.getBlock() == targetBlock) {
				toBreak.add(current);

				for (int dx = -1; dx <= 1; dx++) {
					for (int dy = -1; dy <= 1; dy++) {
						for (int dz = -1; dz <= 1; dz++) {
							if (dx == 0 && dy == 0 && dz == 0) continue;

							BlockPos neighbor = current.offset(dx, dy, dz);
							if (!visited.contains(neighbor) &&
									level.isLoaded(neighbor) &&
									level.getBlockState(neighbor).getBlock() == targetBlock) {

								visited.add(neighbor);
								queue.add(neighbor);
							}
						}
					}
				}
			}
		}

		for (BlockPos pos : toBreak) {
			if (!pos.equals(startPos)) {
				breakBlock(level, player, pos, tool);
			}
		}
	}

	private void breakBlock(Level level, Player player, BlockPos pos, ItemStack tool) {
		BlockState state = level.getBlockState(pos);

		state.getBlock().playerDestroy(level, player, pos, state, null, tool);

		level.removeBlock(pos, false);

		if (!player.isCreative()) {
			tool.hurtAndBreak(1, player, (p) -> {
				p.broadcastBreakEvent(player.getUsedItemHand());
			});
		}
	}
}