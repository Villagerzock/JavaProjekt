package net.villagerzock.tileWorld;

import lombok.Getter;
import net.villagerzock.tileWorld.collision.VoxelShape;

import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.List;

public class Tile {
    public static final List<Tile> tiles = new ArrayList<>();

    public static final Tile QUESTION_MARK = register(new Tile("textures/tiles_spritesheet.png->questionmark_block"));
    public static final Tile STONE = register(new Tile("textures/tiles_spritesheet.png->stone"));
    public static final Tile EXIT_PART_0 = register(new Tile("textures/tiles_spritesheet.png->exit_part_1"));
    public static final Tile EXIT_PART_1 = register(new Tile("textures/tiles_spritesheet.png->exit_part_2"));

    private static Tile register(Tile tile){
        tiles.add(tile);
        return tile;
    }
    public Tile(String texture) {
        this.texture = texture;
    }

    public static Tile getTileType(int type){
        return type < 0 ? null : Tile.tiles.get(type);
    }
    @Getter
    private final String texture;

    public VoxelShape getCollision(){
        return VoxelShape.of(0,0,1,1f);
    }
}
