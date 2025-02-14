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

package xaero.pac.common.parties.party;

import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.api.IPartyAPI;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.parties.party.member.PartyMemberRank;

import java.util.UUID;

public interface IParty<M extends IPartyMember, I extends IPartyPlayerInfo, A extends IPartyAlly> extends IPartyAPI<M,I,A> {
	
	//internal api

	public boolean changeOwner(UUID newOwnerId, String newOwnerUsername);

	public M addMember(UUID playerUUID, PartyMemberRank rank, String playerUsername);
	
	public M removeMember(UUID playerUUID);
	
	public void addAllyParty(UUID partyId);
	
	public void removeAllyParty(UUID partyId);
	
	public I invitePlayer(UUID playerUUID, String playerUsername);
	
	public I uninvitePlayer(UUID playerUUID);

}
