package rip.diamond.practice.kits.menu;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import rip.diamond.practice.Language;
import rip.diamond.practice.kits.Kit;
import rip.diamond.practice.kits.menu.button.impl.*;
import rip.diamond.practice.util.menu.Button;
import rip.diamond.practice.util.menu.Menu;
import rip.diamond.practice.util.menu.button.BackButton;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class KitDetailsMenu extends Menu {
    private final Kit kit;
    private final Menu backMenu;

    @Override
    public String getTitle(Player player) {
        return Language.KIT_KIT_DETAIL_MENU_TITLE.toString(kit.getName());
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();
        if (backMenu != null) {
            buttons.put(0, new BackButton(Material.STAINED_GLASS_PANE, 14, backMenu));
        }
        buttons.put(3, new KitEditMatchTypeButton(kit, this));
        buttons.put(4, new KitPreviewButton(kit, this));
        buttons.put(5, new KitToggleButton(kit, this));
        buttons.put(9, new KitEditDisplayNameButton(kit));
        buttons.put(10, new KitEditDescriptionButton(kit));
        buttons.put(11, new KitEditIconButton(kit));
        buttons.put(12, new KitEditPriorityButton(kit));
        buttons.put(13, new KitEditPotionEffectButton(kit));
        buttons.put(14, new KitEditDamageTicksButton(kit));
        buttons.put(15, new KitToggleRankedButton(kit, this));
        buttons.put(16, new KitSaveLoadoutButton(kit));
        buttons.put(17, new KitEditExtraItemsButton(kit));
        for (int i = 0; i < 27; i++) {
            if (!buttons.containsKey(i)) {
                buttons.put(i, placeholderButton);
            }
        }

        for (Field field : kit.getGameRules().getClass().getDeclaredFields()) {
            if (field.getType() == boolean.class) {
                field.setAccessible(true);
                buttons.put(buttons.size(), new KitRulesToggleButton(this, kit, field));
            }
            if (field.getType() == int.class) {
                field.setAccessible(true);
                buttons.put(buttons.size(), new KitRulesSetValueButton(this, kit, field));
            }
        }

        return buttons;
    }
}
