package com.Birdman311.pcmod;

import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;

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
        
        String speciesName = pokemon.getSpecies().getName().toLowerCase();
        
        // Split the filter by spaces so we can process "@legendary @type:fire" 
        String[] tokens = filter.toLowerCase().trim().split("\\s+");
        
        for (String token : tokens) {
            if (token.isEmpty()) continue;

            // --- YOUR 1.0.3 TYPE FILTER LOGIC ---
            if (token.startsWith("@type:")) {
                String typeString = token.substring(6); 
                String[] requestedTypes = typeString.split(",");

                List<Element> pkmTypes = pokemon.getForm().getTypes();
                if (pkmTypes == null || pkmTypes.isEmpty()) return false;

                String type1 = pkmTypes.get(0).name().toLowerCase();
                String type2 = pkmTypes.size() > 1 ? pkmTypes.get(1).name().toLowerCase() : "";

                boolean hasFirst = type1.equals(requestedTypes[0]) || type2.equals(requestedTypes[0]);
                
                if (requestedTypes.length == 1) {
                    if (!hasFirst) return false; // Fail if it doesn't have the first type
                } 
                else if (requestedTypes.length >= 2) {
                    boolean hasSecond = type1.equals(requestedTypes[1]) || type2.equals(requestedTypes[1]);
                    if (!(hasFirst && hasSecond)) return false; // Fail if it doesn't have both types
                }
            }
            // -----------------------------------
            else {
                // Check all the other standard modifiers
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
                        String palette = pokemon.getPalette().getName().toLowerCase();
                        if (palette.equals("none") || palette.equals("default")) return false;
                        break;
                    default: 
                        // If they just typed a name (e.g., "charizard")
                        if (!speciesName.contains(token) && !pokemon.getDisplayName().toLowerCase().contains(token)) {
                            return false;
                        }
                        break;
                }
            }
        }
        
        // If the Pokemon survived every single token check, it's a perfect match!
        return true;
    }

    public static boolean isValidFilter(String input) {
        if (input == null) return false;
        
        // Check if ANY word in the search box is one of our special mod tags
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
                    return true;
            }
        }
        return false;
    }

    public static List<String> getAllFilters() {
        return new ArrayList<>(Arrays.asList(
            FILTER_SHINY, FILTER_LEGENDARY, FILTER_MYTHICAL, 
            FILTER_ULTRABEAST, FILTER_HA, FILTER_TEXTURED, FILTER_L100
        ));
    }
}