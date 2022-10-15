package team.bananabank.hauntedfields.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import team.bananabank.hauntedfields.HauntedFields;
import team.bananabank.hauntedfields.entity.ScarecrowEntity;

public class ScarecrowRenderer extends GeoEntityRenderer<ScarecrowEntity> {
    public ScarecrowRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ScarecrowModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public ResourceLocation getTextureLocation(ScarecrowEntity instance) {
        return new ResourceLocation(HauntedFields.ID, "textures/entity/scarecrow/scarecrow.png");
    }

    @Override
    public RenderType getRenderType(ScarecrowEntity animatable,
                                    float partialTicks,
                                    PoseStack stack,
                                    @Nullable MultiBufferSource renderTypeBuffer,
                                    @Nullable VertexConsumer vertexBuilder,
                                    int packedLightIn,
                                    ResourceLocation textureLocation) {
        stack.scale(1.0f, 1.0f, 1.0f);
        return super.getRenderType(animatable, partialTicks, stack, renderTypeBuffer, vertexBuilder, packedLightIn, textureLocation);
    }
}
