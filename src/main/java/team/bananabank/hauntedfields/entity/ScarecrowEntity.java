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
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import team.bananabank.hauntedfields.registry.HEntityTypes;

public class ScarecrowEntity extends Monster implements IAnimatable {
    private AnimationFactory factory = new AnimationFactory(this);
    private int activeCrows;

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
        this.addBehaviorGoals();
    }

    protected void addBehaviorGoals() {
        //this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        //this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0D, true, 4, () -> true));
        this.goalSelector.addGoal(7, new ScarecrowEntity.MoveRandomlyGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new BenefitCropsGoal(this, 0.1D));
        this.goalSelector.addGoal(7, new SpawnCrowsGoal(this));
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

    public boolean canSpawnCrow() {
        return this.activeCrows < 5;
    }

    public void crowDeath() {
        if(this.activeCrows != 0) {
            this.activeCrows--;
        }
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

    private class SpawnCrowsGoal extends Goal {

        protected final ScarecrowEntity mob;

        private SpawnCrowsGoal(ScarecrowEntity mob) {
            this.mob = mob;
        }

        @Override
        public void start() {
            BlockPos blockPos = mob.blockPosition().offset(0, 0, 15);
            CrowEntity crow = HEntityTypes.CROW.get().create(mob.level);
            crow.setScarecrow(mob);
            ServerLevel serverLevel = (ServerLevel)mob.level;
            crow.moveTo(blockPos, 0.0f, 0.0f);
            serverLevel.addFreshEntity(crow);
        }

        @Override
        public boolean canUse() {
            return isNightTime(mob.level) && canSpawnCrow();
        }
    }

    private static class BenefitCropsGoal extends Goal {
        protected final PathfinderMob mob;
        protected final double probability;

        /**
         * @param mob
         * @param probability Probability that per tick a crop will grow
         */
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
         * @param blockstate
         * @param block
         * @param blockPos
         * @return t/f if plant was successfully aged
         */
        private boolean tryPlantGrow(BlockState blockstate, Block block, BlockPos blockPos) {
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
                    ((BonemealableBlock) blockstate.getBlock()).performBonemeal((ServerLevel) mob.level, mob.getRandom(), blockPos, blockstate);
                }

                if (flag) {
                    mob.level.levelEvent(2005, blockPos, 0);
                    mob.level.setBlockAndUpdate(blockPos, blockstate.setValue(integerproperty, Integer.valueOf(blockstate.getValue(integerproperty) + 1)));
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
                // randomInCube(random, blocks verticle from center, center pos, blocks horizontal from center)
                for (BlockPos posInRange : randomInCubeBelow(mob.getRandom(), scarecrowPos, 4)) {
                    blockstate = mob.level.getBlockState(posInRange);
                    block = blockstate.getBlock();
                    if (tryPlantGrow(blockstate, block, posInRange)) {
                        return; // Back out of method if plant is successfully grown. Therefore, only one plant will be able to grow per tick.
                    }
                }
            }
        }

        /**
         * From BlockPos.randomInCube(). Changed to limit the cube to only at and below the center position
         * Scarecrow would not be able to scare birds from crops that are above it.
         */
        public static Iterable<BlockPos> randomInCubeBelow(RandomSource random, BlockPos center, int span) {
            int count = (int) (Math.pow((span * 2) + 1, 3) / 2); // We want half as many blocks in the whole cube because we are only interested in blocks below the scarecrow.
            return BlockPos.randomBetweenClosed(random, count, center.getX() - span, center.getY() - span, center.getZ() - span, center.getX() + span, center.getY(), center.getZ() + span);
        }
    }
}

