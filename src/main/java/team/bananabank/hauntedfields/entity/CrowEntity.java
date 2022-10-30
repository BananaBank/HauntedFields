package team.bananabank.hauntedfields.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class CrowEntity extends FlyingMob implements IAnimatable {
    private Vec3 moveTargetPoint = Vec3.ZERO;
    private BlockPos anchorPoint = BlockPos.ZERO;
    private final AnimationFactory factory = new AnimationFactory(this);
    private ScarecrowEntity scarecrow;
    private CrowEntity.AttackPhase attackPhase = CrowEntity.AttackPhase.CIRCLE;

    public CrowEntity(EntityType<? extends CrowEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new CrowMoveControl(this);
    }

    public void setScarecrow(ScarecrowEntity scarecrow) {
        this.scarecrow = scarecrow;
    }

    public static AttributeSupplier createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.FLYING_SPEED, (double)0.8F)
                .build();
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(5, new CrowEntity.CrowCircleAroundAnchorGoal(this));

        this.goalSelector.addGoal(1, new CrowEntity.CrowAttackStrategyGoal(this));
        this.goalSelector.addGoal(2, new CrowEntity.CrowSweepAttackGoal(this));
        this.goalSelector.addGoal(3, new CrowEntity.CrowCircleAroundAnchorGoal(this));
        this.targetSelector.addGoal(1, new CrowEntity.CrowAttackPlayerTargetGoal(this));
        this.targetSelector.addGoal(4, new CrowFollowScarecrowGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    private PlayState predicate(AnimationEvent<CrowEntity> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.crow.fly", true));
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

    @Override
    public void die(DamageSource p_21014_) {
        super.die(p_21014_);
        if (this.scarecrow != null) {
            this.scarecrow.crowDeath();
        }
    }

    enum AttackPhase {
        CIRCLE,
        SWOOP;
    }

    private static class CrowAttackPlayerTargetGoal extends Goal {
        private final CrowEntity crow;
        private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0D);
        private int nextScanTick = reducedTickDelay(20);

        public CrowAttackPlayerTargetGoal(CrowEntity crow) {
            this.crow = crow;
        }

        public boolean canUse() {
            if (nextScanTick > 0) {
                --this.nextScanTick;
            } else {
                nextScanTick = reducedTickDelay(60);
                List<Player> list = crow.level.getNearbyPlayers(attackTargeting, crow, crow.getBoundingBox().inflate(16.0D, 64.0D, 16.0D));

                if (!list.isEmpty()) {
                    list.sort(Comparator.<Entity, Double>comparing(Entity::getY).reversed());

                    for (Player player : list) {
                        if (crow.canAttack(player, TargetingConditions.DEFAULT)) {
                            crow.setTarget(player);
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = crow.getTarget();
            return livingentity != null && crow.canAttack(livingentity, TargetingConditions.DEFAULT);
        }
    }

    private static class CrowAttackStrategyGoal extends Goal {
        private final CrowEntity crow;
        private int nextSweepTick;

        public CrowAttackStrategyGoal(CrowEntity crow) {
            super();
            this.crow = crow;
        }

        public boolean canUse() {
            LivingEntity target = crow.getTarget();
            return target != null && crow.canAttack(target, TargetingConditions.DEFAULT);
        }

        public void start() {
            this.nextSweepTick = this.adjustedTickDelay(10);
            crow.attackPhase = CrowEntity.AttackPhase.CIRCLE;
            this.setAnchorAboveTarget();
        }

        public void stop() {
            crow.anchorPoint = crow.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, crow.anchorPoint).above(10 + crow.random.nextInt(20));
        }

        public void tick() {
            if (crow.attackPhase == CrowEntity.AttackPhase.CIRCLE) {
                --this.nextSweepTick;
                if (this.nextSweepTick <= 0) {
                    crow.attackPhase = CrowEntity.AttackPhase.SWOOP;
                    this.setAnchorAboveTarget();
                    this.nextSweepTick = this.adjustedTickDelay((8 + crow.random.nextInt(4)) * 20);
                    crow.playSound(SoundEvents.PHANTOM_SWOOP, 10.0F, 0.95F + crow.random.nextFloat() * 0.1F);
                }
            }

        }

    private void setAnchorAboveTarget() {
            crow.anchorPoint = crow.getTarget().blockPosition().above(20 + crow.random.nextInt(20));
            if (crow.anchorPoint.getY() < crow.level.getSeaLevel()) {
                crow.anchorPoint = new BlockPos(crow.anchorPoint.getX(), crow.level.getSeaLevel() + 1, crow.anchorPoint.getZ());
            }

        }
    }

    private static class CrowFollowScarecrowGoal extends Goal {

        private final CrowEntity crow;

        private CrowFollowScarecrowGoal(CrowEntity crow) {
            this.crow = crow;
        }

        @Override
        public void tick() {
            crow.anchorPoint = crow.scarecrow.blockPosition().above(20 + crow.random.nextInt(20));
            if (crow.anchorPoint.getY() < crow.level.getSeaLevel()) {
                crow.anchorPoint = new BlockPos(crow.anchorPoint.getX(), crow.level.getSeaLevel() + 1, crow.anchorPoint.getZ());
            };
        }

        @Override
        public boolean canUse() {
            return crow.scarecrow != null;
        }
    }

    // From Phantom
    private static class CrowMoveControl extends MoveControl {
        private final CrowEntity crow;
        private float speed;

        public CrowMoveControl(CrowEntity crow) {
            super(crow);
            this.crow = crow;
        }

        public void tick() {
            if (crow.horizontalCollision) {
                crow.setYRot(crow.getYRot() + 180.0F);
                this.speed = 0.1F;
            }

            double d0 = crow.moveTargetPoint.x - crow.getX();
            double d1 = crow.moveTargetPoint.y - crow.getY();
            double d2 = crow.moveTargetPoint.z - crow.getZ();
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            if (Math.abs(d3) > (double)1.0E-5F) {
                double d4 = 1.0D - Math.abs(d1 * (double)0.7F) / d3;
                d0 *= d4;
                d2 *= d4;
                d3 = Math.sqrt(d0 * d0 + d2 * d2);
                double d5 = Math.sqrt(d0 * d0 + d2 * d2 + d1 * d1);
                float f = crow.getYRot();
                float f1 = (float)Mth.atan2(d2, d0);
                float f2 = Mth.wrapDegrees(crow.getYRot() + 90.0F);
                float f3 = Mth.wrapDegrees(f1 * (180F / (float)Math.PI));
                crow.setYRot(Mth.approachDegrees(f2, f3, 4.0F) - 90.0F);
                crow.yBodyRot = crow.getYRot();
                if (Mth.degreesDifferenceAbs(f, crow.getYRot()) < 3.0F) {
                    this.speed = Mth.approach(this.speed, 1.8F, 0.005F * (1.8F / this.speed));
                } else {
                    this.speed = Mth.approach(this.speed, 0.2F, 0.025F);
                }

                float f4 = (float)(-(Mth.atan2(-d1, d3) * (double)(180F / (float)Math.PI)));
                crow.setXRot(f4);
                float f5 = crow.getYRot() + 90.0F;
                double d6 = (double)(this.speed * Mth.cos(f5 * ((float)Math.PI / 180F))) * Math.abs(d0 / d5);
                double d7 = (double)(this.speed * Mth.sin(f5 * ((float)Math.PI / 180F))) * Math.abs(d2 / d5);
                double d8 = (double)(this.speed * Mth.sin(f4 * ((float)Math.PI / 180F))) * Math.abs(d1 / d5);
                Vec3 vec3 = crow.getDeltaMovement();
                crow.setDeltaMovement(vec3.add((new Vec3(d6, d8, d7)).subtract(vec3).scale(0.2D)));
            }
        }
    }

    // From Phantom
    private abstract static class CrowMoveTargetGoal extends Goal {
        protected CrowEntity crow;
        public CrowMoveTargetGoal(CrowEntity crow) {
            this.crow = crow;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        protected boolean touchingTarget() {
            return crow.moveTargetPoint.distanceToSqr(crow.getX(), crow.getY(), crow.getZ()) < 4.0D;
        }
    }

    // From Phantom
    private static class CrowCircleAroundAnchorGoal extends CrowEntity.CrowMoveTargetGoal {
        private float angle;
        private float distance;
        private float height;
        private float clockwise;

        public CrowCircleAroundAnchorGoal(CrowEntity crow) {
            super(crow);
        }

        public boolean canUse() {
            return crow.getTarget() == null || crow.attackPhase == CrowEntity.AttackPhase.CIRCLE;
        }

        public void start() {
            this.distance = 5.0F + crow.random.nextFloat() * 10.0F;
            this.height = -4.0F + crow.random.nextFloat() * 9.0F;
            this.clockwise = crow.random.nextBoolean() ? 1.0F : -1.0F;
            this.selectNext();
        }

        public void tick() {
            if (crow.random.nextInt(this.adjustedTickDelay(350)) == 0) {
                this.height = -4.0F + crow.random.nextFloat() * 9.0F;
            }

            if (crow.random.nextInt(this.adjustedTickDelay(250)) == 0) {
                ++this.distance;
                if (this.distance > 15.0F) {
                    this.distance = 5.0F;
                    this.clockwise = -this.clockwise;
                }
            }

            if (crow.random.nextInt(this.adjustedTickDelay(450)) == 0) {
                this.angle = crow.random.nextFloat() * 2.0F * (float)Math.PI;
                this.selectNext();
            }

            if (this.touchingTarget()) {
                this.selectNext();
            }

            if (crow.moveTargetPoint.y < crow.getY() && !crow.level.isEmptyBlock(crow.blockPosition().below(1))) {
                this.height = Math.max(1.0F, this.height);
                this.selectNext();
            }

            if (crow.moveTargetPoint.y > crow.getY() && !crow.level.isEmptyBlock(crow.blockPosition().above(1))) {
                this.height = Math.min(-1.0F, this.height);
                this.selectNext();
            }
        }

        private void selectNext() {
            if (BlockPos.ZERO.equals(crow.anchorPoint)) {
                crow.anchorPoint = crow.blockPosition();
            }

            this.angle += this.clockwise * 15.0F * ((float)Math.PI / 180F);
            crow.moveTargetPoint = Vec3.atLowerCornerOf(crow.anchorPoint).add((double)(this.distance * Mth.cos(this.angle)), (double)(-4.0F + this.height), (double)(this.distance * Mth.sin(this.angle)));
        }
    }

    private static class CrowSweepAttackGoal extends CrowEntity.CrowMoveTargetGoal {
        private static final int CAT_SEARCH_TICK_DELAY = 20;
        private boolean isScaredOfCat;
        private int catSearchTick;

        public CrowSweepAttackGoal(CrowEntity crow) {
            super(crow);
        }

        public boolean canUse() {
            return crow.getTarget() != null && crow.attackPhase == CrowEntity.AttackPhase.SWOOP;
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = crow.getTarget();

            if (livingentity == null) {
                return false;
            } else if (!livingentity.isAlive()) {
                return false;
            } else {
                if (livingentity instanceof Player player) {
                    if (livingentity.isSpectator() || player.isCreative()) {
                        return false;
                    }
                }

                if (!this.canUse()) {
                    return false;
                } else {
                    if (crow.tickCount > this.catSearchTick) {
                        this.catSearchTick = crow.tickCount + 20;
                        List<Cat> list = crow.level.getEntitiesOfClass(Cat.class, crow.getBoundingBox().inflate(16.0D), EntitySelector.ENTITY_STILL_ALIVE);

                        for(Cat cat : list) {
                            cat.hiss();
                        }

                        this.isScaredOfCat = !list.isEmpty();
                    }

                    return !this.isScaredOfCat;
                }
            }
        }

        public void start() {
        }

        public void stop() {
            crow.setTarget(null);
            crow.attackPhase = CrowEntity.AttackPhase.CIRCLE;
        }

        public void tick() {
            LivingEntity livingentity = crow.getTarget();

            if (livingentity != null) {
                crow.moveTargetPoint = new Vec3(livingentity.getX(), livingentity.getY(0.5D), livingentity.getZ());
                if (crow.getBoundingBox().inflate(0.2F).intersects(livingentity.getBoundingBox())) {
                    crow.doHurtTarget(livingentity);
                    crow.attackPhase = CrowEntity.AttackPhase.CIRCLE;
                    if (!crow.isSilent()) {
                        crow.level.levelEvent(1039, crow.blockPosition(), 0);
                    }
                } else if (crow.horizontalCollision || crow.hurtTime > 0) {
                    crow.attackPhase = CrowEntity.AttackPhase.CIRCLE;
                }
            }
        }
    }
}
