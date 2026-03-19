package com.aid;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public class levitate {private static final int RADIUS = 15; // Радиус меньше для теста
    private static boolean isActive = false;
    private static BlockPos machinePos = null;
    private static final List<FallingBlockEntity> floatingBlocks = new ArrayList<>();
    private static final Map<BlockPos, BlockState> originalBlocks = new HashMap<>();

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClientSide) return InteractionResult.PASS;

            BlockPos pos = hit.getBlockPos();
            if (world.getBlockState(pos).getBlock() instanceof LeverBlock) {
                checkAndToggle(player, world, pos);
            }
            return InteractionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!isActive || machinePos == null) return;

            // Заставляем блоки парить
            for (FallingBlockEntity block : floatingBlocks) {
                if (!block.isRemoved()) {
                    block.setNoGravity(true);
                    block.setDeltaMovement(0, 0.02, 0); // Медленно вверх

                    // Частицы вокруг каждого блока
                    server.getPlayerList().getPlayers().forEach(p -> {
                        p.level().addParticle(
                                ParticleTypes.END_ROD,
                                block.getX(), block.getY() + 0.5, block.getZ(),
                                0.0d, 0.05d, 0.0d
                        );
                    });
                }
            }

            // Частицы вокруг машины
            server.getPlayerList().getPlayers().forEach(p -> {
                p.level().addParticle(
                        ParticleTypes.PORTAL,
                        machinePos.getX() + 0.5, machinePos.getY() + 1, machinePos.getZ() + 0.5,
                        (Math.random() - 0.5) * 2, Math.random() * 2, (Math.random() - 0.5) * 2
                );
            });
        });
    }

    private static void checkAndToggle(Player player, Level world, BlockPos leverPos) {
        // Проверка конструкции
        boolean hasLamp = false, hasNote = false;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos checkPos = leverPos.offset(dx, dy, dz);
                    BlockState state = world.getBlockState(checkPos);

                    if (state.is(Blocks.REDSTONE_LAMP)) hasLamp = true;
                    if (state.is(Blocks.NOTE_BLOCK)) hasNote = true;
                }
            }
        }

        if (hasLamp && hasNote) {
            isActive = !isActive;
            machinePos = leverPos;

            if (isActive) {
                startLevitation(world, leverPos, player);
            } else {
                stopLevitation(world, leverPos, player);
            }

            // Звук
            world.playSound(null, leverPos,
                    isActive ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE,
                    SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    private static void startLevitation(Level world, BlockPos pos, Player player) {
        floatingBlocks.clear();
        originalBlocks.clear();

        player.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§d⚡ ЛЕВИТАЦИЯ АКТИВИРОВАНА!"),
                false
        );

        // Поднимаем блоки
        int count = 0;
        for (int x = -RADIUS; x <= RADIUS && count < 50; x++) {
            for (int y = -RADIUS/2; y <= RADIUS/2 && count < 50; y++) {
                for (int z = -RADIUS; z <= RADIUS && count < 50; z++) {
                    BlockPos blockPos = pos.offset(x, y, z);
                    BlockState state = world.getBlockState(blockPos);

                    // Не поднимаем воздух, бедрок и части машины
                    if (!state.isAir() &&
                            !state.is(Blocks.BEDROCK) &&
                            !state.is(Blocks.REDSTONE_LAMP) &&
                            !state.is(Blocks.NOTE_BLOCK) &&
                            !state.is(Blocks.LEVER) &&
                            !state.is(Blocks.COMMAND_BLOCK)) {

                        // Сохраняем оригинал
                        originalBlocks.put(blockPos.immutable(), state);

                        // Создаем падающий блок
                        FallingBlockEntity fallingBlock = FallingBlockEntity.fall(world, blockPos, state);
                        fallingBlock.setNoGravity(true);
                        fallingBlock.time = -1000000;
                       // fallingBlock.dropItems = false;

                        // Удаляем оригинальный блок
                        world.removeBlock(blockPos, false);

                        // Добавляем в список
                        floatingBlocks.add(fallingBlock);
                        world.addFreshEntity(fallingBlock);

                        count++;

                        // Частицы при старте
                        world.addParticle(
                                ParticleTypes.EXPLOSION,
                                blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                                0.0d, 1.0d, 0.0d
                        );
                    }
                }
            }
        }

        player.displayClientMessage(
                Component.literal("§aПоднято блоков: §e" + count),
                true
        );
    }

    private static void stopLevitation(Level world, BlockPos pos, Player player) {
        player.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§8⚡ ЛЕВИТАЦИЯ ОТКЛЮЧЕНА"),
                false
        );

        // Возвращаем блоки
        for (FallingBlockEntity block : floatingBlocks) {
            if (!block.isRemoved()) {
                BlockPos blockPos = block.blockPosition();
                BlockState originalState = originalBlocks.get(blockPos);

                if (originalState != null) {
                    world.setBlock(blockPos, originalState, 3);
                }
                block.discard();
            }
        }

        floatingBlocks.clear();
        originalBlocks.clear();
    }
}