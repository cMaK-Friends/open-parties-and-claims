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

package xaero.pac.client.claims;

import xaero.pac.client.claims.api.IClientClaimsManagerAPI;
import xaero.pac.client.claims.player.IClientPlayerClaimInfo;
import xaero.pac.client.claims.tracker.result.IClaimsManagerClaimResultTracker;
import xaero.pac.common.claims.IClaimsManager;
import xaero.pac.common.claims.player.IPlayerChunkClaim;

import javax.annotation.Nonnull;

public interface IClientClaimsManager
<
	C extends IPlayerChunkClaim, 
	PCI extends IClientPlayerClaimInfo<?>,
	WCM extends IClientDimensionClaimsManager<?>
> extends IClaimsManager<PCI, WCM>, IClientClaimsManagerAPI<PCI, WCM> {
	//internal API
	
	public void addClaimState(C claim);
	
	public int getLoadingClaimCount();
	
	public void setLoadingClaimCount(int loadingClaimCount);
	
	public int getLoadingForceloadCount();

	public boolean getAlwaysUseLoadingValues();
	
	public void setLoadingForceloadCount(int loadingForceloadCount);
	
	public void setClaimLimit(int claimLimit);
	
	public void setForceloadLimit(int forceloadLimit);

	public void setMaxClaimDistance(int maxClaimDistance);

	public void setCurrentSubConfigIndex(int currentSubConfigIndex);

	public void setCurrentServerSubConfigIndex(int currentServerSubConfigIndex);

	public void setCurrentSubConfigId(String currentSubConfigId);

	public void setCurrentServerSubConfigId(String currentServerSubConfigId);

	public IPlayerChunkClaim getPotentialClaimStateReflection();
	
	@Nonnull
	@Override
	public IClaimsManagerClaimResultTracker getClaimResultTracker();
	
}
