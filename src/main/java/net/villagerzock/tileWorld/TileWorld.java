package net.villagerzock.tileWorld;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import net.villagerzock.Player;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.*;

public class TileWorld implements Iterable<Map.Entry<Vector2i, TileWorld.Chunk>> {
    private float time;
    @Getter
    private Player overworldPlayer;
    @Getter
    private Player underworldPlayer;
    public int getTime() {
        return meta.maxTime - (int)time;
    }

    public static final int CHUNK_SIZE = 16;

    private final HashMap<Vector2i, Chunk> chunks = new HashMap<>();

    private final WorldMeta meta = new WorldMeta(500);

    @Override
    public Iterator<Map.Entry<Vector2i, Chunk>> iterator() {
        return chunks.entrySet().iterator();
    }

    // Hilfsfunktionen für floorDiv/floorMod (korrekt bei negativen Werten)
    private static int floorDiv(int x, int y) {
        int r = x / y;
        // wenn x und y unterschiedliche Vorzeichen haben und x nicht glatt teilbar ist
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }

    private static int floorMod(int x, int y) {
        int r = x % y;
        if ((r < 0 && y > 0) || (r > 0 && y < 0)) {
            r += y;
        }
        return r;
    }

    private static Vector2i worldToChunkPos(Vector2i tilePos) {
        return new Vector2i(
                floorDiv(tilePos.x, CHUNK_SIZE),
                floorDiv(tilePos.y, CHUNK_SIZE)
        );
    }

    private static int localX(int tileX) {
        return floorMod(tileX, CHUNK_SIZE);
    }

    private static int localY(int tileY) {
        return floorMod(tileY, CHUNK_SIZE);
    }

    public void tick() {
        time += 0.01f;
        overworldPlayer.tick();
        underworldPlayer.tick();
    }

    public class Chunk {
        private final int[][] normalWorldTiles = new int[CHUNK_SIZE][CHUNK_SIZE];
        private final int[][] underWorldTiles  = new int[CHUNK_SIZE][CHUNK_SIZE];

        public Chunk() {
            for (int x = 0; x < CHUNK_SIZE; x++) {
                Arrays.fill(normalWorldTiles[x], -1);
                Arrays.fill(underWorldTiles[x], -1);
            }
        }

        public Tile getTileLocal(int lx, int ly, boolean underworld) {
            int id = (underworld ? underWorldTiles : normalWorldTiles)[lx][ly];
            return Tile.getTileType(id);
        }

        public void setTileLocal(int lx, int ly, Tile tile, boolean underworld) {
            (underworld ? underWorldTiles : normalWorldTiles)[lx][ly] =
                    Tile.tiles.indexOf(tile);
        }
    }

    public Tile getTileAt(Vector2i tilePos, boolean underworld) {
        Vector2i chunkPos = worldToChunkPos(tilePos);
        Chunk chunk = chunks.get(chunkPos);
        if (chunk == null) {
            return null;
        }
        int lx = localX(tilePos.x);
        int ly = localY(tilePos.y);
        return chunk.getTileLocal(lx, ly, underworld);
    }
    public TileWorld(){
        overworldPlayer = new Player(new Vector2f(0, -564),true,false);
        underworldPlayer = new Player(new Vector2f(0, 436),false,true);
    }
    public void setTileAt(Vector2i tilePos, Tile tile, boolean underworld) {
        Vector2i chunkPos = worldToChunkPos(tilePos);
        Chunk chunk = chunks.get(chunkPos);
        if (chunk == null) {
            chunk = new Chunk();
            // WICHTIG: neuen Vector2i als Key, nicht existing mutieren
            chunks.put(new Vector2i(chunkPos), chunk);
        }

        int lx = localX(tilePos.x);
        int ly = localY(tilePos.y);

        // Debug, wenn du willst:
        // System.out.printf("World (%d,%d) -> Chunk (%d,%d), Local (%d,%d)%n",
        // tilePos.x, tilePos.y, chunkPos.x, chunkPos.y, lx, ly);

        chunk.setTileLocal(lx, ly, tile, underworld);
    }

    public Chunk getChunkAt(Vector2i worldTilePos, boolean force) {
        Vector2i chunkPos = worldToChunkPos(worldTilePos);
        Chunk chunk = chunks.get(chunkPos);
        if (chunk == null && force) {
            chunk = new Chunk();
            chunks.put(new Vector2i(chunkPos), chunk);
        }
        return chunk;
    }

    public Chunk getChunkAtChunkPos(Vector2i chunkPos) {
        return chunks.get(chunkPos);
    }

    /* =================== DTOs für Gson =================== */

    // Reine Datenklasse für JSON
    private static class ChunkData {
        int chunkX;
        int chunkY;
        int[][] normalWorldTiles;
        int[][] underWorldTiles;
    }

    private static class WorldData {
        List<ChunkData> chunks;
    }

    /* =================== Save / Load mit Gson =================== */

    /**
     * Speichert die Welt als JSON in den gegebenen Writer.
     */
    public void save(Gson gson, Writer writer) throws IOException {

        WorldData data = new WorldData();
        data.chunks = chunks.entrySet().stream().map(entry -> {
            Vector2i pos = entry.getKey();
            Chunk c = entry.getValue();

            ChunkData cd = new ChunkData();
            cd.chunkX = pos.x;
            cd.chunkY = pos.y;

            // tiefe Kopie der Arrays
            cd.normalWorldTiles = new int[CHUNK_SIZE][CHUNK_SIZE];
            cd.underWorldTiles  = new int[CHUNK_SIZE][CHUNK_SIZE];

            for (int x = 0; x < CHUNK_SIZE; x++) {
                System.arraycopy(c.normalWorldTiles[x], 0, cd.normalWorldTiles[x], 0, CHUNK_SIZE);
                System.arraycopy(c.underWorldTiles[x], 0, cd.underWorldTiles[x], 0, CHUNK_SIZE);
            }

            return cd;
        }).toList();

        gson.toJson(data, writer);
        writer.flush();
    }

    /**
     * Lädt eine TileWorld aus JSON.
     * Existierende Daten in der Instanz werden überschrieben.
     */
    public void load(Gson gson, Reader reader) throws IOException {
        Type worldType = new TypeToken<WorldData>() {}.getType();
        WorldData data = gson.fromJson(reader, worldType);
        chunks.clear();

        if (data == null || data.chunks == null) {
            return;
        }

        for (ChunkData cd : data.chunks) {
            Chunk c = new Chunk();
            // falls Arrays aus der Datei nicht genau CHUNK_SIZE sind, vorsichtig kopieren
            for (int x = 0; x < CHUNK_SIZE && x < cd.normalWorldTiles.length; x++) {
                System.arraycopy(cd.normalWorldTiles[x], 0, c.normalWorldTiles[x], 0,
                        Math.min(CHUNK_SIZE, cd.normalWorldTiles[x].length));
                System.arraycopy(cd.underWorldTiles[x], 0, c.underWorldTiles[x], 0,
                        Math.min(CHUNK_SIZE, cd.underWorldTiles[x].length));
            }

            chunks.put(new Vector2i(cd.chunkX, cd.chunkY), c);
        }
    }

    private record WorldMeta(int maxTime) { }
}
