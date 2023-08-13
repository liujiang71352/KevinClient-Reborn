package kevin.persional.milk.guis.clickgui;

import kevin.persional.milk.guis.clickgui.buttons.BooleanButton;
import kevin.persional.milk.guis.clickgui.buttons.Button;
import kevin.persional.milk.guis.clickgui.buttons.FloatButton;
import kevin.persional.milk.guis.clickgui.buttons.IntegerButton;
import kevin.persional.milk.guis.clickgui.buttons.ListButton;
import kevin.persional.milk.guis.font.CFontRenderer;
import kevin.persional.milk.guis.font.FontLoaders;
import kevin.persional.milk.utils.key.ClickUtils;
import kevin.module.*;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;

public class ModulePanel {
    Module module;
    ArrayList<kevin.persional.milk.guis.clickgui.buttons.Button> buttons;
    boolean waitingForKey = false;
    public ModulePanel(Module module){
        this.module = module;
        this.buttons = new ArrayList<>();
        for(Value<?> value : module.getValues()){
            if(value instanceof ListValue){
                buttons.add(new ListButton((ListValue) value));
            }
            if(value instanceof IntegerValue){
                buttons.add(new IntegerButton((IntegerValue) value));
            }
            if(value instanceof FloatValue){
                buttons.add(new FloatButton((FloatValue) value));
            }
            if(value instanceof BooleanValue){
                buttons.add(new BooleanButton((BooleanValue) value));
            }
        }
    }
    public void drawPanel(int x, int y, int mx, int my, float pticks, float anim, float scroll){
        CFontRenderer font = FontLoaders.novo18;
        int ia = (int)(255 * anim);
        FontLoaders.novo18.drawString("<", x + 52, y + 3, new Color(90, 90, 90, ia).getRGB());
        FontLoaders.novo22.drawString(module.getName(), x + 60, y + 13, new Color(255, 255, 255, ia).getRGB());

        Color color = module.getState() ? new Color(22, 107, 22, ia) : new Color(161, 29, 29, ia);
        Color color2 = module.getState() ? new Color(39, 206, 39, ia) : new Color(232, 42, 42, ia);
        Gui.drawRect(x + 300, y + 33, x + 320, y + 36, color.getRGB());
        if(module.getState())
            Gui.drawRect(x + 320 - 4, y + 33 - 4, x + 320 + 4, y + 36 + 4, color2.getRGB());
        else
            Gui.drawRect(x + 300 - 4, y + 33 - 4, x + 300 + 4, y + 36 + 4, color2.getRGB());
        String keyname = module.getKeyBind() == -1 ? "None" : Keyboard.getKeyName(module.getKeyBind());
        if(waitingForKey) keyname = "press... click again for NONE";
        font.drawString("Bind key: " + keyname, x + 60, y + 25, new Color(255, 255, 255, ia).getRGB());
        int oy = (int) scroll;
        for(kevin.persional.milk.guis.clickgui.buttons.Button button : buttons){
            button.drawButton(x + 60, y + 50 + oy, mx, my, pticks, anim);
            oy += 13 + button.add;
        }
    }
    public void keyType(int key){
        if(waitingForKey){
            module.setKeyBind(key);
            waitingForKey = false;
        }
    }
    public void clickPanel(int x, int y, int mx, int my, float scroll){
        if(ClickUtils.isClickable(x + 60, y + 25, x + 100, y + 33, mx, my)){
            if(!waitingForKey) {
                waitingForKey = true;
            }else{
                waitingForKey = false;
                module.setKeyBind(-1);
            }
        }
        if(ClickUtils.isClickable(x + 300, y + 33, x + 320, y + 36, mx, my)){
            module.toggle();
            return;
        }
        int oy = (int) scroll;
        for(Button button : buttons){
            button.clickButton(x + 60, y + 50 + oy, mx, my);
            oy += 13 + button.add;
        }
    }
}
