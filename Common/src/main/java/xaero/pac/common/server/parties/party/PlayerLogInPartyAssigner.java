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

package xaero.pac.common.server.parties.party;

import net.minecraft.world.entity.player.Player;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;

public final class PlayerLogInPartyAssigner {
	
	public <
		M extends IPartyMember,
		I extends IPartyPlayerInfo,
		A extends IPartyAlly
	> void assign(IPartyManager<IServerParty<M,I,A>> partyManager, Player player, PartyPlayerInfoUpdater partyMemberInfoUpdater) {
		IServerParty<M,I,?> playerParty = partyManager.getPartyByMember(player.getUUID());
		if(playerParty != null) {
			partyMemberInfoUpdater.update(playerParty, playerParty.getMemberInfo(player.getUUID()), player.getGameProfile());
			((ServerParty) (Object)playerParty).registerActivity();
		}
	}

}
