package cimmerdev.studbook.util;

import cimmerdev.studbook.data.NodeWidgetData;

import java.util.*;
import java.util.stream.Collectors;

import static cimmerdev.studbook.gui.LineageScreen.*;

/**
 * Calculates X-coordinates for a genealogical tree (DAG) of LineageData nodes.
 *
 * Layout rules:
 *  - Y is determined externally by generation number (1-indexed).
 *  - A child's ideal X is the midpoint of its two parents' X positions.
 *  - Siblings (same parent pair) are spread evenly around that midpoint.
 *  - No two nodes on the same generation share the same X (minimum gap = spacing).
 *  - Inbreeding / shared parents are handled because the graph is a DAG, not a tree:
 *    the same UUID appears once in the data but can parent many children.
 *
 *    (credit to Qwen for helping with this lol)
 */
public class LineageLayoutEngine {

    public static List<NodeWidgetData> calculateLayout(List<NodeWidgetData> nodesData) {
        if (nodesData == null || nodesData.isEmpty()) {
            return Collections.emptyList();
        }
        int spacing = NODE_WIDTH + NODE_PADDING;

        // build lookup structures
        Map<UUID, NodeWidgetData> byId = new HashMap<>();
        for (NodeWidgetData d : nodesData) {
            byId.put(d.uuid, d);
        }

        Map<UUID, Integer> genOf = computeGenerations(byId);

        // create generation groups
        int maxGen = genOf.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        List<List<UUID>> byGeneration = new ArrayList<>(maxGen + 1);
        for (int i = 0; i <= maxGen; i++) {
            byGeneration.add(new ArrayList<>());
        }
        for (UUID id : byId.keySet()) {
            byGeneration.get(genOf.get(id)).add(id);
        }

        // assigning X
        Map<UUID, Double> xPos = new HashMap<>();

        for (int gen = 1; gen <= maxGen; gen++) {
            List<UUID> nodes = byGeneration.get(gen);
            if (nodes.isEmpty()) continue;

            // ideal X - midpoint between parents
            Map<UUID, Double> idealX = new HashMap<>();
            for (UUID id : nodes) {
                idealX.put(id, computeIdealX(id, byId, xPos));
            }

            // group sibling orderly
            Map<String, List<UUID>> siblingGroups = new LinkedHashMap<>();
            for (UUID id : nodes) {
                NodeWidgetData d = byId.get(id);
                String key = parentPairKey(d.getParentA(), d.getParentB());
                siblingGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(id);
            }

            // evenly spread siblings
            Map<UUID, Double> placed = new HashMap<>();
            for (List<UUID> group : siblingGroups.values()) {
                double centre = group.stream()
                        .mapToDouble(idealX::get)
                        .average()
                        .orElse(0.0);

                // sort by UUID value
                group.sort(Comparator.comparing(u -> u.toString()));

                int n = group.size();
                for (int i = 0; i < n; i++) {
                    double offset = (i - (n - 1) / 2.0) * spacing;
                    placed.put(group.get(i), centre + offset);
                }
            }

            enforceMinimumSpacing(placed, spacing);

            // result X positions
            for (UUID id : nodes) {
                xPos.put(id, placed.get(id));
            }
        }

        // dev inserted Y coordinate calculation
        Map<UUID, Integer> Ypos = new HashMap<>();
        for(NodeWidgetData d : nodesData) {
            Ypos.put(d.uuid, (-1 * (NODE_HEIGHT + (NODE_HEIGHT + NODE_PADDING))) * (genOf.get(d.uuid)) * -1);
        }

        return nodesData.stream()
                .map(d -> new NodeWidgetData(
                        (int) Math.round(xPos.getOrDefault(d.uuid, 0.0)),
                        Ypos.getOrDefault(d.uuid, 0),
                        d.uuid,
                        d.entityType,
                        d.name,
                        d.getEntityTag(),
                        d.status,
                        d.breeder,
                        d.getBirthTimeWorld(),
                        d.deathCause,
                        d.getDeathTimeWorld(),
                        d.getParentA(),
                        d.getParentB(),
                        d.children,
                        genOf.getOrDefault(d.uuid, 1)
                ))
                .collect(Collectors.toList());
    }

    private static Map<UUID, Integer> computeGenerations(Map<UUID, NodeWidgetData> byId) {
        Map<UUID, Integer> memo = new HashMap<>();

        for (UUID id : byId.keySet()) {
            computeGen(id, byId, memo, new HashSet<>());
        }
        return memo;
    }

    private static int computeGen(UUID id,
                                  Map<UUID, NodeWidgetData> byId,
                                  Map<UUID, Integer> memo,
                                  Set<UUID> visiting) {
        if (memo.containsKey(id)) return memo.get(id);

        // just in case
        if (visiting.contains(id)) return 1;
        visiting.add(id);

        NodeWidgetData d = byId.get(id);
        UUID pa = d.getParentA();
        UUID pb = d.getParentB();

        int maxParentGen = 0;
        if (pa != null && byId.containsKey(pa)) {
            maxParentGen = Math.max(maxParentGen, computeGen(pa, byId, memo, visiting));
        }
        if (pb != null && byId.containsKey(pb)) {
            maxParentGen = Math.max(maxParentGen, computeGen(pb, byId, memo, visiting));
        }

        visiting.remove(id);
        int gen = maxParentGen + 1;
        memo.put(id, gen);
        return gen;
    }

    private static double computeIdealX(UUID id,
                                        Map<UUID, NodeWidgetData> byId,
                                        Map<UUID, Double> xPos) {
        NodeWidgetData d = byId.get(id);
        UUID pa = d.getParentA();
        UUID pb = d.getParentB();

        boolean hasA = pa != null && xPos.containsKey(pa);
        boolean hasB = pb != null && xPos.containsKey(pb);

        if (hasA && hasB) {
            return (xPos.get(pa) + xPos.get(pb)) / 2;
        } else if (hasA) {
            return xPos.get(pa);
        } else if (hasB) {
            return xPos.get(pb);
        }
        return 0;
    }

    private static String parentPairKey(UUID a, UUID b) {
        if (a == null && b == null) return "null:null";
        if (a == null) return "null:" + b;
        if (b == null) return a + ":null";
        return a.compareTo(b) <= 0 ? a + ":" + b : b + ":" + a;
    }

    private static void enforceMinimumSpacing(Map<UUID, Double> positions, int spacing) {
        if (positions.size() <= 1) return;

        List<UUID> ids = new ArrayList<>(positions.keySet());
        boolean changed = true;
        int passes = 0;
        final int MAX_PASSES = positions.size() * positions.size();

        while (changed && passes < MAX_PASSES) {
            changed = false;
            passes++;

            // sort by current X so we can walk left to right
            ids.sort(Comparator.comparingDouble(u -> positions.getOrDefault(u, 0.0)));

            for (int i = 1; i < ids.size(); i++) {
                UUID prev = ids.get(i - 1);
                UUID curr = ids.get(i);
                double xPrev = positions.get(prev);
                double xCurr = positions.get(curr);

                if (xCurr - xPrev < spacing) {
                    double overlap = spacing - (xCurr - xPrev);
                    positions.put(prev, xPrev - overlap / 2);
                    positions.put(curr, xCurr + overlap / 2);
                    changed = true;
                }
            }

            // check the left boundary (i == 0 vs i == 1 after shift)
            for (int i = 0; i + 1 < ids.size(); i++) {
                UUID a = ids.get(i);
                UUID b = ids.get(i + 1);
                if (positions.get(b) - positions.get(a) < spacing) {
                    double overlap = spacing - (positions.get(b) - positions.get(a));
                    positions.put(a, positions.get(a) - overlap / 2);
                    positions.put(b, positions.get(b) + overlap / 2);
                    changed = true;
                }
            }
        }
    }
}
