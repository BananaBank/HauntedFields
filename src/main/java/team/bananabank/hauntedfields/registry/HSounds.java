package team.bananabank.hauntedfields.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import team.bananabank.hauntedfields.HauntedFields;

import java.util.ArrayList;
import java.util.Arrays;

public class HSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HauntedFields.ID);

    public static final RegistryObject<SoundEvent> CROW_CAW = registerSoundEvent("crow_caw");
    public static final RegistryObject<SoundEvent> SWOOP = registerSoundEvent("swoop");



    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> new SoundEvent(new ResourceLocation(HauntedFields.ID, name)));
    }
}
