package team.bananabank.hauntedfields.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.entity.EntityAccess;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.ArrayList;

public class ScarecrowEntity extends Monster implements IAnimatable {
    private AnimationFactory factory = new AnimationFactory(this);

    public ScarecrowEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, (double)0.3F)
                .build();
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.addBehaviorGoals();
    }

    protected void addBehaviorGoals() {
        //this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        //this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0D, true, 4, () -> true));
        this.goalSelector.addGoal(7, new ScarecrowEntity.MoveRandomlyGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new BenefitCropsGoal(this, 0.1D));
        //this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(ZombifiedPiglin.class)); // Add nearby crows if added to mod
        //this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        //this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        //this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        //this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (isNightTime(level)) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.scarecrow.idle_night", true));
        } else {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.scarecrow.idle_day", true));
        }

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public static boolean isNightTime(Level level) {
        long dayTime = level.getDayTime();

        return dayTime > 13000L && dayTime < 23999L;
    }

    private static class MoveRandomlyGoal extends WaterAvoidingRandomStrollGoal {
        public MoveRandomlyGoal(PathfinderMob mob, double probability) {
            super(mob, probability);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !isNightTime(mob.level);
        }
    }

    private class BenefitCropsGoal extends Goal {
        protected final PathfinderMob mob;
        protected final double probability;

        public BenefitCropsGoal(PathfinderMob mob, double probability) {
            this.mob = mob;
            // How often will scarecrow as % decimal
            this.probability = probability;
        }
        @Override
        public boolean canUse() {
            return !isNightTime(mob.level);
        }


        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            // Mostly from bee class BeeGrowCropGoal class.

            if (ScarecrowEntity.this.random.nextDouble() <= this.probability) {
                BlockPos blockpos = ScarecrowEntity.this.blockPosition();
                BlockState blockstate = ScarecrowEntity.this.level.getBlockState(blockpos);
                Block block = blockstate.getBlock();

                // Deal with bug where block at entity position was counting the farmland below
                BlockPos blockposAbove = ScarecrowEntity.this.blockPosition().above();
                BlockState blockstateAbove = ScarecrowEntity.this.level.getBlockState(blockposAbove);
                Block blockAbove = blockstateAbove.getBlock();

                if (block instanceof FarmBlock) {
                    blockpos = blockposAbove;
                    blockstate = blockstateAbove;
                    block = blockAbove;
                }

                boolean flag = false;
                IntegerProperty integerproperty = null;


                if (blockstate.is(BlockTags.BEE_GROWABLES)) {
                    if (block instanceof CropBlock) {
                        CropBlock cropblock = (CropBlock) block;
                        if (!cropblock.isMaxAge(blockstate)) {
                            flag = true;
                            integerproperty = cropblock.getAgeProperty();
                        }
                    } else if (block instanceof StemBlock) {
                        int j = blockstate.getValue(StemBlock.AGE);
                        if (j < 7) {
                            flag = true;
                            integerproperty = StemBlock.AGE;
                        }
                    } else if (blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
                        int k = blockstate.getValue(SweetBerryBushBlock.AGE);
                        if (k < 3) {
                            flag = true;
                            integerproperty = SweetBerryBushBlock.AGE;
                        }
                    } else if (blockstate.is(Blocks.CAVE_VINES) || blockstate.is(Blocks.CAVE_VINES_PLANT)) {
                        ((BonemealableBlock) blockstate.getBlock()).performBonemeal((ServerLevel) ScarecrowEntity.this.level, ScarecrowEntity.this.random, blockpos, blockstate);
                    }

                    if (flag) {
                        ScarecrowEntity.this.level.levelEvent(2005, blockpos, 0);
                        ScarecrowEntity.this.level.setBlockAndUpdate(blockpos, blockstate.setValue(integerproperty, Integer.valueOf(blockstate.getValue(integerproperty) + 1)));
                    }
                }
            }
        }
    }
}
