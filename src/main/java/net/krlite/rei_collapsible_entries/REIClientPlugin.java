package net.krlite.rei_collapsible_entries;

import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.krlite.rei_collapsible_entries.REICollapsibleEntries.ModEntry.*;

@SuppressWarnings("UnstableApiUsage")
public class REIClientPlugin implements me.shedaniel.rei.api.client.plugins.REIClientPlugin {
    private Text convertToTranslatableText(String prefix, Identifier identifier) {
        return Text.translatable(prefix + "." + identifier.getNamespace() + "." + String.join(".", identifier.getPath()));
    }

    /**
     * Joins all the given sub strings into a single string by underscores, ignoring
     * nulls and empties.
     * <br />
     * <code>["sub", null, "string"] -> ["sub_string"]</code>
     *
     * @param subs The sub strings to join.
     * @return The joined string.
     */
    private String joinAll(String... subs) {
        if (subs.length < 1)
            return "";
        return Arrays.stream(subs).filter(Objects::nonNull).filter(s -> !s.isEmpty()).reduce((f, s) -> f + "_" + s)
                .orElse(subs[0]);
    }

    /**
     * Predicates all entries that are of the given {@link Identifier}.
     *
     * @param piece The identifier to predicate.
     * @return The predicate result.
     * @param <E> The entry type.
     */
    private <E> Predicate<? extends EntryStack<E>> predicateIds(Identifier piece) {
        return entryStack -> entryStack.getIdentifier() != null && entryStack.getIdentifier().equals(piece);
    }

    /**
     * Predicates all entries that start in the given {@link Identifier}.
     *
     * @param leading The identifier to predicate.
     * @return The predicate result.
     * @param <E> The entry type.
     */
    private <E> Predicate<? extends EntryStack<E>> predicateLeadingIds(Identifier leading) {
        return entryStack -> entryStack.getIdentifier() != null
                && entryStack.getIdentifier().getNamespace().equals(leading.getNamespace())
                && entryStack.getIdentifier().getPath().endsWith(leading.getPath());
    }

    /**
     * Predicates all entries that end in the given {@link Identifier}.
     *
     * @param trailing The identifier to predicate.
     * @return The predicate result.
     * @param <E> The entry type.
     */
    private <E> Predicate<? extends EntryStack<E>> predicateTrailingIds(Identifier trailing) {
        return entryStack -> entryStack.getIdentifier() != null
                && entryStack.getIdentifier().getNamespace().equals(trailing.getNamespace())
                && entryStack.getIdentifier().getPath().endsWith(trailing.getPath());
    }

    /**
     * Gets a {@link Text} from the given {@link Identifier}, prefixed
     * with <code>"tag"</code>.
     *
     * @param identifier The identifier.
     * @return The tagged text.
     */
    private Text tag(Identifier identifier) {
        return convertToTranslatableText("tag", identifier);
    }

    /**
     * Gets a {@link Text} from the given {@link Identifier}, prefixed
     * with <code>"col"</code>.
     *
     * @param identifier The identifier.
     * @return The coled text.
     */
    private Text col(Identifier identifier) {
        return convertToTranslatableText("col", identifier);
    }

    /**
     * Registers a collapsible entry from the given {@link net.minecraft.registry.tag.TagKey}.
     *
     * @param registry The registry to register the entry to.
     * @param modEntry The mod entry to register the entry for.
     * @param tagPaths The tag's paths.
     */
    private void registerCollapsibleEntryFromTag(
            CollapsibleEntryRegistry registry,
            REICollapsibleEntries.ModEntry modEntry, String... tagPaths
    ) {
        registry.group(modEntry.id(tagPaths), tag(modEntry.id(tagPaths)),
                EntryIngredients.ofItemTag(modEntry.asItemTag(tagPaths)));
    }

    private static final String[] DYE_COLORS = {
            "black", "red", "green", "brown", "blue", "purple",
            "cyan", "light_gray", "gray", "pink", "lime",
            "yellow", "light_blue", "magenta", "orange", "white"
    };

    @Override
    public void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
        REICollapsibleEntries.LOGGER.info("Registering quality-of-life collapsible entries for REI!");

        types: {
            // Fluids
            registry.group(
                    MC.id("fluids"),
                    col(MC.id("fluids")),
                    entryStack -> entryStack.getType() == VanillaEntryTypes.FLUID
            );

            // Spawn eggs
            registry.group(
                    MC.id("spawn_eggs"),
                    col(MC.id("spawn_eggs")),
                    entryStack ->
                            entryStack.getIdentifier() != null
                                    && entryStack.getIdentifier().getPath().endsWith("spawn_egg")
            );
        }

        tags: {
            {
                Arrays.stream(new String[]{
                        "shulker_boxes", "ores", "dyes"
                }).forEach(tag -> registerCollapsibleEntryFromTag(registry, C, tag));

                // Glass blocks
                registry.group(
                        C.id("glass_blocks"),
                        tag(C.id("glass_blocks")),
                        entryStack ->
                                entryStack.getTagsFor().anyMatch(tag -> tag.equals(C.asItemTag("glass")))
                                        || (entryStack.getType() != VanillaEntryTypes.FLUID
                                        && (TC.checkContains(entryStack.getIdentifier()) || AE2.checkContains(entryStack.getIdentifier()))
                                        && entryStack.getIdentifier().getPath().endsWith("glass")) // Special case for glass in TC & AE2
                );

                // Glass panes
                registry.group(
                        C.id("glass_panes"),
                        tag(C.id("glass_panes")),
                        entryStack ->
                                entryStack.getTagsFor().anyMatch(tag -> tag.equals(C.asItemTag("glass_panes")))
                                        || (entryStack.getType() != VanillaEntryTypes.FLUID
                                        && TC.checkContains(entryStack.getIdentifier())
                                        && entryStack.getIdentifier().getPath().endsWith("glass_pane")) // Special case for glass panes in TC
                );
            }
        }

        // --- Minecraft

        minecraft: {
            // Tags
            Arrays.stream(new String[]{
                    "music_discs", "carpets", "banners", "candles", "beds",
                    "signs", "hanging_signs", "leaves", "logs", "planks",
                    "stairs", "slabs", "doors", "trapdoors", "fence_gates",
                    "boats", "walls", "fences", "trim_templates",
                    "decorated_pot_sherds"
            }).forEach(tag -> registerCollapsibleEntryFromTag(registry, MC, tag));

            // Enchanted books
            registry.group(
                    MC.id("enchanted_books"),
                    col(MC.id("enchanted_books")),
                    predicateIds(Registries.ITEM.getId(Items.ENCHANTED_BOOK))
            );

            // Tipped arrows
            registry.group(
                    MC.id("tipped_arrows"),
                    col(MC.id("tipped_arrows")),
                    predicateIds(Registries.ITEM.getId(Items.TIPPED_ARROW))
            );

            // Paintings
            registry.group(
                    MC.id("paintings"),
                    col(MC.id("paintings")),
                    predicateIds(Registries.ITEM.getId(Items.PAINTING))
            );

            // Goat Horns
            registry.group(
                    MC.id("goat_horns"),
                    col(MC.id("goat_horns")),
                    predicateIds(Registries.ITEM.getId(Items.GOAT_HORN))
            );

            // Goat Horns
            registry.group(
                    MC.id("suspicious_stews"),
                    col(MC.id("suspicious_stews")),
                    predicateIds(Registries.ITEM.getId(Items.SUSPICIOUS_STEW))
            );


            // Potions
            Arrays.stream(new String[]{ null, "lingering", "splash" }).forEach(prefix -> registry.group(
                    MC.id(joinAll(prefix, "potions")),
                    col(MC.id(joinAll(prefix, "potions"))),
                    predicateIds(MC.id(joinAll(prefix, "potion")))
            ));

            // Colored blocks
            Arrays.stream(new String[]{
                    "terracotta", "glazed_terracotta",
                    "concrete", "concrete_powder"
            }).forEach(type -> registry.group(
                    MC.id("blocks", type),
                    col(MC.id("blocks", type)),
                    EntryIngredients.ofItems(Arrays.stream(DYE_COLORS)
                            .map(color -> MC.asItem(joinAll(color, type)))
                            .collect(Collectors.toList()))
            ));

            // Miscellaneous
            Arrays.stream(new String[]{
                    "button", "pressure_plate", "banner_pattern"
            }).forEach(type -> registry.group(
                    MC.id("misc", type),
                    col(MC.id("misc", type)),
                    predicateTrailingIds(MC.id(type))
            ));
        }

        // --- Ad Astra

        adAstra: {
            // Flags
            registry.group(
                    AD_ASTRA.id("flags"),
                    col(AD_ASTRA.id("flags")),
                    predicateTrailingIds(AD_ASTRA.id("flag"))
            );
        }

        // --- Applied Energetics 2

        ae2: {
            // Paint balls
            final String postfix = "paint_ball";
            Arrays.stream(new String[]{ null, "lumen" }).forEach(
                    type -> registry.group(
                            AE2.id(joinAll(type, "paint_balls")),
                            col(AE2.id(joinAll(type, "paint_balls"))),
                            EntryIngredients.ofItems(Arrays.stream(DYE_COLORS)
                                    .map(color -> AE2.asItem(joinAll(color, type, postfix)))
                                    .collect(Collectors.toList())))
            );
        }

        // --- Catwalks LLC.

        catwalksLLC: {
            // Paint rollers
            registry.group(
                    CATWALKS.id("filled_paint_rollers"),
                    tag(CATWALKS.id("filled_paint_rollers")),
                    entryStack ->
                            CATWALKS.checkContains(entryStack.getIdentifier())
                                    && (entryStack.getTagsFor().anyMatch(tag -> tag.equals(CATWALKS.asItemTag("filled_paint_rollers"))
                                    || entryStack.getIdentifier().getPath().contains("filled_paint_rollers")))
            );
        }

        // --- Computer Craft

        computerCraft: {
            // Disks
            registry.group(CC.id("disks"), col(CC.id("disks")), predicateIds(CC.id("disk")));
            {
                final String[] POSTFIXES = { "advanced", "normal" };

                // Turtles and pocket computers
                Arrays.stream(new String[]{ "turtle", "pocket_computer" }).forEach(thing -> registry.group(
                        CC.id("things", thing),
                        col(CC.id("things", thing)),
                        entryStack -> CC.checkContains(entryStack.getIdentifier())
                                && Arrays.stream(POSTFIXES)
                                .map(p -> joinAll(thing, p))
                                .anyMatch(p -> entryStack.getIdentifier().getPath().equals(p))
                ));
            }
        }

        // --- Create

        create: {
            // Stone types
            Arrays.stream(new String[]{
                    "veridium", "scorchia", "scoria", "ochrum", "limestone",
                    "crimsite", "asurine", "tuff", "deepslate", "dripstone",
                    "calcite", "andesite", "diorite", "granite"
            }).forEach(type -> registry.group(
                    CREATE.id("stone_types", type),
                    tag(CREATE.id("stone_types", type)),
                    entryStack ->
                            CREATE.checkContains(entryStack.getIdentifier())
                                    && (entryStack.getTagsFor().anyMatch(tag -> tag.equals(CREATE.asItemTag("stone_types", type)))
                                    || entryStack.getIdentifier().getPath().contains(type))
            ));

            // Copper tiles & shingles
            Arrays.stream(new String[]{ "tile", "shingle" }).forEach(type -> registry.group(
                    CREATE.id("blocks", joinAll("copper", type)),
                    col(CREATE.id("blocks", joinAll("copper", type))),
                    entryStack ->
                            CREATE.checkContains(entryStack.getIdentifier())
                                    && entryStack.getIdentifier().getPath().contains(joinAll("copper", type))
            ));

            // Toolboxes & seats
            Arrays.stream(new String[]{ "toolboxes", "seats" }).forEach(tag ->
                    registerCollapsibleEntryFromTag(registry, CREATE, tag)
            );
        }

        // --- Farmer's Delight

        farmersDelight: {
            // Canvas signs
            registerCollapsibleEntryFromTag(registry, FARMERS_DELIGHT, "canvas_signs");
        }

        // --- Hephaestus

        hephaestus: {
            // Modifiers
            registry.group(
                    TC.id("modifiers"),
                    col(TC.id("modifiers")),
                    EntryType.deferred(TC.id("modifier_entry")),
                    entryStack -> TC.checkContains(entryStack.getIdentifier())
            );

            // Slime helmets
            registry.group(
                    TC.id("slime_helmets"),
                    col(TC.id("slime_helmets")),
                    predicateIds(TC.id("slime_helmet"))
            );

            // Modifier Crystals
            registry.group(
                    TC.id("modifier_crystals"),
                    col(TC.id("modifier_crystals")),
                    predicateIds(TC.id("modifier_crystal"))
            );

            // Platforms
            registry.group(
                    TC.id("platforms"),
                    col(TC.id("platforms")),
                    predicateTrailingIds(TC.id("platform"))
            );

            // Casts
            Arrays.stream(new String[]{"red_sand", "sand", "gold"}).forEach(cast ->
                    registerCollapsibleEntryFromTag(registry, TC, "casts", cast)
            );

            // Tools
            Arrays.stream(new String[]{
                    "cleaver", "sword", "dagger", "scythe", "kama",
                    "broad_axe", "hand_axe", "excavator", "pickadze",
                    "mattock", "vein_hammer", "sledge_hammer", "pickaxe",
                    "crossbow", "longbow"
            }).forEach(tool -> registry.group(
                    TC.id("tools", tool),
                    col(TC.id("tools", tool)),
                    predicateIds(TC.id(tool))
            ));

            // Parts
            Arrays.stream(new String[]{
                    "tough_handle", "tool_handle", "tool_binding",
                    "large_plate", "round_plate", "broad_blade",
                    "small_blade", "broad_axe_head", "small_axe_head",
                    "hammer_head", "pick_head", "repair_kit",
                    "bow_limb", "bow_grip", "bowstring"
            }).forEach(part -> registry.group(
                    TC.id("parts", part),
                    col(TC.id("parts", part)),
                    predicateIds(TC.id(part))
            ));

            // Anvils
            registry.group(
                    TC.id("anvils"),
                    col(TC.id("anvils")),
                    entryStack ->
                            TC.checkContains(entryStack.getIdentifier())
                                    && (entryStack.getIdentifier().getPath().equals("scorched_anvil")
                                    || entryStack.getIdentifier().getPath().equals("tinkers_anvil"))
            );

            // Stations
            Arrays.stream(new String[]{ "part_builder", "tinker_station", "crafting_station" }).forEach(station -> registry.group(
                    TC.id("stations", station),
                    col(TC.id("stations", station)),
                    predicateIds(TC.id(station))
            ));

            // Foundries & Smelteries
            Arrays.stream(new String[]{ "foundry", "smeltery" }).forEach(type -> registry.group(
                    TC.id("blocks", type),
                    tag(TC.id("blocks", type)),
                    EntryIngredients.ofItemTag(TC.asItemTag(type))
            ));

            // Buckets
            registry.group(
                    MC.id("buckets"),
                    col(MC.id("buckets")),
                    entryStack ->
                            ((MC.checkContains(entryStack.getIdentifier())
                                    || TC.checkContains(entryStack.getIdentifier()) /* Also including TC's buckets */
                                    || CREATE.checkContains(entryStack.getIdentifier()) /* Also including CR's buckets */
                                    || INDREV.checkContains(entryStack.getIdentifier()) /* Also including IR's buckets */
                                    || AD_ASTRA.checkContains(entryStack.getIdentifier()) /* Also including AD's buckets */
                            )
                                    && entryStack.getIdentifier().getPath().endsWith("bucket")
                                    && !entryStack.getIdentifier().getPath().equals("potion_bucket") /* Avoid including potion buckets */
                            )
                                    || entryStack.getIdentifier().equals(KIBE.id("liquid_xp_bucket")) /* Also including Kibe's liquid xp bucket */
            );

            // Potion buckets
            registry.group(
                    TC.id("buckets", "potion"),
                    col(TC.id("buckets", "potion")),
                    predicateIds(TC.id("potion_bucket"))
            );

            // Slime grasses
            Arrays.stream(new String[]{ "ichor", "ender", "sky", "earth", "vanilla" }).forEach(type -> registry.group(
                    TC.id("slime_grasses", type),
                    col(TC.id("slime_grasses", type)),
                    entryStack ->
                            TC.checkContains(entryStack.getIdentifier())
                                    && entryStack.getIdentifier().getPath().endsWith(joinAll(type, "slime_grass"))
            ));

            // Slime dirt & congealed slimes & slimes
            Arrays.stream(new String[]{ "slime_dirt", "congealed_slime", "slime" }).forEach(suffix -> registry.group(
                    TC.id("blocks", suffix),
                    col(TC.id("blocks", suffix)),
                    entryStack ->
                            TC.checkContains(entryStack.getIdentifier())
                                    && entryStack.getIdentifier().getPath().endsWith(suffix)
            ));
        }

        // --- Industrial Revolution

        // Modules
        registry.group(
                INDREV.id("modules"),
                col(INDREV.id("modules")),
                entryStack ->
                        INDREV.checkContains(entryStack.getIdentifier())
                                && entryStack.getIdentifier().getPath().startsWith("module")
        );

        // --- Item Filters

        itemFilters: {
            // Filters
            registerCollapsibleEntryFromTag(registry, ITEM_FILTERS, "filters");
        }

        // --- Kibe

        kibe: {
            // Colorful blocks
            Arrays.stream(new String[]{ "sleeping_bag", "glider", "rune", "elevator" }).forEach(type -> registry.group(
                    KIBE.id("things", type),
                    col(KIBE.id("things", type)),
                    EntryIngredients.ofItems(Stream.concat(
                            Arrays.stream(DYE_COLORS).map(color -> KIBE.asItem(joinAll(color, type))),
                            Objects.equals(type, "glider") // The glider has right/left wings
                                    ? Stream.of(KIBE.asItem("glider_right_wing"), KIBE.asItem("glider_left_wing"))
                                    : Stream.empty()
                            )
                            .collect(Collectors.toList()))
            ));
        }

        // --- Promenade

        promenade: {
            // Piles
            registry.group(
                    PROMENADE.id("piles"),
                    col(PROMENADE.id("piles")),
                    entryStack ->
                            PROMENADE.checkContains(entryStack.getIdentifier())
                                    && entryStack.getIdentifier().getPath().endsWith("pile")
            );

            // Mushrooms & mushroom blocks
            Arrays.stream(new String[]{ null, "block" }).forEach(type -> registry.group(
                    PROMENADE.id("blocks", joinAll("mushroom", type)),
                    col(PROMENADE.id("blocks", joinAll("mushroom", type))),
                    entryStack ->
                            PROMENADE.checkContains(entryStack.getIdentifier())
                                    && entryStack.getIdentifier().getPath().contains(joinAll("mushroom", type))
            ));
        }
    }
}
