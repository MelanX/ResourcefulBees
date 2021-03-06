package com.resourcefulbees.resourcefulbees.item;

import com.resourcefulbees.resourcefulbees.api.beedata.ColorData;
import com.resourcefulbees.resourcefulbees.lib.BeeConstants;
import com.resourcefulbees.resourcefulbees.utils.color.Color;
import com.resourcefulbees.resourcefulbees.utils.color.RainbowColor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class BeeSpawnEggItem extends SpawnEggItem {

	private final Lazy<? extends EntityType<?>> entityType;
	private final ColorData colorData;

	public BeeSpawnEggItem(RegistryObject<? extends EntityType<?>> entityTypeSupplier, int firstColor, int secondColor, ColorData colorData, Properties properties) {
		super(null, firstColor, secondColor, properties);
		this.entityType = Lazy.of(entityTypeSupplier);
		this.colorData = colorData;
	}

    @Nonnull
	@Override
	public EntityType<?> getType(@Nullable final CompoundNBT nbt) {
		return entityType.get();
	}


    public static int getColor(ItemStack stack, int tintIndex) {
	    ColorData colorData = ((BeeSpawnEggItem)stack.getItem()).colorData;
	    int primaryColor = Color.parseInt(BeeConstants.VANILLA_BEE_COLOR);
	    int secondaryColor = 0x303030;

	    if (colorData.hasPrimaryColor()) {
            primaryColor = colorData.getPrimaryColorInt();
        } else if (colorData.isRainbowBee()) {
            primaryColor = RainbowColor.getRGB();
        } else if (colorData.hasHoneycombColor()) {
            primaryColor = colorData.getHoneycombColorInt();
        }
        if (colorData.hasSecondaryColor()) {
            secondaryColor = colorData.getSecondaryColorInt();
        }

        return tintIndex == 0 ? primaryColor : secondaryColor;
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ItemStack itemstack = context.getItem();
        PlayerEntity player = context.getPlayer();
        if (player != null) {
            World world = context.getWorld();
            if (world.isRemote) {
                return ActionResultType.SUCCESS;
            } else {
                BlockPos blockpos = context.getPos();
                Direction direction = context.getFace();
                BlockState blockstate = world.getBlockState(blockpos);

                BlockPos blockpos1;
                if (blockstate.getCollisionShape(world, blockpos).isEmpty()) {
                    blockpos1 = blockpos;
                } else {
                    blockpos1 = blockpos.offset(direction);
                }

                EntityType<?> entitytype = this.getType(itemstack.getTag());
                if (entitytype.spawn((ServerWorld) world, itemstack, context.getPlayer(), blockpos1, SpawnReason.SPAWN_EGG, true, !Objects.equals(blockpos, blockpos1) && direction == Direction.UP) != null) {
                    itemstack.shrink(1);
                }

                return ActionResultType.CONSUME;
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Nonnull
    @Override
    public Optional<MobEntity> spawnBaby(@Nonnull PlayerEntity playerEntity, @Nonnull MobEntity mobEntity, @Nonnull EntityType<? extends MobEntity> entityType, @Nonnull ServerWorld world, @Nonnull Vector3d vector3d, @Nonnull ItemStack stack) {
        return Optional.empty();
    }
}