package ieee754lib;

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
