/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU Lesser General Public License
 * (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received copies of the GNU Lesser General Public License
 * and the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.gui.widget.FixedEditBox;
import xaero.pac.client.world.capability.ClientWorldMainCapability;
import xaero.pac.client.world.capability.api.ClientWorldCapabilityTypes;

public class ConfigMenu extends XPACScreen {
	
	private static final TranslatableComponent ANOTHER_PLAYER_TITLE = new TranslatableComponent("gui.xaero_pac_ui_other_player_config_name_title");
	private boolean serverHasMod;
	private Button myPlayerConfigButton;
	private Button serverClaimsConfigButton;
	private Button expiredClaimsConfigButton;
	private Button wildernessConfigButton;
	private Button defaultConfigButton;
	private Button otherPlayerConfigButton;
	private EditBox otherPlayerNameBox;
	private static String otherPlayerNameString = "";

	public ConfigMenu(Screen escape, Screen parent) {
		super(escape, parent, new TranslatableComponent("gui.xaero_pac_ui_config_menu"));
	}
	
	@Override
	protected void init() {
		super.init();
		addRenderableWidget(myPlayerConfigButton = new Button(width / 2 - 100, height / 7 + 8, 200, 20, new TranslatableComponent("gui.xaero_pac_ui_my_player_config"), this::onPlayerConfigButton));
		addRenderableWidget(serverClaimsConfigButton = new Button(width / 2 - 100, height / 7 + 32, 200, 20, new TranslatableComponent("gui.xaero_pac_ui_server_claims_config"), this::onServerClaimsConfigButton));
		addRenderableWidget(expiredClaimsConfigButton = new Button(width / 2 - 100, height / 7 + 56, 200, 20, new TranslatableComponent("gui.xaero_pac_ui_expired_claims_config"), this::onExpiredClaimsConfigButton));
		addRenderableWidget(wildernessConfigButton = new Button(width / 2 - 100, height / 7 + 80, 200, 20, new TranslatableComponent("gui.xaero_pac_ui_wilderness_config"), this::onWildernessConfigButton));
		addRenderableWidget(defaultConfigButton = new Button(width / 2 - 100, height / 7 + 104, 200, 20, new TranslatableComponent("gui.xaero_pac_ui_default_player_config"), this::onDefaultConfigButton));
		addRenderableWidget(otherPlayerNameBox = new FixedEditBox(font, width / 2 - 99, height / 7 + 148, 98, 20, new TranslatableComponent("gui.xaero_pac_ui_other_player_config_name_field")));
		addRenderableWidget(otherPlayerConfigButton = new Button(width / 2, height / 7 + 148, 100, 20, new TranslatableComponent("gui.xaero_pac_ui_other_player_config_button"), this::onOtherPlayerConfigButton));
		updateOtherPlayerButton();
		ClientWorldMainCapability mainCap = (ClientWorldMainCapability) OpenPartiesAndClaims.INSTANCE.getCapabilityHelper().getCapability(minecraft.level, ClientWorldCapabilityTypes.MAIN_CAP);
		otherPlayerNameBox.setValue(otherPlayerNameString);
		otherPlayerNameBox.setResponder(s -> {otherPlayerNameString = s; updateOtherPlayerButton();});
		otherPlayerNameBox.setEditable(mainCap.getClientWorldData().serverHasMod() && minecraft.player.hasPermissions(2));
		addRenderableWidget(new Button(width / 2 - 100, this.height / 6 + 168, 200, 20, new TranslatableComponent("gui.xaero_pac_back"), this::onBackButton));

		serverHasMod = myPlayerConfigButton.active = serverClaimsConfigButton.active = mainCap.getClientWorldData().serverHasMod();
		expiredClaimsConfigButton.active =
				wildernessConfigButton.active = 
				defaultConfigButton.active = 
				mainCap.getClientWorldData().serverHasMod() && minecraft.player.hasPermissions(2);
		minecraft.keyboardHandler.setSendRepeatsToGui(true);
	}
	
	private void onBackButton(Button b) {
		goBack();
	}
	
	private void onPlayerConfigButton(Button b) {
		OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager().openMyPlayerConfigScreen(escape, this);
	}
	
	private void onServerClaimsConfigButton(Button b) {
		OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager().openServerClaimsConfigScreen(escape, this);
	}
	
	private void onExpiredClaimsConfigButton(Button b) {
		OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager().openExpiredClaimsConfigScreen(escape, this);
	}
	
	private void onWildernessConfigButton(Button b) {
		OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager().openWildernessConfigScreen(escape, this);
	}
	
	private void onDefaultConfigButton(Button b) {
		OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager().openDefaultPlayerConfigScreen(escape, this);
	}
	
	private boolean isPlayerNameAllowed() {
		return !otherPlayerNameString.isEmpty() && !otherPlayerNameString.equalsIgnoreCase(minecraft.player.getGameProfile().getName());
	}
	
	private void onOtherPlayerConfigButton(Button b) {
		if(isPlayerNameAllowed())
			minecraft.setScreen(new OtherPlayerConfigWaitScreen(escape, this, otherPlayerNameString));
	}
	
	private void updateOtherPlayerButton() {
		ClientWorldMainCapability mainCap = (ClientWorldMainCapability) OpenPartiesAndClaims.INSTANCE.getCapabilityHelper().getCapability(minecraft.level, ClientWorldCapabilityTypes.MAIN_CAP);
		otherPlayerConfigButton.active = mainCap.getClientWorldData().serverHasMod() && minecraft.player.hasPermissions(2) && isPlayerNameAllowed();
	}
	
	@Override
	public void tick() {
		super.tick();
		otherPlayerNameBox.tick();
	}
	
	@Override
	public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
		if(otherPlayerNameBox.isFocused() && (p_96552_ == GLFW.GLFW_KEY_ENTER || p_96552_ == GLFW.GLFW_KEY_KP_ENTER) && isPlayerNameAllowed()) {
			setFocused(null);
			otherPlayerConfigButton.onPress();
			return true;
		}
		return super.keyPressed(p_96552_, p_96553_, p_96554_);
	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partial) {
		renderBackground(poseStack);
		drawCenteredString(poseStack, font, title, width / 2, 16, -1);
		drawCenteredString(poseStack, font, ANOTHER_PLAYER_TITLE, width / 2, height / 7 + 132, -1);
		super.render(poseStack, mouseX, mouseY, partial);
		if(!serverHasMod)
			drawCenteredString(poseStack, font, MainMenu.NO_HANDSHAKE, width / 2, 27, 0xFFFF5555);
	}
	
	@Override
	public void setFocused(GuiEventListener l) {//setFocused
		GuiEventListener currentFocused = getFocused();
		if(currentFocused != null && currentFocused != l && currentFocused instanceof EditBox)
			((EditBox) currentFocused).setFocus(false);
		super.setFocused(l);
	}

}
