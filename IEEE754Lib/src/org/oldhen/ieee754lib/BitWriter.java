/*
 * Copyright (C) 2014  Glenn Lane
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package org.oldhen.ieee754lib;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public abstract class BitWriter
{
	private static class BufferWrapper extends BitWriter
	{
		private final ByteBuffer buffer;
		
		public BufferWrapper(ByteBuffer buffer)
		{
			this.buffer = buffer;
		}

		@Override
		protected void writeByte(byte b)
		{
			buffer.put(b);
		}
	}
	
	private static class ChannelWrapper extends BitWriter
	{
		private final WritableByteChannel channel;
		private final byte[] bytes;
		private final ByteBuffer buffer;
		
		public ChannelWrapper(WritableByteChannel channel)
		{
			this.channel = channel;
			bytes = new byte[1];
			buffer = ByteBuffer.wrap(bytes);
		}
		
		@Override
		protected void writeByte(byte b)
		{
			bytes[0] = b;
			try
			{
				channel.write(buffer);
			}
			catch (IOException e)
			{
				throw new IllegalStateException(e);
			}
			buffer.rewind();
		}
	}
	
	public static BitWriter wrap(ByteBuffer buffer)
	{
		return new BufferWrapper(buffer);
	}
	
	public static BitWriter wrap(WritableByteChannel channel)
	{
		return new ChannelWrapper(channel);
	}
	
	private int currentByte;
	private int byteMask;
	
	public BitWriter()
	{
		init();
	}
	
	private void init()
	{
		currentByte = 0x00;
		byteMask = 0x80;
	}
	
	public final void writeBit(boolean bit)
	{
		if (bit)
		{
			currentByte |= byteMask;
		}
		if (byteMask == 0x01)
		{
			writeByte((byte) currentByte);
			init();
		}
		else
		{
			byteMask = byteMask >> 1;
		}
	}
	
	protected abstract void writeByte(byte b);
}