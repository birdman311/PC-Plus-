package com.Birdman311.pcmod;

import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.pokemon.stats.IVStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchFilter {

    public static final String FILTER_SHINY      = "@shiny";
    public static final String FILTER_LEGENDARY  = "@legendary";
    public static final String FILTER_MYTHICAL   = "@mythical";
    public static final String FILTER_ULTRABEAST = "@ultrabeast";
    public static final String FILTER_HA         = "@ha";
    public static final String FILTER_TEXTURED   = "@textured";
    public static final String FILTER_L100       = "@L100"; 
    public static final String FILTER_FAVORITE   = "@favorite";
    public static final String FILTER_70IV       = "@70iv";
    public static final String FILTER_80IV       = "@80iv";
    public static final String FILTER_90IV       = "@90iv";
    public static final String FILTER_100IV      = "@100iv";

    private static final Set<String> LEGENDARIES = new HashSet<>(Arrays.asList(
        "articuno", "zapdos", "moltres", "mewtwo", "raikou", "entei", "suicune", "lugia", "ho-oh", "hooh",
        "regirock", "regice", "registeel", "latias", "latios", "kyogre", "groudon", "rayquaza",
        "uxie", "mesprit", "azelf", "dialga", "palkia", "heatran", "regigigas", "giratina", "cresselia",
        "cobalion", "terrakion", "virizion", "tornadus", "thundurus", "reshiram", "zekrom", "landorus", "kyurem",
        "xerneas", "yveltal", "zygarde", "typenull", "type: null", "silvally", "tapu koko", "tapu lele", "tapu bulu", "tapu fini",
        "cosmog", "cosmoem", "solgaleo", "lunala", "necrozma", "zacian", "zamazenta", "eternatus",
        "kubfu", "urshifu", "regieleki", "regidrago", "glastrier", "spectrier", "calyrex", "enamorus",
        "koraidon", "miraidon", "ting-lu", "chien-pao", "wo-chien", "chi-yu", "okidogi", "munkidori", "fezandipiti", "ogerpon", "terapagos",
        "walking wake", "gouging fire", "raging bolt", "iron leaves", "iron crown", "iron boulder"
    ));

    private static final Set<String> MYTHICALS = new HashSet<>(Arrays.asList(
        "mew", "celebi", "jirachi", "deoxys", "phione", "manaphy", "darkrai", "shaymin", "arceus",
        "victini", "keldeo", "meloetta", "genesect", "diancie", "hoopa", "volcanion", "magearna",
        "marshadow", "zeraora", "meltan", "melmetal", "zarude", "pecharunt"
    ));

    private static final Set<String> ULTRA_BEASTS = new HashSet<>(Arrays.asList(
        "nihilego", "buzzwole", "pheromosa", "xurkitree", "celesteela", "kartana",
        "guzzlord", "poipole", "naganadel", "stakataka", "blacephalon"
    ));

    public static boolean matchesFilter(String filter, Pokemon pokemon) {
        if (pokemon == null) return false;
        
        try {
            String speciesName = "";
            if (pokemon.getSpecies() != null && pokemon.getSpecies().getName() != null) {
                speciesName = pokemon.getSpecies().getName().toLowerCase();
            }
            
            String[] tokens = filter.toLowerCase().trim().split("\\s+");
            
            for (String token : tokens) {
                if (token.isEmpty()) continue;

                if (token.startsWith("@type:")) {
                    if (pokemon.getForm() == null) return false;
                    
                    List<Element> pkmTypes = pokemon.getForm().getTypes();
                    if (pkmTypes == null || pkmTypes.isEmpty() || pkmTypes.get(0) == null) return false;

                    String typeString = token.substring(6); 
                    String[] requestedTypes = typeString.split(",");

                    String type1 = pkmTypes.get(0).name().toLowerCase();
                    String type2 = (pkmTypes.size() > 1 && pkmTypes.get(1) != null) ? pkmTypes.get(1).name().toLowerCase() : "";

                    boolean hasFirst = type1.equals(requestedTypes[0]) || type2.equals(requestedTypes[0]);
                    
                    if (requestedTypes.length == 1) {
                        if (!hasFirst) return false; 
                    } 
                    else if (requestedTypes.length >= 2) {
                        boolean hasSecond = type1.equals(requestedTypes[1]) || type2.equals(requestedTypes[1]);
                        if (!(hasFirst && hasSecond)) return false; 
                    }
                }
                else if (token.contains("iv")) {
                    IVStore ivs = pokemon.getIVs();
                    if (ivs == null) return false;

                    double percentage = ivs.getPercentage(1);

                    if (token.contains("70") && (percentage < 70.0 || percentage >= 80.0)) return false;
                    if (token.contains("80") && (percentage < 80.0 || percentage >= 90.0)) return false;
                    if (token.contains("90") && (percentage < 90.0 || percentage >= 100.0)) return false;

                    if (token.contains("100") && percentage < 100.0) return false;
                }
                else {
                    switch (token) {
                        case "@shiny": 
                            if (!pokemon.isShiny()) return false; 
                            break;
                        case "@legendary": 
                            if (!pokemon.isLegendary() && !LEGENDARIES.contains(speciesName)) return false; 
                            break;
                        case "@mythical": 
                            if (!MYTHICALS.contains(speciesName)) return false; 
                            break;
                        case "@ultrabeast": 
                            if (!pokemon.isUltraBeast() && !ULTRA_BEASTS.contains(speciesName)) return false; 
                            break;
                        case "@ha":
                        case "ha": 
                            if (!pokemon.hasHiddenAbility()) return false; 
                            break;
                        case "@l100":
                        case "l100": 
                            if (pokemon.getPokemonLevel() != 100) return false; 
                            break;
                        case "@textured":
                            if (pokemon.getPalette() == null || pokemon.getPalette().getName() == null) return false;
                            String palette = pokemon.getPalette().getName().toLowerCase();
                            if (palette.equals("none") || palette.equals("default")) return false;
                            break;
                        case "@favorite":
                            if (pokemon.getUUID() == null || !FavoriteManager.isFavorite(pokemon.getUUID())) return false;
                            break;
                        default: 
                            String displayName = "";
                            if (pokemon.getDisplayName() != null) {
                                displayName = pokemon.getDisplayName().toLowerCase();
                            }
                            if (!speciesName.contains(token) && !displayName.contains(token)) {
                                return false;
                            }
                            break;
                    }
                }
            }
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isValidFilter(String input) {
        if (input == null) return false;
        
        String[] tokens = input.trim().toLowerCase().split("\\s+");
        for (String token : tokens) {
            if (token.startsWith("@type:")) return true;
            
            switch (token) {
                case "@shiny":
                case "@legendary":
                case "@mythical":
                case "@ultrabeast":
                case "@ha":
                case "ha":
                case "@textured":
                case "@l100":
                case "l100":
                case "@favorite":
                case "@70iv":
                case "70iv":
                case "@80iv":
                case "80iv":
                case "@90iv":
                case "90iv":
                case "@100iv":
                case "100iv":
                    return true;
            }
        }
        return false;
    }

    public static List<String> getAllFilters() {
        return new ArrayList<>(Arrays.asList(
            FILTER_SHINY, FILTER_LEGENDARY, FILTER_MYTHICAL, 
            FILTER_ULTRABEAST, FILTER_HA, FILTER_TEXTURED, FILTER_L100, 
            FILTER_FAVORITE, FILTER_70IV, FILTER_80IV, FILTER_90IV, FILTER_100IV
        ));
    }
}