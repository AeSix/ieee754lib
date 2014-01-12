package org.oldhen.ieee754lib.test;

import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.oldhen.ieee754lib.BitReader;
import org.oldhen.ieee754lib.IEEE754;
import org.oldhen.ieee754lib.IEEE754Format;
import org.oldhen.ieee754lib.IEEE754Standard;

public class Tester
{
	private static void printBit(BitReader r)
	{
		if (r.readBit())
		{
			System.out.print('1');
		}
		else
		{
			System.out.print('0');
		}
	}
	
	private static void printBits(IEEE754Format format, ByteBuffer buffer)
	{
		BitReader r = BitReader.wrap(buffer);
		printBit(r);
		System.out.print(' ');
		for (int i = 0; i < format.getExponentLength(); i++)
		{
			printBit(r);
		}
		System.out.print(' ');
		for (int i = 0; i < format.getMantissaLength(); i++)
		{
			printBit(r);
		}
		System.out.println();
		
	}
	
	private static void printBits(double v)
	{
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.asDoubleBuffer().put(v);
		printBits(IEEE754Standard.DOUBLE, buffer);
	}
	
	private static void printBits(float v)
	{
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.asFloatBuffer().put(v);
		printBits(IEEE754Standard.SINGLE, buffer);
	}
	
	private static void test(double value)
	{
		System.out.println(value);
		printBits(value);
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.asDoubleBuffer().put(value);
		double test = IEEE754.decode(IEEE754Standard.DOUBLE, 
				BitReader.wrap(buffer)).toDouble();
		printBits(test);
		System.out.println();
		if (Double.isNaN(value))
		{
			Assert.assertTrue(Double.isNaN(test));
		}
		else
		{
			Assert.assertTrue(value == test);
		}
	}
	
	private static void test(float value)
	{
		System.out.println(value);
		printBits(value);
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.asFloatBuffer().put(value);
		float test = IEEE754.decode(IEEE754Standard.SINGLE, 
				BitReader.wrap(buffer)).toFloat();
		printBits(test);
		System.out.println();
		if (Float.isNaN(value))
		{
			Assert.assertTrue(Float.isNaN(test));
		}
		else
		{
			Assert.assertTrue(value == test);
		}
	}
	
	private static double getRandomDouble()
	{
		byte[] bytes = new byte[8];
		new Random().nextBytes(bytes);
		return ByteBuffer.wrap(bytes).asDoubleBuffer().get();
	}
	
	private static float getRandomFloat()
	{
		byte[] bytes = new byte[4];
		new Random().nextBytes(bytes);
		return ByteBuffer.wrap(bytes).asFloatBuffer().get();
	}
	
	@Test
	public void testDouble()
	{
		test(Double.MAX_VALUE);
		test(Double.MIN_VALUE);
		test(Double.MIN_NORMAL);
		test(Double.NaN);
		test(Double.NEGATIVE_INFINITY);
		test(Double.POSITIVE_INFINITY);
		test(getRandomDouble());
	}
	
	@Test
	public void testFloat()
	{
		test(Float.MAX_VALUE);
		test(Float.MIN_VALUE);
		test(Float.MIN_NORMAL);
		test(Float.NaN);
		test(Float.NEGATIVE_INFINITY);
		test(Float.POSITIVE_INFINITY);
		test(getRandomFloat());
	}
}
