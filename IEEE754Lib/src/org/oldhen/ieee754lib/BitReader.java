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
import java.nio.channels.ReadableByteChannel;

public abstract class BitReader
{
	private static class BufferWrapper extends BitReader
	{
		private final ByteBuffer buffer;
		
		public BufferWrapper(ByteBuffer buffer)
		{
			this.buffer = buffer;
		}

		@Override
		public byte readByte()
		{
			return buffer.get();
		}
	}
	
	private static class ChannelWrapper extends BitReader
	{
		private final byte[] bytes;
		private final ByteBuffer buffer;
		private final ReadableByteChannel channel;
		
		public ChannelWrapper(ReadableByteChannel channel)
		{
			this.channel = channel;
			bytes = new byte[1];
			buffer = ByteBuffer.wrap(bytes);
		}
		
		@Override
		public byte readByte()
		{
			try
			{
				channel.read(buffer);
			}
			catch (IOException e)
			{
				throw new IllegalStateException(e);
			}
			buffer.rewind();
			return bytes[0];
		}
		
	}
	
	public static BitReader wrap(ByteBuffer buffer)
	{
		return new BufferWrapper(buffer);
	}
	
	public static BitReader wrap(ReadableByteChannel channel)
	{
		return new ChannelWrapper(channel);
	}
	
	private int currentByte;
	private int byteMask;
	
	public boolean readBit()
	{
		if (byteMask == 0x00)
		{
			byteMask = 0x80;
			currentByte = readByte() & 0xFF;
		}
		boolean result = (currentByte & byteMask) == byteMask;
		byteMask = byteMask >> 1;
		return result;
	}
	
	protected abstract byte readByte();
}
