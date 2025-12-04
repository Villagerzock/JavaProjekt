package net.villagerzock.tileWorld.collision;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;
import java.util.Optional;

import static net.villagerzock.Player.*;

public class VoxelShape {
    private final Voxel[] voxels;
    @Builder(buildMethodName = "finish",builderMethodName = "multiple")
    private VoxelShape(@Singular("add") List<VoxelShape> voxels) {
        int length = 0;
        for (VoxelShape voxelShape : voxels) {
            length += voxelShape.voxels.length;
        }
        this.voxels = new Voxel[length];
        int i = 0;
        for (VoxelShape voxelShape : voxels) {
            for (Voxel voxel : voxelShape.voxels) {
                this.voxels[i++] = voxel;
            }
        }
    }
    private VoxelShape(float x, float y, float w, float h){
        this.voxels = new Voxel[]{new Voxel(x,y,w,h)};
    }

    public static VoxelShape of(float x, float y, float w, float h) {
        return new VoxelShape(x,y,w,h);
    }

    public static VoxelShape empty(){
        return new EmptyVoxelShape();
    }

    public boolean collides(VoxelShape other){
        for (Voxel voxel : other.voxels) {
            if (this.collides(voxel))
                return true;
        }
        return false;
    }
    private boolean collides(Voxel voxel){
        for (Voxel otherVoxel : this.voxels) {
            if (otherVoxel.collides(voxel))
                return true;
        }
        return false;
    }

    public Optional<Float> getVerticalSnap(int tileX, int tileY, float dy, float newY) {
        float bestY = newY;
        for (VoxelShape.Voxel voxel : this.voxels) {
            // Shape in Tile-Koordinaten -> Worldspace
            float voxelTopWorld    = (tileY + voxel.getY()) * TILE_SIZE;
            float voxelBottomWorld = (tileY + voxel.getY() + voxel.getH()) * TILE_SIZE;

            if (dy > 0) {
                // wir bewegen uns nach unten (Player-Bottom geht nach unten)
                float playerBottom = newY + HALF_HEIGHT;

                // nur relevante Shapes: unterhalb der aktuellen Position
                if (playerBottom > voxelTopWorld && playerBottom <= voxelBottomWorld + TILE_SIZE) {
                    float candidateY = voxelTopWorld - HALF_HEIGHT - 0.01f;
                    if (candidateY < bestY) {
                        bestY = candidateY;
                        return Optional.of(bestY);
                    }
                }
            } else if (dy < 0) {
                // wir bewegen uns nach oben (Player-Top geht nach oben)
                float playerTop = newY - HALF_HEIGHT;

                // relevante Shapes: oberhalb der aktuellen Position
                if (playerTop < voxelBottomWorld && playerTop >= voxelTopWorld - TILE_SIZE) {
                    float candidateY = voxelBottomWorld + HALF_HEIGHT + 0.01f;
                    if (candidateY > bestY) {
                        bestY = candidateY;
                        return Optional.of(bestY);
                    }
                }
            }
        }
        return Optional.empty();
    }
    @Getter
    private final class Voxel {
        private final float x;
        private final float y;
        private final float w;
        private final float h;

        private Voxel(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
        public boolean collides(Voxel other) {
            float leftA   = x;
            float rightA  = x + w;
            float topA    = y;
            float bottomA = y + h;

            float leftB   = other.x;
            float rightB  = other.x + other.w;
            float topB    = other.y;
            float bottomB = other.y + other.h;

            return leftA < rightB &&
                    rightA > leftB &&
                    topA < bottomB &&
                    bottomA > topB;
        }
    }

    private static final class EmptyVoxelShape extends VoxelShape {

        private EmptyVoxelShape() {
            super(List.of());
        }

        @Override
        public boolean collides(VoxelShape other) {
            return false;
        }

        @Override
        public Optional<Float> getVerticalSnap(int tileX, int tileY, float dy, float newY) {
            return Optional.empty();
        }
    }
}
