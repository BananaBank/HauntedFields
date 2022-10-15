package team.bananabank.hauntedfields.event;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import team.bananabank.hauntedfields.HauntedFields;
import team.bananabank.hauntedfields.entity.ScarecrowEntity;
import team.bananabank.hauntedfields.registry.HEntityTypes;

@Mod.EventBusSubscriber(modid = HauntedFields.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HEventBusEvents {

    @SubscribeEvent
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(HEntityTypes.SCARECROW.get(), ScarecrowEntity.setAttributes());
        event.put(HEntityTypes.CROW.get(), ScarecrowEntity.setAttributes());
    }
}
