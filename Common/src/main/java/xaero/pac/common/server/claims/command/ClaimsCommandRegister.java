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

package xaero.pac.common.server.claims.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ClaimsCommandRegister {
	
	public static final String COMMAND_PREFIX = "openpac-claims";
	
	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		new ClaimsClaimCommand().register(dispatcher, environment);
		new ClaimsUnclaimCommand().register(dispatcher, environment);
		new ClaimsForceloadCommand().register(dispatcher, environment);
		new ClaimsUnforceloadCommand().register(dispatcher, environment);
		new ClaimsNonAllyModeCommand().register(dispatcher, environment);
		new ClaimsAboutCommand().register(dispatcher, environment);
		new ClaimsServerModeCommand().register(dispatcher, environment);
		new ClaimsSubClaimCurrentCommand().register(dispatcher, environment);
		new ClaimsSubClaimUseCommand().register(dispatcher, environment);
		
		//op commands
		new ClaimsServerClaimCommand().register(dispatcher, environment);
		new ClaimsServerUnclaimCommand().register(dispatcher, environment);
		new ClaimsServerForceloadCommand().register(dispatcher, environment);
		new ClaimsServerUnforceloadCommand().register(dispatcher, environment);
		new ClaimsAdminModeCommand().register(dispatcher, environment);
	}

}
