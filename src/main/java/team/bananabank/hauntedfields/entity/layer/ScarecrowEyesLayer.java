package team.bananabank.hauntedfields.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib3.GeckoLib;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import team.bananabank.hauntedfields.HauntedFields;
import team.bananabank.hauntedfields.entity.ScarecrowEntity;
import team.bananabank.hauntedfields.entity.client.ScarecrowModel;
import team.bananabank.hauntedfields.entity.client.ScarecrowRenderer;

public class ScarecrowEyesLayer extends GeoLayerRenderer {
    private static final ResourceLocation LAYER = new ResourceLocation(HauntedFields.ID, "textures/entity/scarecrow/scarecrow_eyes.png");
    // A resource location for the model of the entity. This model is put on top of the normal one, which is then given the texture
    private static final ResourceLocation MODEL = new ResourceLocation(HauntedFields.ID, "geo/scarecrow.geo.json");

    public ScarecrowEyesLayer(IGeoRenderer entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Entity entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (ScarecrowEntity.isNightTime(entityLivingBaseIn.level)) {
            RenderType cameo =  RenderType.eyes(LAYER);
            matrixStackIn.pushPose();
            //Move or scale the model as you see fit
            matrixStackIn.scale(1.0f, 1.0f, 1.0f);
            matrixStackIn.translate(0.0d, 0.0d, 0.0d);
            this.getRenderer().render(this.getEntityModel().getModel(MODEL), entityLivingBaseIn, partialTicks, cameo, matrixStackIn, bufferIn,
                    bufferIn.getBuffer(cameo), packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
            matrixStackIn.popPose();
        }
    }
}
