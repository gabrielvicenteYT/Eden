package rip.diamond.practice.kits.menu.button.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import rip.diamond.practice.Language;
import rip.diamond.practice.kits.Kit;
import rip.diamond.practice.kits.KitGameRules;
import rip.diamond.practice.kits.menu.KitDetailsMenu;
import rip.diamond.practice.profile.procedure.Procedure;
import rip.diamond.practice.profile.procedure.ProcedureType;
import rip.diamond.practice.util.Checker;
import rip.diamond.practice.util.ItemBuilder;
import rip.diamond.practice.util.menu.Button;

import java.lang.reflect.Field;

@RequiredArgsConstructor
public class KitRulesSetValueButton extends Button {

    private final KitDetailsMenu menu;
    private final Kit kit;
    private final Field field;

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.DOUBLE_PLANT)
                .durability(0)
                .name(Language.KIT_BUTTON_RULES_SET_VALUE_NAME.toString(getName()))
                .lore(Language.KIT_BUTTON_RULES_SET_VALUE_LORE.toStringList(KitGameRules.Readable.valueOf(field.getName()).getDescription(), getValue(player)))
                .build();
    }


    public String getName() {
        return KitGameRules.Readable.valueOf(field.getName()).getRule();
    }

    @SneakyThrows
    public int getValue(Player player) {
        return field.getInt(kit.getGameRules());
    }


    @Override
    public void clicked(Player player, ClickType clickType) {
        player.closeInventory();
        Procedure.buildProcedure(player, Language.KIT_BUTTON_RULES_SET_VALUE_PROCEDURE_MESSAGE.toString(getName()), ProcedureType.CHAT, (s) -> {
            String message = (String) s;
            if (!Checker.isInteger(message)) {
                Language.INVALID_SYNTAX.sendMessage(player);
                return;
            }
            int i = Integer.parseInt(message);

            try {
                field.setInt(kit.getGameRules(), i);
                Language.KIT_BUTTON_RULES_SET_VALUE_PROCEDURE_SUCCESS.sendMessage(player, getName(), i);
                menu.openMenu(player);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        Language.KIT_BUTTON_RULES_SET_VALUE_PROCEDURE_ADDITIONAL_MESSAGE.sendMessage(player);
    }
}
