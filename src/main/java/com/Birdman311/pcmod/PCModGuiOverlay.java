package com.Birdman311.pcmod;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCBox;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.client.gui.pc.PCScreen;
import com.pixelmonmod.pixelmon.client.gui.pc.PokemonScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = PCMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PCModGuiOverlay {

    private static final int SLOT_SIZE = 30;
    private static final int OUTLINE_EXPAND = 2;
    private static final int OUTLINE_COLOR = 0xFF00CC44;
    private static final int OUTLINE_THICKNESS = 2;

    private static Button confirmButton = null;
    private static Button btnTrashSelected = null;
    private static Button btnShiny = null;
    private static Button btnLegendary = null;
    private static Button btnMythical = null;
    private static Button btnUltraBeast = null;
    private static Button btnHA = null;
    private static Button btnTextured = null;
    private static Button btnL100 = null; 
    private static Button btnClearAll = null;

    private static int currentTargetBox = 0;
    private static int currentViewedBox = 0;
    private static Screen currentScreen = null;
    private static boolean lastSearchOpen = false;

    private static Method methodIsVisible = null;

    private static boolean isTypeMenuOpen = false;
    private static int typeScrollOffset = 0;
    public static final List<String> activeTypes = new ArrayList<>();
    
    // CHANGED: Instead of a list of tags, we only allow ONE active tag at a time!
    public static String activeTag = "";
    
    private static Button btnTypeMain = null;
    private static final List<Button> typeButtons = new ArrayList<>();
    private static int menuX = 0;
    private static int menuY = 0;

    private static final String[] ALL_TYPES = {
        "Bug", "Dark", "Dragon", "Electric", "Fairy", "Fighting", "Fire", "Flying",
        "Ghost", "Grass", "Ground", "Ice", "Normal", "Poison", "Psychic", "Rock", "Steel", "Water"
    };

    @SubscribeEvent
    public static void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        Screen screen = event.getGui();
        if (!(screen instanceof PCScreen)) return;

        currentScreen = screen;
        int sw = screen.width;
        int sh = screen.height;

        cacheTextFieldMethods();

        confirmButton = new Button(sw / 2 - 60, sh - 30, 120, 20,
            new StringTextComponent("Confirm (0) Selected"), button -> onConfirmClicked());
        confirmButton.visible = false;
        event.addWidget(confirmButton);

        btnTrashSelected = new Button(sw / 2 + 70, sh - 30, 100, 20,
            new StringTextComponent(TextFormatting.RED + "Trash Selected"), button -> onTrashClicked());
        btnTrashSelected.visible = false;
        event.addWidget(btnTrashSelected);

        int btnX = sw / 2 + 120;
        int btnY = sh / 2 - 80;
        int btnW = 80;
        int btnH = 16;
        int btnGap = 20;

        btnShiny = new Button(btnX, btnY, btnW, btnH, new StringTextComponent("@shiny"), b -> toggleTag("@shiny"));
        btnShiny.visible = false; event.addWidget(btnShiny);

        btnLegendary = new Button(btnX, btnY + btnGap, btnW, btnH, new StringTextComponent("@legendary"), b -> toggleTag("@legendary"));
        btnLegendary.visible = false; event.addWidget(btnLegendary);

        btnMythical = new Button(btnX, btnY + btnGap * 2, btnW, btnH, new StringTextComponent("@mythical"), b -> toggleTag("@mythical"));
        btnMythical.visible = false; event.addWidget(btnMythical);

        btnUltraBeast = new Button(btnX, btnY + btnGap * 3, btnW, btnH, new StringTextComponent("@ultrabeast"), b -> toggleTag("@ultrabeast"));
        btnUltraBeast.visible = false; event.addWidget(btnUltraBeast);

        btnHA = new Button(btnX, btnY + btnGap * 4, btnW, btnH, new StringTextComponent("@HA"), b -> toggleTag("@ha"));
        btnHA.visible = false; event.addWidget(btnHA);

        btnTextured = new Button(btnX, btnY + btnGap * 5, btnW, btnH, new StringTextComponent("@textured"), b -> toggleTag("@textured"));
        btnTextured.visible = false; event.addWidget(btnTextured);

        btnL100 = new Button(btnX, btnY + btnGap * 6, btnW, btnH, new StringTextComponent("L100"), b -> toggleTag("@L100"));
        btnL100.visible = false; event.addWidget(btnL100);

        btnClearAll = new Button(btnX, btnY + btnGap * 7, btnW, btnH, new StringTextComponent("Clear All"), b -> clearAllSelections());
        btnClearAll.visible = false; event.addWidget(btnClearAll);

        menuX = sw / 2 - 200; 
        menuY = sh / 2 - 80;

        btnTypeMain = new Button(menuX, menuY, 80, 16, new StringTextComponent("TYPES"), b -> {
            isTypeMenuOpen = !isTypeMenuOpen;
            updateTypeMenu();
        });
        btnTypeMain.visible = false;
        event.addWidget(btnTypeMain);

        typeButtons.clear();
        for (int i = 0; i < ALL_TYPES.length; i++) {
            String typeName = ALL_TYPES[i];
            Button tb = new Button(menuX, menuY + 16, 80, 16, new StringTextComponent(typeName), b -> {
                String clickedType = typeName.toLowerCase();
                if (activeTypes.contains(clickedType)) {
                    activeTypes.remove(clickedType);
                } else if (activeTypes.size() < 2) {
                    activeTypes.add(clickedType);
                }
                updateSearchBox(); 
                updateTypeMenu();
            });
            tb.visible = false;
            typeButtons.add(tb);
            event.addWidget(tb);
        }

        lastSearchOpen = false;
    }

    private static void cacheTextFieldMethods() {
        if (methodIsVisible != null) return;
        for (Method m : TextFieldWidget.class.getMethods()) {
            if (m.getParameterTypes().length == 0 && m.getReturnType() == boolean.class) {
                if (m.getName().equals("func_146176_q") || m.getName().equals("isVisible")) {
                    methodIsVisible = m;
                    methodIsVisible.setAccessible(true);
                }
            }
        }
    }

    private static boolean isTextFieldVisible(TextFieldWidget widget) {
        try {
            if (methodIsVisible != null) return (boolean) methodIsVisible.invoke(widget);
        } catch (Exception e) {}
        return false;
    }

    private static void updateTypeMenu() {
        for (int i = 0; i < typeButtons.size(); i++) {
            Button b = typeButtons.get(i);
            if (isTypeMenuOpen && i >= typeScrollOffset && i < typeScrollOffset + 6) {
                b.visible = true;
                b.y = menuY + 16 + ((i - typeScrollOffset) * 16);
                
                String typeName = ALL_TYPES[i];
                if (activeTypes.contains(typeName.toLowerCase())) {
                    b.setMessage(new StringTextComponent(TextFormatting.GREEN + typeName));
                } else {
                    b.setMessage(new StringTextComponent(typeName));
                }
            } else {
                b.visible = false;
            }
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(GuiScreenEvent.MouseScrollEvent.Pre event) {
        Screen screen = event.getGui();
        if (!(screen instanceof PCScreen) || !lastSearchOpen || !isTypeMenuOpen) return;

        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();

        if (mouseX >= menuX && mouseX <= menuX + 85 && mouseY >= menuY && mouseY <= menuY + 16 + (6 * 16)) {
            if (event.getScrollDelta() > 0 && typeScrollOffset > 0) {
                typeScrollOffset--;
                updateTypeMenu();
                event.setCanceled(true);
            } else if (event.getScrollDelta() < 0 && typeScrollOffset < ALL_TYPES.length - 6) {
                typeScrollOffset++;
                updateTypeMenu();
                event.setCanceled(true);
            }
        }
    }

    // CHANGED: This now replaces the active tag instead of stacking them!
    private static void toggleTag(String tag) {
        if (activeTag.equals(tag)) {
            activeTag = ""; // Clicking the same button turns it off
        } else {
            activeTag = tag; // Clicking a new button replaces the old one
        }
        updateSearchBox();
    }

    private static void updateSearchBox() {
        try {
            if (currentScreen == null) return;
            Field searchField = PCScreen.class.getDeclaredField("searchField");
            searchField.setAccessible(true);
            TextFieldWidget widget = (TextFieldWidget) searchField.get(currentScreen);
            
            if (widget != null) {
                // FIX: Stop Pixelmon from cutting off long combined searches!
                widget.setMaxLength(150); 
                
                StringBuilder sb = new StringBuilder();
                
                // Add the single active right-side tag
                if (!activeTag.isEmpty()) {
                    sb.append(activeTag).append(" ");
                }
                
                // Add the types
                if (!activeTypes.isEmpty()) {
                    sb.append("@type:").append(String.join(",", activeTypes));
                }
                
                widget.setValue(sb.toString().trim());
                
                // FIX: Force the text box to scroll back to the left so it doesn't say "egendary"
                widget.setCursorPosition(0); 
            }
        } catch (Exception e) {}
    }

    @SubscribeEvent
    public static void onGuiRender(GuiScreenEvent.DrawScreenEvent.Post event) {
        Screen screen = event.getGui();
        if (!(screen instanceof PCScreen)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;

        PCEventHandler handler = PCMod.getEventHandler();
        if (handler == null) return;

        MultiGrabHandler grabHandler = handler.getHandlerForClient();
        if (grabHandler == null) return;

        int count = grabHandler.getSelectionCount();
        
        if (confirmButton != null) {
            confirmButton.visible = count > 0;
            confirmButton.setMessage(new StringTextComponent("Confirm (" + count + ") Selected"));
        }
        
        if (btnTrashSelected != null) {
            btnTrashSelected.visible = count >= 2;
        }

        try {
            Field boxField = PokemonScreen.class.getDeclaredField("boxNumber");
            boxField.setAccessible(true);
            currentViewedBox = (int) boxField.get(screen);
            currentTargetBox = currentViewedBox;
        } catch (Exception e) {}

        boolean searchOpen = isPixelmonSearchOpen(screen);
        if (searchOpen != lastSearchOpen) {
            lastSearchOpen = searchOpen;
            setFilterButtonsVisible(searchOpen);
        }

        MatrixStack matrixStack = event.getMatrixStack();

        if (lastSearchOpen && isTypeMenuOpen) {
            int listY = menuY + 16;
            fill(matrixStack, menuX + 80, listY, menuX + 84, listY + (6 * 16), 0xFF111111);
            int scrollHeight = 6 * 16;
            int maxScroll = ALL_TYPES.length - 6;
            int nobHeight = Math.max(16, scrollHeight * 6 / ALL_TYPES.length);
            int nobY = listY + (int)(((double)typeScrollOffset / maxScroll) * (scrollHeight - nobHeight));
            fill(matrixStack, menuX + 80, nobY, menuX + 84, nobY + nobHeight, 0xFFAAAAAA);
        }

        List<Pokemon> selected = grabHandler.getSelectedPokemon();
        if (selected.isEmpty()) return;

        PCStorage pc = null;
        try {
            Field storageField = PokemonScreen.class.getDeclaredField("storage");
            storageField.setAccessible(true);
            pc = (PCStorage) storageField.get(screen);
        } catch (Exception e) { return; }

        Set<Integer> highlightedSlots = new HashSet<>();

        if (searchOpen) {
            PCBox box = pc.getBox(currentViewedBox);
            if (box != null) {
                for (int i = 0; i < 30; i++) {
                    Pokemon p = box.get(i);
                    if (p != null && grabHandler.isSelected(p)) {
                        highlightedSlots.add(i);
                    }
                }
            }
        } else {
            for (Pokemon p : selected) {
                StoragePosition pos = p.getPosition();
                if (pos != null && pos.box == currentViewedBox) {
                    highlightedSlots.add(pos.order);
                }
            }
        }

        if (highlightedSlots.isEmpty()) return;

        int pcLeft = 0;
        int pcTop = 0;
        try {
            Field leftField = PokemonScreen.class.getDeclaredField("pcLeft");
            leftField.setAccessible(true);
            pcLeft = (int) leftField.get(screen);

            Field topField = PokemonScreen.class.getDeclaredField("pcTop");
            topField.setAccessible(true);
            pcTop = (int) topField.get(screen);
        } catch (Exception e) { return; }

        Set<Long> selectedSet = new HashSet<>();
        for (int slot : highlightedSlots) {
            int col = slot % 6;
            int row = slot / 6;
            selectedSet.add(encodePos(col, row));
        }

        for (int slot : highlightedSlots) {
            int col = slot % 6;
            int row = slot / 6;
            int topRowFix = (row == 0) ? 6 : 0;
            int screenX = pcLeft + (col * SLOT_SIZE);
            int screenY = pcTop + (row * SLOT_SIZE) + topRowFix;
            drawMergedSlotEdges(matrixStack, screenX, screenY, col, row, selectedSet);
        }
    }

    @SubscribeEvent
    public static void onGuiClose(GuiScreenEvent.InitGuiEvent.Pre event) {
        Screen screen = event.getGui();
        if (!(screen instanceof PCScreen)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;

        MultiGrabHandler grabHandler = PCMod.getEventHandler().getHandlerForClient();
        if (grabHandler != null) grabHandler.clearSelection();

        confirmButton = null; btnTrashSelected = null; btnShiny = null; btnLegendary = null; 
        btnMythical = null; btnUltraBeast = null; btnHA = null; btnTextured = null; 
        btnL100 = null; btnClearAll = null; currentScreen = null; lastSearchOpen = false;
        
        btnTypeMain = null; typeButtons.clear();
        isTypeMenuOpen = false; typeScrollOffset = 0; activeTypes.clear(); activeTag = "";
    }

    private static String checkValuableSelected(List<Pokemon> selected) {
        for (Pokemon p : selected) {
            if (p.isLegendary()) return "Legendary";
            if (p.isMythical()) return "Mythical";
            if (p.isUltraBeast()) return "Ultra Beast";
            if (p.hasHiddenAbility()) return "Hidden Ability"; 
            if (p.getIVs().getTotal() == 186) return "100 IV";
            if (!p.getPalette().getName().equalsIgnoreCase("none") && !p.getPalette().getName().equalsIgnoreCase("default")) return "Textured";
        }
        return null;
    }

    private static void onTrashClicked() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        
        MultiGrabHandler grabHandler = PCMod.getEventHandler().getHandlerForClient();
        if (grabHandler == null || grabHandler.getSelectionCount() < 2) return;

        String valuable = checkValuableSelected(grabHandler.getSelectedPokemon());

        if (valuable != null) {
            mc.setScreen(new ConfirmScreen(
                (confirm) -> {
                    if (confirm) {
                        sendTrashPacketAndClear(grabHandler);
                    }
                    mc.setScreen(currentScreen);
                },
                new StringTextComponent(TextFormatting.DARK_RED + "WARNING"),
                new StringTextComponent("Warning! You are about to trash a " + valuable + " in this group. Please be aware that this deletion is permanent and cannot be undone."),
                new StringTextComponent("Continue"),
                new StringTextComponent("Cancel")
            ));
        } else {
            sendTrashPacketAndClear(grabHandler);
        }
    }

    private static void sendTrashPacketAndClear(MultiGrabHandler grabHandler) {
        PCMod.NETWORK.sendToServer(new TrashPacket(grabHandler.getSelectedTruePositions()));
        grabHandler.clearSelection();
        if (btnTrashSelected != null) btnTrashSelected.visible = false;
    }

    private static boolean isPixelmonSearchOpen(Screen screen) {
        try {
            Field searchField = PCScreen.class.getDeclaredField("searchField");
            searchField.setAccessible(true);
            TextFieldWidget widget = (TextFieldWidget) searchField.get(screen);
            if (widget == null) return false;
            return isTextFieldVisible(widget);
        } catch (Exception e) { return false; }
    }

    private static void setFilterButtonsVisible(boolean visible) {
        if (btnShiny != null) btnShiny.visible = visible;
        if (btnLegendary != null) btnLegendary.visible = visible;
        if (btnMythical != null) btnMythical.visible = visible;
        if (btnUltraBeast != null) btnUltraBeast.visible = visible;
        if (btnHA != null) btnHA.visible = visible;
        if (btnTextured != null) btnTextured.visible = visible;
        if (btnL100 != null) btnL100.visible = visible; 
        if (btnClearAll != null) btnClearAll.visible = visible;
        
        if (btnTypeMain != null) btnTypeMain.visible = visible;
        if (!visible) {
            isTypeMenuOpen = false;
            updateTypeMenu();
        }
    }

    private static void clearAllSelections() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null) return;
            MultiGrabHandler grabHandler = PCMod.getEventHandler().getHandlerForClient();
            if (grabHandler != null) grabHandler.clearSelection();
            
            // Clear everything!
            activeTypes.clear();
            activeTag = "";
            updateTypeMenu(); 
            
            if (currentScreen != null) {
                Field searchField = PCScreen.class.getDeclaredField("searchField");
                searchField.setAccessible(true);
                TextFieldWidget widget = (TextFieldWidget) searchField.get(currentScreen);
                if (widget != null) widget.setValue("");
            }
        } catch (Exception e) {}
    }

    private static void onConfirmClicked() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null) return;
            PCMod.getEventHandler().onConfirmMove(mc.player, currentTargetBox);
            if (confirmButton != null) confirmButton.visible = false;
            if (btnTrashSelected != null) btnTrashSelected.visible = false;
        } catch (Exception e) {}
    }

    public static void setCurrentBox(int boxIndex) {
        currentViewedBox = boxIndex;
        currentTargetBox = boxIndex;
    }

    public static int getCurrentBox() { return currentViewedBox; }
    public static void setTargetBox(int boxIndex) { currentTargetBox = boxIndex; }

    private static long encodePos(int col, int row) { return ((long) row << 16) | (col & 0xFFFF); }

    private static void drawMergedSlotEdges(MatrixStack matrixStack, int x, int y, int col, int row, Set<Long> selectedSet) {
        int ox = x - OUTLINE_EXPAND;
        int oy = y - OUTLINE_EXPAND;
        int ow = SLOT_SIZE + (OUTLINE_EXPAND * 2);
        int oh = SLOT_SIZE + (OUTLINE_EXPAND * 2);

        boolean hasTop    = selectedSet.contains(encodePos(col, row - 1));
        boolean hasBottom = selectedSet.contains(encodePos(col, row + 1));
        boolean hasLeft   = selectedSet.contains(encodePos(col - 1, row));
        boolean hasRight  = selectedSet.contains(encodePos(col + 1, row));

        if (!hasTop) fill(matrixStack, ox, oy, ox + ow, oy + OUTLINE_THICKNESS, OUTLINE_COLOR);
        if (!hasBottom) fill(matrixStack, ox, oy + oh - OUTLINE_THICKNESS, ox + ow, oy + oh, OUTLINE_COLOR);
        if (!hasLeft) fill(matrixStack, ox, oy, ox + OUTLINE_THICKNESS, oy + oh, OUTLINE_COLOR);
        if (!hasRight) fill(matrixStack, ox + ow - OUTLINE_THICKNESS, oy, ox + ow, oy + oh, OUTLINE_COLOR);
    }

    private static void fill(MatrixStack stack, int x1, int y1, int x2, int y2, int color) {
        Screen.fill(stack, x1, y1, x2, y2, color);
    }
}