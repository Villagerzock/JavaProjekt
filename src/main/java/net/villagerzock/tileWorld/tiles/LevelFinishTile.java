package net.villagerzock.tileWorld.tiles;

import net.villagerzock.tileWorld.Tile;
import net.villagerzock.tileWorld.collision.VoxelShape;

public class LevelFinishTile extends Tile {
    public LevelFinishTile(String texture) {
        super(texture);
    }

    @Override
    public VoxelShape getCollision() {
        return VoxelShape.empty();
    }
}
