package com.ninni.spawn.block;

import com.ninni.spawn.block.entity.AnthillBlockEntity;
import com.ninni.spawn.entity.Ant;
import com.ninni.spawn.registry.SpawnBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class AnthillBlock extends BaseEntityBlock {

    public AnthillBlock(BlockBehaviour.Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any());
    }
    
    @Override
    public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
        super.playerDestroy(level, player, blockPos, blockState, blockEntity, itemStack);
        if (!level.isClientSide && blockEntity instanceof AnthillBlockEntity anthillBlockEntity) {
            anthillBlockEntity.angerAnts(player, blockState, AnthillBlockEntity.AntState.EMERGENCY);
            level.updateNeighbourForOutputSignal(blockPos, this);
            this.angerNearbyAnts(level, blockPos);
        }
    }

    private void angerNearbyAnts(Level world, BlockPos pos) {
        List<Ant> antList = world.getEntitiesOfClass(Ant.class, new AABB(pos).inflate(8.0, 6.0, 8.0));
        if (!antList.isEmpty()) {
            List<Player> playerList = world.getEntitiesOfClass(Player.class, new AABB(pos).inflate(8.0, 6.0, 8.0));
            for (Ant ant : antList) {
                if (ant.getTarget() != null) continue;
                ant.setTarget(playerList.get(world.random.nextInt(playerList.size())));
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new AnthillBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : AnthillBlock.createTickerHelper(blockEntityType, SpawnBlockEntityTypes.ANTHILL, AnthillBlockEntity::serverTick);
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState blockState, Player player) {
        BlockEntity blockEntity;
        if (!world.isClientSide() && player.isCreative() && world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && (blockEntity = world.getBlockEntity(pos)) instanceof AnthillBlockEntity) {
            AnthillBlockEntity blockEntity1 = (AnthillBlockEntity)blockEntity;
            ItemStack itemStack = new ItemStack(this);
            boolean bl = !blockEntity1.hasNoAnts();
            if (bl) {
                CompoundTag nbtCompound = new CompoundTag();
                nbtCompound.put("Birts", blockEntity1.getAnts());
                BlockItem.setBlockEntityData(itemStack, SpawnBlockEntityTypes.ANTHILL, nbtCompound);
                nbtCompound = new CompoundTag();
                itemStack.addTagElement("BlockStateTag", nbtCompound);
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), itemStack);
                itemEntity.setDefaultPickUpDelay();
                world.addFreshEntity(itemEntity);
            }
        }
        super.playerWillDestroy(world, pos, blockState, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockEntity blockEntity;
        Entity entity = builder.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if ((entity instanceof PrimedTnt || entity instanceof Creeper || entity instanceof WitherSkull || entity instanceof WitherBoss || entity instanceof MinecartTNT) && (blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY)) instanceof AnthillBlockEntity) {
            AnthillBlockEntity blockEntity1 = (AnthillBlockEntity)blockEntity;
            blockEntity1.angerAnts(null, state, AnthillBlockEntity.AntState.EMERGENCY);
        }
        return super.getDrops(state, builder);
    }
}