package team.bananabank.hauntedfields.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.FloatTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import team.bananabank.hauntedfields.registry.HEntityTypes;

import java.nio.file.Path;

public class ScarecrowEntity extends Monster implements IAnimatable {
    private static final int MAX_CROWS = 5;

    private final AnimationFactory factory = new AnimationFactory(this);
    private int activeCrows;

    public ScarecrowEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.activeCrows = 0;
    }

    public static AttributeSupplier setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .build();
    }

    protected void registerGoals() {
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(0, new ScarecrowEntity.ScarecrowFloat(this));
        this.goalSelector.addGoal(8, new ScarecrowEntity.LookPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new ScarecrowEntity.RandomLookGoal(this));
        this.goalSelector.addGoal(7, new BenefitCropsGoal(this, 0.05));
        this.goalSelector.addGoal(7, new SpawnCrowsGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void registerControllers(AnimationData data) {
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

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return isNightTime(this.level) ? SoundEvents.ZOMBIE_VILLAGER_AMBIENT : null;
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

    private static class RandomLookGoal extends RandomLookAroundGoal {
        private final Mob mob;

        public RandomLookGoal(Mob mob) {
            super(mob);
            this.mob = mob;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && isNightTime(mob.level);
        }
    }

    private static class LookPlayerGoal extends LookAtPlayerGoal {
        public LookPlayerGoal(Mob mob, Class<? extends LivingEntity> type, float chance) {
            super(mob, type, chance);
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
            boolean canGrow = false;
            IntegerProperty integerproperty = null;

            if (blockstate.is(BlockTags.BEE_GROWABLES)) {
                if (block instanceof CropBlock cropsBlock) {
                    if (!cropsBlock.isMaxAge(blockstate)) {
                        canGrow = true;
                        integerproperty = cropsBlock.getAgeProperty();
                    }
                } else if (block instanceof StemBlock) {
                    int j = blockstate.getValue(StemBlock.AGE);

                    if (j < 7) {
                        canGrow = true;
                        integerproperty = StemBlock.AGE;
                    }
                } else if (blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
                    int k = blockstate.getValue(SweetBerryBushBlock.AGE);

                    if (k < 3) {
                        canGrow = true;
                        integerproperty = SweetBerryBushBlock.AGE;
                    }
                } else if (blockstate.is(Blocks.CAVE_VINES) || blockstate.is(Blocks.CAVE_VINES_PLANT)) {
                    ((BonemealableBlock) blockstate.getBlock()).performBonemeal((ServerLevel) mob.level, mob.getRandom(), blockPos, blockstate);
                }

                if (canGrow) {
                    int others = mob.level.getEntities(mob, mob.getBoundingBox().inflate(3.0, 1.0, 3.0), ScarecrowEntity.class::isInstance).size();

                    if (others == 0 || mob.getRandom().nextInt(1 + others * 2) < 1) {
                        mob.level.levelEvent(2005, blockPos, 0);
                        mob.level.setBlockAndUpdate(blockPos, blockstate.setValue(integerproperty, blockstate.getValue(integerproperty) + 1));

                        return true;
                    }
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
    private static class ScarecrowFloat extends FloatGoal {
        protected final Mob mob;
        public ScarecrowFloat(Mob mob) {
            super(mob);
            this.mob = mob;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && isNightTime(this.mob.level);
        }
    }

}

