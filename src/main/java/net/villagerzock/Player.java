package net.villagerzock;

import lombok.Getter;
import net.villagerzock.input.KeyBinds;
import net.villagerzock.tileWorld.Tile;
import net.villagerzock.tileWorld.collision.VoxelShape;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.Optional;

public class Player {
    @Getter
    private Vector2f position;
    private Vector2f velocity = new Vector2f();
    private boolean onGround = false;
    private final boolean world;
    private boolean normalGravity;

    public Player(Vector2f position, boolean normalGravity, boolean world) {
        this.position = position;
        this.normalGravity = normalGravity;
        this.world = world;
        // updateOnGround();  // erstmal weglassen, wir machen's über Kollision
    }

    private float gravityDir() {
        // Y nach unten positiv:
        // normalGravity = true  -> nach unten (+1)
        // normalGravity = false -> nach oben (-1)
        return normalGravity ? 1f : -1f;
    }

    public void tick() {
        float g = 0.981f;
        float gDir = gravityDir();

        // Gravitation immer anwenden
        velocity.y += g * gDir;

        // Gravity switchen, KEIN Jump
        int jumps = 0;
        if (KeyBinds.JUMP.wasPressed()) {
            // 1) Grav-Richtung wechseln
            normalGravity = !normalGravity;
            // 2) kleinen Impuls weg vom alten Boden geben
            float newGDir = gravityDir();
            velocity.y = -2.0f * newGDir; // kleiner Kick weg von der Fläche
            onGround = false;
            jumps++;
        }
        if (!world)
            KeyBinds.JUMP.addPressed(jumps);

        // Horizontal
        if (KeyBinds.LEFT.isPressed()) {
            velocity.add(-1, 0);
            if (velocity.x < -5) {
                velocity.x = -5;
            }
        }
        if (KeyBinds.RIGHT.isPressed()) {
            velocity.add(1, 0);
            if (velocity.x > 5) {
                velocity.x = 5;
            }
        }

        // Bewegung anwenden
        this.tryMoving(velocity.x, velocity.y);

        // einfache Reibung
        if (this.velocity.x < 0) {
            this.velocity.add(0.24f, 0);
            if (this.velocity.x > 0) this.velocity.x = 0;
        } else if (this.velocity.x > 0) {
            this.velocity.add(-0.24f, 0);
            if (this.velocity.x < 0) this.velocity.x = 0;
        }

        // KEIN updateOnGround() mehr hier
    }

    // Physics-Constants
    public static final float TILE_SIZE   = 128f;
    public static final float HALF_WIDTH  = 64f;
    public static final float HALF_HEIGHT = 76.0f;
    public static final float HALF_WIDTH_TILES  = HALF_WIDTH / TILE_SIZE;
    public static final float HALF_HEIGHT_TILES = HALF_HEIGHT / TILE_SIZE;


    private void tryMoving(float dx, float dy) {
        moveHorizontal(dx);
        moveVertical(dy);
    }

    private boolean isEmptyTile(int tileX, int tileY) {
        return Main.getCurrentLevel().getTileAt(new Vector2i(tileX, tileY), this.world) == null;
    }

    private boolean isPassableAt(int tileX, int tileY, float futureX, float futureY) {
        Tile tile = Main.getCurrentLevel().getTileAt(new Vector2i(tileX, tileY), this.world);
        if (tile == null) {
            // kein Tile => Luft
            return true;
        }

        VoxelShape tileShape = tile.getCollision();
        if (tileShape == null) {
            // kein Collision-Shape => Luft
            return true;
        }

        // Spielerposition in Tile-Koordinaten (1 = 1 Tile)
        float pxTiles = futureX / TILE_SIZE;
        float pyTiles = futureY / TILE_SIZE;

        // Spieler-AABB in Tile-Koordinaten
        float left   = pxTiles - HALF_WIDTH_TILES;
        float right  = pxTiles + HALF_WIDTH_TILES;
        float top    = pyTiles - HALF_HEIGHT_TILES;
        float bottom = pyTiles + HALF_HEIGHT_TILES;

        // relative Position zur Tile-Position
        // Tile (tileX, tileY) entspricht [tileX, tileX+1] x [tileY, tileY+1]
        float localLeft   = left   - tileX;
        float localTop    = top    - tileY;
        float localWidth  = right  - left;
        float localHeight = bottom - top;

        VoxelShape playerShapeLocal = VoxelShape.of(localLeft, localTop, localWidth, localHeight);

        // wenn die Shapes kollidieren, ist das Tile NICHT passierbar
        return !tileShape.collides(playerShapeLocal);
    }

    private void moveHorizontal(float dx) {
        if (dx == 0) return;

        float dir = Math.signum(dx); // -1 = links, +1 = rechts
        float newX = position.x + dx;
        float futureY = position.y;

        float edgeX = newX + dir * HALF_WIDTH;
        int tileX = (int) Math.floor(edgeX / TILE_SIZE);

        int topY    = (int) Math.floor((futureY - HALF_HEIGHT + 1) / TILE_SIZE);
        int bottomY = (int) Math.floor((futureY + HALF_HEIGHT - 1) / TILE_SIZE);

        boolean passableTop    = isPassableAt(tileX, topY,    newX, futureY);
        boolean passableBottom = isPassableAt(tileX, bottomY, newX, futureY);

        if (passableTop && passableBottom) {
            position.x = newX;
        } else {
            velocity.x = 0;
        }
    }


    private void moveVertical(float dy) {
        if (dy == 0) return;

        float dir = Math.signum(dy); // +1 runter, -1 hoch
        float gDir = gravityDir();   // Richtung "Boden"
        float newY = position.y + dy;

        float edgeY = newY + dir * HALF_HEIGHT;
        int tileY = (int) Math.floor(edgeY / TILE_SIZE);

        float futureX = position.x;

        int leftX  = (int) Math.floor((futureX - HALF_WIDTH + 1) / TILE_SIZE);
        int rightX = (int) Math.floor((futureX + HALF_WIDTH - 1) / TILE_SIZE);

        boolean passableLeft  = isPassableAt(leftX,  tileY, futureX, newY);
        boolean passableRight = isPassableAt(rightX, tileY, futureX, newY);

        if (passableLeft && passableRight) {
            // keine Kollision → einfach bewegen
            position.y = newY;
            if (dir == -gDir) {
                onGround = false;
            }
        } else {
            // Kollision → vertikale Geschwindigkeit killen
            velocity.y = 0;

            // Hier vorher: Snapping stur an tileY * TILE_SIZE
            // Neu: Snapping an tatsächliche Shape-Kante
            float snappedY = resolveVerticalSnap(newY, dy, tileY, leftX, rightX);
            position.y = snappedY;

            // Wenn wir in Richtung Gravitation bewegen und kollidieren → Boden
            if (dir == gDir) {
                onGround = true;
            }
        }
    }

    // hilft beim Auflösen der vertikalen Kollision
    private float resolveVerticalSnap(float newY, float dy, int tileY, int leftX, int rightX) {
        float bestY = newY;
        boolean found = false;

        // wir schauen beide X-Spalten an: leftX und rightX
        for (int tileX : new int[]{leftX, rightX}) {
            Tile tile = Main.getCurrentLevel().getTileAt(new Vector2i(tileX, tileY), this.world);
            if (tile == null) continue;

            VoxelShape shape = tile.getCollision();
            if (shape == null) continue;

            Optional<Float> result = shape.getVerticalSnap(tileX,tileY, dy, newY);
            if (result.isPresent()) {
                found = true;
                bestY = result.get();
                break;
            }
        }

        return found ? bestY : newY;
    }


    // updateOnGround wird nicht mehr für die Hauptlogik benötigt,
    // kann aber bleiben, falls du später Slopes o.Ä. willst.
    private void updateOnGround() {
        float gDir = gravityDir();

        float checkY = position.y + gDir * (HALF_HEIGHT + 1);
        int tileY = (int) Math.floor(checkY / TILE_SIZE);

        int leftX  = (int) Math.floor((position.x - HALF_WIDTH + 1) / TILE_SIZE);
        int rightX = (int) Math.floor((position.x + HALF_WIDTH - 1) / TILE_SIZE);

        boolean empty = isEmptyTile(leftX, tileY) && isEmptyTile(rightX, tileY);
        onGround = !empty;
    }

    public void render(DrawContext context) {
        context.drawSprite("textures/tiles_spritesheet.png->player_walk_1", (int) (position.x - 64), (int) (position.y - (normalGravity ? 180 : -180)),128, (normalGravity ? 256 : -256));
    }
}
