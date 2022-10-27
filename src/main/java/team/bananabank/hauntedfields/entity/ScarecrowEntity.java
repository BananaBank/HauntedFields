package team.bananabank.hauntedfields.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import team.bananabank.hauntedfields.registry.HEntityTypes;

public class ScarecrowEntity extends Monster implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);
    private int activeCrows;
    private final int MAX_CROWS = 5;

    public ScarecrowEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.activeCrows = 0;
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
        this.goalSelector.addGoal(7, new ScarecrowEntity.MoveRandomlyGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new BenefitCropsGoal(this, 0.1D));
        this.goalSelector.addGoal(7, new SpawnCrowsGoal(this));
    }

    private PlayState predicate(AnimationEvent<ScarecrowEntity> event) {
        if (isNightTime(level)) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.scarecrow.idle_night", ILoopType.EDefaultLoopTypes.LOOP));
        } else {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.scarecrow.idle_day", ILoopType.EDefaultLoopTypes.LOOP));
        }

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public static boolean isNightTime(Level level) {
        long dayTime = level.getDayTime();

        return dayTime > 13000L && dayTime < 23999L;
    }

    public boolean canSpawnCrow() {
        return this.activeCrows < MAX_CROWS;
    }

    public void crowDeath() {
        if (this.activeCrows != 0) {
            this.activeCrows--;
        }
    }

    private static class MoveRandomlyGoal extends WaterAvoidingRandomStrollGoal {
        public MoveRandomlyGoal(PathfinderMob mob, double probability) {
            super(mob, probability);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && isNightTime(mob.level);
        }
    }

    private class SpawnCrowsGoal extends Goal {
        protected final ScarecrowEntity scarecrow;

        protected int counter;

        private SpawnCrowsGoal(ScarecrowEntity mob) {
            this.scarecrow = mob;
            this.counter = 0;
        }

        @Override
        public void tick() {
            if (counter < 350) {
                this.counter++;
            } else {
                BlockPos blockPos = scarecrow.blockPosition().offset(0, 2, 0);
                CrowEntity crow = HEntityTypes.CROW.get().create(scarecrow.level);

                crow.setScarecrow(scarecrow);
                crow.moveTo(blockPos, 0.0f, 0.0f);
                level.addFreshEntity(crow);
                scarecrow.activeCrows++;
                counter = 0;
            }
        }

        @Override
        public boolean canUse() {
            return isNightTime(scarecrow.level) && canSpawnCrow();
        }
    }

    private static class BenefitCropsGoal extends Goal {
        protected final PathfinderMob mob;
        protected final double probability;

        public BenefitCropsGoal(PathfinderMob mob, double probability) {
            this.mob = mob;
            this.probability = probability;
        }

        @Override
        public boolean canUse() {
            return !isNightTime(mob.level);
        }

        /**
         * Mostly copied from BeeGrowCropGoal
         * @return true if plant was successfully aged
         */
        private boolean tryPlantGrow(BlockState blockstate, Block block, BlockPos blockPos) {
            boolean flag = false;
            IntegerProperty integerproperty = null;

            if (blockstate.is(BlockTags.BEE_GROWABLES)) {
                if (block instanceof CropBlock cropsBlock) {
                    if (!cropsBlock.isMaxAge(blockstate)) {
                        flag = true;
                        integerproperty = cropsBlock.getAgeProperty();
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
                    ((BonemealableBlock) blockstate.getBlock()).performBonemeal((ServerLevel) mob.level, mob.getRandom(), blockPos, blockstate);
                }

                if (flag) {
                    mob.level.levelEvent(2005, blockPos, 0);
                    mob.level.setBlockAndUpdate(blockPos, blockstate.setValue(integerproperty, blockstate.getValue(integerproperty) + 1));

                    return true;
                }
            }

            return false;
        }

        @Override
        public void tick() {
            if (mob.getRandom().nextDouble() <= this.probability) {
                BlockPos scarecrowPos = mob.blockPosition();
                BlockState blockstate;
                Block block;

                // From isNearWater() in FarmBlock.java
                // randomInCube(random, blocks vertical from center, center pos, blocks horizontal from center)
                for (BlockPos posInRange : randomInCubeBelow(mob.getRandom(), scarecrowPos, 4)) {
                    blockstate = mob.level.getBlockState(posInRange);
                    block = blockstate.getBlock();

                    if (tryPlantGrow(blockstate, block, posInRange)) {
                        // Back out of method if plant is successfully grown. Therefore, only one plant will be able to grow per tick.
                        return;
                    }
                }
            }
        }

        /**
         * From BlockPos.randomInCube(). Changed to limit the cube to only at and below the center position
         * Scarecrow would not be able to scare birds from crops that are above it.
         */
        public static Iterable<BlockPos> randomInCubeBelow(RandomSource random, BlockPos center, int span) {
            // We want half as many blocks in the whole cube because we are only interested in blocks below the scarecrow.
            int count = (int) (Math.pow((span * 2) + 1, 3) / 2);
            return BlockPos.randomBetweenClosed(random, count, center.getX() - span, center.getY() - span, center.getZ() - span, center.getX() + span, center.getY(), center.getZ() + span);
        }
    }
}

