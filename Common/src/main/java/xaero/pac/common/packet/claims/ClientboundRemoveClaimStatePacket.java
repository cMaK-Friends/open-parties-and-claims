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

package xaero.pac.common.packet.claims;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypacket.LazyPacket;

import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundRemoveClaimStatePacket extends LazyPacket<LazyPacket.Encoder<ClientboundRemoveClaimStatePacket>, ClientboundRemoveClaimStatePacket> {

	public static final Encoder<ClientboundRemoveClaimStatePacket> ENCODER = new Encoder<>();

	private final int syncIndex;

	public ClientboundRemoveClaimStatePacket(int syncIndex) {
		super();
		this.syncIndex = syncIndex;
	}

	@Override
	protected void writeOnPrepare(Encoder<ClientboundRemoveClaimStatePacket> encoder, FriendlyByteBuf u) {
		CompoundTag tag = new CompoundTag();
		tag.putInt("i", syncIndex);
		u.writeNbt(tag);
	}

	@Override
	protected Encoder<ClientboundRemoveClaimStatePacket> getEncoder() {
		return ENCODER;
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundRemoveClaimStatePacket> {
		
		@Override
		public ClientboundRemoveClaimStatePacket apply(FriendlyByteBuf input) {
			try {
				CompoundTag tag = input.readNbt(new NbtAccounter(1024));
				if(tag == null)
					return null;
				int syncIndex = tag.getInt("i");
				return new ClientboundRemoveClaimStatePacket(syncIndex);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet ", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler implements Consumer<ClientboundRemoveClaimStatePacket> {
		
		@Override
		public void accept(ClientboundRemoveClaimStatePacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().onClaimStateRemoved(t.syncIndex);
		}
		
	}
	
}
