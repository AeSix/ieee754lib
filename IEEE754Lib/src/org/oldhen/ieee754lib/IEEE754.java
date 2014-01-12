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

import java.math.BigInteger;
import java.nio.ByteBuffer;

public abstract class IEEE754
{
	private static class Constant extends IEEE754
	{
		private final boolean negative;
		private final boolean exponent;
		private final boolean mantissa;
		
		public Constant(boolean negative, boolean exponent, boolean mantissa)
		{
			this.negative = negative;
			this.exponent = exponent;
			this.mantissa = mantissa;
		}

		@Override
		protected void toBytesImpl(IEEE754Format format, BitWriter out)
		{
			out.writeBit(negative);
			for (int i = 0; i < format.getExponentLength(); i++)
			{
				out.writeBit(exponent);
			}
			out.writeBit(mantissa);
			for (int i = 1; i < format.getMantissaLength(); i++)
			{
				out.writeBit(false);
			}
		}
	}
	
	public static final IEEE754 POSITIVE_ZERO;
	public static final IEEE754 NEGATIVE_ZERO;
	public static final IEEE754 POSITIVE_INFINITY;
	public static final IEEE754 NEGATIVE_INFINITY;
	public static final IEEE754 NaN;
	
	static
	{
		POSITIVE_ZERO = new Constant(false, false, false);
		NEGATIVE_ZERO = new Constant(true, false, false);
		POSITIVE_INFINITY = new Constant(false, true, false);
		NEGATIVE_INFINITY = new Constant(true, true, false);
		NaN = new Constant(false, true, true);
	}
	
	private static class IEEE754Number extends IEEE754
	{
		private final boolean negative;
		private final BigInteger exponent;
		private final BigInteger mantissa;
		
		public IEEE754Number(boolean negative, BigInteger exponent,
				BigInteger mantissa)
		{
			this.negative = negative;
			this.exponent = exponent;
			this.mantissa = mantissa;
		}

		@Override
		protected void toBytesImpl(IEEE754Format format, BitWriter out)
		{
			int significandLength = mantissa.bitLength() - 1;
			BigInteger exponentBits = exponent.add(format.getExponentBias())
					.add(BigInteger.valueOf(significandLength));
			int mantissaBitIndex;
			if (exponentBits.compareTo(BigInteger.ZERO) <= 0)
			{
				if (BigInteger.valueOf(format.getMantissaLength())
						.add(exponentBits).compareTo(BigInteger.ZERO) <= 0)
				{
					IEEE754 zero = negative ? NEGATIVE_ZERO : POSITIVE_ZERO;
					zero.toBytesImpl(format, out);
					return;
				}
				mantissaBitIndex = significandLength - exponentBits.intValue();
				exponentBits = BigInteger.ZERO;
			}
			else
			{
				int exponentBitsLength = exponentBits.bitLength();
				if (exponentBitsLength > format.getExponentLength() || (
						exponentBitsLength == format.getExponentLength()
						&& exponentBitsLength == exponentBits.bitCount()))
				{
					throw new ArithmeticException(
							"IEEE754Format is too small to express number");
				}
				mantissaBitIndex = significandLength - 1;
			}
			
			out.writeBit(negative);
			for (int i = format.getExponentLength() - 1; i >= 0; i--)
			{
				out.writeBit(exponentBits.testBit(i));
			}
			for (int i = 0; i < format.getMantissaLength(); i++)
			{
				if (mantissaBitIndex < 0)
				{
					out.writeBit(false);
				}
				else
				{
					out.writeBit(mantissa.testBit(mantissaBitIndex));
					mantissaBitIndex--;
				}
			}
		}
	}
	
	private IEEE754() {}
	
	private static void assertValidFormat(IEEE754Format format)
	{
		if ((format.getExponentLength() + format.getMantissaLength() + 1) % 8 
				!= 0)
		{
			throw new IllegalArgumentException(
					"exponentLength + mantissaLength + 1 must be divisible by 8");
		}
	}
	
	protected abstract void toBytesImpl(IEEE754Format format, BitWriter out);
	
	public final void toBytes(IEEE754Format format, BitWriter out)
	{
		assertValidFormat(format);
		toBytesImpl(format, out);
	}
	
	public final double toDouble()
	{
		ByteBuffer buf = ByteBuffer.allocate(8);
		toBytesImpl(IEEE754Standard.DOUBLE, BitWriter.wrap(buf));
		buf.rewind();
		return buf.asDoubleBuffer().get();
	}
	
	public final float toFloat()
	{
		ByteBuffer buf = ByteBuffer.allocate(4);
		toBytesImpl(IEEE754Standard.SINGLE, BitWriter.wrap(buf));
		buf.rewind();
		return buf.asFloatBuffer().get();
	}
	
	@Override
	public final String toString()
	{
		return Double.toString(toDouble());
	}
	
	public static IEEE754 decode(IEEE754Format format, BitReader in)
	{
		assertValidFormat(format);
		boolean negative = in.readBit();
		BigInteger exponentBits = BigInteger.ZERO;
		for (int i = format.getExponentLength() - 1; i >= 0; i--)
		{
			if (in.readBit())
			{
				exponentBits = exponentBits.setBit(i);
			}
		}
		int exponentBitCount = exponentBits.bitCount();
		if (exponentBitCount == format.getExponentLength())
		{
			boolean nan = false;
			for (int i = 0; i < format.getMantissaLength(); i++)
			{
				if (in.readBit())
				{
					nan = true;
				}
			}
			return nan ? NaN : negative ? 
					NEGATIVE_INFINITY : POSITIVE_INFINITY;
		}
		
		BigInteger mantissa = BigInteger.ZERO;
		for (int i = format.getMantissaLength() - 1; i >= 0; i--)
		{
			if (in.readBit())
			{
				mantissa = mantissa.setBit(i);
			}
		}
		
		if (exponentBitCount == 0 && mantissa.bitCount() == 0)
		{
			return negative ? NEGATIVE_ZERO : POSITIVE_ZERO;
		}
		
		BigInteger exponent = BigInteger.valueOf(format.getMantissaLength())
				.negate();
		if (exponentBitCount == 0)
		{
			exponent = exponent.add(BigInteger.ONE);
		}
		else
		{
			mantissa = mantissa.setBit(format.getMantissaLength());
		}
		
		int mantissaRightTrimLength = mantissa.getLowestSetBit();
		if (mantissaRightTrimLength > 0)
		{
			mantissa = mantissa.shiftRight(mantissaRightTrimLength);
			exponent = exponent.add(
					BigInteger.valueOf(mantissaRightTrimLength));
		}
		
		exponent = exponent.add(exponentBits)
				.subtract(format.getExponentBias());
		return new IEEE754Number(negative, exponent, mantissa);
	}
	
}
