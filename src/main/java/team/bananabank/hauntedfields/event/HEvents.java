package team.bananabank.hauntedfields.event;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.bananabank.hauntedfields.entity.CrowEntity;
import team.bananabank.hauntedfields.entity.ScarecrowEntity;
import team.bananabank.hauntedfields.registry.HEntityTypes;

public class HEvents {
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(HEntityTypes.SCARECROW.get(), ScarecrowEntity.setAttributes());
        event.put(HEntityTypes.CROW.get(), CrowEntity.createAttributes());
    }

    public static void blockPlaceEvent(BlockEvent.EntityPlaceEvent event) {
        var level = event.getLevel();

        if (event.getPlacedBlock().getBlock() == Blocks.CARVED_PUMPKIN && level.getClass() != WorldGenRegion.class) {
            var top = event.getPos();
            var middle = top.below();
            var bottom = middle.below();

            var middleState = level.getBlockState(middle);
            var bottomState = level.getBlockState(bottom);

            if (middleState.is(Blocks.HAY_BLOCK) && bottomState.is(BlockTags.WOODEN_FENCES)) {
                var topState = level.getBlockState(top);

                level.setBlock(top, Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(middle, Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(bottom, Blocks.AIR.defaultBlockState(), 2);
                level.levelEvent(2001, top, Block.getId(topState));
                level.levelEvent(2001, middle, Block.getId(middleState));
                level.levelEvent(2001, bottom, Block.getId(bottomState));

                var scarecrow = new ScarecrowEntity(HEntityTypes.SCARECROW.get(), (Level) level);
                scarecrow.setPos(bottom.getX() + 0.5, bottom.getY(), bottom.getZ() + 0.5);
                level.addFreshEntity(scarecrow);

                level.blockUpdated(top, Blocks.AIR);
                level.blockUpdated(middle, Blocks.AIR);
                level.blockUpdated(bottom, Blocks.AIR);
            }
        }
    }
}
