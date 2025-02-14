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

package xaero.pac.common.server.parties.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class PartyCommandRegister {
	
	public static final String COMMAND_PREFIX = "openpac-parties";
	
	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		CommandRequirementProvider commandRequirementProvider = new CommandRequirementProvider();
		new CreatePartyCommand().register(dispatcher, environment, commandRequirementProvider);
		new LeavePartyCommand().register(dispatcher, environment, commandRequirementProvider);
		new DestroyPartyConfirmCommand().register(dispatcher, environment, commandRequirementProvider);
		new InvitePartyCommand().register(dispatcher, environment, commandRequirementProvider);
		new InviteAcceptPartyCommand().register(dispatcher, environment, commandRequirementProvider);
		new AboutPartyCommand().register(dispatcher, environment, commandRequirementProvider);
		new KickPartyCommand().register(dispatcher, environment, commandRequirementProvider);
		new AllyPartyCommand().register(dispatcher, environment, commandRequirementProvider);
		new UnallyPartyCommand().register(dispatcher, environment, commandRequirementProvider);
		new RankPartyCommand().register(dispatcher, environment, commandRequirementProvider);
		new MessagePartyCommand().register(dispatcher, environment, commandRequirementProvider);
		new TransferPartyCommand().register(dispatcher, environment, commandRequirementProvider);
	}

}
